package com.assetsservice.helper;

import org.hibernate.envers.RevisionListener;

// An implementation of RevisionListener is needed to use a custom RevisionEntity.
public class RevisionAuditListener implements RevisionListener {

    @Override
    public void newRevision(Object object) {
        // This override adds no functionality, as the id and timestamp properties of a RevisionEntity are auto-generated.
    }
}
