/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.Objects;
import java.util.function.Consumer;

import com.top_logic.base.accesscontrol.IdentityVerificationHandler;
import com.top_logic.base.accesscontrol.IdentityVerifications;
import com.top_logic.base.accesscontrol.VerificationResult;
import com.top_logic.base.accesscontrol.VerifiedIdentity;
import com.top_logic.basic.Logger;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.scripting.ReactWindowReplay;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.command.Continuation;

/**
 * A command chain suspended in a browser window until an external identity provider answers, as
 * the {@link IdentityVerificationHandler} of the verification it waits for.
 *
 * <p>
 * The answer of the provider arrives on the thread of a request of another window of the same
 * session. Whatever it is, it is carried into the window the chain waits in: the dialog waiting
 * there is closed, and the chain either {@link Continuation#resume(Object) resumes} with its input
 * unchanged, or {@link Continuation#abort() aborts} with the reason shown in that window. That holds
 * for an identity the {@link #ChainAwaitingIdentity(ReactContext, IdentityVerificationHandler,
 * Object, Continuation, Class, Consumer) decision} refuses and for a verification that
 * {@link IdentityVerificationHandler#expired(Person) has expired} before the answer came, so that
 * the window that took the provider's sign-in is never the place the user learns the result in.
 * </p>
 *
 * @see VerifyIdentityAction
 * @see IdentifyExternallyAction
 */
public class ChainAwaitingIdentity implements IdentityVerificationHandler {

	/**
	 * Names the operation a failure is reported for: the command chain taken up again after the
	 * external identity provider has answered.
	 */
	private static final String RESUME_DESCRIPTION = "Continuing the command chain after the identity confirmation";

	/**
	 * Names the operation a failure of the decision is reported for.
	 */
	private static final String DECISION_DESCRIPTION = "Taking over the identity confirmed by the identity provider";

	private final ReactContext _context;

	private final IdentityVerificationHandler _decision;

	private final Object _input;

	private final Continuation _continuation;

	private final Class<?> _caller;

	private final Consumer<Runnable> _inWindow;

	private volatile Runnable _closePrompt;

	/**
	 * Creates a {@link ChainAwaitingIdentity}.
	 *
	 * @param context
	 *        The React context of the window the chain is suspended in.
	 * @param decision
	 *        What the confirmed identity means; the chain resumes when it accepts the identity.
	 * @param input
	 *        The value the chain continues with.
	 * @param continuation
	 *        The suspended chain.
	 * @param caller
	 *        The action that suspended the chain, under which failures are logged.
	 * @param inWindow
	 *        Runs an action in the window the chain waits in, see
	 *        {@link #runInWindow(ReactContext, Runnable)}.
	 */
	public ChainAwaitingIdentity(ReactContext context, IdentityVerificationHandler decision, Object input,
			Continuation continuation, Class<?> caller, Consumer<Runnable> inWindow) {
		_context = Objects.requireNonNull(context, "No context.");
		_decision = Objects.requireNonNull(decision, "No decision.");
		_input = input;
		_continuation = Objects.requireNonNull(continuation, "No continuation.");
		_caller = Objects.requireNonNull(caller, "No caller.");
		_inWindow = Objects.requireNonNull(inWindow, "No window runner.");
	}

	/**
	 * Sets what closes the dialog the chain waits in.
	 *
	 * <p>
	 * The dialog is opened after the verification has been
	 * {@link IdentityVerifications#register(Person, IdentityVerificationHandler) registered}, since it
	 * needs the token; the answer arrives on a later request, never before the dialog is set.
	 * </p>
	 */
	public void setClosePrompt(Runnable closePrompt) {
		_closePrompt = closePrompt;
	}

