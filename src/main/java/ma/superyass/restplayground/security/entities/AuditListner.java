package ma.superyass.restplayground.security.entities;

import java.util.Date;
import javax.inject.Inject;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import ma.superyass.restplayground.security.utils.SecurityUtils;

/**
 * Entity listener class for audit info
 */
public class AuditListner {

    @Inject
    private SecurityUtils securityUtils;

    @PrePersist
    void onCreate(AbstractAuditingEntity entity) {
        entity.setCreatedDate(new Date());
        entity.setCreatedBy(securityUtils.getCurrentUserLogin());
    }

    @PreUpdate
    void onUpdate(AbstractAuditingEntity entity) {
        entity.setLastModifiedDate(new Date());
        entity.setLastModifiedBy(securityUtils.getCurrentUserLogin());
    }
    
}
