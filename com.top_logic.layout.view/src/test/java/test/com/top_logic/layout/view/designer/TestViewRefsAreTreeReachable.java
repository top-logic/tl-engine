/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.designer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
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
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.reflect.TypeDescriptor;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewRef;
import com.top_logic.layout.view.designer.DesignTreeBuilder;

/**
 * Tests that every {@link ViewRef} property of a UI element configuration is reached by the
 * {@link DesignTreeBuilder}.
 *
 * <p>
 * The design tree descends only {@link TreeProperty} properties. A view reference in a
 * configuration that is reached through a structural property without that annotation (e.g. a
 * command in the action list of another command) would therefore be missing from the design tree.
 * The test collects every configuration type that can occur below a {@link UIElement.Config} and
 * reports every structural property through which a configuration with a {@link ViewRef} property
 * can be reached, but which is not a {@link TreeProperty}.
 * </p>
 */
public class TestViewRefsAreTreeReachable extends TestCase {

	private final Map<Class<?>, Set<Class<?>>> _interfaceSpecializations = new HashMap<>();

	private final Map<Class<?>, Set<Class<?>>> _implementationConfigs = new HashMap<>();

	/**
	 * Every property on a path from a UI element to a view reference is a {@link TreeProperty}.
	 */
	public void testPathsToViewRefsAreTreeProperties() {
		Set<Class<?>> types = reachableConfigTypes();
		Set<Class<?>> referencing = referencingTypes(types);

		Set<String> missing = new TreeSet<>();
		for (Class<?> type : types) {
			for (PropertyDescriptor property : descriptor(type).getProperties()) {
				if (isTreeProperty(property)) {
					continue;
				}
				if (elementTypes(property).stream().anyMatch(referencing::contains)) {
					missing.add(declaringName(property));
				}
			}
		}
		assertEquals("Structural properties leading to a view reference must be tree properties.",
			Set.of(), missing);
	}

	/**
	 * All configuration types that can occur in a UI element configuration.
	 */
	private Set<Class<?>> reachableConfigTypes() {
		Set<Class<?>> result = new LinkedHashSet<>();
		Deque<Class<?>> todo = new ArrayDeque<>(interfaceSpecializations(UIElement.Config.class));
		while (!todo.isEmpty()) {
			Class<?> type = todo.removeFirst();
			if (!ConfigurationItem.class.isAssignableFrom(type) || descriptor(type) == null || !result.add(type)) {
				continue;
			}
			for (PropertyDescriptor property : descriptor(type).getProperties()) {
				todo.addAll(elementTypes(property));
			}
		}
		return result;
	}

	/**
	 * The types among the given ones from which a {@link ViewRef} property can be reached.
	 */
	private Set<Class<?>> referencingTypes(Set<Class<?>> types) {
		Set<Class<?>> result = new HashSet<>();
		boolean changed = true;
		while (changed) {
			changed = false;
			for (Class<?> type : types) {
				if (!result.contains(type) && leadsToViewRef(type, result)) {
					result.add(type);
					changed = true;
				}
			}
		}
		return result;
	}

