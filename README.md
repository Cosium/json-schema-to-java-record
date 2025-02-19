[![Build Status](https://github.com/Cosium/json-schema-to-java-record/actions/workflows/ci.yml/badge.svg)](https://github.com/Cosium/json-schema-to-java-record/actions/workflows/ci.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/com.cosium.json_schema_to_java_record/json-schema-to-java-record)](https://central.sonatype.com/artifact/com.cosium.json_schema_to_java_record/json-schema-to-java-record)

# JSON Schema To Java Record

An annotation processor converting JSON schemas to java records.

# Quick start

1. Import the API:
   ```xml
   <dependencies>
     <!-- ... -->
     <dependency>
       <groupId>com.cosium.json_schema_to_java_record</groupId>
       <artifactId>json-schema-to-java-record-api</artifactId>
       <version>${json-schema-to-java-record.version}</version>
       <scope>provided</scope>
     </dependency>
     <!-- ... -->
   </dependencies>
   ```
2. Import the annotation processor:
   ```xml
   <plugin>
     <groupId>org.apache.maven.plugins</groupId>
     <artifactId>maven-compiler-plugin</artifactId>
     <configuration>
       <annotationProcessorPaths>
         <path>
           <groupId>com.cosium.json_schema_to_java_record</groupId>
           <artifactId>json-schema-to-java-record</artifactId>
           <version>${json-schema-to-java-record.version}</version>
         </path>
       </annotationProcessorPaths>
     </configuration>
   </plugin>
   ```
3. Add your JSON schema files to the class path:
   ```
   src/main/resources/com/aqme
   └── foo.schema.json
   ```
4. Annotate a `package-info.java` file like this:
   ```java
   @GenerateRecordsFromJsonSchemas(
     schemaRootFileLocations =
     @JsonSchemaFileLocation(
       moduleAndPackage = "com.aqme",
       relativeName = "foo.schema.json"
     )
   )
   package com.aqme;
   
   import com.cosium.json_schema_to_java_record_api.GenerateRecordsFromJsonSchemas;
   import com.cosium.json_schema_to_java_record_api.JsonSchemaConfiguration;
   import com.cosium.json_schema_to_java_record_api.JsonSchemaFileLocation;
   ```
5. Compile to generate the java files

# Type mapping

| JSON `type` | JSON `format` | JSON required | Java type               |
|-------------|---------------|---------------|-------------------------|
| object      |               |               | A java record           |
| string      | date-time     |               | java.time.ZonedDateTime |
| string      | uri           |               | java.net.URI            |
| string      |               |               | java.lang.String        |
| array       |               |               | java.util.List          |
| number      |               | required      | double                  |
| number      |               | non required  | java.lang.Double        |
| integer     |               | required      | int                     |
| integer     |               | non required  | java.lang.Integer       |
| boolean     |               | required      | boolean                 |
| boolean     |               | non required  | java.lang.Boolean       |
| null        |               |               | java.lang.Void          |

# JSON enum

A schema having a non-null `enum` array will be converted to a java enum.

# Java JSON binding

Record components will be annotated with [Jackson annotations](https://github.com/FasterXML/jackson-annotations)

# Nullability

JSON schema [required](https://json-schema.org/understanding-json-schema/reference/object#required) is supported.
By default, a property is considered as nullable. If a property is part of JSON schema [required](https://json-schema.org/understanding-json-schema/reference/object#required) array, it will be considered as non-nullable.

## JSpecify

If [JSpecify](https://jspecify.dev) nullness annotations (`@Nullable` and `@NonNull`) are part of the classpath,
they will be used to annotate generated java record components.

## Array

A non-null `array` will default to an empty immutable `java.util.List`.   

# Builder

## RecordBuilder

If [RecordBuilder](https://github.com/Randgalt/record-builder) annotation `io.soabase.recordbuilder.core.RecordBuilder` 
is part of the classpath, the former will be added to each generated java record.

# $ref

JSON schema [$ref](https://json-schema.org/understanding-json-schema/structuring#dollarref) is supported via the classpath "protocol."

This allows you to have multiple JSON schema files referring to each other.

All `$ref` values should start with `classpath:`, followed by the absolute path to the referred JSON file like this:
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "address",
  "type": "object",
  "properties": {
    "country": {
      "$ref": "classpath:/com/cosium/json_schema_to_java_record_tests/case1/country.json"
    }
  }
}
```