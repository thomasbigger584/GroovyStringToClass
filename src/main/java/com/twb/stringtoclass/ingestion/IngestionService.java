package com.twb.stringtoclass.ingestion;

import com.twb.stringtoclass.persist.PersistenceFacade;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryOperations;

public abstract class IngestionService {
    protected ScriptContext context;

    @Getter
    private boolean initialised = false;

    @Getter
    private boolean executing = false;

    public final void init() throws Exception {
        System.out.println("IngestionService.init");
        if (context == null) {
            throw new InstantiationException("Something has gone wrong, " +
                    "the script context is null");
        }
        try {
            onInit();
            initialised = true;
        } catch (Exception e) {
            System.err.println("error on initialising = " + e.getMessage());
            throw e;
        }
    }

    public final void execute() throws Exception {
        System.out.println("IngestionService.execute");
        if (isExecuting()) {
            System.out.println("IngestionService already executing...");
            return;
        }
        executing = true;
        try {
            RetryOperations retryTemplate = context.getRetryTemplate();
            retryTemplate.execute((RetryCallback<Void, Exception>) retryContext -> {
                onExecute(ExecuteParams.builder()
                        .retryContext(retryContext)
                        .build());
                return null;
            });
        } catch (Exception e) {
            System.err.println("error on execute = " + e);
            e.printStackTrace();
            throw e;
        } finally {
            executing = false;
        }
    }

    public void onInit() throws Exception {
    }

    public abstract void onExecute(ExecuteParams params) throws Exception;

    public final ScriptInfo scriptInfo() throws InstantiationException {
        ScriptInfo scriptInfo = AnnotationUtils.findAnnotation(getClass(), ScriptInfo.class);
        if (scriptInfo == null) {
            throw new InstantiationException("No Script Info Annotation Found on class");
        }
        return scriptInfo;
    }

    @Getter
    @Builder
    public static final class ScriptContext {
        private ApplicationContext applicationContext;
        private PersistenceFacade persistenceService;
        private RetryOperations retryTemplate;
    }

    @Getter
    @Builder
    public static final class ExecuteParams {
        private RetryContext retryContext;
    }
}
