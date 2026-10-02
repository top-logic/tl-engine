/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.classic;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.function.Function;

import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.FragmentControl;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ForwardingReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.react.control.overlay.ReactDialogManagerControl;
import com.top_logic.layout.react.control.overlay.ReactMenuControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl;
import com.top_logic.layout.react.resource.ClientResources;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.theme.UITheme;
import com.top_logic.layout.react.theme.UIThemeService;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowEntry;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContextManager;

/**
 * A classic {@link LayoutComponent} that displays a React view, e.g. the agent chat, in its layout
 * cell.
 *
 * <p>
 * A React control is an {@link com.top_logic.base.services.simpleajax.HTMLFragment} like any classic
 * control: it writes its root tag, and the React bridge renders the rest on the client. This
 * component builds the control tree of the configured {@link Config#getView() view} once per
 * window and writes it whenever the classic layout renders the component.
 * </p>
 *
 * <p>
 * <b>The React window is the classic window.</b> The tree is registered in the
 * {@link ReactWindowRegistry} under the name of the classic window's sub-session. That is what makes
 * the two update paths meet in one page: the React servlet resolves the window name of a React
 * command to the classic sub-session and its {@code MainLayout}, so the classic repaints and
 * {@code InfoService} messages a React command causes reach the page over the React event stream,
 * while classic commands keep using the AJAX servlet. It is also the window id the chat's
 * {@code /chat/*} API is addressed with.
 * </p>
 *
 * <p>
 * Errors of React commands are shown in the window snackbar this component adds to the tree, as
 * in a React page.
 * </p>
 *
 * @see ReactCommandLockFilter
 */
public class ReactViewComponent extends LayoutComponent implements ControlRepresentable {

	/**
	 * Configuration options for {@link ReactViewComponent}.
	 */
	public interface Config extends LayoutComponent.Config {

		/**
		 * @see #getView()
		 */
		String VIEW = "view";

		/**
		 * The view to display, as a path below {@code /WEB-INF/views/}, e.g.
		 * {@code tl-chat-classic/chat.view.xml}.
		 */
		@Mandatory
		@Name(VIEW)
		String getView();
	}

	/** The CSS class of the element the React view is written into. */
	private static final String HOST_CSS_CLASS = "tlReactHost";

	/** Classic script that loads the React view layer into a classic page, see its header. */
	private static final String HOST_SCRIPT = "/script/tl-react-host.js";

	private ReactWindowRegistry _registry;

	private String _windowName;

	private ReactControl _root;

	/**
	 * Creates a {@link ReactViewComponent} from configuration.
	 */
	public ReactViewComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public Control getRenderingControl() {
		return new FragmentControl(this::writeHost);
	}

	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();

