/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.resource.ClientResources;
import com.top_logic.layout.react.theme.UITheme;
import com.top_logic.layout.react.theme.UIThemeService;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.UpdateChainLink;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ForwardingReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.react.control.overlay.ReactDialogManagerControl;
import com.top_logic.layout.react.control.overlay.ReactMenuControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.react.protocol.RouteChangeEvent;
import com.top_logic.layout.react.routing.RouteEncoding;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowEntry;
import com.top_logic.layout.view.login.PendingSessionAction;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.TopLogicServlet;

/**
 * Servlet that bootstraps the view-based UI.
 *
 * <p>
 * Loads a {@code .view.xml} file via {@link ViewLoader}, creates per-session control trees via
 * {@link UIElement#createControl(ViewContext)}, and renders the initial HTML page using
 * {@link TagWriter} and {@link IReactControl#write(TagWriter)}.
 * </p>
 *
 * <p>
 * The servlet is mapped to {@code /view/*}. The URL structure is:
 * </p>
 * <ul>
 * <li>{@code /view/} - Serves the window-name bootstrap page</li>
 * <li>{@code /view/<windowName>/} - Renders the default view for the given tab</li>
 * <li>{@code /view/<windowName>/some.view.xml} - Renders a specific view for the given tab. Only
 * the default view and the views the application registers as {@link ViewConfig#getEntryPoints()}
 * can be named here; any other view file is answered with
 * {@link HttpServletResponse#SC_NOT_FOUND}.</li>
 * <li>{@code /view/<windowName>/some.view.xml/<route>} - Renders a specific view and resolves the
 * route inside it.</li>
 * </ul>
 *
 * <p>
 * The view file of an entry point other than the default view is part of every URL of its page:
 * the {@link RouteManager#setPagePath(String) page path} the routes inside the page follow. The
 * default view is addressed without one, by the routes alone.
 * </p>
 *
 * <p>
 * <b>Login view:</b> An application that configures a {@link ViewConfig#getLoginView() login
 * view} shows it to every session that belongs to no account, in place of whatever the URL names.
 * The display it renders is not the application, so it takes up no URL: the route manager
 * {@link RouteManager#holdUrl(String) holds} the requested route instead of adopting it, which
 * keeps the address the visitor asked for until the page is reloaded under a session of their own.
 * An entry point marked {@link ViewConfig.EntryPoint#isAnonymous() anonymous} is the exception: a
 * URL naming it is answered with that view, which is the page the URL addresses and therefore
 * takes the URL up like any other.
 * </p>
 *
 * <p>
 * <b>Tab identity:</b> Each browser tab is identified by a unique window name. The browser's
 * {@code window.name} property persists across page reloads (F5) but is empty in new or duplicated
 * tabs. On the first request without a window name, the servlet serves a small JavaScript bootstrap
 * page that checks or creates {@code window.name} and redirects to a URL that includes the window
 * name as the first path segment. This ensures that each tab gets its own independent subsession.
 * </p>
 */
public class ViewServlet extends TopLogicServlet {

	/**
	 * The path of the view application, relative to the context: the URL a browser loads to enter
	 * it.
	 */
	public static final String ROOT_PATH = "/view/";

	/**
	 * The ending by which a path names a view file rather than a route.
	 */
	private static final String VIEW_FILE_SUFFIX = ".view.xml";

	/** Separates the segments of the path of a URL. */
	private static final char PATH_SEPARATOR = '/';

	/** Separates the query string from the path of a URL. */
	private static final char QUERY_START = '?';

	/**
	 * Attribute of the page body carrying the token of the rendered page, which the page reports
	 * back when it is unloaded.
	 *
	 * @see ReactWindowRegistry#issuePageLoad(String)
	 */
	private static final String PAGE_LOAD_ATTRIBUTE = "data-page-load";

