package com.twb.stringtoclass.config.ingestion;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//@Component
public class IngestionInitializer {
    private static final String VENDOR_SERVICE_V1 = "VendorIngestionServiceV1.groovy";
    private static final String VENDOR_SERVICE_V2 = "VendorIngestionServiceV2.groovy";
    private static final String VENDOR_SERVICE_V3 = "VendorIngestionServiceV3.groovy";

    private static final List<String> GITHUB_FILE_LIST
            = Arrays.asList(VENDOR_SERVICE_V1, VENDOR_SERVICE_V2, VENDOR_SERVICE_V3);

//    @Autowired
    private IngestionConfig ingestionConfig;

//    @Scheduled(fixedDelay = 1000)
    public void run() {
        try {
            List<String> githubScriptFiles = getGithubFiles();

            for (String githubScriptFile : githubScriptFiles) {
                ingestionConfig.addService(githubScriptFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> getGithubFiles() throws IOException {
        List<String> githubScriptFiles = new ArrayList<>();
        for (String githubFile : GITHUB_FILE_LIST) {
            String fileContent = getFileContents(githubFile);
            githubScriptFiles.add(fileContent);
        }
        return githubScriptFiles;
    }

    //    read the file from the resources as string. this could just as easy be downloaded from github
    private String getFileContents(String fileName) throws IOException {
        Resource resource = new ClassPathResource(fileName);
        File file = resource.getFile();
        return FileUtils.readFileToString(file, StandardCharsets.US_ASCII);
    }
}
