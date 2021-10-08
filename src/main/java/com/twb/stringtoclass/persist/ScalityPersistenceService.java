package com.twb.stringtoclass.persist;

import org.springframework.stereotype.Component;

@Component
public class ScalityPersistenceService implements PersistenceFacade {

    @Override
    public void persist(String bucket, String path) {
        System.out.println("ScalityPersistenceService.persist");
        System.out.println("bucket = " + bucket);
        System.out.println("path = " + path);
    }
}
