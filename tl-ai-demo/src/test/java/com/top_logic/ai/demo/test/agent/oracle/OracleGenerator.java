/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent.oracle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.ai.demo.test.agent.util.ExpectedFactsBuilder;
import com.top_logic.ai.service.tools.model.messages.TypeListResponse;
import com.top_logic.basic.Logger;

/**
 * Simple utility to generate a complete oracle JSON file for all tools on a module.
 * 
 * <p>
 * This generates the "available facts" - everything that could be discovered by calling
 * all available tools on the module. This oracle is shared by all tests.
 * </p>
 * 
 * <p>
 * Usage: Run the {@link #generateOracle(String, String)} method once to create the oracle file.
 * </p>
 */
public class OracleGenerator {

	private final ExpectedFactsBuilder _builder;

	/**
	 * Creates a new oracle generator.
	 */
	public OracleGenerator() {
		_builder = new ExpectedFactsBuilder();
	}

	/**
	 * Generates a complete oracle for the given module.
	 * 
	 * @param moduleName
	 *        The module name (e.g., "luigis.pizzashop")
	 * @param outputPath
	 *        The file path to save the oracle JSON
	 * @return The oracle JSON string
	 */
	public String generateOracle(String moduleName, String outputPath) throws IOException {
		Logger.info("Generating oracle for module: " + moduleName, OracleGenerator.class);

		StringBuilder json = new StringBuilder();
		json.append("{\n");
		json.append("  \"module\": \"").append(moduleName).append("\",\n");

		// 1. Get all types
		Logger.info("Fetching types...", OracleGenerator.class);
		String typesJson = _builder.getBrowserTool().listTypes(moduleName);
		TypeListResponse typesResponse = _builder.parseResponse(typesJson, TypeListResponse.class);
		json.append("  \"types\": ").append(typesJson).append(",\n");

		// 2. Get annotations for the module
		Logger.info("Fetching module annotations...", OracleGenerator.class);
		String moduleAnnotationsJson = null;
		try {
			moduleAnnotationsJson = _builder.getAnnotationTool().listAnnotations(moduleName, true);
		} catch (Exception ex) {
			Logger.warn("Failed to get module annotations: " + ex.getMessage(), OracleGenerator.class);
		}
		json.append("  \"moduleAnnotations\": ").append(moduleAnnotationsJson != null ? moduleAnnotationsJson : "null").append(",\n");

		// 3. For each type, get parts, classifiers, usages, and annotations
		Logger.info("Fetching details for each type...", OracleGenerator.class);
		Map<String, String> partsByType = new LinkedHashMap<>();
		Map<String, String> classifiersByEnum = new LinkedHashMap<>();
		Map<String, String> usagesByType = new LinkedHashMap<>();
		Map<String, String> typeAnnotations = new LinkedHashMap<>();
		Map<String, String> partAnnotations = new LinkedHashMap<>();
		Map<String, String> classifierAnnotations = new LinkedHashMap<>();

		if (typesResponse.getTypes() != null) {
			for (var type : typesResponse.getTypes()) {
				String typeName = type.getName();
				String qualifiedName = type.getQualifiedName();
				Logger.info("Processing: " + qualifiedName, OracleGenerator.class);

				// Get type annotations
				try {
					String typeAnnoJson = _builder.getAnnotationTool().listAnnotations(qualifiedName, true);
					typeAnnotations.put(typeName, typeAnnoJson);
				} catch (Exception ex) {
					Logger.warn("Failed to get annotations for " + qualifiedName + ": " + ex.getMessage(),
						OracleGenerator.class);
				}

				// Get parts (for classes)
				if ("class".equals(type.getMetaType())) {
					try {
						String partsJson =
							_builder.getBrowserTool().listTypeParts(moduleName, typeName, true);
						partsByType.put(typeName, partsJson);

						// Get annotations for each part
						var partsResponse = _builder.parseResponse(partsJson,
							com.top_logic.ai.service.tools.model.messages.TypePartListResponse.class);
						if (partsResponse.getParts() != null) {
							for (var part : partsResponse.getParts()) {
								String partRef = qualifiedName + "#" + part.getName();
								try {
									String partAnnoJson = _builder.getAnnotationTool().listAnnotations(partRef, true);
									partAnnotations.put(partRef, partAnnoJson);
								} catch (Exception ex) {
									Logger.warn("Failed to get annotations for " + partRef + ": " + ex.getMessage(),
										OracleGenerator.class);
								}
							}
						}
					} catch (Exception ex) {
						Logger.warn("Failed to get parts for " + qualifiedName + ": " + ex.getMessage(),
							OracleGenerator.class);
					}
				}

				// Get classifiers (for enums)
				if ("enumeration".equals(type.getMetaType())) {
					try {
						String classifiersJson =
							_builder.getBrowserTool().listClassifiers(moduleName, typeName);
						classifiersByEnum.put(typeName, classifiersJson);

						// Get annotations for each classifier
						var classifiersResponse = _builder.parseResponse(classifiersJson,
							com.top_logic.ai.service.tools.model.messages.EnumerationResponse.class);
						if (classifiersResponse.getClassifiers() != null) {
							for (var classifier : classifiersResponse.getClassifiers()) {
								String classifierRef = qualifiedName + "#" + classifier.getName();
								try {
									String classifierAnnoJson =
										_builder.getAnnotationTool().listAnnotations(classifierRef, true);
									classifierAnnotations.put(classifierRef, classifierAnnoJson);
								} catch (Exception ex) {
									Logger.warn("Failed to get annotations for " + classifierRef + ": " + ex.getMessage(),
										OracleGenerator.class);
								}
							}
						}
					} catch (Exception ex) {
						Logger.warn("Failed to get classifiers for " + qualifiedName + ": " + ex.getMessage(),
							OracleGenerator.class);
					}
				}

				// Get usages
				try {
					String usagesJson = _builder.getAnalysisTool().findTypeUsages(qualifiedName);
					usagesByType.put(typeName, usagesJson);
				} catch (Exception ex) {
					Logger.warn("Failed to get usages for " + qualifiedName + ": " + ex.getMessage(),
						OracleGenerator.class);
				}
			}
		}

		// Build parts object
		json.append("  \"parts\": {\n");
		appendJsonMap(json, partsByType, "    ");
		json.append("  },\n");

		// Build classifiers object
		json.append("  \"classifiers\": {\n");
		appendJsonMap(json, classifiersByEnum, "    ");
		json.append("  },\n");

		// Build usages object
		json.append("  \"usages\": {\n");
		appendJsonMap(json, usagesByType, "    ");
		json.append("  },\n");

		// Build annotations objects
		json.append("  \"typeAnnotations\": {\n");
		appendJsonMap(json, typeAnnotations, "    ");
		json.append("  },\n");

		json.append("  \"partAnnotations\": {\n");
		appendJsonMap(json, partAnnotations, "    ");
		json.append("  },\n");

		json.append("  \"classifierAnnotations\": {\n");
		appendJsonMap(json, classifierAnnotations, "    ");
		json.append("  }\n");

		json.append("}");

		String oracleJson = json.toString();

		// Save to file
		if (outputPath != null) {
			Logger.info("Saving oracle to: " + outputPath, OracleGenerator.class);
			Files.write(Paths.get(outputPath), oracleJson.getBytes());
			Logger.info("Oracle saved successfully (" + oracleJson.length() + " bytes)", OracleGenerator.class);
		}

		return oracleJson;
	}

	/**
	 * Helper to append a map as JSON object entries.
	 */
	private void appendJsonMap(StringBuilder json, Map<String, String> map, String indent) {
		int count = 0;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (count > 0) {
				json.append(",\n");
			}
			json.append(indent).append("\"").append(escapeJson(entry.getKey())).append("\": ");
			json.append(entry.getValue());
			count++;
		}
		if (count > 0) {
			json.append("\n");
		}
	}

	/**
	 * Helper to escape JSON string values.
	 */
	private String escapeJson(String str) {
		return str.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	/**
	 * Convenience method to generate oracle for luigis.pizzashop module.
	 * 
	 * @param outputPath
	 *        The file path to save the oracle
	 * @return The oracle JSON string
	 */
	public String generatePizzaShopOracle(String outputPath) throws IOException {
		return generateOracle("luigis.pizzashop", outputPath);
	}
}
