/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import com.top_logic.ai.service.tools.util.JsonToolResponseBuilder;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.visit.LabelVisitor;
import com.top_logic.util.Resources;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool for accessing TopLogic instance data from the knowledge base.
 *
 * <p>
 * This tool provides two methods for working with instances:
 * <ul>
 * <li>{@link #listInstances(String, String)} - Lists all TIDs for a type</li>
 * <li>{@link #getInstance(String)} - Gets detailed info for a specific instance</li>
 * </ul>
 * </p>
 *
 * <p>
 * All methods return JSON-formatted strings for consumption by AI agents.
 * </p>
 */
public class InstanceBrowserTool {

	/**
	 * Lists all instances of a specific type.
	 *
	 * <p>
	 * Returns a JSON object containing the module name, type name, instance count, and an array
	 * of TID strings for all instances. These TIDs can be used with {@link #getInstance(String)}
	 * to retrieve detailed information.
	 * </p>
	 *
	 * @param moduleName
	 *        The module containing the type.
	 * @param typeName
	 *        The type name.
	 * @return JSON object with instance TIDs, or error JSON if type not found.
	 */
	@Tool("Get all instances of a specific type (returns object TIDs for further inspection)")
	public String listInstances(
			@P("The module containing the type") String moduleName,
			@P("The type name") String typeName) {

		return ThreadContextManager.inSystemInteraction(
			InstanceBrowserTool.class,
			() -> {
				try {
					// Validate parameters
					if (moduleName == null || moduleName.isEmpty()) {
						return JsonToolResponseBuilder.buildErrorJson("Module name is required");
					}
					if (typeName == null || typeName.isEmpty()) {
						return JsonToolResponseBuilder.buildErrorJson("Type name is required");
					}

					// Get the type using MetaElementUtil
					TLClass type = MetaElementUtil.getGlobalType(moduleName, typeName);
					if (type == null) {
						return JsonToolResponseBuilder.buildErrorJson(
							"Type '" + typeName + "' not found in module '" + moduleName + "'");
					}

					// Get all instances of this type
					List<TLObject> allInstances = MetaElementUtil.getAllInstancesOf(type, TLObject.class);

					// Build JSON response with all TIDs
					StringWriter buffer = new StringWriter();
					try (JsonWriter json = JsonToolResponseBuilder.createWriter(buffer)) {
						json.beginObject();

						// Basic information
						json.name("module").value(moduleName);
						json.name("type").value(typeName);
						json.name("count").value(allInstances.size());

						// Array of TIDs
						json.name("instances");
						json.beginArray();
						for (TLObject instance : allInstances) {
							json.value(instance.tId().asString());
						}
						json.endArray();

						json.endObject();
					}

					String result = buffer.toString();
					Logger.info("listInstances() result: " + result, InstanceBrowserTool.class);
					System.out.println("InstanceBrowserTool.listInstances() result: " + result);
					return result;

				} catch (IOException ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Failed to list instances: " + ex.getMessage());
					Logger.error("listInstances() error: " + ex.getMessage(), ex, InstanceBrowserTool.class);
					System.err.println("InstanceBrowserTool.listInstances() error: " + error);
					return error;
				} catch (Exception ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Error accessing instances: " + ex.getMessage());
					Logger.error("listInstances() error: " + ex.getMessage(), ex, InstanceBrowserTool.class);
					System.err.println("InstanceBrowserTool.listInstances() error: " + error);
					return error;
				}
			});
	}

	/**
	 * Gets detailed information about a single instance by its TID.
	 *
	 * <p>
	 * Returns a JSON object containing the instance's TID, type information, all attribute
	 * values, and metadata (container, transient status). References to other objects are
	 * represented as structured objects with TIDs.
	 * </p>
	 *
	 * @param tid
	 *        The TopLogic identifier in format "TypeName:id".
	 * @return JSON object with instance details, or error JSON if instance not found.
	 */
	@Tool("Get detailed information about a single instance by its TID including all attributes and values")
	public String getInstance(@P("The TopLogic identifier (TID) in format 'TypeName:id'") String tid) {

		return ThreadContextManager.inSystemInteraction(
			InstanceBrowserTool.class,
			() -> {
				try {
					// Validate parameter
					if (tid == null || tid.isEmpty()) {
						return JsonToolResponseBuilder.buildErrorJson("TID is required");
					}

					// Parse TID (format: "TypeName:hexId")
					String[] split = tid.split(":", 2);
					if (split.length != 2) {
						return JsonToolResponseBuilder.buildErrorJson(
							"Invalid TID format. Expected 'TypeName:id', got: " + tid);
					}

					String typeName = split[0];
					String id = split[1];

					// Convert string ID to TLID
					TLID tlid = IdentifierUtil.fromExternalForm(id);

					// Get the knowledge base
					KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

					// Get the knowledge object
					KnowledgeObject ko = kb.getKnowledgeObject(typeName, tlid);

					if (ko == null) {
						return JsonToolResponseBuilder.buildErrorJson("Object with TID '" + tid + "' not found");
					}

					// Wrap it as a TLObject
					TLObject tlObject = WrapperFactory.getWrapper(ko);

					// Build JSON response with all object information
					StringWriter buffer = new StringWriter();
					try (JsonWriter json = JsonToolResponseBuilder.createWriter(buffer)) {
						json.beginObject();

						// Basic object information
						json.name("tid").value(tid);

						// Type information
						TLStructuredType type = tlObject.tType();
						if (type != null) {
							json.name("type");
							writeTypeInfo(json, type);
						}

						// Attributes
						json.name("attributes");
						json.beginObject();

						if (type != null) {
							// Get all parts (attributes) of this type
							for (TLStructuredTypePart part : type.getAllParts()) {
								String partName = part.getName();
								Object value = tlObject.tValue(part);

								// Write attribute value
								writeAttributeValue(json, partName, value);
							}
						}

						json.endObject(); // attributes

						// Additional metadata
						json.name("metadata");
						json.beginObject();
						json.name("container").value(
							tlObject.tContainer() != null ? tlObject.tContainer().tId().toString() : null);
						json.name("containerReference").value(
							tlObject.tContainerReference() != null ? tlObject.tContainerReference().getName() : null);
						json.name("transient").value(tlObject.tTransient());
						json.endObject(); // metadata

						json.endObject(); // root
					}

					String result = buffer.toString();
					Logger.info("getInstance() result: " + result, InstanceBrowserTool.class);
					System.out.println("InstanceBrowserTool.getInstance() result: " + result);
					return result;

				} catch (IOException ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Failed to get instance: " + ex.getMessage());
					Logger.error("getInstance() error: " + ex.getMessage(), ex, InstanceBrowserTool.class);
					System.err.println("InstanceBrowserTool.getInstance() error: " + error);
					return error;
				} catch (Exception ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Error retrieving instance: " + ex.getMessage());
					Logger.error("getInstance() error: " + ex.getMessage(), ex, InstanceBrowserTool.class);
					System.err.println("InstanceBrowserTool.getInstance() error: " + error);
					return error;
				}
			});
	}

	/**
	 * Writes type information to JSON output.
	 */
	private static void writeTypeInfo(JsonWriter json, TLStructuredType type) throws IOException {
		json.beginObject();
		json.name("name").value(type.getName());

		// Module information
		TLModule module = type.getModule();
		if (module != null) {
			json.name("module").value(module.getName());

			// Module label if available
			ResKey moduleKey = LabelVisitor.getModuleResourceKey(module);
			Resources resources = Resources.getInstance();
			String moduleLabel = resources.getString(moduleKey, null);
			if (moduleLabel != null && !moduleLabel.isEmpty()) {
				json.name("moduleLabel").value(moduleLabel);
			}
		}

		// Type label if available
		ResKey typeKey = TLModelNamingConvention.getTypeLabelKey(type);
		Resources resources = Resources.getInstance();
		String typeLabel = resources.getString(typeKey, null);
		if (typeLabel != null && !typeLabel.isEmpty()) {
			json.name("label").value(typeLabel);
		}

		// Model kind
		ModelKind kind = type.getModelKind();
		if (kind != null) {
			json.name("kind").value(kind.name().toLowerCase());
		}

		json.endObject();
	}

	/**
	 * Writes an attribute value to JSON output with proper type handling.
	 */
	private static void writeAttributeValue(JsonWriter json, String name, Object value) throws IOException {
		json.name(name);

		if (value == null) {
			json.nullValue();
		} else if (value instanceof TLObject) {
			// For reference to another TLObject, use the utility method
			JsonToolResponseBuilder.writeTLObjectReference(json, (TLObject) value);
		} else if (value instanceof Iterable) {
			// For collections, write as JSON array
			json.beginArray();
			for (Object item : (Iterable<?>) value) {
				if (item instanceof TLObject) {
					JsonToolResponseBuilder.writeTLObjectReference(json, (TLObject) item);
				} else {
					json.value(item != null ? item.toString() : null);
				}
			}
			json.endArray();
		} else {
			// For primitive values, write as string
			json.value(value.toString());
		}
	}
}
