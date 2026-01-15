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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.ai.service.tools.util.JsonToolResponseBuilder;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.resources.TLTypePartResourceProvider;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.visit.LabelVisitor;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool for searching across the TopLogic application model.
 * <p>
 * This tool provides intelligent text search across modules, types (classes, enumerations,
 * datatypes), and properties/references. It searches technical names, qualified names, labels, and
 * descriptions, returning the top 10 most relevant matches with contextual information.
 * </p>
 */
public class ModelSearchTool {

	/** Maximum number of results to return. */
	private static final int MAX_RESULTS = 10;

	// Score values for different match types
	private static final int SCORE_EXACT_NAME = 1000;

	private static final int SCORE_NAME_PREFIX = 500;

	private static final int SCORE_NAME_CONTAINS = 250;

	private static final int SCORE_QUALIFIED_NAME = 200;

	private static final int SCORE_LABEL = 100;

	private static final int SCORE_DESCRIPTION = 50;

	/**
	 * Searches for model elements by name, label, or description.
	 *
	 * <p>
	 * Searches across modules, types, and properties/references. Returns top 10 matches sorted by
	 * relevance score. Exact name matches score highest, followed by prefix matches, contains
	 * matches, qualified name matches, label matches, and description matches.
	 * </p>
	 *
	 * @param query
	 *        Search query string (case-insensitive).
	 * @param moduleFilter
	 *        Optional list of module names to limit search to. Null or empty = search all modules.
	 * @param kindFilter
	 *        Optional list of kinds to search (module, class, enumeration, datatype, property,
	 *        reference). Null or empty = search all kinds.
	 * @return JSON object with search results and relevance scores.
	 */
	@Tool("Search for model elements by name, label, or description. Returns top 10 matches with relevance scores.")
	public String searchModel(
			@P("Search query string (case-insensitive)") String query,
			@P("Optional: Limit to specific modules") List<String> moduleFilter,
			@P("Optional: Limit to specific kinds (module, class, enumeration, datatype, property, reference)") List<String> kindFilter) {

		return ThreadContextManager.inSystemInteraction(
			ModelSearchTool.class,
			() -> {
				try {
					// Validate parameters
					if (query == null || query.trim().isEmpty()) {
						String error = buildErrorJson("Search query is required");
						Logger.error("searchModel() error: Search query is required", ModelSearchTool.class);
						System.err.println("ModelSearchTool.searchModel() error: " + error);
						return error;
					}

					// Perform search
					List<SearchResult> results = performSearch(query, moduleFilter, kindFilter);

					// Build JSON response
					String result = buildSearchResultJson(query, results);
					Logger.info("searchModel() result: " + result, ModelSearchTool.class);
					return result;

				} catch (Exception ex) {
					String error = buildErrorJson("Search failed: " + ex.getMessage());
					Logger.error("searchModel() error: " + ex.getMessage(), ex, ModelSearchTool.class);
					System.err.println("ModelSearchTool.searchModel() error: " + error);
					return error;
				}
			});
	}

	/**
	 * Performs the actual search across the model.
	 *
	 * @param query
	 *        The search query string.
	 * @param moduleFilter
	 *        Optional list of module names to search within.
	 * @param kindFilter
	 *        Optional list of element kinds to include.
	 * @return List of search results, sorted by relevance, limited to top 10.
	 */
	private static List<SearchResult> performSearch(
			String query,
			List<String> moduleFilter,
			List<String> kindFilter) {

		List<SearchResult> allResults = new ArrayList<>();
		TLModel model = ModelService.getApplicationModel();
		Resources resources = Resources.getInstance();

		// Get modules to search (filtered if specified)
		List<TLModule> modules = model.getModules().stream()
			.filter(m -> moduleFilter == null || moduleFilter.isEmpty() || moduleFilter.contains(m.getName()))
			.collect(Collectors.toList());

		// Search each module
		for (TLModule module : modules) {
			searchModule(module, query, kindFilter, resources, allResults);
		}

		// Sort by score (descending), then by kind rank, then by name
		allResults.sort((r1, r2) -> {
			int scoreCompare = Integer.compare(r2.score(), r1.score());
			if (scoreCompare != 0) {
				return scoreCompare;
			}

			int kindCompare = Integer.compare(getKindRank(r1.kind()), getKindRank(r2.kind()));
			if (kindCompare != 0) {
				return kindCompare;
			}

			return r1.name().compareTo(r2.name());
		});

		// Return top MAX_RESULTS
		return allResults.stream()
			.limit(MAX_RESULTS)
			.collect(Collectors.toList());
	}

