package ma.superyass.restplayground.api;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import ma.superyass.restplayground.api.security.TokenProvider;
import ma.superyass.restplayground.api.security.dto.LoginDTO;
import ma.superyass.restplayground.api.utils.JWTTokenDTO;
import ma.superyass.restplayground.security.entities.User;
import ma.superyass.restplayground.security.service.AuthenticationService;
import ma.superyass.restplayground.security.utils.AuthenticationException;
import ma.superyass.restplayground.utils.Constants;

/**
 *
 * @author superyass
 */
@Path("/security")
public class SecurityController {
    
    @Inject
    AuthenticationService authenticationService;
    
    @Inject
    TokenProvider tokenProvider;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Valid @NotNull LoginDTO loginDTO) {
        System.out.println(loginDTO);

        try {
            User user = authenticationService.authenticate(loginDTO.getUsername(),loginDTO.getPassword());
            boolean rememberMe = (loginDTO.isRememberMe() == null) ? false : loginDTO.isRememberMe();
            String jwt = tokenProvider.createToken(user, rememberMe);
            return Response.ok(new JWTTokenDTO(jwt)).header(Constants.AUTHORIZATION_HEADER, "Bearer " + jwt).build();
        } catch (AuthenticationException exception) {
            return Response.status(Response.Status.UNAUTHORIZED).header("AuthenticationException", exception.getLocalizedMessage()).build();
        }
    }
}
