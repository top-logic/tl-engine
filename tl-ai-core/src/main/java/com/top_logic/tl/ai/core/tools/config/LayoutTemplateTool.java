/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.config;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.config.json.JsonConfigurationWriter;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.gui.layout.LayoutConfig;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.DynamicComponentService;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.core.tools.config.messages.LayoutCreateResponse;
import com.top_logic.tl.ai.core.tools.config.messages.LayoutTemplateInfo;
import com.top_logic.tl.ai.core.tools.config.messages.LayoutTemplateListResponse;
import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;
import com.top_logic.util.Resources;

import de.haumacher.msgbuf.io.StringW;

/**
 * Tool for managing TopLogic layout templates and layout configurations.
 *
 * <p>
 * This tool provides methods for:
 * <ul>
 * <li>{@link #listLayoutTemplates()} - Lists all available layout templates</li>
 * <li>{@link #getLayout(String)} - Retrieves a layout configuration by key</li>
 * <li>{@link #createLayout(String, String, String)} - Creates a new layout from a template</li>
 * </ul>
 * </p>
 *
 * <p>
 * All methods return JSON-formatted strings for consumption by AI agents.
 * </p>
 */
public class LayoutTemplateTool {

	/**
	 * Lists all available layout templates in the application.
	 *
	 * <p>
	 * Returns a JSON array of templates with their names, labels, and group memberships. Assistant
	 * templates are excluded from the listing.
	 * </p>
	 *
	 * @return JSON string containing the list of layout templates.
	 */
	@ToolMethod(description = "List all available layout templates with names, labels, and groups. Use the template name with getConfigSchema (toplogic://schema/layout-template/{name}) to get the parameter schema.", mode = ToolMode.READ)
	public String listLayoutTemplates() {
		try {
			LayoutTemplateListResponse response = ThreadContextManager.inSystemInteraction(
				LayoutTemplateTool.class,
				() -> {
					LayoutTemplateListResponse result = LayoutTemplateListResponse.create();
					Resources resources = Resources.getInstance();

					Collection<DynamicComponentDefinition> definitions =
						DynamicComponentService.getInstance().getComponentDefinitions();

					for (DynamicComponentDefinition definition : definitions) {
						// Skip assistant templates
						if (definition.getAssistentFor().isPresent()) {
							continue;
						}

						LayoutTemplateInfo info = LayoutTemplateInfo.create();
						info.setName(definition.definitionFile());

						// Resolve label
						String label = resources.getString(definition.label(), null);
						if (label != null && !label.isEmpty()) {
							info.setLabel(label);
						}

						// Add group memberships
						for (String group : definition.getBelongingGroups()) {
							info.addGroup(group);
						}

						result.addTemplate(info);
					}

					return result;
				});

			Map<String, Object> args = new HashMap<>();
			return DataClassSerializer.toJson(response, LayoutTemplateTool.class, "listLayoutTemplates()", args);

		} catch (Exception ex) {
			Logger.error("listLayoutTemplates() error: " + ex.getMessage(), ex, LayoutTemplateTool.class);
			Map<String, Object> args = new HashMap<>();
			return DataClassSerializer.errorToJson(
				"Error listing layout templates: " + ex.getMessage(),
				LayoutTemplateTool.class,
				"listLayoutTemplates()",
				args);
		}
	}

	/**
	 * Returns the layout key of the application's top-level layout.
	 *
	 * <p>
	 * This is the entry point for navigating the layout tree. Use the returned key with
	 * {@link #getLayout(String)} to inspect the top-level layout configuration.
	 * </p>
	 *
	 * @return The top-level layout key.
	 */
	@ToolMethod(description = "Get the top-level layout key of the application. Use this as entry point for getLayout to navigate the layout tree.", mode = ToolMode.READ)
	public String getTopLevelLayoutKey() {
		try {
			return ThreadContextManager.inSystemInteraction(
				LayoutTemplateTool.class,
				() -> LayoutConfig.getDefaultLayout());
		} catch (Exception ex) {
			Logger.error("getTopLevelLayoutKey() error: " + ex.getMessage(), ex, LayoutTemplateTool.class);
			return DataClassSerializer.errorToJson(
				"Error retrieving top-level layout key: " + ex.getMessage(),
				LayoutTemplateTool.class,
				"getTopLevelLayoutKey()",
				new HashMap<>());
		}
	}

