/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx;
import com.top_logic.knowledge.gui.layout.LayoutConfig;
import com.top_logic.knowledge.service.KBBasedManagedClass;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.structure.MediaQueryControl.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundMainLayout;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;

/**
 * {@link KBBasedManagedClass} that initialises the security objects that base on the layout
 * configurations of the application.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies({
	LayoutStorage.Module.class,
	SecurityComponentCache.Module.class,
	BoundHelper.Module.class,
})
public class LayoutBasedSecurity extends KBBasedManagedClass<LayoutBasedSecurity.Config> {

	/**
	 * Typed configuration interface definition for {@link LayoutBasedSecurity}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends KBBasedManagedClass.Config<LayoutBasedSecurity> {
		// configuration interface definition
	}

	final LayoutStorage _layoutStorage;

	/**
	 * Create a {@link LayoutBasedSecurity}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public LayoutBasedSecurity(InstantiationContext context, Config config) {
		super(context, config);
		_layoutStorage = LayoutStorage.getInstance();
	}

	@Override
	protected void startUp() {
		super.startUp();
		initPersBoundComps();
	}

	/**
	 * Reloads the {@link PersBoundComp} for the application.
	 */
	public static void reloadPersistentSecurity() {
		if (Module.INSTANCE.isActive()) {
			Module.INSTANCE.getImplementationInstance().initPersBoundComps();
		}
	}

	/**
	 * Initialises the {@link PersBoundComp}s within a new {@link Transaction}.
	 */
	protected final void initPersBoundComps() {
		try (Transaction tx = kb().beginTransaction(I18NConstants.INITIALIZING_LAYOUT_BASED_SECURITY)) {
			initPersBoundComps(tx);
			tx.commit();
		}
	}

	/**
	 * Initialises the {@link PersBoundComp}s. Must be called within an transaction.
	 * 
	 * @param tx
	 *        The current transaction.
	 */
	protected void initPersBoundComps(Transaction tx) {
		SecurityComponentCache.setupCache();

		LayoutConfig.getAvailableLayouts().forEach(this::initComponent);
	}

	/**
	 * Initialised the component with the given <code>layoutName</code>.
	 * 
	 * @param layoutName
	 *        Identifier for the {@link Layout}
	 */
	protected void initComponent(String layoutName) {

		LayoutComponent.Config layout;
		try {
			layout = loadLayout(layoutName);
		} catch (ConfigurationException ex) {
			Logger.error("Loading layout '" + layoutName + "' failed.", ex, LayoutBasedSecurity.class);
			return;
		}

		if (layout == null) {
			// Error has already been reported.
			return;
		}

		int count = BoundMainLayout.initPersBoundComp(kb(), layout);
		if (count > 0) {
			SecurityComponentCache.setupCache();
			Logger.info("Created " + count + " objects.", LayoutBasedSecurity.class);
		}
	}

	private LayoutComponent.Config loadLayout(String layoutName) throws ConfigurationException {
		return ThreadContextManager.inSystemInteraction(LayoutBasedSecurity.class,
			new ComputationEx<LayoutComponent.Config, ConfigurationException>() {

				@Override
				public LayoutComponent.Config run() throws ConfigurationException {
					LayoutComponent.Config result = _layoutStorage.getOrCreateLayoutConfig(layoutName);
					result = LayoutUtils.inlineLayoutReferences(result);
					return result;
				}
			});
	}

	/**
	 * Singleton holder for the {@link LayoutBasedSecurity}.
	 */
	public static final class Module extends TypedRuntimeModule<LayoutBasedSecurity> {

		/**
		 * Singleton {@link LayoutBasedSecurity.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<LayoutBasedSecurity> getImplementation() {
			return LayoutBasedSecurity.class;
		}
	}

}
