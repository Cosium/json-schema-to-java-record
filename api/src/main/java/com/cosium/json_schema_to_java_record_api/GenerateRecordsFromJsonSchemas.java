package com.cosium.json_schema_to_java_record_api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author Réda Housni Alaoui
 */
@Target({ElementType.PACKAGE})
public @interface GenerateRecordsFromJsonSchemas {

  JsonSchemaFileLocation[] schemaRootFileLocations();

  JsonSchemaConfiguration[] schemaConfigurations() default {};

  /**
   * @return A non-empty string to generate a report class. Empty otherwise.
   */
  String reportClassQualifiedName() default "";
}