	/**
	 * Hands the confirmed identity to the decision and carries its answer into the window the chain
	 * waits in.
	 *
	 * <p>
	 * A failure of the decision itself is a refusal as well: it is reported in the window the way a
	 * failed command is, and the result names its message (or a generic one) for the case that the
	 * window is gone.
	 * </p>
	 *
	 * @return The answer of the decision, {@link VerificationResult#delivered() delivered} when it
	 *         has reached the window.
	 */
	@Override
	public VerificationResult handle(Person expected, VerifiedIdentity identity) {
		VerificationResult result;
		try {
			result = _decision.handle(expected, identity);
		} catch (RuntimeException ex) {
			I18NFailure i18nFailure = CommandErrors.findI18NFailure(ex);
			VerificationResult refusal = VerificationResult.refused(
				i18nFailure != null ? i18nFailure.getErrorKey() : I18NConstants.ERROR_IDENTIFICATION_FAILED);
			boolean delivered = inWindow(() -> {
				closePrompt();
				CommandErrors.report(_context.getErrorSink(), ex, DECISION_DESCRIPTION, _caller);
				_continuation.abort();
			});
			if (!delivered) {
				CommandErrors.log(ex, DECISION_DESCRIPTION, _caller);
				return refusal;
			}
			return refusal.delivered();
		}
		return answer(result);
	}

	@Override
	public boolean expired(Person expected) {
		return answer(VerificationResult.expired()).isDelivered();
	}

	/**
	 * Carries the given result into the window the chain waits in: closes the dialog there, and
	 * resumes the chain for an accepted identity, or shows the message of the result and aborts the
	 * chain otherwise.
	 *
	 * <p>
	 * Runs on the thread of the request that came back from the provider. What fails in the window
	 * is reported or logged there: the request answers for the identity alone.
	 * </p>
	 *
	 * @return The given result, {@link VerificationResult#delivered() delivered} when it has reached
	 *         the window.
	 */
	public VerificationResult answer(VerificationResult result) {
		boolean delivered = inWindow(() -> {
			closePrompt();
			if (result.isVerified()) {
				resume();
			} else {
				refuse(result.getMessage());
			}
		});
		return delivered ? result.delivered() : result;
	}

	private void closePrompt() {
		Runnable closePrompt = _closePrompt;
		if (closePrompt != null) {
			closePrompt.run();
		}
	}

	private void refuse(ResKey message) {
		ViewMessages.error(_context, message);
		_continuation.abort();
	}

	/**
	 * Continues the chain, reporting what it throws in the window the chain belongs to.
	 *
	 * <p>
	 * The failure travels the way a failed command travels: an internal error is logged as one and
	 * shown generically, a user-level failure carries its own message into the window's snackbar,
	 * see {@link CommandErrors#report(ErrorSink, Throwable, String, Class)}.
	 * </p>
	 */
	private void resume() {
		try {
			_continuation.resume(_input);
		} catch (Throwable ex) {
			CommandErrors.report(_context.getErrorSink(), ex, RESUME_DESCRIPTION, _caller);
		}
	}

	/**
	 * Runs the given action in the window the chain waits in.
	 *
	 * @return Whether the action has run there.
	 */
	private boolean inWindow(Runnable action) {
		boolean[] ran = { false };
		try {
			_inWindow.accept(() -> {
				ran[0] = true;
				action.run();
			});
		} catch (Throwable ex) {
			Logger.error("The answer of the identity provider could not be carried into window '"
				+ _context.getWindowName() + "'.", ex, _caller);
		}
		return ran[0];
	}

	/**
	 * Runs the given action in the window of the given context, from whatever thread is calling.
	 *
	 * <p>
	 * The action is run in the window's sub-session and as one interaction of the session, so that
	 * its display updates reach that window's page and its texts are resolved in that window's
	 * language. A window whose page is gone is not acted on.
	 * </p>
	 */
	public static void runInWindow(ReactContext context, Runnable action) {
		String windowName = context.getWindowName();
		if (!ReactWindowReplay.inWindow(context.getWindowRegistry(), windowName, action)) {
			Logger.warn("Window '" + windowName + "' is gone, the command chain waiting in it does not continue.",
				ChainAwaitingIdentity.class);
		}
	}

}
