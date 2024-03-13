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
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.ThreadSafeSubSessionContext;
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
	 * Reset the super user mode. Access restrictions apply for the current user in the current thread.
     *
     * Use only to finally reset the SuperUser, normally you should use something 
     * like <pre>
     * try {
     *   pushSuperUser();
     * }
     * finally {
     *   popSuperUser();
     * }</pre>
     * This way you can nest SuperUserContexts that do not influence each other.
	 */
	public static void resetSuperUser() {
		SuperUserStateImpl.getInstance().resetSuperUser();
	}

	/** Set the super user mode one higher than it was before.
	 *
	 * The next call to popSuperUser() will result in the same state as before.
	 */
	public static void pushSuperUser() {
		SuperUserStateImpl.getInstance().pushSuperUser();
	}

	/** Reset the super user to the mode before the last call to pushSuperUser(). 
     *
     * @throws EmptyStackException in case SuperUserMode was not set before.
	 */
	public static void popSuperUser() {
		SuperUserStateImpl.getInstance().popSuperUser();
	}
	
	/**
	 * Whether the super user mode is set for the current user in the current thread.
	 */
	public static boolean isAdmin() {
		boolean superUser = SuperUserStateImpl.getInstance().isSuperUser();
		if (superUser) {
			return true;

		}
		ThreadContext context = getThreadContext();
		return context != null && context.isAdminContext();
 	}
	
	/**
	 * Evaluate the given {@link ComputationEx2} in a system context identified by the given caller
	 * class.
	 * 
	 * <p>
	 * If no {@link ThreadContext} is established, a new system context is created exclusively for
	 * the evaluation of the given computation. If a context is already established, no new context
	 * is created, but the given {@link ComputationEx2} is evaluated in the existing context.
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
		return ThreadContextManager.inSystemInteraction(caller, computation);
	}

	/**
	 * Evaluate the given {@link InContext} in a system context identified by the given caller
	 * class.
	 * 
	 * <p>
	 * If no {@link ThreadContext} is established, a new system context is created exclusively for
	 * the evaluation of the given computation. If a context is already established, no new context
	 * is created, but the given {@link InContext} is evaluated in the existing context.
	 * </p>
	 * 
	 * @param caller
	 *        Identifier for the system context to create.
	 * @param job
	 *        The job to execute in system context.
	 */
	public static void inSystemContext(final Class<?> caller, final InContext job) {
		ThreadContextManager.inSystemInteraction(caller, job);
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

}
