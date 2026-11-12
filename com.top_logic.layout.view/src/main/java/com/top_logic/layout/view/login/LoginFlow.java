/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.Login.AccountLockedException;
import com.top_logic.base.accesscontrol.Login.InMaintenanceModeException;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.protocol.PageNavigationEvent;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.ViewServlet;
import com.top_logic.layout.view.command.OpenDialogAction;
import com.top_logic.util.error.TopLogicException;

/**
 * A login of the React UI in progress: an account that has authenticated itself and runs the
 * {@link LoginStep}s before its session starts.
 *
 * <p>
 * A login entry - the password {@link LoginAction}, a passwordless login with a passkey - creates
 * the flow for the authenticated account and {@link #begin(ReactContext) begins} it. The flow runs
 * the first {@link LoginStep#appliesTo(LoginFlow) applying} step; the step's dialog
 * {@link #finish(ReactContext, Class, Person) finishes} it, which runs the next applying step. After
 * the last step, the account is logged in within the window, and only then.
 * </p>
 *
 * <p>
 * The flow is held on the server, by the sub-session of the browser window the login happens in;
 * nothing of it is sent to the client. Which step is due is the flow's own position, so a client
 * cannot skip a step: {@link #finish(ReactContext, Class, Person)} finishes only the step that is
 * due, and only when it is of the kind the finishing action proves. A new login in the window
 * replaces the flow; closing the dialog of the due step - cancelling the login - discards it.
 * </p>
 *
 * <p>
 * What the login entry knows of the authentication that the account alone does not tell - e.g.
 * that the password used has expired, or that the account has already proven a second factor - is
 * set as property of the flow before it begins, for the steps to read.
 * </p>
 */
public class LoginFlow extends LazyTypedAnnotatable {

	/**
	 * The flow of the window, on the window's sub-session.
	 */
	private static final Property<LoginFlow> FLOW = TypedAnnotatable.property(LoginFlow.class, "loginFlow");

	/**
	 * Property of a {@link LoginFlow} naming the page the window loads after the login, an address
	 * as the browser loads it (e.g. the context path followed by {@link ViewServlet#ROOT_PATH}).
	 *
	 * <p>
	 * Without a value, the window loads the page it shows again, so that the user gets the page
	 * the login was asked for on. A login entry whose page is not meant for the user logged in -
	 * e.g. the page on which an invited person creates the account - names another one.
	 * </p>
	 */
	public static final Property<String> LANDING_PAGE = TypedAnnotatable.property(String.class, "landingPage");

	private final Person _account;

	private final List<LoginStep> _steps;

	/**
	 * Index of the due step in {@link #_steps}, <code>-1</code> before the first.
	 */
	private int _current = -1;

	/**
	 * The dialog of the due step, <code>null</code> when it has none open (yet).
	 */
	private DialogHandle _dialog;

	/**
	 * Creates a {@link LoginFlow} running the {@link LoginStepsConfig#configured() configured} steps.
	 *
	 * @param account
	 *        The account that has authenticated itself.
	 */
	public LoginFlow(Person account) {
		this(account, LoginStepsConfig.configured());
	}

	/**
	 * Creates a {@link LoginFlow}.
	 *
	 * @param account
	 *        The account that has authenticated itself.
	 * @param steps
	 *        The steps to run, in their order.
	 */
	public LoginFlow(Person account, List<? extends LoginStep> steps) {
		_account = account;
		_steps = List.copyOf(steps);
	}

	/**
	 * The account logging in.
	 */
	public Person getAccount() {
		return _account;
	}

	/**
	 * The step that is due, <code>null</code> before the flow has begun and after its last step.
	 */
	public LoginStep getCurrentStep() {
		return _current >= 0 && _current < _steps.size() ? _steps.get(_current) : null;
	}

	/**
	 * Begins this flow in the window of the given context: replaces a flow begun before in the
	 * window, and runs the first applying step or, when none applies, logs the account in.
	 *
	 * <p>
	 * The login entry has checked {@link #checkMayLogIn(Person) whether the account may log in} at
	 * all before, so that no step is shown to an account that is refused anyway.
	 * </p>
	 *
	 * @param context
	 *        The context of the display the login is in.
	 */
	public void begin(ReactContext context) {
		holder().set(FLOW, this);
		advance(context);
	}