	/**
	 * The title of a page whose application has no {@link ViewConfig#getTitle() title}.
	 *
	 * <p>
	 * The page is loaded with it and shows it while its display names no labeled route: the title
	 * the display composes takes its place as soon as there is one.
	 * </p>
	 */
	public static final String DEFAULT_TITLE = "TopLogic";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session.");
			return;
		}

		String pathInfo = request.getPathInfo();
		String windowName = extractWindowName(pathInfo);

		if (windowName == null) {
			writeBootstrapPage(request, response);
			return;
		}

		// Create or reuse the SubSession for this browser tab. On F5 (page reload) the
		// window name is the same, so the existing SubSession is reused. In a new tab, a
		// fresh SubSession is created. This mirrors the traditional layout system's
		// ContentHandlersRegistry.startLogin() but without the SubsessionHandler /
		// MainLayout setup that is specific to the traditional layout engine.
		TLSubSessionContext subSession = ensureSubSession(request, windowName);

		// A logout initiated from the React UI swaps the underlying session here, on the reload
		// request, rather than inside the command pipeline (see PendingSessionAction). This runs
		// after the subsession context is installed, because the logout event fired by
		// invalidateSession opens a knowledge-base transaction that requires a valid session
		// context.
		if (PendingSessionAction.apply(request, response)) {
			return;
		}

		String address = extractAddress(rawPathInfo(request), windowName);
		if (address == null) {
			// Entered without naming a page, so the user's own choice of where to begin applies.
			// A URL that does name one - a route, or the view file of an entry point - asks for that
			// page and is never overridden.
			address = startPage();
		} else {
			String query = request.getQueryString();
			if (query != null && !query.isEmpty()) {
				// The query belongs to the route: it carries the values that refine what the named page
				// shows - a filter term, a sorting - and the display takes them up together with the
				// path. Raw, because a query travels percent-encoded and the routing decodes it.
				address = address + QUERY_START + query;
			}
		}

		ReactWindowRegistry windowRegistry = ReactWindowRegistry.forSession(session);

		// Rendering the page attaches the window's tree, and that changes the display like any
		// command does - on a reload, while the report of the page just unloaded detaches the same
		// tree. The trees are therefore only touched within an interaction.
		try (Interaction interaction = windowRegistry.beginInteraction()) {
			displayWindow(request, response, subSession, windowRegistry, windowName, address);

			// The client of the rendered page connects within seconds. One that never does - the tab
			// was closed while the page loaded - leaves a window that is torn down like one whose
			// client went away, which is what the heartbeat started here checks.
			SSEUpdateQueue sseQueue = windowRegistry.getQueue(windowName);
			TLSessionContext sessionContext = TLContextManager.getSession();
			if (sseQueue != null && sessionContext != null) {
				sseQueue.setWindowContext(windowName, sessionContext, windowRegistry);
				sseQueue.expectClient(System.currentTimeMillis());
			}
		}
	}

	/**
	 * Renders the page of the given window, reusing the control tree it already holds where possible.
	 *
	 * <p>
	 * Called within an {@link Interaction} of the given registry.
	 * </p>
	 *
	 * @param subSession
	 *        The sub-session of the window, installed on the current thread.
	 * @param windowRegistry
	 *        The registry of the session's windows.
	 * @param windowName
	 *        The name of the window whose page is requested.
	 * @param address
	 *        The URL requested for the window, as the client shows it: the view file naming the page
	 *        if any, the route inside it and the query, without leading slash; {@code null} where it
	 *        names no page.
	 */
	private void displayWindow(HttpServletRequest request, HttpServletResponse response,
			TLSubSessionContext subSession, ReactWindowRegistry windowRegistry, String windowName,
			String address) throws IOException {
		PageAddress page = PageAddress.parse(address);
		// Collect the windows whose page was unloaded and did not come back within the grace period.
		windowRegistry.sweepUnloadedWindows();
		SSEUpdateQueue sseQueue = windowRegistry.getOrCreateQueue(windowName);
		// The page rendered now replaces the one the window displayed: the previous page's stream and
		// poll end, and what is enqueued from here on waits for this page's client.
		sseQueue.beginPage();
		// Every window is represented by an entry, an ordinary browser tab as well: it holds the tree
		// the window displays, which the registry detaches when the page is unloaded and disposes when
		// the window is torn down.
		WindowEntry windowEntry = windowRegistry.getOrCreateWindow(windowName);
		windowEntry.markConnected();

		// A reload renders the tree the window still holds instead of replacing it: everything the tree
		// holds - a table's selection, its scroll position and expansion, the input of a form, the
		// position of a pager - is state the user produced, and a rebuild throws all of it away. Only a
		// window that asks to be rebuilt is an exception: what its tree was built from is gone, so the
		// tree goes with it.
		ReactControl displayed = windowEntry.getRootControl();
		boolean rebuildRequested = windowEntry.isRebuildRequested();

		// A programmatically opened window brings its own control provider instead of a view file.
		ReactControlProvider controlProvider = windowEntry.getControlProvider();
		if (controlProvider != null) {
			// The provider and the model of a window never change, so a tree it already has always
			// fits - unlike a view, which has to be the same one.
			if (displayed != null && !rebuildRequested) {
				renderAgain(request, response, displayed, sseQueue, "", page.route(), false);
				return;
			}
			if (displayed != null) {
				// A rebuild was asked for: the old tree is never rendered again, so release the model
				// listeners its controls hold.
				displayed.detach();
				displayed.cleanupTree();
			}

			ReactContext baseContext = new DefaultReactContext(
				request.getContextPath(), windowName, sseQueue, windowRegistry);
			// The window displays what its provider creates, not a view a URL may name: only the route
			// is taken up.
			wireRouteManager(baseContext, sseQueue, "", page.route(), false);
			ReactSnackbarControl snackbar = createWindowSnackbar(baseContext);
			// The window's overlays report the results of their commands - a menu selection, say - to
			// the window snackbar.
			ReactContext reportingContext = withWindowErrorSink(baseContext, snackbar);
			ReactMenuControl menu = createWindowMenu(reportingContext);
			ReactDialogManagerControl dialogs = new ReactDialogManagerControl(reportingContext);
			ReactContext displayContext = withWindowContextMenu(reportingContext, createWindowMenuOpener(menu));
			ReactControl content = controlProvider.createControl(
				displayContext, windowEntry.getModel());
			ReactControl rootControl =
				new ReactStackControl(displayContext, List.of(content, snackbar, menu, dialogs));
			windowEntry.setRootControl(rootControl);
			sseQueue.setRootControl(rootControl);
			renderPage(request, response, rootControl, displayContext);
			return;
		}

		ViewConfig viewConfig = ApplicationConfig.getInstance().getConfig(ViewConfig.class);
		// Which account the session belongs to decides what is displayed, so it is read where the
		// session context is installed and handed to the decision as a value.
		boolean anonymous = TLContext.isAnonymous();
		String viewFile = page.decodedViewFile();
		ViewResolution resolution = resolveNamedView(viewConfig, viewFile, anonymous);
		String viewPath = resolution.viewPath();
		boolean loginView = resolution.loginView();
		if (viewPath == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such entry point.");
			return;
		}

		String pagePath;
		String url;
		if (loginView) {
			// The login view stands in for the page the URL names: the URL is held as the client
			// shows it, view file included, so that the page reloaded after the login is the one the
			// visitor asked for.
			pagePath = "";
			url = address;
		} else if (viewFile == null || isDefaultView(viewConfig, viewFile)) {
			// The default view is addressed by its routes alone, also where the URL names its file:
			// the address bar is corrected to that form by what the display reports.
			pagePath = "";
			url = page.route();
		} else {
			// In the form the client shows and sends the URLs of the page, which is what the page
			// path has to lead.
			pagePath = page.viewFile();
			url = address;
		}

		ViewElement view;
		try {
			view = ViewLoader.getOrLoadView(viewPath);
		} catch (ConfigurationException ex) {
			Logger.error("Failed to load view: " + viewPath, ex, ViewServlet.class);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				"Failed to load view: " + ex.getMessage());
			return;
		}

		// Reuse is correct only for the same view in the same language for the same user.
		Locale locale = Resources.getCurrentLocale();
		Person user = subSession == null ? null : subSession.getPerson();
		RenderedView rendered = RenderedView.lookup(subSession);
		if (displayed != null && !rebuildRequested && rendered != null
			&& rendered.matches(viewPath, view, locale, user)) {
			renderAgain(request, response, displayed, sseQueue, pagePath, url, loginView);
			return;
		}
		if (displayed != null) {
			// Another view, a view file edited in the meantime, a language or a user the tree was not
			// built for, or a rebuild asked for because what the tree was built from is gone: the old tree is
			// never rendered again, so release the model listeners its controls hold.
			displayed.detach();
			displayed.cleanupTree();
		}

		ReactContext baseContext = new DefaultReactContext(
			request.getContextPath(), windowName, sseQueue, windowRegistry);
		wireRouteManager(baseContext, sseQueue, pagePath, url, loginView);
		ReactSnackbarControl snackbar = createWindowSnackbar(baseContext);
		// The window's overlays report the results of their commands - a menu selection, say - to
		// the window snackbar.
		ReactContext reportingContext = withWindowErrorSink(baseContext, snackbar);
		ReactMenuControl menu = createWindowMenu(reportingContext);
		ReactDialogManagerControl dialogs = new ReactDialogManagerControl(reportingContext);
		ReactContext displayContext = withWindowContextMenu(reportingContext, createWindowMenuOpener(menu));
		ViewContext viewContext = new DefaultViewContext(displayContext, viewPath);

		ReloadableControl content = new ReloadableControl(viewPath, viewContext,
			(ReactControl) view.createControl(viewContext));
		ReactControl rootControl =
			new ReactStackControl(displayContext, List.of(content, snackbar, menu, dialogs));
		sseQueue.setRootControl(rootControl);
		windowEntry.setRootControl(rootControl);
		RenderedView.store(subSession, new RenderedView(viewPath, view, locale, user));

		renderPage(request, response, rootControl, displayContext);
	}

	/**
	 * Renders a control tree the browser tab already holds into a freshly loaded page.
	 *
	 * <p>
	 * The rendered output carries the full state of every control and reuses their IDs, so the new
	 * client addresses the same controls as the old one. The events queued for the previous client
	 * are dropped already ({@link SSEUpdateQueue#beginPage()}): they describe steps towards a state
	 * the rendering below contains.
	 * </p>
	 *
	 * @param pagePath
	 *        The page path of the displayed page - see
	 *        {@link #wireRouteManager(ReactContext, SSEUpdateQueue, String, String, boolean)}.
	 * @param url
	 *        The URL requested, adopted by the tree while it is rendered (a deep link entered in an
	 *        existing tab).
	 * @param loginView
	 *        Whether the tree is the application's login view, which keeps the requested URL instead
	 *        of taking it up - see
	 *        {@link #wireRouteManager(ReactContext, SSEUpdateQueue, String, String, boolean)}.
	 */
	private void renderAgain(HttpServletRequest request, HttpServletResponse response,
			ReactControl rootControl, SSEUpdateQueue sseQueue, String pagePath, String url,
			boolean loginView) throws IOException {
		ReactContext context = rootControl.getReactContext();

		sseQueue.setRootControl(rootControl);
		wireRouteManager(context, sseQueue, pagePath, url, loginView);

		renderPage(request, response, rootControl, context);
	}

	/**
	 * Creates the window-level snackbar that renders error and info notifications in windows whose
	 * view does not embed an app shell (which carries its own snackbar), e.g. tool side-windows.
	 */
	private static ReactSnackbarControl createWindowSnackbar(ReactContext context) {
		return new ReactSnackbarControl(context, "", ReactSnackbarControl.Variant.SUCCESS, () -> {
			// No dismiss handling needed.
		});
	}

	/**
	 * Derives a context whose {@link ReactContext#getErrorSink()} routes to the window snackbar, so
	 * command errors and action feedback are user-visible in every view window.
	 */
	private static ReactContext withWindowErrorSink(ReactContext context, ReactSnackbarControl snackbar) {
		ErrorSink errorSink = snackbar.asErrorSink();
		return new ForwardingReactContext(context) {
			@Override
			public ErrorSink getErrorSink() {
				return errorSink;
			}
		};
	}

	/**
	 * Creates the context menu overlay of the browser window, serving every view it displays.
	 *
	 * <p>
	 * A {@link ReactMenuControl} positions itself at viewport coordinates, so exactly one overlay per
	 * browser window is required. The control must be part of the window's root control tree to be
	 * rendered; see {@link #withWindowContextMenu(ReactContext, ContextMenuOpener)} for publishing the
	 * matching {@link ContextMenuOpener} to the view.
	 * </p>
	 */
	private static ReactMenuControl createWindowMenu(ReactContext context) {
		return new ReactMenuControl(context, null, List.of(),
			// The select handler is installed per open() by the ContextMenuOpener.
			itemId -> HandlerResult.DEFAULT_RESULT,
			() -> {
				// The close handler is installed per open() by the ContextMenuOpener.
			});
	}

	/**
	 * Creates the {@link ContextMenuOpener} rendering into the given window menu overlay.
	 */
	private static ContextMenuOpener createWindowMenuOpener(ReactMenuControl menu) {
		return new ContextMenuOpener(new ContextMenuOpener.MenuRenderer() {
			@Override
			public void show(int x, int y, List<ReactMenuControl.MenuEntry> items,
					Function<String, HandlerResult> selectHandler, Runnable closeHandler) {
				menu.updateItems(items);
				menu.setSelectHandler(selectHandler);
				menu.setCloseHandler(closeHandler);
				menu.open(x, y);
			}

			@Override
			public void show(String anchorId, List<ReactMenuControl.MenuEntry> items,
					Function<String, HandlerResult> selectHandler, Runnable closeHandler) {
				menu.updateItems(items);
				menu.setSelectHandler(selectHandler);
				menu.setCloseHandler(closeHandler);
				menu.setAnchorId(anchorId);
				menu.open();
			}

			@Override
			public void hide() {
				menu.close();
			}
		});
	}

	/**
	 * Derives a context whose {@link ReactContext#getContextMenuOpener()} is the window-level opener,
	 * so any view the window displays can open a context menu.
	 */
	private static ReactContext withWindowContextMenu(ReactContext context, ContextMenuOpener opener) {
		ReactContext result = new ForwardingReactContext(context) {
			@Override
			public ContextMenuOpener getContextMenuOpener() {
				return opener;
			}
		};
		opener.bindReactContext(() -> result);
		return result;
	}

	/**
	 * Creates a new {@link TLSubSessionContext} for the given window name, or reuses the existing
	 * one (e.g. on page reload).
	 *
	 * <p>
	 * The SubSession is stored in the {@link TLSessionContext} under the window name so that
	 * {@link com.top_logic.layout.react.servlet.ReactServlet} can look it up when handling
	 * subsequent commands and uploads.
	 * </p>
	 */
	private TLSubSessionContext ensureSubSession(HttpServletRequest request, String windowName) {
		TLSessionContext sessionContext = TLContextManager.getSession();
		if (sessionContext == null) {
			return null;
		}

		TLSubSessionContext subSession = sessionContext.getSubSession(windowName);
		if (subSession == null) {
			subSession = (TLSubSessionContext) ThreadContextManager.getManager().newSubSessionContext();
			subSession.setSessionContext(sessionContext);
			subSession = sessionContext.setIfAbsent(windowName, subSession);
			subSession.setPerson(sessionContext.getOriginalUser());

			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			HistoryManager hm = kb.getHistoryManager();
			UpdateChainLink lastKBRevision = ((UpdateChainView) kb.getUpdateChain()).current();
			subSession.updateSessionRevision(hm, lastKBRevision);
		}

		// Install on the current thread so that TLContext.getContext() is available during
		// view rendering (e.g. for locale resolution).
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
		displayContext.installSubSessionContext(subSession);

		return subSession;
	}

	/**
	 * Extracts the window name from the first path segment.
	 *
	 * <p>
	 * Window names start with {@code v} followed by alphanumeric characters (generated client-side).
	 * If the first path segment contains a dot, it is interpreted as a view file name rather than a
	 * window name.
	 * </p>
	 *
	 * @return the window name, or {@code null} if no valid window name is present.
	 */
	private String extractWindowName(String pathInfo) {
		if (pathInfo == null || pathInfo.length() <= 1) {
			return null;
		}
		// Remove leading slash.
		String path = pathInfo.substring(1);
		int slashIdx = path.indexOf('/');
		String firstSegment = slashIdx >= 0 ? path.substring(0, slashIdx) : path;

		if (firstSegment.isEmpty() || firstSegment.indexOf('.') >= 0) {
			// Contains a dot -> view file name, not a window name.
			return null;
		}
		if (firstSegment.charAt(0) != 'v') {
			// Window names start with 'v'.
			return null;
		}

		return firstSegment;
	}

	/**
	 * Extracts the address of the requested page from the URL: everything after the window name
	 * segment.
	 *
	 * <p>
	 * For URL {@code /v1a2b3c/property/42}, returns {@code "property/42"}, for
	 * {@code /v1a2b3c/demo/x.view.xml/property/42} {@code "demo/x.view.xml/property/42"} - the form in
	 * which the client shows the URL, and which {@link PageAddress#parse(String)} splits into the
	 * view file and the route inside it. A URL naming a view file names a page, even with an empty
	 * route: it asks for the view it names rather than for the page the user last chose.
	 * </p>
	 *
	 * @param pathInfo
	 *        The path below the servlet, with its segments percent-encoded - see
	 *        {@link #rawPathInfo(HttpServletRequest)}.
	 * @param windowName
	 *        The window name occupying the first segment.
	 * @return The address without leading slash, or {@code null} where the URL names no page at all.
	 */
	private String extractAddress(String pathInfo, String windowName) {
		if (pathInfo == null || windowName == null) {
			return null;
		}
		String normalized = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
		if (!normalized.startsWith(windowName)) {
			return null;
		}
		String afterWindow = normalized.substring(windowName.length());
		if (afterWindow.startsWith("/")) {
			afterWindow = afterWindow.substring(1);
		}
		if (afterWindow.isEmpty()) {
			return null;
		}
		return afterWindow;
	}

	/**
	 * The address of the page the current user starts on, or {@code null} where they have chosen
	 * none.
	 *
	 * <p>
	 * A start page is the URL of a page the user displayed, view file included. A start page naming
	 * a view the application no longer offers as an entry point is no page to begin on, and the
	 * application is entered as if none were chosen.
	 * </p>
	 *
	 * @see StartPage
	 */
	private static String startPage() {
		String stored = StartPage.get();
		if (stored == null) {
			return null;
		}
		String viewFile = PageAddress.parse(stored).decodedViewFile();
		ViewConfig config = ApplicationConfig.getInstance().getConfig(ViewConfig.class);
		if (viewFile != null && !isEntryPoint(config, viewFile)) {
			return null;
		}
		return stored;
	}

	/**
	 * The path below the servlet in the form the browser requested it, with the percent-encoding of
	 * its segments intact.
	 *
	 * <p>
	 * A route carries values whose characters have a meaning in a URL, a slash above all, so the
	 * route is read in encoded form: the segments of the request URI are the ones the route pattern
	 * matches, and only the value a parameter captures is decoded.
	 * {@link HttpServletRequest#getPathInfo()} delivers the path already decoded by the servlet
	 * container, where such a value is indistinguishable from the segments around it - and where the
	 * URL a back navigation sends as a {@code navigateToRoute} command, which is the encoded one,
	 * would resolve differently than the same URL entered into the address bar.
	 * </p>
	 *
	 * @param request
	 *        The request being served.
	 * @return The path below the context and servlet path, starting with a slash, or {@code null}
	 *         for a request that names none.
	 */
	private static String rawPathInfo(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String servletUrl = request.getContextPath() + request.getServletPath();
		if (!uri.startsWith(servletUrl)) {
			// The context or servlet path itself is encoded in the URI, so the path below it cannot
			// be cut off by length. Such an application has no place to put an encoded route.
			return request.getPathInfo();
		}
		String pathInfo = uri.substring(servletUrl.length());
		return pathInfo.isEmpty() ? null : pathInfo;
	}

	@Override
	protected String getEntryPage(HttpServletRequest request) {
		return requestedPage(request.getRequestURI(), request.getContextPath());
	}

	/**
	 * The page the given request URI names, relative to the context.
	 *
	 * <p>
	 * A request that arrives without a session is answered with a redirect to this page once the
	 * session exists, so that the URL a user asked for is the one they get. The segments keep the
	 * percent-encoding the browser sent them with, for the reason
	 * {@link #rawPathInfo(HttpServletRequest)} describes; the query string is not part of the page
	 * and is appended by {@link #createRedirectURL(String, HttpServletRequest)}.
	 * </p>
	 *
	 * @param requestURI
	 *        The URI of the request, as {@link HttpServletRequest#getRequestURI()} reports it:
	 *        beginning with the context path and encoded.
	 * @param contextPath
	 *        The context path of the application, as
	 *        {@link HttpServletRequest#getContextPath()} reports it: empty for an application
	 *        deployed at the root.
	 * @return The requested page, starting with a slash and relative to the context.
	 */
	public static String requestedPage(String requestURI, String contextPath) {
		if (!requestURI.startsWith(contextPath)) {
			// The context path is encoded in the URI, so the path below it cannot be cut off by
			// length. Such an application enters its view UI at the root of the servlet.
			return ROOT_PATH;
		}
		String page = requestURI.substring(contextPath.length());
		return page.isEmpty() ? ROOT_PATH : page;
	}

	/**
	 * The {@link ViewConfig#getTitle() title of the application}, or {@code null} if it has none.
	 */
	private static ResKey applicationTitle() {
		return ApplicationConfig.getInstance().getConfig(ViewConfig.class).getTitle();
	}

	/**
	 * Wires the {@link RouteManager} from the given context to the SSE queue.
	 *
	 * <p>
	 * Hands the route manager the page path and the URL the loaded page displays (for deep-link
	 * resolution) and installs a URL change handler that pushes {@link RouteChangeEvent}s via SSE.
	 * Also stores the route manager on the SSE queue so that
	 * {@link com.top_logic.layout.react.servlet.ReactServlet} can look it up for handling
	 * {@code navigateToRoute} commands.
	 * </p>
	 *
	 * @param pagePath
	 *        The view file of an entry point that is not the default view, which every URL of the
	 *        page starts with - see {@link RouteManager#setPagePath(String)}; empty for the default
	 *        view.
	 * @param url
	 *        The URL to take up, starting with the page path; {@code null} for none.
	 * @param loginView
	 *        Whether the page displays the application's login view. Such a page is not the
	 *        application the URL addresses, so it takes the URL up not at all: the URL is
	 *        {@link RouteManager#holdUrl(String) held}, which keeps the address the visitor asked
	 *        for while they log in.
	 */
	private void wireRouteManager(ReactContext context, SSEUpdateQueue sseQueue, String pagePath,
			String url, boolean loginView) {
		RouteManager routeManager = context.getRouteManager();
		if (routeManager == null) {
			return;
		}

		routeManager.setPagePath(pagePath);
		String requestedRoute = url == null ? "" : url;
		if (loginView) {
			routeManager.holdUrl(requestedRoute);
		} else {
			// Unconditionally, an empty route included: a freshly loaded page displays what its URL
			// says, which for a bare view is nothing - and the address bar has to be completed from
			// the display rather than left describing less than it shows.
			routeManager.adoptUrl(requestedRoute);
		}

		routeManager.setApplicationTitle(applicationTitle());
		routeManager.setUrlChangeHandler((changedUrl, title, replace) -> {
			RouteChangeEvent event = RouteChangeEvent.create()
				.setUrl(changedUrl)
				.setTitle(title)
				.setReplace(replace);
			sseQueue.enqueue(event);
		});

		sseQueue.setRouteManager(routeManager);
	}

	/**
	 * The view a request is answered with.
	 *
	 * <p>
	 * Which view is displayed and whether that view is the application's login view are one
	 * decision, made by {@link ViewServlet#resolveView(ViewConfig, String, boolean)}: the login
	 * view stands in for the page the URL names, while every other view <em>is</em> that page -
	 * which is what decides whether the requested route is
	 * {@link RouteManager#adoptUrl(String) adopted} or {@link RouteManager#holdUrl(String) held}.
	 * </p>
	 *
	 * @param viewPath
	 *        The path of the view file to load, below {@link ViewLoader#VIEW_BASE_PATH}, or
	 *        {@code null} where the URL names a view that is no entry point.
	 * @param loginView
	 *        Whether the displayed view is the {@link ViewConfig#getLoginView() login view} shown
	 *        in place of the page the URL names.
	 */
	public record ViewResolution(String viewPath, boolean loginView) {
		// Pure result of the resolution.
	}

	/**
	 * Resolves the view a request displays from its path info.
	 *
	 * <p>
	 * Skips the first path segment (window name) and uses the rest as the view file name. A
	 * remainder that does not name a view file is a route path handled by the
	 * {@link RouteManager}, so the default view is loaded and the route resolved inside it.
	 * </p>
	 *
	 * <p>
	 * A named view is loaded only where the application declares it as an entry point: the
	 * {@link ViewConfig#getDefaultView()} or one of the {@link ViewConfig#getEntryPoints()}. Every
	 * other view file is a fragment of a display, which the view enclosing it supplies with the
	 * channels it reads, and naming it here is refused.
	 * </p>
	 *
	 * <p>
	 * A session that belongs to no account sees the {@link ViewConfig#getLoginView() login view} of
	 * an application that has one, whatever the URL names: a route, the default view, an entry
	 * point, or a view that is none - the visitor is shown the login and nothing else. What the URL
	 * names is not lost with it, because the route is held while the login view is displayed. An
	 * entry point marked {@link ViewConfig.EntryPoint#isAnonymous() anonymous} is shown to such a
	 * session as the page it is, because it is written for a visitor without an account.
	 * </p>
	 *
	 * @param config
	 *        The application's view configuration, naming the views a URL may load.
	 * @param pathInfo
	 *        The path below the servlet, its first segment the window name, decoded as
	 *        {@link HttpServletRequest#getPathInfo()} delivers it.
	 * @param anonymous
	 *        Whether the session belongs to no account.
	 * @return What the request displays - see {@link ViewResolution}.
	 */
	public static ViewResolution resolveView(ViewConfig config, String pathInfo, boolean anonymous) {
		return resolveNamedView(config, namedView(pathInfo), anonymous);
	}

	/**
	 * Resolves the view a request displays from the view file its URL names.
	 *
	 * @param namedView
	 *        The view file the URL names, {@code null} where it names none.
	 * @see #resolveView(ViewConfig, String, boolean)
	 */
	private static ViewResolution resolveNamedView(ViewConfig config, String namedView, boolean anonymous) {
		if (anonymous && hasLoginView(config) && !isAnonymousEntryPoint(config, namedView)) {
			return new ViewResolution(config.getLoginView().fullPath(), true);
		}
		if (namedView == null) {
			return new ViewResolution(config.getDefaultView().fullPath(), false);
		}
		if (!isEntryPoint(config, namedView)) {
			return new ViewResolution(null, false);
		}
		return new ViewResolution(ViewLoader.VIEW_BASE_PATH + namedView, false);
	}

	/**
	 * The view file the given path names, or {@code null} where it names none.
	 *
	 * <p>
	 * The path after the window name names a view where it starts with a view file - see
	 * {@link PageAddress#parse(String)}. Everything else is a route path handled by the
	 * {@link RouteManager} inside the default view.
	 * </p>
	 *
	 * @param pathInfo
	 *        The path below the servlet, its first segment the window name, e.g.
	 *        {@code /v1a2b3c/}, {@code /v1a2b3c/app.view.xml}, {@code /v1a2b3c/demo/x.view.xml/a/b}
	 *        or {@code /v1a2b3c/config-editor}.
	 */
	private static String namedView(String pathInfo) {
		String path = pathInfo.substring(1);
		int slashIdx = path.indexOf(PATH_SEPARATOR);
		if (slashIdx < 0) {
			return null;
		}
		return PageAddress.parse(path.substring(slashIdx + 1)).viewFile();
	}

	/**
	 * Whether the given view file may be named by a URL: the default view or a registered entry
	 * point.
	 */
	private static boolean isEntryPoint(ViewConfig config, String view) {
		return isDefaultView(config, view) || entryPoint(config, view) != null;
	}

	/**
	 * Whether the given view file is the {@link ViewConfig#getDefaultView() default view}.
	 */
	private static boolean isDefaultView(ViewConfig config, String view) {
		return view.equals(config.getDefaultView().getPath());
	}

	/**
	 * The page and the route inside it that an address of the view application names.
	 *
	 * <p>
	 * An address is the part of a URL after {@link #ROOT_PATH} and the window name, as the client
	 * shows it. It names a view where it starts with a view file: a path ending in
	 * {@link #VIEW_FILE_SUFFIX}, followed by the end of the address, a {@link #PATH_SEPARATOR} and
	 * the route inside the page, or the query. The view file may consist of several segments.
	 * </p>
	 *
	 * @param viewFile
	 *        The view file the address names, relative to {@link ViewLoader#VIEW_BASE_PATH}, in the
	 *        percent-encoded form of the address, or {@code null} where it names none and the route
	 *        is one of the default view.
	 * @param route
	 *        The route inside the page, without leading slash, with the query; empty for none.
	 */
	public record PageAddress(String viewFile, String route) {

		/**
		 * The {@link #viewFile()} as the configuration names it, its percent-escapes decoded, or
		 * {@code null} where the address names no view file.
		 */
		public String decodedViewFile() {
			return viewFile == null ? null : RouteEncoding.decodeSegment(viewFile);
		}

		/**
		 * Splits the given address into the view file it names and the route behind it.
		 *
		 * @param address
		 *        The address without leading slash, with the query; {@code null} for none.
		 */
		public static PageAddress parse(String address) {
			if (address == null) {
				return new PageAddress(null, "");
			}
			int queryStart = address.indexOf(QUERY_START);
			int pathEnd = queryStart < 0 ? address.length() : queryStart;
			int suffix = address.indexOf(VIEW_FILE_SUFFIX);
			while (suffix >= 0) {
				int fileEnd = suffix + VIEW_FILE_SUFFIX.length();
				if (fileEnd > pathEnd) {
					break;
				}
				if (fileEnd == pathEnd) {
					return new PageAddress(address.substring(0, fileEnd), address.substring(fileEnd));
				}
				if (address.charAt(fileEnd) == PATH_SEPARATOR) {
					return new PageAddress(address.substring(0, fileEnd), address.substring(fileEnd + 1));
				}
				// Only a segment ending in the suffix names a file.
				suffix = address.indexOf(VIEW_FILE_SUFFIX, suffix + 1);
			}
			return new PageAddress(null, address);
		}
	}

	/**
	 * Whether the application answers a session that belongs to no account with a
	 * {@link ViewConfig#getLoginView() login view} instead of showing itself.
	 */
	private static boolean hasLoginView(ViewConfig config) {
		return config.getLoginView() != null;
	}

	/**
	 * Whether the given view file is an entry point a session that belongs to no account is shown.
	 */
	private static boolean isAnonymousEntryPoint(ViewConfig config, String view) {
		if (view == null) {
			return false;
		}
		ViewConfig.EntryPoint entryPoint = entryPoint(config, view);
		return entryPoint != null && entryPoint.isAnonymous();
	}

	/**
	 * The registration of the given view file among the application's entry points, or {@code null}
	 * where it is none.
	 */
	private static ViewConfig.EntryPoint entryPoint(ViewConfig config, String view) {
		for (ViewConfig.EntryPoint entryPoint : config.getEntryPoints()) {
			if (view.equals(entryPoint.getView().getPath())) {
				return entryPoint;
			}
		}
		return null;
	}

	/**
	 * Serves a small HTML page that checks or creates the browser's {@code window.name} and
	 * redirects to a URL containing the window name.
	 *
	 * <p>
	 * {@code window.name} persists across page reloads (F5) within the same tab, but is empty in
	 * newly opened or duplicated tabs. This ensures that each tab gets a unique window name, while
	 * reloads reuse the existing one.
	 * </p>
	 */
	private void writeBootstrapPage(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		String basePath = request.getContextPath() + request.getServletPath();

		TagWriter out = new TagWriter(response.getWriter());
		out.writeContent(HTMLConstants.DOCTYPE_HTML);
		out.beginBeginTag(HTMLConstants.HTML);
		out.endBeginTag();

		out.beginBeginTag(HTMLConstants.HEAD);
		out.endBeginTag();
		out.beginBeginTag(HTMLConstants.META);
		out.writeAttribute("charset", "UTF-8");
		out.endEmptyTag();
		out.endTag(HTMLConstants.HEAD);

		out.beginBeginTag(HTMLConstants.BODY);
		out.endBeginTag();

		out.beginScript();
		out.writeScript("(function() {");
		out.writeScript("var wn = window.name;");
		out.writeScript("if (!wn || wn.charAt(0) !== 'v') {");
		out.writeScript("var arr = new Uint8Array(8);");
		out.writeScript("crypto.getRandomValues(arr);");
		out.writeScript("wn = 'v';");
		out.writeScript("for (var i = 0; i < arr.length; i++) {");
		out.writeScript("wn += arr[i].toString(16).padStart(2, '0');");
		out.writeScript("}");
		out.writeScript("window.name = wn;");
		out.writeScript("}");
		out.writeScript("var base = ");
		out.writeJsString(basePath);
		out.writeScript(";");
		out.writeScript("var routePath = location.pathname.substring(base.length);");
		out.writeScript("if (routePath.charAt(0) !== '/') routePath = '/' + routePath;");
		out.writeScript("var suffix = location.search + location.hash;");
		out.writeScript("window.location.replace(base + '/' + encodeURIComponent(wn) + routePath + suffix);");
		out.writeScript("})();");
		out.endScript();

		out.endTag(HTMLConstants.BODY);
		out.endTag(HTMLConstants.HTML);

		out.flushBuffer();
	}

	/**
	 * Renders the HTML page with the root control using the {@link IReactControl#write} path.
	 *
	 * <p>
	 * The page about to be rendered is the displayed one, so its tree is attached first. Rendering
	 * alone would attach each control as it is serialized, which is too late for anything that acts on
	 * a <em>different</em> control when it attaches: a {@code <slot-content>} registers its
	 * contribution with the slot registry, and the {@code <slot>} placeholder it is routed to may
	 * already have been written - the contribution would then reach the client as a patch after the
	 * first paint instead of being part of it.
	 * </p>
	 *
	 * <p>
	 * Attaching here also makes the display state reliable for views nobody else attaches: only a view
	 * rooted in an {@code <app-shell>} attaches itself, so in e.g. a {@code <window>}-rooted login view
	 * everything keyed on being displayed - a form contributing its commands to the enclosing toolbar,
	 * the registration of routing participants - would silently not happen.
	 * </p>
	 */
	private void renderPage(HttpServletRequest request, HttpServletResponse response,
			ReactControl rootControl, ReactContext context) throws IOException {
		rootControl.attach();

		// The page rendered here is the one the window displays from now on: the unload report of a
		// page it replaces no longer applies to the tree.
		String pageLoad = context.getWindowRegistry().issuePageLoad(context.getWindowName());

		// The display exists now, so the URL the request carries can be adopted: a page rendered into
		// a control tree it already has registers no participants while attaching, and nothing else
		// would hand them the requested route.
		RouteManager routeManager = context.getRouteManager();
		if (routeManager != null) {
			routeManager.resolvePending();
		}

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		String contextPath = context.getContextPath();

		TagWriter out = new TagWriter(response.getWriter());

		UIThemeService themes = UIThemeService.getInstance();

		out.writeContent(HTMLConstants.DOCTYPE_HTML);
		out.beginBeginTag(HTMLConstants.HTML);
		// The language the page is actually rendered in, so that assistive technology and the
		// browser's own text handling follow the user's choice.
		out.writeAttribute("lang", Resources.getCurrentLocale().getLanguage());
		// The theme the user has selected. Left out while there is no selection, which is what makes
		// the page follow the operating system's appearance preference.
		String selectedTheme = themes.getSelectedThemeId();
		if (selectedTheme != null) {
			out.writeAttribute(UIThemeService.THEME_ATTRIBUTE, selectedTheme);
			UITheme theme = themes.getTheme(selectedTheme);
			if (theme != null) {
				themes.writeModeAttribute(out, theme);
			}
		}
		// The density the user has selected, whatever the theme: the first paint is dense already, so
		// that nothing jumps once the theme script has run.
		themes.writeDensityAttribute(out);
		out.endBeginTag();

		out.beginBeginTag(HTMLConstants.HEAD);
		out.endBeginTag();
		out.beginBeginTag(HTMLConstants.META);
		out.writeAttribute("charset", "UTF-8");
		out.endEmptyTag();
		ClientResources clientResources = ClientResources.getInstance();
		// The order of the CSS cascade layers, before any style of the page mentions a layer.
		clientResources.writeLayerOrder(out);
		// The theme switch, put into effect before the first paint: a page carrying no theme yet
		// follows the operating system's appearance preference.
		themes.writeThemeScript(out);
		out.beginBeginTag(HTMLConstants.META);
		out.writeAttribute("name", "viewport");
		out.writeAttribute("content", "width=device-width, initial-scale=1.0");
		out.endEmptyTag();
		out.beginBeginTag(HTMLConstants.TITLE);
		out.endBeginTag();
		// The application title: the page title the display composes is sent once the route of the
		// page is adopted.
		ResKey applicationTitle = applicationTitle();
		out.writeText(
			applicationTitle == null ? DEFAULT_TITLE : Resources.getInstance().getString(applicationTitle));
		out.endTag(HTMLConstants.TITLE);
		// Emit the registered client scripts: classic scripts, the import map, then ES module scripts.
		clientResources.writeScriptRefs(out, contextPath);
		// Emit the design tokens of every registered theme as CSS custom properties, each scoped by
		// the theme attribute of <html> naming the theme in effect, in the engine's cascade layer.
		themes.writeThemeStyles(out);
		// Append the React stylesheets (fonts, icons, component CSS), each in its cascade layer.
		clientResources.writeStyleRefs(out, contextPath);
		out.endTag(HTMLConstants.HEAD);

		out.beginBeginTag(HTMLConstants.BODY);
		out.writeAttribute("data-window-name", context.getWindowName());
		out.writeAttribute("data-context-path", context.getContextPath());
		out.writeAttribute(PAGE_LOAD_ATTRIBUTE, pageLoad);
		out.endBeginTag();

		// Delegate rendering to the control itself. ReactControl.write() outputs a
		// declarative div with data-react-module/data-react-state attributes.
		// The static tl-react-bridge script discovers and mounts these elements.
		rootControl.write(out);

		out.endTag(HTMLConstants.BODY);
		out.endTag(HTMLConstants.HTML);

		out.flushBuffer();

		// The display is complete now: whatever the requested URL still names is not part of it.
		if (routeManager != null) {
			routeManager.finishAdoption();
		}
	}

	@Override
	protected void handleNoSession(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		super.handleNoSession(request, response);

		ThreadContext.inSystemContext(TopLogicServlet.class, () -> {
			Person anonymous = PersonManager.getManager().getAnonymous();
			SessionService.getInstance().loginUser(request, response, anonymous);
		});

		redirectToStartPage(request, response);
	}

}
