package ma.superyass.restplayground.api;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;

/**
 * @author superyass
 */
@Path("/res1")
public class Resource1Controller {

    @Inject
    private ProjectStage projectStage;
    
    @GET
    @Path("/projectStage")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjectStage() {
        return Response.ok().entity(projectStage).build();
    }
}
