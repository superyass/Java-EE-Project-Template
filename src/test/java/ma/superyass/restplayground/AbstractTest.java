package ma.superyass.restplayground;


import java.io.File;
import java.net.URL;
import java.util.Map;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import ma.superyass.restplayground.api.utils.HeaderUtil;
import ma.superyass.restplayground.core.facades.AbstractFacade;
import ma.superyass.restplayground.core.facades.producer.EntityManagerProducer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * Abstract class for base application packaging.
 *
 */
@RunWith(Arquillian.class)
public abstract class AbstractTest {

    @ArquillianResource
    private URL deploymentUrl;
    private WebTarget webTarget;
    protected final static MavenResolverSystem RESOLVER = Maven.resolver();

    public static WebArchive buildArchive() {
        File[] jacksonFiles = RESOLVER.resolve("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.8.7").withTransitivity().asFile();
        File[] deltaspikeFiles = RESOLVER.resolve("org.apache.deltaspike.core:deltaspike-core-api:1.7.2").withTransitivity().asFile();
        File[] deltaspikeImplFiles = RESOLVER.resolve("org.apache.deltaspike.core:deltaspike-core-impl:1.7.2").withTransitivity().asFile();

        final WebArchive archive = ShrinkWrap.create(WebArchive.class);
        archive.addClass(AbstractFacade.class).addPackage(HeaderUtil.class.getPackage())
                .addClass(EntityManagerProducer.class)
                .addAsLibraries(jacksonFiles).addAsLibraries(deltaspikeFiles).addAsLibraries(deltaspikeImplFiles)
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsResource(new ClassLoaderAsset("META-INF/insert.sql"), "META-INF/insert.sql")
                .setWebXML("web.xml");
        return archive;
    }

    @Before
    public void buildWebTarget() throws Exception {
        webTarget = ClientBuilder.newClient().target(deploymentUrl.toURI().toString() + "api1/");
        System.out.println("webtarget path: "+deploymentUrl.toURI().toString() + "api1/");
    }

    protected Invocation.Builder target(String path) {
        return webTarget.path(path).request();
    }

    protected Invocation.Builder target(String path, Map<String, Object> params) {
        WebTarget target = webTarget.path(path);
        for (String key : params.keySet()) {
            if (path.contains(String.format("{%s}", key))) {
                target = target.resolveTemplate(key, params.get(key));
            } else {
                target = target.queryParam(key, params.get(key));
            }
        }
        return target.request();
    }

}
