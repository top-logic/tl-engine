/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link ConfiguredImportHandler} writing a configurable message to the import log.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LogHandler extends ConfiguredImportHandler<LogHandler.Config<?>> {

	/**
	 * Configuration options for {@link LogHandler}.
	 */
	@TagName("log")
	public interface Config<I extends LogHandler> extends ConfiguredImportHandler.Config<I> {

		/**
		 * The message to log.
		 */
		ResKey getMessage();

	}

	/**
	 * Creates a {@link LogHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LogHandler(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		context.info(in.getLocation(), getConfig().getMessage());
		return null;
	}

}
