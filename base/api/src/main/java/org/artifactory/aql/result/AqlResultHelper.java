package org.artifactory.aql.result;

import org.artifactory.aql.result.rows.AqlRowResult;
import org.jfrog.common.StreamSupportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Saffi Hartal 18-03-06
 * Stream tool for resultset support generics.
 * close provider (resultset) when done.
 */
@SuppressWarnings("WeakerAccess")
public class AqlResultHelper {
    private static final Logger log = LoggerFactory.getLogger(AqlResultHelper.class);

    /**
     * return stream of this, and calls next()
     *
     * @param onFinish Consumer<Exception>
     * @return Stream<AqlResultSetProvider>
     */
    static <
            T extends AqlRowResult,
            P extends AqlResultSetProvider<T>
            >
    Stream<P> whileNextStream(
            P provider,
            Consumer<Exception> onFinish) {

        if (provider == null) {
            log.warn("Null provider ignored - return empty stream");
            return Stream.empty();
        }

        final boolean[] finished = {false};

        // null protected finish once
        Consumer<Exception> finishOnce = (e) -> {
            if (finished[0]) return; // ignore is already finished
            if (e != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed {} ", AqlResultHelper.class.getName(), e);
                }
            }

            if (onFinish != null) onFinish.accept(e);
            finished[0] = true;
        };

        final boolean[] closed = {false};

        Runnable closeOnce = () -> {
            // ignore already closed.
            if (closed[0]) {
                if (log.isTraceEnabled()) log.trace("Already closed.");
                return;
            }
            // close provider
            try {
                provider.close();
                closed[0] = true;
                finishOnce.accept(null);
            } catch (Exception e) {
                finishOnce.accept(e);
            }
        };


        Function<Integer, P> generator = (i) -> {
            try {
                if (provider.getResultSet().next()) {
                    return provider;  // streaming
                }
            } catch (Exception e) {
                finishOnce.accept(e);
            }
            // close resource when done
            closeOnce.run();
            return null;
        };

        // stream.close closes the provider
        return StreamSupportUtils.generateTillNull(generator).onClose(closeOnce).filter(Objects::nonNull);
    }

}
