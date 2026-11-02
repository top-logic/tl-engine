/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.config.NamedPolymorphicConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Test of the check {@link TestPolymorphicConfigurationWildcards#violations(java.util.Collection)}.
 *
 * <p>
 * The sample interfaces with wrong declarations deliberately do not extend
 * {@link com.top_logic.basic.config.ConfigurationItem}: the check inspects the methods of any
 * interface it gets, while the module scan of {@link TestPolymorphicConfigurationWildcards} only
 * finds configuration interfaces and therefore does not report these samples.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestPolymorphicConfigurationWildcardsCheck extends TestCase {

	public interface WildcardGetter {
		PolymorphicConfiguration<? extends Runnable> getImpl();

		void setImpl(PolymorphicConfiguration<? extends Runnable> value);

		List<PolymorphicConfiguration<? extends Runnable>> getImpls();

		Map<String, NamedPolymorphicConfiguration<? extends Runnable>> getNamed();

		PolymorphicConfiguration<?> getAny();
	}

	public interface TypeVariableGetter<T> {
		PolymorphicConfiguration<T> getImpl();

		List<NamedPolymorphicConfiguration<T>> getImpls();
	}

	public interface SupertypeBinding extends PolymorphicConfiguration<Runnable> {
		// The implementation class bound in the supertype declaration is no property.
	}

	public interface NoProperties {
		static PolymorphicConfiguration<Runnable> create() {
			return null;
		}

		default PolymorphicConfiguration<Runnable> derived() {
			return null;
		}

		PolymorphicConfiguration<Runnable> lookup(String name, int index);
	}

	public interface ConcreteGetter {
		PolymorphicConfiguration<Runnable> getImpl();
	}

	public interface ConcreteSetter {
		void setImpl(PolymorphicConfiguration<Runnable> value);
	}

	public interface ConcreteList {
		List<PolymorphicConfiguration<Runnable>> getImpls();
	}

	public interface ConcreteMap {
		Map<String, PolymorphicConfiguration<Runnable>> getImpls();
	}

	public interface ConcreteArray {
		PolymorphicConfiguration<Runnable>[] getImpls();
	}

	public interface ConcreteNamed {
		NamedPolymorphicConfiguration<Runnable> getImpl();
	}

	public interface ConcreteBehindWildcard {
		List<? extends PolymorphicConfiguration<Runnable>> getImpls();
	}

	public void testValid() {
		assertEquals(List.of(), TestPolymorphicConfigurationWildcards.violations(
			List.of(WildcardGetter.class, TypeVariableGetter.class, SupertypeBinding.class, NoProperties.class)));
	}

	public void testConcreteGetter() {
		assertViolation(ConcreteGetter.class, "getImpl",
			"com.top_logic.basic.config.PolymorphicConfiguration<java.lang.Runnable>");
	}

	public void testConcreteSetter() {
		assertViolation(ConcreteSetter.class, "setImpl",
			"com.top_logic.basic.config.PolymorphicConfiguration<java.lang.Runnable>");
	}

	public void testConcreteList() {
		assertViolation(ConcreteList.class, "getImpls",
			"java.util.List<com.top_logic.basic.config.PolymorphicConfiguration<java.lang.Runnable>>");
	}

	public void testConcreteMap() {
		assertViolation(ConcreteMap.class, "getImpls",
			"java.util.Map<java.lang.String, com.top_logic.basic.config.PolymorphicConfiguration<java.lang.Runnable>>");
	}

	public void testConcreteArray() {
		assertViolation(ConcreteArray.class, "getImpls",
			"com.top_logic.basic.config.PolymorphicConfiguration<java.lang.Runnable>[]");
	}

	public void testConcreteNamed() {
		assertViolation(ConcreteNamed.class, "getImpl",
			"com.top_logic.basic.config.NamedPolymorphicConfiguration<java.lang.Runnable>");
	}

	public void testConcreteBehindWildcard() {
		assertViolation(ConcreteBehindWildcard.class, "getImpls",
			"java.util.List<? extends com.top_logic.basic.config.PolymorphicConfiguration<java.lang.Runnable>>");
	}

	public void testSorted() {
		assertEquals(
			List.of(ConcreteGetter.class.getName() + "#getImpl: "
				+ "com.top_logic.basic.config.PolymorphicConfiguration<java.lang.Runnable>",
				ConcreteSetter.class.getName() + "#setImpl: "
					+ "com.top_logic.basic.config.PolymorphicConfiguration<java.lang.Runnable>"),
			TestPolymorphicConfigurationWildcards.violations(List.of(ConcreteSetter.class, ConcreteGetter.class)));
	}

	private static void assertViolation(Class<?> type, String method, String propertyType) {
		assertEquals(List.of(type.getName() + "#" + method + ": " + propertyType),
			TestPolymorphicConfigurationWildcards.violations(List.of(type)));
	}

}
