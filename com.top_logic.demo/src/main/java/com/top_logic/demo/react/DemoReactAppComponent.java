/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.demo.react.DemoReactCounterComponent.DemoCounterControl;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.control.ReactButtonControl;
import com.top_logic.layout.react.control.ReactFieldListControl;
import com.top_logic.layout.react.control.layout.ReactCardControl;
import com.top_logic.layout.react.control.layout.ReactGridControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.nav.ReactAppBarControl;
import com.top_logic.layout.react.control.nav.ReactAppShellControl;
import com.top_logic.layout.react.control.nav.ReactBottomBarControl;
import com.top_logic.layout.react.control.nav.ReactBottomBarControl.BottomBarEntry;
import com.top_logic.layout.react.control.nav.ReactBreadcrumbControl;
import com.top_logic.layout.react.control.nav.ReactBreadcrumbControl.BreadcrumbEntry;
import com.top_logic.layout.react.control.overlay.ReactDialogControl;
import com.top_logic.layout.react.control.overlay.ReactDrawerControl;
import com.top_logic.layout.react.control.overlay.ReactMenuControl;
import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;
import com.top_logic.layout.react.control.sidebar.CommandItem;
import com.top_logic.layout.react.control.sidebar.NavigationItem;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.control.sidebar.SeparatorItem;
import com.top_logic.layout.react.control.sidebar.SidebarItem;
import com.top_logic.layout.structure.PersonalizingExpandable;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Demo {@link LayoutComponent} showcasing all new React controls in a "Task Manager" mini-app.
 *
 * <p>
 * Uses {@link ReactAppShellControl} as the single root control with header (app bar + breadcrumb),
 * content (sidebar with pages), and footer (bottom bar). The built-in snackbar provides
 * notifications. Overlay controls (dialog, drawer, menu) are owned by the content components that
 * trigger them.
 * </p>
 */
public class DemoReactAppComponent extends LayoutComponent {

	/**
	 * Configuration for {@link DemoReactAppComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private static final String PAGE_DASHBOARD = "dashboard";

	private static final String PAGE_MESSAGES = "messages";

	private static final String PAGE_SETTINGS = "settings";

	private ReactAppShellControl _appShell;

	private ReactBreadcrumbControl _breadcrumb;

	private ReactSidebarControl _sidebar;

	private ReactBottomBarControl _bottomBar;

	private ReactDialogControl _dialog;

	private ReactDrawerControl _drawer;

	private ReactMenuControl _menu;

	private ReactButtonControl _moreButton;

	/**
	 * Creates a new {@link DemoReactAppComponent}.
	 */
	public DemoReactAppComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void writeBody(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
			TagWriter out) throws IOException, ServletException {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		if (_appShell == null) {
			initControls();
		}

		_appShell.write(displayContext, out);
	}

	private void initControls() {
		// Overlay controls (created first so content factories can reference them).
		_drawer = new ReactDrawerControl("Details", "right", "medium", () -> { /* no-op */ });
		_drawer.setChild(createDrawerContent());

		_dialog = new ReactDialogControl("New Task", () -> { /* no-op */ });
		_dialog.setChild(createDialogContent());
		_dialog.setActions(List.of(
			new ReactButtonControl("Cancel", (context) -> {
				_dialog.close();
				return HandlerResult.DEFAULT_RESULT;
			}),
			new ReactButtonControl("Create", (context) -> {
				_dialog.close();
				_appShell.showSnackbar("Task created!");
				return HandlerResult.DEFAULT_RESULT;
			})
		));

		List<MenuEntry> menuItems = List.of(
			MenuEntry.item("refresh", "Refresh", "bi bi-arrow-clockwise"),
			MenuEntry.separator(),
			MenuEntry.item("about", "About", "bi bi-info-circle")
		);
		_menu = new ReactMenuControl("_", menuItems, this::handleMenuSelect, () -> { /* no-op */ });

		// Header: AppBar + Breadcrumb in a compact stack, plus invisible overlays (menu, drawer).
		ReactButtonControl infoButton = new ReactButtonControl("Info", (context) -> {
			_drawer.setTitle("App Info");
			_drawer.open();
			return HandlerResult.DEFAULT_RESULT;
		});
		_moreButton = new ReactButtonControl("More", (context) -> {
			_menu.patchReactState(Map.of("anchorId", _moreButton.getID()));
			_menu.open();
			return HandlerResult.DEFAULT_RESULT;
		});
		ReactAppBarControl appBar = new ReactAppBarControl("Task Manager", List.of(infoButton, _moreButton));

		_breadcrumb = new ReactBreadcrumbControl(
			breadcrumbItems(PAGE_DASHBOARD), this::handleBreadcrumbNavigate);

		ReactControl header = new ReactStackControl("column", "compact", "stretch", false,
			List.of(appBar, _breadcrumb, _menu, _drawer));

		// Content: Sidebar.
		_sidebar = createSidebar();

		// Footer: BottomBar.
		List<BottomBarEntry> bottomItems = List.of(
			new BottomBarEntry(PAGE_DASHBOARD, "Dashboard", "bi bi-speedometer2"),
			new BottomBarEntry(PAGE_MESSAGES, "Messages", "bi bi-chat-dots"),
			new BottomBarEntry(PAGE_SETTINGS, "Settings", "bi bi-gear")
		);
		_bottomBar = new ReactBottomBarControl(bottomItems, PAGE_DASHBOARD, this::handleBottomBarSelect);

		// Assemble the shell.
		_appShell = new ReactAppShellControl(header, _sidebar, _bottomBar);
	}

