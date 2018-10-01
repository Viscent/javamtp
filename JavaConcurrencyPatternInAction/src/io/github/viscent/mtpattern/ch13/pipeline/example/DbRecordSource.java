package io.github.viscent.mtpattern.ch13.pipeline.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DbRecordSource implements RecordSource {
    private final ResultSet rs;

    public DbRecordSource(Properties config) throws Exception {
        Connection cnn = getConnection(config);
        this.rs = qryRecords(cnn);
    }
    

    @Override
    public void close() throws IOException {
        try (Statement stmt = rs.getStatement();
                Connection cnn = stmt.getConnection();) {
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean hasNext() {
        boolean isNotLast = true;
        try {
            isNotLast = !rs.isLast();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isNotLast;
    }


    @Override
    public Record next() {
        Record record = null;
        try {
            rs.next();
            record = makeRecordFrom(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return record;
    }

    private ResultSet qryRecords(Connection dbConn) throws Exception {
        dbConn.setReadOnly(true);
        PreparedStatement ps = dbConn.prepareStatement(
                "select id,productId,packageId,msisdn,operationTime,operationType,"
                        + "effectiveDate,dueDate from subscriptions order by operationTime",
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = ps.executeQuery();
        return rs;
    }

    protected Connection getConnection(Properties props) throws Exception {
        Connection dbConn = null;
        dbConn = DriverManager.getConnection(props.getProperty("jdbc.url"),
                props.getProperty("jdbc.username"),
                props.getProperty("jdbc.password"));
        return dbConn;
    }

    private static Record makeRecordFrom(ResultSet rs) throws SQLException {
        Record record = new Record();
        record.setId(rs.getInt("id"));
        record.setProductId(rs.getString("productId"));
        record.setPackageId(rs.getString("packageId"));
        record.setMsisdn(rs.getString("msisdn"));
        record.setOperationTime(rs.getTimestamp("operationTime"));
        record.setOperationType(rs.getInt("operationType"));
        record.setEffectiveDate(rs.getTimestamp("effectiveDate"));
        record.setDueDate(rs.getTimestamp("dueDate"));
        return record;
    }
}
