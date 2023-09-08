/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Id;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * An atomic building block of an import.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Handler {

	/**
	 * Configuration options for a {@link Handler}.
	 */
	interface Config<I extends Handler> extends PolymorphicConfiguration<I> {
		@Id(value = Handler.class)
		@Nullable
		String getId();

		/**
		 * Wrapped configuration of an inner handler that allows specifying the inner handler by tag
		 * name (if the using configuration already has another {@link DefaultContainer} property.
		 */
		interface HandlerRef extends ConfigurationItem {
			/**
			 * The inner handler.
			 */
			@DefaultContainer
			@Mandatory
			PolymorphicConfiguration<Handler> getHandler();
		}

	}

	/**
	 * Actually performs a single import step.
	 * 
	 * @param context
	 *        The current {@link ImportContext} for accessing the model.
	 * @param in
	 *        The current XML input.
	 * @return The imported object.
	 *
	 * @throws XMLStreamException
	 *         If importing fails do to invalid XML format.
	 */
	Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException;

}
