/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.io.StringWriter;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.config.json.JsonConfigurationWriter;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;

/**
 * Builds a stable, locale- and session-independent identity for a business object, for use as a
 * headless agent address/argument key.
 *
 * <p>
 * The identity is a {@link ModelName} — the same object-naming the script recorder uses to refer to
 * a model object across runs — serialized as JSON via {@link JsonConfigurationWriter}. Unlike a
 * positional index or a session-allocated id, this key designates the actual business object, so it
 * survives sorting, filtering, re-rendering and replay in a different session.
 * </p>
 */
public final class AgentModelKey {

	private AgentModelKey() {
		// Static utility.
	}

	/**
	 * The JSON-serialized {@link ModelName} of the given model object, or {@code null} if the object
	 * cannot be named (e.g. has no applicable naming scheme).
	 *
	 * <p>
	 * Best-effort: any failure to name or serialize yields {@code null} rather than an error, so the
	 * projection degrades to no key for that node.
	 * </p>
	 *
	 * @param model
	 *        The business object to identify.
	 * @return The key JSON, or {@code null}.
	 */
	public static String toJson(Object model) {
		return toJson(null, model);
	}

	/**
	 * The JSON-serialized {@link ModelName} of the given model object built relative to a value
	 * context, or {@code null} if it cannot be named.
	 *
	 * <p>
	 * The value context selects a context-relative naming scheme (e.g. naming a select option by its
	 * label within the control's {@link com.top_logic.layout.react.scripting.ReactOptionScope option
	 * scope}), giving an identity that need only be unique within that context. Pass {@code null} for
	 * a global name.
	 * </p>
	 *
	 * @param valueContext
	 *        The context object the name is built relative to, or {@code null} for a global name.
	 * @param model
	 *        The business object to identify.
	 * @return The key JSON, or {@code null}.
	 */
	public static String toJson(Object valueContext, Object model) {
		if (model == null) {
			return null;
		}
		Maybe<? extends ModelName> name;
		try {
			name = valueContext == null
				? ModelResolver.buildModelNameIfAvailable(model)
				: ModelResolver.buildModelNameIfAvailable(valueContext, model);
		} catch (Throwable ex) {
			return null;
		}
		if (!name.hasValue()) {
			return null;
		}
		try {
			StringWriter buffer = new StringWriter();
			JsonWriter json = new JsonWriter(buffer);
			// Write with the polymorphic ModelName static type so the concrete scheme's name type is
			// encoded (as the array type tag). Without it the key would be a bare property object that
			// {@link #fromJson(String)} could not reconstruct the scheme for.
			new JsonConfigurationWriter(json).write(ModelName.class, name.get());
			json.flush();
			return buffer.toString();
		} catch (Throwable ex) {
			return null;
		}
	}

	/**
	 * The {@link ModelName} parsed back from a key produced by {@link #toJson(Object, Object)}, or
	 * {@code null} if it cannot be parsed.
	 *
	 * <p>
	 * Inverse of the build direction: hand the result to {@link ModelResolver#locateModel(
	 * com.top_logic.layout.scripting.runtime.ActionContext, Object, ModelName)} together with the same
	 * kind of value context the key was built against to recover the business object.
	 * </p>
	 *
	 * @param json
	 *        A key JSON as returned by {@link #toJson(Object, Object)}.
	 * @return The reconstructed {@link ModelName}, or {@code null}.
	 */
	public static ModelName fromJson(String json) {
		if (json == null || json.isEmpty()) {
			return null;
		}
		try {
			ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(ModelName.class);
			JsonConfigurationReader reader =
				new JsonConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, descriptor);
			reader.setSource(CharacterContents.newContent(json));
			return (ModelName) reader.read();
		} catch (Throwable ex) {
			Logger.warn("Cannot parse agent model key: " + json, ex, AgentModelKey.class);
			return null;
		}
	}
}
