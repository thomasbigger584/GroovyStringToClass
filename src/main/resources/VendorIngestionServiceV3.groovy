import com.twb.stringtoclass.ingestion.IngestionService
import com.twb.stringtoclass.ingestion.ScriptInfo

@ScriptInfo(name = "vendor-name", email = "tbigg@email.com", version = 3, maxTries = 5)
class VendorIngestionServiceV3 extends IngestionService {

    static final String ENV_VAR_PROPERTY = "vendor.property.test"

    String testProperty

    @Override
    void onInit() {
        testProperty = getTestProperty()
    }

    @Override
    void onExecute(ExecuteParams params) throws Exception {
        def tryCount = params.retryContext.retryCount + 1
        println "VendorIngestionServiceV3.onExecute try count - $tryCount"
        println "testProperty = $testProperty"

        def info = scriptInfo()
        println "name = ${info.name()}"

        throw new Exception("Some error happened in script")
    }

    String getTestProperty() {
        return context.applicationContext
                .environment.getProperty(ENV_VAR_PROPERTY);
    }
}
