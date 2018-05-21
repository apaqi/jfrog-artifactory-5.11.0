package org.artifactory.storage.db;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class DbMetaData {
    private String productName;
    private String productVersion;
    private String driverName;
    private String driverVersion;
    private String url;

    DbMetaData(DatabaseMetaData metaData) throws SQLException {
        if (metaData == null) {
            return;
        }
        productName = metaData.getDatabaseProductName();
        productVersion = metaData.getDatabaseProductVersion();
        driverName = metaData.getDriverName();
        driverVersion = metaData.getDriverVersion();
        url = metaData.getURL();
    }

    public String getProductName() {
        return productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverVersion() {
        return driverVersion;
    }

    public String getURL() {
        return url;
    }
}