	private boolean leadsToViewRef(Class<?> type, Set<Class<?>> referencing) {
		for (PropertyDescriptor property : descriptor(type).getProperties()) {
			if (property.getType() == ViewRef.class) {
				return true;
			}
			if (elementTypes(property).stream().anyMatch(referencing::contains)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The configuration types a value of the given structural property can have.
	 */
	private Set<Class<?>> elementTypes(PropertyDescriptor property) {
		Set<Class<?>> result = new HashSet<>();
		PropertyKind kind = property.kind();
		if (kind != PropertyKind.ITEM && kind != PropertyKind.LIST && kind != PropertyKind.ARRAY
			&& kind != PropertyKind.MAP) {
			return result;
		}
		ConfigurationDescriptor valueDescriptor = property.getValueDescriptor();
		if (valueDescriptor == null) {
			return result;
		}
		Class<?> base = valueDescriptor.getConfigurationInterface();
		if (base == ConfigurationItem.class) {
			// An untyped property is not a structural part of a view.
			return result;
		}
		result.addAll(interfaceSpecializations(base));
		Class<?> instanceType = property.getInstanceType();
		if (PolymorphicConfiguration.class.isAssignableFrom(base) && instanceType != null
			&& instanceType != Object.class) {
			result.addAll(implementationConfigs(instanceType));
		}
		for (String elementName : property.getElementNames()) {
			ConfigurationDescriptor elementDescriptor = property.getElementDescriptor(elementName);
			if (elementDescriptor != null && elementDescriptor.getConfigurationInterface() != ConfigurationItem.class) {
				result.addAll(interfaceSpecializations(elementDescriptor.getConfigurationInterface()));
			}
		}
		if (base != PolymorphicConfiguration.class) {
			result.removeIf(type -> !base.isAssignableFrom(type));
		}
		return result;
	}

	/**
	 * The given interface and all interfaces specializing it.
	 */
	private Set<Class<?>> interfaceSpecializations(Class<?> base) {
		return _interfaceSpecializations.computeIfAbsent(base, type -> {
			Set<Class<?>> result = new HashSet<>();
			if (type != PolymorphicConfiguration.class) {
				result.add(type);
				for (TypeDescriptor specialization : allSpecializations(type)) {
					if (specialization.isInterface()) {
						addLoaded(result, specialization.getClassName());
					}
				}
			}
			return result;
		});
	}

	/**
	 * The configuration interfaces of all implementations of the given type.
	 */
	private Set<Class<?>> implementationConfigs(Class<?> instanceType) {
		return _implementationConfigs.computeIfAbsent(instanceType, type -> {
			Set<Class<?>> result = new HashSet<>();
			for (TypeDescriptor implementation : allSpecializations(type)) {
				TypeDescriptor config = implementation.getConfiguration();
				if (config != null) {
					addLoaded(result, config.getClassName());
				}
			}
			return result;
		});
	}

	/**
	 * The given type and its transitive specializations as known to the {@link TypeIndex}.
	 *
	 * <p>
	 * Works on the descriptors, so that no class is initialized (some have static initializers
	 * requiring a running application).
	 * </p>
	 */
	private static Set<TypeDescriptor> allSpecializations(Class<?> type) {
		Set<TypeDescriptor> result = new HashSet<>();
		Deque<TypeDescriptor> todo = new ArrayDeque<>();
		TypeDescriptor start = TypeIndex.getInstance().getDescriptor(type);
		if (start != null) {
			todo.add(start);
		}
		while (!todo.isEmpty()) {
			TypeDescriptor descriptor = todo.removeFirst();
			if (result.add(descriptor)) {
				todo.addAll(descriptor.getSpecializations());
			}
		}
		return result;
	}

	private static void addLoaded(Set<Class<?>> result, String className) {
		try {
			result.add(Class.forName(className, false, TestViewRefsAreTreeReachable.class.getClassLoader()));
		} catch (ClassNotFoundException | LinkageError ex) {
			// Not on the class path of this module, cannot occur in its views.
		}
	}

	private static ConfigurationDescriptor descriptor(Class<?> type) {
		try {
			return TypedConfiguration.getConfigurationDescriptor(type);
		} catch (RuntimeException | LinkageError ex) {
			return null;
		}
	}

	private static boolean isTreeProperty(PropertyDescriptor property) {
		TreeProperty annotation = property.getAnnotation(TreeProperty.class);
		return annotation != null && annotation.value();
	}

	/**
	 * The qualified name of the given property at the interface declaring it.
	 */
	private static String declaringName(PropertyDescriptor property) {
		PropertyDescriptor declaring = property;
		while (declaring.getSuperProperties().length > 0) {
			declaring = declaring.getSuperProperties()[0];
		}
		return declaring.getDescriptor().getConfigurationInterface().getName() + "#" + declaring.getPropertyName();
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestViewRefsAreTreeReachable.class, TypeIndex.Module.INSTANCE);
	}
}
