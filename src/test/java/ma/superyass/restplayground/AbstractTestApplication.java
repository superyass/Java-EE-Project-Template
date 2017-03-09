package ma.superyass.restplayground;

import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import ma.superyass.restplayground.api.SecurityController;
import ma.superyass.restplayground.api.config.JwtSecurityConfig;
import ma.superyass.restplayground.api.security.JWTAuthenticationFilter;
import ma.superyass.restplayground.api.security.dto.LoginDTO;
import ma.superyass.restplayground.core.config.ConfigResource;
import ma.superyass.restplayground.security.dto.UserDTO;
import ma.superyass.restplayground.security.entities.Authority;
import ma.superyass.restplayground.security.entities.User;
import ma.superyass.restplayground.security.facades.AuthorityFacade;
import ma.superyass.restplayground.security.facades.UserFacade;
import ma.superyass.restplayground.security.service.AuthenticationService;
import ma.superyass.restplayground.security.utils.SecurityUtils;
import ma.superyass.restplayground.utils.RandomUtil;
import org.glassfish.grizzly.http.server.Constants;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;

/**
 * Abstract class for application packaging.
 *.addClass(AuthenticationService.).addClass(AuthenticationService.)
 */
public abstract class AbstractTestApplication extends AbstractTest {

    protected static final String USERNAME = "admin";
    protected static final String PASSWORD = "admin";
    protected static final String INVALID_PASSWORD = "invalid_password";
    protected static final String INCORRECT_PASSWORD = "pw";
    private static final String AUTH_RESOURCE_PATH = "security/login";

    protected String tokenId;

    public static WebArchive buildApplication() {
        return buildArchive().addPackages(true, ConfigResource.class.getPackage(),
                UserDTO.class.getPackage(), SecurityUtils.class.getPackage(), RandomUtil.class.getPackage())
                .addClass(User.class).addClass(Authority.class).addClass(UserFacade.class).addClass(AuthorityFacade.class)
                .addAsResource(new ClassLoaderAsset("config/application.properties"), "config/application.properties")
                .addAsResource(new ClassLoaderAsset("i18n/messages.properties"), "i18n/messages.properties")
                .addClass(JwtSecurityConfig.class).addClass(AuthenticationService.class)
                .addClass(SecurityController.class).addPackage(JWTAuthenticationFilter.class.getPackage());
    }

    @Before
    public void setUp() throws Exception {
        login(USERNAME, PASSWORD);
    }

    @After
    public void tearDown() {
        logout();
    }

    protected Response login(String username, String password) {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(username);
        loginDTO.setPassword(password);
        Response response = target(AUTH_RESOURCE_PATH).post(Entity.json(loginDTO));
        tokenId = response.getHeaderString(Constants.AUTHORIZATION_HEADER);
        System.out.println(">>>>Loged in - tokenId: "+tokenId);
        return response;
    }

    protected void logout() {
        tokenId = null;
        System.out.println(">>>>Loged Out");
    }

    @Override
    protected Invocation.Builder target(String path) {
        return super.target(path).header(Constants.AUTHORIZATION_HEADER, tokenId);
    }

    @Override
    protected Invocation.Builder target(String path, Map<String, Object> params) {
        return super.target(path, params).header(Constants.AUTHORIZATION_HEADER, tokenId);
    }

}
