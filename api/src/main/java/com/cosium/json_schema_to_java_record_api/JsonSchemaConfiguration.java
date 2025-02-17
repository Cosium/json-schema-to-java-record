package com.cosium.json_schema_to_java_record_api;

/**
 * @author Réda Housni Alaoui
 */
public @interface JsonSchemaConfiguration {

  /**
   * @return A value that should match the JSON schema $id attribute.
   */
  String schemaId();

  /**
   * Allows forcing the java type qualified name of the schema. If the type already exists, it will
   * be used as is.
   *
   * <p>E.g. "com.aqme.Foo"
   */
  String javaTypeQualifiedName() default "";

  /**
   * @return The interfaces that the generated record must implement.
   */
  String[] javaInterfaceQualifiedNames() default {};
}
