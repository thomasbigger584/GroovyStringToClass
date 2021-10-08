import com.twb.stringtoclass.ingestion.IngestionService

class VendorIngestionService extends IngestionService {
    static final String VENDOR_NAME = "vendor"
    static final String EMAIL = "tbigg@email.com"
    static final int VERSION = 1

    static final String BUCKET_NAME = "bucket-name"
    static final String UPLOAD_PATH = "path/to/file/fileName.csv"
    static final String ENV_VAR_PROPERTY = "vendor.property.test"
    static final long LONG_RUNNING_PROCESS_SLEEP = 10000L

    String testProperty

    @Override
    void onInit() {
        testProperty = getTestProperty();
    }

    @Override
    void onExecute() throws Exception {
        println "VendorIngestionService.onExecute"
        println "testProperty = $testProperty"

        // simulate doing some long process
        Thread.sleep(LONG_RUNNING_PROCESS_SLEEP)

        persistence.persist(BUCKET_NAME, UPLOAD_PATH)
    }

    String getTestProperty() {
        // private methods seem to throw exception
        return environment.getProperty(ENV_VAR_PROPERTY);
    }

    ScriptInfo scriptInfo() {
        return ScriptInfo.builder()
                .vendor(VENDOR_NAME)
                .email(EMAIL)
                .version(VERSION)
                .build();
    }
}
