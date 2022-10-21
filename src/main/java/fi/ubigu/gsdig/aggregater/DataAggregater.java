package fi.ubigu.gsdig.aggregater;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.io.WKBReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fi.ubigu.gsdig.arealdivision.ArealDivision;
import fi.ubigu.gsdig.arealdivision.ArealDivisionService;
import fi.ubigu.gsdig.arealdivision.AttributeInfo;
import fi.ubigu.gsdig.data.DataRepositoryPG;
import fi.ubigu.gsdig.joins.AggregateFunction;
import fi.ubigu.gsdig.joins.JobRepository;
import fi.ubigu.gsdig.joins.JoinAttribute;
import fi.ubigu.gsdig.joins.JoinJob;
import fi.ubigu.gsdig.unitdata.SensitivitySetting;
import fi.ubigu.gsdig.unitdata.UnitDataService;
import fi.ubigu.gsdig.unitdata.UnitDataset;
import fi.ubigu.gsdig.utility.JDBC;

@Component
@EnableAsync
public class DataAggregater {
    
    @Autowired
    private JobRepository jobRepo;
    
    @Autowired
    private ArealDivisionService arealDivisionService;
    
    @Autowired
    private UnitDataService unitDataService;
    
    @Autowired
    private DataSource ds;
    
    @Async
    @Scheduled(fixedRate = 5000, initialDelay = 5000)
    public void runAggregateTask() throws Exception {
        for (UnitDataset dataset : unitDataService.findAll()) {
            if (dataset.isRemote()) {
                continue;
            }
            for (JoinJob job : jobRepo.findAcceptedJobsByUnitDataset(dataset.getUuid())) {
                try {
                    handle(dataset, job);
                } catch (Exception e) {
                    e.printStackTrace();
                    jobRepo.error(job.getUuid(), e.getMessage());
                }
            }
        }
    }

    private void handle(UnitDataset dataset, JoinJob job) throws IllegalStateException, Exception {
        if (jobRepo.start(job.getUuid()).isEmpty()) {
            return;
        }
        
        UUID uuid = job.getUuid();
        UUID userId = job.getCreatedBy();
        UUID arealDivisionId = job.getRequest().getArealDivision();
        List<String> areaAttributes = job.getRequest().getAreaAttributes();
        List<JoinAttribute> dataAttributes = job.getRequest().getDataAttributes();
        String additionalGroupingProperty = job.getRequest().getAdditionalGroupingProperty();
        double[] envelope;
        
        ArealDivision arealDivision = arealDivisionService.findById(arealDivisionId).orElse(null);
        if (arealDivision == null) {
            jobRepo.error(job.getUuid(), "Unknown areal division");
            return;
        }

        Map<String, AttributeInfo> attributes = new LinkedHashMap<>();
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            
            createDataTable(c, uuid, arealDivisionId, dataset, dataAttributes, additionalGroupingProperty, attributes);

            if (additionalGroupingProperty != null) {
                groupByToColumns(c, uuid, dataset, dataAttributes, additionalGroupingProperty, attributes);
            }
            
            addPrimaryKey(c, uuid);
            
            joinArealDivision(c, uuid, arealDivisionId, areaAttributes, attributes.keySet());
            for (String areaAttribute : areaAttributes) {
                Map<String, AttributeInfo> swap = new LinkedHashMap<>();
                AttributeInfo info = arealDivision.getAttributes().get(areaAttribute);
                swap.put(areaAttribute, info);
                swap.putAll(attributes);
                attributes = swap;
            }

            envelope = selectEnvelope(c, uuid);
            
            c.commit();
        }
        
        boolean publicity = false;
        ArealDivision created = new ArealDivision(uuid, userId, job.getRequest().getTitle(), job.getRequest().getDescription(), null, publicity, envelope, attributes);