	// -- Sidebar --

	private ReactSidebarControl createSidebar() {
		List<SidebarItem> items = new ArrayList<>();
		items.add(new NavigationItem(PAGE_DASHBOARD, "Dashboard", "bi bi-speedometer2", this::createDashboardPage));
		items.add(new NavigationItem(PAGE_MESSAGES, "Messages", "bi bi-chat-dots", this::createMessagesPage));
		items.add(new NavigationItem(PAGE_SETTINGS, "Settings", "bi bi-gear", this::createSettingsPage));
		items.add(new SeparatorItem("sep1"));
		items.add(new CommandItem("about", "About", "bi bi-info-circle", (context) -> {
			_drawer.setTitle("About Task Manager");
			_drawer.open();
			return HandlerResult.DEFAULT_RESULT;
		}));

		boolean collapsed = PersonalizingExpandable.loadCollapsed("demo.app.collapsed", false);

		return new ReactSidebarControl(
			items, PAGE_DASHBOARD,
			collapsed, null,
			c -> PersonalizingExpandable.saveCollapsed("demo.app.collapsed", c, false),
			null,
			null, null, null, null) {
			@Override
			public void selectItem(String itemId) {
				super.selectItem(itemId);
				onNavigate(itemId);
			}
		};
	}

	private void onNavigate(String pageId) {
		_breadcrumb.updateItems(breadcrumbItems(pageId));
		_bottomBar.setActiveItem(pageId);
	}

	// -- Page content factories --

	private ReactControl createDashboardPage() {
		// Card 1: Active Tasks (elevated) with counter and "Complete" header action.
		ReactButtonControl completeBtn = new ReactButtonControl("Complete", (context) -> {
			_appShell.showSnackbar("Task completed!");
			return HandlerResult.DEFAULT_RESULT;
		});
		ReactCardControl activeTasksCard = new ReactCardControl(
			"Active Tasks", "elevated", "none", List.of(completeBtn),
			new DemoCounterControl("Tasks"));

		// Card 2: In Review (outlined) with counter.
		ReactCardControl inReviewCard = new ReactCardControl(
			"In Review", "outlined", "none", List.of(),
			new DemoCounterControl("Reviews"));

		// Card 3: Team Notes (outlined) with "Open Details" button.
		ReactButtonControl detailsBtn = new ReactButtonControl("Open Details", (context) -> {
			_drawer.setTitle("Team Notes");
			_drawer.open();
			return HandlerResult.DEFAULT_RESULT;
		});
		ReactCardControl teamNotesCard = new ReactCardControl("Team Notes",
			new ReactFieldListControl(List.of(detailsBtn)));

		// Card 4: Quick Add (outlined) with "+ New Task" button that opens the dialog.
		ReactButtonControl newTaskBtn = new ReactButtonControl("+ New Task", (context) -> {
			_dialog.open();
			return HandlerResult.DEFAULT_RESULT;
		});
		ReactCardControl quickAddCard = new ReactCardControl("Quick Add",
			new ReactFieldListControl(List.of(newTaskBtn)));

		// Dashboard grid with the dialog as an invisible overlay child.
		return new ReactStackControl(List.of(
			new ReactGridControl("16rem", "default",
				List.of(activeTasksCard, inReviewCard, teamNotesCard, quickAddCard)),
			_dialog
		));
	}

