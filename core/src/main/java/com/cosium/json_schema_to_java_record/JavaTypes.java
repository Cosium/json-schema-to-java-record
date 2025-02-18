package com.cosium.json_schema_to_java_record;

import static java.util.Objects.requireNonNull;

import com.cosium.json_schema_to_java_record_api.GenerateRecordsFromJsonSchemas;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import javax.tools.StandardLocation;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Réda Housni Alaoui
 */
class JavaTypes implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(JavaTypes.class);

  private final Filer filer;
  @Nullable private final ClassName reportClassName;
  private final Map<String, ClassName> classNameBySchemaId = new HashMap<>();

  JavaTypes(Filer filer, GenerateRecordsFromJsonSchemas annotation) {
    this.filer = requireNonNull(filer);
    reportClassName =
        Optional.of(annotation.reportClassQualifiedName())
            .filter(Predicate.not(String::isBlank))
            .map(ClassName::bestGuess)
            .orElse(null);
  }

  public boolean existsOnClassPath(ClassName className) {
    try {
      filer.getResource(
          StandardLocation.CLASS_PATH, className.packageName(), className.simpleName() + ".class");
      return true;
    } catch (IOException | IllegalArgumentException e) {
      return false;
    }
  }

  public void write(
      @Nullable String schemaId, String packageName, TypeSpec.Builder typeSpecBuilder) {

    TypeSpec typeSpec = markAsGenerated(typeSpecBuilder).build();

    if (schemaId != null) {
      classNameBySchemaId.put(schemaId, ClassName.get(packageName, typeSpec.name()));
    }

    write(JavaFile.builder(packageName, typeSpec).build());
  }

  private void write(JavaFile javaFile) {
    LOGGER.debug("Writing {}", javaFile);
    try {
      javaFile.writeTo(filer);
    } catch (FilerException e) {
      if (e.getMessage().contains("Attempt to recreate a file")) {
        LOGGER.debug("{} already exists. Skipping write.", javaFile);
        return;
      }
      throw new UncheckedIOException(e);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void close() {
    if (reportClassName == null) {
      return;
    }

    TypeSpec.Builder reportBuilder =
        markAsGenerated(TypeSpec.classBuilder(reportClassName))
            .addModifiers(Modifier.PUBLIC)
            .addModifiers(Modifier.FINAL)
            .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build());

    CodeBlock classBySchemaIdFieldEntries =
        classNameBySchemaId.entrySet().stream()
            .map(
                schemaIdToClassName ->
                    CodeBlock.of(
                        "$T.entry($S,$T.class)",
                        Map.class,
                        schemaIdToClassName.getKey(),
                        schemaIdToClassName.getValue()))
            .collect(CodeBlock.joining(","));

    FieldSpec classBySchemaIdField =
        FieldSpec.builder(
                ParameterizedTypeName.get(Map.class, String.class, Class.class),
                "CLASS_BY_SCHEMA_ID")
            .addModifiers(Modifier.PUBLIC)
            .addModifiers(Modifier.STATIC)
            .addModifiers(Modifier.FINAL)
            .initializer("$T.ofEntries($L)", Map.class, classBySchemaIdFieldEntries)
            .build();

    reportBuilder.addField(classBySchemaIdField);

    write(JavaFile.builder(reportClassName.packageName(), reportBuilder.build()).build());
  }

  private TypeSpec.Builder markAsGenerated(TypeSpec.Builder typeBuilder) {
    return typeBuilder.addAnnotation(
        AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", GenerateRecordsFromJsonSchemas.class.getName())
            .build());
  }
}
