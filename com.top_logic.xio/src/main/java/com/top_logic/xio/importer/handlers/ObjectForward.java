/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link Handler} assigning an ID to a new object reference that is expected to be filled later on
 * by an assignment through {@link ObjectCreate.Config#getFillForwardVar()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ObjectForward<C extends ObjectForward.Config<?>> extends ObjectImport<C> {

	/**
	 * Configuration options for {@link ObjectForward}.
	 */
	@TagName("forward")
	public interface Config<I extends ObjectForward<?>> extends ObjectImport.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link ObjectForward} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ObjectForward(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected Object importInScope(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		Location location = in.getLocation();
		String id = resolveId(context, in);
		Object result = context.resolveObject(this, location, id);
		if (!(result instanceof ImportContext.Resolution)) {
			context.error(location, I18NConstants.ERROR_FORWARD_VARIABLE_ALREADY_ASSIGNED__HANDLER_VAR.fill(location(),
				getConfig().getVar()));
		}
		return importTarget(context, in, result);
	}

}
