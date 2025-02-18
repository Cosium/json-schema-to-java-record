package com.cosium.json_schema_to_java_record;

import static java.util.Objects.requireNonNull;

import com.palantir.javapoet.ClassName;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Réda Housni Alaoui
 */
record JsonSchema(String fileRelativeName, JsonSchemaContent content) {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonSchema.class);

  JsonSchema {
    requireNonNull(fileRelativeName);
    requireNonNull(content);
  }

  public void writeJavaType(JavaTypes javaTypes, Configuration configuration) {
    LOGGER.debug("Writing java types of {}", this);
    content.writeJavaType(
        javaTypes, configuration, createDefaultJavaClassName(configuration), false);
  }

  private ClassName createDefaultJavaClassName(Configuration configuration) {

    return ClassName.get(
        configuration.rootPackage(),
        Stream.of(fileRelativeName.split("\\.")[0].split("-"))
            .map(this::capitalize)
            .collect(Collectors.joining("")));
  }

  private String capitalize(String text) {
    return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
  }
}
