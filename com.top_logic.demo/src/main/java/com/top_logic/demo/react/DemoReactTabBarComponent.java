/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactButtonControl;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.ReactTabBarControl;
import com.top_logic.layout.react.TabDefinition;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Demo {@link LayoutComponent} that showcases {@link ReactTabBarControl} with lazy tab content.
 *
 * <p>
 * Creates a tab bar with three tabs, each containing different React controls. Tab content is
 * created lazily on first selection and cached for instant re-selection. Switching back to a
 * previously visited tab shows its preserved state (e.g. counter values are not reset).
 * </p>
 */
public class DemoReactTabBarComponent extends LayoutComponent {

	/**
	 * Configuration for {@link DemoReactTabBarComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private ReactTabBarControl _tabBarControl;

	/**
	 * Creates a new {@link DemoReactTabBarComponent}.
	 */
	public DemoReactTabBarComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void writeBody(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
			TagWriter out) throws IOException, ServletException {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		if (_tabBarControl == null) {
			_tabBarControl = createTabBar();
		}

		_tabBarControl.write(displayContext, out);
	}

	private ReactTabBarControl createTabBar() {
		List<TabDefinition> tabs = new ArrayList<>();
		tabs.add(new TabDefinition("counter-a", "Counter A", this::createCounterA));
		tabs.add(new TabDefinition("counter-b", "Counter B", this::createCounterB));
		tabs.add(new TabDefinition("buttons", "Buttons", this::createButtonsTab));
		return new ReactTabBarControl(null, tabs);
	}

	private ReactControl createCounterA() {
		return new DemoReactCounterComponent.DemoCounterControl();
	}

	private ReactControl createCounterB() {
		return new DemoReactCounterComponent.DemoCounterControl();
	}

	private ReactControl createButtonsTab() {
		List<Object> buttons = new ArrayList<>();
		buttons.add(new ReactButtonControl("Say Hello", (context) -> {
			com.top_logic.basic.Logger.info("Hello from React tab bar button!", DemoReactTabBarComponent.class);
			return com.top_logic.tool.boundsec.HandlerResult.DEFAULT_RESULT;
		}));
		buttons.add(new ReactButtonControl("Say Goodbye", (context) -> {
			com.top_logic.basic.Logger.info("Goodbye from React tab bar button!", DemoReactTabBarComponent.class);
			return com.top_logic.tool.boundsec.HandlerResult.DEFAULT_RESULT;
		}));

		ReactControl container = new ReactControl(null, "TLFieldList");
		container.getReactState().put("title", "Button Controls");
		container.getReactState().put("fields", buttons);
		return container;
	}

}
