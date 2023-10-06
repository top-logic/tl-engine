/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.execution;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Consumer;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.gui.layout.LayoutConfig;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.ContentHandlersRegistry;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.internal.WindowId;
import com.top_logic.layout.scripting.action.ActionChain;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.ConditionalAction;
import com.top_logic.layout.scripting.action.DynamicAction;
import com.top_logic.layout.scripting.action.LoginAction;
import com.top_logic.layout.scripting.action.LogoutAction;
import com.top_logic.layout.scripting.runtime.LiveActionContext;
import com.top_logic.layout.scripting.runtime.ScriptingRuntimeUtil;
import com.top_logic.layout.scripting.runtime.action.ApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.structure.BrowserWindowControl;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContextManager;

/**
 * Executes an {@link ApplicationAction} "live", meaning: In a running system with the tester
 * watching the GUI.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LiveActionExecutor {

	private final BufferingProtocol _protocol;

	private final LiveActionContext _actionContext;

	private InstantiationContext _instantiationContext;

	private MainLayout _mainLayout;

	private final ScriptController _controller;

	private boolean _conditionEvaluated;

	private Object _lastResult;

	private Consumer<LiveActionExecutor> _onStop;

	private Object _processId;

	private boolean _master;

	/**
	 * Creates a {@link LiveActionExecutor}.
	 * 
	 * @param actionContext
	 *        The context to execute actions in.
	 * @param controller
	 *        The {@link ScriptController} that controls the control flow in the script.
	 * @param processId
	 *        The ID to be dispatched e.g. to {@link ScriptController#next()}.
	 * @param master
	 *        Whether this execution should control login/logout actions.
	 * @param onStop
	 *        The callback to invoke when the execution is stopped.
	 */
	public LiveActionExecutor(LiveActionContext actionContext, ScriptController controller, Object processId,
			boolean master, Consumer<LiveActionExecutor> onStop) {
		_processId = processId;
		_master = master;
		_protocol = new BufferingProtocol();
		_actionContext = actionContext;
		_controller = controller;
		_onStop = onStop;
		_instantiationContext = new DefaultInstantiationContext(_protocol);
		_mainLayout = _actionContext.getMainLayout();
	}

	/**
	 * Whether this is the process responsible for spawning new sub-sessions.
	 */
	public boolean isMaster() {
		return _master;
	}

	/**
	 * The node where the execution has started.
	 */
	public TLTreeNode<?> getRoot() {
		return _controller.getRoot();
	}

	/**
	 * Whether the execution has more actions to execute.
	 * 
	 * @see #processNext(DisplayContext)
	 */
	public boolean hasNext() throws WaitTimeoutException {
		return _controller.hasNext(_processId, _master);
	}

	/**
	 * Process the next action.
	 * 
	 * @param displayContext
	 *        The currently active {@link DisplayContext}.
	 * 
	 * @see #hasNext()
	 * 
	 * @throws EnvironmentMismatch
	 *         iff the {@link #current() current} action is executed with wrong environment
	 *         variables, i.e. the script expects environment variables which are not available,
	 *         e.g. wrong user.
	 */
	public void processNext(DisplayContext displayContext) throws EnvironmentMismatch, WaitTimeoutException {
		_actionContext.setDisplayContext(displayContext);
		BrowserWindowControl.Internal.keepDownloads(displayContext);

		while (hasNext()) {
			if (_master) {
				String actionUser = currentUser();
				// The master gets all own actions and all actions that have no logged-in session.
				// Check, whether such action was encountered.
				if (!Utils.equals(_processId, actionUser)) {
					throw ApplicationAssertions.fail(current(), "Missing login action for user '" + actionUser + "'.");
				}
			}
			ApplicationAction action = current();
			if (action instanceof ConditionalAction) {
				ApplicationAction condition = ((ConditionalAction) action).getCondition();
				if (_conditionEvaluated) {
					_conditionEvaluated = false;

					if (isTrue(_lastResult)) {
						next();
					} else {
						// The condition evaluated to false, skip inner actions.
						nextSibling();
					}
					continue;
				} else {
					processAtomic(condition);
					_conditionEvaluated = true;
					return;
				}
			} else if (action instanceof LoginAction) {
				// A login must be executed.
				LoginAction login = (LoginAction) action;
				String newUser = login.getProcessId();
				if (_controller.hasSession(newUser)) {
					throw ApplicationAssertions.fail(current(), "User '" + newUser + "' is already logged in.");
				} else {
					startSession(displayContext, _controller, login, false);
					next();

					// Immediately return to the client to allow executing the window open action.
					throw new WaitTimeoutException();
				}
			} else if (action instanceof LogoutAction) {
				// Note: The actual logout in the controller and the step to the next action must
				// not be interrupted with an other thread's call to hasNext(). Otherwise, the
				// master session would immediately accept the current logout action (that is still
				// active after the logout), since the master session is responsible for all actions
				// that have no logged-in session. This would trigger the error "Missing login
				// action for ..." above.
				synchronized (_controller) {
					// Simulate logout.
					_controller.logoutUser(currentUser());

					// Make sure, this executor no longer consumes any actions.
					_processId = ScriptController.LOGGED_OUT;

					// Mark action as processed.
					next();
				}

				// Stop processing.
				return;
			} else if (action instanceof ActionChain || action instanceof DynamicAction) {
				// ActionChain has no own actions.
				next();
			} else {
				processAtomic(action);
				next();

				// Processing must stop to produce updates on the client.
				return;
			}
		}
	}

	private static boolean isTrue(Object value) {
		return value instanceof Boolean && ((Boolean) value).booleanValue();
	}

	/**
	 * The action node that will be executed next.
	 * 
	 * @see #processNext(DisplayContext)
	 */
	public ApplicationAction current() throws EnvironmentMismatch {
		return _controller.current();
	}

	private String currentUser() {
		return _controller.currentUser();
	}

	private void next() {
		_controller.next();
	}

	private void nextSibling() {
		_controller.nextSibling();
	}

	private void processAtomic(ApplicationAction action) throws EnvironmentMismatch {
		checkCorrectEnvironment(action);

		ScriptingRuntimeUtil.INSTANCE.logActionExecution(action);
		ApplicationActionOp<?> actionOp = _instantiationContext.getInstance(action);
		_protocol.checkErrors();
		_lastResult = actionOp.process(_actionContext, null);
		_mainLayout.globallyValidateModel(_actionContext.getDisplayContext());
	}

	private void checkCorrectEnvironment(ApplicationAction action) throws EnvironmentMismatch {
		TLSubSessionContext subsession = _actionContext.getDisplayContext().getSubSessionContext();
		checkCorrectLocale(subsession, action);
		checkCorrectTimeZone(subsession, action);
	}

	private void checkCorrectTimeZone(SubSessionContext sessionContext, ApplicationAction action)
			throws EnvironmentMismatch {
		TimeZone currentTimezone = sessionContext.getCurrentTimeZone();
		TimeZone actionTZ = action.getTimeZone();
		if (actionTZ != null && !actionTZ.hasSameRules(currentTimezone)) {
			ResKey key = I18NConstants.WRONG_TIMEZONE__ACTION__EXPECTED_TIMEZONE__ACTUAL_TIMEZONE.fill(
				action, currentTimezone.getID(), actionTZ.getID());
			throw new EnvironmentMismatch(key, action, currentTimezone, actionTZ);
		}
	}

	private void checkCorrectLocale(SubSessionContext sessionContext, ApplicationAction action)
			throws EnvironmentMismatch {
		Locale currentLocale = sessionContext.getCurrentLocale();
		Locale actionLocale = action.getLocale();
		if (actionLocale != null && !actionLocale.equals(currentLocale)) {
			ResKey key = I18NConstants.WRONG_LOCALE__ACTION__EXPECTED_LOCALE__ACTUAL_LOCALE.fill(action,
				currentLocale.getDisplayName(currentLocale), actionLocale.getDisplayName(currentLocale));
			throw new EnvironmentMismatch(key, action, currentLocale, actionLocale);
		}
	}

	/**
	 * The error log for the current execution.
	 */
	public BufferingProtocol getLog() {
		return _protocol;
	}

	/**
	 * Stops the currently running execution.
	 */
	public void stop() {
		_controller.stop();

		if (_onStop != null) {
			_onStop.accept(this);
		}
	}

	/**
	 * Whether {@link #stop()} has been called.
	 */
	public boolean isStopped() {
		return _controller.isStopped();
	}

	/**
	 * The position of the {@link #current()} action in the script.
	 */
	public TLTreeNode<?> scriptPosition() {
		return _controller.scriptPosition();
	}

	/**
	 * Starts a fresh script session.
	 *
	 * @param context
	 *        The {@link DisplayContext} of the parent session.
	 * @param login
	 *        The {@link LoginAction} that holds the parameters for the login.
	 * @param master
	 *        Whether this session should monitor for new sessions that must be started.
	 */
	public static void startSession(DisplayContext context, ScriptController controller, LoginAction login,
			boolean master) {
		String loginAccount = login.getAccount();
		String processId = login.getProcessId();
		if (loginAccount == null) {
			loginAccount = processId;
		}
		String password = login.getPassword();
	
		startSession(context, controller, login, processId, loginAccount, password, master);
	}

	/**
	 * Starts a fresh script session.
	 *
	 * @param context
	 *        The {@link DisplayContext} of the parent session.
	 * @param action
	 *        The script context.
	 * @param processId
	 *        to processor ID to start.
	 * @param accountName
	 *        The user account to use for execution.
	 * @param password
	 *        The password that gives access to the script account.
	 * @param master
	 *        Whether this session should monitor for new sessions that must be started.
	 */
	public static void startSession(DisplayContext context, ScriptController controller, ApplicationAction action,
			String processId,
			String accountName,
			String password, boolean master) {
		Person account = Person.byName(accountName);
		if (account == null) {
			throw ApplicationAssertions.fail(action, "Account '" + accountName + "' does not exist.");
		}
		if (account != context.getSubSessionContext().getPerson()) {
			// Requesting script execution in a foreign account.
			String deviceId = account.getAuthenticationDeviceID();
			AuthenticationDevice device =
				TLSecurityDeviceManager.getInstance().getAuthenticationDevice(deviceId);
			try (LoginCredentials login = LoginCredentials.fromUserAndPassword(account, password.toCharArray())) {
				if (!device.authentify(login)) {
					throw ApplicationAssertions.fail(action, "Password of script account does not match.");
				}
			}
		}
	
		startSession(context, controller, processId, account, master);
	}

	/**
	 * Starts a fresh script session.
	 *
	 * @param context
	 *        The {@link DisplayContext} of the parent session.
	 * @param controller
	 *        The script that should be executed.
	 * @param processId
	 *        The user that should execute the actions.
	 * @param account
	 *        The account on behalf of which to execute the script.
	 * @param master
	 *        Whether this session should monitor for new sessions that must be started.
	 */
	public static void startSession(DisplayContext context, ScriptController controller, String processId,
			Person account, boolean master) {
		// Create new subsession.
		ContentHandlersRegistry handlersRegistry = context.getSessionContext().getHandlersRegistry();
		String rootLayoutName;
		try {
			rootLayoutName = LayoutConfig.getDefaultLayout();
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		WindowId subsessionId = new WindowId();
		TLSessionContext session = context.getSessionContext();
		Locale lang = null;
		TimeZone zone = null;
		SubsessionHandler rootHandler =
			handlersRegistry.startLogin(session, rootLayoutName, subsessionId, account, lang, zone);
		TLSubSessionContext subSession = TLContextManager.getSession().getSubSession(subsessionId.getWindowName());
		TLContextManager.initLayoutContext(subSession, rootHandler);
		
		controller.loginUser(processId);

		context.getWindowScope().getTopLevelFrameScope().addClientAction(
			new JSSnipplet((c, out) -> {
				out.append("window.open(");
				TagUtil.beginJsString(out);
				LayoutUtils.appendFullLayoutServletURL(context, out);
				out.append('/');
				out.append(subsessionId.getEncodedForm());
				TagUtil.endJsString(out);
				out.append(", ");
				TagUtil.writeJsString(out, subsessionId.getWindowName());
				out.append(");");
			}));

		StartReplay startReplay = StartReplay.newInstance();
		startReplay.setUserName(processId);
		startReplay.setController(controller);
		startReplay.setMaster(master);

		rootHandler.setOnLoad(subsession -> {
			MainLayout mainLayout = subsession.getMainLayout();
			mainLayout.registerCommand(startReplay);
			HTMLFragment onLoad = new HTMLFragment() {
				@Override
				public void write(DisplayContext renderContext, TagWriter out) throws IOException {
					HTMLUtil.writeJavaScriptContent(out, startReplay.getID() + "();");

					// Just once after login.
					mainLayout.removeOnLoad(this);
				}
			};
			mainLayout.addOnLoad(onLoad);
		});
	}

	/**
	 * {@link CommandHandler} directly executed after logging in the new session to start the
	 * replay.
	 */
	public static class StartReplay extends AbstractCommandHandler {
		private static final String COMMAND_ID = "startReplay";

		private String _userName;

		private ScriptController _controller;

		private boolean _master;

		/**
		 * The central action dispatching logic.
		 */
		public void setController(ScriptController controller) {
			_controller = controller;
		}

		/**
		 * Whether the started session is the script execution master.
		 * 
		 * <p>
		 * The script execution master logs in new sessions, if actions must be executed for which
		 * no current executor session is active.
		 * </p>
		 */
		public void setMaster(boolean master) {
			_master = master;
		}

		/**
		 * The user name of actions that should be executed in this session.
		 */
		public void setUserName(String userName) {
			_userName = userName;
		}

		/**
		 * Creates a {@link StartReplay} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public StartReplay(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
				Map<String, Object> arguments) {
			// Command is only called once during startup.
			component.getMainLayout().unregisterCommand(COMMAND_ID);

			LiveActionContext actionContext = new LiveActionContext(context, component);
			LiveActionExecutor executor = new LiveActionExecutor(actionContext, _controller, _userName, _master, null);

			return new ScriptDriver(component.getEnclosingFrameScope(), executor).next(context);
		}

		/**
		 * Creates a {@link StartReplay} command.
		 */
		public static StartReplay newInstance() {
			return newInstance(StartReplay.class, COMMAND_ID);
		}

	}
}
