package fi.ubigu.gsdig.utility;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBC {

    public static final StatementPreparer NOP = __ -> {};

    private static final Logger LOG = LoggerFactory.getLogger(JDBC.class);

    public static <T> List<T> findAll(DataSource ds, String select, RowMapper<T> rowMapper) throws Exception {
        try (Connection c = ds.getConnection()) {
            return findAll(c, select, rowMapper);
        }
    }

    public static <T> List<T> findAll(Connection c, String select, RowMapper<T> rowMapper) throws Exception {
        return findAll(c, select, NOP, rowMapper);
    }

    public static <T> List<T> findAll(DataSource ds, String select, StatementPreparer preparer, RowMapper<T> rowMapper) throws Exception {
        try (Connection c = ds.getConnection()) {
            return findAll(c, select, preparer, rowMapper);
        }
    }

    public static <T> List<T> findAll(Connection c, String select, StatementPreparer preparer, RowMapper<T> rowMapper) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(select)) {
            preparer.prepare(ps);
            LOG.debug("{}", ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> list = new ArrayList<>();
                while (rs.next()) {
                    T t = rowMapper.map(rs);
                    if (t != null) {
                        list.add(t);
                    }
                }
                return list;
            }
        }
    }

    public static <T> Optional<T> findFirst(DataSource ds, String select, RowMapper<T> rowMapper) throws Exception {
        return findFirst(ds, select, NOP, rowMapper);
    }

    public static <T> Optional<T> findFirst(DataSource ds, String select, StatementPreparer preparer, RowMapper<T> rowMapper) throws Exception {
        try (Connection c = ds.getConnection()) {
            return findFirst(c, select, preparer, rowMapper);
        }
    }

    public static <T> Optional<T> findFirst(Connection c, String select, RowMapper<T> rowMapper) throws Exception {
        return findFirst(c, select, NOP, rowMapper);
    }

    public static <T> Optional<T> findFirst(Connection c, String select, StatementPreparer preparer, RowMapper<T> rowMapper) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(select)) {
            preparer.prepare(ps);
            LOG.debug("{}", ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.ofNullable(rowMapper.map(rs)) : Optional.empty();
            }
        }
    }

    public static <T> T findResultset(DataSource ds, String select, StatementPreparer preparer, RowMapper<T> resultSetMapper) throws Exception {
        try (Connection c = ds.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(select)) {
                preparer.prepare(ps);
                LOG.debug("{}", ps);
                try (ResultSet rs = ps.executeQuery()) {
                    return resultSetMapper.map(rs);
                }
            }
        }
    }

    public static int executeUpdate(DataSource ds, String sql) throws Exception {
        return executeUpdate(ds, sql, NOP);
    }

    public static int executeUpdate(Connection c, String sql) throws Exception {
        return executeUpdate(c, sql, NOP);
    }

    public static int executeUpdate(DataSource ds, String sql, StatementPreparer preparer) throws Exception {
        try (Connection c = ds.getConnection()) {
            return executeUpdate(c, sql, preparer);
        }
    }

    public static int executeUpdate(Connection c, String sql, StatementPreparer preparer) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            preparer.prepare(ps);
            LOG.debug("{}", ps);
            return ps.executeUpdate();
        }
    }

    public static Date toSQLDate(LocalDate date) {
        return date == null ? null : Date.valueOf(date);
    }

    public static LocalDate fromSQLDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    public static Timestamp toSQLTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    public static Instant fromSQLTimestamp(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    public static Long getLong(ResultSet rs, int i) throws SQLException {
        final long v = rs.getLong(i);
        return v == 0 && rs.wasNull() ? null : v;
    }

}
