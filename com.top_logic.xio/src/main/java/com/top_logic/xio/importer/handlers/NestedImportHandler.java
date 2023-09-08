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
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * Base class for {@link Handler}s that contain nested {@link Handler}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class NestedImportHandler<C extends NestedImportHandler.Config<?>> extends ConfiguredImportHandler<C> {

	/**
	 * Configuration options for {@link NestedImportHandler}.
	 */
	public interface Config<I extends NestedImportHandler<?>> extends ConfiguredImportHandler.Config<I> {

		/**
		 * The inner handlers to apply in the context of the current {@link Handler}'s import
		 * result.
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<? extends Handler>> getContent();

	}

	private final Handler _contentHandler;

	/**
	 * Creates a {@link NestedImportHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public NestedImportHandler(InstantiationContext context, C config) {
		super(context, config);

		List<PolymorphicConfiguration<? extends Handler>> content = config.getContent();
		_contentHandler = buildHandlers(context, content);
	}

	/**
	 * Instantiates a list of {@link Handler} configurations to a single (potentially chained)
	 * {@link Handler}.
	 */
	public static Handler buildHandlers(InstantiationContext context,
			List<PolymorphicConfiguration<? extends Handler>> handlerConfigs) {
		if (handlerConfigs.isEmpty()) {
			com.top_logic.xio.importer.handlers.DispatchingImporter.Config<?> emptyDispatch =
				TypedConfiguration.newConfigItem(DispatchingImporter.Config.class);
			return context.getInstance(emptyDispatch);
		} else if (handlerConfigs.size() > 1) {
			List<Handler> handlers = TypedConfiguration.getInstanceList(context, handlerConfigs);
			return new HandlerChain(handlers);
		} else {
			return context.getInstance(handlerConfigs.get(0));
		}
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		return importXmlInScope(context, in);
	}

	/**
	 * Calls nested content handlers.
	 * 
	 * <p>
	 * To be invoked in the scope of the currently imported object.
	 * </p>
	 * 
	 * @param context
	 *        See {@link #importXml(ImportContext, XMLStreamReader)}.
	 * @param in
	 *        See {@link #importXml(ImportContext, XMLStreamReader)}.
	 * @throws XMLStreamException
	 *         If the imported file has invalid format.
	 */
	protected final Object importXmlInScope(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		inScopeLocal(context, in);

		return _contentHandler.importXml(context, in);
	}

	/**
	 * Hook bing called from {@link #importXmlInScope(ImportContext, XMLStreamReader)} before
	 * delegating to the configured nested {@link Handler}s.
	 * 
	 * @param context
	 *        See {@link #importXml(ImportContext, XMLStreamReader)}.
	 * @param in
	 *        See {@link #importXml(ImportContext, XMLStreamReader)}.
	 * @throws XMLStreamException
	 *         If the imported file has invalid format.
	 */
	protected void inScopeLocal(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		// Hook for sub-classes.
	}

	/**
	 * {@link Handler} that dispatches to other {@link Handler}s in sequence.
	 */
	private static final class HandlerChain implements Handler {

		private final List<Handler> _handlers;
	
		/**
		 * Creates a {@link HandlerChain}.
		 * 
		 * @param handlers
		 *        The {@link Handler}s to apply in sequence.
		 */
		public HandlerChain(List<Handler> handlers) {
			_handlers = handlers;
		}
	
		@Override
		public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
			Object lastResult = null;
			for (Handler handler : _handlers) {
				lastResult = handler.importXml(context, in);
			}
			return lastResult;
		}
	}
}
