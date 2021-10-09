package com.twb.stringtoclass.beantest;

import com.twb.stringtoclass.StringToClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class BeanTestService {
    private static final String VENDOR_SERVICE_V1 = "VendorIngestionServiceV1.groovy";
    private static final String VENDOR_SERVICE_V2 = "VendorIngestionServiceV2.groovy";
    private static final String VENDOR_SERVICE_V3 = "VendorIngestionServiceV3.groovy";
    private static final String BEAN_NAME = "vendor";
    private static final String LINE = "\n----------------------------------";

    @Autowired
    private StringToClassService service;

    @Autowired
    private ApplicationContext context;

    @PostConstruct
    public void test() throws Exception {

        service.execute(VENDOR_SERVICE_V1);
        service.execute(VENDOR_SERVICE_V2);
        service.execute(VENDOR_SERVICE_V3);

        System.out.println(service.getIngestionServiceMap());
        service.getIngestionServiceMap().clear();
        System.out.println(service.getIngestionServiceMap());

    }


}
