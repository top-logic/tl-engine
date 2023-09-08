/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;

/**
 * Base class for {@link Handler}s importing model references.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AbstractReferenceImport<C extends AbstractReferenceImport.Config<?>> extends StoreHandler<C> {

	/**
	 * Configuration options for {@link AbstractReferenceImport}.
	 */
	public interface Config<I extends AbstractReferenceImport<?>> extends StoreHandler.Config<I> {

		/**
		 * Name of the XML ID attribute containing the serialized reference.
		 */
		@Mandatory
		String getIdAttribute();

	}

	/**
	 * Creates a {@link AbstractReferenceImport} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractReferenceImport(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * Reads the ID from the XML source.
	 */
	protected String readRawId(XMLStreamReader in) {
		return in.getAttributeValue(null, getConfig().getIdAttribute());
	}

}
