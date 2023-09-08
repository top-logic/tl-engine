/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileBasedBinaryContent;
import com.top_logic.element.ElementException;
import com.top_logic.element.meta.schema.ElementSchemaConstants;
import com.top_logic.element.model.DynamicModelService;

/**
 * Utilities for loading {@link ModelConfig}s from XML files.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefinitionReader {

	private static final Map<String, ConfigurationDescriptor> MODEL_DESCRIPTORS = Collections.singletonMap(ElementSchemaConstants.ROOT_ELEMENT,
		TypedConfiguration.getConfigurationDescriptor(ModelConfig.class));

	/**
	 * Call this to get the config from XML.
	 * 
	 * @param    aFile    The file to read the data from.
	 * @return   The created configuration.
	 */
	public static ModelConfig readElementConfig(File aFile) {
	    return DefinitionReader.readElementConfig(aFile, true);
	}

	/**
	 * Call this to get the config from XML.
	 * 
	 * @param content
	 *        The stream to read the data from.
	 * @return The created configuration.
	 */
	public static ModelConfig readElementConfig(BinaryContent content) throws ElementException {
		InstantiationContext context = new DefaultInstantiationContext(DynamicModelService.class);
		ModelConfig result;
	    try {
			result = DefinitionReader.readElementConfig(context, content, null, true);
		} catch (IOException e) {
			throw new ElementException("Problem reading configuration '" + content + "'.", e);
		}
		return result;
	}

	/**
	 * Call this to get the config from XML.
	 * 
	 * @param aFile
	 *        The file to read the data from.
	 * @param resolve
	 *        Whether to resolve references after parsing.
	 * @return The created configuration.
	 */
	public static ModelConfig readElementConfig(File aFile, boolean resolve) {
		return readElementConfig(aFile, null, resolve);
	}

	public static ModelConfig readElementConfig(File aFile, ModelConfig base, boolean resolve) {
		BinaryContent in = FileBasedBinaryContent.createBinaryContent(aFile);
		return readElementConfig(in, base, resolve);
	}

	public static ModelConfig readElementConfig(BinaryContent in, ModelConfig base, boolean resolve) {
		DefaultInstantiationContext context = new DefaultInstantiationContext(DefinitionReader.class);
		try {
			return DefinitionReader.readElementConfig(context, in, base, resolve);
		} catch (IOException ex) {
			throw new ConfigurationError("Invalid StructuredElement config '" + in + "'.", ex);
		}
	}

	/**
	 * Call this to get the config from XML.
	 * 
	 * @param context
	 *        The {@link InstantiationContext} to instantiate configuration parts.
	 * @param content
	 *        The stream to read the data from.
	 * @param base
	 *        The model to extend.
	 * @param resolve
	 *        Whether resolve names after parsing. .
	 * 
	 * @return The created configuration.
	 */
	public static ModelConfig readElementConfig(InstantiationContext context, BinaryContent content,
			ModelConfig base, boolean resolve) throws IOException {

		ModelConfig config = parse(context, content, base);

		if (resolve) {
			ElementConfigUtil.resolveNames(config);
		}

		return config;
	}

	static ModelConfig parse(InstantiationContext context, BinaryContent in, ModelConfig base)
			throws IOException {
		ModelConfig model;
		try {
			ConfigurationReader reader = new ConfigurationReader(context, MODEL_DESCRIPTORS);
			reader.setSource(in);
			reader.setBaseConfig(base);
			model = (ModelConfig) reader.read();
			context.checkErrors();
		} catch (RuntimeException | ConfigurationException ex) {
			throw new IOException("Parsing '" + in + "' failed.", ex);
		}
		return model;
	}

}
