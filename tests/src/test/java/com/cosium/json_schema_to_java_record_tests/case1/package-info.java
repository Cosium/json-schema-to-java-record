/**
 * @author Réda Housni Alaoui
 */
@GenerateRecordsFromJsonSchemas(
    schemaRootFileLocations =
        @JsonSchemaFileLocation(
            moduleAndPackage = "com.cosium.json_schema_to_java_record_tests.case1",
            relativeName = "customer.json"),
    schemaConfigurations = {
      @JsonSchemaConfiguration(
          schemaId = "customer",
          javaTypeQualifiedName = "com.cosium.json_schema_to_java_record_tests.case1.Customer"),
      @JsonSchemaConfiguration(
          schemaId = "address",
          javaTypeQualifiedName = "com.cosium.json_schema_to_java_record_tests.case1.Address"),
      @JsonSchemaConfiguration(
          schemaId = "country",
          javaTypeQualifiedName = "com.cosium.json_schema_to_java_record_tests.case1.Country",
          javaInterfaceQualifiedNames =
              "com.cosium.json_schema_to_java_record_tests.case1.Location"),
    })
package com.cosium.json_schema_to_java_record_tests.case1;

import com.cosium.json_schema_to_java_record_api.GenerateRecordsFromJsonSchemas;
import com.cosium.json_schema_to_java_record_api.JsonSchemaConfiguration;
import com.cosium.json_schema_to_java_record_api.JsonSchemaFileLocation;
