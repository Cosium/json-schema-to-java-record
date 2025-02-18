package com.cosium.json_schema_to_java_record;

import static java.util.Objects.requireNonNull;

import com.cosium.json_schema_to_java_record_api.GenerateRecordsFromJsonSchemas;
import java.util.Arrays;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;

/**
 * @author Réda Housni Alaoui
 */
record Command(Element annotatedElement, GenerateRecordsFromJsonSchemas annotation) {

  public Command {
    requireNonNull(annotatedElement);
    requireNonNull(annotation);
  }

  public void execute(Filer filer, JsonSchemas jsonSchemas) {
    Configuration configuration = new Configuration(annotatedElementPackageName(), annotation);

    try (JavaTypes javaTypes = new JavaTypes(filer, annotation)) {
      Arrays.stream(annotation.schemaRootFileLocations())
          .map(jsonSchemas::parse)
          .forEach(jsonSchema -> jsonSchema.writeJavaType(javaTypes, configuration));
    }
  }

  private String annotatedElementPackageName() {

    Element visitedElement = annotatedElement;
    while (visitedElement != null) {
      if (visitedElement instanceof PackageElement packageElement) {
        return packageElement.getQualifiedName().toString();
      }
      visitedElement = visitedElement.getEnclosingElement();
    }
    return "";
  }
}
