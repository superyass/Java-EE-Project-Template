package ma.superyass.restplayground.api.security;

import io.jsonwebtoken.ExpiredJwtException;
import java.io.IOException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import ma.superyass.restplayground.utils.Constants;
import org.apache.commons.lang3.StringUtils;

@Priority(Priorities.AUTHENTICATION)
@Provider
@Secured
public class JWTAuthenticationFilter implements ContainerRequestFilter {

    @Inject
    private TokenProvider tokenProvider;

    @Context
    private HttpServletRequest request;

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String jwt = resolveToken();
        if (StringUtils.isNotBlank(jwt)) {
            try {
                if (tokenProvider.validateToken(jwt)) {
                    UserAuthenticationToken authenticationToken = this.tokenProvider.getAuthentication(jwt);
                    if (!isAllowed(authenticationToken)) {
                        requestContext.setProperty("auth-failed", true);
                        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                    }
                    final SecurityContext securityContext = requestContext.getSecurityContext();
                    requestContext.setSecurityContext(new SecurityContext() {
                        @Override
                        public Principal getUserPrincipal() {
                            return authenticationToken::getPrincipal;
                        }

                        @Override
                        public boolean isUserInRole(String role) {
                            return securityContext.isUserInRole(role);
                        }

                        @Override
                        public boolean isSecure() {
                            return securityContext.isSecure();
                        }

                        @Override
                        public String getAuthenticationScheme() {
                            return securityContext.getAuthenticationScheme();
                        }
                    });
                }
            } catch (ExpiredJwtException eje) {
                String msg = "Security exception for user {" + eje.getClaims().getSubject() + "} - {" + eje.getMessage() + "}";
                Logger.getLogger(JWTAuthenticationFilter.class.getName()).log(Level.SEVERE, msg);
                requestContext.setProperty("auth-failed", true);
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }

        } else {
            Logger.getLogger(JWTAuthenticationFilter.class.getName()).log(Level.SEVERE, "No JWT token found");
            requestContext.setProperty("auth-failed", true);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }

    }

    private String resolveToken() {
        String bearerToken = request.getHeader(Constants.AUTHORIZATION_HEADER);
        if (StringUtils.isNotEmpty(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String jwt = bearerToken.substring(7, bearerToken.length());
            return jwt;
        }
        return null;
    }

    private boolean isAllowed(UserAuthenticationToken authenticationToken) {
        System.out.println("is Allowed");
        Secured secured = resourceInfo.getResourceMethod().getAnnotation(Secured.class);
        if (secured == null) {
            secured = resourceInfo.getResourceClass().getAnnotation(Secured.class);
        }
        for (String role : secured.value()) {
            if (!authenticationToken.getAuthorities().contains(role)) {
                return false;
            }
        }
        return true;
    }

}
