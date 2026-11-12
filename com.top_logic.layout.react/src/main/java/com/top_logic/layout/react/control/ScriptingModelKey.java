/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.io.StringWriter;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.config.json.JsonConfigurationWriter;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.identity.ObjectIdentities;
import com.top_logic.layout.react.control.identity.ProjectionMode;
import com.top_logic.layout.react.scripting.ReactActionContext;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Builds a stable, locale- and session-independent identity for a business object, for use as a
 * headless interface address/argument key.
 *
 * <p>
 * The identity is a {@link ModelName} — the same object-naming the script recorder uses to refer to
 * a model object across runs — serialized as JSON via {@link JsonConfigurationWriter}. Unlike a
 * positional index or a session-allocated id, this key designates the actual business object, so it
 * survives sorting, filtering, re-rendering and replay in a different session.
 * </p>
 */
public final class ScriptingModelKey {

	/**
	 * Property of a projected entry holding the business identity of its object, see
	 * {@link #putIdentity(Map, ProjectionMode, ObjectIdentities, Object, Object, String, Object)}.
	 */
	public static final String KEY = "key";

	/**
	 * Property of a projected entry holding the session-bound id of its object: the id the commands
	 * of the control take the object by, see
	 * {@link #putIdentity(Map, ProjectionMode, ObjectIdentities, Object, Object, String, Object)}.
	 *
	 * <p>
	 * Every control projects the id of an object under this one name - a table row, a tree node, a
	 * card, an option -, whatever property its client state carries the id in.
	 * </p>
	 */
	public static final String ID = "id";

	/**
	 * Property of a projected entry holding the label of the object the entry identifies by its id,
	 * see {@link #putIdentity(Map, ProjectionMode, ObjectIdentities, Object, Object, String, Object)}.
	 */
	public static final String LABEL = "label";

	private ScriptingModelKey() {
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
		return toJson(name(valueContext, model));
	}

	/**
	 * The {@link ModelName} of the given model object built relative to a value context, or
	 * {@code null} if the object cannot be named (e.g. has no applicable naming scheme).
	 *
	 * <p>
	 * Best-effort: any failure to name yields {@code null} rather than an error. See
	 * {@link #toJson(Object, Object)} for the value-context semantics.
	 * </p>
	 *
	 * @param valueContext
	 *        The context object the name is built relative to, or {@code null} for a global name.
	 * @param model
	 *        The business object to identify.
	 * @return The name, or {@code null}.
	 */
	public static ModelName name(Object valueContext, Object model) {
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
		return name.hasValue() ? name.get() : null;
	}

	/**
	 * The identity of the given model object as a JSON <em>value</em> (the parsed form of
	 * {@link #toJson(Object, Object)}), or {@code null} if the object cannot be named.
	 *
	 * <p>
	 * This is the {@link #KEY} the key projection puts on rows and options: the config-JSON value of
	 * the object's {@link ModelName}.
	 * </p>
	 *
	 * @param valueContext
	 *        The context object the name is built relative to, or {@code null} for a global name.
	 * @param model
	 *        The business object to identify.
	 * @return The key JSON value, or {@code null}.
	 */
	public static Object toKey(Object valueContext, Object model) {
		String json = toJson(valueContext, model);
		if (json == null) {
			return null;
		}
		try {
			return JSON.fromString(json);
		} catch (JSON.ParseException ex) {
			return null;
		}
	}

	/**
	 * Puts the identity of the given object into a part of the headless projection.
	 *
	 * <p>
	 * In the {@link ProjectionMode#KEY key form}, the object is projected by its
	 * {@link #toKey(Object, Object) business identity} under {@link #KEY}, or by the
	 * given session-bound id under {@link #ID} if it has none: the projection is what an
	 * assertion of a recorded script compares, in whatever session it runs. Where the
	 * {@link ProjectionMode#projectsIds(ObjectIdentities) projection shows ids}, the object is
	 * projected by its id under {@link #ID}, with its label under {@link #LABEL} unless the entry
	 * has a label of its own - the id the commands of the control take it by.
	 * </p>
	 *
	 * <p>
	 * The property the entry carries the id in for the client is dropped, so that an object is
	 * identified the same way in the projection of every control.
	 * </p>
	 *
	 * @param projection
	 *        The projected entry of the object (an option, a node).
	 * @param mode
	 *        How the projection identifies objects.
	 * @param identities
	 *        The {@link ScriptingControl#objectIdentities() object identities} of the control,
	 *        {@code null} if it offers none.
	 * @param valueContext
	 *        The context object the name is built relative to, or {@code null} for a global name.
	 * @param model
	 *        The object to identify.
	 * @param idProperty
	 *        The property the entry carries the session-bound id in for the client, dropped from
	 *        the projection.
	 * @param id
	 *        The id the client addresses the object by in this session.
	 */
	public static void putIdentity(Map<String, Object> projection, ProjectionMode mode, ObjectIdentities identities,
			Object valueContext, Object model, String idProperty, Object id) {
		projection.remove(idProperty);
		if (mode.projectsIds(identities)) {
			projection.remove(KEY);
			projection.put(ID, id);
			if (!projection.containsKey(LABEL)) {
				String label = label(model);
				if (label != null) {
					projection.put(LABEL, label);
				}
			}
			return;
		}
		Object key = toKey(valueContext, model);
		if (key != null) {
			projection.put(KEY, key);
		} else {
			projection.put(ID, id);
		}
	}

