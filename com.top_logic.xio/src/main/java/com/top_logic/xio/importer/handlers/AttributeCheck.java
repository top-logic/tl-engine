/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.format.RegExpValueProvider;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link Handler} that checks that a certain XML attribute has a given value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AttributeCheck<C extends AttributeCheck.Config<?>> extends DispatchingImporter<C> {

	/**
	 * Configuration options for {@link AttributeCheck}.
	 */
	@TagName("attribute-check")
	public interface Config<I extends AttributeCheck<?>> extends DispatchingImporter.Config<I> {

		/**
		 * Name of the XML attribute to check.
		 * 
		 * @see #getValue()
		 */
		String getXmlAttribute();

		/**
		 * The {@link Pattern} that must match the value of the {@link #getXmlAttribute()}.
		 */
		@Format(RegExpValueProvider.class)
		Pattern getValue();

	}

	/**
	 * Creates a {@link AttributeCheck} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributeCheck(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String rawValue = in.getAttributeValue(null, getConfig().getXmlAttribute());
		if (!getConfig().getValue().matcher(rawValue).matches()) {
			context.error(in.getLocation(),
				I18NConstants.ERROR_UNEXPECTED_ATTRIBUTE_VALUE__HANDLER_GIVEN_EXPECTED.fill(location(), rawValue,
					getConfig().getValue().toString()));
		}
		return null;
	}

}
