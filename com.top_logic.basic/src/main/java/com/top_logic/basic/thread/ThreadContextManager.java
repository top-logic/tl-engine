/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.thread;

import java.util.function.Consumer;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingListener;

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.SimpleSessionContext;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.RuntimeCategory;
import com.top_logic.basic.util.ComputationEx2;

/**
 * Manages the creation of {@link ThreadContext}s for interactions and background computations.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies(SecureRandomService.Module.class)
@Label("Thread context management")
@ServiceCategory.Of(RuntimeCategory.class)
public abstract class ThreadContextManager extends ManagedClass {

	/**
	 * Configuration of a {@link ThreadContextManager}.
	 * 
	 * @since 5.7.4
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ManagedClass.ServiceConfiguration<ThreadContextManager> {

		/**
		 * Configuration property that enables {@link #debugContextRemoval}.
		 */
		String DEBUG_CONTEXT_REMOVAL_PROPERTY = "debug-context-removal";

		/**
		 * Option to remember for each {@link ThreadContextManager#removeInteraction() remove}
		 * operation the stack trace of the caller.
		 * 
		 * <p>
		 * This is a debugging option to track duplicate context removal problems.
		 * </p>
		 */
		@Name(DEBUG_CONTEXT_REMOVAL_PROPERTY)
		@BooleanDefault(false)
		boolean isDebugContextRemoval();

		@Override
		@ClassDefault(BasicThreadContextManager.class)
		Class<? extends ThreadContextManager> getImplementationClass();
	}

	/**
	 * <p>
	 * This option is enabled through the {@link Config#DEBUG_CONTEXT_REMOVAL_PROPERTY}
	 * configuration option.
	 * </p>
	 */
	private final boolean debugContextRemoval;
    
	private final ThreadLocal<InteractionContext> _contexts = new ThreadLocal<>();

	/**
	 * Creates a new {@link ThreadContextManager}.
	 */
	public ThreadContextManager(InstantiationContext context, Config configuration) {
		super(context, configuration);
		debugContextRemoval = configuration.isDebugContextRemoval();
		if (debugContextRemoval) {
			Logger.info(
				"Debug mode for thread context removal is enabled, this setting will decrease system performance.",
				ThreadContextManager.class);
		}

	}
	
	/**
	 * Returns the sole {@link ThreadContextManager} instance.
	 */
	public static ThreadContextManager getManager() {
		return ThreadContextManager.Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Create a new {@link ThreadContext}.
	 */
	public final ThreadContext newSubSessionContext() {
		return internalNewSubSessionContext();
	}

	/**
	 * Creates the result for {@link #newSubSessionContext()}.
	 */
	protected abstract ThreadContext internalNewSubSessionContext();

	/**
	 * Creates a new {@link InteractionContext} for the given request.
	 */
	public final InteractionContext newInteraction(ServletContext servletContext, HttpServletRequest request,
			HttpServletResponse response) {
		return internalNewInteraction(servletContext, request, response);
	}

	/**
	 * Creates the result for
	 * {@link #newInteraction(ServletContext, HttpServletRequest, HttpServletResponse)}.
	 */
	protected abstract InteractionContext internalNewInteraction(ServletContext servletContext,
			HttpServletRequest request, HttpServletResponse response);

	/**
	 * Creates a new {@link SessionContext}.
	 */
	public final SessionContext newSessionContext() {
		return internalNewSessionContext();
	}

	/**
	 * Creates the result for {@link #newSessionContext()}.
	 */
	protected abstract SessionContext internalNewSessionContext();

	/**
	 * Creates a new {@link SessionContext} for the given {@link HttpSession}.
	 * 
	 * @param session
	 *        {@link HttpSession} for which a {@link SessionContext} representation is needed.
	 */
	public final SessionContext newSessionContext(HttpSession session) {
		return internalNewSessionContext(session);
	}

	/**
	 * Creates the result for {@link #newSessionContext(HttpSession)}.
	 * 
	 * This implementation falls back to {@link #internalNewSessionContext()}
	 * 
	 * @param session
	 *        {@link HttpSession} for which a {@link SessionContext} representation is needed.
	 * 
	 * @see #internalNewSessionContext()
	 */
	protected SessionContext internalNewSessionContext(HttpSession session) {
		return internalNewSessionContext();
	}

	/**
	 * Returns the {@link SessionContext} for the current thread.
	 */
	public static SessionContext getSession() {
		return getManager().getSessionInternal();
	}

	/**
	 * @see #getSession()
	 */
	protected SessionContext getSessionInternal() {
		InteractionContext interaction = getInteractionInternal();
		if (interaction == null) {
			return null;
		}
		assert checkConsistency(interaction);
		return interaction.getSessionContext();
	}

	private static boolean checkConsistency(InteractionContext interaction) {
		SubSessionContext subSessionContext = interaction.getSubSessionContext();
		if (subSessionContext != null) {
			if (subSessionContext.getSessionContext() != interaction.getSessionContext()) {
				throw new IllegalStateException("Session of interaction does not match session of its subsession");
			}
		}
		return true;
	}

	/**
	 * Returns the {@link SubSessionContext} for the current thread.
	 */
	public static SubSessionContext getSubSession() {
		return getManager().getSubSessionInternal();
	}

	/**
	 * @see #getSubSession()
	 */
	protected SubSessionContext getSubSessionInternal() {
		InteractionContext interaction = getInteractionInternal();
		if (interaction == null) {
			return null;
		}
		return interaction.getSubSessionContext();
	}

	/**
	 * Returns the {@link InteractionContext} for the current thread.
	 */
	public static InteractionContext getInteraction() {
		return getManager().getInteractionInternal();
	}

	/**
	 * @see #getInteraction()
	 */
	protected InteractionContext getInteractionInternal() {
		InteractionContext interactionContext = _contexts.get();
		if (debugContextRemoval && Remover.isRemover(interactionContext)) {
			return null;
		}
		return interactionContext;
	}

	/**
	 * Installs the given {@link InteractionContext} for the current thread.
	 */
	@FrameworkInternal
	public void setInteraction(InteractionContext context) {
		assert context != null;
		InteractionContext current = getInteractionInternal();
		if (current == context) {
			return; // Avoid duplicate sending of Events
		}
		if (current != null) {
			Logger.info("Overriding interaction " + current, ThreadContextManager.class);
			current.invalidate();
		}

		_contexts.set(context);
	}

	/**
	 * Removes the current {@link InteractionContext} for the current thread.
	 */
	@FrameworkInternal
	public void removeInteraction() {
		InteractionContext interaction = _contexts.get();
		if (interaction == null) {
			if (Logger.isDebugEnabled(ThreadContext.class)) {
				StringBuilder msg = new StringBuilder();
				msg.append("No context in removeContext(), enable ");
				msg.append(ThreadContextManager.Config.DEBUG_CONTEXT_REMOVAL_PROPERTY);
				msg.append(" for ");
				msg.append(ThreadContextManager.class);
				msg.append(" to find the premature remove operation.");
				Logger.debug(msg.toString(), new Exception("duplicate remove"),
					ThreadContextManager.class);
			}
			if (debugContextRemoval) {
				_contexts.set(Remover.newRemover());
			}
			return;
		}
		if (debugContextRemoval) {
			if (Remover.isRemover(interaction)) {
				Throwable newStack = Remover.updateStack(interaction);
				Logger.info("No context in removeContext().", newStack, ThreadContextManager.class);
			} else {
				interaction.invalidate();
				_contexts.set(Remover.newRemover());
			}
		} else {
			interaction.invalidate();
			_contexts.remove();
		}

	}

	/**
	 * Implementation of {@link ThreadContext#inSystemContext(Class, InContext)}.
	 *
	 * @param contextId
	 *        {@link SubSessionContext#getContextId() Identifier} of the system context.
	 * @param job
	 *        Callback to execute
	 */
	protected void inSystemContextInternal(String contextId, InContext job) {
		inSystemContextInternal(contextId, () -> {
			job.inContext();
			return null;
		});
	}

	/**
	 * Implementation of {@link ThreadContext#inSystemContext(Class, ComputationEx2)}.
	 *
	 * @param contextId
	 *        {@link SubSessionContext#getContextId() Identifier} of the system context.
	 * @param job
	 *        Callback to execute
	 * @return The result of the given job.
	 */
	protected <T, E1 extends Throwable, E2 extends Throwable> T inSystemContextInternal(String contextId,
			ComputationEx2<T, E1, E2> job) throws E1, E2 {
		return this.<T, E1, E2> inIdentity(subSession -> applySystemIdentity(subSession, contextId), false, job);
	}

	/**
	 * Computes the given job with the identity established by the given function.
	 *
	 * <p>
	 * If no interaction is active, a new interaction with its own synthetic {@link SessionContext}
	 * and {@link SubSessionContext} is created for the job, the identity is applied to its
	 * {@link SubSessionContext}, and the synthetic session is {@link #sessionUnbound(SessionContext)
	 * unbound} afterwards. Otherwise, the identity is applied in place to the
	 * {@link SubSessionContext} of the current interaction, which keeps its session and the state of
	 * the interaction; its {@link #captureIdentity(ThreadContext) previous identity} is restored
	 * after the job.
	 * </p>
	 *
	 * @param applyIdentity
	 *        Establishes the identity on the {@link SubSessionContext} the job runs in.
	 * @param securityChecks
	 *        Whether access checks are performed during the job, see
	 *        {@link ThreadContext#withSecurityChecks(boolean, ComputationEx2)}.
	 * @param job
	 *        Callback to execute
	 * @return The result of the given job.
	 */
	protected final <T, E1 extends Throwable, E2 extends Throwable> T inIdentity(
			Consumer<? super ThreadContext> applyIdentity, boolean securityChecks, ComputationEx2<T, E1, E2> job)
			throws E1, E2 {
		ComputationEx2<T, E1, E2> securedJob = () -> ThreadContext.withSecurityChecks(securityChecks, job);
		InteractionContext interaction = getInteractionInternal();
		if (interaction == null) {
			InteractionContext freshInteraction = newInteractionInternal();
			registerSubSessionInSessionForInteraction(freshInteraction);
			setInteraction(freshInteraction);
			try {
				ThreadContext freshSubSession = (ThreadContext) freshInteraction.getSubSessionContext();
				return ThreadContextManager.<T, E1, E2> runInSyntheticSession(freshInteraction.getSessionContext(),
					() -> {
						applyIdentity.accept(freshSubSession);
						return securedJob.run();
					});
			} finally {
				removeInteraction();
			}
		}

		ThreadContext subSession = (ThreadContext) interaction.getSubSessionContext();
		if (subSession == null) {
			/* An interaction without sub-session: The job gets a sub-session of the interaction's
			 * session for its identity. */
			SessionContext session = interaction.getSessionContext();
			ThreadContext jobSubSession = newSubSessionContext();
			jobSubSession.setSessionContext(session != null ? session : newSessionContext());
			interaction.installSubSessionContext(jobSubSession);
			try {
				applyIdentity.accept(jobSubSession);
				return securedJob.run();
			} finally {
				interaction.installSubSessionContext(null);
				interaction.installSessionContext(session);
			}
		}

		Runnable restore = captureIdentity(subSession);
		try {
			applyIdentity.accept(subSession);
			return securedJob.run();
		} finally {
			restore.run();
		}
	}

	/**
	 * Records the identity of the given {@link SubSessionContext}.
	 *
	 * @param subSession
	 *        The {@link SubSessionContext} whose identity is about to be changed.
	 * @return Restores the recorded identity of the given {@link SubSessionContext}.
	 */
	protected Runnable captureIdentity(ThreadContext subSession) {
		String contextId = subSession.getContextId();
		return () -> subSession.setContextId(contextId);
	}

	/**
	 * Makes the given {@link SubSessionContext} a system context with the given context id.
	 *
	 * @param subSession
	 *        The {@link SubSessionContext} to change.
	 * @param contextId
	 *        The {@link SubSessionContext#getContextId() context id} of the system context.
	 */
	protected void applySystemIdentity(ThreadContext subSession, String contextId) {
		subSession.setContextId(contextId);
	}

	/**
	 * Runs the given job and {@link #sessionUnbound(SessionContext) unbinds} the given synthetic
	 * session afterwards.
	 */
	private static <T, E1 extends Throwable, E2 extends Throwable> T runInSyntheticSession(SessionContext session,
			ComputationEx2<T, E1, E2> job) throws E1, E2 {
		Throwable problem = null;
		try {
			return job.run();
		} catch (Throwable ex) {
			problem = ex;
			throw ex;
		} finally {
			try {
				sessionUnbound(session);
			} catch (Throwable ex) {
				if (problem == null) {
					throw ex;
				} else {
					problem.addSuppressed(ex);
				}
			}
		}
	}

	/**
	 * This method registers the {@link SubSessionContext} in the {@link SessionContext}.
	 * 
	 * <p>
	 * This method is only a hook method for subclasses.
	 * </p>
	 * 
	 * @param interaction
	 *        The interaction to get {@link SubSessionContext} and {@link SessionContext} from.
	 * 
	 */
	protected void registerSubSessionInSessionForInteraction(InteractionContext interaction) {
		// nothing to register here.
	}

	/**
	 * Executes the given job in the given {@link SubSessionContext}.
	 * 
	 * @param subSession
	 *        The {@link SubSessionContext} to use for the job.
	 * @param job
	 *        Callback to execute.
	 */
	public static void inContext(SubSessionContext subSession, InContext job) {
		getManager().inContextInternal(subSession, job);
	}

	/**
	 * @see #inContext(SubSessionContext, InContext)
	 */
	protected void inContextInternal(SubSessionContext subSession, InContext job) {
		InteractionContext interaction = getInteractionInternal();
		if (interaction == null) {
			interaction = createInteraction();
			setInteraction(interaction);
			try {
				interaction.installSubSessionContext(subSession);
				job.inContext();
			} finally {
				removeInteraction();
			}
		} else {
			SubSessionContext oldSubSession = interaction.getSubSessionContext();
			if (subSession == oldSubSession) {
				job.inContext();
			} else {
				Logger.info("Overriding subsession " + oldSubSession, ThreadContextManager.class);
				/* Sub session may be null but session may be not null, in such case the session
				 * must be saved to be able to reinstall it. */
				SessionContext oldSessionContext = interaction.getSessionContext();
				interaction.installSubSessionContext(subSession);
				try {
					job.inContext();
				} finally {
					interaction.installSessionContext(oldSessionContext);
					interaction.installSubSessionContext(oldSubSession);
				}
			}
		}
	}

	/**
	 * Executes the given job in the given {@link SubSessionContext}.
	 * 
	 * @param subSession
	 *        The {@link SubSessionContext} to use for the job.
	 * @param job
	 *        Callback to execute.
	 */
	public static <T, E1 extends Throwable, E2 extends Throwable> T inContext(SubSessionContext subSession,
			ComputationEx2<T, E1, E2> job) throws E1, E2 {
		return getManager().inContextInternal(subSession, job);
	}

	/**
	 * @see #inContext(SubSessionContext, ComputationEx2)
	 */
	protected <T, E1 extends Throwable, E2 extends Throwable> T inContextInternal(SubSessionContext subSession,
			ComputationEx2<T, E1, E2> job) throws E1, E2 {
		InteractionContext interaction = getInteractionInternal();
		if (interaction == null) {
			interaction = createInteraction();
			setInteraction(interaction);
			try {
				interaction.installSubSessionContext(subSession);
				return job.run();
			} finally {
				removeInteraction();
			}
		} else {
			SubSessionContext oldSubSession = interaction.getSubSessionContext();
			if (subSession == oldSubSession) {
				return job.run();
			} else {
				Logger.info("Overriding subsession " + oldSubSession, ThreadContextManager.class);
				/* Sub session may be null but session may be not null, in such case the session
				 * must be saved to be able to reinstall it. */
				SessionContext oldSessionContext = interaction.getSessionContext();
				interaction.installSubSessionContext(subSession);
				try {
					return job.run();
				} finally {
					interaction.installSessionContext(oldSessionContext);
					interaction.installSubSessionContext(oldSubSession);
				}
			}
		}
	}

	private InteractionContext createInteraction() {
		ServletContext servletContext = null;
		HttpServletRequest request = null;
		HttpServletResponse response = null;
		return newInteraction(servletContext, request, response);
	}

	/**
	 * Creates the context id for a {@link SubSessionContext} when the system executes some
	 * operations.
	 * 
	 * @param caller
	 *        Class that sets the context id.
	 */
	@FrameworkInternal
	public static String systemContextId(Class<?> caller) {
		return SessionContext.SYSTEM_ID_PREFIX + caller.getName();
	}

	/**
	 * Creates the context id for a {@link SubSessionContext} when the system executes some
	 * operations on behalf of a named instance of the given caller class.
	 *
	 * @param caller
	 *        Class that sets the context id.
	 * @param name
	 *        Name of the instance of the caller class, e.g. the name of a task, appended to the
	 *        {@link #systemContextId(Class) context id of the caller class}.
	 */
	@FrameworkInternal
	public static String systemContextId(Class<?> caller, String name) {
		return systemContextId(caller) + "-" + name;
	}

	/**
	 * Creates a new {@link InteractionContext} which has an {@link SubSessionContext} and a
	 * {@link SessionContext}.
	 */
	protected InteractionContext newInteractionInternal() {
		InteractionContext interaction = createInteraction();
		ThreadContext subsession = newSubSessionContext();
		SessionContext session = newSessionContext();

		subsession.setSessionContext(session);
		interaction.installSessionContext(session);
		interaction.installSubSessionContext(subsession);
		return interaction;
	}

	/**
	 * Sets up an {@link InteractionContext} with the given subsession and servlet components.
	 *
	 * @param sessionContext
	 *        Sub-session context to bind to the interaction.
	 * @param servletContext
	 *        Web application context for servlet container communication.
	 * @param request
	 *        HTTP request that initiated this interaction.
	 * @param response
	 *        HTTP response for sending content back to client.
	 * @return The created and installed interaction context.
	 */
	public static InteractionContext setupInteractionContext(SubSessionContext sessionContext,
			ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) {
		InteractionContext context = getManager().newInteraction(servletContext, request, response);
		context.installSubSessionContext(sessionContext);
		getManager().setInteraction(context);
		return context;
	}

	/**
	 * Notifies that the given session is going out of scope.
	 * 
	 * <p>
	 * This is made for {@link SessionContext} which does not represent a {@link HttpSession},
	 * because the {@link InteractionContext} it belongs to is a synthetic interaction.
	 * </p>
	 * 
	 * @see #inIdentity(Consumer, boolean, ComputationEx2)
	 */
	protected static void sessionUnbound(SessionContext session) {
		HttpSessionBindingListener listener = (SimpleSessionContext) session;
		listener.valueUnbound(null);
	}

	/**
	 * Module for instantiation of {@link ThreadContextManager}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<ThreadContextManager> {
		
		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();
		
		@Override
		public Class<ThreadContextManager> getImplementation() {
			return ThreadContextManager.class;
		}

	}

}

/**
 * Dummy {@link InteractionContext} stored for the current thread if the current interaction is
 * removed.
 * 
 * <p>
 * Such an {@link InteractionContext} is used to trace the multiple removal of
 * {@link InteractionContext} within one Interaction.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class Remover implements InteractionContext {

	static Remover newRemover() {
		Remover remover = new Remover();
		remover._stack = new Exception("First removal.");
		return remover;
	}

	static Throwable updateStack(InteractionContext interaction) {
		assert isRemover(interaction);
		Remover remover = (Remover) interaction;
		Throwable oldStack = remover._stack;
		remover._stack = new Exception("Multiple removal.").initCause(oldStack);
		return remover._stack;
	}

	static boolean isRemover(InteractionContext interactionContext) {
		return interactionContext instanceof Remover;
	}

	private Throwable _stack;

	@Override
	public void installSessionContext(SessionContext session) {
		throw fail();
	}

	private UnsupportedOperationException fail() {
		return new UnsupportedOperationException("marker class");
	}

	@Override
	public SessionContext getSessionContext() {
		throw fail();
	}

	@Override
	public void installSubSessionContext(SubSessionContext subSession) {
		throw fail();
	}

	@Override
	public SubSessionContext getSubSessionContext() {
		throw fail();
	}

	@Override
	public void invalidate() {
		throw fail();
	}

	@Override
	public <T> T get(Property<T> property) {
		throw fail();
	}

	@Override
	public <T> T set(Property<T> property, T value) {
		throw fail();
	}

	@Override
	public boolean isSet(Property<?> property) {
		throw fail();
	}

	@Override
	public <T> T reset(Property<T> property) {
		throw fail();
	}

	@Override
	public void addUnboundListener(UnboundListener l) {
		fail();
	}

	@Override
	public HttpServletRequest asRequest() {
		throw fail();
	}

	@Override
	public ServletContext asServletContext() {
		throw fail();
	}

	@Override
	public HttpServletResponse asResponse() {
		throw fail();
	}

}