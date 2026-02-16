/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import com.top_logic.ai.mcp.server.util.JsonResponseBuilder;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.json.gstream.JsonWriter;
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
 * MCP tool for creating backward references to existing forward references.
 * <p>
 * This tool adds a navigable backward reference to an existing unidirectional forward reference,
 * converting it into a bidirectional association. The backward reference is created on the target
 * class of the forward reference, pointing back to the source class.
 * </p>
 * <p>
 * The tool finds the existing forward reference, locates its backward association end (which has no
 * TLReference), and creates a new TLReference on that end, making it navigable.
 * </p>
 */
public class BackwardReferenceCreationTool {

	/** Tool name for the backward reference creation functionality. */
	public static final String TOOL_NAME = "create-backward-reference";

	/** Tool description. */
	private static final String DESCRIPTION =
		"Create a backward reference to an existing forward reference, "
			+ "or return an existing one by name.";

	/** JSON Schema for the tool input. */
	private static final String INPUT_SCHEMA_JSON = """
			{
			  "type": "object",
			  "properties": {
			    "moduleName": {
			      "type": "string",
			      "description": "Name of the module containing the source class (that has the forward reference)"
			    },
			    "className": {
			      "type": "string",
			      "description": "Name of the source class that has the forward reference"
			    },
			    "forwardReferenceName": {
			      "type": "string",
			      "description": "Name of the existing forward reference"
			    },
			    "backwardReferenceName": {
			      "type": "string",
			      "description": "Name for the new backward reference"
			    },
			    "navigate": {
			      "type": "boolean",
			      "description": "Whether the backward reference is navigable (default: true)"
			    },
			    "label": {
			      "type": "object",
			      "properties": {
			        "en": {
			          "type": "string",
			          "description": "English label for the backward reference"
			        },
			        "de": {
			          "type": "string",
			          "description": "German label for the backward reference"
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
			          "description": "English description for the backward reference"
			        },
			        "de": {
			          "type": "string",
			          "description": "German description for the backward reference"
			        }
			      },
			      "required": ["en", "de"],
			      "additionalProperties": false
			    }
			  },
			  "required": ["moduleName", "className", "forwardReferenceName", "backwardReferenceName"],
			  "additionalProperties": false
			}
			""";

	/**
	 * Creates the MCP tool specification for backward reference creation.
	 *
	 * @return The tool specification that can be registered with the MCP server.
	 */
	public static McpServerFeatures.SyncToolSpecification createSpecification() {
		McpJsonMapper jsonMapper = McpJsonMapper.getDefault();

		McpSchema.Tool tool = McpSchema.Tool.builder()
			.name(TOOL_NAME)
			.title("Create TopLogic backward reference")
			.description(DESCRIPTION)
			.inputSchema(jsonMapper, INPUT_SCHEMA_JSON)
			.build();

		return McpServerFeatures.SyncToolSpecification.builder()
			.tool(tool)
			.callHandler(BackwardReferenceCreationTool::handleToolRequest)
			.build();
	}

	/**
	 * Handles tool requests for creating backward references.
	 *
	 * @param exchange
	 *        The MCP server exchange for interacting with the client.
	 * @param request
	 *        The tool call request (includes tool name, arguments, etc.).
	 * @return The result of the backward reference creation operation.
	 */
	private static McpSchema.CallToolResult handleToolRequest(
			McpSyncServerExchange exchange,
			CallToolRequest request) {

		Map<String, Object> arguments = request.arguments();

		return ThreadContextManager.inSystemInteraction(
			BackwardReferenceCreationTool.class,
			() -> createBackwardReference(arguments));
	}

