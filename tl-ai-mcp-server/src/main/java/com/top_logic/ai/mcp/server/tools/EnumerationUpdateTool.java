/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.top_logic.ai.mcp.server.util.JsonResponseBuilder;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;

/**
 * Tool for updating properties of existing TopLogic enumerations and their classifiers.
 *
 * This tool allows updating the internationalized labels and descriptions of enumerations,
 * adding new classifiers, updating existing classifiers, and setting the default classifier.
 */
public class EnumerationUpdateTool {

	/** Tool name for the enumeration update functionality. */
	public static final String TOOL_NAME = "update-enumeration";

	/** Tool description. */
	private static final String DESCRIPTION =
		"Update properties and classifiers of an existing TopLogic enumeration";

	/**
	 * Pattern for classifier names.
	 * <p>
	 * Classifier names must start with a Java identifier start character, followed by identifier
	 * part characters. Dots and hyphens are allowed as separators, but consecutive separators
	 * and minus signs, and must not end with a dot.
	 * </p>
	 */
	private static final Pattern CLASSIFIER_NAME_PATTERN = Pattern.compile(
		"^\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(?:[\\.\\-]\\p{javaJavaIdentifierPart}+)*$");

	/** JSON Schema for the tool input arguments. */
	private static final String INPUT_SCHEMA_JSON = """
		{
		  "type": "object",
		  "properties": {
		    "moduleName": {
		      "type": "string",
		      "description": "Name of the module containing the enumeration"
		    },
		    "enumName": {
		      "type": "string",
		      "description": "Name of the enumeration to update"
		    },
		    "label": {
		      "type": "object",
		      "properties": {
		        "en": {
		          "type": "string",
		          "description": "English label for the enumeration"
		        },
		        "de": {
		          "type": "string",
		          "description": "German label for the enumeration"
		        }
		      },
		      "additionalProperties": false
		    },
		    "description": {
		      "type": "object",
		      "properties": {
		        "en": {
		          "type": "string",
		          "description": "English description for the enumeration"
		        },
		        "de": {
		          "type": "string",
		          "description": "German description for the enumeration"
		        }
		      },
		      "additionalProperties": false
		    },
		    "addClassifiers": {
		      "type": "array",
		      "description": "List of new classifiers to add",
		      "items": {
		        "type": "object",
		        "properties": {
		          "classifierName": {
		            "type": "string",
		            "description": "Name of the new classifier"
		          },
		          "default": {
		            "type": "boolean",
		            "description": "Whether this classifier should be the default"
		          },
		          "label": {
		            "type": "object",
		            "properties": {
		              "en": {
		                "type": "string",
		                "description": "English label for the classifier"
		              },
		              "de": {
		                "type": "string",
		                "description": "German label for the classifier"
		              }
		            },
		            "additionalProperties": false
		          },
		          "description": {
		            "type": "object",
		            "properties": {
		              "en": {
		                "type": "string",
		                "description": "English description for the classifier"
		              },
		              "de": {
		                "type": "string",
		                "description": "German description for the classifier"
		              }
		            },
		            "additionalProperties": false
		          }
		        },
		        "required": ["classifierName"],
		        "additionalProperties": false
		      }
		    },
		    "updateClassifiers": {
		      "type": "array",
		      "description": "List of existing classifiers to update",
		      "items": {
		        "type": "object",
		        "properties": {
		          "classifierName": {
		            "type": "string",
		            "description": "Name of the existing classifier to update"
		          },
		          "default": {
		            "type": "boolean",
		            "description": "Whether this classifier should be the default"
		          },
		          "label": {
		            "type": "object",
		            "properties": {
		              "en": {
		                "type": "string",
		                "description": "English label for the classifier"
		              },
		              "de": {
		                "type": "string",
		                "description": "German label for the classifier"
		              }
		            },
		            "additionalProperties": false
		          },
		          "description": {
		            "type": "object",
		            "properties": {
		              "en": {
		                "type": "string",
		                "description": "English description for the classifier"
		              },
		              "de": {
		                "type": "string",
		                "description": "German description for the classifier"
		              }
		            },
		            "additionalProperties": false
		          }
		        },
		        "required": ["classifierName"],
		        "additionalProperties": false
		      }
		    },
		    "defaultClassifier": {
		      "type": "string",
		      "description": "Name of the classifier that should be set as the default"
		    }
		  },
		  "required": ["moduleName", "enumName"],
		  "additionalProperties": false
		}
		""";

