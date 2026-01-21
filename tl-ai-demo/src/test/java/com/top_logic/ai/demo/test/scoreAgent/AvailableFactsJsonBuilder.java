/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.scoreAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.ai.service.tools.model.messages.AnnotationListResponse;
import com.top_logic.ai.service.tools.model.messages.EnumerationResponse;
import com.top_logic.ai.service.tools.model.messages.TypeInfo;
import com.top_logic.ai.service.tools.model.messages.TypeListResponse;
import com.top_logic.ai.service.tools.model.messages.TypePartInfo;
import com.top_logic.ai.service.tools.model.messages.TypePartListResponse;
import com.top_logic.ai.service.tools.model.messages.TypeUsageResponse;

/**
 * Builder for creating "available facts" JSON (oracle dataset) from comprehensive tool calls.
 *
 * <p>
 * This represents the complete, deterministic truth set of the model - everything that could
 * be discovered by calling all available tools. This is used to:
 * <ul>
 *   <li>Detect true hallucinations (claims not supported anywhere)</li>
 *   <li>Detect non-fetched specifics (agent stated something without fetching it)</li>
 *   <li>Provide better diagnostics (what the agent could have found)</li>
 * </ul>
 * </p>
 *
 * <p>
 * Unlike {@link ExpectedFactsJsonBuilder}, this does NOT represent "required" facts for a specific
 * prompt. It's the complete oracle truth for hallucination detection.
 * </p>
 */
public class AvailableFactsJsonBuilder {

	private final List<String> _allowedModules;
	private final boolean _allowFrameworkModules;
	private final List<String> _allowedFrameworkTypes = new ArrayList<>();
	private final List<TypeInfo> _types = new ArrayList<>();
	private final List<TypePartInfo> _parts = new ArrayList<>();
	private final List<RelationshipInfo> _relationships = new ArrayList<>();
	private final List<EnumInfo> _enums = new ArrayList<>();
	private final List<AnnotationInfo> _annotations = new ArrayList<>();
	private final List<UsageInfo> _usages = new ArrayList<>();

	/**
	 * Creates a new builder with the specified scope configuration.
	 *
	 * @param allowedModules
	 *        The modules that are in scope (e.g., ["luigis.pizzashop"]).
	 * @param allowFrameworkModules
	 *        Whether framework modules (tl.*) are allowed.
	 */
	public AvailableFactsJsonBuilder(List<String> allowedModules, boolean allowFrameworkModules) {
		_allowedModules = new ArrayList<>(allowedModules);
		_allowFrameworkModules = allowFrameworkModules;
	}

	/**
	 * Adds allowed framework types (e.g., ["tl.model:TLObject", "tl.core:String"]).
	 * These are allowed even when allow_framework_modules=false.
	 */
	public AvailableFactsJsonBuilder addAllowedFrameworkTypes(String... types) {
		_allowedFrameworkTypes.addAll(Arrays.asList(types));
		return this;
	}

	/**
	 * Adds types from a TypeListResponse, filtering by scope.
	 */
	public AvailableFactsJsonBuilder addTypes(TypeListResponse response) {
		if (response == null || response.getTypes() == null) {
			return this;
		}
		for (TypeInfo type : response.getTypes()) {
			if (isInScope(type.getQualifiedName())) {
				_types.add(type);
			}
		}
		return this;
	}

	/**
	 * Adds parts from a TypePartListResponse, filtering by scope.
	 * This includes ALL parts (local + inherited) to build the complete truth set.
	 */
	public AvailableFactsJsonBuilder addParts(TypePartListResponse response) {
		if (response == null || response.getParts() == null) {
			return this;
		}
		for (TypePartInfo part : response.getParts()) {
			if (isInScope(part.getDefinedIn())) {
				_parts.add(part);
			}
		}
		return this;
	}

	/**
	 * Adds relationships from reference parts.
	 * This builds proper relationships: from ownerType to targetType via partName.
	 */
	public AvailableFactsJsonBuilder addRelationshipsFromParts(TypePartListResponse response) {
		if (response == null || response.getParts() == null) {
			return this;
		}
		for (TypePartInfo part : response.getParts()) {
			// Skip framework metadata parts
			if ("tType".equals(part.getName())) {
				continue;
			}
			if ("reference".equals(part.getKind()) && isInScope(part.getDefinedIn())) {
				_relationships.add(new RelationshipInfo(
					part.getDefinedIn(),
					part.getName(),
					part.getType(),
					part.isMultiple()));
			}
		}
		return this;
	}

	/**
	 * Adds enumeration information from an EnumerationResponse.
	 * This includes ALL classifier literals (if listClassifiers was called).
	 */
	public AvailableFactsJsonBuilder addEnumeration(EnumerationResponse response) {
		if (response == null) {
			return this;
		}
		String enumName = response.getEnumeration();
		if (!isInScope(enumName)) {
			return this;
		}

		List<String> classifiers = new ArrayList<>();
		if (response.getClassifiers() != null) {
			for (var classifier : response.getClassifiers()) {
				classifiers.add(classifier.getName());
			}
		}

		_enums.add(new EnumInfo(enumName, classifiers));
		return this;
	}

