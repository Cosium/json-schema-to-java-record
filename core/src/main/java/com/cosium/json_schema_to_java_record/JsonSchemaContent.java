package com.cosium.json_schema_to_java_record;

import com.cosium.json_schema_to_java_record_api.JsonSchemaConfiguration;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Réda Housni Alaoui
 */
record JsonSchemaContent(
    @Nullable String $schema,
    @Nullable String $id,
    Type type,
    @Nullable String format,
    @Nullable JsonSchemaContent items,
    @Nullable List<Object> enumeration,
    Map<String, JsonSchemaContent> properties,
    Set<String> required,
    @Nullable String constValue) {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonSchemaContent.class);

  @JsonCreator
  JsonSchemaContent(
      @JsonProperty("$schema") @Nullable String $schema,
      @JsonProperty("$id") @Nullable String $id,
      @JsonProperty("type") @Nullable String type,
      @JsonProperty("format") @Nullable String format,
      @JsonProperty("items") @Nullable JsonSchemaContent items,
      @JsonProperty("enum") @Nullable List<Object> enumeration,
      @JsonProperty("properties") @Nullable Map<String, JsonSchemaContent> properties,
      @JsonProperty("required") @Nullable Set<String> required,
      @JsonProperty("const") @Nullable String constValue) {
    this(
        $schema,
        $id,
        Type.parseRawValue(type),
        format,
        items,
        enumeration,
        Optional.ofNullable(properties).orElseGet(Map::of),
        Optional.ofNullable(required).orElseGet(Set::of),
        constValue);
  }

  public TypeName writeJavaType(
      JavaTypes javaTypes,
      Configuration configuration,
      ClassName fallbackClassName,
      boolean preferPrimitive) {

    if (constValue != null) {
      return ClassName.get(String.class);
    }

    final TypeName processedTypeName =
        switch (type()) {
          case STRING -> {
            if ("date-time".equals(format)) {
              yield ClassName.get(ZonedDateTime.class);
            }
            if ("uri".equals(format)) {
              yield ClassName.get(URI.class);
            }
            yield ClassName.get(String.class);
          }
          case ARRAY ->
              ParameterizedTypeName.get(
                  ClassName.get(List.class),
                  items.writeJavaType(javaTypes, configuration, fallbackClassName, false));
          case NUMBER -> {
            if (preferPrimitive) {
              yield TypeName.DOUBLE;
            }
            yield ClassName.get(Double.class);
          }
          case INTEGER -> {
            if (preferPrimitive) {
              yield TypeName.INT;
            }
            yield ClassName.get(Integer.class);
          }
          case BOOLEAN -> {
            if (preferPrimitive) {
              yield TypeName.BOOLEAN;
            }
            yield ClassName.get(Boolean.class);
          }
          case NULL -> ClassName.get(Void.class);
          case OBJECT -> null;
        };
    if (processedTypeName != null) {
      LOGGER.debug("{} already processed. Skipping.", processedTypeName);
      return processedTypeName;
    }

    JsonSchemaConfiguration schemaConfiguration =
        Optional.ofNullable($id()).flatMap(configuration::bySchemaId).orElse(null);

    ClassName className =
        Optional.ofNullable(schemaConfiguration)
            .map(JsonSchemaConfiguration::javaTypeQualifiedName)
            .map(ClassName::bestGuess)
            .orElse(fallbackClassName);

    if (javaTypes.existsOnClassPath(className)) {
      LOGGER.info("{} already exists on the class path. Skipping.", className);
      return className;
    }

    Modifier[] javaTypeVisibility =
        switch (Optional.ofNullable(schemaConfiguration)
            .map(JsonSchemaConfiguration::javaTypeVisibility)
            .orElse(JsonSchemaConfiguration.JavaTypeVisibility.PUBLIC)) {
          case PUBLIC -> new Modifier[] {Modifier.PUBLIC};
          case DEFAULT -> new Modifier[0];
        };

    TypeSpec.Builder typeBuilder;
    if (enumeration != null) {
      typeBuilder = TypeSpec.enumBuilder(className).addModifiers(javaTypeVisibility);
      enumeration.stream().map(String::valueOf).forEach(typeBuilder::addEnumConstant);
    } else {
      typeBuilder = TypeSpec.recordBuilder(className).addModifiers(javaTypeVisibility);

      MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
      MethodSpec.Builder compactConstructorBuilder =
          MethodSpec.compactConstructorBuilder().addModifiers(javaTypeVisibility);

      for (Map.Entry<String, JsonSchemaContent> property : properties().entrySet()) {

        String propertyName = property.getKey();
        JsonSchemaContent propertySchema = property.getValue();

        String propertyConstValue = propertySchema.constValue();
        if (propertyConstValue != null) {
          typeBuilder.addMethod(
              addNotNullRelatedAnnotations(
                      javaTypes,
                      addJsonRelatedAnnotations(
                          MethodSpec.methodBuilder(propertyName), propertyName))
                  .addModifiers(javaTypeVisibility)
                  .returns(String.class)
                  .addStatement("return $S", propertyConstValue)
                  .build());
          continue;
        }

        TypeName propertyType =
            propertySchema.writeJavaType(
                javaTypes,
                configuration,
                createClassNameFromPropertyName(configuration, propertyName),
                required.contains(propertyName));

        ParameterSpec.Builder parameterSpecBuilder =
            ParameterSpec.builder(propertyType, propertyName);

        addJsonRelatedAnnotations(parameterSpecBuilder, propertyName);

        if (!propertyType.isPrimitive()) {
          addNullRelatedAnnotations(javaTypes, parameterSpecBuilder, propertyName);
        }

        constructorBuilder.addParameter(parameterSpecBuilder.build());

        boolean nonNull = required.contains(propertyName);
        if (isList(propertyType)) {
          if (nonNull) {
            compactConstructorBuilder.addStatement(
                "$N = $T.ofNullable($N).map($T::copyOf).orElseGet($T::of)",
                propertyName,
                Optional.class,
                propertyName,
                List.class,
                List.class);
          } else {
            compactConstructorBuilder.addStatement(
                "$N = $T.ofNullable($N).map($T::copyOf).orElse(null)",
                propertyName,
                Optional.class,
                propertyName,
                List.class);
          }
        } else if (!propertyType.isPrimitive() && nonNull) {
          compactConstructorBuilder.addStatement(
              "$T.requireNonNull($N)", Objects.class, propertyName);
        }
      }
      typeBuilder
          .recordConstructor(constructorBuilder.build())
          .addMethod(compactConstructorBuilder.build());

      addRecordBuilderRelatedAnnotations(javaTypes, typeBuilder);
      addJsonRelatedAnnotations(typeBuilder);
    }

    Optional.ofNullable(schemaConfiguration)
        .map(JsonSchemaConfiguration::javaInterfaceQualifiedNames)
        .map(Set::of)
        .orElseGet(Set::of)
        .stream()
        .map(ClassName::bestGuess)
        .forEach(typeBuilder::addSuperinterface);

    javaTypes.write($id, className.packageName(), typeBuilder);

    return className;
  }

  private boolean isList(TypeName type) {
    ClassName className;
    if (type instanceof ClassName classNameCandidate) {
      className = classNameCandidate;
    } else if (type instanceof ParameterizedTypeName parameterizedTypeName) {
      className = parameterizedTypeName.rawType();
    } else {
      return false;
    }
    return className.canonicalName().equals(List.class.getCanonicalName());
  }

  private void addJsonRelatedAnnotations(ParameterSpec.Builder builder, String propertyName) {

    builder.addAnnotation(
        AnnotationSpec.builder(JsonProperty.class).addMember("value", "$S", propertyName).build());
  }

  private MethodSpec.Builder addJsonRelatedAnnotations(
      MethodSpec.Builder builder, String propertyName) {

    return builder.addAnnotation(
        AnnotationSpec.builder(JsonProperty.class).addMember("value", "$S", propertyName).build());
  }

  private void addJsonRelatedAnnotations(TypeSpec.Builder typeBuilder) {

    typeBuilder.addAnnotation(
        AnnotationSpec.builder(JsonInclude.class)
            .addMember(
                "value", "$T.$L", JsonInclude.Include.class, JsonInclude.Include.NON_NULL.name())
            .build());
  }

  private void addNullRelatedAnnotations(
      JavaTypes javaTypes, ParameterSpec.Builder parameterBuilder, String propertyName) {

    boolean notNull = required.contains(propertyName);

    if (notNull && javaTypes.existsOnClassPath(ClassName.get(NonNull.class))) {
      parameterBuilder.addAnnotation(NonNull.class);
    } else if (!notNull && javaTypes.existsOnClassPath(ClassName.get(Nullable.class))) {
      parameterBuilder.addAnnotation(Nullable.class);
    }
  }

  private MethodSpec.Builder addNotNullRelatedAnnotations(
      JavaTypes javaTypes, MethodSpec.Builder builder) {
    if (!javaTypes.existsOnClassPath(ClassName.get(NonNull.class))) {
      return builder;
    }
    return builder.addAnnotation(NonNull.class);
  }

  private void addRecordBuilderRelatedAnnotations(
      JavaTypes javaTypes, TypeSpec.Builder typeBuilder) {

    ClassName recordBuilderClassName =
        ClassName.get("io.soabase.recordbuilder.core", "RecordBuilder");
    if (!javaTypes.existsOnClassPath(recordBuilderClassName)) {
      return;
    }
    typeBuilder.addAnnotation(recordBuilderClassName);
  }

  private ClassName createClassNameFromPropertyName(
      Configuration configuration, String propertyName) {

    return ClassName.get(
        configuration.rootPackage(),
        propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1));
  }

  public enum Type {
    STRING,
    NUMBER,
    INTEGER,
    OBJECT,
    ARRAY,
    BOOLEAN,
    NULL;

    public static Type parseRawValue(@Nullable String type) {
      if (type == null) {
        return OBJECT;
      }
      return valueOf(type.toUpperCase());
    }
  }
}