	/**
	 * The flow in progress in the window of the current interaction, <code>null</code> if no login
	 * is in progress there.
	 */
	public static LoginFlow pending() {
		TypedAnnotatable holder = subSession();
		return holder == null ? null : holder.get(FLOW);
	}

	/**
	 * The account of the login in progress, when its due step is of the given kind.
	 *
	 * @param kind
	 *        The kind of step the caller acts for.
	 * @return The account, or <code>null</code> when no login is in progress or its due step is not
	 *         of the given kind.
	 */
	public static Person pendingAccount(Class<? extends LoginStep> kind) {
		LoginFlow flow = pending();
		if (flow == null || !kind.isInstance(flow.getCurrentStep())) {
			return null;
		}
		return flow.getAccount();
	}

	/**
	 * Finishes the due step of the login in progress and runs the next one, or logs the account in
	 * after the last step.
	 *
	 * @param context
	 *        The context of the dialog the login is in.
	 * @param kind
	 *        The kind of step the calling action has completed: the action proves what a step of this
	 *        kind asks for, and nothing else.
	 * @param account
	 *        The account the calling action has acted for, <code>null</code> if it acted for the
	 *        account of the flow without naming one.
	 * @throws TopLogicException
	 *         If no login is in progress, its due step is not of the given kind, or it is the login
	 *         of another account. The login in progress, if any, is discarded.
	 */
	public static void finish(ReactContext context, Class<? extends LoginStep> kind, Person account) {
		LoginFlow flow = pending();
		if (flow == null) {
			Logger.warn("Login step '" + kind.getName() + "' finished without a login in progress.",
				LoginFlow.class);
			throw new TopLogicException(I18NConstants.LOGIN_FAILED);
		}
		LoginStep current = flow.getCurrentStep();
		if (!kind.isInstance(current) || (account != null && !account.equals(flow.getAccount()))) {
			Logger.warn("Login of '" + flow.getAccount().getName() + "' refused: step '" + kind.getName()
				+ "' finished while step '" + (current == null ? null : current.getClass().getName())
				+ "' is due.", LoginFlow.class);
			flow.discard();
			throw new TopLogicException(I18NConstants.LOGIN_FAILED);
		}
		flow.advance(context);
	}

	/**
	 * Closes the current top dialog and opens the given view as a modal dialog seeded with the given
	 * channel values: the way a step of the login opens its dialog, taking over from the dialog
	 * before it.
	 *
	 * <p>
	 * The dialog does not close on a click on the backdrop, so that an accidental click does not
	 * abort the login. Closing it in any other way than by finishing its step discards the login.
	 * </p>
	 *
	 * @param context
	 *        The context of the dialog the login is in.
	 * @param view
	 *        The dialog view, relative to {@link ViewLoader#VIEW_BASE_PATH}.
	 * @param channels
	 *        The values of the dialog's channels, by channel name, e.g. the account on the
	 *        {@link LoginAction#ACCOUNT_CHANNEL} channel.
	 * @throws TopLogicException
	 *         If no login is in progress.
	 */
	public static void replaceDialog(ReactContext context, String view, Map<String, ?> channels) {
		LoginFlow flow = pending();
		if (flow == null) {
			throw new TopLogicException(I18NConstants.LOGIN_FAILED);
		}
		flow.openDialog(context, view, channels);
	}

	private void openDialog(ReactContext context, String view, Map<String, ?> channels) {
		// The dialog closed now belongs to a step that is done.
		_dialog = null;
		closeTopDialog(context);

		DialogHandle[] opened = new DialogHandle[1];
		opened[0] = OpenDialogAction.openDialog(context, ViewLoader.VIEW_BASE_PATH + view, false, channels,
			Collections.emptyList(), result -> {
				if (opened[0] != null && opened[0] == _dialog) {
					// The dialog of the due step closed without the step being finished.
					discard();
				}
			});
		_dialog = opened[0];
	}

