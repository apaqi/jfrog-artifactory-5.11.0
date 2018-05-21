package org.artifactory.rest.common.exception;

import com.sun.jersey.api.Responses;
import org.artifactory.rest.ErrorResponse;
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
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        ErrorResponse errorResponse = new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(),
                exception.getMessage());
        return Responses.clientError().type(MediaType.APPLICATION_JSON).entity(errorResponse).build();
    }
}
