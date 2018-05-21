package org.artifactory.aql.result;

import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.result.rows.AqlRowResult;

import java.util.List;

/**
 * @author Shay Bagants
 */
public interface AqlComposedResult extends AqlLazyResult<AqlRowResult> {

    List<AqlFieldEnum> getOriginalFields();
}
