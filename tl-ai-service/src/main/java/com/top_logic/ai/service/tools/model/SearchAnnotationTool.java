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
 * Tool for searching available annotation types in the TopLogic model system.
 * <p>
 * This tool allows AI agents to discover annotation types by keyword search, optionally filtered by
 * a target model element. It searches tag names, configuration interface names, descriptions, and
 * package names, returning the top 10 most relevant matches with contextual information.
 * </p>
 */
public class SearchAnnotationTool {

	/** Maximum number of results to return. */
	private static final int MAX_RESULTS = 10;

	// Score values for different match types
	private static final int SCORE_EXACT_TAG_NAME = 1000;

	private static final int SCORE_TAG_NAME_PREFIX = 500;

	private static final int SCORE_TAG_NAME_CONTAINS = 250;

	private static final int SCORE_CONFIG_INTERFACE_NAME = 200;

	private static final int SCORE_DESCRIPTION_CONTAINS = 100;

	private static final int SCORE_PACKAGE_CONTAINS = 50;

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
	 * Searches for annotation types by keyword, optionally filtered by a target model element.
	 *
	 * <p>
	 * Searches across tag names, configuration interface names, descriptions, and package names.
	 * Returns the top 10 matches sorted by relevance score.
	 * </p>
	 *
	 * @param query
	 *        Search query string (case-insensitive).
	 * @param targetRef
	 *        Optional target reference to filter annotations applicable to a specific model element
	 *        (e.g. "module", "module:Type", "module:Type#part").
	 * @return JSON object with search results and relevance scores.
	 */
	@Tool("Search for annotation types by keyword. Optionally filter by target model element to find applicable annotations.")
	public String searchAnnotations(
			@P("Search query string (case-insensitive)") String query,
			@P("Optional: Target reference to filter applicable annotations (e.g. 'module:Type#part')") String targetRef) {

		try {
			// Validate parameters before entering system interaction
			if (query == null || query.trim().isEmpty()) {
				Map<String, Object> args = new HashMap<>();
				args.put("query", query);
				args.put("targetRef", targetRef);
				return DataClassSerializer.errorToJson(
					"Search query is required and must not be empty.",
					SearchAnnotationTool.class, "searchAnnotations()", args);
			}

			AnnotationSearchResponse response = ThreadContextManager.inSystemInteraction(
				SearchAnnotationTool.class,
				() -> {

					// Resolve target reference if provided
					TLModelPart targetPart = null;
					Class<? extends TLAnnotation> markerInterface = TLAnnotation.class;
					TLTypeKind targetTypeKind = null;
					String targetKind = "";

					if (targetRef != null && !targetRef.trim().isEmpty()) {
						TargetRefParser parser = TargetRefParser.parse(targetRef);
						targetPart = parser.resolve(ModelService.getApplicationModel());
						targetKind = TargetRefParser.determineKind(targetPart, parser);

						// Determine marker interface and TLTypeKind based on target
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
					}

					// Enumerate annotation types
					Collection<Class<?>> annotationTypes =
						TypeIndex.getInstance().getSpecializations(markerInterface, true, true, false);

					// Filter and score
					List<AnnotationSearchResult> results = new ArrayList<>();
					for (Class<?> annotationType : annotationTypes) {
						// InApp filter
						InApp inApp = annotationType.getAnnotation(InApp.class);
						if (inApp == null || !inApp.value()) {
							continue;
						}

						// TargetType filter
						if (targetTypeKind != null) {
							TargetType targetType = annotationType.getAnnotation(TargetType.class);
							if (targetType != null) {
								TLTypeKind[] allowedKinds = targetType.value();
								boolean matches = false;
								for (TLTypeKind kind : allowedKinds) {
									if (kind == targetTypeKind) {
										matches = true;
										break;
									}
								}
								if (!matches) {
									continue;
								}
							}
						}

						// Score the annotation
						String tagName = getTagName(annotationType);
						String description = getDescription(annotationType);
						int score = calculateScore(query, tagName, annotationType, description);
						if (score == 0) {
							continue;
						}

						// Build result
						AnnotationSearchResult result = AnnotationSearchResult.create();
						result.setTagName(tagName);
						result.setConfigInterface(annotationType.getName());
						if (description != null) {
							result.setDescription(description);
						}
						result.setScore(score);
						result.setMatchedField(determineMatchedField(query, tagName, annotationType, description));

						// Applicable to
						for (String applicable : getApplicableKinds(annotationType)) {
							result.addApplicableTo(applicable);
						}

						// Target types from @TargetType
						for (String tt : getTargetTypes(annotationType)) {
							result.addTargetType(tt);
						}

						results.add(result);
					}

					// Sort by score desc then tag name asc
					results.sort((r1, r2) -> {
						int scoreCompare = Integer.compare(r2.getScore(), r1.getScore());
						if (scoreCompare != 0) {
							return scoreCompare;
						}
						return r1.getTagName().compareToIgnoreCase(r2.getTagName());
					});

					int totalMatches = results.size();

					// Limit to top results
					if (results.size() > MAX_RESULTS) {
						results = results.subList(0, MAX_RESULTS);
					}

					// Build response
					AnnotationSearchResponse resp = AnnotationSearchResponse.create();
					resp.setQuery(query);
					resp.setTotalMatches(totalMatches);
					if (targetRef != null && !targetRef.trim().isEmpty()) {
						resp.setTargetRef(targetRef);
						resp.setTargetKind(targetKind);
						if (targetTypeKind != null) {
							resp.setTargetTypeKind(targetTypeKind.name());
						}
					}
					for (AnnotationSearchResult r : results) {
						resp.addResult(r);
					}

					return resp;
				});

			// Serialize to JSON
			Map<String, Object> args = new HashMap<>();
			args.put("query", query);
			args.put("targetRef", targetRef);
			return DataClassSerializer.toJson(response, SearchAnnotationTool.class, "searchAnnotations()", args);

		} catch (Exception ex) {
			Logger.error("searchAnnotations() error: " + ex.getMessage(), ex, SearchAnnotationTool.class);
			Map<String, Object> args = new HashMap<>();
			args.put("query", query);
			args.put("targetRef", targetRef);
			return DataClassSerializer.errorToJson(
				"Error searching annotations: " + ex.getMessage(),
				SearchAnnotationTool.class,
				"searchAnnotations()",
				args);
		}
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
					resp.setQuery("");
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
	 * Calculates the relevance score for a potential match.
	 *
	 * @param query
	 *        The search query.
	 * @param tagName
	 *        The tag name of the annotation.
	 * @param annotationType
	 *        The annotation configuration interface class.
	 * @param description
	 *        The description of the annotation, or {@code null}.
	 * @return The score (0 if no match).
	 */
	private static int calculateScore(String query, String tagName, Class<?> annotationType, String description) {
		String lowerQuery = query.toLowerCase(Locale.ROOT);
		String lowerTagName = tagName.toLowerCase(Locale.ROOT);

		// Exact tag name match
		if (lowerTagName.equals(lowerQuery)) {
			return SCORE_EXACT_TAG_NAME;
		}

		// Tag name starts with query
		if (lowerTagName.startsWith(lowerQuery)) {
			return SCORE_TAG_NAME_PREFIX;
		}

		// Tag name contains query
		if (lowerTagName.contains(lowerQuery)) {
			return SCORE_TAG_NAME_CONTAINS;
		}

		// Config interface simple name contains query
		String simpleName = annotationType.getSimpleName().toLowerCase(Locale.ROOT);
		if (simpleName.contains(lowerQuery)) {
			return SCORE_CONFIG_INTERFACE_NAME;
		}

		// Description contains query
		if (description != null && description.toLowerCase(Locale.ROOT).contains(lowerQuery)) {
			return SCORE_DESCRIPTION_CONTAINS;
		}

		// Package name contains query
		String packageName = annotationType.getPackageName().toLowerCase(Locale.ROOT);
		if (packageName.contains(lowerQuery)) {
			return SCORE_PACKAGE_CONTAINS;
		}

		return 0;
	}

	/**
	 * Determines which field was matched for display purposes.
	 *
	 * @param query
	 *        The search query.
	 * @param tagName
	 *        The tag name of the annotation.
	 * @param annotationType
	 *        The annotation configuration interface class.
	 * @param description
	 *        The description of the annotation, or {@code null}.
	 * @return The name of the matched field.
	 */
	private static String determineMatchedField(String query, String tagName, Class<?> annotationType,
			String description) {
		String lowerQuery = query.toLowerCase(Locale.ROOT);

		if (tagName.toLowerCase(Locale.ROOT).contains(lowerQuery)) {
			return "tagName";
		}

		if (annotationType.getSimpleName().toLowerCase(Locale.ROOT).contains(lowerQuery)) {
			return "configInterface";
		}

		if (description != null && description.toLowerCase(Locale.ROOT).contains(lowerQuery)) {
			return "description";
		}

		if (annotationType.getPackageName().toLowerCase(Locale.ROOT).contains(lowerQuery)) {
			return "package";
		}

		return "unknown";
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
