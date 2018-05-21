package org.artifactory.aql.result;

import org.artifactory.aql.result.rows.AqlRowResult;

import java.sql.ResultSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Saffi Hartal 18-03-06
 * Autoclose stream of rows.
 * asStream provides the rows.
 * based on whileNextStream which keep return the container and calls next till exhousted.
 * A method onFinish recive exception or nulll on success - can be used to log errors.
 */
public interface AqlResultSetProvider<T extends AqlRowResult> extends AutoCloseable {
    ResultSet getResultSet();

    /**
     * return stream of this, and calls next()
     *
     * @param onFinish Consumer<Exception>
     * @return
     */
    default Stream<? extends AqlResultSetProvider<T>> whileNextStream(Consumer<Exception> onFinish) {
        return AqlResultHelper.whileNextStream(this, onFinish);
    }

    Stream<T> asStream(Consumer<Exception> onFinish);

    default void close() throws Exception {
        if (getResultSet() != null) {
            getResultSet().close();
        }
    }
}
