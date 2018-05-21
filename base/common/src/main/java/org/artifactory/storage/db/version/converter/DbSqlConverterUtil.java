package org.artifactory.storage.db.version.converter;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.jfrog.common.ResourceUtils;
import org.jfrog.storage.DbType;
import org.jfrog.storage.util.DbStatementUtils;
import org.jfrog.storage.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author Dan Feldman
 */
public class DbSqlConverterUtil {
    private static final Logger log = LoggerFactory.getLogger(DbSqlConverterUtil.class);

    public static void convert(Connection conn, DbType dbType, String fromVersion) {
        try {
            String dbTypeName = getDbTypeNameForSqlResources(dbType);
            String resourcePath = null;
            String conversionDdl = null;
            if (ConstantValues.allowExternalConversionScripts.getBoolean()) {
                // Check whether the user provided his own conversion script for this conversion
                resourcePath = getConversionScriptPath(fromVersion, dbTypeName, true);
                if (isNotBlank(resourcePath)) {
                    conversionDdl = readUserProvidedConversionScript(resourcePath);
                }
            }
            // No external script, go ahead with reading the default classpath conversion resource
            if (isBlank(resourcePath)) {
                resourcePath = getConversionScriptPath(fromVersion, dbTypeName, false);
                try (InputStream resource = ResourceUtils.getResource(resourcePath)) {
                    conversionDdl = IOUtils.toString(resource);
                }
            }

            if (conversionDdl == null) {
                throw new IllegalStateException("Database DDL resource not found at: '" + resourcePath + "'");
            } else if (conversionDdl.equals("")) {
                // Nothing left to do here, this is an empty conversion script
                return;
            }

            try (InputStream conversionScriptStream = new ByteArrayInputStream(conversionDdl.getBytes(Charsets.UTF_8))) {
                // Execute the conversion
                doConvert(conn, conversionScriptStream, resourcePath);
            }
        } catch (SQLException | IOException e) {
            String msg = "Could not convert DB using " + fromVersion + " converter";
            log.error(msg + " due to " + e.getMessage(), e);
            throw new RuntimeException(msg, e);
        }
    }

    private static void doConvert(Connection conn, InputStream conversionScriptStream, String resourcePath) throws IOException, SQLException {
        log.info("Starting schema conversion: {}", resourcePath);
        DbStatementUtils.executeSqlStream(conn, conversionScriptStream);
        log.info("Finished schema conversion: {}", resourcePath);
    }

    /**
     * Gets the DDL path based on whether it is {@param external} or not. External path will be returned only if it exits
     * @param external - indicates whether the resource should be read from the Artifactory home dir or as a classpath
     * resource
     */
    private static String getConversionScriptPath(String fromVersion, String dbTypeName, boolean external) {
        if (external) {
            // Should always be ArtifactoryHome bound at this point
            String conversionScriptPath = ArtifactoryHome.get().getExternalConversionsDir().toPath() + "/" +
                    dbTypeName + "_" + fromVersion + ".sql";
            return new File(conversionScriptPath).exists() ? conversionScriptPath : null;
        } else {
            // classpath resource
            return "/conversion/" + dbTypeName + "/" + dbTypeName + "_" + fromVersion + ".sql";
        }
    }

    private static String readUserProvidedConversionScript(String resourcePath) throws IOException {
        File externalConversionScript = new File(resourcePath);
        if (!externalConversionScript.exists()) {
            return null;
        }
        // Note: we support empty external conversion scripts to allow "skipping" of real conversion files
        StringBuilder conversionFileContent = new StringBuilder("");
        try (FileReader reader = new FileReader(externalConversionScript)) {
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (isNotBlank(line)) {
                    DropStatementValidator.validate(line);
                    conversionFileContent.append(line);
                    conversionFileContent.append(System.lineSeparator());
                }
            }
            return StringUtils.chomp(conversionFileContent.toString());
        } catch (IOException e) {
            String err = "Unable to read conversion script file";
            log.error(err, e);
            // The startup bound to this conversion will fail. If this is the first conversion in line, the user can
            // rename the script file back to its original name, restart, and let the conversion re-run.
            throw new RuntimeException(err, e);
        } finally {
            File externalConversionScriptBackup = new File(resourcePath + "." + System.currentTimeMillis() +
                    ".bak");
            // Rename this file immediately to avoid an erroneous re-conversion attempt
            log.info("Renaming the external conversion script file to: " + externalConversionScriptBackup.toPath());
            Files.move(externalConversionScript.toPath(), externalConversionScriptBackup.toPath());
        }
    }

    public static String getDbTypeNameForSqlResources(DbType dbType) {
        return DbUtils.getDbTypeNameForSqlResources(dbType);
    }

    private static class DropStatementValidator {
        private static List<String> illegalQueries = Arrays.asList("drop table", "drop database");

        public static void validate(String query) {
            for (String illegalQuery : illegalQueries) {
                if (query.toLowerCase().contains(illegalQuery)) {
                    log.error("Offending DROP query with the following SQL syntax found while reading an externalized" +
                            " conversion script: " + query);
                    throw new RuntimeException("DROP DATABASE and DROP TABLE statements are forbidden inside" +
                            " external conversion files. Correct the conversion script or contact Support " +
                            "for help");
                }
            }
        }
    }
}
