import com.twb.stringtoclass.ingestion.IngestionService

class VendorIngestionService extends IngestionService {
    private static final String BUCKET_NAME = "bucket-name"
    private static final String UPLOAD_PATH = "path/to/file/fileName.csv"
    private static final String ENV_VAR_PROPERTY = "vendor.property.test"
    private static final String VENDOR_NAME = "vendor"

    String testProperty

    @Override
    void onInit() {
        testProperty = getTestProperty();
    }

    @Override
    void onExecute() throws Exception {
        println "VendorIngestionService.onExecute"
        println "testProperty = $testProperty"
        persistence.persist(BUCKET_NAME, UPLOAD_PATH)
    }

    String getTestProperty() {
        // private methods throw exception
        return environment.getProperty(ENV_VAR_PROPERTY);
    }

    @Override
    String getVendorName() {
        return VENDOR_NAME
    }
}
