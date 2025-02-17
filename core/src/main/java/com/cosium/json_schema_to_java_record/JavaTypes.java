package com.cosium.json_schema_to_java_record;

import static java.util.Objects.requireNonNull;

import com.cosium.json_schema_to_java_record_api.GenerateRecordsFromJsonSchemas;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Generated;
import javax.tools.StandardLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Réda Housni Alaoui
 */
class JavaTypes {

  private static final Logger LOGGER = LoggerFactory.getLogger(JavaTypes.class);

  private final Filer filer;

  JavaTypes(Filer filer) {
    this.filer = requireNonNull(filer);
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

  public void write(String packageName, TypeSpec.Builder typeSpecBuilder) {
    typeSpecBuilder.addAnnotation(
        AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", GenerateRecordsFromJsonSchemas.class.getName())
            .build());
    write(JavaFile.builder(packageName, typeSpecBuilder.build()).build());
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
}
