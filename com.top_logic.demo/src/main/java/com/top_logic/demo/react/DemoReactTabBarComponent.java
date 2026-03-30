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
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactFieldListControl;
import com.top_logic.layout.react.control.tabbar.ReactTabBarControl;
import com.top_logic.layout.react.control.tabbar.TabDefinition;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

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

	private ReactContext _context;

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
			_context = ReactContext.fromDisplayContext(displayContext);
			_tabBarControl = createTabBar();
		}

		_tabBarControl.write(displayContext, out);
	}

	private ReactTabBarControl createTabBar() {
		List<TabDefinition> tabs = new ArrayList<>();
		tabs.add(new TabDefinition("counter-a", "Counter A", this::createCounterA, null));
		tabs.add(new TabDefinition("counter-b", "Counter B", this::createCounterB, null));
		tabs.add(new TabDefinition("buttons", "Buttons", this::createButtonsTab, null));
		return new ReactTabBarControl(_context, null, tabs);
	}

	private ReactControl createCounterA() {
		return new DemoReactCounterComponent.DemoCounterControl(_context, "Counter A");
	}

	private ReactControl createCounterB() {
		return new DemoReactCounterComponent.DemoCounterControl(_context, "Counter B");
	}

	private ReactControl createButtonsTab() {
		List<ReactControl> buttons = new ArrayList<>();
		buttons.add(new ReactButtonControl(_context, "Say Hello", (context) -> {
			InfoService.showInfo(ResKey.text("Hello from the React tab bar!"));
			return HandlerResult.DEFAULT_RESULT;
		}));
		buttons.add(new ReactButtonControl(_context, "Say Goodbye", (context) -> {
			InfoService.showInfo(ResKey.text("Goodbye from the React tab bar!"));
			return HandlerResult.DEFAULT_RESULT;
		}));

		return new ReactFieldListControl(_context, "Button Controls", buttons);
	}

}
