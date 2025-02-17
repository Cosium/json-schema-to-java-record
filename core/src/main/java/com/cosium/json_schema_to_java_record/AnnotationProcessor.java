package com.cosium.json_schema_to_java_record;

import com.cosium.json_schema_to_java_record_api.GenerateRecordsFromJsonSchemas;
import com.cosium.logging.annotation_processor.AbstractLoggingProcessor;
import com.google.auto.service.AutoService;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Réda Housni Alaoui
 */
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractLoggingProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationProcessor.class);

  private static final Boolean ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS = false;

  private final Set<Command> processedCommands = new CopyOnWriteArraySet<>();

  @Nullable private JavaTypes javaTypes;
  @Nullable private JsonSchemas jsonSchemas;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.javaTypes = new JavaTypes(processingEnv.getFiler());
    jsonSchemas = new JsonSchemas(new ReadonlyClassPathResources(processingEnv.getFiler()));
  }

  @Override
  protected boolean doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver() || annotations.isEmpty()) {
      return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
    }

    Set<Command> commands =
        roundEnv.getElementsAnnotatedWith(GenerateRecordsFromJsonSchemas.class).stream()
            .map(
                element ->
                    new Command(
                        element, element.getAnnotation(GenerateRecordsFromJsonSchemas.class)))
            .filter(Predicate.not(processedCommands::contains))
            .collect(Collectors.toUnmodifiableSet());

    commands.stream()
        .peek(command -> LOGGER.debug("Executing {}", command))
        .forEach(command -> command.execute(javaTypes, jsonSchemas));

    processedCommands.addAll(commands);

    return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(GenerateRecordsFromJsonSchemas.class.getCanonicalName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