        arealDivisionService.create(created);
        jobRepo.finish(job.getUuid());
    }
    
    private void createDataTable(
            Connection c,
            UUID uuid,
            UUID arealDivisionId,
            UnitDataset unitDataset,
            List<JoinAttribute> dataAttributes,
            String additionalGroupingProperty,
            Map<String, AttributeInfo> attributesOut
    ) throws Exception {
        // CREATE UNLOGGED TABLE <table> AS
        // SELECT a.id <, additional grouping property>, count(b.*) AS _count <, rest of the fields from unit dataset>
        // FROM a
        // JOIN b 
        // ON ST_Intersects(a.geom, b.geom)  
        // GROUP BY a.id <, additional grouping property>
        // HAVING <restictions set by UnitDataset>
        
        List<String> columns = new ArrayList<>();
        columns.add("a.id");

        if (additionalGroupingProperty != null) {
            columns.add("b." + additionalGroupingProperty);
        }
        columns.add("count(b.*) AS _count");
        attributesOut.put("_count", new AttributeInfo("_count", Long.class));
        for (JoinAttribute attr : dataAttributes) {
            for (AggregateFunction f : attr.getAggregate()) {
                String column = String.format("%s(b.%s)", f.toString(), attr.getProperty()); 
                String alias = String.format("%s_%s", attr.getProperty(), f.toString().toLowerCase());
                columns.add(column + " AS " + alias);
                attributesOut.put(alias, new AttributeInfo(alias, Double.class));
            }
        }

        List<String> groupBy = new ArrayList<>();
        groupBy.add("a.id");
        if (additionalGroupingProperty != null) {
            groupBy.add("b." + additionalGroupingProperty);
        }
        
        String createTable = ""
                + "CREATE UNLOGGED TABLE " + DataRepositoryPG.getDataTable(uuid) + " AS "
                + "SELECT " + columns.stream().collect(Collectors.joining(","))
                + " FROM " + DataRepositoryPG.getDataTable(arealDivisionId) + " a"
                + " JOIN " + DataRepositoryPG.getDataTable(unitDataset.getUuid()) + " b"
                + " ON ST_Intersects(a.geom, b.geom)"
                + " GROUP BY " + groupBy.stream().collect(Collectors.joining(","));

        if (unitDataset.getSensitivitySetting() == null) {
            JDBC.executeUpdate(c, createTable);
        } else {
            // HAVING
            createTable = createTable + " HAVING ";
            SensitivitySetting ss = unitDataset.getSensitivitySetting();
            AggregateFunction agg = ss.getAggregate();
            double minValue = ss.getMinValue();
            String property = ss.getProperty();
            if (property == null) {
                property = "*";
            }
            createTable += String.format("%s(b.%s) >= ?", agg.toString(), property);
    
            JDBC.executeUpdate(c, createTable, ps -> {
                if (agg == AggregateFunction.COUNT) {
                    ps.setLong(1, (long) minValue);
                } else {
                    ps.setDouble(1, minValue);
                }
            });
        }
    }
    
    private void addPrimaryKey(Connection c, UUID uuid) throws Exception {
        String table = DataRepositoryPG.getDataTable(uuid);
        JDBC.executeUpdate(c, "ALTER TABLE " + table + " ADD PRIMARY KEY (id)");
    }

    private List<String> groupByToColumns(
            Connection c,
            UUID uuid,
            UnitDataset unitDataset,
            List<JoinAttribute> dataAttributes,
            String additionalGroupingProperty,
            Map<String, AttributeInfo> attributesOut
    ) throws Exception {
        attributesOut.clear();

        String selectGroupingValues = String.format("SELECT DISTINCT %s FROM %s",
                additionalGroupingProperty, DataRepositoryPG.getDataTable(unitDataset.getUuid()));
        List<String> groupingValues = JDBC.findAll(c, selectGroupingValues, rs -> rs.getString(1));
        
        String tableToCreate = DataRepositoryPG.getDataTable(uuid);
        String table = "t_" + tableToCreate;
        JDBC.executeUpdate(c, String.format("ALTER TABLE %s RENAME TO %s", tableToCreate, table));
        
        List<String> columns = new ArrayList<>();
        List<String> subselects = new ArrayList<>();
        
        columns.add("t1.id");
        
        int i = 2;
        for (String value : groupingValues) {
            String subselectTableAlias = "t" + i;

            List<String> subselectColumns = new ArrayList<>();
            subselectColumns.add("id");
            
            String countAlias = "_count_" + (value == null ? "null" : value);
            columns.add(subselectTableAlias + "." + countAlias);
            attributesOut.put(countAlias, new AttributeInfo(countAlias, Long.class));
            subselectColumns.add("_count AS " + countAlias);

            for (JoinAttribute attr : dataAttributes) {
                for (AggregateFunction f : attr.getAggregate()) {
                    String column = String.format("%s_%s",
                            attr.getProperty(), f.toString().toLowerCase());
                    String alias = String.format("%s_%s_%s",
                            attr.getProperty(), f.toString().toLowerCase(), value == null ? "null" : value);
                    columns.add(subselectTableAlias + "." + alias);
                    attributesOut.put(alias, new AttributeInfo(alias, Double.class));
                    subselectColumns.add(column + " AS " + alias);
                }
            }

            String subselect = ""
                    + "SELECT " + subselectColumns.stream().collect(Collectors.joining(","))
                    + " FROM " + table
                    + " WHERE " + additionalGroupingProperty + (value == null ? " IS NULL " : " = " + value);
            subselects.add(subselect);

            i++;
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE UNLOGGED TABLE " + tableToCreate + " AS");
        
        sql.append(" SELECT ").append(columns.stream().collect(Collectors.joining(",")));
        sql.append(" FROM (SELECT DISTINCT id FROM ").append(table).append(") t1");
        
        i = 2;
        for (String subselect : subselects) {
            String tableAlias = "t" + i++;
            sql.append(" LEFT JOIN (");
            sql.append(subselect);
            sql.append(") ").append(tableAlias);
            sql.append(" ON ").append("t1.id = ").append(tableAlias).append(".id");
        }
        
        String createTable = sql.toString();
    
        JDBC.executeUpdate(c, createTable);
        JDBC.executeUpdate(c, "DROP TABLE " + table);
        
        return groupingValues;
    }
    
    private void joinArealDivision(Connection c, UUID uuid, UUID arealDivisionId, List<String> areaAttributes, Set<String> aggregatedColumns) throws Exception {
        String tableToCreate = DataRepositoryPG.getDataTable(uuid);
        String tempTable = "t_" + tableToCreate;
        JDBC.executeUpdate(c, String.format("ALTER TABLE %s RENAME TO %s", tableToCreate, tempTable));
        
        List<String> areaColumns = new ArrayList<>(2 + areaAttributes.size());
        areaColumns.add("id");
        areaColumns.add(DataRepositoryPG.GEOM_COLUMN);
        areaColumns.addAll(areaAttributes);

        String columns = Stream.concat(
                areaColumns.stream().map(col -> "a." + col),
                aggregatedColumns.stream().map(col -> "b." + col)
        ).collect(Collectors.joining(","));

        String arealDivisionTable = DataRepositoryPG.getDataTable(arealDivisionId);

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE UNLOGGED TABLE " + tableToCreate + " AS");
        sql.append(" SELECT ").append(columns);
        sql.append(" FROM ").append(arealDivisionTable).append(" a ");
        sql.append(" LEFT JOIN ").append(tempTable).append(" b ");
        sql.append(" ON a.id = b.id");
        
        JDBC.executeUpdate(c, sql.toString());
        JDBC.executeUpdate(c, "DROP TABLE " + tempTable);
    }
    
    private double[] selectEnvelope(Connection c, UUID uuid) throws Exception {
        String table = DataRepositoryPG.getDataTable(uuid);
        String geom = DataRepositoryPG.GEOM_COLUMN;
        String select = "SELECT ST_AsBinary(ST_Extent(" + geom + ")) FROM " + table;
        return JDBC.findFirst(c, select, rs -> {
            byte[] wkb = rs.getBytes(1);
            Envelope env = new WKBReader().read(wkb).getEnvelopeInternal();
            double[] extent = {
                    env.getMinX(), env.getMinY(),
                    env.getMaxX(), env.getMaxY()
            };
            return extent;
        }).orElse(null);
        
    }

}
