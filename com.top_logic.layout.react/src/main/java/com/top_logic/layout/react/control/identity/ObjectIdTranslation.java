/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.identity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.json.JsonBinding;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactCommands;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.util.error.TopLogicException;

/**
 * Translates the {@link DisplayedObjectId object ids} in the arguments of a {@link ReactCommand}
 * into business keys and back.
 *
 * <p>
 * A recorded step carries, in each property marked {@link DisplayedObjectId}, the key of the
 * object in place of its id: the {@link ModelName} serialized as JSON, see
 * {@link ScriptingModelKey#toJson(ModelName)}. An id whose object has no key stays as it is, so the
 * step replays in the session it was recorded in only.
 * </p>
 */
public final class ObjectIdTranslation {

	/** The properties marked {@link DisplayedObjectId} per argument interface. */
	private static final Map<ConfigurationDescriptor, List<PropertyDescriptor>> ID_PROPERTIES =
		new ConcurrentHashMap<>();

	private ObjectIdTranslation() {
		// Static utility.
	}

	/**
	 * The properties of the given argument interface marked {@link DisplayedObjectId}.
	 */
	public static List<PropertyDescriptor> idProperties(ConfigurationDescriptor descriptor) {
		return ID_PROPERTIES.computeIfAbsent(descriptor, ObjectIdTranslation::findIdProperties);
	}

	private static List<PropertyDescriptor> findIdProperties(ConfigurationDescriptor descriptor) {
		List<PropertyDescriptor> result = new ArrayList<>();
		for (PropertyDescriptor property : descriptor.getProperties()) {
			if (property.getAnnotation(DisplayedObjectId.class) != null) {
				if (List.class.isAssignableFrom(property.getType())
					&& property.getAnnotation(JsonBinding.class) == null) {
					throw new IllegalArgumentException("The list of object ids '" + property.getPropertyName()
						+ "' of '" + descriptor.getConfigurationInterface().getName()
						+ "' is not bound as a JSON array, see " + DisplayedObjectId.class.getName() + ".");
				}
				result.add(property);
			}
		}
		return List.copyOf(result);
	}

	/**
	 * Replaces the object ids in the given command by the keys of their objects, in place - the
	 * replay-stable form of a recorded step.
	 *
	 * @param command
	 *        The command as dispatched to the control.
	 * @param identities
	 *        The {@link com.top_logic.layout.react.control.ScriptingControl#objectIdentities() object
	 *        identities} of the target control; {@code null} leaves the command unchanged.
	 */
	public static void idsToKeys(ReactCommand command, ObjectIdentities identities) {
		if (identities == null) {
			return;
		}
		for (PropertyDescriptor property : idProperties(command.descriptor())) {
			Object value = command.value(property);
			if (value != null) {
				command.update(property, map(value, id -> keyOrId(identities, id)));
			}
		}
	}

	private static String keyOrId(ObjectIdentities identities, String id) {
		String key = ScriptingModelKey.toJson(identities.keyOf(id));
		return key != null ? key : id;
	}

	/**
	 * The arguments to dispatch a recorded step with: its {@link ReactCommands#arguments(ReactCommand)
	 * arguments}, the keys in its {@link DisplayedObjectId} properties resolved to the ids the
	 * objects have now.
	 *
	 * @param step
	 *        The recorded step.
	 * @param identities
	 *        The {@link com.top_logic.layout.react.control.ScriptingControl#objectIdentities() object
	 *        identities} of the target control; {@code null} for the arguments as recorded.
	 * @return The arguments, never {@code null}.
	 * @throws TopLogicException
	 *         If a key designates no object the control has at hand.
	 */
	public static Map<String, Object> keysToIds(ReactCommand step, ObjectIdentities identities) {
		List<PropertyDescriptor> properties = idProperties(step.descriptor());
		if (identities == null || properties.isEmpty()) {
			return ReactCommands.arguments(step);
		}
		ReactCommand resolved = TypedConfiguration.copy(step);
		for (PropertyDescriptor property : properties) {
			Object value = resolved.value(property);
			if (value != null) {
				resolved.update(property, map(value, recorded -> idOf(identities, recorded)));
			}
		}
		return ReactCommands.arguments(resolved);
	}

	private static String idOf(ObjectIdentities identities, String recorded) {
		ModelName key = parseKey(recorded);
		if (key == null) {
			// An id recorded as it was dispatched.
			return recorded;
		}
		String id = identities.idOfKey(key);
		if (id == null) {
			throw new TopLogicException(I18NConstants.ERROR_OBJECT_NOT_DISPLAYED__KEY.fill(key));
		}
		return id;
	}

	/**
	 * The given command with the keys in its {@link DisplayedObjectId} properties replaced by their
	 * labels, for the description of a recorded step.
	 *
	 * @param command
	 *        The recorded step, not modified.
	 * @param labels
	 *        Renders a key.
	 * @return A copy with labels in place of keys, the command itself if it has no
	 *         {@link DisplayedObjectId} properties.
	 */
	public static ReactCommand withKeyLabels(ReactCommand command, Function<ModelName, String> labels) {
		List<PropertyDescriptor> properties = idProperties(command.descriptor());
		if (properties.isEmpty()) {
			return command;
		}
		ReactCommand result = TypedConfiguration.copy(command);
		for (PropertyDescriptor property : properties) {
			Object value = result.value(property);
			if (value != null) {
				result.update(property, map(value, recorded -> {
					ModelName key = parseKey(recorded);
					return key == null ? recorded : labels.apply(key);
				}));
			}
		}
		return result;
	}

	/**
	 * The {@link ModelName} a recorded {@link DisplayedObjectId} value holds, {@code null} if it holds
	 * an id.
	 */
	public static ModelName parseKey(String recorded) {
		if (recorded == null || recorded.isEmpty()) {
			return null;
		}
		char first = recorded.charAt(0);
		if (first != '[' && first != '{') {
			// A key is a JSON array or object, an id is neither.
			return null;
		}
		return ScriptingModelKey.fromJson(recorded);
	}

	/**
	 * Applies the given function to a {@link DisplayedObjectId} value: a single value or each
	 * element of a list of values.
	 */
	private static Object map(Object value, UnaryOperator<String> function) {
		if (value instanceof String single) {
			return function.apply(single);
		}
		if (value instanceof List<?> list) {
			List<Object> result = new ArrayList<>(list.size());
			for (Object element : list) {
				result.add(element instanceof String single ? function.apply(single) : element);
			}
			return result;
		}
		return value;
	}

}
