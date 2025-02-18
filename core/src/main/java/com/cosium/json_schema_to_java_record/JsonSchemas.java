package com.cosium.json_schema_to_java_record;

import static java.util.Objects.requireNonNull;

import com.cosium.json_schema_to_java_record_api.JsonSchemaFileLocation;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zenwave360.jsonrefparser.$RefParser;
import io.zenwave360.jsonrefparser.resolver.RefFormat;
import io.zenwave360.jsonrefparser.resolver.Resolver;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Réda Housni Alaoui
 */
class JsonSchemas {

  private final ReadonlyClassPathResources classPathResources;
  private final Resolver resolver;
  private final ObjectMapper objectMapper =
      new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  public JsonSchemas(ReadonlyClassPathResources classPathResources) {
    this.classPathResources = requireNonNull(classPathResources);
    resolver = new $RefClasspathResolver(classPathResources);
  }

  public JsonSchema parse(JsonSchemaFileLocation fileLocation) {
    try (InputStream inputStream =
        classPathResources.openInputStream(
            fileLocation.moduleAndPackage(), fileLocation.relativeName())) {

      JsonSchemaContent jsonSchemaContent =
          objectMapper.convertValue(
              objectMapper.valueToTree(
                  new $RefParser(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8))
                      .withResolver(RefFormat.CLASSPATH, resolver)
                      .parse()
                      .dereference()
                      .getRefs()
                      .schema()),
              JsonSchemaContent.class);
      return new JsonSchema(fileLocation.relativeName(), jsonSchemaContent);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
