package com.twb.stringtoclass;

import com.twb.stringtoclass.factory.IngestionServiceFactory;
import com.twb.stringtoclass.ingestion.IngestionService;
import com.twb.stringtoclass.ingestion.ScriptInfo;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class StringToClassService {

    @Autowired
    private IngestionServiceFactory factory;

    @Getter
    private Map<String, IngestionService> ingestionServiceMap = new HashMap<>();

    public void execute(String fileName) throws Exception {
        String fileContent;
        try {
            fileContent = getFileContents(fileName);
        } catch (IOException e) {
            throw new Exception("Could not read file contents", e);
        }

        // initialise the bean with groovy file contents as string
        IngestionService ingestionService = initialise(fileContent);

        ingestionService.execute();
    }

    // register the bean and call the init method
    private IngestionService initialise(String fileContent) {
        IngestionService ingestionService = null;
        try {
            ingestionService = register(fileContent);
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
        return ingestionService;
    }

    //given the file content as string, register the class, only if its not executing and if it's a new version
    private IngestionService register(String fileContent) throws InstantiationException, IllegalAccessException {
        IngestionService service = factory.getService(fileContent);
        ScriptInfo newScriptInfo = service.scriptInfo();

        int version = newScriptInfo.version();
        String vendor = newScriptInfo.vendor();

        if (ingestionServiceMap.containsKey(vendor)) {
            IngestionService existingIngestionService = ingestionServiceMap.get(vendor);

//          we dont want to interrupt if its already running ?
            if (!existingIngestionService.isExecuting()) {
                ScriptInfo existingScriptInfo = existingIngestionService.scriptInfo();
                int existingBeanVersion = existingScriptInfo.version();

//              we dont want to overwrite with either the same or old version ?
                if (version > existingBeanVersion) {
                    System.out.println("--> Bean not executing and is a new version, so creating: " + vendor);
                    ingestionServiceMap.put(vendor, service);
                    return service;
                } else {
                    System.out.println("--> Bean is not a new version: " + vendor);
                    return existingIngestionService;
                }
            } else {
                System.out.println("--> Bean is already executing: " + vendor);
                return existingIngestionService;
            }
        } else {
            System.out.println("--> No Such instance with name, creating " + vendor);
            ingestionServiceMap.put(vendor, service);
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

    //    read the file from the resources as string. this could just as easy be downloaded from github
    private String getFileContents(String fileName) throws IOException {
        Resource resource = new ClassPathResource(fileName);
        File file = resource.getFile();
        return FileUtils.readFileToString(file, StandardCharsets.US_ASCII);
    }
}
