/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;

import com.top_logic.basic.module.category.OtherCategory;
import com.top_logic.basic.util.ResKey;

/**
 * A functional category of services, by which an administrator finds a service among all services
 * of an application.
 *
 * <p>
 * A category is a marker interface extending {@link ServiceCategory}. Its label is the label of
 * the interface, which is given by a {@code @Label} annotation or
 * the first sentence of its documentation comment, like the label of a service. An {@link Order}
 * annotation on the interface places it among the other categories. A service declares its category with an
 * {@link Of} annotation on its implementation class:
 * </p>
 *
 * <pre>
 * &#64;ServiceCategory.Of(OperationsCategory.class)
 * public class Scheduler extends ConfiguredManagedClass&lt;Scheduler.Config&lt;?&gt;&gt; { ... }
 * </pre>
 *
 * <p>
 * A service declaring no category belongs to {@link OtherCategory}.
 * </p>
 *
 * @see #of(Class)
 * @see ResKey#forClass(Class)
 */
public interface ServiceCategory {

	/**
	 * Position of a category without {@link Order} annotation: after the categories of the
	 * framework, before {@link OtherCategory}.
	 */
	int DEFAULT_ORDER = 90;

	/**
	 * Assigns the annotated service implementation to a {@link ServiceCategory}.
	 *
	 * <p>
	 * The annotation is inherited by subclasses. Placed on an interface the implementation class
	 * implements, it applies to all implementations not declaring a category of their own.
	 * </p>
	 */
	@Documented
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface Of {

		/**
		 * The category of the service.
		 */
		Class<? extends ServiceCategory> value();
	}

	/**
	 * Position of the annotated {@link ServiceCategory} among the categories; categories with a
	 * lower value come first.
	 *
	 * @see ServiceCategory#DEFAULT_ORDER
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface Order {

		/**
		 * The position of the category.
		 */
		int value();
	}

	/**
	 * Orders categories by their {@link #order(Class) position}, then by their label key.
	 */
	Comparator<Class<? extends ServiceCategory>> ORDER =
		Comparator.<Class<? extends ServiceCategory>> comparingInt(ServiceCategory::order)
			.thenComparing(Class::getName);

	/**
	 * The category of the given service implementation.
	 *
	 * @param service
	 *        The implementation class of a service.
	 * @return The category declared by an {@link Of} annotation on the class, a superclass or an
	 *         implemented interface; {@link OtherCategory} if none is declared.
	 */
	static Class<? extends ServiceCategory> of(Class<?> service) {
		Of declared = service.getAnnotation(Of.class);
		if (declared != null) {
			return declared.value();
		}
		Class<? extends ServiceCategory> fromInterface = ofInterfaces(service);
		return fromInterface == null ? OtherCategory.class : fromInterface;
	}

	private static Class<? extends ServiceCategory> ofInterfaces(Class<?> type) {
		for (Class<?> current = type; current != null; current = current.getSuperclass()) {
			for (Class<?> iface : current.getInterfaces()) {
				Of declared = iface.getAnnotation(Of.class);
				if (declared != null) {
					return declared.value();
				}
				Class<? extends ServiceCategory> inherited = ofInterfaces(iface);
				if (inherited != null) {
					return inherited;
				}
			}
		}
		return null;
	}

	/**
	 * The position of the given category among the categories.
	 *
	 * @return The value of its {@link Order} annotation, {@link #DEFAULT_ORDER} without one.
	 */
	static int order(Class<? extends ServiceCategory> category) {
		Order order = category.getAnnotation(Order.class);
		return order == null ? DEFAULT_ORDER : order.value();
	}

	/**
	 * The key of the label of the given category.
	 */
	static ResKey label(Class<? extends ServiceCategory> category) {
		return ResKey.forClass(category);
	}
}
