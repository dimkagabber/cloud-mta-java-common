package com.sap.cloud.lm.sl.persistence.changes;

import java.sql.SQLException;

import javax.sql.DataSource;

public interface AsyncChange {

    void execute(DataSource dataSource) throws SQLException;

}
