package com.cosium.json_schema_to_java_record;

import static java.util.Objects.requireNonNull;

import io.zenwave360.jsonrefparser.$Ref;
import io.zenwave360.jsonrefparser.resolver.Resolver;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * @author Réda Housni Alaoui
 */
class $RefClasspathResolver implements Resolver {

  private final ReadonlyClassPathResources classPathResources;

  $RefClasspathResolver(ReadonlyClassPathResources classPathResources) {
    this.classPathResources = requireNonNull(classPathResources);
  }

  @Override
  public String resolve($Ref $ref) {
    try {
      URI uri = $ref.getURI();
      if (uri.toString().startsWith("classpath:") && !uri.toString().startsWith("classpath:/")) {
        // gracefully handle classpath: without the slash
        uri = URI.create(uri.toString().replace("classpath:", "classpath:/"));
      }

      String uriPath = uri.getPath().replaceFirst("^/", "");
      int lastIndexOfSlash = uriPath.lastIndexOf("/");

      String packageName = uriPath.substring(0, lastIndexOfSlash).replace("/", ".");
      String relativeName = uriPath.substring(lastIndexOfSlash + 1);
      try (InputStream inputStream =
          classPathResources.openInputStream(packageName, relativeName)) {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      }

    } catch (MissingResourceException missingResourceException) {
      throw missingResourceException;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
