package ma.superyass.restplayground;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import ma.superyass.restplayground.api.Resource1Controller;
import org.apache.deltaspike.core.api.exclude.Exclude;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.valid4j.matchers.http.HttpResponseMatchers.hasStatus;

public class Resource1ControllerTest extends AbstractTestApplication {

    @Deployment(name = "restplaygroundtest")
    public static WebArchive createDeployment() {
        return buildApplication().addClass(Resource1Controller.class);
    }

    @Test
    public void test() throws Exception {
        System.out.println(">>>test started");
        
//        Response response = target("security/login").post(Entity.json("{\"username\":\"admin\",\"password\":\"admin\"}"));
        Response response = target("res1/projectStage").get();
        
        System.out.println(">>"+response.getStatus());
        
        System.out.println(">>>test finished");
        
        assertThat(response, hasStatus(Response.Status.OK));
        
        
    }

    
}
