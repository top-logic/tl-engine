/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.model;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.service.tools.ToolCallTracking;
import com.top_logic.ai.service.tools.config.ConfigSchemaTool;
import com.top_logic.ai.service.tools.model.messages.AnnotationListResponse;
import com.top_logic.ai.service.tools.util.DataClassSerializer;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.json.JsonConfigurationWriter;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

import de.haumacher.msgbuf.io.StringW;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool for browsing annotations on TopLogic model elements (modules, types, parts, enumerations,
 * classifiers).
 *
 * <p>
 * This tool allows AI agents to inspect annotation metadata on model elements with optional
 * inheritance support. It provides detailed information about each annotation including its tag
 * name, configuration interface, and property values.
 * </p>
 *
 * <h3>Example Usage:</h3>
 *
 * <pre>
 * // List local annotations on a module
 * listAnnotations("tl.core", false)
 *
 * // List local annotations on a type
 * listAnnotations("tl.core:TLObject", false)
 *
 * // List all annotations including inherited
 * listAnnotations("tl.core:TLObject", true)
 *
 * // List annotations on an enumeration
 * listAnnotations("tl.core:TLScope", false)
 *
 * // List annotations on a type part
 * listAnnotations("tl.core:TLObject#name", false)
 *
 * // List annotations on a classifier (enum literal)
 * listAnnotations("tl.core:TLScope#global", false)
 * </pre>
 *
 * <h3>Integration:</h3>
 *
 * <p>
 * To use this tool with an AI agent, instantiate it and add to the agent's tools:
 * </p>
 *
 * <pre>
 * ModelAnnotationBrowserTool annotationTool = new ModelAnnotationBrowserTool();
 *
 * ModelExplorer explorer = AgenticServices.agentBuilder(ModelExplorer.class)
 *     .chatModel(explorationModel)
 *     .tools(browserTool, instanceTool, analysisTool, searchTool, annotationTool)
 *     .build();
 * </pre>
 * 
 * @author jhu
 */
public class ModelAnnotationBrowserTool {

