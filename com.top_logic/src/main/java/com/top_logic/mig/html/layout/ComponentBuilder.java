/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;

import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.StopWatch;

/**
 * Builder to create a {@link LayoutComponent}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ComponentBuilder {

	private InstantiationContext _context;

	private LayoutComponent _parent;

	private String _layoutName;

	private LayoutComponent.Config _configuration;

	private boolean _resolveComponent;

	/**
	 * Context to instantiate a {@link LayoutComponent.Config}.
	 */
	public void setContext(InstantiationContext context) {
		_context = context;
	}

	/**
	 * Flag for a very late lowlevel postinitialization.
	 */
	public void setResolveComponent(boolean resolveComponent) {
		_resolveComponent = resolveComponent;
	}

	/**
	 * Parent component for the component to be created.
	 */
	public void setParent(LayoutComponent parent) {
		_parent = parent;
	}

	/**
	 * Layout name for the component to be created.
	 */
	public void setLayoutName(String layoutName) {
		_layoutName = layoutName;
	}

	/**
	 * Component configuration to be instantiated.
	 */
	public void setConfiguration(LayoutComponent.Config configuration) {
		_configuration = configuration;
	}

	/**
	 * Instantiate the {@link LayoutComponent.Config} to create a {@link LayoutComponent}.
	 */
	public LayoutComponent build() throws ConfigurationException, IOException {
		LayoutComponent newComponent = instantiateComponent(_configuration);
		if (newComponent == null) {
			return null;
		}

		newComponent.getMainLayout().getAvailableComponents().put(LayoutUtils.normalizeLayoutKey(_layoutName),
			newComponent);
		return newComponent;
	}

	private LayoutComponent instantiateComponent(LayoutComponent.Config configuration) {
		StopWatch instantiationTime = StopWatch.createStartedWatch();

		LayoutComponent result = _context.deferredReferenceCheck(() -> {
			LayoutComponent component = _context.getInstance(configuration);
			if (component == null || _context.hasErrors()) {
				return null;
			}
			component.setParent(_parent);
			InstantiationContext componentContext;
			if (_parent != null) {
				componentContext = new ComponentInstantiationContext(new LogProtocol(MainLayout.class),
					_context,
					_parent.getMainLayout());
			} else {
				if (component instanceof MainLayout) {
					componentContext = new ComponentInstantiationContext(new LogProtocol(MainLayout.class),
						_context,
						(MainLayout) component);
				} else {
					_context
						.error(
						"Either a main layout must be available (direct or via parent) or the component created in '"
								+ _layoutName + "' must be a MainLayout.");
					return component;
				}
			}
			component.createSubComponents(componentContext);
			if (_resolveComponent) {
				component.resolveComponent(componentContext);
			}
			return component;
		});
		DebugHelper.logTiming(null, "Instantiating layout '" + _layoutName + "'",
			instantiationTime,
			500,
			MainLayout.class);

		return result;
	}

}
