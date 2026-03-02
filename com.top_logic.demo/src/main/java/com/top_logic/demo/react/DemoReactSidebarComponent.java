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
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.control.ReactButtonControl;
import com.top_logic.layout.react.control.ReactFieldListControl;
import com.top_logic.layout.react.control.sidebar.CommandItem;
import com.top_logic.layout.react.control.sidebar.GroupItem;
import com.top_logic.layout.react.control.sidebar.HeaderItem;
import com.top_logic.layout.react.control.sidebar.NavigationItem;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.control.sidebar.SeparatorItem;
import com.top_logic.layout.react.control.sidebar.SidebarItem;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Demo {@link LayoutComponent} that showcases {@link ReactSidebarControl}.
 *
 * <p>
 * Creates a sidebar with navigation items (some in a group), a command button, a separator, and
 * header/footer slots. Navigation content is created lazily and cached.
 * </p>
 */
public class DemoReactSidebarComponent extends LayoutComponent {

	/**
	 * Configuration for {@link DemoReactSidebarComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private ReactSidebarControl _sidebarControl;

	/**
	 * Creates a new {@link DemoReactSidebarComponent}.
	 */
	public DemoReactSidebarComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void writeBody(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
			TagWriter out) throws IOException, ServletException {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		if (_sidebarControl == null) {
			_sidebarControl = createSidebar();
		}

		_sidebarControl.write(displayContext, out);
	}

	private ReactSidebarControl createSidebar() {
		List<SidebarItem> items = new ArrayList<>();

		// Top-level navigation items.
		items.add(new NavigationItem("dashboard", "Dashboard", "bi bi-speedometer2", this::createDashboard));
		items.add(new NavigationItem("reports", "Reports", "bi bi-bar-chart", this::createReports));

		// A collapsible group with child navigation items.
		List<SidebarItem> settingsChildren = new ArrayList<>();
		settingsChildren.add(new NavigationItem("general", "General", "bi bi-sliders", this::createGeneral));
		settingsChildren.add(new NavigationItem("security", "Security", "bi bi-shield-lock", this::createSecurity));
		items.add(new GroupItem("settings", "Settings", "bi bi-gear", settingsChildren, true));

		// A header item.
		items.add(new HeaderItem("actions-hdr", "Actions", null));

		// A separator.
		items.add(new SeparatorItem("sep1"));

		// A command button.
		items.add(new CommandItem("greet", "Say Hello", "bi bi-chat-dots", (context) -> {
			InfoService.showInfo(ResKey.text("Hello from the sidebar command!"));
			return HandlerResult.DEFAULT_RESULT;
		}));

		// Header slot: a simple branding control.
		ReactControl headerSlot = createBrandingControl();

		// Footer slot: a simple info control.
		ReactControl footerSlot = createFooterControl();

		return new ReactSidebarControl(
			"demo.sidebar", items, "dashboard", false, headerSlot, footerSlot);
	}

	private ReactControl createDashboard() {
		return new DemoReactCounterComponent.DemoCounterControl("Dashboard Counter");
	}

	private ReactControl createReports() {
		List<ReactControl> buttons = new ArrayList<>();
		buttons.add(new ReactButtonControl("Generate Report", (context) -> {
			InfoService.showInfo(ResKey.text("Report generated!"));
			return HandlerResult.DEFAULT_RESULT;
		}));
		buttons.add(new ReactButtonControl("Export CSV", (context) -> {
			InfoService.showInfo(ResKey.text("CSV exported!"));
			return HandlerResult.DEFAULT_RESULT;
		}));
		return new ReactFieldListControl("Reports", buttons);
	}

	private ReactControl createGeneral() {
		return new DemoReactCounterComponent.DemoCounterControl("General Settings Counter");
	}

	private ReactControl createSecurity() {
		List<ReactControl> buttons = new ArrayList<>();
		buttons.add(new ReactButtonControl("Reset Password", (context) -> {
			InfoService.showInfo(ResKey.text("Password reset!"));
			return HandlerResult.DEFAULT_RESULT;
		}));
		return new ReactFieldListControl("Security", buttons);
	}

	private ReactControl createBrandingControl() {
		List<ReactControl> fields = new ArrayList<>();
		fields.add(new ReactButtonControl("App Logo", (context) -> {
			InfoService.showInfo(ResKey.text("Logo clicked!"));
			return HandlerResult.DEFAULT_RESULT;
		}));
		return new ReactFieldListControl("Branding", fields);
	}

	private ReactControl createFooterControl() {
		List<ReactControl> fields = new ArrayList<>();
		fields.add(new ReactButtonControl("User Info", (context) -> {
			InfoService.showInfo(ResKey.text("User info clicked!"));
			return HandlerResult.DEFAULT_RESULT;
		}));
		return new ReactFieldListControl("Footer", fields);
	}

}