	private ReactControl createMessagesPage() {
		// Card 1: Feature Request.
		ReactButtonControl archiveBtn1 = new ReactButtonControl("Archive", (context) -> {
			_appShell.showSnackbar("Feature request archived!");
			return HandlerResult.DEFAULT_RESULT;
		});
		ReactCardControl featureCard = new ReactCardControl("Feature Request",
			new ReactFieldListControl(List.of(archiveBtn1)));

		// Card 2: Bug Report #42.
		ReactButtonControl archiveBtn2 = new ReactButtonControl("Archive", (context) -> {
			_appShell.showSnackbar("Bug report archived!");
			return HandlerResult.DEFAULT_RESULT;
		});
		ReactCardControl bugCard = new ReactCardControl("Bug Report #42",
			new ReactFieldListControl(List.of(archiveBtn2)));

		return new ReactStackControl(List.of(featureCard, bugCard));
	}

	private ReactControl createSettingsPage() {
		List<ReactControl> buttons = new ArrayList<>();
		buttons.add(new ReactButtonControl("Clear Cache", (context) -> {
			_appShell.showSnackbar("Cache cleared!");
			return HandlerResult.DEFAULT_RESULT;
		}));
		buttons.add(new ReactButtonControl("Reset Preferences", (context) -> {
			_appShell.showSnackbar("Preferences reset!");
			return HandlerResult.DEFAULT_RESULT;
		}));
		buttons.add(new ReactButtonControl("Export Data", (context) -> {
			_appShell.showSnackbar("Data exported!");
			return HandlerResult.DEFAULT_RESULT;
		}));
		return new ReactFieldListControl("Settings", buttons);
	}

	// -- Overlay content --

	private ReactControl createDrawerContent() {
		List<ReactControl> items = new ArrayList<>();
		items.add(new ReactButtonControl("Task Manager v1.0", (context) -> {
			return HandlerResult.DEFAULT_RESULT;
		}));
		items.add(new ReactButtonControl("Close", (context) -> {
			_drawer.close();
			return HandlerResult.DEFAULT_RESULT;
		}));
		return new ReactFieldListControl("Information", items);
	}

	private ReactControl createDialogContent() {
		List<ReactControl> fields = new ArrayList<>();
		fields.add(new ReactButtonControl("Task Name: (placeholder)", (context) -> {
			return HandlerResult.DEFAULT_RESULT;
		}));
		fields.add(new ReactButtonControl("Priority: Normal", (context) -> {
			return HandlerResult.DEFAULT_RESULT;
		}));
		return new ReactFieldListControl("New Task Details", fields);
	}

	// -- Handlers --

	private void handleMenuSelect(String itemId) {
		switch (itemId) {
			case "refresh":
				_appShell.showSnackbar("Refreshed!");
				break;
			case "about":
				_drawer.setTitle("About Task Manager");
				_drawer.open();
				break;
			default:
				break;
		}
	}

	private void handleBottomBarSelect(String itemId) {
		_sidebar.selectItem(itemId);
		_appShell.showSnackbar("Switched to " + labelForPage(itemId));
	}

	private void handleBreadcrumbNavigate(String itemId) {
		if ("home".equals(itemId)) {
			_sidebar.selectItem(PAGE_DASHBOARD);
			_appShell.showSnackbar("Navigated to Home");
		}
	}

	// -- Helpers --

	private static List<BreadcrumbEntry> breadcrumbItems(String pageId) {
		return List.of(
			new BreadcrumbEntry("home", "Home"),
			new BreadcrumbEntry(pageId, labelForPage(pageId))
		);
	}

	private static String labelForPage(String pageId) {
		switch (pageId) {
			case PAGE_DASHBOARD:
				return "Dashboard";
			case PAGE_MESSAGES:
				return "Messages";
			case PAGE_SETTINGS:
				return "Settings";
			default:
				return pageId;
		}
	}

}