	/**
	 * Searches within a specific module.
	 */
	private static void searchModule(
			TLModule module,
			String query,
			List<String> kindFilter,
			Resources resources,
			List<SearchResult> results) {

		// Search module itself
		if (shouldSearchKind("module", kindFilter)) {
			SearchResult moduleResult = checkModuleMatch(module, query, resources);
			if (moduleResult != null) {
				results.add(moduleResult);
			}
		}

		// Search types in module
		for (TLType type : module.getTypes()) {
			// Skip associations (internal implementation details)
			if (type.getModelKind() == ModelKind.ASSOCIATION) {
				continue;
			}

			searchType(module, type, query, kindFilter, resources, results);
		}
	}

	/**
	 * Searches within a specific type.
	 */
	private static void searchType(
			TLModule module,
			TLType type,
			String query,
			List<String> kindFilter,
			Resources resources,
			List<SearchResult> results) {

		String kind = getTypeKind(type);
		if (shouldSearchKind(kind, kindFilter)) {
			SearchResult typeResult = checkTypeMatch(module, type, query, resources);
			if (typeResult != null) {
				results.add(typeResult);
			}
		}

		// Search parts if structured type
		if (type instanceof TLStructuredType) {
			TLStructuredType structuredType = (TLStructuredType) type;
			for (TLStructuredTypePart part : structuredType.getLocalParts()) {
				searchPart(module, structuredType, part, query, kindFilter, resources, results);
			}
		}
	}

	/**
	 * Searches a specific part (property/reference).
	 */
	private static void searchPart(
			TLModule module,
			TLStructuredType ownerType,
			TLStructuredTypePart part,
			String query,
			List<String> kindFilter,
			Resources resources,
			List<SearchResult> results) {

		String kind = getPartKind(part);
		if (shouldSearchKind(kind, kindFilter)) {
			SearchResult partResult = checkPartMatch(module, ownerType, part, query, resources);
			if (partResult != null) {
				results.add(partResult);
			}
		}
	}

	/**
	 * Checks if a module matches the search query.
	 */
	private static SearchResult checkModuleMatch(TLModule module, String query, Resources resources) {
		ResKey labelKey = LabelVisitor.getModuleResourceKey(module);
		String label = resources.getString(labelKey, null);
		String description = resources.getString(labelKey.tooltip(), null);

		int score = calculateScore(query, module.getName(), null, label, description);
		if (score == 0) {
			return null;
		}

		String matchedField = determineMatchedField(query, module.getName(), null, label, description);

		// Build context
		Map<String, Object> context = new HashMap<>();
		long typeCount = module.getTypes().stream()
			.filter(type -> type.getModelKind() != ModelKind.ASSOCIATION)
			.count();
		context.put("typeCount", typeCount);

		return new SearchResult(
			"module",
			module.getName(),
			null,
			score,
			matchedField,
			context,
			label != null && !label.isEmpty() ? label : module.getName(),
			description,
			module);
	}

	/**
	 * Checks if a type matches the search query.
	 */
	private static SearchResult checkTypeMatch(TLModule module, TLType type, String query, Resources resources) {
		String qualifiedName = TLModelUtil.qualifiedName(type);
		ResKey labelKey = TLModelNamingConvention.getTypeLabelKey(type);
		String label = resources.getString(labelKey, null);
		String description = resources.getString(labelKey.tooltip(), null);

		int score = calculateScore(query, type.getName(), qualifiedName, label, description);
		if (score == 0) {
			return null;
		}

		String matchedField = determineMatchedField(query, type.getName(), qualifiedName, label, description);

		// Build context
		Map<String, Object> context = new HashMap<>();
		context.put("module", module.getName());
		String moduleLabel = resources.getString(LabelVisitor.getModuleResourceKey(module), null);
		if (moduleLabel != null && !moduleLabel.isEmpty()) {
			context.put("moduleLabel", moduleLabel);
		}

		// Add classifier count for enumerations, part count for other structured types
		if (type instanceof TLEnumeration) {
			context.put("classifierCount", ((TLEnumeration) type).getClassifiers().size());
		} else if (type instanceof TLStructuredType) {
			context.put("partCount", ((TLStructuredType) type).getLocalParts().size());
		}

		// Add inheritance info for classes
		if (type instanceof TLClass) {
			TLClass tlClass = (TLClass) type;
			if (!tlClass.getGeneralizations().isEmpty()) {
				List<String> generalizations = tlClass.getGeneralizations().stream()
					.map(TLModelUtil::qualifiedName)
					.collect(Collectors.toList());
				context.put("generalizations", generalizations);
			}
		}

		return new SearchResult(
			getTypeKind(type),
			type.getName(),
			qualifiedName,
			score,
			matchedField,
			context,
			label != null && !label.isEmpty() ? label : type.getName(),
			description,
			type);
	}

