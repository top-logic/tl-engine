/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.model;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.ai.service.tools.ToolCallTracking;
import com.top_logic.ai.service.tools.model.messages.AnnotationEditorResponse;
import com.top_logic.ai.service.tools.util.DataClassSerializer;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.config.json.JsonConfigurationWriter;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.util.model.ModelService;

import de.haumacher.msgbuf.io.StringW;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool for setting and removing annotations on TopLogic model elements.
 *
 * <p>
 * This tool allows AI agents to modify annotation metadata on model elements (modules, types,
 * parts, classifiers). Annotation configurations are accepted as JSON matching the schema from
 * {@link com.top_logic.ai.service.tools.config.ConfigSchemaTool}.
 * </p>
 */
public class ModelAnnotationEditorTool {

	/**
	 * Configuration for {@link ModelAnnotationEditorTool}.
	 */
	public interface Config extends PolymorphicConfiguration<ModelAnnotationEditorTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link ModelAnnotationEditorTool}.
	 */
	public ModelAnnotationEditorTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link ModelAnnotationEditorTool} from configuration.
	 */
	@CalledByReflection
	public ModelAnnotationEditorTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	/**
	 * Sets or replaces an annotation on a model element.
	 *
	 * <p>
	 * The annotation configuration JSON must match the schema for the given configuration
	 * interface. Use {@code getConfigSchema} with a {@code toplogic://schema/config/...} URI
	 * to obtain the schema, and {@code describeAnnotation} to explore available properties.
	 * </p>
	 *
	 * @param targetRef
	 *        Target in format {@code "module"}, {@code "module:Type"}, or
	 *        {@code "module:Type#part"}.
	 * @param configInterfaceName
	 *        Fully-qualified annotation configuration interface class name (e.g.,
	 *        {@code "com.top_logic.model.annotate.TLVisibility"}).
	 * @param configJson
	 *        Annotation configuration as JSON matching the schema.
	 * @return JSON with operation result and the annotation's canonical JSON representation.
	 */
	@Tool("Add or replace an annotation on a model element (module, type, part, classifier). Accepts annotation config as JSON. Use listAvailableAnnotations to discover options and getConfigSchema to understand the structure first.")
	public String addAnnotation(
			@P("Target: 'module', 'module:Type', or 'module:Type#part'") String targetRef,
			@P("Fully-qualified annotation config interface class name") String configInterfaceName,
			@P("Annotation configuration as JSON matching the schema from getConfigSchema") String configJson) {

		try {
			String result = ThreadContextManager.inSystemInteraction(
				ModelAnnotationEditorTool.class,
				() -> doSetAnnotation(targetRef, configInterfaceName, configJson));

			Map<String, Object> args = buildArgs(targetRef, configInterfaceName);
			ToolCallTracking.recordIfTracking("addAnnotation", args, result);
			Logger.info(ToolCallTracking.formatToolCall("addAnnotation", args, result),
				ModelAnnotationEditorTool.class);
			return result;

		} catch (Exception ex) {
			Logger.error("addAnnotation() error: " + ex.getMessage(), ex, ModelAnnotationEditorTool.class);
			return DataClassSerializer.errorToJson(
				"Error setting annotation: " + ex.getMessage(),
				ModelAnnotationEditorTool.class, "addAnnotation()",
				buildArgs(targetRef, configInterfaceName));
		}
	}

	private String doSetAnnotation(String targetRef, String configInterfaceName, String configJson)
			throws IOException, ConfigurationException {

		// 1. Validate parameters
		if (targetRef == null || targetRef.isEmpty()) {
			throw new IllegalArgumentException("Target reference is required");
		}
		if (configInterfaceName == null || configInterfaceName.isEmpty()) {
			throw new IllegalArgumentException("Configuration interface name is required");
		}
		if (configJson == null || configJson.isEmpty()) {
			throw new IllegalArgumentException("Annotation configuration JSON is required");
		}

		// 2. Parse target reference and resolve
		TargetRefParser parser = TargetRefParser.parse(targetRef);
		TLModelPart target = parser.resolve(ModelService.getApplicationModel());
		String kind = TargetRefParser.determineKind(target, parser);

		// 3. Load annotation config interface
		Class<? extends TLAnnotation> annotationClass = resolveAnnotationClass(configInterfaceName);

		// 4. Parse JSON into ConfigurationItem
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(annotationClass);
		ConfigurationItem annotationConfig = new JsonConfigurationReader(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, descriptor)
				.schemaAware()
				.setSource(CharacterContents.newContent(configJson, "annotation-config"))
				.read();

		// 5. Set annotation inside transaction
		try (Transaction tx =
			PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Set Annotation"))) {
			target.setAnnotation((TLAnnotation) annotationConfig);
			tx.commit();
		}

		// 6. Re-read and serialize the annotation for verification
		TLAnnotation setAnnotation = target.getAnnotation(annotationClass);
		String annotationJsonStr = serializeAnnotationConfig(setAnnotation);

		// 7. Build response with inline JSON
		return buildSetResponse(targetRef, kind, configInterfaceName, annotationJsonStr);
	}

	/**
	 * Removes an annotation from a model element.
	 *
	 * @param targetRef
	 *        Target in format {@code "module"}, {@code "module:Type"}, or
	 *        {@code "module:Type#part"}.
	 * @param configInterfaceName
	 *        Fully-qualified annotation configuration interface class name to remove.
	 * @return JSON with operation result.
	 */
	@Tool("Remove an annotation from a model element. Use getCurrentAnnotations to see which annotations exist on a target.")
	public String removeAnnotation(
			@P("Target: 'module', 'module:Type', or 'module:Type#part'") String targetRef,
			@P("Fully-qualified annotation config interface class name to remove") String configInterfaceName) {

		try {
			AnnotationEditorResponse response = ThreadContextManager.inSystemInteraction(
				ModelAnnotationEditorTool.class,
				() -> doRemoveAnnotation(targetRef, configInterfaceName));

			return DataClassSerializer.toJson(response, ModelAnnotationEditorTool.class,
				"removeAnnotation()", buildArgs(targetRef, configInterfaceName));

		} catch (Exception ex) {
			Logger.error("removeAnnotation() error: " + ex.getMessage(), ex,
				ModelAnnotationEditorTool.class);
			return DataClassSerializer.errorToJson(
				"Error removing annotation: " + ex.getMessage(),
				ModelAnnotationEditorTool.class, "removeAnnotation()",
				buildArgs(targetRef, configInterfaceName));
		}
	}

	private AnnotationEditorResponse doRemoveAnnotation(String targetRef, String configInterfaceName) {
		// 1. Validate
		if (targetRef == null || targetRef.isEmpty()) {
			throw new IllegalArgumentException("Target reference is required");
		}
		if (configInterfaceName == null || configInterfaceName.isEmpty()) {
			throw new IllegalArgumentException("Configuration interface name is required");
		}

		// 2. Parse and resolve
		TargetRefParser parser = TargetRefParser.parse(targetRef);
		TLModelPart target = parser.resolve(ModelService.getApplicationModel());
		String kind = TargetRefParser.determineKind(target, parser);

		// 3. Load annotation class
		Class<? extends TLAnnotation> annotationClass = resolveAnnotationClass(configInterfaceName);

		// 4. Remove inside transaction
		try (Transaction tx =
			PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Remove Annotation"))) {
			target.removeAnnotation(annotationClass);
			tx.commit();
		}

		// 5. Build response
		AnnotationEditorResponse response = AnnotationEditorResponse.create();
		response.setSuccess(true);
		response.setTarget(targetRef);
		response.setKind(kind);
		response.setConfigInterface(configInterfaceName);
		response.setDetail("Annotation removed successfully");
		return response;
	}

	/**
	 * Loads and validates an annotation configuration interface class.
	 *
	 * @param configInterfaceName
	 *        Fully-qualified class name.
	 * @return The validated annotation class.
	 * @throws IllegalArgumentException
	 *         If the class cannot be found or is not a {@link TLAnnotation}.
	 */
	@SuppressWarnings("unchecked")
	private static Class<? extends TLAnnotation> resolveAnnotationClass(String configInterfaceName) {
		Class<?> rawClass;
		try {
			rawClass = Class.forName(configInterfaceName);
		} catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("Annotation class not found: " + configInterfaceName);
		}
		if (!TLAnnotation.class.isAssignableFrom(rawClass)) {
			throw new IllegalArgumentException(
				"Class is not a TLAnnotation: " + configInterfaceName);
		}
		return (Class<? extends TLAnnotation>) rawClass;
	}

	/**
	 * Serializes a {@link TLAnnotation}'s configuration values to JSON with schema references.
	 */
	private static String serializeAnnotationConfig(TLAnnotation annotation) throws IOException {
		StringWriter sw = new StringWriter();
		new JsonConfigurationWriter(sw).prettyPrint().schemaAware().write(annotation);
		return sw.toString();
	}

	/**
	 * Builds the addAnnotation response JSON with inline annotation config.
	 */
	private static String buildSetResponse(String targetRef, String kind,
			String configInterfaceName, String annotationJson) throws IOException {

		StringW buffer = new StringW();
		try (de.haumacher.msgbuf.json.JsonWriter json = new de.haumacher.msgbuf.json.JsonWriter(buffer)) {
			json.beginObject();

			json.name("success");
			json.value(true);
			json.name("target");
			json.value(targetRef);
			json.name("kind");
			json.value(kind);
			json.name("configInterface");
			json.value(configInterfaceName);
			json.name("detail");
			json.value("Annotation set successfully");
			json.name("configJson");
			json.jsonValue(annotationJson);

			json.endObject();
		}
		return buffer.toString();
	}

	private static Map<String, Object> buildArgs(String targetRef, String configInterfaceName) {
		Map<String, Object> args = new HashMap<>();
		args.put("targetRef", targetRef);
		args.put("configInterfaceName", configInterfaceName);
		return args;
	}
}
