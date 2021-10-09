package com.twb.stringtoclass.config.helper;

import com.twb.stringtoclass.ingestion.IngestionService;
import com.twb.stringtoclass.ingestion.ScriptInfo;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
public class ScriptInfoHelper {

    public ScriptInfo scriptInfo(IngestionService ingestionService) throws InstantiationException {
        ScriptInfo scriptInfo = AnnotationUtils.findAnnotation(ingestionService.getClass(), ScriptInfo.class);
        if (scriptInfo == null) {
            throw new InstantiationException("No Script Info Annotation Found on class");
        }
        return scriptInfo;
    }
}
