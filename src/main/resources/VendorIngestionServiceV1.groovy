import com.twb.stringtoclass.ingestion.IngestionService
import com.twb.stringtoclass.ingestion.ScriptInfo

@ScriptInfo(vendor = "vendor-name", email = "tbigg@email.com", version = 1)
class VendorIngestionServiceV1 extends IngestionService {
    static final String BUCKET_NAME = "bucket-name"
    static final String UPLOAD_PATH = "path/to/file/fileName.csv"
    static final String ENV_VAR_PROPERTY = "vendor.property.test"
    static final long LONG_RUNNING_PROCESS_SLEEP = 7000L

    String testProperty

    @Override
    void onInit() {
        testProperty = getTestProperty();
    }

    @Override
    void onExecute() throws Exception {
        println "VendorIngestionService.onExecute version 1"
        println "testProperty = $testProperty"

        // simulate doing some long process
        println "sleeping for $LONG_RUNNING_PROCESS_SLEEP ms"
        Thread.sleep(LONG_RUNNING_PROCESS_SLEEP)

        persistence.persist(BUCKET_NAME, UPLOAD_PATH)
    }

    String getTestProperty() {
        // private methods seem to throw exception
        return environment.getProperty(ENV_VAR_PROPERTY);
    }
}
