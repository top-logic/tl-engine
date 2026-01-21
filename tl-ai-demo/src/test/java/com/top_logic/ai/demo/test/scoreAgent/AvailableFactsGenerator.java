/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.scoreAgent;

import java.util.List;

import com.top_logic.ai.service.tools.model.messages.EnumerationResponse;
import com.top_logic.ai.service.tools.model.messages.TypeListResponse;
import com.top_logic.ai.service.tools.model.messages.TypePartListResponse;
import com.top_logic.ai.service.tools.model.messages.TypeUsageResponse;
import com.top_logic.basic.Logger;

/**
 * Utility for generating complete "available facts" oracle dataset for a module.
 *
 * <p>
 * This class generates a comprehensive truth set by calling ALL available model exploration tools
 * for a given module. This dataset represents everything that could be discovered by the agent
 * and is used for hallucination and notFetched detection.
 * </p>
 *
 * <p>
 * Usage:
 * <pre>{@code
 * AvailableFactsGenerator generator = new AvailableFactsGenerator();
 * String availableFacts = generator.generateForModule("luigis.pizzashop", false);
 * }</pre>
 * </p>
 */
public class AvailableFactsGenerator {

	private final ExpectedFactsBuilder _builder;

	/**
	 * Creates a new generator with default tool instances.
	 */
	public AvailableFactsGenerator() {
		_builder = new ExpectedFactsBuilder();
	}

	/**
	 * Generates complete available facts for a module by calling all exploration tools.
	 *
	 * @param moduleName The module to explore (e.g., "luigis.pizzashop")
	 * @param allowFrameworkModules Whether to include framework modules
	 * @return Complete available facts JSON
	 */
	public String generateForModule(String moduleName, boolean allowFrameworkModules) {
		return generateForModules(List.of(moduleName), allowFrameworkModules);
	}

	/**
	 * Generates complete available facts for multiple modules.
	 *
	 * @param moduleNames The modules to explore
	 * @param allowFrameworkModules Whether to include framework modules
	 * @return Complete available facts JSON
	 */
	public String generateForModules(List<String> moduleNames, boolean allowFrameworkModules) {
		Logger.info("Generating available facts for modules: " + moduleNames, AvailableFactsGenerator.class);

		AvailableFactsJsonBuilder factsBuilder = new AvailableFactsJsonBuilder(moduleNames, allowFrameworkModules);

		// Add standard allowed framework types (common base types)
		factsBuilder.addAllowedFrameworkTypes(
			"tl.model:TLObject",
			"tl.core:String",
			"tl.core:Double",
			"tl.core:Integer",
			"tl.core:Long",
			"tl.core:Boolean",
			"tl.core:Date",
			"tl.model:TLType",
			"tl.model:TLClass",
			"tl.model:TLEnumeration"
		);

		// For each module, call all exploration tools
		for (String module : moduleNames) {
			Logger.info("Processing module: " + module, AvailableFactsGenerator.class);

			// 1. Get all types in the module
			String typesJson = _builder.getBrowserTool().listTypes(module);
			TypeListResponse typesResponse = _builder.parseResponse(typesJson, TypeListResponse.class);
			factsBuilder.addTypes(typesResponse);

			if (typesResponse.getTypes() == null) {
				Logger.warn("No types found in module: " + module, AvailableFactsGenerator.class);
				continue;
			}

			// 2. For each type, get comprehensive details
			for (var type : typesResponse.getTypes()) {
				String typeName = type.getName();
				String qualifiedName = type.getQualifiedName();

				Logger.info("Processing type: " + qualifiedName, AvailableFactsGenerator.class);

				// Get all parts (local + inherited)
				if ("class".equals(type.getMetaType())) {
					String partsJson = _builder.getBrowserTool().listTypeParts(module, typeName, true);
					TypePartListResponse partsResponse = _builder.parseResponse(partsJson, TypePartListResponse.class);
					factsBuilder.addParts(partsResponse);
					factsBuilder.addRelationshipsFromParts(partsResponse);
				}

				// Get enumeration classifiers (if enum)
				if ("enumeration".equals(type.getMetaType())) {
					try {
						String enumJson = _builder.getBrowserTool().listClassifiers(module, typeName);
						EnumerationResponse enumResponse = _builder.parseResponse(enumJson, EnumerationResponse.class);
						factsBuilder.addEnumeration(enumResponse);
					} catch (Exception ex) {
						Logger.warn("Failed to get classifiers for: " + qualifiedName + " - " + ex.getMessage(),
							AvailableFactsGenerator.class);
					}
				}

				// Get type usages (where this type is referenced)
				try {
					String usagesJson = _builder.getAnalysisTool().findTypeUsages(qualifiedName);
					TypeUsageResponse usagesResponse = _builder.parseResponse(usagesJson, TypeUsageResponse.class);
					factsBuilder.addTypeUsages(qualifiedName, usagesResponse);
				} catch (Exception ex) {
					Logger.warn("Failed to get usages for: " + qualifiedName + " - " + ex.getMessage(),
						AvailableFactsGenerator.class);
				}

				// Get annotations (for derived attributes, etc.)
				// Note: This requires knowing which parts have annotations
				// For now, we skip this - can be added if needed
			}
		}

		String result = factsBuilder.build();
		Logger.info("Generated available facts (" + result.length() + " chars)", AvailableFactsGenerator.class);
		return result;
	}

	/**
	 * Generates available facts for luigis.pizzashop module (convenience method).
	 */
	public String generateForPizzaShop() {
		return generateForModule("luigis.pizzashop", false);
	}
}
