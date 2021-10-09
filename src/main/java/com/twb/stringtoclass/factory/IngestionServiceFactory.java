package com.twb.stringtoclass.factory;

import com.twb.stringtoclass.ingestion.IngestionService;
import com.twb.stringtoclass.ingestion.ScriptInfo;
import com.twb.stringtoclass.persist.ScalityPersistenceService;
import groovy.lang.GroovyClassLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import static com.twb.stringtoclass.factory.RetryTemplateFactory.retryTemplate;
import static org.apache.commons.lang3.reflect.FieldUtils.writeField;

@Component
public class IngestionServiceFactory {
    private static final String SCRIPT_CONTEXT_FIELD = "context";

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ScalityPersistenceService persistenceService;

    //    read the string as a groovy class, create new instance of IngestionService and initialise it with beans
    //    note: the beans dont automatically get set with Autowired
    public IngestionService getService(String fileContent) throws InstantiationException, IllegalAccessException {
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
        Class scriptClass = groovyClassLoader.parseClass(fileContent);

        IngestionService service = (IngestionService) scriptClass.newInstance();
        ScriptInfo scriptInfo = service.scriptInfo();

        //using reflection over setter for immutability from script
        writeField(service, SCRIPT_CONTEXT_FIELD, IngestionService.ScriptContext.builder()
                .applicationContext(context)
                .persistenceService(persistenceService)
                .retryTemplate(retryTemplate(scriptInfo.maxTries()))
                .build(), true);

        return service;
    }
}
