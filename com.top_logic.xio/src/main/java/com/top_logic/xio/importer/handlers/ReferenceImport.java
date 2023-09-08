/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.StringValueProvider;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link Handler} converting the value of an {@link Config#getIdAttribute()} to a model reference.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReferenceImport<C extends ReferenceImport.Config<?>> extends AbstractReferenceImport<C> {

	/**
	 * Configuration options for {@link ReferenceImport}.
	 */
	@TagName("reference")
	public interface Config<I extends ReferenceImport<?>> extends AbstractReferenceImport.Config<I> {

		/**
		 * Format converter of the ID attribute value.
		 */
		@ImplementationClassDefault(StringValueProvider.class)
		@ItemDefault
		PolymorphicConfiguration<? extends ConfigurationValueProvider<String>> getFormat();

	}

	private ConfigurationValueProvider<String> _format;

	/**
	 * Creates a {@link ReferenceImport} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ReferenceImport(InstantiationContext context, C config) {
		super(context, config);

		_format = context.getInstance(config.getFormat());
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in)
			throws XMLStreamException {
		String rawId = readRawId(in);
		if (rawId != null) {
			try {
				String id = _format.getValue(getConfig().getIdAttribute(), rawId);
				Object ref = context.resolveObject(this, in.getLocation(), id);
				storeReference(context, ref);
			} catch (ConfigurationException ex) {
				context.error(in.getLocation(),
					I18NConstants.ERROR_ID_FORMAT__HANDLER_MESSAGE.fill(location(), ex.getErrorKey()));
			}
		}
		return null;
	}

}
