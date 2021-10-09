package com.twb.stringtoclass.ingestion;

import com.twb.stringtoclass.persist.PersistenceFacade;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryOperations;

public interface IngestionService {
    void init(ScriptContext context);
    void execute(ScriptContext context, ExecuteParams params);

    @Getter
    @Builder
    final class ScriptContext {
        private ScriptInfo scriptInfo;
        private ApplicationContext applicationContext;
        private PersistenceFacade persistenceService;
        private RetryOperations retryTemplate;
    }

    @Getter
    @Builder
    final class ExecuteParams {
        private RetryContext retryContext;
    }
}