	/**
	 * Creates the MCP tool specification for the enumeration update functionality.
	 *
	 * @return The tool specification that can be registered with the MCP server.
	 */
	public static McpServerFeatures.SyncToolSpecification createSpecification() {
		McpJsonMapper jsonMapper = McpJsonMapper.getDefault();

		McpSchema.Tool tool = McpSchema.Tool.builder()
			.name(TOOL_NAME)
			.title("Update TopLogic Enumeration")
			.description(DESCRIPTION)
			.inputSchema(jsonMapper, INPUT_SCHEMA_JSON)
			.build();

		return McpServerFeatures.SyncToolSpecification.builder()
			.tool(tool)
			.callHandler(EnumerationUpdateTool::handleToolRequest)
			.build();
	}

	/**
	 * Handles the tool request for updating an enumeration.
	 *
	 * @param exchange The MCP server exchange context.
	 * @param request The tool request containing the arguments.
	 * @return The tool result with success/error information.
	 */
	private static McpSchema.CallToolResult handleToolRequest(
			McpSyncServerExchange exchange,
			CallToolRequest request) {

		Map<String, Object> arguments = request.arguments();

		return ThreadContextManager.inSystemInteraction(
			EnumerationUpdateTool.class,
			() -> updateEnumeration(arguments));
	}

