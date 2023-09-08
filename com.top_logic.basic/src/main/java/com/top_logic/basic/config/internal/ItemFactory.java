/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.internal;

import java.io.File;
import java.io.IOException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.GenericConfigFactory;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.LocationImpl;

/**
 * Internal factory for {@link ConfigurationItem}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public abstract class ItemFactory {

	private static final FactoryBuilder BUILDER;

	static {
		FactoryBuilder builder;

		if (Boolean.getBoolean("com.top_logic.basic.config.internal.ItemFactory.generatedTypes")) {
			try {
				File generationDir = new File("tmp/generated");
				generationDir.mkdirs();
				builder = new CompiledItemFactoryBuilder(generationDir);
			} catch (RuntimeException | IOException ex) {
				Logger.warn("Cannot install configration item generator.", ex, ItemFactory.class);
				builder = genericBuilder();
			}
		} else {
			builder = genericBuilder();
		}

		BUILDER = builder;
	}

	private static FactoryBuilder genericBuilder() {
		FactoryBuilder builder;
		builder = new FactoryBuilder() {
			@Override
			public ItemFactory createFactory(ConfigurationDescriptor descriptor) {
				return new GenericConfigFactory(descriptor);
			}
		};
		return builder;
	}

	/**
	 * Creates a new {@link ConfigurationItem} without location with all values set to their
	 * defaults.
	 * 
	 * @see #createNew(Location)
	 */
	public final ConfigurationItem createNew() {
		return createNew(LocationImpl.NONE);
	}

	/**
	 * Creates a new {@link ConfigurationItem} with all values set to their defaults.
	 * 
	 * @param location
	 *        See {@link ConfigurationItem#location()}.
	 */
	public abstract ConfigurationItem createNew(Location location);

	/**
	 * Creates a new {@link ConfigurationItem} taking its default values from the given builder.
	 * 
	 * @param other
	 *        The source of initial values.
	 */
	public abstract ConfigurationItem createCopy(ConfigBuilder other) throws ConfigurationException;

	/**
	 * Creates a {@link ItemFactory} for the given {@link ConfigurationDescriptor}.
	 */
	public static ItemFactory createFactory(ConfigurationDescriptor descriptor) {
		return BUILDER.createFactory(descriptor);
	}

}