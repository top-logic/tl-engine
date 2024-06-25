/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.internal;

import java.io.IOError;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.services.simpleajax.AJAXOutOfSequenceException;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.MaxRequestNumberException;
import com.top_logic.base.services.simpleajax.RequestLock;
import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.base.services.simpleajax.RequestStore;
import com.top_logic.base.services.simpleajax.RequestTimeoutException;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.gui.layout.LayoutConfig;
import com.top_logic.layout.CompositeContentHandler;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.ContentHandlersRegistry;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.component.I18NConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.Action;
import com.top_logic.mig.html.layout.BookmarkNotFoundNotification;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.LayoutComponentScope;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.ModelEventListener;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.CommandHandlerUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.commandhandlers.DefaultBookmarkHandler;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.util.ActionQueue;
import com.top_logic.util.DefaultValidationQueue;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.ToBeValidated;
import com.top_logic.util.TopLogicServlet;
import com.top_logic.util.ValidationQueue;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ContentHandler} and {@link LayoutContext} implementation of a subsession.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SubsessionHandler extends WindowHandler implements LayoutContext {

	private final String _rootLayoutName;

	private ScheduledFuture<?> _cancelTimer;

	private Consumer<SubsessionHandler> _onLoad;

	private MainLayout _mainLayout;

	/** @see #getValidationQueueDispatch() */
	private DefaultValidationQueue _validationActionQueue;

	/**
	 * Whether global updates are currently enabled.
	 * 
	 * @see #checkUpdate(Object)
	 */
	private boolean updateEnabled;

	/**
	 * The last illegal updater.
	 * 
	 * @see #checkUpdate(Object)
	 */
	private Object lastIllegalUpdateBy;

	/**
	 * The {@link RequestLock} to synchronize the complete {@link LayoutComponent} tree of a user
	 * session against concurrent requests.
	 */
	private final RequestLock _lock = RequestLockFactory.getInstance().createLock();

	/**
	 * Creates a {@link SubsessionHandler}.
	 * 
	 * @param urlContext
	 *        The root handler for the session.
	 * @param subsessionId
	 *        The identifier for this subsession.
	 * @param rootLayoutName
	 *        The location to load the component root from.
	 * @param cancelTimer
	 *        Timer for a login in progress.
	 */
	public SubsessionHandler(CompositeContentHandler urlContext, WindowId subsessionId, String rootLayoutName,
			ScheduledFuture<?> cancelTimer) {
		super(urlContext, subsessionId);

		_rootLayoutName = rootLayoutName;
		_cancelTimer = cancelTimer;
		updateEnabled = false;
	}

	/**
	 * Set an on-load hook that is executed after the layout is loaded in the new session.
	 */
	public void setOnLoad(Consumer<SubsessionHandler> onLoad) {
		_onLoad = onLoad;
	}

	@Override
	public RequestLock getLock() {
		return _lock;
	}

	@Override
	protected void deliverLocalContent(DisplayContext context, String id, URLParser url) throws IOException {
		finishLogin(context);

		internalHandleRequestLock(context, id, url);
	}

	private void internalHandleRequestLock(DisplayContext context, String id, URLParser url) throws IOException {
		Integer key = Integer.valueOf(_lock.reset());

		try {
			_lock.enterWriter(key);
			try {
				internalHandleBookmark(context, id, url);
			} finally {
				_lock.exitWriter(key);
			}
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		} catch (AJAXOutOfSequenceException ex) {
			handleTimeout(context);
		} catch (RequestTimeoutException ex) {
			handleTimeout(context);
		} catch (MaxRequestNumberException ex) {
			handleTimeout(context);
		}
	}

	private void internalHandleBookmark(DisplayContext context, String id, URLParser url) throws IOException {
		MainLayout mainLayout = getMainLayout();

		HttpServletRequest request = context.asRequest();
		Map<String, ?> arguments = request.getParameterMap();
		if (!arguments.isEmpty()) {
			CommandHandler handler =
				CommandHandlerFactory.getInstance().getHandler(
					DefaultBookmarkHandler.COMMAND_RESOLVE_BOOKMARK);
			if (handler != null) {
				// Note: Argument map must be copied, because the original one
				// contains
				// array instances containing single values.
				HashMap<String, Object> gotoArguments = new HashMap<>();
				for (String paramName : arguments.keySet()) {
					if (GotoHandler.COMMAND_PARAM_COMPONENT.equals(paramName)) {
						// convert given component id from string to component name
						String compName = request.getParameter(paramName);
						if (!StringServices.isEmpty(compName)) {
							try{
								gotoArguments.put(paramName, ComponentName.newConfiguredName(paramName, compName));
							}catch(ConfigurationException ce){
								Logger.warn("Bookmark link contained invalid component name",ce, this);
							}
						}
					} else {
						gotoArguments.put(paramName, request.getParameter(paramName));
					}
				}
				gotoArguments.remove(ContentHandlersRegistry.LAYOUT_PARAMETER);
				if (!gotoArguments.isEmpty()) {
					boolean before = enableUpdate(true);
					try {
						try {
							CommandHandlerUtil.handleCommand(handler, context, mainLayout, gotoArguments);
						} catch (TopLogicException ex) {
							// Goto object not found. Perhaps it was deleted
							notifyBookmarkNotFound(mainLayout, ex.getErrorKey());
						}
						mainLayout.globallyValidateModel(context);
					} finally {
						enableUpdate(before);
					}
				}
			}

			// Allow rendering without additional window name check.
			provideRenderToken();

			// Drop parameters.
			context.asResponse().sendRedirect(getURL(context).getURL());
			return;
		}

		// Send global refresh event
		if (!mainLayout.isInvalid()) {
			validateMainLayout(context, mainLayout);
		}

		reallyDeliverLocalContent(context, id, url);
	}

	/**
	 * Called to inform the user that the bookmark object could not be found
	 * 
	 * @param bookmarkArguments
	 *        the arguments used to resolve the bookmark.
	 */
	private void notifyBookmarkNotFound(MainLayout mainLayout, Object bookmarkArguments) {
		final String commandId = "bookmarkNotFound_" + mainLayout.getEnclosingFrameScope().createNewID();
		Command continuation = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				mainLayout.unregisterCommand(commandId);
				return HandlerResult.DEFAULT_RESULT;
			}
		};
		BookmarkNotFoundNotification notification =
			BookmarkNotFoundNotification.newInstance(commandId, bookmarkArguments, continuation);
		mainLayout.registerCommand(notification);
		HTMLFragment onLoad = new HTMLFragment() {
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				HTMLUtil.writeJavaScriptContent(out, notification.getID() + "();");

				// Only once after login, not after any further repaint.
				mainLayout.removeOnLoad(this);
			}
		};
		mainLayout.addOnLoad(onLoad);
	}

	private void validateMainLayout(DisplayContext context, MainLayout mainLayout) {
		boolean before = enableUpdate(true);
		try {
			/* Ensure that the MainLayout is context component, because the request is actually
			 * processed by MainLayout. */
			LayoutComponent oldComponent = MainLayout.getComponent(context);
			LayoutUtils.setContextComponent(context, mainLayout);
			try {
				mainLayout.doFireModelEvent(null, mainLayout, ModelEventListener.GLOBAL_REFRESH);
				mainLayout.globallyValidateModel(context);
			} finally {
				LayoutUtils.setContextComponent(context, oldComponent);
			}

			// Drop all pending yet unacknowledged requests, since after a complete redraw, there
			// is no chance to get an acknowledgment.
			SubSessionContext subSession = context.getSubSessionContext();
			RequestStore store = subSession.get(RequestStore.SESSION_KEY);
			if (store != null) {
				store.clear();
			}
		} catch (Throwable ex) {
			InfoService.logError(context, I18NConstants.ERROR_VIEW_CREATION, ex, SubsessionHandler.class);
		} finally {
			enableUpdate(before);
		}
	}

	private void reallyDeliverLocalContent(DisplayContext context, String id, URLParser url) throws IOException {
		super.deliverLocalContent(context, id, url);
	}

	@Override
	protected void handleCompositeContent(DisplayContext context, String id, URLParser url) throws IOException {

		String resourceName = context.asRequest().getRequestURI();
		try {
			Integer key = _lock.enterReader(resourceName);
			try {
				checkUpdateDisabled();
				super.handleCompositeContent(context, id, url);
			} finally {
				_lock.exitReader(key, resourceName);
			}
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		} catch (MaxRequestNumberException ex) {
			handleTimeout(context);
		} catch (RequestTimeoutException ex) {
			handleTimeout(context);
		}
	}

	private void handleTimeout(DisplayContext context) throws IOException {
		try {
			HttpServletResponse response = context.asResponse();
			response.setStatus(HttpURLConnection.HTTP_UNAVAILABLE);
			HttpServletRequest request = context.asRequest();
			String buzzyPage = ApplicationPages.getInstance().getBuzzyPage();
			request.getRequestDispatcher(buzzyPage).forward(request, response);
		} catch (ServletException ex1) {
			throw new IOException(ex1);
		}
	}

	@Override
	public MainLayout getMainLayout() {
		return _mainLayout;
	}

	@Override
	public void notifyInvalid(ToBeValidated o) {
		assert isInCommandPhase() : "Deferred validation must only happen in a command thread.";
		getValidationQueueDispatch().notifyInvalid(o);
	}

	@Override
	public HandlerResult runValidation(DisplayContext context) {
		return getValidationQueueDispatch().runValidation(context);
	}

	@Override
	public void enqueueAction(Action action) {
		getActionQueueDispatch().enqueueAction(action);
	}

	@Override
	public void forceQueueing() {
		getActionQueueDispatch().forceQueueing();
	}

	@Override
	public void processActions() {
		getActionQueueDispatch().processActions();
	}

	/**
	 * The actual implementation of {@link ActionQueue} to use
	 */
	private ActionQueue<Action> getActionQueueDispatch() {
		initializeQueue();
		return _validationActionQueue;
	}

	/**
	 * The actual implementation of {@link ValidationQueue} to use
	 */
	private DefaultValidationQueue getValidationQueueDispatch() {
		initializeQueue();
		return _validationActionQueue;
	}

	private void initializeQueue() {
		// do it lazy as validation queue is only necessary in command request not during rendering
		if (_validationActionQueue == null) {
			_validationActionQueue = new DefaultValidationQueue();
		}
	}

	@Override
	public final void checkUpdate(Object updater) {
		if (!this.updateEnabled) {
			if (updater != this.lastIllegalUpdateBy) {
				errorStateModification();
			}
			this.lastIllegalUpdateBy = updater;
		}
	}

	/**
	 * Reports a state modification error.
	 * 
	 * <p>
	 * A state modification is only allowed during the command phase of an interaction. This is
	 * necessary to ensure that updates have a chance to be transported back to the client.
	 * </p>
	 */
	protected void errorStateModification() {
		Logger.error("State modification during rendering.", new Exception("Stack trace."), MainLayout.class);
	}

	/**
	 * Enables or disables global state changes in this session's layout.
	 * 
	 * @param enabled
	 *        Whether to enable or disable updates.
	 * @return The former enable state.
	 */
	public boolean enableUpdate(boolean enabled) {
		boolean enabledBefore = this.updateEnabled;

		if (enabled != enabledBefore) {
			this.updateEnabled = enabled;
			this.lastIllegalUpdateBy = null;
		}

		return enabledBefore;
	}

	private final void checkUpdateDisabled() {
		if (this.updateEnabled) {
			throw new IllegalStateException("Must have updates disabled.");
		}
	}

	private synchronized void finishLogin(DisplayContext displayContext) {
		if (_mainLayout == null) {
			StopWatch watch = StopWatch.createStartedWatch();
			if (_cancelTimer != null) {
				_cancelTimer.cancel(false);
				_cancelTimer = null;
			}

			InstantiationContext instantiationContext = new DefaultInstantiationContext(SubsessionHandler.class);
			instantiationContext.deferredReferenceCheck(() -> {
				try {
					setLoadLayoutTime(displayContext);
					/* SubSession is installed in startLogin() (during creation of this handler). */
					TLSubSessionContext subSession = TLContextManager.getSubSession();
					Config config = LayoutStorage.getInstance().getOrCreateLayoutConfig(_rootLayoutName);
					_mainLayout = (MainLayout) LayoutUtils.createComponentFromXML(instantiationContext, null,
						_rootLayoutName, false, config);
					instantiationContext.checkErrors();
					_mainLayout.setLocation(_rootLayoutName);
					MainLayout.initializeMainLayout(instantiationContext, displayContext, _mainLayout, this,
						subSession);
					// Note: This breaks the login, if references cannot be resolved due to previous
					// errors.
					//
					// instantiationContext.checkErrors();
				} catch (ConfigurationException ex) {
					throw new RuntimeException(ex);
				} catch (IOException ex) {
					throw new IOError(ex);
				}
				return null;
			});

			LayoutComponentScope componentHandler = _mainLayout.getEnclosingFrameScope();
			componentHandler.setUrlContext(this);
			DebugHelper.logTiming(displayContext.asRequest(), "Initializing session", watch, 500,
				SubsessionHandler.class);

			if (_onLoad != null) {
				_onLoad.accept(this);
				_onLoad = null;
			}
		}
	}

	private static void setLoadLayoutTime(DisplayContext displayContext) {
		final long loadLayoutTime = ApplicationConfig.getInstance().getConfig(LayoutConfig.class).getLoadLayoutTime();
		displayContext.asRequest().setAttribute(TopLogicServlet.MAX_SERVICE_ATTR, loadLayoutTime);
	}

	@Override
	protected void handleDeregister(ContentHandler handler) {
		super.handleDeregister(handler);

		// After disposing the component tree, the subsession has to be disposed, too.
		getUrlContext().deregisterContentHandler(this);
	}

	@Override
	protected String getClonedWindowLocation(DisplayContext context) {
		return getSubsessionLocation(context);
	}

	/**
	 * The configured location to redirect to, if a cloned application window is detected.
	 * 
	 * @see LayoutConfig#getSubsessionLocation()
	 */
	public static String getSubsessionLocation(DisplayContext context) {
		final LayoutConfig config = ApplicationConfig.getInstance().getConfig(LayoutConfig.class);
		return context.getContextPath() + "/" + config.getSubsessionLocation();
	}

	@Override
	public boolean isInCommandPhase() {
		return updateEnabled;
	}

	@FrameworkInternal
	public boolean hasPendingToBeValidated() {
		return _validationActionQueue.hasPendingToBeValidated();
	}

}
