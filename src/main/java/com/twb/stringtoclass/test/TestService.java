package com.twb.stringtoclass.test;

import com.twb.stringtoclass.StringToClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class TestService {
    private static final String VENDOR_SERVICE_V1 = "VendorIngestionServiceV1.groovy";
    private static final String VENDOR_SERVICE_V2 = "VendorIngestionServiceV2.groovy";
    private static final String VENDOR_SERVICE_V3 = "VendorIngestionServiceV3.groovy";

    @Autowired
    private StringToClassService service;

    @Autowired
    private ApplicationContext context;

    @PostConstruct
    public void test() {
        System.out.println("BeanTestService.test");

        try {
            service.execute(VENDOR_SERVICE_V1);
            service.execute(VENDOR_SERVICE_V2);
            service.execute(VENDOR_SERVICE_V3);

            System.out.println(service.getIngestionServiceMap());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