	/**
	 * Configuration for {@link ModelAnnotationBrowserTool}.
	 */
	public interface Config extends PolymorphicConfiguration<ModelAnnotationBrowserTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link ModelAnnotationBrowserTool}.
	 */
	public ModelAnnotationBrowserTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link ModelAnnotationBrowserTool} from configuration.
	 */
	@CalledByReflection
	public ModelAnnotationBrowserTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	/**
	 * Lists all annotations on a model element (module, type, enumeration, part, classifier) with
	 * optional inheritance.
	 *
	 * <p>
	 * Target references must be in one of these formats:
	 * </p>
	 * <ul>
	 * <li><b>Module:</b> {@code "moduleName"} (e.g., {@code "tl.core"})</li>
	 * <li><b>Type/Enumeration:</b> {@code "module:TypeName"} (e.g., {@code "tl.core:TLObject"})</li>
	 * <li><b>Part:</b> {@code "module:TypeName#partName"} (e.g., {@code "tl.core:TLObject#name"})</li>
	 * <li><b>Classifier:</b> {@code "module:EnumName#classifierName"} (e.g., {@code "tl.core:TLScope#global"})
	 * </li>
	 * </ul>
	 *
	 * <p>
	 * When {@code includeInherited} is {@code true}:
	 * </p>
	 * <ul>
	 * <li>For types/enumerations: annotations from supertypes are included.</li>
	 * <li>For classifiers: includes both the classifier's own annotations and annotations inherited
	 * from the owning enumeration/type.</li>
	 * <li>For parts: annotations from part definitions and supertypes are included.</li>
	 * <li>For modules: inheritance does not apply (module annotations are returned).</li>
	 * </ul>
	 *
	 * <p>
	 * Inherited annotations are marked with {@code "inherited": true} and include the
	 * {@code "definedIn"} location.
	 * </p>
	 *
	 * @param targetRef
	 *        The target reference in format {@code "module"}, {@code "module:Type"}, or
	 *        {@code "module:Type#part"}.
	 * @param includeInherited
	 *        Whether to include inherited annotations from supertypes/definitions. Default:
	 *        {@code false}.
	 * @return JSON string containing annotation information, or error JSON if the operation fails.
	 */
	@Tool("List all annotations on a model element (module, type, enumeration, part, classifier) with optional inheritance")
	public String listAnnotations(
			@P("Target reference in format 'module', 'module:Type', or 'module:Type#part'") String targetRef,
			@P("Include inherited annotations from supertypes/definitions (default: false)") Boolean includeInherited) {

		try {
			String result = ThreadContextManager.inSystemInteraction(
				ModelAnnotationBrowserTool.class,
				() -> {
					// 1. Validate and default parameters
					if (targetRef == null || targetRef.isEmpty()) {
						throw new IllegalArgumentException("Target reference is required");
					}
					boolean includeInheritedAnnotations = includeInherited != null && includeInherited;

					// 2. Parse target reference
					TargetRefParser parser = TargetRefParser.parse(targetRef);

					// 3. Resolve to TLModelPart
					TLModel model = ModelService.getApplicationModel();
					TLModelPart target = parser.resolve(model);

					// 4. Determine the actual kind based on the resolved target
					String kind = determineKind(target, parser);

					// 5. Collect annotations
					List<InternalAnnotationInfo> annotations =
						AnnotationCollector.collect(target, includeInheritedAnnotations);

					// 6. Build response metadata using data classes
					AnnotationListResponse responseData = AnnotationListResponse.create();
					responseData.setTarget(targetRef);
					responseData.setKind(kind);
					responseData.setModule(parser.getModuleName());
					responseData.setType(parser.getTypeName());

					if ("part".equals(kind)) {
						responseData.setPart(parser.getPartName());
					}
					if ("classifier".equals(kind)) {
						responseData.setClassifier(parser.getPartName());
					}
					responseData.setIncludeInherited(includeInheritedAnnotations);

					// 7. Build JSON with inline annotation config
					return buildResponse(responseData, annotations);
				});

			// Track and log
			Map<String, Object> args = new HashMap<>();
			args.put("targetRef", targetRef);
			args.put("includeInherited", includeInherited);
			ToolCallTracking.recordIfTracking("listAnnotations", args, result);
			Logger.info(ToolCallTracking.formatToolCall("listAnnotations", args, result),
				ModelAnnotationBrowserTool.class);
			return result;

		} catch (Exception ex) {
			Logger.error("listAnnotations() error: " + ex.getMessage(), ex, ModelAnnotationBrowserTool.class);
			System.err.println("ModelAnnotationBrowserTool.listAnnotations() error: " + ex.getMessage());
			Map<String, Object> args = new HashMap<>();
			args.put("targetRef", targetRef);
			args.put("includeInherited", includeInherited);
			return DataClassSerializer.errorToJson(
				"Error accessing annotations: " + ex.getMessage(),
				ModelAnnotationBrowserTool.class,
				"listAnnotations()",
				args);
		}
	}

	/**
	 * Extracts the tag name from an annotation's {@link TagName} annotation.
	 *
	 * @param annotation
	 *        The annotation to get the tag name for.
	 * @return The tag name, or the simple class name in lowercase if no {@link TagName} annotation
	 *         is present.
	 */
	private static String getTagName(TLAnnotation annotation) {
		Class<? extends TLAnnotation> annotationType = annotation.getConfigurationInterface();
		TagName tagNameAnnotation = annotationType.getAnnotation(TagName.class);

		if (tagNameAnnotation != null) {
			return tagNameAnnotation.value();
		}

		// Fallback: use simple class name in lowercase
		return annotationType.getSimpleName().toLowerCase();
	}

	/**
	 * Serializes a {@link TLAnnotation}'s configuration values to JSON with schema references.
	 *
	 * <p>
	 * Uses {@link JsonConfigurationWriter} with schema-aware mode to produce JSON that includes
	 * {@code $schema} URIs pointing to {@code toplogic://schema/config/...}, enabling LLMs to
	 * resolve the full configuration schema via {@link ConfigSchemaTool}.
	 * </p>
	 *
	 * @param annotation
	 *        The annotation to serialize.
	 * @return JSON string representing the annotation's configuration values.
	 */
	private static String serializeAnnotationConfig(TLAnnotation annotation) throws IOException {
		StringWriter sw = new StringWriter();
		new JsonConfigurationWriter(sw).prettyPrint().schemaAware().write(annotation);
		return sw.toString();
	}

	/**
	 * Builds the JSON response string, embedding annotation configurations as inline JSON objects.
	 *
	 * <p>
	 * This method writes the JSON manually (rather than using {@link DataClassSerializer}) so that
	 * the {@code configJson} field is embedded as a structured JSON object via
	 * {@link de.haumacher.msgbuf.json.JsonWriter#jsonValue(String)}, not as an escaped string.
	 * </p>
	 *
	 * @param response
	 *        The response metadata (target, kind, module, etc.).
	 * @param annotationInfos
	 *        The internal annotation info list with the original {@link TLAnnotation} references.
	 * @return The complete JSON response string.
	 */
	private static String buildResponse(AnnotationListResponse response,
			List<InternalAnnotationInfo> annotationInfos) throws IOException {

		StringW buffer = new StringW();
		try (de.haumacher.msgbuf.json.JsonWriter json = new de.haumacher.msgbuf.json.JsonWriter(buffer)) {
			json.beginObject();

			json.name("target");
			json.value(response.getTarget());
			json.name("kind");
			json.value(response.getKind());
			json.name("module");
			json.value(response.getModule());

			if (response.getType() != null && !response.getType().isEmpty()) {
				json.name("type");
				json.value(response.getType());
			}
			if (response.getPart() != null && !response.getPart().isEmpty()) {
				json.name("part");
				json.value(response.getPart());
			}
			if (response.getClassifier() != null && !response.getClassifier().isEmpty()) {
				json.name("classifier");
				json.value(response.getClassifier());
			}

			json.name("includeInherited");
			json.value(response.isIncludeInherited());

			json.name("annotations");
			json.beginArray();

			for (InternalAnnotationInfo info : annotationInfos) {
				json.beginObject();

				json.name("tag");
				json.value(getTagName(info.annotation));

				json.name("configInterface");
				json.value(info.annotation.getConfigurationInterface().getName());

				json.name("inherited");
				json.value(info.inherited);

				if (info.definedIn != null) {
					json.name("definedIn");
					json.value(info.definedIn);
				}

				json.name("configJson");
				json.jsonValue(serializeAnnotationConfig(info.annotation));

				json.endObject();
			}

			json.endArray();
			json.endObject();
		}
		return buffer.toString();
	}

	/**
	 * Determines the kind string based on the resolved model part.
	 *
	 * @param target
	 *        The resolved model part.
	 * @param parser
	 *        The parser that was used to resolve the target.
	 * @return The kind string ("module", "type", "enumeration", "part", or "classifier").
	 */
	private static String determineKind(TLModelPart target, TargetRefParser parser) {
		if (target instanceof TLModule) {
			return "module";
		}
		if (target instanceof TLEnumeration) {
			return "enumeration";
		}
		if (target instanceof TLClassifier) {
			return "classifier";
		}
		if (target instanceof TLStructuredTypePart) {
			return "part";
		}
		if (target instanceof TLType) {
			return "type";
		}
		// Fallback
		return parser.getTypeName() == null ? "module" : "type";
	}

	/**
	 * Parser and resolver for target references in format {@code "module"}, {@code "module:Type"},
	 * or {@code "module:Type#part"}.
	 */
	private static class TargetRefParser {

		private final String targetRef;

		private final String moduleName;

		private final String typeName; // null for module references

		private final String partName; // null for module/type/enumeration references

		private final boolean isModule;

		private TargetRefParser(String targetRef, String moduleName, String typeName, String partName, boolean isModule) {
			this.targetRef = targetRef;
			this.moduleName = moduleName;
			this.typeName = typeName;
			this.partName = partName;
			this.isModule = isModule;
		}

		/**
		 * Parses a target reference string.
		 *
		 * @param targetRef
		 *        The target reference to parse.
		 * @return The parsed {@link TargetRefParser}.
		 * @throws IllegalArgumentException
		 *         If the format is invalid.
		 */
		static TargetRefParser parse(String targetRef) throws IllegalArgumentException {
			if (targetRef == null || targetRef.isEmpty()) {
				throw new IllegalArgumentException("Target reference cannot be null or empty");
			}

			// Split on '#' to detect part or classifier reference
			String[] refParts = targetRef.split("#", 2);
			String typeOrModuleRef = refParts[0];
			String partOrClassifierName = refParts.length > 1 ? refParts[1] : null;

			// Split on ':' to extract module and type
			String[] typeParts = typeOrModuleRef.split(":", 2);

			// If no ':' is present, it's a module-only reference
			if (typeParts.length == 1) {
				String moduleName = typeParts[0];
				if (moduleName.isEmpty()) {
					throw new IllegalArgumentException("Module name cannot be empty in: " + targetRef);
				}
				if (partOrClassifierName != null) {
					throw new IllegalArgumentException(
						"Invalid target reference format: cannot have '#' without module:type separator: " + targetRef);
				}
				return new TargetRefParser(targetRef, moduleName, null, null, true);
			}

			// Has ':' separator - it's a type or type#part reference
			if (typeParts.length != 2) {
				throw new IllegalArgumentException(
					"Invalid target reference format. Expected 'module', 'module:Type', or 'module:Type#part', got: "
						+ targetRef);
			}

			String moduleName = typeParts[0];
			String typeName = typeParts[1];

			// Validate components
			if (moduleName.isEmpty()) {
				throw new IllegalArgumentException("Module name cannot be empty in: " + targetRef);
			}
			if (typeName.isEmpty()) {
				throw new IllegalArgumentException("Type name cannot be empty in: " + targetRef);
			}
			if (partOrClassifierName != null && partOrClassifierName.isEmpty()) {
				throw new IllegalArgumentException("Part/classifier name cannot be empty in: " + targetRef);
			}

			// At this point we don't know if it's a classifier or part reference
			// We'll determine that during resolution
			return new TargetRefParser(targetRef, moduleName, typeName, partOrClassifierName, false);
		}

		/**
		 * Resolves the parsed reference to a {@link TLModelPart}.
		 *
		 * @param model
		 *        The model to resolve in.
		 * @return The resolved {@link TLModelPart}.
		 * @throws IllegalArgumentException
		 *         If the reference cannot be resolved.
		 */
		TLModelPart resolve(TLModel model) throws IllegalArgumentException {
			// Module-only reference
			if (isModule) {
				TLModule module = model.getModule(moduleName);
				if (module == null) {
					throw new IllegalArgumentException("Module not found: " + moduleName);
				}
				return module;
			}

			// Resolve module
			TLModule module = model.getModule(moduleName);
			if (module == null) {
				throw new IllegalArgumentException("Module not found: " + moduleName);
			}

			// Resolve type
			TLType type = module.getType(typeName);
			if (type == null) {
				throw new IllegalArgumentException("Type not found: " + typeName + " in module " + moduleName);
			}

			// If no part/classifier reference, return the type
			if (partName == null) {
				return type;
			}

			// Has part or classifier reference
			// Check if it's an enumeration (classifier) or structured type (part)
			if (type instanceof TLEnumeration) {
				TLEnumeration enumeration = (TLEnumeration) type;
				TLClassifier classifier = enumeration.getClassifier(partName);
				if (classifier == null) {
					throw new IllegalArgumentException(
						"Classifier not found: " + partName + " in enumeration " + TLModelUtil.qualifiedName(type));
				}
				return classifier;
			} else if (type instanceof TLStructuredType) {
				TLStructuredType structuredType = (TLStructuredType) type;
				TLStructuredTypePart part = structuredType.getPart(partName);
				if (part == null) {
					throw new IllegalArgumentException(
						"Part not found: " + partName + " in type " + TLModelUtil.qualifiedName(type));
				}
				return part;
			} else {
				throw new IllegalArgumentException(
					"Type has no parts or classifiers: " + TLModelUtil.qualifiedName(type));
			}
		}

		String getModuleName() {
			return moduleName;
		}

		String getTypeName() {
			return typeName;
		}

		String getPartName() {
			return partName;
		}
	}

	/**
	 * Collects annotations from a model element with optional inheritance support.
	 */
	private static class AnnotationCollector {

		private final Map<Class<? extends TLAnnotation>, InternalAnnotationInfo> annotations = new LinkedHashMap<>();

		/**
		 * Collects annotations from the target model part.
		 *
		 * @param target
		 *        The model part to collect annotations from.
		 * @param includeInherited
		 *        Whether to include inherited annotations.
		 * @return List of annotation information.
		 */
		static List<InternalAnnotationInfo> collect(TLModelPart target, boolean includeInherited) {
			AnnotationCollector collector = new AnnotationCollector();

			// Always add local annotations first
			for (TLAnnotation annotation : target.getAnnotations()) {
				collector.addLocal(annotation);
			}

			if (includeInherited) {
				if (target instanceof TLType) {
					collector.collectInheritedForType((TLType) target);
				} else if (target instanceof TLStructuredTypePart) {
					collector.collectInheritedForPart((TLStructuredTypePart) target);
				} else if (target instanceof TLClassifier) {
					collector.collectInheritedForClassifier((TLClassifier) target);
				}
				// Note: TLModule has no inheritance concept
			}

			return new ArrayList<>(collector.annotations.values());
		}

		private void addLocal(TLAnnotation annotation) {
			annotations.put(annotation.getConfigurationInterface(), new InternalAnnotationInfo(annotation, false, null));
		}

		private void addInherited(TLAnnotation annotation, String definedIn) {
			Class<? extends TLAnnotation> key = annotation.getConfigurationInterface();
			// Only add if not already present (local takes precedence)
			if (!annotations.containsKey(key)) {
				annotations.put(key, new InternalAnnotationInfo(annotation, true, definedIn));
			}
		}

		private void collectInheritedForType(TLType type) {
			if (!(type instanceof TLClass)) {
				return;
			}

			TLClass tlClass = (TLClass) type;
			for (TLClass generalization : tlClass.getGeneralizations()) {
				// Collect annotations from this generalization
				for (TLAnnotation annotation : generalization.getAnnotations()) {
					addInherited(annotation, TLModelUtil.qualifiedName(generalization));
				}
				// Recurse to handle transitive inheritance
				collectInheritedForType(generalization);
			}
		}

		private void collectInheritedForPart(TLStructuredTypePart part) {
			// Get definition
			TLStructuredTypePart definition = part.getDefinition();
			if (definition != null && definition != part) {
				for (TLAnnotation annotation : definition.getAnnotations()) {
					addInherited(annotation,
						TLModelUtil.qualifiedName(definition.getOwner()) + "#" + definition.getName());
				}
			}

			// Also check owner's generalizations
			TLStructuredType owner = part.getOwner();
			if (owner instanceof TLClass) {
				collectFromOwnerGeneralizations((TLClass) owner, part.getName());
			}
		}

		private void collectFromOwnerGeneralizations(TLClass owner, String partName) {
			for (TLClass generalization : owner.getGeneralizations()) {
				TLStructuredTypePart inheritedPart = generalization.getPart(partName);
				if (inheritedPart != null) {
					for (TLAnnotation annotation : inheritedPart.getAnnotations()) {
						addInherited(annotation, TLModelUtil.qualifiedName(generalization) + "#" + partName);
					}
				}
				// Recurse
				collectFromOwnerGeneralizations(generalization, partName);
			}
		}

		private void collectInheritedForClassifier(TLClassifier classifier) {
			// For classifiers, inherit annotations from the owning enumeration/type
			TLEnumeration owner = classifier.getOwner();
			if (owner != null) {
				for (TLAnnotation annotation : owner.getAnnotations()) {
					addInherited(annotation, TLModelUtil.qualifiedName(owner));
				}
				// Also collect from supertypes if the owner is a class-based enumeration
				if (owner instanceof TLClass) {
					collectInheritedForType(owner);
				}
			}
		}
	}

	/**
	 * Internal helper class for annotation information including its source.
	 */
	private static class InternalAnnotationInfo {

		final TLAnnotation annotation;

		final boolean inherited;

		final String definedIn; // null for local, qualified name for inherited

		InternalAnnotationInfo(TLAnnotation annotation, boolean inherited, String definedIn) {
			this.annotation = annotation;
			this.inherited = inherited;
			this.definedIn = definedIn;
		}
	}

}
