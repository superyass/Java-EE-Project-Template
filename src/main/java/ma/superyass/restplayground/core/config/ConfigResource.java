package ma.superyass.restplayground.core.config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.config.PropertyFileConfig;

/**
 *
 * @author superyass
 */
@ApplicationScoped
public class ConfigResource implements PropertyFileConfig{
    
    @Override
    public String getPropertyFileName() {
        return "config/application.properties";
    }

    @Override
    public boolean isOptional() {
        return true;
    }

}
