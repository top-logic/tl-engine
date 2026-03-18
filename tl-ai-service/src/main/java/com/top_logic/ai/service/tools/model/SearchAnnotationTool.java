/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.ai.service.tools.model.messages.AnnotationSearchResponse;
import com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult;
import com.top_logic.ai.service.tools.util.DataClassSerializer;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLClassifierAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.config.TLModuleAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.util.model.ModelService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool for listing available annotation types for TopLogic model elements.
 * <p>
 * This tool allows AI agents to discover which annotation types can be applied to a given model
 * element. It enumerates all {@link InApp @InApp} annotation types filtered by the target element's
 * kind and type.
 * </p>
 *
 * @see ModelAnnotationBrowserTool
 * @see AnnotationDefinitionExplorerTool
 * @see ModelAnnotationEditorTool
 */
public class SearchAnnotationTool {

	/**
	 * Configuration for {@link SearchAnnotationTool}.
	 */
	public interface Config extends PolymorphicConfiguration<SearchAnnotationTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link SearchAnnotationTool}.
	 */
	public SearchAnnotationTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link SearchAnnotationTool} from configuration.
	 */
	@CalledByReflection
	public SearchAnnotationTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	/**
	 * Lists all available annotation types for a given target model element.
	 *
	 * <p>
	 * Returns all {@link InApp @InApp} annotation types applicable to the target, filtered by
	 * marker interface and {@link TLTypeKind}. Results are sorted alphabetically by tag name.
	 * </p>
	 *
	 * @param targetRef
	 *        Target reference (e.g. "module", "module:Type", "module:Type#part").
	 * @return JSON object listing all applicable annotation types.
	 */
	@Tool("List all available annotation types for a given model element. "
		+ "Returns every applicable annotation that can be set on the target.")
	public String listAvailableAnnotations(
			@P("Target reference (e.g. 'module:Type#part')") String targetRef) {

		try {
			if (targetRef == null || targetRef.trim().isEmpty()) {
				Map<String, Object> args = new HashMap<>();
				args.put("targetRef", targetRef);
				return DataClassSerializer.errorToJson(
					"Target reference is required.",
					SearchAnnotationTool.class, "listAvailableAnnotations()", args);
			}

			AnnotationSearchResponse response = ThreadContextManager.inSystemInteraction(
				SearchAnnotationTool.class,
				() -> {
					TargetRefParser parser = TargetRefParser.parse(targetRef.trim());
					TLModelPart targetPart = parser.resolve(ModelService.getApplicationModel());
					String targetKind = TargetRefParser.determineKind(targetPart, parser);

					Class<? extends TLAnnotation> markerInterface = TLAnnotation.class;
					TLTypeKind targetTypeKind = null;

					if (targetPart instanceof TLStructuredTypePart) {
						markerInterface = TLAttributeAnnotation.class;
						targetTypeKind = TLTypeKind.getTLTypeKind((TLStructuredTypePart) targetPart);
					} else if (targetPart instanceof TLClassifier) {
						markerInterface = TLClassifierAnnotation.class;
					} else if (targetPart instanceof TLType) {
						markerInterface = TLTypeAnnotation.class;
						targetTypeKind = TLTypeKind.getTLTypeKind((TLType) targetPart);
					} else if (targetPart instanceof TLModule) {
						markerInterface = TLModuleAnnotation.class;
					}

					Collection<Class<?>> annotationTypes =
						TypeIndex.getInstance().getSpecializations(markerInterface, true, true, false);

					List<AnnotationSearchResult> results = new ArrayList<>();
					for (Class<?> annotationType : annotationTypes) {
						InApp inApp = annotationType.getAnnotation(InApp.class);
						if (inApp == null || !inApp.value()) {
							continue;
						}

						if (targetTypeKind != null) {
							TargetType targetType = annotationType.getAnnotation(TargetType.class);
							if (targetType != null) {
								boolean matchesKind = false;
								for (TLTypeKind kind : targetType.value()) {
									if (kind == targetTypeKind) {
										matchesKind = true;
										break;
									}
								}
								if (!matchesKind) {
									continue;
								}
							}
						}

						String tagName = getTagName(annotationType);
						String description = getDescription(annotationType);

						AnnotationSearchResult result = AnnotationSearchResult.create();
						result.setTagName(tagName);
						result.setConfigInterface(annotationType.getName());
						if (description != null) {
							result.setDescription(description);
						}

						for (String applicable : getApplicableKinds(annotationType)) {
							result.addApplicableTo(applicable);
						}
						for (String tt : getTargetTypes(annotationType)) {
							result.addTargetType(tt);
						}

						results.add(result);
					}

					results.sort((r1, r2) -> r1.getTagName().compareToIgnoreCase(r2.getTagName()));

					AnnotationSearchResponse resp = AnnotationSearchResponse.create();
					resp.setTotalMatches(results.size());
					resp.setTargetRef(targetRef);
					resp.setTargetKind(targetKind);
					if (targetTypeKind != null) {
						resp.setTargetTypeKind(targetTypeKind.name());
					}
					for (AnnotationSearchResult r : results) {
						resp.addResult(r);
					}
					return resp;
				});

			Map<String, Object> args = new HashMap<>();
			args.put("targetRef", targetRef);
			return DataClassSerializer.toJson(
				response, SearchAnnotationTool.class, "listAvailableAnnotations()", args);

		} catch (Exception ex) {
			Logger.error("listAvailableAnnotations() error: " + ex.getMessage(), ex, SearchAnnotationTool.class);
			Map<String, Object> args = new HashMap<>();
			args.put("targetRef", targetRef);
			return DataClassSerializer.errorToJson(
				"Error listing annotations: " + ex.getMessage(),
				SearchAnnotationTool.class,
				"listAvailableAnnotations()",
				args);
		}
	}

	/**
	 * Extracts the tag name from a configuration interface's {@link TagName} annotation.
	 *
	 * @param annotationType
	 *        The annotation configuration interface class.
	 * @return The tag name, or the simple class name in lowercase if no {@link TagName} annotation
	 *         is present.
	 */
	private static String getTagName(Class<?> annotationType) {
		TagName tagNameAnnotation = annotationType.getAnnotation(TagName.class);
		if (tagNameAnnotation != null) {
			return tagNameAnnotation.value();
		}
		return annotationType.getSimpleName().toLowerCase(Locale.ROOT);
	}

	/**
	 * Looks up a human-readable description for the annotation type from i18n resources.
	 *
	 * @param annotationType
	 *        The annotation configuration interface class.
	 * @return The description string, or {@code null} if no resource is available.
	 */
	private static String getDescription(Class<?> annotationType) {
		return Resources.getInstance().getString(ResKey.forClass(annotationType), null);
	}

	/**
	 * Determines which model element kinds the annotation is applicable to.
	 *
	 * @param annotationType
	 *        The annotation configuration interface class.
	 * @return List of applicable kind strings (e.g. "attribute", "type", "module", "classifier").
	 */
	private static List<String> getApplicableKinds(Class<?> annotationType) {
		List<String> kinds = new ArrayList<>();
		if (TLAttributeAnnotation.class.isAssignableFrom(annotationType)) {
			kinds.add("attribute");
		}
		if (TLTypeAnnotation.class.isAssignableFrom(annotationType)) {
			kinds.add("type");
		}
		if (TLModuleAnnotation.class.isAssignableFrom(annotationType)) {
			kinds.add("module");
		}
		if (TLClassifierAnnotation.class.isAssignableFrom(annotationType)) {
			kinds.add("classifier");
		}
		if (kinds.isEmpty()) {
			kinds.add("any");
		}
		return kinds;
	}

	/**
	 * Extracts the target type kind values from the {@link TargetType} annotation, if present.
	 *
	 * @param annotationType
	 *        The annotation configuration interface class.
	 * @return List of {@link TLTypeKind} name strings, or an empty list if unrestricted.
	 */
	private static List<String> getTargetTypes(Class<?> annotationType) {
		TargetType targetType = annotationType.getAnnotation(TargetType.class);
		if (targetType == null) {
			return Collections.emptyList();
		}
		TLTypeKind[] kinds = targetType.value();
		List<String> result = new ArrayList<>(kinds.length);
		for (TLTypeKind kind : kinds) {
			result.add(kind.name());
		}
		return result;
	}
}