	/**
	 * Updates the specified enumeration with the provided properties.
	 *
	 * @param arguments The tool arguments containing enumeration name and update data.
	 * @return The tool result with updated enumeration information.
	 */
	private static McpSchema.CallToolResult updateEnumeration(Map<String, Object> arguments) {
		// Validate and extract module and enumeration names
		final String moduleName;
		final String enumName;
		try {
			moduleName = ToolArgumentUtil.requireNonEmptyString(
				arguments,
				"moduleName",
				"Module name");
			enumName = ToolArgumentUtil.requireNonEmptyString(
				arguments,
				"enumName",
				"Enumeration name");
		} catch (ToolArgumentUtil.ToolInputException e) {
			return createErrorResult(e.getMessage());
		}

		// Retrieve module and validate existence
		TLModel model = ModelService.getApplicationModel();
		TLModule module = model.getModule(moduleName);
		if (module == null) {
			return createErrorResult("Module '" + moduleName + "' not found");
		}

		// Retrieve enumeration and validate existence
		TLType existingType = module.getType(enumName);
		if (!(existingType instanceof TLEnumeration)) {
			if (existingType == null) {
				return createErrorResult("Enumeration '" + enumName + "' not found in module '" + moduleName + "'");
			} else {
				return createErrorResult("Type '" + enumName + "' exists but is not an enumeration");
			}
		}
		TLEnumeration enumeration = (TLEnumeration) existingType;

		// Parse add classifiers
		List<ClassifierUpdate> addClassifiers = new ArrayList<>();
		if (arguments.containsKey("addClassifiers")) {
			List<Map<String, Object>> classifierDataList = ToolArgumentUtil.getOptionalObjectList(arguments, "addClassifiers");
			if (classifierDataList != null) {
				for (Map<String, Object> classifierData : classifierDataList) {
					try {
						addClassifiers.add(parseAddClassifier(classifierData));
					} catch (ToolArgumentUtil.ToolInputException e) {
						return createErrorResult("Invalid classifier in addClassifiers: " + e.getMessage());
					}
				}
			}
		}

		// Parse update classifiers
		List<ClassifierUpdate> updateClassifiers = new ArrayList<>();
		if (arguments.containsKey("updateClassifiers")) {
			List<Map<String, Object>> classifierDataList = ToolArgumentUtil.getOptionalObjectList(arguments, "updateClassifiers");
			if (classifierDataList != null) {
				for (Map<String, Object> classifierData : classifierDataList) {
					try {
						updateClassifiers.add(parseUpdateClassifier(classifierData));
					} catch (ToolArgumentUtil.ToolInputException e) {
						return createErrorResult("Invalid classifier in updateClassifiers: " + e.getMessage());
					}
				}
			}
		}

		// Get default classifier
		String defaultClassifierName = ToolArgumentUtil.getOptionalString(arguments, "defaultClassifier", null);

		// Validate classifier operations
		String validationError = validateClassifierOperations(
			enumeration, addClassifiers, updateClassifiers, defaultClassifierName);
		if (validationError != null) {
			return createErrorResult(validationError);
		}

		// Extract I18N data for enumeration
		ToolI18NUtil.LocalizedTexts i18n = ToolI18NUtil.extractFromArguments(arguments);

		// Check if there are any updates to apply
		boolean hasUpdates = i18n.hasAny() || !addClassifiers.isEmpty()
			|| !updateClassifiers.isEmpty() || defaultClassifierName != null;

		if (!hasUpdates) {
			return createErrorResult("No update data provided. At least one of 'label', 'description', 'addClassifiers', 'updateClassifiers', or 'defaultClassifier' must be specified");
		}

		// Track changes for response
		ChangeTracker changes = new ChangeTracker();

		// Apply updates in transaction
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(
				ResKey.text("Update Enumeration"))) {

			// Apply enumeration I18N updates
			if (i18n.hasAny()) {
				ToolI18NUtil.applyIfPresent(enumeration, i18n);
				changes.enumerationUpdated = true;
			}

			// Add new classifiers
			for (ClassifierUpdate addClassifier : addClassifiers) {
				TLClassifier classifier = TLModelUtil.addClassifier(enumeration, addClassifier.name);
				if (addClassifier.isDefault != null && addClassifier.isDefault) {
					classifier.setDefault(true);
				}
				// Apply classifier I18N if provided
				if (addClassifier.i18n != null && addClassifier.i18n.hasAny()) {
					ToolI18NUtil.applyIfPresent(classifier, addClassifier.i18n);
				}
				changes.classifiersAdded++;
			}

			// Update existing classifiers
			for (ClassifierUpdate updateClassifier : updateClassifiers) {
				TLClassifier classifier = enumeration.getClassifier(updateClassifier.name);
				if (classifier == null) {
					// This should have been caught by validation
					continue;
				}
				if (updateClassifier.isDefault != null) {
					classifier.setDefault(updateClassifier.isDefault);
					changes.classifiersUpdated++;
				}
				// Apply classifier I18N if provided
				if (updateClassifier.i18n != null && updateClassifier.i18n.hasAny()) {
					ToolI18NUtil.applyIfPresent(classifier, updateClassifier.i18n);
					changes.classifiersUpdated++;
				}
			}

			// Set default classifier if specified
			if (defaultClassifierName != null) {
				TLClassifier defaultClassifier = enumeration.getClassifier(defaultClassifierName);
				if (defaultClassifier != null) {
					// Clear all existing defaults
					for (TLClassifier classifier : enumeration.getClassifiers()) {
						if (classifier.isDefault()) {
							classifier.setDefault(false);
						}
					}
					// Set new default
					defaultClassifier.setDefault(true);
					changes.defaultChanged = true;
				}
			}

			tx.commit();

			String jsonResponse = buildSuccessJson(enumeration, changes);
			return new McpSchema.CallToolResult(jsonResponse, false);

		} catch (Exception ex) {
			return createErrorResult("Failed to update enumeration: " + ex.getMessage());
		}
	}

	/**
	 * Parses data for adding a new classifier.
	 */
	private static ClassifierUpdate parseAddClassifier(Map<String, Object> data)
			throws ToolArgumentUtil.ToolInputException {
		String classifierName = ToolArgumentUtil.requireNonEmptyString(
			data, "classifierName", "Classifier name");

		validateClassifierName(classifierName);

		Boolean isDefault = ToolArgumentUtil.getBooleanArgument(data, "default", false);
		ToolI18NUtil.LocalizedTexts i18n = ToolI18NUtil.extractFromArguments(data);

		return new ClassifierUpdate(classifierName, isDefault, i18n);
	}

	/**
	 * Parses data for updating an existing classifier.
	 */
	private static ClassifierUpdate parseUpdateClassifier(Map<String, Object> data)
			throws ToolArgumentUtil.ToolInputException {
		String classifierName = ToolArgumentUtil.requireNonEmptyString(
			data, "classifierName", "Classifier name");

		Boolean isDefault = null;
		if (data.containsKey("default")) {
			isDefault = ToolArgumentUtil.getBooleanArgument(data, "default", false);
		}

		ToolI18NUtil.LocalizedTexts i18n = ToolI18NUtil.extractFromArguments(data);

		return new ClassifierUpdate(classifierName, isDefault, i18n);
	}

	/**
	 * Validates classifier names against the allowed pattern.
	 */
	private static void validateClassifierName(String name) throws ToolArgumentUtil.ToolInputException {
		if (!CLASSIFIER_NAME_PATTERN.matcher(name).matches()) {
			throw new ToolArgumentUtil.ToolInputException(
				"Invalid classifier name '" + name + "'. Must start with a Java identifier character and contain only identifier parts, dots, and hyphens");
		}
	}

	/**
	 * Validates all classifier operations to ensure consistency.
	 */
	private static String validateClassifierOperations(
			TLEnumeration enumeration,
			List<ClassifierUpdate> addClassifiers,
			List<ClassifierUpdate> updateClassifiers,
			String defaultClassifierName) {

		// Check for duplicate names in {@code addClassifiers}
		Set<String> addNames = new HashSet<>();
		for (ClassifierUpdate add : addClassifiers) {
			if (addNames.contains(add.name)) {
				return "Duplicate classifier name in addClassifiers: " + add.name;
			}
			addNames.add(add.name);
		}

		// Check that update classifiers exist
		Set<String> existingClassifierNames = new LinkedHashSet<>();
		for (TLClassifier classifier : enumeration.getClassifiers()) {
			existingClassifierNames.add(classifier.getName());
		}

		for (ClassifierUpdate update : updateClassifiers) {
			if (!existingClassifierNames.contains(update.name)) {
				return "Classifier '" + update.name + "' does not exist in enumeration '" + enumeration.getName() + "'";
			}
		}

		// Check for conflicts between add and update operations
		for (ClassifierUpdate add : addClassifiers) {
			if (existingClassifierNames.contains(add.name)) {
				return "Cannot add classifier '" + add.name + "' because it already exists in the enumeration. Use updateClassifiers instead.";
			}
		}

		// Validate default classifier exists
		if (defaultClassifierName != null) {
			if (!existingClassifierNames.contains(defaultClassifierName)) {
				boolean existsInAdd = addClassifiers.stream()
					.anyMatch(add -> add.name.equals(defaultClassifierName));
				if (!existsInAdd) {
					return "Default classifier '" + defaultClassifierName + "' does not exist and is not being added";
				}
			}
		}

		// Validate single default constraint
		Set<String> defaultCandidates = new HashSet<>();

		// Check existing defaults
		for (TLClassifier classifier : enumeration.getClassifiers()) {
			if (classifier.isDefault()) {
				defaultCandidates.add(classifier.getName());
			}
		}

		// Check add classifiers with default flag
		for (ClassifierUpdate add : addClassifiers) {
			if (Boolean.TRUE.equals(add.isDefault)) {
				defaultCandidates.add(add.name);
			}
		}

		// Check update classifiers with default flag
		for (ClassifierUpdate update : updateClassifiers) {
			if (Boolean.TRUE.equals(update.isDefault)) {
				defaultCandidates.add(update.name);
			}
		}

		// Add explicit default classifier
		if (defaultClassifierName != null) {
			defaultCandidates.add(defaultClassifierName);
		}

		if (defaultCandidates.size() > 1) {
			return "Multiple default classifiers specified: " + String.join(", ", defaultCandidates) + ". Only one default is allowed.";
		}

		return null; // No validation errors
	}

	/**
	 * Builds a JSON response for successful enumeration updates.
	 */
	private static String buildSuccessJson(TLEnumeration enumeration, ChangeTracker changes) throws IOException {
		StringWriter buffer = new StringWriter();
		try (JsonWriter json = JsonResponseBuilder.createWriter(buffer)) {
			json.beginObject();

			json.name("success").value(true);
			json.name("updated").value(true);

			json.name("enumeration").beginObject();
			json.name("name").value(enumeration.getName());
			json.name("tid").value(enumeration.tId().asString());
			json.name("module").value(enumeration.getModule().getName());

			// Add label and description
			JsonResponseBuilder.writeLabelAndDescription(json, TLModelNamingConvention.getTypeLabelKey(enumeration));

			// Add classifiers
			json.name("classifiers").beginArray();
			for (TLClassifier classifier : enumeration.getClassifiers()) {
				json.beginObject();
				json.name("name").value(classifier.getName());
				json.name("default").value(classifier.isDefault());

				// Add classifier label and description
				JsonResponseBuilder.writeLabelAndDescription(json, TLModelNamingConvention.resourceKey(classifier));

				json.endObject();
			}
			json.endArray();

			json.endObject();

			// Add change summary
			json.name("changes").beginObject();
			json.name("enumerationUpdated").value(changes.enumerationUpdated);
			json.name("classifiersAdded").value(changes.classifiersAdded);
			json.name("classifiersUpdated").value(changes.classifiersUpdated);
			json.name("defaultChanged").value(changes.defaultChanged);
			json.endObject();

			json.name("message").value("Enumeration '" + enumeration.getName() + "' updated successfully");

			json.endObject();
		}
		return buffer.toString();
	}

	/**
	 * Creates a standardized error response.
	 */
	private static McpSchema.CallToolResult createErrorResult(String errorMessage) {
		String errorJson = JsonResponseBuilder.buildToolErrorJson(errorMessage);
		return new McpSchema.CallToolResult(errorJson, false);
	}

	/**
	 * Internal record for holding classifier update data.
	 */
	private static record ClassifierUpdate(
		String name,
		Boolean isDefault,
		ToolI18NUtil.LocalizedTexts i18n) {
	}

	/**
	 * Helper class to track changes made during the update operation.
	 */
	private static class ChangeTracker {
		boolean enumerationUpdated = false;
		int classifiersAdded = 0;
		int classifiersUpdated = 0;
		boolean defaultChanged = false;
	}
}