	/**
	 * Retrieves the configuration of a specific layout.
	 *
	 * <p>
	 * For template-based layouts, returns the template name, arguments as JSON, and a schema URI.
	 * For non-template layouts, returns the raw component configuration as JSON.
	 * </p>
	 *
	 * <p>
	 * The response JSON is built directly (not via msgbuf) so that the configuration JSON
	 * ({@code arguments} or {@code componentConfig}) is embedded as a structured JSON object rather
	 * than double-encoded as a string.
	 * </p>
	 *
	 * @param layoutKey
	 *        The layout identifier.
	 * @return JSON string containing the layout configuration.
	 */
	@ToolMethod(description = "Get the configuration of a layout by its key. Returns template name and arguments for template-based layouts, or the component configuration for others. Use getTopLevelLayoutKey to find the application's top-level layout key.", mode = ToolMode.READ)
	public String getLayout(
			@P("The layout key (identifier). Use getTopLevelLayoutKey to find the top-level layout key.") String layoutKey) {

		try {
			String response = ThreadContextManager.inSystemInteraction(
				LayoutTemplateTool.class,
				() -> {
					if (layoutKey == null || layoutKey.isEmpty()) {
						throw new IllegalArgumentException("Layout key is required");
					}

					TLLayout layout = LayoutStorage.getInstance()
						.getLayout(ThemeFactory.getTheme(), null, layoutKey);

					if (layout == null) {
						throw new IllegalArgumentException("Layout not found: " + layoutKey);
					}

					return buildLayoutResponse(layoutKey, layout);
				});

			return response;

		} catch (Exception ex) {
			Logger.error("getLayout() error: " + ex.getMessage(), ex, LayoutTemplateTool.class);
			Map<String, Object> args = new HashMap<>();
			args.put("layoutKey", layoutKey);
			return DataClassSerializer.errorToJson(
				"Error retrieving layout: " + ex.getMessage(),
				LayoutTemplateTool.class,
				"getLayout()",
				args);
		}
	}

	/**
	 * Builds the JSON response for a layout, embedding configuration JSON directly.
	 */
	private String buildLayoutResponse(String layoutKey, TLLayout layout) throws Exception {
		StringW buffer = new StringW();
		try (de.haumacher.msgbuf.json.JsonWriter json = new de.haumacher.msgbuf.json.JsonWriter(buffer)) {
			json.beginObject();
			json.name("layoutKey");
			json.value(layoutKey);
			json.name("hasTemplate");
			json.value(layout.hasTemplate());

			if (layout.hasTemplate()) {
				String templateName = layout.getTemplateName();
				json.name("templateName");
				json.value(templateName);

				String schemaUri =
					ConfigSchemaTool.LAYOUT_TEMPLATE_SCHEMA_URI_PREFIX + templateName;

				// Serialize template arguments, embedding as raw JSON object.
				ConfigurationItem arguments = layout.getArguments();
				if (arguments != null) {
					json.name("arguments");
					json.jsonValue(serializeTemplateArguments(arguments, templateName, schemaUri));
				}

				json.name("templateSchemaUri");
				json.value(schemaUri);

			} else {
				// Serialize the full component config, embedding as raw JSON object.
				ConfigurationItem componentConfig = layout.get();
				if (componentConfig != null) {
					json.name("componentConfig");
					json.jsonValue(serializeConfig(componentConfig));
				}
			}

			json.endObject();
		}
		return buffer.toString();
	}

