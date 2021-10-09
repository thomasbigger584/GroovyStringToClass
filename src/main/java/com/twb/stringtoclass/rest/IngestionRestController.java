package com.twb.stringtoclass.rest;

import com.twb.stringtoclass.config.IngestionConfig;
import com.twb.stringtoclass.ingestion.IngestionService;
import com.twb.stringtoclass.config.helper.IngestionHandler;
import org.eclipse.xtend.lib.annotations.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/ingestion")
public class IngestionRestController {

    @Autowired
    private IngestionConfig ingestionConfig;

    @Autowired
    private IngestionHandler handler;

    @PostMapping("/add-service")
    public void addService(@RequestBody AddServiceRequest addServiceRequest) {
        String fileContentBase64 = addServiceRequest.getFileContentBase64();
        byte[] decodedBytes = Base64.getUrlDecoder().decode(fileContentBase64);
        String fileContent = new String(decodedBytes);
        System.out.println("adding service:\n" + fileContent);
        ingestionConfig.addService(fileContent);
    }

    @PostMapping("/execute/{name}")
    public ResponseEntity<String> execute(@PathVariable("name") String name) throws Exception {
        Map<String, IngestionService> registeredServices = ingestionConfig.getRegisteredServices();
        if (registeredServices.containsKey(name)) {
            IngestionService ingestionService = registeredServices.get(name);
            handler.execute(ingestionService);
            return ResponseEntity.ok("Service Executed");
        } else {
            throw new Exception("Service with name " + name + " not found");
        }
    }

    @Data
    public static class AddServiceRequest {
        private String fileContentBase64;

        public String getFileContentBase64() {
            return fileContentBase64;
        }

        public void setFileContentBase64(String fileContentBase64) {
            this.fileContentBase64 = fileContentBase64;
        }
    }
}
