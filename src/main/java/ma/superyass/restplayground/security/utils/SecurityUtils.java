package ma.superyass.restplayground.security.utils;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author superyass
 */
public class SecurityUtils {

    @Context
    private SecurityContext securityContext;

    public String getCurrentUserLogin() {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return null;
        }
        return securityContext.getUserPrincipal().getName();
    }
}
