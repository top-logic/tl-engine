/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.category.ModelCategory;
import com.top_logic.basic.module.category.OperationsCategory;
import com.top_logic.basic.module.category.OtherCategory;
import com.top_logic.basic.module.category.PersistenceCategory;
import com.top_logic.basic.module.category.RuntimeCategory;

/**
 * Tests for {@link ServiceCategory}: how the category of a service is resolved, labelled and
 * ordered.
 */
public class TestServiceCategory extends TestCase {

	/** A category without a declared position. */
	public interface UnorderedCategory extends ServiceCategory {
		// Pure marker interface.
	}

	/** A service declaring its category. */
	@ServiceCategory.Of(OperationsCategory.class)
	public static class DeclaringService {
		// Test class.
	}

	/** A service inheriting the category of its superclass. */
	public static class InheritingService extends DeclaringService {
		// Test class.
	}

	/** A service overriding the category of its superclass. */
	@ServiceCategory.Of(PersistenceCategory.class)
	public static class OverridingService extends DeclaringService {
		// Test class.
	}

	/** An interface declaring the category of its implementations. */
	@ServiceCategory.Of(ModelCategory.class)
	public interface CategorizedApi {
		// Test interface.
	}

	/** A service taking its category from an interface it implements. */
	public static class ImplementingService implements CategorizedApi {
		// Test class.
	}

	/** A service declaring no category. */
	public static class PlainService {
		// Test class.
	}

	/** A declared category is resolved. */
	public void testDeclared() {
		assertSame(OperationsCategory.class, ServiceCategory.of(DeclaringService.class));
	}

	/** The category of a superclass applies to a subclass, unless the subclass declares its own. */
	public void testInherited() {
		assertSame(OperationsCategory.class, ServiceCategory.of(InheritingService.class));
		assertSame(PersistenceCategory.class, ServiceCategory.of(OverridingService.class));
	}

	/** The category of an implemented interface applies to the implementation. */
	public void testFromInterface() {
		assertSame(ModelCategory.class, ServiceCategory.of(ImplementingService.class));
	}

	/** A service declaring no category belongs to the other services. */
	public void testFallback() {
		assertSame(OtherCategory.class, ServiceCategory.of(PlainService.class));
	}

	/** The label of a category is the label of its interface. */
	public void testLabelKey() {
		assertEquals(OperationsCategory.class.getName(), ServiceCategory.label(OperationsCategory.class).getKey());
	}

	/** Categories are ordered by their declared position, undeclared ones before the others. */
	public void testOrder() {
		assertEquals(1, ServiceCategory.order(RuntimeCategory.class));
		assertEquals(ServiceCategory.DEFAULT_ORDER, ServiceCategory.order(UnorderedCategory.class));

		List<Class<? extends ServiceCategory>> categories = new ArrayList<>(
			List.of(OtherCategory.class, UnorderedCategory.class, OperationsCategory.class, RuntimeCategory.class));
		categories.sort(ServiceCategory.ORDER);
		assertEquals(
			List.of(RuntimeCategory.class, OperationsCategory.class, UnorderedCategory.class, OtherCategory.class),
			categories);
	}
}
