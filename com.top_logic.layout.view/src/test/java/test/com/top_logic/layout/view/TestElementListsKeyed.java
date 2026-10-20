/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import java.lang.reflect.Constructor;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeDescriptor;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;

/**
 * Audit that a same-path overlay of a view can descend into every list of the element tree.
 *
 * <p>
 * An overlay addresses an entry of a list by its key. The test walks the configuration types of a
 * view, starting from {@link ViewElement.Config} and every specialization of
 * {@link UIElement.Config}, and checks:
 * </p>
 * <ul>
 * <li>A list whose entries have an {@link UIElement.Config#ID id} property is keyed by it, or by
 * the kind of its entries ({@link ConfigurationItem#CONFIGURATION_INTERFACE_NAME}) for a list that
 * holds at most one entry per kind.</li>
 * <li>A list whose entry type has no id, while some specialization of it has one, is reported: the
 * entry type must declare the id for the list to be keyed.</li>
 * <li>Every concrete {@link UIElement} is configured by a specialization of
 * {@link UIElement.Config}, since only such a configuration can be an entry of the element
 * lists.</li>
 * </ul>
 */
public class TestElementListsKeyed extends TestCase {

	/**
	 * Every list of the element tree whose entries carry an id is keyed by that id.
	 */
	public void testListsKeyedById() {
		Set<String> problems = new TreeSet<>();

		Deque<Class<?>> pending = new ArrayDeque<>();
		Set<Class<?>> visited = new HashSet<>();
		pending.add(ViewElement.Config.class);
		pending.add(UIElement.Config.class);
		pending.addAll(specializations(UIElement.Config.class));

		while (!pending.isEmpty()) {
			Class<?> configInterface = pending.removeFirst();
			if (!visited.add(configInterface)) {
				continue;
			}
			ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(configInterface);
			for (PropertyDescriptor property : descriptor.getProperties()) {
				ConfigurationDescriptor valueDescriptor = property.getValueDescriptor();
				if (valueDescriptor == null) {
					continue;
				}
				Class<?> entryType = valueDescriptor.getConfigurationInterface();
				pending.add(entryType);
				Collection<Class<?>> entrySpecializations = entryTypes(property, entryType);
				pending.addAll(entrySpecializations);

				if (property.kind() != PropertyKind.LIST) {
					continue;
				}
				String name = property.getDescriptor().getConfigurationInterface().getName() + "#"
					+ property.getPropertyName();
				if (hasId(valueDescriptor)) {
					PropertyDescriptor key = property.getKeyProperty();
					if (key == null) {
						problems.add(name + ": entries have an id, but the list is not keyed.");
					} else if (!UIElement.Config.ID.equals(key.getPropertyName())
						&& !ConfigurationItem.CONFIGURATION_INTERFACE_NAME.equals(key.getPropertyName())) {
						// A list keyed by the kind of its entries holds at most one entry per kind,
						// which an overlay addresses by its kind.
						problems.add(name + ": entries have an id, but the list is keyed by '"
							+ key.getPropertyName() + "'.");
					}
				} else if (property.getKeyProperty() == null) {
					List<String> withId = new ArrayList<>();
					for (Class<?> specialization : entrySpecializations) {
						if (hasId(TypedConfiguration.getConfigurationDescriptor(specialization))) {
							withId.add(specialization.getName());
						}
					}
					if (!withId.isEmpty()) {
						problems.add(name + ": entry type " + entryType.getName()
							+ " has no id, but its specializations " + withId + " have one.");
					}
				}
			}
		}

		assertTrue("Element lists not addressable by an overlay:\n" + String.join("\n", problems),
			problems.isEmpty());
	}

