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

import com.top_logic.ai.service.tools.model.messages.EnumerationResponse;
import com.top_logic.ai.service.tools.model.messages.PartUsageResponse;
import com.top_logic.ai.service.tools.model.messages.TypeInfo;
import com.top_logic.ai.service.tools.model.messages.TypeListResponse;
import com.top_logic.ai.service.tools.model.messages.TypePartInfo;
import com.top_logic.ai.service.tools.model.messages.TypePartListResponse;
import com.top_logic.ai.service.tools.model.messages.TypeUsageResponse;

/**
 * Builder for creating expected facts JSON from model exploration tool responses.
 *
 * <p>
 * This class helps build structured JSON that represents the "golden fact-set" for AI judge
 * evaluation. It filters and formats tool responses to create a clean, scoped set of expected
 * facts.
 * </p>
 *
 * <p>
 * IMPORTANT: This builder now supports a "required" section for prompt-specific expectations.
 * Use this in conjunction with {@link AvailableFactsJsonBuilder} (the oracle truth set):
 * <ul>
 *   <li><b>required_facts</b>: What this specific prompt MUST mention (missing = penalized)</li>
 *   <li><b>available_facts</b>: What exists in the model (for hallucination detection)</li>
 * </ul>
 * </p>
 */
public class ExpectedFactsJsonBuilder {

	private final List<String> _allowedModules;
	private final boolean _allowFrameworkModules;
	private final List<String> _allowedFrameworkTypes = new ArrayList<>();
	private final List<TypeInfo> _types = new ArrayList<>();
	private final List<TypePartInfo> _parts = new ArrayList<>();
	private final List<RelationshipInfo> _relationships = new ArrayList<>();
	private final List<EnumInfo> _enums = new ArrayList<>();
	private final List<DerivedPartInfo> _derivedParts = new ArrayList<>();
	private final List<MandatoryPartInfo> _mandatoryParts = new ArrayList<>();
	
	// New: Required facts section (prompt-specific minimum expectations)
	private boolean _useRequiredSection = false;
	private final List<String> _requiredTypes = new ArrayList<>();
	private final List<RelationshipInfo> _requiredRelationships = new ArrayList<>();

	/**
	 * Creates a new builder with the specified scope configuration.
	 *
	 * @param allowedModules
	 *        The modules that are in scope (e.g., ["luigis.pizzashop"]).
	 * @param allowFrameworkModules
	 *        Whether framework modules (tl.*) are allowed.
	 */
	public ExpectedFactsJsonBuilder(List<String> allowedModules, boolean allowFrameworkModules) {
		_allowedModules = new ArrayList<>(allowedModules);
		_allowFrameworkModules = allowFrameworkModules;
	}

	/**
	 * Adds allowed framework types (e.g., ["tl.model:TLObject", "tl.core:String"]).
	 * These are allowed even when allow_framework_modules=false.
	 */
	public ExpectedFactsJsonBuilder addAllowedFrameworkTypes(String... types) {
		_allowedFrameworkTypes.addAll(Arrays.asList(types));
		return this;
	}

	/**
	 * Enables the "required" section for prompt-specific minimum expectations.
	 * When enabled, the judge will only check for missing facts against the "required" section,
	 * while using the full facts for hallucination detection.
	 * 
	 * Call this BEFORE adding facts to specify what's minimally required for this prompt type.
	 */
	public ExpectedFactsJsonBuilder enableRequiredSection() {
		_useRequiredSection = true;
		return this;
	}

	/**
	 * Marks types as required for this prompt.
	 * Only used when {@link #enableRequiredSection()} is called.
	 */
	public ExpectedFactsJsonBuilder markTypesAsRequired(TypeListResponse response) {
		if (response == null || response.getTypes() == null) {
			return this;
		}
		for (TypeInfo type : response.getTypes()) {
			if (isInScope(type.getQualifiedName())) {
				_requiredTypes.add(type.getQualifiedName());
			}
		}
		return this;
	}

	/**
	 * Marks relationships as required for this prompt.
	 * Only used when {@link #enableRequiredSection()} is called.
	 */
	public ExpectedFactsJsonBuilder markRelationshipsAsRequired(TypePartListResponse response) {
		if (response == null || response.getParts() == null) {
			return this;
		}
		for (TypePartInfo part : response.getParts()) {
			if ("tType".equals(part.getName())) {
				continue;
			}
			if ("reference".equals(part.getKind()) && isInScope(part.getDefinedIn())) {
				_requiredRelationships.add(new RelationshipInfo(
					part.getDefinedIn(),
					part.getName(),
					part.getType(),
					part.isMultiple()));
			}
		}
		return this;
	}

