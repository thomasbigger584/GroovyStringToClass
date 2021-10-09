import com.twb.stringtoclass.ingestion.IngestionService
import com.twb.stringtoclass.ingestion.ScriptInfo

@ScriptInfo(name = "vendor-name", email = "tbigg@email.com", version = 1)
class VendorIngestionServiceV1 extends IngestionService {

    static final String BUCKET_NAME = "bucket-name"
    static final String UPLOAD_PATH = "path/to/file/fileName.csv"
    static final String ENV_VAR_PROPERTY = "vendor.property.test"
    static final long LONG_RUNNING_PROCESS_SLEEP = 7000L

    String testProperty

    @Override
    void onInit() {
        testProperty = getTestProperty()
    }

    @Override
    void onExecute(ExecuteParams params) throws Exception {
        def tryCount = params.retryContext.retryCount + 1
        println "VendorIngestionServiceV1.onExecute try count - $tryCount"
        println "testProperty = $testProperty"

        def info = scriptInfo()
        println "name = ${info.name()}"

        // simulate doing some long process
        println "sleeping for $LONG_RUNNING_PROCESS_SLEEP ms"
        Thread.sleep(LONG_RUNNING_PROCESS_SLEEP)

        context.persistenceService.persist(BUCKET_NAME, UPLOAD_PATH)
    }

    String getTestProperty() {
        return context.applicationContext
                .environment.getProperty(ENV_VAR_PROPERTY);
    }
}
