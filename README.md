[![Build Status](https://github.com/Cosium/json-schema-to-java-record/actions/workflows/ci.yml/badge.svg)](https://github.com/Cosium/json-schema-to-java-record/actions/workflows/ci.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/com.cosium.json_schema_to_java_record/json-schema-to-java-record)](https://central.sonatype.com/artifact/com.cosium.json_schema_to_java_record/json-schema-to-java-record)

# JSON Schema To Java Record

An annotation processor converting JSON schemas to java records.

# Quick start

1. Import the BOM:
   ```xml
   <dependencyManagement>
     <dependencies>
       <!-- ... -->
       <dependency>
         <groupId>com.cosium.json_schema_to_java_record</groupId>
         <artifactId>json-schema-to-java-record-bom</artifactId>
         <version>${json-schema-to-java-record.version}</version>
         <scope>import</scope>
         <type>pom</type>
       </dependency>
       <!-- ... -->
     </dependencies>
   </dependencyManagement>
   ```
2. Import the API:
   ```xml
   <dependencies>
     <!-- ... -->
     <dependency>
       <groupId>com.cosium.json_schema_to_java_record</groupId>
       <artifactId>json-schema-to-java-record-api</artifactId>
     </dependency>
     <!-- ... -->
   </dependencies>
   ```
3. Import the annotation processor:
   ```xml
   <plugin>
     <groupId>org.apache.maven.plugins</groupId>
     <artifactId>maven-compiler-plugin</artifactId>
     <configuration>
       <annotationProcessorPaths>
         <path>
           <groupId>com.cosium.json_schema_to_java_record</groupId>
           <artifactId>json-schema-to-java-record</artifactId>
         </path>
       </annotationProcessorPaths>
     </configuration>
   </plugin>
   ```
4. Add your JSON schema files to the class path:
   ```
   src/main/resources/com/aqme
   └── foo.schema.json
   ```
5. Annotate a `package-info.java` file like this:
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
6. Compile to generate the java files