	/**
	 * Adds annotation information from an AnnotationListResponse.
	 * This captures TLScript logic and other derived attribute metadata.
	 */
	public AvailableFactsJsonBuilder addAnnotations(String ownerType, AnnotationListResponse response) {
		if (response == null || response.getAnnotations() == null) {
			return this;
		}
		if (!isInScope(ownerType)) {
			return this;
		}

		for (var annotation : response.getAnnotations()) {
			_annotations.add(new AnnotationInfo(
				ownerType,
				annotation.getConfigInterface(),
				annotation.getTag()));
		}
		return this;
	}

	/**
	 * Adds type usage information (where a type is referenced).
	 */
	public AvailableFactsJsonBuilder addTypeUsages(String qualifiedTypeName, TypeUsageResponse response) {
		if (response == null) {
			return this;
		}
		if (!isInScope(qualifiedTypeName)) {
			return this;
		}

		// Add reference usages (where this type is referenced)
		if (response.getReferences() != null) {
			for (var usage : response.getReferences()) {
				if (isInScope(usage.getOwner())) {
					_usages.add(new UsageInfo(
						qualifiedTypeName,
						usage.getOwner(),
						usage.getReference()));
				}
			}
		}
		
		// Add property usages (where this type is used as value type)
		if (response.getProperties() != null) {
			for (var usage : response.getProperties()) {
				if (isInScope(usage.getOwner())) {
					_usages.add(new UsageInfo(
						qualifiedTypeName,
						usage.getOwner(),
						usage.getProperty()));
				}
			}
		}
		
		return this;
	}

	/**
	 * Builds the final JSON string from all collected facts.
	 */
	public String build() {
		StringBuilder json = new StringBuilder();
		json.append("{\n");
		json.append("  \"scope\": {\n");
		json.append("    \"allowed_modules\": ")
			.append(formatStringList(_allowedModules))
			.append(",\n");
		json.append("    \"allow_framework_modules\": ")
			.append(_allowFrameworkModules);
		if (!_allowedFrameworkTypes.isEmpty()) {
			json.append(",\n");
			json.append("    \"allowed_framework_types\": ")
				.append(formatStringList(_allowedFrameworkTypes));
		}
		json.append("\n");
		json.append("  },\n");

		// Types
		json.append("  \"types\": [\n");
		json.append(_types.stream()
			.map(this::formatTypeInfo)
			.collect(Collectors.joining(",\n")));
		json.append("\n  ],\n");

		// Parts
		json.append("  \"parts\": [\n");
		json.append(_parts.stream()
			.map(this::formatPartInfo)
			.collect(Collectors.joining(",\n")));
		json.append("\n  ],\n");

		// Relationships
		json.append("  \"relationships\": [\n");
		json.append(_relationships.stream()
			.distinct()
			.map(this::formatRelationship)
			.collect(Collectors.joining(",\n")));
		json.append("\n  ],\n");

		// Enums
		json.append("  \"enums\": [\n");
		json.append(_enums.stream()
			.map(this::formatEnum)
			.collect(Collectors.joining(",\n")));
		json.append("\n  ],\n");

		// Annotations
		json.append("  \"annotations\": [\n");
		json.append(_annotations.stream()
			.map(this::formatAnnotation)
			.collect(Collectors.joining(",\n")));
		json.append("\n  ],\n");

		// Usages
		json.append("  \"usages\": [\n");
		json.append(_usages.stream()
			.map(this::formatUsage)
			.collect(Collectors.joining(",\n")));
		json.append("\n  ]\n");

		json.append("}");
		return json.toString();
	}

	/**
	 * Checks if a qualified name is in scope.
	 */
	private boolean isInScope(String qualifiedName) {
		if (qualifiedName == null) {
			return false;
		}
		String module = extractModule(qualifiedName);
		if (module == null) {
			return false;
		}
		boolean isFramework = module.startsWith("tl.") || module.startsWith("tl5.");
		if (isFramework) {
			// Check if explicitly allowed
			if (_allowedFrameworkTypes.contains(qualifiedName)) {
				return true;
			}
			return _allowFrameworkModules;
		}
		return _allowedModules.contains(module);
	}

	/**
	 * Extracts module name from qualified name (e.g., "luigis.pizzashop:Product" -> "luigis.pizzashop").
	 */
	private String extractModule(String qualifiedName) {
		int colon = qualifiedName.indexOf(':');
		if (colon == -1) {
			return null;
		}
		return qualifiedName.substring(0, colon);
	}

	private String formatStringList(List<String> list) {
		if (list.isEmpty()) {
			return "[]";
		}
		return "[" + list.stream()
			.map(s -> "\"" + escapeJson(s) + "\"")
			.collect(Collectors.joining(", ")) + "]";
	}

