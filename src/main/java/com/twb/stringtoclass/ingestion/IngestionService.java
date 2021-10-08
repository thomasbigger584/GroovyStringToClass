package com.twb.stringtoclass.ingestion;

import com.twb.stringtoclass.persist.PersistenceFacade;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

public abstract class IngestionService {
    protected ApplicationContext context;
    protected Environment environment;
    protected PersistenceFacade persistence;

    @Getter
    private boolean isExecuting = false;

    @Getter
    private boolean hasInitialised = false;

    public void setBeans(BeanParams beans) {
        this.context = beans.getContext();
        this.environment = this.context.getEnvironment();
        this.persistence = beans.getPersistenceService();
    }

    public final void init() throws Exception {
        System.out.println("IngestionService.init");
        try {
            onInit();
            hasInitialised = true;
        } catch (Exception e) {
            System.err.println("error on initialising = " + e.getMessage());
            throw e;
        }
    }

    public final void execute() throws Exception {
        System.out.println("IngestionService.execute");
        isExecuting = true;
        try {
            onExecute();
        } catch (Exception e) {
            System.err.println("error on execute = " + e.getMessage());
            throw e;
        } finally {
            isExecuting = false;
        }
    }

    public void onInit() throws Exception {
    }

    public abstract void onExecute() throws Exception;

    public abstract ScriptInfo scriptInfo();

    @Getter
    @Builder
    public static final class BeanParams {
        private ApplicationContext context;
        private PersistenceFacade persistenceService;
    }

    @Getter
    @Builder
    public static final class ScriptInfo {
        private String vendor;
        private String email;
        private int version;
    }
}
