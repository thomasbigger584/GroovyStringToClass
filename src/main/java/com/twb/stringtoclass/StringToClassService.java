package com.twb.stringtoclass;

import com.twb.stringtoclass.ingestion.IngestionService;
import com.twb.stringtoclass.ingestion.IngestionService.ScriptInfo;
import com.twb.stringtoclass.persist.ScalityPersistenceService;
import groovy.lang.GroovyClassLoader;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.twb.stringtoclass.ingestion.IngestionService.BeanParams;

@Component
public class StringToClassService {
    @Autowired
    private ApplicationContext context;

    @Autowired
    private ScalityPersistenceService persistenceService;

//      Used for testing purpose
    @Value("classpath:VendorIngestionService.groovy")
    private Resource vendorIngestionService;

    @PostConstruct
    public void execute() throws Exception {
        String fileContent = null;
        try {
            fileContent = getFileContents();
        } catch (IOException e) {
            throw new Exception("Could not read file contents", e);
        }
        System.out.println("fileContent = " + fileContent);

        // initialise the bean with groovy file contents as string
        Optional<IngestionService> ingestionServiceOptional = initialiseBean(fileContent);

        if (ingestionServiceOptional.isPresent()) {
            //it is a new bean and hasnt called process yet
            IngestionService ingestionService = ingestionServiceOptional.get();

            //lets call it a few times
            for (int index = 0; index < 3; index++) {
                System.out.println("calling index = " + index);
                try {
                    //should this be on a new thread ?
                    ingestionService.execute();
                } catch (Exception e) {
                    sendErrorEmail(ingestionService, e);
                }
            }
        }
    }

    // register the bean and call the init method
    private Optional<IngestionService> initialiseBean(String fileContent) {
        IngestionService ingestionService = null;
        try {
            Optional<IngestionService> ingestionServiceOpt = registerBean(fileContent);
            if (ingestionServiceOpt.isPresent()) {
                 ingestionService = ingestionServiceOpt.get();
                try {
                    ingestionService.init();
                } catch (Exception e) {
                    sendErrorEmail(ingestionService, e);
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(ingestionService);
    }

//    given the file content as string, register the bean in the application context
//    only if its not executing and if its a new version
    private Optional<IngestionService> registerBean(String fileContent) throws InstantiationException, IllegalAccessException {

        IngestionService service = getIngestionService(fileContent);

        ScriptInfo newScriptInfo = service.scriptInfo();
        int newVersion = newScriptInfo.getVersion();
        String vendor = newScriptInfo.getVendor();

        ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();

        try {
            IngestionService existingIngestionService = (IngestionService) beanFactory.getBean(vendor);

//            we dont want to interrupt if its already running ?
            if (!existingIngestionService.isExecuting()) {

                ScriptInfo existingScriptInfo = existingIngestionService.scriptInfo();
                int existingBeanVersion = existingScriptInfo.getVersion();

//                we dont want to overwrite with either the same or old version ?
                if (newVersion > existingBeanVersion) {

                    System.out.println("Bean not executing and is a new version, so recreating " + vendor);
                    beanFactory.destroyBean(existingIngestionService);
                    beanFactory.registerSingleton(vendor, service);
                    return Optional.of(service);
                } else {

                    System.out.println("Bean is not a new version: " + vendor);
                    return Optional.empty();
                }
            } else {

                System.out.println("Bean is already executing " + vendor);
                return Optional.empty();
            }
        } catch (NoSuchBeanDefinitionException e) {

            System.err.println("No Such bean with name, creating " + vendor);
            beanFactory.registerSingleton(vendor, service);
            return Optional.of(service);
        }
    }

//    read the string as a groovy class, create new instance of IngestionService and initialise it with beans
//    the beans dont automatically get set with Autowired
    private IngestionService getIngestionService(String fileContent) throws InstantiationException, IllegalAccessException {
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
        Class scriptClass = groovyClassLoader.parseClass(fileContent);
        IngestionService ingestionService = (IngestionService) scriptClass.newInstance();
        ingestionService.setBeans(BeanParams.builder()
                .context(context)
                .persistenceService(persistenceService)
                .build());
        return ingestionService;
    }

//      if there is anything wrong with the vendors execution they could be sent an email ?
    private void sendErrorEmail(IngestionService ingestionService, Exception e) {
        ScriptInfo scriptInfo = ingestionService.scriptInfo();
        String email = scriptInfo.getEmail();
        String exceptionMessage = e.getMessage();
        System.out.printf("Send Email to %s - reason %s", email, exceptionMessage);
    }

//    read the file from the resources as string. this could just as easy be downloaded from github
    private String getFileContents() throws IOException {
        File file = vendorIngestionService.getFile();
        return FileUtils.readFileToString(file, StandardCharsets.US_ASCII);
    }
}
