package ma.superyass.restplayground.security.facades;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.inject.Inject;
import ma.superyass.restplayground.core.facades.AbstractFacade;
import ma.superyass.restplayground.security.entities.Authority;

@Stateless
@Named("authority")
public class AuthorityFacade extends AbstractFacade<Authority, String> {

    @Inject
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public AuthorityFacade() {
        super(Authority.class);
    }
}
