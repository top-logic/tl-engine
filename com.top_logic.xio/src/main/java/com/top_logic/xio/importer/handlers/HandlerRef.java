/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * A {@link Handler} that is defined somewhere else in the import file specification.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HandlerRef<C extends HandlerRef.Config<?>> extends ConfiguredImportHandler<C> {

	/**
	 * Configuration options for {@link com.top_logic.xio.importer.handlers.HandlerRef}.
	 */
	@TagName("ref")
	public interface Config<I extends HandlerRef<?>> extends ConfiguredImportHandler.Config<I> {
		/**
		 * The ID of the referenced {@link Handler}.
		 * 
		 * @see com.top_logic.xio.importer.handlers.Handler.Config#getId()
		 */
		String getHandlerId();
	}

	private Handler _delegate;

	/**
	 * Creates a {@link HandlerRef} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public HandlerRef(InstantiationContext context, C config) {
		super(context, config);
		context.resolveReference(config.getHandlerId(), Handler.class, h -> _delegate = h);
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		return _delegate.importXml(context, in);
	}

}
