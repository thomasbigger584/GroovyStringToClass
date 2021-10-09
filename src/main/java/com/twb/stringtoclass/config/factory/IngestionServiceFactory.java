package com.twb.stringtoclass.config.factory;

import com.twb.stringtoclass.ingestion.IngestionService;
import groovy.lang.GroovyClassLoader;
import org.springframework.stereotype.Component;

@Component
public class IngestionServiceFactory {

    //    read the string as a groovy class, create new instance of IngestionService and initialise it with beans
    //    note: the beans dont automatically get set with Autowired
    public IngestionService getService(String fileContent) throws InstantiationException, IllegalAccessException {
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
        Class scriptClass = groovyClassLoader.parseClass(fileContent);

        return (IngestionService) scriptClass.newInstance();
    }
}
