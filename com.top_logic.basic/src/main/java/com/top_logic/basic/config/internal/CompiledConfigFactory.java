/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.internal.gen.ConfigurationDescriptorSPI;

/**
 * {@link ItemFactory} that generates dynamic implementations for {@link ConfigurationItem} types.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class CompiledConfigFactory extends ItemFactory {

	private static final Class<?>[] NEW_SIGNATURE =
		new Class<?>[] { ConfigurationDescriptor.class, Location.class };

	private static final Class<?>[] COPY_SIGNATURE =
		new Class<?>[] { ConfigurationDescriptor.class, ConfigurationItem.class };

	private final ConfigurationDescriptor _descriptor;

	private final Constructor<?> _copyConstructor;

	private final Constructor<?> _emptyConstructor;

	/**
	 * Creates a {@link CompiledConfigFactory}.
	 * 
	 * @param descriptor
	 *        The {@link ConfigurationDescriptor} identifying the {@link ConfigurationItem} to be
	 *        created by this {@link ItemFactory}.
	 */
	public CompiledConfigFactory(FactoryBuilder builder, ConfigCompiler compiler, ConfigurationDescriptorSPI descriptor)
			throws GenerationFailedException
	{
		_descriptor = descriptor;
		
		for (ConfigurationDescriptorSPI superDescriptor : descriptor.getSuperDescriptors()) {
			// Ensure generation of super class.
			builder.createFactory(superDescriptor);
		}

		Class<?> implClass = compiler.compileImplementation(descriptor.getConfigurationInterface());
		try {
			final Constructor<?> emptyConstructor = implClass.getConstructor(NEW_SIGNATURE);
			final Constructor<?> copyConstructor = implClass.getConstructor(COPY_SIGNATURE);

			_copyConstructor = copyConstructor;
			_emptyConstructor = emptyConstructor;
		} catch (NoSuchMethodException | SecurityException ex) {
			throw new GenerationFailedException(ex);
		}
	}

	@Override
	public ConfigurationItem createCopy(ConfigBuilder other) throws ConfigurationException {
		try {
			return (ConfigurationItem) _copyConstructor.newInstance(_descriptor, other);
		} catch (IllegalAccessException | IllegalArgumentException ex) {
			throw (AssertionError) new AssertionError("Cannot instantiate generated item class.")
				.initCause(ex);
		} catch (InvocationTargetException ex) {
			throw handleTargetException(ex);
		} catch (InstantiationException ex) {
			throw errorInstantiationException(ex);
		}
	}

	@Override
	public ConfigurationItem createNew(Location location) {
		try {
			return (ConfigurationItem) _emptyConstructor.newInstance(_descriptor, location);
		} catch (IllegalAccessException | IllegalArgumentException ex) {
			throw (AssertionError) new AssertionError("Cannot instantiate generated item class.")
				.initCause(ex);
		} catch (InvocationTargetException ex) {
			throw handleRuntimeTargetException(ex);
		} catch (InstantiationException ex) {
			throw errorInstantiationException(ex);
		}
	}

	private RuntimeException handleTargetException(InvocationTargetException ex)
			throws ConfigurationException {
		handleRuntimeTargetException(ex);

		Throwable targetException = ex.getTargetException();
		if (targetException instanceof ConfigurationException) {
			throw (ConfigurationException) targetException;
		} else {
			throw fail(ex);
		}
	}

	private RuntimeException handleRuntimeTargetException(InvocationTargetException ex) {
		Throwable targetException = ex.getTargetException();
		if (targetException instanceof RuntimeException) {
			throw (RuntimeException) targetException;
		} else if (targetException instanceof Error) {
			throw (Error) targetException;
		}
		throw fail(ex);
	}

	private AssertionError fail(InvocationTargetException ex) {
		return (AssertionError) new AssertionError("Cannot instantiate generated item class.")
			.initCause(ex);
	}

	private IllegalArgumentException errorInstantiationException(InstantiationException ex) {
		return new IllegalArgumentException(
			"Configuration type '" + _descriptor.getConfigurationInterface() + "' cannot be instantiated.",
			ex);
	}
}