	/**
	 * Creates a backward reference based on the given arguments.
	 *
	 * @param arguments
	 *        The tool call arguments containing module name, class name, forward reference name,
	 *        backward reference name, and configuration.
	 * @return The result indicating success or failure with backward reference details.
	 */
	private static McpSchema.CallToolResult createBackwardReference(Map<String, Object> arguments) {
		// Extract and validate arguments
		final String moduleName;
		final String className;
		final String forwardReferenceName;
		final String backwardReferenceName;

		try {
			moduleName = ToolArgumentUtil.requireNonEmptyString(
				arguments,
				"moduleName",
				"Module name");

			className = ToolArgumentUtil.requireNonEmptyString(
				arguments,
				"className",
				"Class name");

			forwardReferenceName = ToolArgumentUtil.requireNonEmptyString(
				arguments,
				"forwardReferenceName",
				"Forward reference name");

			backwardReferenceName = ToolArgumentUtil.requireNonEmptyString(
				arguments,
				"backwardReferenceName",
				"Backward reference name");

		} catch (ToolArgumentUtil.ToolInputException e) {
			return createErrorResult(e.getMessage());
		}

		// Extract optional navigate flag (default: true)
		boolean navigate = ToolArgumentUtil.getBooleanArgument(arguments, "navigate", true);

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

		// Find existing forward reference
		TLStructuredTypePart existingPart = sourceClass.getPart(forwardReferenceName);
		if (existingPart == null) {
			return createErrorResult(
				"Forward reference '" + forwardReferenceName + "' does not exist in class '" + className + "'");
		}
		if (!(existingPart instanceof TLReference)) {
			return createErrorResult(
				"'" + forwardReferenceName + "' exists in class '" + className + "' but is not a reference");
		}
		TLReference forwardRef = (TLReference) existingPart;

		// Get target class (where backward ref will be added)
		TLAssociationEnd forwardEnd = forwardRef.getEnd();
		TLType targetType = forwardEnd.getType();
		if (!(targetType instanceof TLClass)) {
			return createErrorResult(
				"Target type of forward reference must be a class, but is: " + targetType.getClass().getSimpleName());
		}
		TLClass targetClass = (TLClass) targetType;

		// Check if backward reference already exists on target class
		TLStructuredTypePart existingBackwardPart = targetClass.getPart(backwardReferenceName);
		if (existingBackwardPart != null) {
			if (!(existingBackwardPart instanceof TLReference)) {
				return createErrorResult(
					"Part '" + backwardReferenceName + "' already exists on class '" + targetClass.getName()
						+ "' but is not a reference");
			}
			// IDEMPOTENT: Existiert bereits, gebe zurück
			TLReference backwardRef = (TLReference) existingBackwardPart;
			try {
				String jsonResponse = buildSuccessJson(backwardRef, forwardRef, false);
				return new McpSchema.CallToolResult(jsonResponse, false);
			} catch (Exception ex) {
				return createErrorResult("Failed to build success response: " + ex.getMessage());
			}
		}

		// Get association
		TLAssociation association = forwardEnd.getOwner();

		// Get backward end (das andere End in der Association)
		TLAssociationEnd backwardEnd = TLModelUtil.getOtherEnd(forwardEnd);

		// Verify backward end hat KEINE TLReference yet
		if (backwardEnd.getReference() != null) {
			return createErrorResult(
				"Backward end already has a reference: " + backwardEnd.getReference().getName());
		}

		// Verify backward end zeigt auf source class
		if (backwardEnd.getType() != sourceClass) {
			return createErrorResult(
				"Backward end does not point to source class. Expected: " + sourceClass.getName()
					+ ", but points to: " + backwardEnd.getType().getName());
		}

		// Create backward reference within transaction
		TLReference backwardRef;
		try (Transaction tx =
			PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Create Backward Reference"))) {

			// Create TLReference on target class pointing to backward end
			backwardRef = TLModelUtil.addReference(
				targetClass,
				backwardReferenceName,
				backwardEnd);

			// Configure navigation
			backwardEnd.setNavigate(navigate);

			// Configure multiplicity (copy from forward reference)
			backwardRef.setMandatory(forwardRef.isMandatory());
			backwardRef.setMultiple(forwardRef.isMultiple());
			backwardRef.setOrdered(forwardRef.isOrdered());
			backwardRef.setBag(forwardRef.isBag());
			backwardRef.setAbstract(forwardRef.isAbstract());

			// Note: History type and deletion policy are already configured on {@code backwardEnd}
			// (they were set when the forward reference was created)

			tx.commit();

		} catch (Exception ex) {
			return createErrorResult("Failed to create backward reference: " + ex.getMessage());
		}

		// Apply i18n only after successful creation (not for existing references)
		ToolI18NUtil.applyIfPresent(backwardRef, i18n);

		try {
			String jsonResponse = buildSuccessJson(backwardRef, forwardRef, true);
			return new McpSchema.CallToolResult(jsonResponse, false);
		} catch (Exception ex) {
			return createErrorResult("Failed to build success response: " + ex.getMessage());
		}
	}

	/**
	 * Builds a JSON response for successful backward reference creation.
	 *
	 * @param backwardRef
	 *        The created or retrieved backward reference.
	 * @param forwardRef
	 *        The existing forward reference.
	 * @param newlyCreated
	 *        Whether the backward reference was newly created or already existed.
	 */
	private static String buildSuccessJson(TLReference backwardRef, TLReference forwardRef, boolean newlyCreated)
			throws IOException {
		StringWriter buffer = new StringWriter();
		try (JsonWriter json = JsonResponseBuilder.createWriter(buffer)) {
			json.beginObject();

			json.name("success").value(true);

			// Backward reference details
			json.name("backwardReference").beginObject();
			json.name("name").value(backwardRef.getName());
			json.name("tid").value(backwardRef.tId().asString());
			json.name("owner")
				.value(backwardRef.getOwner().getModule().getName() + ":" + backwardRef.getOwner().getName());
			json.name("targetType")
				.value(backwardRef.getType().getModule().getName() + ":" + backwardRef.getType().getName());
			json.name("mandatory").value(backwardRef.isMandatory());
			json.name("multiple").value(backwardRef.isMultiple());
			json.name("ordered").value(backwardRef.isOrdered());
			json.name("bag").value(backwardRef.isBag());
			json.name("abstract").value(backwardRef.isAbstract());
			json.name("navigate").value(backwardRef.getEnd().canNavigate());

			// Add label and description from backward reference resource key (optional)
			JsonResponseBuilder.writeLabelAndDescription(json,
				TLModelNamingConvention.resourceKey(backwardRef));

			json.endObject();

			// Forward reference info
			json.name("forwardReference").beginObject();
			json.name("name").value(forwardRef.getName());
			json.name("owner")
				.value(forwardRef.getOwner().getModule().getName() + ":" + forwardRef.getOwner().getName());
			json.endObject();

			json.name("created").value(newlyCreated);

			if (newlyCreated) {
				json.name("message").value("Backward reference '" + backwardRef.getName()
					+ "' created successfully in class '" + backwardRef.getOwner().getName() + "'");
			} else {
				json.name("message").value("Backward reference '" + backwardRef.getName()
					+ "' already existed in class '" + backwardRef.getOwner().getName() + "'");
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
