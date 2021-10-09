package com.twb.stringtoclass.config.ingestion;

import com.twb.stringtoclass.ingestion.IngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

//@Component
public class IngestionScheduler {

//    @Autowired
    private IngestionConfig ingestionConfig;

//    @Scheduled(fixedRate = 10000)
    public void run() {
        Map<String, IngestionService> registeredServices
                = ingestionConfig.getRegisteredServices();

        for (Map.Entry<String, IngestionService> registeredServiceEntry : registeredServices.entrySet()) {
            IngestionService ingestionService = registeredServiceEntry.getValue();

            // determine how execute should be called
            try {
                ingestionService.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
