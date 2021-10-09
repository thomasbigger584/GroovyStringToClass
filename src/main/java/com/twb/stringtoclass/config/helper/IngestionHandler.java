package com.twb.stringtoclass.config.helper;

import com.twb.stringtoclass.ingestion.IngestionService;
import com.twb.stringtoclass.ingestion.ScriptInfo;
import com.twb.stringtoclass.persist.ScalityPersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryOperations;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.twb.stringtoclass.config.factory.RetryTemplateFactory.retryTemplate;
import static com.twb.stringtoclass.ingestion.IngestionService.ScriptContext;

@Component
public class IngestionHandler {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ScalityPersistenceService persistenceService;

    private Map<String, ScriptContext> serviceScriptContexts =
            Collections.synchronizedMap(new HashMap<>());

    private List<String> executingServices =
            Collections.synchronizedList(new ArrayList<>());

    public void init(IngestionService ingestionService) throws Exception {
        try {
            ScriptContext scriptContext = buildScriptContext(ingestionService);
            ingestionService.init(scriptContext);
            ScriptInfo scriptInfo = scriptContext.getScriptInfo();
            serviceScriptContexts.put(scriptInfo.name(), scriptContext);
        } catch (Exception e) {
            System.err.println("error on initialising = " + e.getMessage());
            throw e;
        }
    }

    public void execute(IngestionService ingestionService) throws Exception {
        ScriptInfo scriptInfo = getScriptInfo(ingestionService);
        String name = scriptInfo.name();

        if (isExecuting(ingestionService)) {
            System.out.println("IngestionService already executing...");
            return;
        }
        executingServices.add(name);
        try {
            ScriptContext scriptContext = serviceScriptContexts.get(name);
            RetryOperations retryTemplate = scriptContext.getRetryTemplate();

            retryTemplate.execute((RetryCallback<Void, Exception>) retryContext -> {
                ingestionService.execute(scriptContext, IngestionService.ExecuteParams.builder()
                        .retryContext(retryContext)
                        .build());
                return null;
            });
        } catch (Exception e) {
            System.err.println("error on execute = " + e);
            e.printStackTrace();
            throw e;
        } finally {
            executingServices.remove(name);
        }
    }

    public boolean isExecuting(IngestionService ingestionService) throws Exception {
        ScriptInfo scriptInfo = getScriptInfo(ingestionService);
        String name = scriptInfo.name();
        return executingServices.contains(name);
    }

    public boolean isInitialised(IngestionService ingestionService) throws Exception {
        ScriptInfo scriptInfo = getScriptInfo(ingestionService);
        String name = scriptInfo.name();
        return serviceScriptContexts.containsKey(name);
    }

    private ScriptContext buildScriptContext(IngestionService ingestionService) throws Exception {
        ScriptInfo scriptInfo = getScriptInfo(ingestionService);
        return ScriptContext.builder()
                .scriptInfo(scriptInfo)
                .applicationContext(context)
                .persistenceService(persistenceService)
                .retryTemplate(retryTemplate(scriptInfo.maxTries()))
                .build();
    }

    public ScriptInfo getScriptInfo(IngestionService ingestionService) throws InstantiationException {
        ScriptInfo scriptInfo = AnnotationUtils.findAnnotation(ingestionService.getClass(), ScriptInfo.class);
        if (scriptInfo == null) {
            throw new InstantiationException("No Script Info Annotation Found on class");
        }
        return scriptInfo;
    }
}
