package com.twb.stringtoclass;

import com.twb.stringtoclass.ingestion.IngestionService;
import com.twb.stringtoclass.persist.ScalityPersistenceService;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.twb.stringtoclass.ingestion.IngestionService.BeanParams;

@Component
public class StringtoclassService {
    @Autowired
    private ApplicationContext context;

    @Autowired
    private ScalityPersistenceService persistenceService;

    @Value("classpath:VendorIngestionService.groovy")
    private Resource vendorIngestionService;

    @PostConstruct
    public void execute() {
        String fileContent = null;
        try {
            fileContent = getFileContents();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("fileContent = " + fileContent);

        try {
            Optional<IngestionService> ingestionServiceOpt = registerBean(fileContent);
            if (ingestionServiceOpt.isPresent()) {
                IngestionService ingestionService = ingestionServiceOpt.get();
                try {
                    ingestionService.init();
                    ingestionService.execute();
                } catch (Exception e) {
                    System.out.println("e.getMessage() = " + e.getMessage());
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Optional<IngestionService> registerBean(String fileContent) throws InstantiationException, IllegalAccessException {
        IngestionService service = getIngestionService(fileContent);
        String beanName = service.getVendorName();

        ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
        try {
            IngestionService existingIngestionService = (IngestionService) beanFactory.getBean(beanName);
            if (!existingIngestionService.isExecuting()) {
                System.err.println("Bean not executing, so recreating " + beanName);
                beanFactory.destroyBean(existingIngestionService);
                beanFactory.registerSingleton(beanName, service);
                return Optional.of(service);
            } else {
                System.err.println("Bean is already executing " + beanName);
                return Optional.empty();
            }
        } catch (NoSuchBeanDefinitionException e) {
            System.err.println("No Such bean with name, creating " + beanName);
            beanFactory.registerSingleton(beanName, service);
            return Optional.of(service);
        }
    }

    private IngestionService getIngestionService(String fileContent) throws InstantiationException, IllegalAccessException {
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
        System.out.println("groovyClassLoader = " + groovyClassLoader);
        Class scriptClass = groovyClassLoader.parseClass(fileContent);
        IngestionService ingestionService = (IngestionService) scriptClass.newInstance();
        ingestionService.setBeans(BeanParams.builder()
                .context(context)
                .persistenceService(persistenceService)
                .build());
        return ingestionService;
    }

    private String getFileContents() throws IOException {
        File file = vendorIngestionService.getFile();
        return FileUtils.readFileToString(file, StandardCharsets.US_ASCII);
    }
}
