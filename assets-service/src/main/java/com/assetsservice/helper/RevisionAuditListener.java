package com.assetsservice.helper;

import com.assetsservice.model.db.RevisionAudit;
import org.hibernate.envers.RevisionListener;

/**
 * Listener for Hibernate Envers revisions that captures the current authenticated user.
 */
public class RevisionAuditListener implements RevisionListener {

    @Override
    public void newRevision(Object object) {
        if (object instanceof RevisionAudit revisionAudit) {

            // Get the current authenticated user
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = "system";
            
//            if (authentication != null && authentication.isAuthenticated()) {
//                username = authentication.getName();
//            }
            
            revisionAudit.setUsername(username);
        }
    }
}
