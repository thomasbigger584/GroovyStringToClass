package com.twb.stringtoclass.ingestion;

import com.twb.stringtoclass.persist.PersistenceFacade;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryOperations;

public abstract class IngestionService {
    protected ApplicationContext context;
    protected Environment environment;
    protected PersistenceFacade persistence;
    protected RetryOperations retryTemplate;

    @Getter
    private boolean initialised = false;

    @Getter
    private boolean executing = false;

    public void setBeans(BeanParams beans) {
        this.context = beans.getContext();
        this.environment = this.context.getEnvironment();
        this.persistence = beans.getPersistenceService();
        this.retryTemplate = beans.getRetryTemplate();
    }

    public final void init() throws Exception {
        System.out.println("IngestionService.init");
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
            retryTemplate.execute((RetryCallback<Void, Exception>) retryContext -> {
                System.out.println("Retry Count " + retryContext.getRetryCount());
                onExecute();
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

    public abstract void onExecute() throws Exception;

    public ScriptInfo scriptInfo() throws InstantiationException {
        ScriptInfo scriptInfo = AnnotationUtils.findAnnotation(getClass(), ScriptInfo.class);
        if (scriptInfo == null) {
            throw new InstantiationException("No Script Info Annotation Found on class");
        }
        return scriptInfo;
    }

    @Getter
    @Builder
    public static final class BeanParams {
        private ApplicationContext context;
        private PersistenceFacade persistenceService;
        private RetryOperations retryTemplate;
    }
}
