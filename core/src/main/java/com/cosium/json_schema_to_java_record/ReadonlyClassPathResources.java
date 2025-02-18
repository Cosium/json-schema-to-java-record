package com.cosium.json_schema_to_java_record;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.processing.Filer;
import javax.tools.StandardLocation;

/**
 * @author Réda Housni Alaoui
 */
class ReadonlyClassPathResources {

  private final Filer filer;

  ReadonlyClassPathResources(Filer filer) {
    this.filer = requireNonNull(filer);
  }

  public InputStream openInputStream(String moduleAndPackageLocation, String relativeNameLocation) {
    try {
      return filer
          .getResource(StandardLocation.CLASS_PATH, moduleAndPackageLocation, relativeNameLocation)
          .openInputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
