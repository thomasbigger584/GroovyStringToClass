import com.twb.stringtoclass.ingestion.IngestionService
import com.twb.stringtoclass.ingestion.ScriptInfo

@ScriptInfo(name = "vendor-name", email = "tbigg@email.com", version = 3, maxTries = 5)
class VendorIngestionServiceV3 implements IngestionService {

    static final String ENV_VAR_PROPERTY = "vendor.property.test"

    String testProperty

    @Override
    void init(ScriptContext context) {
        testProperty = getTestProperty(context)
    }

    @Override
    void execute(ScriptContext context, ExecuteParams params) {
        def tryCount = params.retryContext.retryCount + 1
        println "VendorIngestionServiceV3.onExecute try count - $tryCount"
        println "testProperty = $testProperty"

        def info = context.scriptInfo
        println "name = ${info.name()}"

        throw new Exception("Some error occured in script")
    }

    static String getTestProperty(ScriptContext context) {
        return context.applicationContext
                .environment.getProperty(ENV_VAR_PROPERTY);
    }
}
