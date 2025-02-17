package com.cosium.json_schema_to_java_record_api;

/**
 * @author Réda Housni Alaoui
 */
public @interface JsonSchemaFileLocation {
  /**
   * e.g. "com.aqme"
   *
   * <p>Use empty string "" for the root package
   */
  String moduleAndPackage();

  /** e.g. "foo.schema.json" */
  String relativeName();
}
