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
 * Assigns an additional ID to a referenced object.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WithResult<C extends WithResult.Config<?>> extends ObjectRef<C> {

	/**
	 * Configuration options for {@link WithResult}.
	 */
	@TagName("with-result")
	public interface Config<I extends WithResult<?>> extends ObjectRef.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link WithResult} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public WithResult(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String id = in.getAttributeValue(null, getConfig().getIdAttribute());

		Object result = super.importXml(context, in);
		context.assignId(result, id);

		return result;
	}

}