	/**
	 * Checks if a part (property/reference) matches the search query.
	 */
	private static SearchResult checkPartMatch(
			TLModule module,
			TLStructuredType ownerType,
			TLStructuredTypePart part,
			String query,
			Resources resources) {

		ResKey labelKey = TLTypePartResourceProvider.labelKey(part);
		String label = resources.getString(labelKey, null);
		String description = resources.getString(labelKey.tooltip(), null);

		int score = calculateScore(query, part.getName(), null, label, description);
		if (score == 0) {
			return null;
		}

		String matchedField = determineMatchedField(query, part.getName(), null, label, description);

		// Build context
		Map<String, Object> context = new HashMap<>();
		context.put("module", module.getName());
		context.put("type", ownerType.getName());
		context.put("typeQualifiedName", TLModelUtil.qualifiedName(ownerType));

		String ownerTypeLabel = resources.getString(TLModelNamingConvention.getTypeLabelKey(ownerType), null);
		if (ownerTypeLabel != null && !ownerTypeLabel.isEmpty()) {
			context.put("typeLabel", ownerTypeLabel);
		}

		// Add part type
		if (part.getType() != null) {
			context.put("partType", TLModelUtil.qualifiedName(part.getType()));
		}

		// Add multiplicity info
		context.put("mandatory", part.isMandatory());
		context.put("multiple", part.isMultiple());
		if (part.isMultiple()) {
			context.put("ordered", part.isOrdered());
			context.put("bag", part.isBag());
		}

		// Add structural properties
		context.put("abstract", part.isAbstract());
		context.put("derived", part.isDerived());
		context.put("override", part.isOverride());

		return new SearchResult(
			getPartKind(part),
			part.getName(),
			null,
			score,
			matchedField,
			context,
			label != null && !label.isEmpty() ? label : part.getName(),
			description,
			part);
	}

	/**
	 * Calculates the relevance score for a potential match.
	 *
	 * @return The score (0 if no match).
	 */
	private static int calculateScore(
			String query,
			String name,
			String qualifiedName,
			String label,
			String description) {

		String lowerQuery = query.toLowerCase(Locale.ROOT);
		String lowerName = name.toLowerCase(Locale.ROOT);

		// Exact name match
		if (lowerName.equals(lowerQuery)) {
			return SCORE_EXACT_NAME;
		}

		// Name starts with query
		if (lowerName.startsWith(lowerQuery)) {
			return SCORE_NAME_PREFIX;
		}

		// Name contains query
		if (lowerName.contains(lowerQuery)) {
			return SCORE_NAME_CONTAINS;
		}

		// Qualified name match (for types)
		if (qualifiedName != null && containsIgnoreCase(qualifiedName, query)) {
			return SCORE_QUALIFIED_NAME;
		}

		// Label match
		if (containsIgnoreCase(label, query)) {
			return SCORE_LABEL;
		}

		// Description match
		if (containsIgnoreCase(description, query)) {
			return SCORE_DESCRIPTION;
		}

		return 0; // No match
	}

	/**
	 * Determines which field was matched for display purposes.
	 */
	private static String determineMatchedField(
			String query,
			String name,
			String qualifiedName,
			String label,
			String description) {

		String lowerQuery = query.toLowerCase(Locale.ROOT);
		String lowerName = name.toLowerCase(Locale.ROOT);

		if (lowerName.contains(lowerQuery)) {
			return "name";
		}

		if (qualifiedName != null && containsIgnoreCase(qualifiedName, query)) {
			return "qualifiedName";
		}

		if (containsIgnoreCase(label, query)) {
			return "label";
		}

		if (containsIgnoreCase(description, query)) {
			return "description";
		}

		return "unknown";
	}