	/**
	 * Adds types from a TypeListResponse, filtering by scope.
	 */
	public ExpectedFactsJsonBuilder addTypes(TypeListResponse response) {
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
	 */
	public ExpectedFactsJsonBuilder addParts(TypePartListResponse response) {
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
	 * Adds relationships from reference parts only (not properties).
	 * This builds proper relationships: from ownerType to targetType via partName.
	 * Filters out framework edges like tType.
	 */
	public ExpectedFactsJsonBuilder addRelationshipsFromParts(TypePartListResponse response) {
		if (response == null || response.getParts() == null) {
			return this;
		}
		for (TypePartInfo part : response.getParts()) {
			// Skip tType and other framework parts
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
	 * @deprecated Use addRelationshipsFromParts() instead. This method builds relationships backwards.
	 */
	@Deprecated
	public ExpectedFactsJsonBuilder addTypeUsages(String qualifiedTypeName, TypeUsageResponse response) {
		// This method is deprecated and should not be used for relationships
		return this;
	}

	/**
	 * Adds part usage information from a PartUsageResponse.
	 * 
	 * @deprecated This method is not currently used. Inheritance tracking needs to be 
	 * redesigned to fit the new relationship structure.
	 */
	@Deprecated
	public ExpectedFactsJsonBuilder addPartUsage(PartUsageResponse response) {
		// This method is deprecated and not implemented for the new relationship structure
		return this;
	}

	/**
	 * Adds enumeration information from an EnumerationResponse.
	 */
	public ExpectedFactsJsonBuilder addEnumeration(EnumerationResponse response) {
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
	 * Extracts derived parts from the current parts list.
	 */
	public ExpectedFactsJsonBuilder extractDerivedParts() {
		for (TypePartInfo part : _parts) {
			if (part.isDerived()) {
				_derivedParts.add(new DerivedPartInfo(
					part.getDefinedIn(),
					part.getName(),
					part.getType()));
			}
		}
		return this;
	}

	/**
	 * Extracts mandatory parts from the current parts list.
	 */
	public ExpectedFactsJsonBuilder extractMandatoryParts() {
		for (TypePartInfo part : _parts) {
			if (part.isMandatory()) {
				_mandatoryParts.add(new MandatoryPartInfo(
					part.getDefinedIn(),
					part.getName(),
					part.getType()));
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

		// If using required section, output it first
		if (_useRequiredSection) {
			json.append("  \"required\": {\n");
			json.append("    \"types\": ")
				.append(formatStringList(_requiredTypes))
				.append(",\n");
			json.append("    \"relationships\": [\n");
			json.append(_requiredRelationships.stream()
				.distinct()
				.map(this::formatRelationship)
				.collect(Collectors.joining(",\n")));
			json.append("\n    ]\n");
			json.append("  },\n");
		}

		// Types (full set for hallucination detection)
		json.append("  \"types\": [\n");
		json.append(_types.stream()
			.map(this::formatTypeInfo)
			.collect(Collectors.joining(",\n")));
		json.append("\n  ],\n");

		// Relationships (full set for hallucination detection)
		json.append("  \"relationships\": [\n");
		json.append(_relationships.stream()
			.distinct()  // Remove duplicates using equals/hashCode
			.map(this::formatRelationship)
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
			"      \"classifiers\": " + formatStringList(enumInfo.classifiers) + "\n" +
			"    }";
	}

	private String formatDerivedPart(DerivedPartInfo part) {
		return "    {\n" +
			"      \"ownerType\": \"" + escapeJson(part.ownerType) + "\",\n" +
			"      \"name\": \"" + escapeJson(part.name) + "\",\n" +
			"      \"type\": \"" + escapeJson(part.type) + "\"\n" +
			"    }";
	}

	private String formatMandatoryPart(MandatoryPartInfo part) {
		return "    {\n" +
			"      \"ownerType\": \"" + escapeJson(part.ownerType) + "\",\n" +
			"      \"name\": \"" + escapeJson(part.name) + "\",\n" +
			"      \"type\": \"" + escapeJson(part.type) + "\"\n" +
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

	private static class DerivedPartInfo {
		final String ownerType;
		final String name;
		final String type;

		DerivedPartInfo(String ownerType, String name, String type) {
			this.ownerType = ownerType;
			this.name = name;
			this.type = type;
		}
	}

	private static class MandatoryPartInfo {
		final String ownerType;
		final String name;
		final String type;

		MandatoryPartInfo(String ownerType, String name, String type) {
			this.ownerType = ownerType;
			this.name = name;
			this.type = type;
		}
	}
}
