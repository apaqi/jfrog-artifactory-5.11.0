package org.artifactory.rest.resource.replication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.artifactory.api.common.BasicStatusHolder;

@AllArgsConstructor
public class StatusHasErrorException extends RuntimeException {

    private @Getter BasicStatusHolder status;
}