	/**
	 * Case-insensitive contains check.
	 */
	private static boolean containsIgnoreCase(String text, String query) {
		if (text == null || text.isEmpty()) {
			return false;
		}
		return text.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
	}

	/**
	 * Checks if a specific kind should be searched based on the filter.
	 */
	private static boolean shouldSearchKind(String kind, List<String> kindFilter) {
		return kindFilter == null || kindFilter.isEmpty() || kindFilter.contains(kind);
	}

	/**
	 * Gets the kind rank for sorting (lower numbers = higher priority).
	 */
	private static int getKindRank(String kind) {
		return switch (kind) {
			case "module" -> 1;
			case "class", "enumeration", "datatype" -> 2;
			case "property", "reference" -> 3;
			default -> 4;
		};
	}

	/**
	 * Gets the kind string for a type.
	 */
	private static String getTypeKind(TLType type) {
		return switch (type.getModelKind()) {
			case CLASS -> "class";
			case ENUMERATION -> "enumeration";
			case DATATYPE -> "datatype";
			default -> "type";
		};
	}

	/**
	 * Gets the kind string for a part.
	 */
	private static String getPartKind(TLStructuredTypePart part) {
		return switch (part.getModelKind()) {
			case PROPERTY -> "property";
			case REFERENCE -> "reference";
			default -> "part";
		};
	}

	/**
	 * Builds the JSON response containing search results.
	 */
	private static String buildSearchResultJson(String query, List<SearchResult> results) throws IOException {
		StringWriter buffer = new StringWriter();
		try (JsonWriter json = JsonToolResponseBuilder.createWriter(buffer)) {
			json.beginObject();

			json.name("success").value(true);
			json.name("query").value(query);
			json.name("resultCount").value(results.size());

			json.name("results").beginArray();
			for (SearchResult result : results) {
				writeSearchResult(json, result);
			}
			json.endArray();

			json.endObject();
		}
		return buffer.toString();
	}

	/**
	 * Writes a single search result to JSON.
	 */
	private static void writeSearchResult(JsonWriter json, SearchResult result) throws IOException {
		json.beginObject();

		// Common fields
		json.name("kind").value(result.kind());
		json.name("name").value(result.name());
		json.name("score").value(result.score());
		json.name("matchedField").value(result.matchedField());

		// Qualified name (types only)
		if (result.qualifiedName() != null) {
			json.name("qualifiedName").value(result.qualifiedName());
		}

		// Context
		json.name("context").beginObject();
		for (Map.Entry<String, Object> entry : result.context().entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			if (value instanceof String) {
				json.name(key).value((String) value);
			} else if (value instanceof Number) {
				json.name(key).value((Number) value);
			} else if (value instanceof Boolean) {
				json.name(key).value((Boolean) value);
			} else if (value instanceof List) {
				json.name(key).beginArray();
				for (Object item : (List<?>) value) {
					json.value(item.toString());
				}
				json.endArray();
			}
		}
		json.endObject();

		// Label and description
		json.name("label").value(result.label());
		if (result.description() != null && !result.description().isEmpty()) {
			json.name("description").value(result.description());
		}

		json.endObject();
	}

	/**
	 * Creates an error response JSON string.
	 */
	private static String buildErrorJson(String errorMessage) {
		try {
			StringWriter buffer = new StringWriter();
			try (JsonWriter json = JsonToolResponseBuilder.createWriter(buffer)) {
				json.beginObject();
				json.name("success").value(false);
				json.name("error").value(errorMessage);
				json.name("results").beginArray().endArray();
				json.endObject();
			}
			return buffer.toString();
		} catch (IOException ex) {
			// Fallback if JSON creation fails
			return JsonToolResponseBuilder.buildErrorJson(errorMessage);
		}
	}

	/**
	 * Helper record to store search result information.
	 */
	private record SearchResult(
			String kind,
			String name,
			String qualifiedName,
			int score,
			String matchedField,
			Map<String, Object> context,
			String label,
			String description,
			TLNamedPart element) {
		// Record body
	}
}
