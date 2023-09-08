/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link Handler} that applies a chain of {@link Handler}s in nested contexts.
 * 
 * @see Config#getInputVar()
 * @see Config#getChainVar()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ChainHandler<C extends ChainHandler.Config<?>> extends ConfiguredImportHandler<C> {

	/**
	 * Configuration options for {@link ChainHandler}.
	 */
	@TagName("chain")
	public interface Config<I extends ChainHandler<?>> extends ConfiguredImportHandler.Config<I> {

		/**
		 * Name of the variable to take the initial context value from.
		 */
		@StringDefault(ImportContext.SCOPE_VAR)
		@Nullable
		String getInputVar();

		/**
		 * Name of the variable to set to the result of the last handler (or {@link #getInputVar()}
		 * for the first handler) when invoking the next {@link Handler} of the
		 * {@link #getHandlers() chain}.
		 */
		@StringDefault(ImportContext.SCOPE_VAR)
		@Nullable
		String getChainVar();

		/**
		 * The chain of {@link Handler}s to invoke.
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<? extends Handler>> getHandlers();

	}

	private final List<Handler> _handlers;

	/**
	 * Creates a {@link ChainHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ChainHandler(InstantiationContext context, C config) {
		super(context, config);

		_handlers = TypedConfiguration.getInstanceList(context, config.getHandlers());
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String chainVar = getConfig().getChainVar();
		Object result = context.getVar(getConfig().getInputVar());
		for (Handler handler : _handlers) {
			result = context.withVar(chainVar, result, in, handler);
		}
		return result;
	}

}
