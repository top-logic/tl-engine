/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;

/**
 * Algorithm loading stacked configurations from {@link ResourceDeclaration}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigResourceLoader {

	/**
	 * Loads all referenced resources each one potentially overloading all preceding ones.
	 * 
	 * @param context
	 *        The context to instantiate configurations.
	 * @param rootElement
	 *        The expected root tag name.
	 * @param type
	 *        the expected top-level type in the referenced resources.
	 * @param declarations
	 *        Resource references to resolve.
	 * @return The consolidated configuration read.
	 * @throws ConfigurationException
	 *         If one of the resources has a configuration problem.
	 */
	public static <T extends ConfigurationItem> T loadDeclarations(InstantiationContext context, String rootElement,
			Class<T> type, List<? extends ResourceDeclaration> declarations) throws ConfigurationException {
		ConfigurationDescriptor configDescriptor = TypedConfiguration.getConfigurationDescriptor(type);
		Map<String, ConfigurationDescriptor> globalDescriptorsByLocalName =
			Collections.<String, ConfigurationDescriptor> singletonMap(rootElement, configDescriptor);

		ConfigurationReader reader = new ConfigurationReader(context, globalDescriptorsByLocalName);

		@SuppressWarnings("unchecked")
		T config = (T) reader.setSources(toBinaryContents(declarations)).read();

		return config;
	}

	private static List<BinaryContent> toBinaryContents(List<? extends ResourceDeclaration> declarations) {
		List<BinaryContent> declarationSources = new ArrayList<>();
		for (ResourceDeclaration declaration : declarations) {
			declarationSources.add(new DataAccessProxyBinaryContent(declaration.getResource()));
		}
		return declarationSources;
	}

}
