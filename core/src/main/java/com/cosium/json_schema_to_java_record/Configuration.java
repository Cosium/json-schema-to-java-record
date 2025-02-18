package com.cosium.json_schema_to_java_record;

import static java.util.Objects.requireNonNull;

import com.cosium.json_schema_to_java_record_api.GenerateRecordsFromJsonSchemas;
import com.cosium.json_schema_to_java_record_api.JsonSchemaConfiguration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Réda Housni Alaoui
 */
class Configuration {

  private final String rootPackage;
  private final Map<String, JsonSchemaConfiguration> map;

  public Configuration(String rootPackage, GenerateRecordsFromJsonSchemas annotation) {
    this.rootPackage = requireNonNull(rootPackage);
    map =
        Arrays.stream(annotation.schemaConfigurations())
            .collect(Collectors.toMap(JsonSchemaConfiguration::schemaId, Function.identity()));
  }

  public String rootPackage() {
    return rootPackage;
  }

  public Optional<JsonSchemaConfiguration> bySchemaId(String schemaId) {
    return Optional.ofNullable(map.get(schemaId));
  }
}
