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
   └── customer.json
   ```
   ```json
   {
     "$schema": "https://json-schema.org/draft/2020-12/schema",
     "$id": "customer",
     "type": "object",
     "properties": {
       "firstName": {
         "type": "string"
       },
       "lastName": {
         "type": "string"
       }
     },
     "required": [
       "firstName",
       "lastName"
     ]
   }
   ```
4. Annotate a `package-info.java` file like this:
   ```java
   @GenerateRecordsFromJsonSchemas(
     schemaRootFileLocations =
     @JsonSchemaFileLocation(
       moduleAndPackage = "com.aqme",
       relativeName = "customer.json"
     )
   )
   package com.aqme;
   
   import com.cosium.json_schema_to_java_record_api.GenerateRecordsFromJsonSchemas;
   import com.cosium.json_schema_to_java_record_api.JsonSchemaConfiguration;
   import com.cosium.json_schema_to_java_record_api.JsonSchemaFileLocation;
   ```
5. Compile to generate this kind of output:
   ```java
   package com.aqme;
   
   import com.fasterxml.jackson.annotation.JsonInclude;
   import com.fasterxml.jackson.annotation.JsonProperty;
   import java.util.Objects;
   import javax.annotation.processing.Generated;
   import org.jspecify.annotations.NonNull;
   import org.jspecify.annotations.Nullable;
   
   @JsonInclude(JsonInclude.Include.NON_NULL)
   @Generated("com.cosium.json_schema_to_java_record_api.GenerateRecordsFromJsonSchemas")
   public record Customer(
       @JsonProperty("firstName") @NonNull String firstName,
       @JsonProperty("lastName") @NonNull String lastName) {
     public Customer {
       Objects.requireNonNull(firstName);
       Objects.requireNonNull(lastName);
     }
   }
   ```

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

Record components will be annotated with [Jackson annotations](https://github.com/FasterXML/jackson-annotations) .

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

# Customization

Most customizations rely on the JSON schema `$id` attribute (aka `schemaId` on the java API side).
Make sure this attribute is valued and unique to benefit from customizations using it.

## Forcing the Java type qualified name of a particular schema

You can ask a particular `schemaId`
to be bound to an explicit Java type qualified name via `JsonSchemaConfiguration#javaTypeQualifiedName`.

Example of JSON schema `country.json`:
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "country",
  "enum": ["FRANCE", "MOROCCO"]
}
```

Example of configuration:
```java
@GenerateRecordsFromJsonSchemas(
    schemaRootFileLocations =
        @JsonSchemaFileLocation(
            moduleAndPackage = "com.cosium.json_schema_to_java_record_tests.case1",
            relativeName = "country.json"),
    schemaConfigurations = 
      @JsonSchemaConfiguration(
          schemaId = "country",
          javaTypeQualifiedName = "com.cosium.json_schema_to_java_record_tests.case1.Country")
    )
package com.cosium.json_schema_to_java_record_tests.case1;
```

Example of generated java class:
```java
package com.cosium.json_schema_to_java_record_tests.case1;

import javax.annotation.processing.Generated;

@Generated("com.cosium.json_schema_to_java_record_api.GenerateRecordsFromJsonSchemas")
public enum Country {
  FRANCE,
  MOROCCO
}

```

## Making a generated type implement interfaces

You can ask a generated Java type
to implement a list of interfaces via `JsonSchemaConfiguration#javaInterfaceQualifiedNames`.

Example of JSON schema `country.json`:
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "country",
  "enum": ["FRANCE", "MOROCCO"]
}
```

Example of configuration:
```java
@GenerateRecordsFromJsonSchemas(
    schemaRootFileLocations =
        @JsonSchemaFileLocation(
            moduleAndPackage = "com.cosium.json_schema_to_java_record_tests.case1",
            relativeName = "country.json"),
    schemaConfigurations =
    @JsonSchemaConfiguration(
            schemaId = "country",
            javaInterfaceQualifiedNames =
                    "com.cosium.json_schema_to_java_record_tests.case1.Location")
    )
package com.cosium.json_schema_to_java_record_tests.case1;
```

Example of generated java class:
```java
import com.cosium.json_schema_to_java_record_tests.case1.Location;
import javax.annotation.processing.Generated;

@Generated("com.cosium.json_schema_to_java_record_api.GenerateRecordsFromJsonSchemas")
public enum Country implements Location {
   FRANCE,
   MOROCCO
}
```

## Report generation

You can ask for the creation of a Java class generation report
by providing a non-empty value to `GenerateRecordsFromJsonSchemas#reportClassQualifiedName`.

The generated report class will contain:
- A public constant `Map<String, Class> CLASS_BY_SCHEMA_ID` mapping each generated class to its JSON schema `$id`. If the latter was missing, there will be no entry in the Map.

Example of configuration:
```java
@GenerateRecordsFromJsonSchemas(
  schemaRootFileLocations =
    @JsonSchemaFileLocation(
      moduleAndPackage = "com.cosium.json_schema_to_java_record_tests.case1",
      relativeName = "customers.json"
    ),
  reportClassQualifiedName = "com.cosium.json_schema_to_java_record_tests.case1.Report"   
)
package com.aqme;
   
import com.cosium.json_schema_to_java_record_api.GenerateRecordsFromJsonSchemas;
import com.cosium.json_schema_to_java_record_api.JsonSchemaFileLocation;

```

Example of generated report class:
```java
package com.cosium.json_schema_to_java_record_tests.case1;

import java.lang.Class;
import java.lang.String;
import java.util.Map;
import javax.annotation.processing.Generated;

@Generated("com.cosium.json_schema_to_java_record_api.GenerateRecordsFromJsonSchemas")
public final class Report {
  public static final Map<String, Class> CLASS_BY_SCHEMA_ID = Map.ofEntries(Map.entry("country",Country.class),Map.entry("address",Address.class),Map.entry("customers",Customers.class),Map.entry("customer",Customer.class));

  private Report() {
  }
}
```