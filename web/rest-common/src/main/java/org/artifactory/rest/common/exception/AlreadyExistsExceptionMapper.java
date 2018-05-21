package org.artifactory.rest.common.exception;

import com.sun.jersey.api.Responses;
import org.artifactory.rest.ErrorResponse;
import org.artifactory.util.AlreadyExistsException;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Rotem Kfir
 */
@Component
@Provider
public class AlreadyExistsExceptionMapper implements ExceptionMapper<AlreadyExistsException> {

    @Override
    public Response toResponse(AlreadyExistsException exception) {
        ErrorResponse errorResponse = new ErrorResponse(Response.Status.CONFLICT.getStatusCode(),
                exception.getMessage());
        return Responses.conflict().type(MediaType.APPLICATION_JSON).entity(errorResponse).build();
    }
}