		// Nothing is displayed until the component is rendered again, which attaches the tree
		// again: stop observing the model meanwhile, as a React page does when it is unloaded.
		if (_root != null) {
			try (Interaction interaction = _registry.beginInteraction()) {
				_root.detach();
			}
		}
	}

	@Override
	protected void notifyRemoveFromMainLayout() {
		if (_root != null) {
			try (Interaction interaction = _registry.beginInteraction()) {
				_registry.windowClosed(_windowName);
			}
			_root = null;
		}
		super.notifyRemoveFromMainLayout();
	}

	private void writeHost(DisplayContext context, TagWriter out) throws IOException {
		UIThemeService themes = UIThemeService.getInstance();
		String themeId = themes.getSelectedThemeId();
		if (themeId == null) {
			themeId = themes.getDefaultThemeId();
		}
		UITheme theme = themes.getTheme(themeId);

		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, HOST_CSS_CLASS);
		// The theme the React page would name on <html>. The React stylesheets are scoped to this
		// element, and so are the design tokens selected by these attributes.
		out.writeAttribute(UIThemeService.THEME_ATTRIBUTE, themeId);
		themes.writeModeAttribute(out, theme);
		out.writeAttribute(UIThemeService.DS_DENSITY_ATTRIBUTE, UIThemeService.DS_DENSITY_NORMAL);
		// Before the scoped stylesheets have arrived, the host already takes the cell's height.
		out.writeAttribute(HTMLConstants.STYLE_ATTR, "height:100%");
		out.endBeginTag();
		{
			// The tree is shared with the threads of the React servlet and of the agent refresher.
			ReactWindowRegistry registry = registry(context);
			try (Interaction interaction = registry.beginInteraction()) {
				root(context, registry).write(out);
			}
		}
		out.endTag(HTMLConstants.DIV);

		writeBootScript(context, out);
	}

	/**
	 * Writes the script that loads the React view layer into the page, once per page, and mounts
	 * the controls written above.
	 *
	 * <p>
	 * Temporary workaround for the classic-UI bridge (E3): the engine writes the React client
	 * resources only into the {@code <head>} of a React page and offers no way to load them into a
	 * classic page. The markup it would write there is handed to {@code tl-react-host.js}, which
	 * installs it and scopes the stylesheets (E1). Remove when the engine can load React resources
	 * into a classic page itself.
	 * </p>
	 */
	private void writeBootScript(DisplayContext context, TagWriter out) throws IOException {
		String contextPath = context.getContextPath();
		StringWriter buffer = new StringWriter();
		TagWriter head = new TagWriter(buffer);
		ClientResources resources = ClientResources.getInstance();
		resources.writeScriptRefs(head, contextPath);
		UIThemeService.getInstance().writeThemeStyles(head);
		resources.writeStyleRefs(head, contextPath);
		head.flushBuffer();

		out.beginScript();
		out.append("(function(){var head=");
		out.writeJsString(buffer.toString());
		out.append(";if(window.TLReactHost){TLReactHost.boot(head);return;}");
		out.append("var s=document.createElement('script');s.src=");
		out.writeJsString(contextPath + HOST_SCRIPT);
		out.append(";s.onload=function(){TLReactHost.boot(head);};document.head.appendChild(s);})();");
		out.endScript();
	}

	private ReactWindowRegistry registry(DisplayContext context) {
		if (_registry == null) {
			_registry = ReactWindowRegistry.forSession(context.asRequest().getSession());
		}
		return _registry;
	}

	private ReactControl root(DisplayContext context, ReactWindowRegistry registry) {
		if (_root != null) {
			WindowEntry current = registry.getWindow(_windowName);
			if (current != null && current.getRootControl() == _root && !current.isRebuildRequested()) {
				return _root;
			}
			// The registry closed the window or asked for it to be rebuilt, e.g. because the personal
			// configuration the tree was built from was discarded: build it anew, as a React page does.
			if (!_root.isDisposed()) {
				_root.detach();
				_root.cleanupTree();
			}
			_root = null;
		}

		// The classic window's sub-session name, which every React request of the tree carries.
		String windowName =
			ChatSessionRegistry.windowId(TLContextManager.getSession(), TLContextManager.getSubSession());
		if (windowName == null) {
			throw new IllegalStateException("Component '" + getName() + "' is rendered outside a classic window.");
		}
		SSEUpdateQueue queue = registry.getOrCreateQueue(windowName);
		WindowEntry windowEntry = registry.getOrCreateWindow(windowName);
		windowEntry.markConnected();

		ReactControl root = windowRoot(
			new DefaultReactContext(context.getContextPath(), windowName, queue, registry), this::createContent);
		queue.setRootControl(root);
		windowEntry.setRootControl(root);

		_windowName = windowName;
		_root = root;
		return root;
	}

	private ReactControl createContent(ReactContext reactContext) {
		String viewPath = ViewLoader.fullPath(((Config) getConfig()).getView());
		ViewElement view;
		try {
			view = ViewLoader.getOrLoadView(viewPath);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to load view '" + viewPath + "' for component '" + getName() + "'.",
				ex);
		}
		ReactControl content = (ReactControl) view.createControl(new DefaultViewContext(reactContext, viewPath));
		content.setViewSource(viewPath);
		return content;
	}

	/**
	 * Builds the tree a React window displays: the content plus the window's snackbar, context
	 * menu and dialog manager.
	 *
	 * <p>
	 * Temporary workaround for the classic-UI bridge (E2): the engine builds this window chrome only
	 * in private methods of {@code ViewServlet.displayWindow}, so it is rebuilt here the same way.
	 * Remove when the engine offers a public factory for a window's root tree.
	 * </p>
	 *
	 * @param content
	 *        Creates the content in the context the chrome provides.
	 * @return The root of the window's tree.
	 */
	private static ReactControl windowRoot(ReactContext base, Function<ReactContext, ReactControl> content) {
		ReactSnackbarControl snackbar =
			new ReactSnackbarControl(base, "", ReactSnackbarControl.Variant.SUCCESS, () -> {
				// No dismiss handling needed.
			});
		ErrorSink errorSink = snackbar.asErrorSink();
		ReactContext reporting = new ForwardingReactContext(base) {
			@Override
			public ErrorSink getErrorSink() {
				return errorSink;
			}
		};
		ReactMenuControl menu = new ReactMenuControl(reporting, null, List.of(),
			// The select and close handlers are installed per open() by the ContextMenuOpener.
			itemId -> HandlerResult.DEFAULT_RESULT,
			() -> {
				// See above.
			});
		ReactDialogManagerControl dialogs = new ReactDialogManagerControl(reporting);
		ContextMenuOpener opener = new ContextMenuOpener(new ContextMenuOpener.MenuRenderer() {
			@Override
			public void show(int x, int y, List<ReactMenuControl.MenuEntry> items,
					Function<String, HandlerResult> selectHandler, Runnable closeHandler) {
				menu.updateItems(items);
				menu.setSelectHandler(selectHandler);
				menu.setCloseHandler(closeHandler);
				menu.open(x, y);
			}

			@Override
			public void hide() {
				menu.close();
			}
		});
		ReactContext result = new ForwardingReactContext(reporting) {
			@Override
			public ContextMenuOpener getContextMenuOpener() {
				return opener;
			}
		};
		opener.bindReactContext(() -> result);

		return new ReactStackControl(result, List.of(content.apply(result), snackbar, menu, dialogs));
	}

}