	private static String label(Object model) {
		if (model == null) {
			return null;
		}
		try {
			return MetaLabelProvider.INSTANCE.getLabel(model);
		} catch (RuntimeException ex) {
			return null;
		}
	}

	/**
	 * The identity of the given object in the headless projection: its
	 * {@link #toKey(Object, Object) business identity}, or the given session-bound id if it has none.
	 *
	 * @see #putIdentity(Map, ProjectionMode, ObjectIdentities, Object, Object, String, Object)
	 */
	public static Object identity(Object valueContext, Object model, Object id) {
		Object key = toKey(valueContext, model);
		return key != null ? key : id;
	}

	/**
	 * The given {@link ModelName} serialized as JSON, or {@code null} if it cannot be serialized (or
	 * {@code name} is {@code null}).
	 *
	 * @param name
	 *        The name to serialize.
	 * @return The key JSON, or {@code null}.
	 */
	public static String toJson(ModelName name) {
		if (name == null) {
			return null;
		}
		try {
			StringWriter buffer = new StringWriter();
			JsonWriter json = new JsonWriter(buffer);
			// Write with the polymorphic ModelName static type so the concrete scheme's name type is
			// encoded (as the array type tag). Without it the key would be a bare property object that
			// {@link #fromJson(String)} could not reconstruct the scheme for.
			new JsonConfigurationWriter(json).write(ModelName.class, name);
			json.flush();
			return buffer.toString();
		} catch (Throwable ex) {
			return null;
		}
	}

	/**
	 * An {@link ActionContext} of the running interaction for resolving a {@link ModelName}.
	 *
	 * @throws RuntimeException
	 *         if there is no running interaction offering a display context to build one from.
	 */
	private static ActionContext newActionContext() {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		return new ReactActionContext(displayContext, displayContext.asRequest().getSession());
	}

	/**
	 * An {@link ActionContext} of the running interaction for resolving a {@link ModelName}, or
	 * {@code null} if there is no running interaction to build one from.
	 *
	 * @see #newActionContext()
	 */
	private static ActionContext newActionContextOrNull() {
		try {
			return newActionContext();
		} catch (RuntimeException ex) {
			Logger.warn("Cannot resolve a business identity outside an interaction.", ex, ScriptingModelKey.class);
			return null;
		}
	}

	/**
	 * The object the given {@link ModelName} designates, or {@code null} if it designates none.
	 *
	 * <p>
	 * Inverse of {@link #name(Object, Object)}. Best-effort: a name that cannot be resolved, and a
	 * missing {@code context}, yield {@code null} rather than an error.
	 * </p>
	 *
	 * @param context
	 *        The action context to resolve the name in, or {@code null} if there is none.
	 * @param valueContext
	 *        The context object the name was built relative to, or {@code null} for a global name.
	 * @param name
	 *        The name to resolve.
	 * @return The designated object, or {@code null}.
	 */
	private static Object locate(ActionContext context, Object valueContext, ModelName name) {
		if (context == null || name == null) {
			return null;
		}
		try {
			return ModelResolver.locateModel(context, valueContext, name);
		} catch (RuntimeException ex) {
			Logger.warn("Cannot resolve object for key: " + name, ex, ScriptingModelKey.class);
			return null;
		}
	}

	/**
	 * The object the given {@link ModelName} designates within the running interaction, or
	 * {@code null} if it designates none (or there is no running interaction).
	 *
	 * @param valueContext
	 *        The context object the name was built relative to, or {@code null} for a global name.
	 * @param name
	 *        The name to resolve.
	 * @return The designated object, or {@code null}.
	 *
	 * @see #locate(ActionContext, Object, ModelName)
	 */
	public static Object locate(Object valueContext, ModelName name) {
		if (name == null) {
			return null;
		}
		return locate(newActionContextOrNull(), valueContext, name);
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
			Logger.warn("Cannot parse agent model key: " + json, ex, ScriptingModelKey.class);
			return null;
		}
	}
}
