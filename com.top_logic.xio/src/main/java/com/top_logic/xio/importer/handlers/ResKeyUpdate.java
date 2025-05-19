/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import java.util.Locale;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.util.ResKey.LiteralKey;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link Handler} adding a translation of a certain language to a model property of type
 * {@link ResKey}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResKeyUpdate<C extends ResKeyUpdate.Config<?>> extends StoreHandler<C> {

	/**
	 * Configuration options for {@link ResKeyUpdate}.
	 */
	@TagName("set-translation")
	public interface Config<I extends ResKeyUpdate<?>> extends StoreHandler.Config<I> {

		/**
		 * The XML attribute to read.
		 */
		@Mandatory
		String getXmlAttribute();

		/**
		 * The language to assign.
		 */
		@Mandatory
		String getLanguage();

	}

	/**
	 * Creates a {@link ResKeyUpdate} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ResKeyUpdate(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String rawValue = in.getAttributeValue(null, getConfig().getXmlAttribute());
		if (rawValue != null) {
			context.getProperty(this, var(context), name(), oldValue -> {
				ResKey oldResKey = (ResKey) oldValue;
				Builder builder = ResKey.builder(oldResKey);
				if (oldResKey instanceof LiteralKey literal) {
					builder.addAll(literal);
				}
				builder.add(Locale.forLanguageTag(getConfig().getLanguage()), rawValue);
				context.setProperty(this, var(context), name(), builder.build());
			});
		}
		return null;
	}

	private String name() {
		return getConfig().getName();
	}

}
