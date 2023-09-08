/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.generics;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;

/**
 * Test generics for the typed configuration.
 * 
 * Assert {@link PropertyDescriptor} element type for inherited properties with a generic return
 * type.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TestTypedConfigurationGenerics extends TestCase {

	/**
	 * Name for a generic {@link List} return type.
	 */
	public static final String TEST_LIST_NAME = "testList";

	/**
	 * Name for a generic {@link Set} return type.
	 */
	public static final String TEST_SET_NAME = "testSet";

	/**
	 * Name for a generic {@link Array} return type.
	 */
	public static final String TEST_ARRAY_NAME = "testArray";

	/**
	 * Name for a generic {@link Map} return type.
	 */
	public static final String TEST_MAP_NAME = "testMap";

	/**
	 * Base class for a single type variable.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Test<A> extends ConfigurationItem {

		/**
		 * @see TestTypedConfigurationGenerics#TEST_LIST_NAME
		 */
		@Name(TEST_LIST_NAME)
		@ListBinding(format = ObjectTestFormat.class)
		List<A> getTestList();

		/**
		 * @see TestTypedConfigurationGenerics#TEST_SET_NAME
		 */
		@Name(TEST_SET_NAME)
		@ListBinding(format = ObjectTestFormat.class)
		Set<A> getTestSet();

		/**
		 * @see TestTypedConfigurationGenerics#TEST_ARRAY_NAME
		 */
		@Name(TEST_ARRAY_NAME)
		@ListBinding(format = ObjectTestFormat.class)
		A[] getTestArray();
	}

	/**
	 * Direct sub interface. Sets the type variable.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface TestDirect extends Test<String> {
		// Marker interface.
	}

	/**
	 * Delegates the type variable.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface TestTransitivityDelegates<B> extends Test<B> {
		// Marker interface.
	}

	/**
	 * Leaf of the sub interface tree which sets the type variable.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface TestTransitivityLeaf extends TestTransitivityDelegates<String> {
		// Marker interface.
	}

	/**
	 * Base class for a multiple type variable.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface TestMultipleTypeVariable<A, B, C> extends ConfigurationItem {

		/**
		 * @see TestTypedConfigurationGenerics#TEST_LIST_NAME
		 */
		@Name(TEST_LIST_NAME)
		@ListBinding(format = ObjectTestFormat.class)
		List<B> getTestList();

		/**
		 * @see TestTypedConfigurationGenerics#TEST_SET_NAME
		 */
		@Name(TEST_SET_NAME)
		@ListBinding(format = ObjectTestFormat.class)
		Set<C> getTestSet();

		/**
		 * @see TestTypedConfigurationGenerics#TEST_ARRAY_NAME
		 */
		@Name(TEST_ARRAY_NAME)
		@ListBinding(format = ObjectTestFormat.class)
		A[] getTestArray();

		/**
		 * @see TestTypedConfigurationGenerics#TEST_MAP_NAME
		 */
		@Name(TEST_MAP_NAME)
		@ListBinding(format = ObjectTestFormat.class)
		Map<A, C> getTestMap();
	}

	/**
	 * Direct sub interface. Sets the type variables.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface TestMultipleTypeVariableDirect extends TestMultipleTypeVariable<String, Double, Integer> {
		// Marker interface.
	}

	/**
	 * Delegates the type variables.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface TestMultipleTypeVariableTransitivityDelegates<A, B> extends TestMultipleTypeVariable<A, B, String> {
		// Marker interface.
	}

	/**
	 * Leaf of the sub interface tree which sets the type variables.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface TestMultipleTypeVariableTransitivityLeaf extends TestMultipleTypeVariableTransitivityDelegates<Double, Integer> {
		// Marker interface.
	}

	/**
	 * Tests direct inerhitance for {@link Array}s.
	 */
	public void testArray() {
		assertArrayProperty(TestDirect.class, TEST_ARRAY_NAME, String.class);
	}

	/**
	 * Tests direct inerhitance for {@link Set}s.
	 */
	public void testSet() {
		assertSingleRefProperty(TestDirect.class, TEST_SET_NAME, Set.class, String.class);
	}

	/**
	 * Tests direct inerhitance for {@link List}s.
	 */
	public void testList() {
		assertSingleRefProperty(TestDirect.class, TEST_LIST_NAME, List.class, String.class);
	}

	/**
	 * Tests transitive inerhitance for {@link Array}s.
	 */
	public void testArrayTransitivity() {
		assertArrayProperty(TestTransitivityLeaf.class, TEST_ARRAY_NAME, String.class);
	}

	/**
	 * Tests transitive inerhitance for {@link Set}s.
	 */
	public void testSetTransitivity() {
		assertSingleRefProperty(TestTransitivityLeaf.class, TEST_SET_NAME, Set.class, String.class);
	}

	/**
	 * Tests transitive inerhitance for {@link List}s.
	 */
	public void testListTransitivity() {
		assertSingleRefProperty(TestTransitivityLeaf.class, TEST_LIST_NAME, List.class, String.class);
	}

	/**
	 * Tests direct inerhitance for multiple type variables in {@link Map}s.
	 */
	public void testMapTypeVariables() {
		assertMapProperty(TestMultipleTypeVariableDirect.class, TEST_MAP_NAME, String.class, Integer.class);
	}

	/**
	 * Tests direct inerhitance for multiple type variables in {@link Array}s.
	 */
	public void testArrayTypeVariables() {
		assertArrayProperty(TestMultipleTypeVariableDirect.class, TEST_ARRAY_NAME, String.class);
	}

	/**
	 * Tests direct inerhitance for multiple type variables in {@link Set}s.
	 */
	public void testSetTypeVariables() {
		assertSingleRefProperty(TestMultipleTypeVariableDirect.class, TEST_SET_NAME, Set.class, Integer.class);
	}

	/**
	 * Tests direct inerhitance for multiple type variables in {@link List}s.
	 */
	public void testListTypeVariables() {
		assertSingleRefProperty(TestMultipleTypeVariableDirect.class, TEST_LIST_NAME, List.class, Double.class);
	}

	/**
	 * Tests transitive inerhitance for multiple type variables in {@link Map}s.
	 */
	public void testMapTypeVariablesTransitivity() {
		assertMapProperty(TestMultipleTypeVariableTransitivityLeaf.class, TEST_MAP_NAME, Double.class, String.class);
	}

	/**
	 * Tests transitive inerhitance for multiple type variables in {@link Array}s.
	 */
	public void testArrayTypeVariablesTransitivity() {
		assertArrayProperty(TestMultipleTypeVariableTransitivityLeaf.class, TEST_ARRAY_NAME, Double.class);
	}

	/**
	 * Tests transitive inerhitance for multiple type variables in {@link Set}s.
	 */
	public void testSetTypeVariablesTransitivity() {
		assertSingleRefProperty(TestMultipleTypeVariableTransitivityLeaf.class, TEST_SET_NAME, Set.class, String.class);
	}

	/**
	 * Tests transitive inerhitance for multiple type variables in {@link List}s.
	 */
	public void testListTypeVariablesTransitivity() {
		assertSingleRefProperty(TestMultipleTypeVariableTransitivityLeaf.class, TEST_LIST_NAME, List.class, Integer.class);
	}

	private void assertSingleRefProperty(Class<?> configInterface, String propertyName, Class<?> type, Class<?> elementType) {
		PropertyDescriptor property = getPropertyDescriptor(configInterface, propertyName);

		assertEquals(type, property.getType());
		assertEquals(elementType, property.getElementType());
	}

	private void assertMapProperty(Class<?> configInterface, String propertyName, Class<?> keyType,
			Class<?> elementType) {
		PropertyDescriptor property = getPropertyDescriptor(configInterface, propertyName);

		assertEquals(Map.class, property.getType());
		assertEquals(elementType, property.getElementType());
		assertEquals(keyType, ((PropertyDescriptorImpl) property).getKeyType());
	}

	private void assertArrayProperty(Class<?> configInterface, String propertyName, Class<?> elementType) {
		PropertyDescriptor property = getPropertyDescriptor(configInterface, propertyName);

		assert (property.getType().isArray());
		assertEquals(elementType, property.getElementType());
	}

	private PropertyDescriptor getPropertyDescriptor(Class<?> configInterface, String propertyName) {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(configInterface);

		return descriptor.getProperty(propertyName);
	}
}
