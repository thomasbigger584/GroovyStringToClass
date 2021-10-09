package com.twb.stringtoclass.config;

import com.twb.stringtoclass.config.factory.IngestionServiceFactory;
import com.twb.stringtoclass.ingestion.IngestionService;
import com.twb.stringtoclass.config.helper.IngestionHandler;
import com.twb.stringtoclass.ingestion.ScriptInfo;
import com.twb.stringtoclass.config.helper.ScriptInfoHelper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class IngestionConfig {

    @Autowired
    private IngestionServiceFactory factory;

    @Autowired
    private ScriptInfoHelper scriptInfoHelper;

    @Autowired
    private IngestionHandler handler;

    @Getter
    private Map<String, IngestionService> registeredServices = new HashMap<>();

    public void addService(String fileContent) {
        try {
            IngestionService ingestionService = register(fileContent);
            if (!handler.isInitialised(ingestionService)) {
                try {
                    handler.init(ingestionService);
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

    private IngestionService register(String fileContent) throws Exception {
        IngestionService newIngestionService = factory.getService(fileContent);
        ScriptInfo newScriptInfo = scriptInfoHelper.scriptInfo(newIngestionService);

        int newVersion = newScriptInfo.version();
        String name = newScriptInfo.name();

        if (registeredServices.containsKey(name)) {
            IngestionService existingIngestionService = registeredServices.get(name);

            if (!handler.isExecuting(existingIngestionService)) {
                ScriptInfo existingScriptInfo = scriptInfoHelper.scriptInfo(existingIngestionService);
                int existingVersion = existingScriptInfo.version();

                if (newVersion > existingVersion) {
                    registeredServices.put(name, newIngestionService);
                    return newIngestionService;
                } else {
                    return existingIngestionService;
                }
            } else {
                return existingIngestionService;
            }
        } else {
            registeredServices.put(name, newIngestionService);
            return newIngestionService;
        }
    }

    //if there is anything wrong with the vendors execution they could be sent an email ?
    private void sendErrorEmail(IngestionService ingestionService, Exception e) throws InstantiationException {
        ScriptInfo scriptInfo = scriptInfoHelper.scriptInfo(ingestionService);
        String email = scriptInfo.email();
        String exceptionMessage = e.getMessage();
        System.out.printf("Send Email to %s - reason %s", email, exceptionMessage);
    }
}
