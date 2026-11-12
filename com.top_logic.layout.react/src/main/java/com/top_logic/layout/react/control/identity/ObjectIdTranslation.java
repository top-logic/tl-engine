/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.identity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import com.top_logic.layout.react.control.ReactControl;
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
 *
 * <p>
 * The ids of a property belong to the target control of the command, or to the control another
 * argument names (see {@link DisplayedObjectId#control()}). Such a control reference is recorded
 * as the semantic address of the control and replayed as its id.
 * </p>
 */
public final class ObjectIdTranslation {

	/**
	 * The {@link DisplayedObjectId} properties whose ids belong to one control.
	 *
	 * @param control
	 *        The property naming the control, {@code null} for the target control of the command.
	 * @param ids
	 *        The properties holding ids of objects the control displays.
	 */
	private record IdGroup(PropertyDescriptor control, List<PropertyDescriptor> ids) {
		// Pure data.
	}

	/** The properties marked {@link DisplayedObjectId} per argument interface. */
	private static final Map<ConfigurationDescriptor, List<PropertyDescriptor>> ID_PROPERTIES =
		new ConcurrentHashMap<>();

	/** The {@link #ID_PROPERTIES} grouped by the control their ids belong to. */
	private static final Map<ConfigurationDescriptor, List<IdGroup>> ID_GROUPS = new ConcurrentHashMap<>();

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

	private static List<IdGroup> idGroups(ConfigurationDescriptor descriptor) {
		return ID_GROUPS.computeIfAbsent(descriptor, ObjectIdTranslation::findIdGroups);
	}

	private static List<IdGroup> findIdGroups(ConfigurationDescriptor descriptor) {
		Map<String, List<PropertyDescriptor>> byControl = new LinkedHashMap<>();
		for (PropertyDescriptor property : idProperties(descriptor)) {
			String control = property.getAnnotation(DisplayedObjectId.class).control();
			byControl.computeIfAbsent(control, x -> new ArrayList<>()).add(property);
		}
		List<IdGroup> result = new ArrayList<>(byControl.size());
		for (Map.Entry<String, List<PropertyDescriptor>> entry : byControl.entrySet()) {
			String control = entry.getKey();
			PropertyDescriptor controlProperty = null;
			if (!control.isEmpty()) {
				controlProperty = descriptor.getProperty(control);
				if (controlProperty == null || controlProperty.getType() != String.class) {
					throw new IllegalArgumentException("The control of the object ids " + entry.getValue()
						+ " of '" + descriptor.getConfigurationInterface().getName() + "' is no string property '"
						+ control + "', see " + DisplayedObjectId.class.getName() + ".");
				}
			}
			result.add(new IdGroup(controlProperty, List.copyOf(entry.getValue())));
		}
		return List.copyOf(result);
	}

	/**
	 * Replaces the object ids in the given command by the keys of their objects, and the references
	 * of the controls displaying them by their addresses, in place - the replay-stable form of a
	 * recorded step.
	 *
	 * @param command
	 *        The command as dispatched to the target control.
	 * @param target
	 *        The control the command is dispatched to.
	 * @param controls
	 *        The controls of the target's window, for ids that belong to another control.
	 */
	public static void idsToKeys(ReactCommand command, ReactControl target, ControlReferences controls) {
		for (IdGroup group : idGroups(command.descriptor())) {
			ObjectIdentities identities;
			if (group.control() == null) {
				identities = target.objectIdentities();
			} else {
				ReactControl control = referencedControl(command, group.control(), controls);
				if (control == null) {
					// No control of the window: the ids stay as they were sent.
					continue;
				}
				identities = control.objectIdentities();
				String address = controls.addressOf(control);
				if (address != null) {
					command.update(group.control(), address);
				}
			}
			if (identities == null) {
				continue;
			}
			for (PropertyDescriptor property : group.ids()) {
				Object value = command.value(property);
				if (value != null) {
					command.update(property, map(value, id -> keyOrId(identities, id)));
				}
			}
		}
	}

	private static ReactControl referencedControl(ReactCommand command, PropertyDescriptor property,
			ControlReferences controls) {
		String reference = (String) command.value(property);
		if (reference == null || reference.isEmpty()) {
			return null;
		}
		return controls.controlOf(reference);
	}

	private static String keyOrId(ObjectIdentities identities, String id) {
		String key = ScriptingModelKey.toJson(identities.keyOf(id));
		return key != null ? key : id;
	}

	/**
	 * The arguments to dispatch a recorded step with: its {@link ReactCommands#arguments(ReactCommand)
	 * arguments}, the keys in its {@link DisplayedObjectId} properties resolved to the ids the
	 * objects have now, and the addresses of the controls displaying them resolved to the ids of
	 * the controls.
	 *
	 * @param step
	 *        The recorded step.
	 * @param target
	 *        The control the step is dispatched to.
	 * @param controls
	 *        The controls of the target's window, for keys of objects another control displays.
	 * @return The arguments, never {@code null}.
	 * @throws TopLogicException
	 *         If a key designates no object the control displaying it has at hand.
	 */
	public static Map<String, Object> keysToIds(ReactCommand step, ReactControl target, ControlReferences controls) {
		List<IdGroup> groups = idGroups(step.descriptor());
		if (groups.isEmpty()) {
			return ReactCommands.arguments(step);
		}
		ReactCommand resolved = TypedConfiguration.copy(step);
		for (IdGroup group : groups) {
			ObjectIdentities identities;
			if (group.control() == null) {
				identities = target.objectIdentities();
			} else {
				ReactControl control = referencedControl(resolved, group.control(), controls);
				if (control == null) {
					// No control of the window: the values stay as they were recorded.
					continue;
				}
				resolved.update(group.control(), control.getID());
				identities = control.objectIdentities();
			}
			if (identities == null) {
				continue;
			}
			for (PropertyDescriptor property : group.ids()) {
				Object value = resolved.value(property);
				if (value != null) {
					resolved.update(property, map(value, recorded -> idOf(identities, recorded)));
				}
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
