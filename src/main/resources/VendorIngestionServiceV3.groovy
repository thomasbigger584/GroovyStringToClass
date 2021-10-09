import com.twb.stringtoclass.ingestion.IngestionService
import com.twb.stringtoclass.ingestion.ScriptInfo

@ScriptInfo(vendor = "vendor-name", email = "tbigg@email.com", version = 3, maxTries = 5)
class VendorIngestionServiceV3 extends IngestionService {

    static final String ENV_VAR_PROPERTY = "vendor.property.test"

    String testProperty

    @Override
    void onInit() {
        testProperty = getTestProperty()
    }

    @Override
    void onExecute() throws Exception {
        println "VendorIngestionService.onExecute version 3"
        println "testProperty = $testProperty"

        def info = scriptInfo()
        println "vendor = ${info.vendor()}"

        throw new Exception("Some error happened in script")
    }

    String getTestProperty() {
        // private methods seem to throw exception
        return context.applicationContext
                .environment.getProperty(ENV_VAR_PROPERTY);
    }
}