	/**
	 * Serializes template arguments to JSON, resolving the dynamic descriptor's {@code $type} to
	 * the layout template schema URI.
	 */
	private String serializeTemplateArguments(ConfigurationItem arguments, String templateName,
			String schemaUri) throws IOException {
		DynamicComponentDefinition definition =
			DynamicComponentService.getInstance().getComponentDefinition(templateName);
		ConfigurationDescriptor templateDescriptor =
			definition != null ? definition.descriptor() : null;

		StringWriter sw = new StringWriter();
		new JsonConfigurationWriter(sw) {
			@Override
			protected String resolveTypeId(
					ConfigurationDescriptor itemDescriptor,
					Class<?> typeClass) {
				if (itemDescriptor == templateDescriptor) {
					return schemaUri;
				}
				return super.resolveTypeId(itemDescriptor, typeClass);
			}
		}.prettyPrint().schemaAware().write(arguments);
		return sw.toString();
	}

	/**
	 * Serializes a configuration item to a JSON string.
	 */
	private String serializeConfig(ConfigurationItem config) throws IOException {
		StringWriter sw = new StringWriter();
		new JsonConfigurationWriter(sw).prettyPrint().schemaAware().write(config);
		return sw.toString();
	}

	/**
	 * Creates a new layout from a template.
	 *
	 * <p>
	 * The template arguments must match the schema of the template's configuration descriptor. Use
	 * {@link #listLayoutTemplates()} to find available templates, and the
	 * {@link ConfigSchemaTool#getConfigSchema(String)} tool with a
	 * {@code toplogic://schema/layout-template/} URI to obtain the parameter schema.
	 * </p>
	 *
	 * @param templateName
	 *        The name of the template to instantiate.
	 * @param layoutKey
	 *        The layout key for the new layout.
	 * @param argumentsJson
	 *        JSON string with the template arguments.
	 * @return JSON string indicating success or failure.
	 */
	@ToolMethod(description = "Create a new layout from a template. Use listLayoutTemplates to find templates and getConfigSchema with toplogic://schema/layout-template/{name} to get the parameter schema.")
	public String createLayout(
			@P("The name of the template to instantiate") String templateName,
			@P("The layout key for the new layout") String layoutKey,
			@P("JSON string with template arguments matching the template schema") String argumentsJson) {

		try {
			LayoutCreateResponse response = ThreadContextManager.inSystemInteraction(
				LayoutTemplateTool.class,
				() -> {
					if (templateName == null || templateName.isEmpty()) {
						throw new IllegalArgumentException("Template name is required");
					}
					if (layoutKey == null || layoutKey.isEmpty()) {
						throw new IllegalArgumentException("Layout key is required");
					}
					if (argumentsJson == null || argumentsJson.isEmpty()) {
						throw new IllegalArgumentException("Arguments JSON is required");
					}

					// Look up the template definition
					DynamicComponentDefinition definition =
						DynamicComponentService.getInstance().getComponentDefinition(templateName);

					if (definition == null) {
						throw new IllegalArgumentException("Template not found: " + templateName);
					}

					// Parse the JSON arguments using the template's descriptor
					ConfigurationDescriptor descriptor = definition.descriptor();
					ConfigurationItem arguments = new JsonConfigurationReader(
						SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, descriptor)
							.schemaAware()
							.setSource(CharacterContents.newContent(argumentsJson, "layout-arguments"))
							.read();

					// Store the layout (null person = global layout)
					LayoutStorage.storeLayout(null, layoutKey, templateName, arguments);

					LayoutCreateResponse result = LayoutCreateResponse.create();
					result.setSuccess(true);
					result.setLayoutKey(layoutKey);
					return result;
				});

			Map<String, Object> args = new HashMap<>();
			args.put("templateName", templateName);
			args.put("layoutKey", layoutKey);
			args.put("argumentsJson", argumentsJson);
			return DataClassSerializer.toJson(response, LayoutTemplateTool.class, "createLayout()", args);

		} catch (Exception ex) {
			Logger.error("createLayout() error: " + ex.getMessage(), ex, LayoutTemplateTool.class);
			Map<String, Object> args = new HashMap<>();
			args.put("templateName", templateName);
			args.put("layoutKey", layoutKey);
			args.put("argumentsJson", argumentsJson);
			return DataClassSerializer.errorToJson(
				"Error creating layout: " + ex.getMessage(),
				LayoutTemplateTool.class,
				"createLayout()",
				args);
		}
	}

}
