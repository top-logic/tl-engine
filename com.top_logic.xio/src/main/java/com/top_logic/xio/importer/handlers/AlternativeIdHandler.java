/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link Handler} assigning an alternative ID to the context object.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AlternativeIdHandler<C extends AlternativeIdHandler.Config<?>> extends ConfiguredImportHandler<C> {

	/**
	 * Configuration options for {@link AlternativeIdHandler}.
	 */
	@TagName("alternative-id")
	public interface Config<I extends AlternativeIdHandler<?>> extends ConfiguredImportHandler.Config<I> {

		/**
		 * The XML attribute that assigns the ID to the created object.
		 */
		String getIdAttribute();

	}

	/**
	 * Creates a {@link AlternativeIdHandler}.
	 */
	public AlternativeIdHandler(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String id = in.getAttributeValue(null, getConfig().getIdAttribute());
		if (id != null) {
			context.assignId(context.getVar(ImportContext.THIS_VAR), id);
		}
		return null;
	}

}
