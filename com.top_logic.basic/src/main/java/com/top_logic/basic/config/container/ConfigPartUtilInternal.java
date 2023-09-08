/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.container;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * <em>Internal</em> methods of the {@link TypedConfiguration} for implementing the
 * {@link ConfigPart container} feature.
 * <p>
 * <em>This class should be used only be the typed configuration.</em><br/>
 * It is public only because of limitations of Java with visibility boundaries. (It is not possible
 * to limit the visibility of this class to <code>com.top_logic.basic.config</code> and
 * "sub packages".)
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@FrameworkInternal
public final class ConfigPartUtilInternal {

	/** The name of the {@link ConfigPart#container()} method. */
	public static final String CONTAINER_METHOD_NAME = "container";

	/** The name of the {@link ConfigPartInternal#updateContainer(ConfigurationItem)} method. */
	public static final String UPDATE_CONTAINER_METHOD_NAME = "updateContainer";

	/** The {@link ConfigPart#container()} method. */
	public static final Method CONTAINER_METHOD = getMethod(ConfigPart.class, CONTAINER_METHOD_NAME);

	/** The {@link ConfigPartInternal#updateContainer(ConfigurationItem)} method. */
	public static final Method UPDATE_CONTAINER_METHOD =
		getMethod(ConfigPartInternal.class, UPDATE_CONTAINER_METHOD_NAME, ConfigurationItem.class);

	/** The internal (=package visible) subtype of {@link ConfigPart} which has to be implemented. */
	public static final Class<? extends ConfigPart> INTERNAL_IMPLEMENTATION_CLASS = ConfigPartInternal.class;

	private static Method getMethod(Class<?> type, String name, Class<?>... signature) {
		try {
			return type.getMethod(name, signature);
		} catch (SecurityException e) {
			throw new AssertionError(e);
		} catch (NoSuchMethodException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Convenience method which calls {@link #initContainer(Object, ConfigurationItem)} for those
	 * entries of the given {@link Iterable}, which are {@link ConfigPart}s. Ignores all other
	 * entries.
	 * <p>
	 * If one of the objects is not a {@link ConfigPart}, it is ignored.
	 * </p>
	 * 
	 * @param parts
	 *        Is not allowed to be <code>null</code>.
	 * @param newContainer
	 *        Is not allowed to be <code>null</code>.
	 */
	public static void initContainers(Iterable<?> parts, ConfigurationItem newContainer) {
		for (Object part : parts) {
			if (part instanceof ConfigPartInternal) {
				initContainerInternal((ConfigPartInternal) part, newContainer);
			}
		}
	}

	/**
	 * Sets the {@link ConfigPart#container() container} of all {@link ConfigPart}s in the given
	 * array.
	 * 
	 * @see #initContainer(Object, ConfigurationItem)
	 */
	public static void initContainerArray(Object[] part, ConfigurationItem newContainer) {
		for (Object element : part) {
			initContainer(element, newContainer);
		}
	}

	/**
	 * Sets the {@link ConfigPart#container() container} of the given {@link ConfigPart} to the
	 * given {@link ConfigurationItem}.
	 * <p>
	 * If the given object is not a {@link ConfigPart}, nothing is done. <br/>
	 * Is only allowed to be called if the old container is <code>null</code>.
	 * </p>
	 * 
	 * @param part
	 *        Is allowed to be <code>null</code>.
	 * @param newContainer
	 *        Is not allowed to be <code>null</code>.
	 */
	public static void initContainer(Object part, ConfigurationItem newContainer) {
		if (part instanceof ConfigPartInternal) {
			initContainerInternal((ConfigPartInternal) part, newContainer);
		}
	}

	/**
	 * Sets the {@link ConfigPart#container() container} of all contents of the given array to
	 * <code>null</code>.
	 * 
	 * @see #clearContainer(Object)
	 */
	public static void clearContainerArray(Object[] part) {
		for (Object element : part) {
			clearContainer(element);
		}
	}

	/**
	 * Sets the {@link ConfigPart#container() container} of the given object to <code>null</code>.
	 * <p>
	 * If the given object is not a {@link ConfigPart}, nothing is done. <br/>
	 * </p>
	 * 
	 * @param part
	 *        Is not allowed to be <code>null</code>.
	 */
	public static void clearContainer(Object part) {
		if (part instanceof ConfigPartInternal) {
			clearContainerInternal((ConfigPartInternal) part);
		}
	}

	private static void initContainerInternal(ConfigPartInternal part, ConfigurationItem newContainer) {
		checkHasNoContainer(part, newContainer);
		checkIsContainerCompatible(part, newContainer);
		part.updateContainer(newContainer);
	}

	private static void checkHasNoContainer(ConfigPartInternal part, ConfigurationItem newContainer) {
		ConfigurationItem oldContainer = part.container();
		if (oldContainer != null) {
			throw new IllegalArgumentException("Item is already an entry in another container. Item: " + part
				+ " This container: " + newContainer + ", other container: " + oldContainer);
		}
	}

	private static void checkIsContainerCompatible(ConfigPartInternal part, ConfigurationItem newContainer) {
		List<Class<?>> declaredContainerTypes = getDeclaredContainerTypes(part);
		for (Class<?> declaredContainerType : declaredContainerTypes) {
			if (!declaredContainerType.isInstance(newContainer)) {
				String partClassName = part.descriptor().getConfigurationInterface().getName();
				String containerClassName = newContainer.getConfigurationInterface().getName();
				throw new IllegalArgumentException(partClassName + " is only allowed to be put into containers"
					+ " that implement all the types declared by its container properties: "
					+ StringServices.join(declaredContainerTypes, ", ") + "."
					+ " Actual container Type: " + containerClassName);
			}
		}
	}

	private static List<Class<?>> getDeclaredContainerTypes(ConfigPartInternal part) {
		List<Class<?>> result = new ArrayList<>();
		result.add(getContainerMethod(part).getReturnType());
		for (PropertyDescriptor property : part.descriptor().getProperties()) {
			if (property.hasContainerAnnotation()) {
				result.add(property.getType());
			}
		}
		return result;
	}

	private static Method getContainerMethod(ConfigPartInternal part) {
		try {
			return part.getClass().getMethod(CONTAINER_METHOD_NAME);
		} catch (NoSuchMethodException ex) {
			throw new UnreachableAssertion("Every " + ConfigPartInternal.class.getName() + " should have a '"
				+ CONTAINER_METHOD_NAME + "' method.");
		}
	}

	private static void clearContainerInternal(ConfigPartInternal part) {
		part.updateContainer(null);
	}

	/**
	 * Actually instantiates the config using the given constructor and {@link InvocationHandler}.
	 * 
	 * <p>
	 * Note: Instantiation must happen in the same package as {@link ConfigPartInternal} is
	 * declared. Otherwise instantiation fails with {@link IllegalAccessException} for configuration
	 * items that declare the {@link ConfigPart} interface.
	 * </p>
	 */
	public static ConfigurationItem newInstance(Constructor<?> constructor, InvocationHandler impl)
			throws InstantiationException, IllegalAccessException, InvocationTargetException {
		return (ConfigurationItem) constructor.newInstance(new Object[] { impl });
	}

}
