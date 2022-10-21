package fi.ubigu.gsdig.joins;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.ubigu.gsdig.utility.JDBC;

@Component
public class JobRepositoryPG implements JobRepository {

    private static final String TABLE = "job";
    private static final String SELECT = "SELECT "
            + "uuid,"
            + "created_by,"
            + "title,"
            + "description,"
            + "areal_division,"
            + "area_attributes,"
            + "unit_dataset,"
            + "data_attributes,"
            + "additional_grouping_property,"
            + "status,"
            + "message,"
            + "created,"
            + "updated,"
            + "started,"
            + "finished"
            + " FROM " + TABLE;

    @Autowired
    private DataSource ds;

    @Autowired
    private ObjectMapper om;

    @Override
    public JoinJob create(JoinRequest request, UUID userId) throws Exception {
        Instant now = Instant.now();
        JoinJob job = new JoinJob();
        job.setUuid(UUID.randomUUID());
        job.setRequest(request);
        job.setCreated(now);
        job.setUpdated(now);
        job.setStatus(StatusCode.accepted);
        insert(job, userId);
        return job;

    }

    @Override
    public List<JoinJob> findAcceptedJobsByUnitDataset(UUID unitDataset) throws Exception {
        return JDBC.findAll(ds,
                SELECT + " WHERE unit_dataset = ? AND status = ?", 
                ps -> {
                    ps.setObject(1, unitDataset, Types.OTHER);
                    ps.setString(2, StatusCode.accepted.name());
                },
                this::parse
        );
    }

    @Override
    public Optional<JoinJob> findByUuid(UUID jobId) throws Exception {
        return JDBC.findFirst(ds,
                SELECT + " WHERE uuid = ?",
                ps -> ps.setObject(1, jobId, Types.OTHER),
                this::parse
        );
    }
    
    private JoinJob parse(ResultSet rs) throws Exception {
        ObjectReader stringListReader = om.readerForListOf(String.class);
        ObjectReader dataAttributeReader = om.readerForListOf(JoinAttribute.class);

        int i = 1;

        JoinJob d = new JoinJob();
        JoinRequest r = new JoinRequest();
        d.setRequest(r);

        d.setUuid(rs.getObject(i++, UUID.class));
        d.setCreatedBy(rs.getObject(i++, UUID.class));
        
        r.setTitle(rs.getString(i++));
        r.setDescription(rs.getString(i++));
        
        r.setArealDivision(rs.getObject(i++, UUID.class));
        r.setAreaAttributes(stringListReader.readValue(rs.getString(i++)));
        
        r.setUnitDataset(rs.getObject(i++, UUID.class));
        r.setDataAttributes(dataAttributeReader.readValue(rs.getString(i++)));

        r.setAdditionalGroupingProperty(rs.getString(i++));

        d.setStatus(StatusCode.valueOf(rs.getString(i++)));
        d.setMessage(rs.getString(i++));
        
        d.setCreated(JDBC.fromSQLTimestamp(rs.getTimestamp(i++)));
        d.setUpdated(JDBC.fromSQLTimestamp(rs.getTimestamp(i++)));
        d.setStarted(JDBC.fromSQLTimestamp(rs.getTimestamp(i++)));
        d.setFinished(JDBC.fromSQLTimestamp(rs.getTimestamp(i++)));
        
        return d;
    }

    @Override
    public Optional<JoinJob> start(UUID jobId) throws Exception {
        int updated = updateStatus(jobId, StatusCode.accepted, StatusCode.running, "Job started", "started");
        if (updated == 1) {
             return findByUuid(jobId);
        }
        return Optional.empty();
    }

    @Override
    public Optional<JoinJob> finish(UUID jobId) throws Exception {
        int updated = updateStatus(jobId, StatusCode.running, StatusCode.succesful, "Job finished", "finished");
        if (updated == 1) {
             return findByUuid(jobId);
        }
        return Optional.empty();
    }

    @Override
    public Optional<JoinJob> error(UUID jobId, String message) throws Exception {
        int updated = updateStatus(jobId, StatusCode.running, StatusCode.failed, message, "finished");
        if (updated == 1) {
             return findByUuid(jobId);
        }
        return Optional.empty();
    }
    
    private void insert(JoinJob job, UUID userId) throws Exception {
        String insert = "INSERT INTO " + TABLE + " ("
                + "uuid,"
                + "created_by,"
                + "title,"
                + "description,"
                + "areal_division,"
                + "area_attributes,"
                + "unit_dataset,"
                + "data_attributes,"
                + "additional_grouping_property,"
                + "status,"
                + "message,"
                + "created,"
                + "updated,"
                + "started,"
                + "finished"
                + ") VALUES ("
                + "?,"
                + "?,"
                + "?,"
                + "?,"
                + "?,"
                + "?,"
                + "?,"
                + "?,"
                + "?,"
                + "?,"
                + "?,"
                + "?,"
                + "?,"
                + "?,"
                + "?)";
        JDBC.executeUpdate(ds, insert, ps -> {
            int i = 1;
            ps.setObject(i++, job.getUuid(), Types.OTHER);
            ps.setObject(i++, userId, Types.OTHER);
    
            ps.setString(i++, job.getRequest().getTitle());
            ps.setString(i++, job.getRequest().getDescription());
    
            ps.setObject(i++, job.getRequest().getArealDivision(), Types.OTHER);
            ps.setObject(i++, om.writeValueAsString(job.getRequest().getAreaAttributes()), Types.OTHER);
    
            ps.setObject(i++, job.getRequest().getUnitDataset(), Types.OTHER);
            ps.setObject(i++, om.writeValueAsString(job.getRequest().getDataAttributes()), Types.OTHER);
    
            ps.setString(i++, job.getRequest().getAdditionalGroupingProperty());
    
            ps.setString(i++, job.getStatus().name());
            ps.setString(i++, job.getMessage());
            
            ps.setTimestamp(i++, JDBC.toSQLTimestamp(job.getCreated()));
            ps.setTimestamp(i++, JDBC.toSQLTimestamp(job.getUpdated()));
            ps.setTimestamp(i++, JDBC.toSQLTimestamp(job.getStarted()));
            ps.setTimestamp(i++, JDBC.toSQLTimestamp(job.getFinished()));
        });
    }

    private int updateStatus(UUID jobId, StatusCode expectedStatus, StatusCode newStatus, String message, String timestampColumn) throws Exception {
        String update = "UPDATE " + TABLE + " SET status = ?, message = ?, " + timestampColumn + " = ?, updated = ? WHERE uuid = ? AND status = ?"; 
        return JDBC.executeUpdate(ds, update, ps -> {
            Timestamp now = Timestamp.from(Instant.now());
            int i = 1;
    
            ps.setString(i++, newStatus.name());
            ps.setString(i++, message);
            ps.setTimestamp(i++, now);
            ps.setTimestamp(i++, now);

            ps.setObject(i++, jobId, Types.OTHER);
            ps.setString(i++, expectedStatus.name());
        });
        
    }

}
