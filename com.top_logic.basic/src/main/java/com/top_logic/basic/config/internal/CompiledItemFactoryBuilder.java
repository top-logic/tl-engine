/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.internal;

import java.io.File;
import java.io.IOException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.GenericConfigFactory;
import com.top_logic.basic.config.internal.gen.ConfigurationDescriptorSPI;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;

/**
 * {@link FactoryBuilder} that dynamically generates code for given {@link ConfigurationItem}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CompiledItemFactoryBuilder extends FactoryBuilder {

	private final ConfigCompiler _compiler;

	/**
	 * Creates a {@link CompiledItemFactoryBuilder}.
	 *
	 * @param generationDir
	 *        The location where dynamically generated code is stored.
	 */
	public CompiledItemFactoryBuilder(File generationDir) throws IOException {
		_compiler = new ConfigCompiler(generationDir);
	}

	@Override
	public ItemFactory createFactory(ConfigurationDescriptor descriptor) {
		if (descriptor.getConfigurationInterface().getAnnotation(NoImplementationClassGeneration.class) != null) {
			return new GenericConfigFactory(descriptor);
		}
		try {
			return new CompiledConfigFactory(this, _compiler, (ConfigurationDescriptorSPI) descriptor);
		} catch (GenerationFailedException ex) {
			Logger.warn("Cannot generate configuration implementation.", ex, CompiledItemFactoryBuilder.class);
			return new GenericConfigFactory(descriptor);
		}
	}
}
