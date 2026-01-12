/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.service.tools.util.JsonToolResponseBuilder;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool for browsing annotations on TopLogic model elements (types and parts).
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
 * // List local annotations on a type
 * listAnnotations("tl.core:TLObject", false)
 *
 * // List all annotations including inherited
 * listAnnotations("tl.core:TLObject", true)
 *
 * // List annotations on a type part
 * listAnnotations("tl.core:TLObject#name", false)
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
 */
public class ModelAnnotationBrowserTool {

	/**
	 * Lists all annotations on a model element (type or part) with optional inheritance.
	 *
	 * <p>
	 * Target references must be in one of these formats:
	 * </p>
	 * <ul>
	 * <li><b>Type:</b> {@code "module:TypeName"} (e.g., {@code "tl.core:TLObject"})</li>
	 * <li><b>Part:</b> {@code "module:TypeName#partName"} (e.g., {@code "tl.core:TLObject#name"})
	 * </li>
	 * </ul>
	 *
	 * <p>
	 * When {@code includeInherited} is {@code true}, annotations from supertypes and part
	 * definitions are included in the result. Inherited annotations are marked with
	 * {@code "inherited": true} and include the {@code "definedIn"} location.
	 * </p>
	 *
	 * @param targetRef
	 *        The target reference in format {@code "module:Type"} or {@code "module:Type#part"}.
	 * @param includeInherited
	 *        Whether to include inherited annotations from supertypes/definitions. Default:
	 *        {@code false}.
	 * @return JSON string containing annotation information, or error JSON if the operation fails.
	 */
	@Tool("List all annotations on a model element (type or part) with optional inheritance")
	public String listAnnotations(
			@P("Target reference in format 'module:Type' or 'module:Type#part'") String targetRef,
			@P("Include inherited annotations from supertypes/definitions (default: false)") Boolean includeInherited) {

		return ThreadContextManager.inSystemInteraction(
			ModelAnnotationBrowserTool.class,
			() -> {
				try {
					// 1. Validate and default parameters
					if (targetRef == null || targetRef.isEmpty()) {
						return JsonToolResponseBuilder.buildErrorJson("Target reference is required");
					}
					boolean includeInheritedAnnotations = includeInherited != null && includeInherited;

					// 2. Parse target reference
					TargetRefParser parser = TargetRefParser.parse(targetRef);

					// 3. Resolve to TLModelPart
					TLModel model = ModelService.getApplicationModel();
					TLModelPart target = parser.resolve(model);

					// 4. Collect annotations
					List<AnnotationInfo> annotations =
						AnnotationCollector.collect(target, includeInheritedAnnotations);

					// 5. Build JSON response
					StringWriter buffer = new StringWriter();
					try (JsonWriter json = JsonToolResponseBuilder.createWriter(buffer)) {
						json.beginObject();

						// Target information
						json.name("target").value(targetRef);
						json.name("kind").value(parser.isPart() ? "part" : "type");
						json.name("module").value(parser.getModuleName());
						json.name("type").value(parser.getTypeName());
						if (parser.isPart()) {
							json.name("part").value(parser.getPartName());
						} else {
							json.name("part").nullValue();
						}
						json.name("includeInherited").value(includeInheritedAnnotations);

						// Annotations array
						json.name("annotations").beginArray();
						for (AnnotationInfo info : annotations) {
							json.beginObject();

							json.name("tag").value(getTagName(info.annotation));
							json.name("configInterface").value(info.annotation.getConfigurationInterface().getName());
							json.name("inherited").value(info.inherited);

							if (info.definedIn != null) {
								json.name("definedIn").value(info.definedIn);
							} else {
								json.name("definedIn").nullValue();
							}

							// Serialize annotation as XML string
							json.name("configXml").value(info.annotation.toString());

							json.endObject();
						}
						json.endArray();

						json.endObject();
					}

					String result = buffer.toString();
					Logger.info("listAnnotations() result: " + result, ModelAnnotationBrowserTool.class);
					System.out.println("ModelAnnotationBrowserTool.listAnnotations() result: " + result);
					return result;

				} catch (IllegalArgumentException ex) {
					// Parsing or resolution errors
					String error = JsonToolResponseBuilder.buildErrorJson(ex.getMessage());
					Logger.error("listAnnotations() error: " + ex.getMessage(), ex, ModelAnnotationBrowserTool.class);
					System.err.println("ModelAnnotationBrowserTool.listAnnotations() error: " + error);
					return error;
				} catch (IOException ex) {
					String error =
						JsonToolResponseBuilder.buildErrorJson("Failed to list annotations: " + ex.getMessage());
					Logger.error("listAnnotations() error: " + ex.getMessage(), ex, ModelAnnotationBrowserTool.class);
					System.err.println("ModelAnnotationBrowserTool.listAnnotations() error: " + error);
					return error;
				} catch (Exception ex) {
					String error =
						JsonToolResponseBuilder.buildErrorJson("Error accessing annotations: " + ex.getMessage());
					Logger.error("listAnnotations() error: " + ex.getMessage(), ex, ModelAnnotationBrowserTool.class);
					System.err.println("ModelAnnotationBrowserTool.listAnnotations() error: " + error);
					return error;
				}
			});
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
	 * Parser and resolver for target references in format {@code "module:Type"} or
	 * {@code "module:Type#part"}.
	 */
	private static class TargetRefParser {

		private final String targetRef;

		private final String moduleName;

		private final String typeName;

		private final String partName; // null for type references

		private final boolean isPart;

		private TargetRefParser(String targetRef, String moduleName, String typeName, String partName) {
			this.targetRef = targetRef;
			this.moduleName = moduleName;
			this.typeName = typeName;
			this.partName = partName;
			this.isPart = partName != null;
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

			// Split on '#' to detect part reference
			String[] refParts = targetRef.split("#", 2);
			String typeRef = refParts[0];
			String partName = refParts.length > 1 ? refParts[1] : null;

			// Split on ':' to extract module and type
			String[] typeParts = typeRef.split(":", 2);
			if (typeParts.length != 2) {
				throw new IllegalArgumentException(
					"Invalid target reference format. Expected 'module:Type' or 'module:Type#part', got: "
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
			if (partName != null && partName.isEmpty()) {
				throw new IllegalArgumentException("Part name cannot be empty in: " + targetRef);
			}

			return new TargetRefParser(targetRef, moduleName, typeName, partName);
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

			// If not a part reference, return the type
			if (!isPart) {
				return type;
			}

			// Resolve part
			if (!(type instanceof TLStructuredType)) {
				throw new IllegalArgumentException(
					"Type is not structured and has no parts: " + TLModelUtil.qualifiedName(type));
			}

			TLStructuredType structuredType = (TLStructuredType) type;
			TLStructuredTypePart part = structuredType.getPart(partName);
			if (part == null) {
				throw new IllegalArgumentException(
					"Part not found: " + partName + " in type " + TLModelUtil.qualifiedName(type));
			}

			return part;
		}

		boolean isPart() {
			return isPart;
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

		private final Map<Class<? extends TLAnnotation>, AnnotationInfo> annotations = new LinkedHashMap<>();

		/**
		 * Collects annotations from the target model part.
		 *
		 * @param target
		 *        The model part to collect annotations from.
		 * @param includeInherited
		 *        Whether to include inherited annotations.
		 * @return List of annotation information.
		 */
		static List<AnnotationInfo> collect(TLModelPart target, boolean includeInherited) {
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
				}
			}

			return new ArrayList<>(collector.annotations.values());
		}

		private void addLocal(TLAnnotation annotation) {
			annotations.put(annotation.getConfigurationInterface(), new AnnotationInfo(annotation, false, null));
		}

		private void addInherited(TLAnnotation annotation, String definedIn) {
			Class<? extends TLAnnotation> key = annotation.getConfigurationInterface();
			// Only add if not already present (local takes precedence)
			if (!annotations.containsKey(key)) {
				annotations.put(key, new AnnotationInfo(annotation, true, definedIn));
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
	}

	/**
	 * Information about an annotation including its source.
	 */
	private static class AnnotationInfo {

		final TLAnnotation annotation;

		final boolean inherited;

		final String definedIn; // null for local, qualified name for inherited

		AnnotationInfo(TLAnnotation annotation, boolean inherited, String definedIn) {
			this.annotation = annotation;
			this.inherited = inherited;
			this.definedIn = definedIn;
		}
	}

}
