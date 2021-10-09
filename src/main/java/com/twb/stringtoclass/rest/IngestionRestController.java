package com.twb.stringtoclass.rest;

import com.twb.stringtoclass.config.handler.IngestionHandler;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/ingestion")
public class IngestionRestController {

    @Autowired
    private IngestionHandler handler;

    @PostMapping("/add-service")
    public void addService(@RequestBody AddServiceRequest addServiceRequest) throws Exception {
        String fileContentBase64 = addServiceRequest.getFileContentBase64();
        byte[] decodedBytes = Base64.getUrlDecoder().decode(fileContentBase64);
        String fileContent = new String(decodedBytes);
        System.out.println("adding service:\n" + fileContent);
        handler.addService(fileContent);
    }

    @PostMapping("/execute/{name}")
    public ResponseEntity<String> executeService(@PathVariable("name") String name) throws Exception {
        if (handler.executeService(name)) {
            return ResponseEntity.ok("Service Executed");
        }
        return ResponseEntity.badRequest().body("Service did not execute");
    }

    @Data
    public static class AddServiceRequest {
        private String fileContentBase64;
    }
}
