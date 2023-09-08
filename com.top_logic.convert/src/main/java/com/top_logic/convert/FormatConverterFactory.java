/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.convert.converters.FormatConverter;

/**
 * Registry of configured {@link FormatConverter}s
 * 
 * @see #getFormatConverter(String, String)
 * 
 * @author <a href="mailto:tma@top-logic.com>tma</a>
 */
public final class FormatConverterFactory extends ManagedClass {

	/**
	 * MIME type for plain text.
	 */
	public static final String TXT_MIMETYPE = "text/plain";

	private final Map<ConverterMapping, FormatConverter> _converters;
	
    /** The property keys for the configured (embedded) format converters */
    public static final String PROPERTY_ENTRY_NAME = "FormatConverters";
    
	/**
	 * Configuration options for {@link FormatConverterFactory}.
	 */
	public interface Config extends ServiceConfiguration<FormatConverterFactory> {

		/**
		 * Configured converters.
		 */
		@InstanceFormat
		List<FormatConverter> getFormatConverters();
	}

	/**
	 * Creates a {@link FormatConverterFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FormatConverterFactory(InstantiationContext context, Config config) {
		super(context, config);
		_converters = buildIndex(config.getFormatConverters());
	}

	private static Map<ConverterMapping, FormatConverter> buildIndex(List<FormatConverter> converters) {
		Map<ConverterMapping, FormatConverter> index = new HashMap<>();
		for (FormatConverter converter : converters) {
			register(index, converter);
		}
		return index;
	}

	private static void register(Map<ConverterMapping, FormatConverter> index, FormatConverter converter) {
		for (Iterator<ConverterMapping> it = converter.getConverterMappings(); it.hasNext();) {
			ConverterMapping key = it.next();
			FormatConverter clash = index.put(key, converter);
			if (clash != null) {
				Logger.error("Multiple converters defined for the conversion '" + key.getKey() + "': " + converter
					+ ", " + clash, FormatConverterFactory.class);
			}
		}
	}

	/**
	 * Singleton pattern
	 */
    public static FormatConverterFactory getInstance() {
    	return Module.INSTANCE.getImplementationInstance();
    }

	/**
	 * Find a format converter that converts a document from one MIME-type to another.
	 * 
	 * @param sourceMimeType
	 *        the MIME-type of the source file
	 * @param targetMimeType
	 *        the MIME-type to convert to
	 * @return a matching format converter, or <code>null</code> if no such converter is registered.
	 */
	public FormatConverter getFormatConverter(String sourceMimeType, String targetMimeType) {
		ConverterMapping mapping = new ConverterMapping(sourceMimeType, targetMimeType);
        return getFormatConverter(mapping);
    }
	
	/**
	 * Retrieve a {@link FormatConverter} that converts from the given MIME type to plain text.
	 * 
	 * @param sourceMimeType
	 *        The source MIME type to convert from.
	 * @return a converter which is able to convert to plain text
	 */
	public FormatConverter getFormatConverter(String sourceMimeType) {
		return getFormatConverter(sourceMimeType, TXT_MIMETYPE);
	}
	
	/**
	 * Find a format converter that converts a document from one MIME-type to another.
	 *
	 * @see FormatConverterFactory#getFormatConverter(String, String)
	 */
	public FormatConverter getFormatConverter(ConverterMapping key) {
		return _converters.get(key);
	}
	
	/**
	 * The number of registered converters.
	 */
	public int getConverterCount() {
	    return _converters.size();
	}
	
	/**
	 * Module reference to {@link FormatConverterFactory}.
	 */
	public static final class Module extends TypedRuntimeModule<FormatConverterFactory> {

		/**
		 * Singleton {@link FormatConverterFactory.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<FormatConverterFactory> getImplementation() {
			return FormatConverterFactory.class;
		}
		
	}

}