	/**
	 * Runs the next applying step, or logs the account in after the last one.
	 */
	private void advance(ReactContext context) {
		try {
			for (int next = _current + 1; next < _steps.size(); next++) {
				LoginStep step = _steps.get(next);
				if (step.appliesTo(this)) {
					_current = next;
					step.start(context, this);
					return;
				}
			}
			_current = _steps.size();
			discard();
			complete(context);
		} catch (RuntimeException ex) {
			discard();
			throw ex;
		}
	}

	/**
	 * Removes this flow from its window, so that nothing finishes a step of it any more.
	 */
	private void discard() {
		_dialog = null;
		TypedAnnotatable holder = subSession();
		if (holder != null && holder.get(FLOW) == this) {
			holder.reset(FLOW);
		}
	}

	private static TypedAnnotatable holder() {
		TypedAnnotatable holder = subSession();
		if (holder == null) {
			throw new IllegalStateException("A login requires the sub-session of a window.");
		}
		return holder;
	}

	/**
	 * Logs the account in after the last step: logs it in within the current browser window, closes
	 * the top dialog, and rebuilds the window for the user.
	 *
	 * <p>
	 * The login applies to the window it happens in, see
	 * {@link SessionService#loginUserInWindow(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, Person)}:
	 * the session is kept, other windows of the session keep their user, and a window opened later
	 * starts with the user logged in here. The changed session ID reaches the browser with the
	 * answer to the current command. The window's tree, built for the previous user, is discarded
	 * and the window is reloaded ({@link ReactWindowRegistry#rebuildWindow(String)}), which renders
	 * it anew for the user; with a {@link #LANDING_PAGE} the window loads that page instead.
	 * </p>
	 *
	 * <p>
	 * Re-checks {@link Login#checkMayLogIn(Person) whether the account may log in} before the
	 * login: the steps can complete long after the authentication, so maintenance mode may have
	 * been activated or the account locked in between.
	 * </p>
	 *
	 * @param context
	 *        The context of the dialog the login is in.
	 */
	protected void complete(ReactContext context) {
		String userName = _account.getName();
		Person account = Person.byName(userName);
		checkMayLogIn(account);

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		SessionService.getInstance().loginUserInWindow(displayContext.asRequest(), displayContext.asResponse(),
			account);

		closeTopDialog(context);
		String landingPage = get(LANDING_PAGE);
		if (landingPage == null) {
			context.getWindowRegistry().rebuildWindow(context.getWindowName());
		} else {
			// A page of its own builds the tree of the window anew for the user.
			context.getSSEQueue().enqueue(PageNavigationEvent.create().setUrl(landingPage));
		}
	}

	/**
	 * Checks whether the given authenticated account may log in, see
	 * {@link Login#checkMayLogIn(Person)}.
	 *
	 * <p>
	 * Must be called only after the identity of the user is established, since the reason tells
	 * the state of the account.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         With the reason to tell the user, if the account may not log in.
	 */
	public static void checkMayLogIn(Person account) {
		String userName = account.getName();
		try {
			Login.getInstance().checkMayLogIn(account);
		} catch (InMaintenanceModeException ex) {
			Logger.info("Login of '" + userName + "' rejected: maintenance mode active.", LoginFlow.class);
			throw new TopLogicException(Login.getMaintenanceMessage(userName), ex);
		} catch (AccountLockedException ex) {
			Logger.info("Login of '" + userName + "' rejected: " + ex.getMessage(), LoginFlow.class);
			throw new TopLogicException(ex.getReason(), ex);
		}
	}

	private static void closeTopDialog(ReactContext context) {
		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager != null) {
			dialogManager.closeTopDialog(DialogResult.cancelled());
		}
	}

	/**
	 * The sub-session of the current interaction, which is the sub-session of the window a React
	 * command runs for; <code>null</code> outside an interaction.
	 */
	private static TypedAnnotatable subSession() {
		return ThreadContextManager.getSubSession();
	}

}
