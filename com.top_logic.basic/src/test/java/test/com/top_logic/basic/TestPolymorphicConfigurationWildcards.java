/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.basic.util.AbstractBasicTestAll;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedPolymorphicConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.reflect.TypeDescriptor;
import com.top_logic.basic.reflect.TypeIndex;

/**
 * Test that checks that the configuration properties of the module name the base type of a
 * {@link PolymorphicConfiguration} as wildcard bound.
 *
 * <p>
 * A property referencing a polymorphic configuration must be declared as
 * {@code PolymorphicConfiguration<? extends Renderer>}, not as
 * {@code PolymorphicConfiguration<Renderer>}. The rule applies to getters and setters, to
 * {@link NamedPolymorphicConfiguration}, and to the element, key and value types of collections,
 * maps and arrays. A type variable as type argument is allowed, and so is a configuration
 * interface binding the implementation class in its supertype declaration
 * ({@code interface Config extends PolymorphicConfiguration<MyImpl>}).
 * </p>
 *
 * <p>
 * The test inspects all configuration interfaces whose class files lie in the tested module (the
 * working directory of the test run, see {@link AbstractBasicTestAll#MODULE_LAYOUT}).
 * </p>
 *
 * @see #violations(Collection)
 */
@DeactivatedTest("Prevent duplicate execution: This is a test that should be run for every project. Such Tests are run via the TestAll, which explizitly calls such tests.")
public class TestPolymorphicConfigurationWildcards extends TestCase {

	/**
	 * Checks the configuration interfaces of the module.
	 */
	public void testWildcards() throws IOException {
		File moduleDir = AbstractBasicTestAll.MODULE_LAYOUT.getModuleDir().getCanonicalFile();
		assertEquals("Properties referencing a polymorphic configuration must name its base type as wildcard bound: "
			+ "use 'PolymorphicConfiguration<? extends X>' instead of 'PolymorphicConfiguration<X>' "
			+ "(also for NamedPolymorphicConfiguration and as element type of List, Collection, Map or array).",
			List.of(), violations(moduleTypes(moduleDir)));
	}

	/**
	 * All indexed {@link ConfigurationItem} interfaces whose class files lie in the given module
	 * directory.
	 */
	private static Collection<Class<?>> moduleTypes(File moduleDir) {
		TypeIndex index = TypeIndex.getInstance();
		List<Class<?>> result = new ArrayList<>();
		collectInterfaces(result, new HashSet<>(), index.getDescriptor(ConfigurationItem.class), moduleDir);
		return result;
	}

	private static void collectInterfaces(List<Class<?>> result, Set<TypeDescriptor> seen, TypeDescriptor type,
			File moduleDir) {
		if (!seen.add(type)) {
			return;
		}
		if (type.isInterface()) {
			Class<?> clazz = loadClass(type.getClassName());
			if (clazz != null && isInModule(clazz, moduleDir)) {
				result.add(clazz);
			}
		}
		for (TypeDescriptor specialization : type.getSpecializations()) {
			collectInterfaces(result, seen, specialization, moduleDir);
		}
	}

	private static Class<?> loadClass(String className) {
		try {
			return Class.forName(className, false, TestPolymorphicConfigurationWildcards.class.getClassLoader());
		} catch (ClassNotFoundException | LinkageError ex) {
			// Indexed in a module that is not completely on the class path, not part of the tested
			// module.
			return null;
		}
	}

	private static boolean isInModule(Class<?> clazz, File moduleDir) {
		CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
		if (codeSource == null || codeSource.getLocation() == null) {
			return false;
		}
		try {
			File location = new File(codeSource.getLocation().toURI()).getCanonicalFile();
			return location.toPath().startsWith(moduleDir.toPath());
		} catch (URISyntaxException | IOException | IllegalArgumentException ex) {
			return false;
		}
	}

	/**
	 * Checks the properties declared in the given interfaces.
	 *
	 * <p>
	 * Only the methods declared by an interface itself are inspected, inherited properties are
	 * reported for the interface declaring them. A getter is a method without parameters and with
	 * a result, a setter is a method with a single parameter. Static and default methods declare
	 * no properties.
	 * </p>
	 *
	 * @param types
	 *        The interfaces to check.
	 * @return Each violation as {@code <interface>#<method>: <type>}, sorted.
	 */
	public static List<String> violations(Collection<Class<?>> types) {
		List<String> result = new ArrayList<>();
		for (Class<?> type : types) {
			for (Method method : type.getDeclaredMethods()) {
				if (Modifier.isStatic(method.getModifiers()) || method.isDefault() || method.isSynthetic()
					|| method.isBridge()) {
					continue;
				}
				Type propertyType;
				switch (method.getParameterCount()) {
					case 0:
						if (method.getReturnType() == void.class) {
							continue;
						}
						propertyType = method.getGenericReturnType();
						break;
					case 1:
						propertyType = method.getGenericParameterTypes()[0];
						break;
					default:
						continue;
				}
				if (!isValid(propertyType)) {
					result.add(type.getName() + "#" + method.getName() + ": " + propertyType.getTypeName());
				}
			}
		}
		Collections.sort(result);
		return result;
	}

	/**
	 * Whether every {@link PolymorphicConfiguration} within the given type has a wildcard or a
	 * type variable as type argument.
	 */
	private static boolean isValid(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterized = (ParameterizedType) type;
			Type[] arguments = parameterized.getActualTypeArguments();
			Type raw = parameterized.getRawType();
			if (raw == PolymorphicConfiguration.class || raw == NamedPolymorphicConfiguration.class) {
				Type argument = arguments[0];
				return argument instanceof WildcardType || argument instanceof TypeVariable<?>;
			}
			for (Type argument : arguments) {
				if (!isValid(argument)) {
					return false;
				}
			}
			return true;
		}
		if (type instanceof GenericArrayType) {
			return isValid(((GenericArrayType) type).getGenericComponentType());
		}
		if (type instanceof WildcardType) {
			WildcardType wildcard = (WildcardType) type;
			for (Type bound : wildcard.getUpperBounds()) {
				if (!isValid(bound)) {
					return false;
				}
			}
			for (Type bound : wildcard.getLowerBounds()) {
				if (!isValid(bound)) {
					return false;
				}
			}
			return true;
		}
		return true;
	}

	/**
	 * The suite of this test.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(
			ServiceTestSetup.createSetup(TestPolymorphicConfigurationWildcards.class, TypeIndex.Module.INSTANCE));
	}

}
