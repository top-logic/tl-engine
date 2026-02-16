/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Pattern;

import com.top_logic.ai.mcp.server.util.JsonResponseBuilder;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
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
 * MCP tool for creating unidirectional references on TopLogic classes.
 * <p>
 * This tool creates a reference from one class to another in the TopLogic application model using
 * {@link TLModelUtil#addAssociation}, {@link TLModelUtil#addEnd}, and
 * {@link TLModelUtil#addReference}. The tool supports comprehensive configuration of reference
 * properties including multiplicity, composition, aggregation, navigation optimization, history
 * type, and deletion policy.
 * </p>
 * <p>
 * The reference is unidirectional - only the forward direction is navigable. The backward
 * association end is created automatically by the framework for structural completeness (required
 * by TopLogic) but no TLReference is created for it, making it effectively non-navigable.
 * </p>
 */
public class ReferenceCreationTool {

	/** Tool name for the reference creation functionality. */
	public static final String TOOL_NAME = "create-reference";

	/** Tool description. */
	private static final String DESCRIPTION =
		"Create a new reference on a TopLogic class pointing to another class, "
			+ "or return an existing one by name.";

	/** JSON Schema for the tool input. */
	private static final String INPUT_SCHEMA_JSON = """
			{
			  "type": "object",
			  "properties": {
			    "moduleName": {
			      "type": "string",
			      "description": "Name of the module containing the source class"
			    },
			    "className": {
			      "type": "string",
			      "description": "Name of the source class to add the reference to"
			    },
			    "referenceName": {
				  "type": "string",
				  "description": "Name of the reference. Must start with a letter or underscore, may contain only letters (including umlauts), digits, dots, underscores, and minus signs, and must not end with a dot."
				},
			    "targetModuleName": {
			      "type": "string",
			      "description": "Name of the module containing the target class"
			    },
			    "targetClassName": {
			      "type": "string",
			      "description": "Name of the target class the reference points to"
			    },
			    "mandatory": {
			      "type": "boolean",
			      "description": "Whether the reference is required (default: false)"
			    },
			    "multiple": {
			      "type": "boolean",
			      "description": "Whether the reference can hold multiple values (default: false)"
			    },
			    "ordered": {
			      "type": "boolean",
			      "description": "Whether order matters for multiple-valued references (default: false)"
			    },
			    "bag": {
			      "type": "boolean",
			      "description": "Whether duplicates are allowed for multiple-valued references (default: false)"
			    },
			    "abstract": {
			      "type": "boolean",
			      "description": "Whether this reference must be overridden in specific classes (default: false)"
			    },
			    "composite": {
			      "type": "boolean",
			      "description": "Whether this reference points to a composite part (default: false)"
			    },
			    "aggregate": {
			      "type": "boolean",
			      "description": "Whether this reference points to an aggregate part (default: false)"
			    },
			    "navigate": {
			      "type": "boolean",
			      "description": "Whether navigation is efficient for this reference (default: true)"
			    },
			    "historyType": {
			      "type": "string",
			      "description": "Type of history for reference values: CURRENT (default), HISTORIC, or MIXED"
			    },
			    "deletionPolicy": {
			      "type": "string",
			      "description": "How to handle deletion of referenced objects: CLEAR_REFERENCE (default), DELETE_OBJECT, STABILISE_REFERENCE, or VETO"
			    },
			    "label": {
			      "type": "object",
			      "properties": {
			        "en": {
			          "type": "string",
			          "description": "English label for the reference"
			        },
			        "de": {
			          "type": "string",
			          "description": "German label for the reference"
			        }
			      },
			      "required": ["en", "de"],
			      "additionalProperties": false
			    },
			    "description": {
			      "type": "object",
			      "properties": {
			        "en": {
			          "type": "string",
			          "description": "English description for the reference"
			        },
			        "de": {
			          "type": "string",
			          "description": "German description for the reference"
			        }
			      },
			      "required": ["en", "de"],
			      "additionalProperties": false
			    }
			  },
			  "required": ["moduleName", "className", "referenceName", "targetModuleName", "targetClassName"],
			  "additionalProperties": false
			}
			""";

	private static final Pattern REFERENCE_NAME_PATTERN = Pattern.compile(
		"^[\\p{L}_](?:[\\p{L}0-9._-]*[\\p{L}0-9_-])?$");

	/**
	 * Creates the MCP tool specification for reference creation.
	 *
	 * @return The tool specification that can be registered with the MCP server.
	 */
	public static McpServerFeatures.SyncToolSpecification createSpecification() {
		// Get (or lazily create) the default JSON mapper from the MCP JSON module
		McpJsonMapper jsonMapper = McpJsonMapper.getDefault();

		McpSchema.Tool tool = McpSchema.Tool.builder()
			.name(TOOL_NAME)
			.title("Create TopLogic reference")
			.description(DESCRIPTION)
			.inputSchema(jsonMapper, INPUT_SCHEMA_JSON)
			.build();

		return McpServerFeatures.SyncToolSpecification.builder()
			.tool(tool)
			.callHandler(ReferenceCreationTool::handleToolRequest)
			.build();
	}

	/**
	 * Handles tool requests for creating references.
	 *
	 * @param exchange
	 *        The MCP server exchange for interacting with the client.
	 * @param request
	 *        The tool call request (includes tool name, arguments, etc.).
	 * @return The result of the reference creation operation.
	 */
	private static McpSchema.CallToolResult handleToolRequest(
			McpSyncServerExchange exchange,
			CallToolRequest request) {

		Map<String, Object> arguments = request.arguments();

		return ThreadContextManager.inSystemInteraction(
			ReferenceCreationTool.class,
			() -> createReference(arguments));
	}

	/**
	 * Creates a reference based on the given arguments.
	 *
	 * @param arguments
	 *        The tool call arguments containing module names, class names, reference name, and
	 *        configuration.
	 * @return The result indicating success or failure with reference details.
	 */
	private static McpSchema.CallToolResult createReference(Map<String, Object> arguments) {
		// Extract and validate arguments
		final String moduleName;
		final String className;
		final String referenceName;
		final String targetModuleName;
		final String targetClassName;

		try {
			moduleName = ToolArgumentUtil.requireNonEmptyString(
				arguments,
				"moduleName",
				"Module name");

			className = ToolArgumentUtil.requireNonEmptyString(
				arguments,
				"className",
				"Class name");

			referenceName = ToolArgumentUtil.requireNonEmptyString(
				arguments,
				"referenceName",
				"Reference name");
			validateReferenceName(referenceName);

			targetModuleName = ToolArgumentUtil.requireNonEmptyString(
				arguments,
				"targetModuleName",
				"Target module name");

			targetClassName = ToolArgumentUtil.requireNonEmptyString(
				arguments,
				"targetClassName",
				"Target class name");

		} catch (ToolArgumentUtil.ToolInputException e) {
			return createErrorResult(e.getMessage());
		}

		// Extract optional flags
		boolean mandatory = ToolArgumentUtil.getBooleanArgument(arguments, "mandatory", false);
		boolean multiple = ToolArgumentUtil.getBooleanArgument(arguments, "multiple", false);
		boolean ordered = ToolArgumentUtil.getBooleanArgument(arguments, "ordered", false);
		boolean bag = ToolArgumentUtil.getBooleanArgument(arguments, "bag", false);
		boolean isAbstract = ToolArgumentUtil.getBooleanArgument(arguments, "abstract", false);
		boolean composite = ToolArgumentUtil.getBooleanArgument(arguments, "composite", false);
		boolean aggregate = ToolArgumentUtil.getBooleanArgument(arguments, "aggregate", false);
		boolean navigate = ToolArgumentUtil.getBooleanArgument(arguments, "navigate", true);

		// Extract optional enum values with defaults
		final HistoryType historyType;
		final DeletionPolicy deletionPolicy;
		try {
			String historyTypeStr = ToolArgumentUtil.getOptionalString(arguments, "historyType", "CURRENT");
			historyType = resolveHistoryType(historyTypeStr);

			String deletionPolicyStr = ToolArgumentUtil.getOptionalString(arguments, "deletionPolicy", "CLEAR_REFERENCE");
			deletionPolicy = resolveDeletionPolicy(deletionPolicyStr);

		} catch (ToolArgumentUtil.ToolInputException e) {
			return createErrorResult(e.getMessage());
		}

		// Extract I18N once, for potential use if creating new reference
		ToolI18NUtil.LocalizedTexts i18n = ToolI18NUtil.extractFromArguments(arguments);

		// Look up source module
		TLModel model = ModelService.getApplicationModel();
		TLModule module = model.getModule(moduleName);
		if (module == null) {
			return createErrorResult("Module '" + moduleName + "' does not exist");
		}

		// Look up source class
		TLType existingType = module.getType(className);
		if (existingType == null) {
			return createErrorResult("Class '" + className + "' does not exist in module '" + moduleName + "'");
		}
		if (!(existingType instanceof TLClass)) {
			return createErrorResult(
				"Type '" + className + "' exists in module '" + moduleName + "' but is not a class");
		}
		TLClass sourceClass = (TLClass) existingType;

		// Look up target module
		TLModule targetModule = model.getModule(targetModuleName);
		if (targetModule == null) {
			return createErrorResult("Target module '" + targetModuleName + "' does not exist");
		}

		// Look up target class
		TLType targetType = targetModule.getType(targetClassName);
		if (targetType == null) {
			return createErrorResult(
				"Target class '" + targetClassName + "' does not exist in module '" + targetModuleName + "'");
		}
		if (!(targetType instanceof TLClass)) {
			return createErrorResult(
				"Target type '" + targetClassName + "' exists in module '" + targetModuleName + "' but is not a class");
		}
		TLClass targetClass = (TLClass) targetType;

		// Check if reference already exists
		TLStructuredTypePart existingPart = sourceClass.getPart(referenceName);
		TLReference reference;
		boolean created = false;

		if (existingPart != null) {
			// Already exists: ensure it is actually a TLReference
			if (!(existingPart instanceof TLReference)) {
				return createErrorResult(
					"Structured type part '" + referenceName + "' already exists on class '" + className
						+ "' but is not a reference");
			}
			reference = (TLReference) existingPart;
			// Variant A: return existing reference without modifying it
		} else {
			// Create reference within transaction
			try (Transaction tx =
				PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Create Reference"))) {

				// Create association with generated name
				// Pattern: ClassName$referenceName (TopLogic convention for private associations)
				String associationName = sourceClass.getName() + "$" + referenceName;
				TLAssociation association = TLModelUtil.addAssociation(
					sourceClass.getModule(),
					sourceClass.getScope(),
					associationName);

				// WICHTIG: Backward end MUSS zuerst erstellt werden (Index 0),
				// damit forward end Index 1 bekommt und AssociationStorage verwendet wird (nicht ReverseStorage)

				// Create backward end FIRST for structural completeness (required by TopLogic)
				// Note: No TLReference is created for this end (unidirectional reference)
				String backwardEndName = referenceName + "_source";
				TLAssociationEnd backwardEnd = TLModelUtil.addEnd(association, backwardEndName, sourceClass);  // Index 0

				// Create forward association end SECOND pointing to target
				TLAssociationEnd end = TLModelUtil.addEnd(association, referenceName, targetClass);  // Index 1

				// Create reference in source class (only for forward direction)
				// This end has Index 1 → will use AssociationStorage (not ReverseStorage)
				reference = TLModelUtil.addReference(sourceClass, referenceName, end);
				created = true;

				// Configure multiplicity (via reference)
				reference.setMandatory(mandatory);
				reference.setMultiple(multiple);
				reference.setOrdered(ordered);
				reference.setBag(bag);
				reference.setAbstract(isAbstract);

				// Configure reference-specific properties (via end)
				end.setComposite(composite);
				end.setAggregate(aggregate);
				end.setNavigate(navigate);

				// Configure advanced properties
				end.setHistoryType(historyType);
				end.setDeletionPolicy(deletionPolicy);

				// Configure backward end properties (Index 0 - no storage needed)
				backwardEnd.setNavigate(false);  // Non-navigable backward end
				backwardEnd.setHistoryType(historyType);  // Match forward end
				backwardEnd.setDeletionPolicy(deletionPolicy);  // Match forward end
				// Note: composite/aggregate are NOT set on backward end (opposite semantics)

				tx.commit();

			} catch (Exception ex) {
				return createErrorResult("Failed to create reference: " + ex.getMessage());
			}

			// Apply i18n only after successful creation (not for existing references)
			ToolI18NUtil.applyIfPresent(reference, i18n);
		}

		try {
			String jsonResponse = buildSuccessJson(reference, created);
			return new McpSchema.CallToolResult(jsonResponse, false);
		} catch (Exception ex) {
			return createErrorResult("Failed to build success response: " + ex.getMessage());
		}
	}

	/**
	 * Validates that the {@code referenceName} meets the naming requirements.
	 * <p>
	 * The name must start with a letter or underscore. It may only consist of letters (including
	 * umlauts), digits, dots, underscores, and minus signs and must not end with a dot.
	 * <p>
	 * Naming convention (not enforced): The name should start with a lower-case letter and only
	 * consist of letters, digits, and underscores.
	 */
	private static void validateReferenceName(String referenceName)
			throws ToolArgumentUtil.ToolInputException {

		if (!REFERENCE_NAME_PATTERN.matcher(referenceName).matches()) {
			throw new ToolArgumentUtil.ToolInputException(
				"Reference name must start with a letter or underscore, may only contain letters (including umlauts), "
					+ "digits, dots, underscores, and minus signs, and must not end with a dot.");
		}
	}

	/**
	 * Resolves a history type from the given string.
	 *
	 * @param value
	 *        The history type string (e.g., "CURRENT", "HISTORIC", "MIXED").
	 * @return The resolved HistoryType.
	 * @throws ToolArgumentUtil.ToolInputException
	 *         If the value is not a valid history type.
	 */
	private static HistoryType resolveHistoryType(String value)
			throws ToolArgumentUtil.ToolInputException {
		try {
			return HistoryType.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ToolArgumentUtil.ToolInputException(
				"Invalid historyType: '" + value + "'. " +
					"Valid values are: CURRENT, HISTORIC, MIXED");
		}
	}

	/**
	 * Resolves a deletion policy from the given string.
	 * <p>
	 * Supports both external name format (e.g., "clear-reference") and enum name format (e.g.,
	 * "CLEAR_REFERENCE").
	 * </p>
	 *
	 * @param value
	 *        The deletion policy string.
	 * @return The resolved DeletionPolicy.
	 * @throws ToolArgumentUtil.ToolInputException
	 *         If the value is not a valid deletion policy.
	 */
	private static DeletionPolicy resolveDeletionPolicy(String value)
			throws ToolArgumentUtil.ToolInputException {
		try {
			// First try to parse as external name format (e.g., "clear-reference")
			for (DeletionPolicy policy : DeletionPolicy.values()) {
				if (policy.getExternalName().equalsIgnoreCase(value)) {
					return policy;
				}
			}
			// Also try enum name format (e.g., "CLEAR_REFERENCE")
			return DeletionPolicy.valueOf(value.toUpperCase().replace('-', '_'));
		} catch (IllegalArgumentException e) {
			throw new ToolArgumentUtil.ToolInputException(
				"Invalid deletionPolicy: '" + value + "'. " +
					"Valid values are: CLEAR_REFERENCE, DELETE_OBJECT, STABILISE_REFERENCE, VETO");
		}
	}

	/**
	 * Builds a JSON response for successful reference creation.
	 *
	 * @param reference
	 *        The created or retrieved reference.
	 * @param newlyCreated
	 *        Whether the reference was newly created or already existed.
	 */
	private static String buildSuccessJson(TLReference reference, boolean newlyCreated) throws IOException {
		StringWriter buffer = new StringWriter();
		try (JsonWriter json = JsonResponseBuilder.createWriter(buffer)) {
			json.beginObject();

			json.name("success").value(true);
			json.name("reference").beginObject();
			json.name("name").value(reference.getName());
			json.name("tid").value(reference.tId().asString());
			json.name("owner")
				.value(reference.getOwner().getModule().getName() + ":" + reference.getOwner().getName());
			json.name("targetType")
				.value(reference.getType().getModule().getName() + ":" + reference.getType().getName());
			json.name("mandatory").value(reference.isMandatory());
			json.name("multiple").value(reference.isMultiple());
			json.name("ordered").value(reference.isOrdered());
			json.name("bag").value(reference.isBag());
			json.name("abstract").value(reference.isAbstract());
			json.name("composite").value(reference.isComposite());
			json.name("aggregate").value(reference.isAggregate());
			json.name("navigate").value(reference.getEnd().canNavigate());
			json.name("historyType").value(reference.getHistoryType().getExternalName());
			json.name("deletionPolicy").value(reference.getDeletionPolicy().getExternalName());

			// Add label and description from reference resource key (optional)
			JsonResponseBuilder.writeLabelAndDescription(json,
				TLModelNamingConvention.resourceKey(reference));

			json.endObject();
			json.name("created").value(newlyCreated);

			if (newlyCreated) {
				json.name("message").value("Reference '" + reference.getName()
					+ "' created successfully in class '" + reference.getOwner().getName() + "'");
			} else {
				json.name("message").value("Reference '" + reference.getName()
					+ "' already existed in class '" + reference.getOwner().getName() + "'");
			}

			json.endObject();
		}
		return buffer.toString();
	}

	/**
	 * Creates an error response.
	 *
	 * @param errorMessage
	 *        The error message to return.
	 * @return CallToolResult containing the error.
	 */
	private static McpSchema.CallToolResult createErrorResult(String errorMessage) {
		String errorJson = JsonResponseBuilder.buildToolErrorJson(errorMessage);
		return new McpSchema.CallToolResult(errorJson, true);
	}
}
