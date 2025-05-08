package com.assetsservice.helper;

import com.assetsservice.model.db.RevisionAudit;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// An implementation of RevisionListener is needed to use a custom RevisionEntity.
public class RevisionAuditListener implements RevisionListener {

    @Override
    public void newRevision(Object object) {
        if (object instanceof RevisionAudit) {
            RevisionAudit revisionAudit = (RevisionAudit) object;
            
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = "system";
            
            if (authentication != null && authentication.isAuthenticated()) {
                username = authentication.getName();
            }
            
            revisionAudit.setUsername(username);
        }
    }
}
