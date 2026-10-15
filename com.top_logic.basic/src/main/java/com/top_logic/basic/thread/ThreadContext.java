/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.thread;

import java.util.EmptyStackException;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.ThreadSafeSubSessionContext;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleRuntimeException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ComputationEx2;

/**
 * A Context to be installed and removed from a Thread.
 * 
 * This is (as of now) used to install a session context
 * in the Thread in some Servlet and to remove it when
 * the Servlet is done. This way all Classes can access 
 * the context without having a HttpSession around.
 * 
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class ThreadContext extends ThreadSafeSubSessionContext {

    /** Name of the user currently logged in */
	private String currentUserName;

	/**
	 * Every ThreadContext needs a default CTor.
	 */
    public ThreadContext () {
        super ();
    }

	/**
	 * Extract the {@link ThreadContext} from the perThread context, may return <code>null</code>.
	 */
    public static ThreadContext getThreadContext() {
		if (!ThreadContextManager.Module.INSTANCE.isActive()) {
			return null;
		}
        InteractionContext interaction = ThreadContextManager.getInteraction();
		if (interaction != null) {
			return (ThreadContext) interaction.getSubSessionContext();
		}
		return null;
    }
    
    /** 
     * Get the the name of the user currently logged in.
     *
     * @return null only just before sucessfull login.
     */
    public String getCurrentUserName() {
		return currentUserName;
    }
    
    /** 
     * Set the the name of the user currently logged in.
     */
    public void setCurrentUserName(String aName) {
        currentUserName = aName;
    }

    /**
	 * Whether the owner of this context has administrative rights.
	 * 
	 * @see #isAdmin()
	 */
	protected boolean isAdminContext() {
        return false;
    }

	/**
	 * Executes the given job in the given this {@link SubSessionContext}.
	 * 
	 * @param job
	 *        Callback to execute.
	 */
	public final void inContext(InContext job) {
		ThreadContextManager.inContext(this, job);
	}

	/**
	 * Switches off the security checks for the current thread until {@link #popSuperUser()}.
	 *
	 * <p>
	 * Calls nest: the checks are switched on again by the {@link #popSuperUser()} matching the
	 * first call. Prefer {@link #uncheckedSecurity(InContext)}, which restores the previous state
	 * on its own.
	 * </p>
	 *
	 * @see #isSecurityUnchecked()
	 */
	public static void pushSuperUser() {
		SecurityState.current()._depth++;
	}

	/**
	 * Undoes the last {@link #pushSuperUser()}.
	 *
	 * @throws EmptyStackException
	 *         if there is no {@link #pushSuperUser()} to undo.
	 */
	public static void popSuperUser() {
		SecurityState state = SecurityState.current();
		if (state._depth == 0) {
			throw new EmptyStackException();
		}
		state._depth--;
	}

	/**
	 * Switches the security checks for the current thread on again, regardless of the number of
	 * {@link #pushSuperUser()} calls.
	 */
	public static void resetSuperUser() {
		SecurityState.current()._depth = 0;
	}

	/**
	 * Whether access checks are switched off for the current thread.
	 *
	 * <p>
	 * While the security is unchecked, every access is allowed. Otherwise, access is decided for
	 * the person of the current context.
	 * </p>
	 *
	 * @see #uncheckedSecurity(InContext)
	 * @see #inSystemContext(Class, InContext)
	 */
	public static boolean isSecurityUnchecked() {
		return SecurityState.current()._depth > 0;
	}

	/**
	 * Executes the given job with access checks switched off, keeping the current context.
	 *
	 * <p>
	 * In contrast to {@link #inSystemContext(Class, InContext)}, the person of the current context
	 * stays in place, so that e.g. changes made by the job are attributed to that person.
	 * </p>
	 *
	 * @param job
	 *        The job to execute.
	 *
	 * @see #isSecurityUnchecked()
	 */
	public static void uncheckedSecurity(InContext job) {
		withSecurityUnchecked(true, job);
	}

	/**
	 * Computes the given job with access checks switched off, keeping the current context.
	 *
	 * @param job
	 *        The computation to evaluate.
	 * @return The result of the given job.
	 *
	 * @see #uncheckedSecurity(InContext)
	 */
	public static <T, E1 extends Throwable, E2 extends Throwable> T uncheckedSecurity(ComputationEx2<T, E1, E2> job)
			throws E1, E2 {
		return withSecurityUnchecked(true, job);
	}

	/**
	 * Executes the given job with {@link #isSecurityUnchecked()} set to the given value, restoring
	 * the previous state afterwards.
	 *
	 * @param unchecked
	 *        Whether access checks are switched off during the job.
	 * @param job
	 *        The job to execute.
	 */
	@FrameworkInternal
	public static void withSecurityUnchecked(boolean unchecked, InContext job) {
		SecurityState state = SecurityState.current();
		int before = state._depth;
		state._depth = unchecked ? before + 1 : 0;
		try {
			job.inContext();
		} finally {
			state._depth = before;
		}
	}

	/**
	 * Computes the given job with {@link #isSecurityUnchecked()} set to the given value, restoring
	 * the previous state afterwards.
	 *
	 * @param unchecked
	 *        Whether access checks are switched off during the job.
	 * @param job
	 *        The computation to evaluate.
	 * @return The result of the given job.
	 */
	@FrameworkInternal
	public static <T, E1 extends Throwable, E2 extends Throwable> T withSecurityUnchecked(boolean unchecked,
			ComputationEx2<T, E1, E2> job) throws E1, E2 {
		SecurityState state = SecurityState.current();
		int before = state._depth;
		state._depth = unchecked ? before + 1 : 0;
		try {
			return job.run();
		} finally {
			state._depth = before;
		}
	}

	/**
	 * Whether the current thread has administrative rights.
	 *
	 * <p>
	 * This is the case while the {@link #isSecurityUnchecked() security is unchecked}, or if the
	 * owner of the current context is an administrator.
	 * </p>
	 */
	public static boolean isAdmin() {
		if (isSecurityUnchecked()) {
			return true;
		}
		ThreadContext context = getThreadContext();
		return context != null && context.isAdminContext();
	}

	/**
	 * Whether the current thread runs in a system context.
	 *
	 * <p>
	 * A system context is established by {@link #inSystemContext(Class, InContext)} (and its
	 * variants). Its {@link SubSessionContext#getContextId() context id} carries the
	 * {@link SessionContext#SYSTEM_ID_PREFIX}. Setting a person on the context replaces this id.
	 * </p>
	 */
	public static boolean isSystemContext() {
		ThreadContext context = getThreadContext();
		if (context == null) {
			return false;
		}
		String contextId = context.getContextId();
		return contextId != null && contextId.startsWith(SessionContext.SYSTEM_ID_PREFIX);
	}

	/**
	 * Evaluate the given {@link ComputationEx2} in a system context identified by the given caller
	 * class.
	 *
	 * <p>
	 * The system context has no person, its {@link SubSessionContext#getContextId() context id} is
	 * {@link ThreadContextManager#systemContextId(Class)}, and the
	 * {@link #isSecurityUnchecked() security is unchecked} during the computation. The system
	 * context is established also when an interaction is already active; the previous context is
	 * restored afterwards.
	 * </p>
	 *
	 * @param caller
	 *        Identifier for the system context to create.
	 * @param computation
	 *        The computation to evaluate in system context.
	 * @return the {@link ComputationEx2#run() result} of the given {@link ComputationEx2}.
	 */
	public static <T, E1 extends Throwable, E2 extends Throwable> T inSystemContext(final Class<?> caller,
			final ComputationEx2<T, E1, E2> computation) throws E1, E2 {
		return ThreadContextManager.inSystemContext(caller, computation);
	}

	/**
	 * Executes the given {@link InContext} in a system context identified by the given caller
	 * class.
	 *
	 * @param caller
	 *        Identifier for the system context to create.
	 * @param job
	 *        The job to execute in system context.
	 *
	 * @see #inSystemContext(Class, ComputationEx2)
	 */
	public static void inSystemContext(final Class<?> caller, final InContext job) {
		ThreadContextManager.inSystemContext(caller, job);
	}

	/**
	 * Switches to system context ensuring that a given set of modules is active.
	 * 
	 * <p>
	 * Note: This utility must only be used from tools, not a running application since there all
	 * modules must be started in advance.
	 * </p>
	 */
	public static <T, E1 extends Throwable, E2 extends Throwable> T inSystemContextWithModules(
			final Class<?> contextClass, final ComputationEx2<T, E1, E2> computation,
			final BasicRuntimeModule<?>... modules) throws E1, E2 {
		final ComputationEx2<T, E1, E2> withAdditionalModules;
		if (modules == null || modules.length == 0) {
			withAdditionalModules = computation;
		} else {
			withAdditionalModules = new ComputationEx2<>() {
				@Override
				public T run() throws ModuleRuntimeException, E1, E2 {
					return ModuleUtil.INSTANCE.inModuleContext(computation, modules);
				}
			};
		}

		ComputationEx2<T, E1, E2> withThreadContextManager = new ComputationEx2<>() {
			@Override
			public T run() throws E1, E2 {
				return inSystemContext(contextClass, withAdditionalModules);
			}
		};

		return ModuleUtil.INSTANCE.inModuleContext(withThreadContextManager, ThreadContextManager.Module.INSTANCE);
	}

	/**
	 * Switches to system context ensuring that a given set of modules is active.
	 * 
	 * <p>
	 * Note: This utility must only be used from tools, not a running application since there all
	 * modules must be started in advance.
	 * </p>
	 */
	public static void inSystemContextWithModules(final Class<?> contextClass, final InContext job,
			final BasicRuntimeModule<?>... modules) {
		final Computation<Void> jobComputation = new Computation<>() {
			@Override
			public Void run() {
				job.inContext();
				return null;
			}
		};

		inSystemContextWithModules(contextClass, jobComputation, modules);
	}

	/**
	 * Get the {@link TimeZone} for the current {@link ThreadContext} or fall back to
	 * {@link TimeZone#getDefault() system time zone}.
	 */
	public static TimeZone getTimeZone() {
		ThreadContext This = ThreadContext.getThreadContext();
		if (This != null) {
			TimeZone timeZone = This.getCurrentTimeZone();
			if (timeZone != null) {
				return timeZone;
			}
		}
		return TimeZones.defaultUserTimeZone();
	}

	/**
	 * Get the {@link Locale} for the current {@link ThreadContext} or fall back to
	 * {@link Locale#getDefault() system locale}.
	 */
	public static Locale getLocale() {
		ThreadContext This = ThreadContext.getThreadContext();
		if (This != null) {
			Locale locale = This.getCurrentLocale();
			if (locale != null) {
				return locale;
			}
		}
		return Locale.getDefault();
	}

	/**
	 * Per-thread state of {@link #isSecurityUnchecked()}.
	 */
	private static final class SecurityState {

		private static final ThreadLocal<SecurityState> STATE = ThreadLocal.withInitial(SecurityState::new);

		/**
		 * The number of active requests to switch off the security checks.
		 */
		int _depth;

		static SecurityState current() {
			return STATE.get();
		}

	}

}