	private String formatTypeInfo(TypeInfo type) {
		StringBuilder json = new StringBuilder();
		json.append("    {\n");
		json.append("      \"qualifiedName\": \"").append(escapeJson(type.getQualifiedName())).append("\",\n");
		json.append("      \"name\": \"").append(escapeJson(type.getName())).append("\",\n");
		json.append("      \"metaType\": \"").append(escapeJson(type.getMetaType())).append("\",\n");
		json.append("      \"label\": \"").append(escapeJson(type.getLabel())).append("\",\n");
		json.append("      \"partCount\": ").append(type.getPartCount()).append(",\n");
		json.append("      \"classifierCount\": ").append(type.getClassifierCount()).append(",\n");
		json.append("      \"generalizations\": ").append(formatStringList(type.getGeneralizations())).append(",\n");
		json.append("      \"specializations\": ").append(formatStringList(type.getSpecializations())).append("\n");
		json.append("    }");
		return json.toString();
	}

	private String formatPartInfo(TypePartInfo part) {
		return "    {\n" +
			"      \"qualifiedName\": \"" + escapeJson(part.getDefinedIn() + "." + part.getName()) + "\",\n" +
			"      \"name\": \"" + escapeJson(part.getName()) + "\",\n" +
			"      \"ownerType\": \"" + escapeJson(part.getDefinedIn()) + "\",\n" +
			"      \"kind\": \"" + escapeJson(part.getKind()) + "\",\n" +
			"      \"type\": \"" + escapeJson(part.getType()) + "\",\n" +
			"      \"mandatory\": " + part.isMandatory() + ",\n" +
			"      \"multiple\": " + part.isMultiple() + ",\n" +
			"      \"derived\": " + part.isDerived() + ",\n" +
			"      \"inherited\": " + part.isInherited() + "\n" +
			"    }";
	}

	private String formatRelationship(RelationshipInfo rel) {
		return "    {\n" +
			"      \"from\": \"" + escapeJson(rel.from) + "\",\n" +
			"      \"part\": \"" + escapeJson(rel.part) + "\",\n" +
			"      \"to\": \"" + escapeJson(rel.to) + "\",\n" +
			"      \"multiple\": " + rel.multiple + "\n" +
			"    }";
	}

	private String formatEnum(EnumInfo enumInfo) {
		return "    {\n" +
			"      \"qualifiedName\": \"" + escapeJson(enumInfo.qualifiedName) + "\",\n" +
			"      \"classifierCount\": " + enumInfo.classifiers.size() + ",\n" +
			"      \"literals\": " + formatStringList(enumInfo.classifiers) + "\n" +
			"    }";
	}

	private String formatAnnotation(AnnotationInfo annotation) {
		return "    {\n" +
			"      \"ownerType\": \"" + escapeJson(annotation.ownerType) + "\",\n" +
			"      \"configInterface\": \"" + escapeJson(annotation.configInterface) + "\",\n" +
			"      \"property\": \"" + escapeJson(annotation.property) + "\"\n" +
			"    }";
	}

	private String formatUsage(UsageInfo usage) {
		return "    {\n" +
			"      \"targetType\": \"" + escapeJson(usage.targetType) + "\",\n" +
			"      \"usedBy\": \"" + escapeJson(usage.usedBy) + "\",\n" +
			"      \"via\": \"" + escapeJson(usage.via) + "\"\n" +
			"    }";
	}

	private String escapeJson(String str) {
		if (str == null) {
			return "";
		}
		return str.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\t", "\\t");
	}

	// Helper data classes
	private static class RelationshipInfo {
		final String from;
		final String part;
		final String to;
		final boolean multiple;

		RelationshipInfo(String from, String part, String to, boolean multiple) {
			this.from = from;
			this.part = part;
			this.to = to;
			this.multiple = multiple;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof RelationshipInfo)) return false;
			RelationshipInfo other = (RelationshipInfo) obj;
			return Objects.equals(from, other.from) &&
				   Objects.equals(part, other.part) &&
				   Objects.equals(to, other.to) &&
				   multiple == other.multiple;
		}

		@Override
		public int hashCode() {
			return Objects.hash(from, part, to, multiple);
		}
	}

	private static class EnumInfo {
		final String qualifiedName;
		final List<String> classifiers;

		EnumInfo(String qualifiedName, List<String> classifiers) {
			this.qualifiedName = qualifiedName;
			this.classifiers = classifiers;
		}
	}

	private static class AnnotationInfo {
		final String ownerType;
		final String configInterface;
		final String property;

		AnnotationInfo(String ownerType, String configInterface, String property) {
			this.ownerType = ownerType;
			this.configInterface = configInterface;
			this.property = property;
		}
	}

	private static class UsageInfo {
		final String targetType;
		final String usedBy;
		final String via;

		UsageInfo(String targetType, String usedBy, String via) {
			this.targetType = targetType;
			this.usedBy = usedBy;
			this.via = via;
		}
	}
}