	/**
	 * Every concrete {@link UIElement} is configured by a specialization of {@link UIElement.Config}.
	 */
	public void testElementsConfiguredByElementConfig() throws ClassNotFoundException {
		Set<String> problems = new TreeSet<>();
		for (Class<?> element : concreteElements()) {
			List<Class<?>> configTypes = new ArrayList<>();
			for (Constructor<?> constructor : element.getConstructors()) {
				for (Class<?> parameterType : constructor.getParameterTypes()) {
					if (ConfigurationItem.class.isAssignableFrom(parameterType)) {
						configTypes.add(parameterType);
					}
				}
			}
			if (configTypes.isEmpty()) {
				problems.add(element.getName() + ": no public configuration constructor.");
			}
			for (Class<?> configType : configTypes) {
				if (!UIElement.Config.class.isAssignableFrom(configType)) {
					problems.add(element.getName() + ": configured by " + configType.getName()
						+ ", not by a specialization of " + UIElement.Config.class.getName() + ".");
				}
			}
		}
		assertTrue("Elements that cannot be entries of an element list:\n" + String.join("\n", problems),
			problems.isEmpty());
	}

	/**
	 * The public concrete implementations of {@link UIElement}, loaded without initializing them.
	 */
	private static List<Class<?>> concreteElements() throws ClassNotFoundException {
		ClassLoader loader = TestElementListsKeyed.class.getClassLoader();
		List<Class<?>> result = new ArrayList<>();
		Set<TypeDescriptor> seen = new HashSet<>();
		Deque<TypeDescriptor> pending = new ArrayDeque<>();
		pending.add(TypeIndex.getInstance().getDescriptor(UIElement.class));
		while (!pending.isEmpty()) {
			TypeDescriptor type = pending.removeFirst();
			if (!seen.add(type)) {
				continue;
			}
			pending.addAll(type.getSpecializations());
			if (type.isPublic() && !type.isInterface() && !type.isAbstract()) {
				result.add(Class.forName(type.getClassName(), false, loader));
			}
		}
		assertFalse("No element implementations found.", result.isEmpty());
		return result;
	}

	private static boolean hasId(ConfigurationDescriptor descriptor) {
		PropertyDescriptor id = descriptor.getProperty(UIElement.Config.ID);
		return id != null && id.getType() == String.class;
	}

	/**
	 * The configuration interfaces an entry of the given property can have, beside its declared
	 * entry type.
	 *
	 * <p>
	 * An entry declared as plain {@link PolymorphicConfiguration} is configured by the
	 * configuration interfaces of the implementations of the property's instance type.
	 * </p>
	 */
	private static Collection<Class<?>> entryTypes(PropertyDescriptor property, Class<?> entryType) {
		if (entryType == PolymorphicConfiguration.class && property.getInstanceType() != null) {
			List<Class<?>> result = new ArrayList<>();
			for (Class<?> config : TypeIndex.getInstance().implTagNames(property.getInstanceType()).values()) {
				if (!config.getName().startsWith("test.")) {
					result.add(config);
				}
			}
			return result;
		}
		return specializations(entryType);
	}

	/**
	 * The configuration interfaces specializing the given one, except test fixtures.
	 *
	 * <p>
	 * The generic roots {@link ConfigurationItem} and {@link PolymorphicConfiguration} are not
	 * expanded: a property of such a type accepts any configuration, which is not part of the
	 * element tree.
	 * </p>
	 */
	private static Collection<Class<?>> specializations(Class<?> configInterface) {
		if (!ConfigurationItem.class.isAssignableFrom(configInterface)
			|| configInterface == ConfigurationItem.class
			|| configInterface == PolymorphicConfiguration.class) {
			return List.of();
		}
		List<Class<?>> result = new ArrayList<>();
		for (Class<?> specialization : TypeIndex.getInstance().getSpecializations(configInterface, true, true, true)) {
			if (!specialization.getName().startsWith("test.")) {
				result.add(specialization);
			}
		}
		return result;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which enumerates the configuration types.
	 */
	public static Test suite() throws ModuleException {
		return ServiceTestSetup.createSetup(TestElementListsKeyed.class, TypeIndex.Module.INSTANCE);
	}

}
