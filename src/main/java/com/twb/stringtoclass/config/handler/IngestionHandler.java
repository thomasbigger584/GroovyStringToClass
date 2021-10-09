package com.twb.stringtoclass.config.handler;

import com.twb.stringtoclass.config.factory.IngestionServiceFactory;
import com.twb.stringtoclass.ingestion.IngestionService;
import com.twb.stringtoclass.ingestion.IngestionService.ExecuteParams;
import com.twb.stringtoclass.ingestion.ScriptInfo;
import com.twb.stringtoclass.persist.ScalityPersistenceService;
import lombok.Builder;
import lombok.Getter;
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
    private static final Map<String, ServiceHolder> SERVICES_INITIALISED =
            Collections.synchronizedMap(new HashMap<>());
    private static final List<String> SERVICES_EXECUTING =
            Collections.synchronizedList(new ArrayList<>());

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ScalityPersistenceService persistenceService;

    @Autowired
    private IngestionServiceFactory factory;

    public void addService(String fileContent) throws Exception {
        IngestionService service = factory.getService(fileContent);
        if (!isInitialised(service)) {
            init(service);
        }
    }

    public boolean executeService(String name) throws Exception {
        if (SERVICES_INITIALISED.containsKey(name)) {
            return execute(name);
        }
        return false;
    }

    private void init(IngestionService ingestionService) throws Exception {
        try {
            ScriptContext scriptContext = buildScriptContext(ingestionService);
            ingestionService.init(scriptContext);

            ScriptInfo scriptInfo = scriptContext.getScriptInfo();
            String name = scriptInfo.name();
            ServiceHolder serviceHolder = ServiceHolder.builder()
                    .scriptContext(scriptContext)
                    .ingestionService(ingestionService)
                    .build();

            SERVICES_INITIALISED.put(name, serviceHolder);
        } catch (Exception e) {
            System.err.println("error on initialising = " + e.getMessage());
            throw e;
        }
    }

    private boolean execute(String name) throws Exception {
        ServiceHolder serviceHolder = SERVICES_INITIALISED.get(name);
        IngestionService ingestionService = serviceHolder.getIngestionService();
        ScriptContext scriptContext = serviceHolder.getScriptContext();

        if (isExecuting(ingestionService)) {
            System.out.println("IngestionService already executing...");
            return false;
        }
        SERVICES_EXECUTING.add(name);
        try {
            RetryOperations retryTemplate = scriptContext.getRetryTemplate();
            retryTemplate.execute((RetryCallback<Void, Exception>) retryContext -> {
                ingestionService.execute(scriptContext, ExecuteParams.builder()
                        .retryContext(retryContext)
                        .build());
                return null;
            });
            return true;
        } catch (Exception e) {
            System.err.println("error on execute = " + e);
            e.printStackTrace();
            throw e;
        } finally {
            SERVICES_EXECUTING.remove(name);
        }
    }

    private boolean isExecuting(IngestionService ingestionService) throws Exception {
        ScriptInfo scriptInfo = getScriptInfo(ingestionService);
        String name = scriptInfo.name();
        return SERVICES_EXECUTING.contains(name);
    }

    private boolean isInitialised(IngestionService ingestionService) throws Exception {
        ScriptInfo scriptInfo = getScriptInfo(ingestionService);
        String name = scriptInfo.name();
        if (SERVICES_INITIALISED.containsKey(name)) {
            ServiceHolder serviceHolder = SERVICES_INITIALISED.get(name);
            ScriptContext scriptContext = serviceHolder.getScriptContext();
            ScriptInfo initialisedScriptInfo = scriptContext.getScriptInfo();
            return initialisedScriptInfo.version() >= scriptInfo.version();
        }
        return false;
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

    @Getter
    @Builder
    static class ServiceHolder {
        private final ScriptContext scriptContext;
        private final IngestionService ingestionService;
    }
}
