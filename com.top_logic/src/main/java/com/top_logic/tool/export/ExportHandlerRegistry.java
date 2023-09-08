/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * TODO FSC Please document your code.
 * 
 * @author <a href=mailto:fsc@top-logic.com>FSC</a>
 */
public final class ExportHandlerRegistry extends ManagedClass {

	/** The map of known handlers. */
	private transient Map<String, ExportHandler> map;

	/**
	 * Configuration for the export handlers {@link ExportHandlerRegistry}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<ExportHandlerRegistry> {

		/**
		 * Configuration {@link ExportHandler}.
		 * 
		 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
		 */
		interface NamedExportHandler extends NamedConfigMandatory {

			Class<? extends ExportHandler> getHandler();
		}

		/**
		 * Export handlers.
		 */
		@Key(NamedExportHandler.NAME_ATTRIBUTE)
		Map<String, NamedExportHandler> getExportHandlers();
	}

	/**
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        Configuration for {@link ExportHandlerRegistry}.
	 */
	public ExportHandlerRegistry(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		this.map = createHandlers(config);
	}

	private Map<String, ExportHandler> createHandlers(Config config)
			throws ConfigurationException {
		Map<String, ExportHandler> mapExportHandlers = new HashMap<>();

		for (String key : config.getExportHandlers().keySet()) {
			ExportHandler theHandler =
				ConfigUtil.getNewInstance(ExportHandler.class, key,
					config.getExportHandlers().get(key).getHandler().getName(),
					null,
					ExportHandler.CONSTRUCTOR_SIGNATURE, new Object[] { key });

			mapExportHandlers.put(key, theHandler);
		}

		return mapExportHandlers;
	}

	/**
	 * Return the handler for the given key.
	 * 
	 * If there is no matching handler for that key, this method will return
	 * <code>null</code>.
	 * 
	 * @param aKey
	 *            The key, the requested handler is registered to.
	 * @return The requested hander or <code>null</code>, if there is no such
	 *         handler.
	 */
	public ExportHandler getHandler(String aKey) {
		ExportHandler theHandler = this.map.get(aKey);
		if (theHandler == null) {
			throw new IllegalArgumentException("Handler with id '" + aKey + "' is not configured. Please check the configuration of " + ExportHandlerRegistry.class.getSimpleName());
		}
		return theHandler;
	}

	/**
	 * Return the only instance of this class.
	 * 
	 * @return The only instance (singleton).
	 */
	public static ExportHandlerRegistry getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link ExportHandlerRegistry}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public static final class Module extends TypedRuntimeModule<ExportHandlerRegistry> {

		/**
		 * Module instance for {@link ExportHandlerRegistry}.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<ExportHandlerRegistry> getImplementation() {
			return ExportHandlerRegistry.class;
		}
	}
	
	@Override
	protected void shutDown() {
		this.map = null;
		super.shutDown();
	}
}
