
#file=$(<src/main/resources/VendorIngestionServiceV1.groovy)
base64file=$(base64 -i src/main/resources/VendorIngestionServiceV1.groovy)


echo $base64file