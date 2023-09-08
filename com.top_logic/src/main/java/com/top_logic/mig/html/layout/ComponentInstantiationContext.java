/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.InstantiationContextAdaptor;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.mig.html.layout.LayoutComponent.Config;

/**
 * {@link InstantiationContext} that allows access to the {@link MainLayout} currently being
 * created.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComponentInstantiationContext extends InstantiationContextAdaptor {

	private final MainLayout _mainLayout;

	/**
	 * Creates a {@link ComponentInstantiationContext}.
	 * 
	 * @param context
	 *        The wrapped {@link InstantiationContext}.
	 * @param mainLayout
	 *        See {@link #getMainlayout()}.
	 */
	public ComponentInstantiationContext(InstantiationContext context, MainLayout mainLayout) {
		this(context, context, mainLayout);
	}

	/**
	 * Creates a {@link ComponentInstantiationContext}.
	 * 
	 * @param log
	 *        The {@link Log} to write messages to.
	 * @param context
	 *        The wrapped {@link InstantiationContext}.
	 * @param mainLayout
	 *        See {@link #getMainlayout()}.
	 */
	public ComponentInstantiationContext(Log log, InstantiationContext context, MainLayout mainLayout) {
		super(log, context);
		_mainLayout = mainLayout;
	}

	/**
	 * The {@link MainLayout} currently being created.
	 */
	public MainLayout getMainlayout() {
		return _mainLayout;
	}

	@Override
	public <T> T getInstance(InstantiationContext self, PolymorphicConfiguration<T> configuration) {
		if (configuration instanceof LayoutReference) {
			String referencedLayout = ((LayoutReference) configuration).getResource();
			if (referencedLayout.isEmpty()) {
				self.error("Reference to empty resource in configuration " + configuration.location());
				return null;
			}
			referencedLayout = LayoutUtils.normalizeLayoutKey(referencedLayout);
			LayoutComponent result = resolveReference(self, referencedLayout);
			if (result == null) {
				return null;
			}
			if (configuration.valueSet(configuration.descriptor().getProperty(LayoutComponent.Config.LAYOUT_INFO))) {
				LayoutInfo layoutInfo = ((LayoutReference) configuration).getLayoutInfo();
				result.getConfig().setLayoutInfo(layoutInfo);
			}
			/* LayoutReference is a PolymorphicConfiguration<LayoutComponent>, therefore T =
			 * LayoutComponent. */
			@SuppressWarnings("unchecked")
			T typeSafeResult = (T) result;
			return typeSafeResult;
		} else {
			return super.getInstance(self, configuration);
		}
	}

	private LayoutComponent resolveReference(InstantiationContext self, String referencedLayout) {
		LayoutComponent result = getMainlayout().getComponentForLayoutKey(referencedLayout);
		if (result != null) {
			return result;
		}

		/* Create the inner component with a "fresh" log. When this context already has errors,
		 * creating inner components would always fail, because the context is checked. Moreover
		 * when creating the inner component fails, this context must not be contaminated. */
		InstantiationContext innerContext = new ComponentInstantiationContext(
			new LogProtocol(ComponentInstantiationContext.class), self, getMainlayout());
		try {
			Config config = LayoutStorage.getInstance().getOrCreateLayoutConfig(referencedLayout);
			LayoutComponent component =
				LayoutUtils.createComponentFromXML(innerContext, getMainlayout(), referencedLayout, false, config);
			return component;
		} catch (ConfigurationException ex) {
			/* Do not fail when instantiation failed. Try return "null" and hope that the complete
			 * layout does not crash. */
			InfoService.showError(
				InfoService.messages(I18NConstants.CREATING_COMPONENT_ERROR__LAYOUT.fill(referencedLayout), ex));
			return null;
		} catch (IOException ex) {
			/* Do not fail when instantiation failed. Try return "null" and hope that the complete
			 * layout does not crash. */
			InfoService.showError(InfoService.messages(I18NConstants.READ_CONTENT_ERROR.fill(referencedLayout), ex));
			return null;
		}
	}

	/**
	 * Access to the {@link MainLayout} during the construction of a {@link LayoutComponent} and
	 * during {@link LayoutComponent#componentsResolved(InstantiationContext)}.
	 */
	public static MainLayout getMainLayout(InstantiationContext context) {
		return ((ComponentInstantiationContext) context).getMainlayout();
	}

	/**
	 * Creates the sub components for the given {@link LayoutComponent}.
	 */
	public static void createSubComponents(InstantiationContext context, LayoutComponent component) {
		if (getMainLayout(context).getAvailableComponents().containsValue(component)) {
			// sub components already created.
			return;
		}
		component.createSubComponents(context);
	}

}
