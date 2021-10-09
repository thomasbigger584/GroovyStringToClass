package com.twb.stringtoclass.config.ingestion;

import com.twb.stringtoclass.factory.IngestionServiceFactory;
import com.twb.stringtoclass.ingestion.IngestionService;
import com.twb.stringtoclass.ingestion.ScriptInfo;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class IngestionConfig {

    @Autowired
    private IngestionServiceFactory factory;

    @Getter
    private Map<String, IngestionService> registeredServices = new HashMap<>();

    public void addService(String fileContent) {
        try {
            IngestionService ingestionService = register(fileContent);
            if (!ingestionService.isInitialised()) {
                try {
                    ingestionService.init();
                } catch (Exception e) {
                    sendErrorEmail(ingestionService, e);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private IngestionService register(String fileContent) throws InstantiationException, IllegalAccessException {
        IngestionService service = factory.getService(fileContent);
        ScriptInfo newScriptInfo = service.scriptInfo();

        int newVersion = newScriptInfo.version();
        String name = newScriptInfo.name();

        if (registeredServices.containsKey(name)) {
            IngestionService existingIngestionService = registeredServices.get(name);

            if (!existingIngestionService.isExecuting()) {
                ScriptInfo existingScriptInfo = existingIngestionService.scriptInfo();
                int existingVersion = existingScriptInfo.version();

                if (newVersion > existingVersion) {
                    registeredServices.put(name, service);
                    return service;
                } else {
                    return existingIngestionService;
                }
            } else {
                return existingIngestionService;
            }
        } else {
            registeredServices.put(name, service);
            return service;
        }
    }

    //if there is anything wrong with the vendors execution they could be sent an email ?
    private void sendErrorEmail(IngestionService ingestionService, Exception e) throws InstantiationException {
        ScriptInfo scriptInfo = ingestionService.scriptInfo();
        String email = scriptInfo.email();
        String exceptionMessage = e.getMessage();
        System.out.printf("Send Email to %s - reason %s", email, exceptionMessage);
    }
}
