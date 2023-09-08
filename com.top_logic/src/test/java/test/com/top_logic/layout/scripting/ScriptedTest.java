/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.basic.LogListeningTestCase;
import test.com.top_logic.layout.scripting.runtime.TestedApplicationSession;

import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.ConfigurationErrorProtocol;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.Computation;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.scripting.action.ActionChain;
import com.top_logic.layout.scripting.action.ActionUtil;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.ConditionalAction;
import com.top_logic.layout.scripting.action.DynamicAction;
import com.top_logic.layout.scripting.action.LoginAction;
import com.top_logic.layout.scripting.action.LogoutAction;
import com.top_logic.layout.scripting.runtime.ApplicationSession;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertion;

/**
 * Represents an application test using the scripting framework. <br/>
 * Is usually created via: {@link XmlScriptedTestUtil} and {@link AbstractScriptedTestFactory}.
 * 
 * <b>Hint:</b> If you came here looking for a test case that seems to belong to this class, try
 * pressing "ctrl + shift + r" in eclipse and type a "*" followed by the name of the test, followed
 * by ".xml": "*testSomething.xml" That will bring up the XML-Script of the test.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@DeactivatedTest("No test case in narrower sense.")
public final class ScriptedTest extends LogListeningTestCase {

	private static final boolean TREAT_WARNINGS_AS_ERRORS = false;

	/**
	 * Used for configuring a {@link ScriptedTest}.
	 */
	public static class ScriptedTestParameters {
	
		private Protocol loggingProtocol;
		private Locale locale;

		private TimeZone timeZone;
	
		/**
		 * Sets the protocol whose {@link Protocol#error(String, Throwable)} is triggered in case
		 * some action fails. Default is the {@link AssertProtocol}.
		 */
		public ScriptedTestParameters setLoggingProtocol(Protocol loggingProtocol) {
			this.loggingProtocol = loggingProtocol;
			return this;
		}
	
		/**
		 * Sets the {@link Locale} that should be used for the tests. Default is the locale of the
		 * user.
		 */
		public ScriptedTestParameters setLocale(Locale locale) {
			this.locale = locale;
			return this;
		}

		/**
		 * Sets the {@link TimeZone} that should be used for the tests. Default is the time zone of
		 * the user.
		 */
		public ScriptedTestParameters setTimeZone(TimeZone timeZone) {
			this.timeZone = timeZone;
			return this;
		}

		/**
		 * Default is the {@link AssertProtocol}.
		 */
		public Protocol getLoggingProtocol() {
			if (loggingProtocol == null) {
				AssertProtocol assertProtocol = new AssertProtocol();
				assertProtocol.setVerbosity(Protocol.DEBUG);
				return assertProtocol;
			} else {
				return loggingProtocol;
			}
		}
	
		/**
		 * Default is the users locale.
		 */
		public Locale getLocale() {
			return locale;
		}

		/**
		 * Default is the users time zone.
		 */
		public TimeZone getTimeZone() {
			return timeZone;
		}

	}

	/**
	 * {@link InstantiationContext} for the {@link ScriptedTest}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public static class ScriptedTestInstantiationContext extends SimpleInstantiationContext {

		private final ScriptedTest _test;

		/**
		 * @param test
		 *        {@link ScriptedTest}.
		 */
		public ScriptedTestInstantiationContext(ScriptedTest test) {
			super(ConfigurationErrorProtocol.INSTANCE);

			_test = test;
		}

		/**
		 * Scripted test.
		 */
		public ScriptedTest getTest() {
			return _test;
		}
	}

	Provider<ApplicationAction> actionProvider;
	private ScriptedTestParameters parameters;

	Map<String, ApplicationSession> _sessionByProcessId = new HashMap<>();

	private Map<String, String> _logins = new HashMap<>();

	/**
	 * Creates a new {@link ScriptedTest}.
	 * <p>
	 * The {@link ApplicationAction}s are given via a {@link Provider} and not directly to delay
	 * errors until the {@link ApplicationAction} is requested, i.e. until the test is executed.
	 * Otherwise, a mistake on {@link File} instantiation could kill the whole test tree creation.
	 * </p>
	 * 
	 * @param actionProvider
	 *        The {@link Provider} that will create the {@link ApplicationAction}s. Must not be
	 *        <code>null</code>.
	 * @param testName
	 *        The name of the test. Must not be <code>null</code>.
	 * @param parameters
	 *        Parameters for the script execution, like which user should execute the script. Must
	 *        not be <code>null</code>.
	 */
	public ScriptedTest(Provider<ApplicationAction> actionProvider, String testName, ScriptedTestParameters parameters) {
		super(testName, TREAT_WARNINGS_AS_ERRORS);
		this.actionProvider = actionProvider;
		this.parameters = parameters;
	}

	@Override
	protected String getErrorLoggedMessage() {
		/* Some funny ExcelAction contain FastListElements in their configuration. Therefore a
		 * ThreadContext must be available to access the database. */
		ApplicationAction action =
			ThreadContext.inSystemContext(ScriptedTest.class, new Computation<ApplicationAction>() {

			@Override
				public ApplicationAction run() {
					return actionProvider.get();
			}

		});
		return action.getFailureMessage();
	}

	@Override
	protected void runTest() throws Throwable {
		Throwable problem = ThreadContext.inSystemContext(ScriptedTest.class, new Computation<Throwable>() {

			@Override
			public Throwable run() {
				try {
					try {
						executeTestLogListenerWrapping();
					} finally {
						invalidateSessions();
					}
					return null;
				} catch (Throwable ex) {
					return ex;
				}
			}
		});

		if (problem != null) {
			throw problem;
		}
	}

	void executeTestLogListenerWrapping() {
		processAction(actionProvider.get(), null, Maybe.none(), Maybe.none());
	}

	private void processAction(ApplicationAction action, String parentProcessId,
			Maybe<TimeZone> parentActionTimeZone, Maybe<Locale> parentActionLocale) {
		Maybe<TimeZone> actionTimeZone = Maybe.toMaybe(action.getTimeZone());
		Maybe<Locale> actionLocale = Maybe.toMaybe(action.getLocale());
		if (action instanceof LoginAction) {
			String processId = ((LoginAction) action).getProcessId();
			String account = ((LoginAction) action).getAccount();
			if (account == null) {
				account = processId;
			}
			_logins.put(processId, account);
		} else if (action instanceof DynamicAction) {
			String processId = processId(parentProcessId, action);
			Person testingPerson = getPersonById(processId);
			Maybe<TimeZone> testingTimeZone = getCurrentOrParent(actionTimeZone, parentActionTimeZone);
			Maybe<Locale> testingLocale = getCurrentOrParent(actionLocale, parentActionLocale);
			ApplicationSession sessionForUser =
				getSessionForUser(processId, testingPerson, testingTimeZone, testingLocale, action);

			List<ApplicationAction> actions = sessionForUser.resolve((DynamicAction) action);

			ActionUtil.propagateDebugInfo(action, actions);
			try {
				processActions(processId, parentActionTimeZone, parentActionLocale, actionTimeZone, actionLocale,
					actions);
			} catch (Throwable ex) {
				throw TestedApplicationSession.enhanceThrowable(action, ex);
			}
		} else if (action instanceof ActionChain) {
			String processId = processId(parentProcessId, action);
			ActionChain actionChain = (ActionChain) action;
			ActionUtil.propagateDebugInfo(actionChain);

			if (action instanceof ConditionalAction) {
				ApplicationAction condition = ((ConditionalAction) actionChain).getCondition();
				Maybe<TimeZone> testingTimeZone = getCurrentOrParent(actionTimeZone, parentActionTimeZone);
				Maybe<Locale> testingLocale = getCurrentOrParent(actionLocale, parentActionLocale);
				Person testingPerson = getPersonById(processId);
				if (evaluate(condition, processId, testingPerson, testingTimeZone, testingLocale)) {
					processActions(processId, parentActionTimeZone, parentActionLocale,
						actionTimeZone, actionLocale, actionChain.getActions());
				}
			} else {
				processActions(processId, parentActionTimeZone, parentActionLocale,
					actionTimeZone, actionLocale, actionChain.getActions());
			}
		} else {
			String processId = processId(parentProcessId, action);
			Person testingPerson = getPersonById(processId);
			Maybe<TimeZone> testingTimeZone = getCurrentOrParent(actionTimeZone, parentActionTimeZone);
			Maybe<Locale> testingLocale = getCurrentOrParent(actionLocale, parentActionLocale);
			processAtomic(action, processId, testingPerson, testingTimeZone, testingLocale);
		}
		if (action instanceof LogoutAction) {
			String processId = processId(parentProcessId, action);
			_logins.remove(processId);
			_sessionByProcessId.remove(processId);
		}
	}

	private String processId(String parentProcessId, ApplicationAction action) {
		String actionProcessId = action.getUserID();

		if (!StringServices.isEmpty(actionProcessId)) {
			return actionProcessId;
		}

		if (!StringServices.isEmpty(parentProcessId)) {
			return parentProcessId;
		}

		return PersonManager.getManager().getRoot().getName();
	}

	private boolean evaluate(ApplicationAction condition, String processId, Person person,
			Maybe<TimeZone> timeZone, Maybe<Locale> locale) {
		Object result = processAtomic(condition, processId, person, timeZone, locale);
		return isTrue(result);
	}

	private boolean isTrue(Object value) {
		return (value instanceof Boolean) && ((Boolean) value).booleanValue();
	}

	private void processActions(String parentProcessId, Maybe<TimeZone> parentActionTimeZone,
			Maybe<Locale> parentActionLocale, Maybe<TimeZone> actionTimeZone,
			Maybe<Locale> actionLocale, List<ApplicationAction> actions) {
		for (ApplicationAction innerAction : actions) {
			Maybe<TimeZone> testingTimeZone = getCurrentOrParent(actionTimeZone, parentActionTimeZone);
			Maybe<Locale> testingLocale = getCurrentOrParent(actionLocale, parentActionLocale);
			processAction(innerAction, parentProcessId, testingTimeZone, testingLocale);
		}
	}

	private ApplicationSession getSessionForUser(String processId, Person person, Maybe<TimeZone> timeZone, Maybe<Locale> locale, ApplicationAction action) {
		ApplicationSession session;
		if (!_sessionByProcessId.containsKey(processId)) {
			session = createSession(person, timeZone, locale, action);
			_sessionByProcessId.put(processId, session);
		} else {
			session = _sessionByProcessId.get(processId);
			checkSessionConsistency(session, timeZone, locale);
		}
		return session;
	}

	private void checkSessionConsistency(ApplicationSession session, Maybe<TimeZone> timeZone, Maybe<Locale> locale) {
		// Check for same locale and TimeZone
		checkSameLocale(session, locale);
		checkSameTimeZone(session, timeZone);
	}

	private void checkSameTimeZone(ApplicationSession session, Maybe<TimeZone> timeZone) {
		if (!timeZone.hasValue()) {
			return;
		}
		TimeZone actionTimeZone = timeZone.get();
		TimeZone sessionTimeZone = session.getSubSession().getCurrentTimeZone();
		if (!actionTimeZone.hasSameRules(sessionTimeZone)) {
			parameters.getLoggingProtocol()
				.error("Different time zone for action: " + sessionTimeZone + " vs. " + actionTimeZone);
		}
	}

	private void checkSameLocale(ApplicationSession session, Maybe<Locale> locale) {
		if (!locale.hasValue()) {
			return;
		}
		Locale actionLocale = locale.get();
		Locale sessionLocale = session.getSubSession().getCurrentLocale();
		if (!actionLocale.equals(sessionLocale)) {
			parameters.getLoggingProtocol()
				.error("Different locale for action: " + sessionLocale + " vs. " + actionLocale);
		}
	}

	private Object processAtomic(ApplicationAction action, String processId, Person person, Maybe<TimeZone> timeZone, Maybe<Locale> locale) {
		ApplicationSession sessionForUser = getSessionForUser(processId, person, timeZone, locale, action);
		return processAtomic(sessionForUser, action);
	}

	private Object processAtomic(ApplicationSession session, ApplicationAction applAction) {
		try {
			log(applAction);
			return session.process(applAction);
		} catch (ApplicationAssertion ex) {
			parameters.getLoggingProtocol().error(ex.getMessage(), ex);
			return null;
		} catch (Throwable throwable) {
			/* JUnit displays Exceptions itself and does not report anything to the Logger. This
			 * makes it harder to analyze problems. Therefore: report all Exceptions to the
			 * Logger. */
			Logger.error(throwable.getMessage(), throwable, ScriptedTest.class);
			throw throwable;
		}
	}

	private void log(ApplicationAction action) {
		if (Logger.isDebugEnabled(ScriptedTest.class)) {
			Logger.debug("Processing action: " + action, ScriptedTest.class);
		}
	}

	private ApplicationSession createSession(Person person, Maybe<TimeZone> actionTimeZone,
			Maybe<Locale> actionLocale, ApplicationAction action) {
		TimeZone timeZone = getTimeZone(person, actionTimeZone);
		Locale locale = getLocale(person, actionLocale);
		try {
			return ApplicationTestSetup.getApplication().login(person, timeZone, locale);
		} catch (Exception ex) {
			String enhancedMessage = ActionUtil.enhanceMessage(ex, action);
			throw (AssertionError) new AssertionError(enhancedMessage).initCause(ex);
		}

	}

	void invalidateSessions() throws Throwable {
		Throwable problem = null;
		ApplicationSession[] sessionCopy =
			_sessionByProcessId.values().toArray(new ApplicationSession[_sessionByProcessId.size()]);
		for (ApplicationSession session : sessionCopy) {
			try {
				invalidateSession(session);
			} catch (ThreadDeath exception) {
				throw exception;
			} catch (Throwable exception) {
				// Keep the first problem, as the others are often just consequential errors
				if (problem == null) {
					problem = exception;
				}
			}
		}
		// Prevent Memory Leak: Depending on the concrete session implementation,
		// sessions can reference huge amounts of data.
		// And Test classes like this one are not garbage collected
		// until all tests are done and JUnit shuts down the VM.
		_sessionByProcessId.clear();
		if (problem != null) {
			throw problem;
		}
	}

	private void invalidateSession(ApplicationSession session) {
		try {
			session.invalidate();
		} catch (Throwable throwable) {
			Logger.error(throwable.getMessage(), throwable, ScriptedTest.class);
			throw throwable;
		}
	}

	private <T> Maybe<T> getCurrentOrParent(Maybe<T> actionValue, Maybe<T> parentValue) {
		if (actionValue.hasValue()) {
			return actionValue;
		}
		if (parentValue.hasValue()) {
			return parentValue;
		}
		return Maybe.none();
	}

	private Person getPersonById(String processId) {
		String account = _logins.get(processId);
		if (account == null) {
			account = processId;
		}
		Person person = resolveUserId(account);
		assert person != null : "Getting a Person for userId '" + account + "' failed!";
		return person;
	}

	private Person resolveUserId(final String userId) {
		return PersonManager.getManager().getPersonByName(userId);
	}

	/**
	 * Fetches the first non <code>null</code> {@link Locale} in following order:
	 * <ol>
	 * <li>The locale specified by the {@link ApplicationAction#getLocale()}.</li>
	 * <li>The locale specified by the {@link ScriptedTest#parameters}.</li>
	 * <li>The default locale of the user.</li>
	 * </ol>
	 */
	private Locale getLocale(Person person, Maybe<Locale> actionLocale) {
		if (actionLocale.hasValue()) {
			return actionLocale.get();
		}
		if (parameters.getLocale() != null) {
			return parameters.getLocale();
		}
		return person.getLocale();
	}

	/**
	 * Fetches the first non <code>null</code> {@link TimeZone} in following order:
	 * <ol>
	 * <li>The time zone specified by the {@link ApplicationAction#getTimeZone()}.</li>
	 * <li>The time zone specified by the {@link ScriptedTest#parameters}.</li>
	 * <li>The default time zone of the user.</li>
	 * </ol>
	 */
	private TimeZone getTimeZone(Person person, Maybe<TimeZone> actionTimeZone) {
		if (actionTimeZone.hasValue()) {
			return actionTimeZone.get();
		}
		if (parameters.getTimeZone() != null) {
			return parameters.getTimeZone();
		}
		return person.getTimeZone();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	public void tearDown() throws Exception {
		// Prevent Memory Leak:
		// As all test classes (like this class) are kept in memory
		// until all tests are finished and the VM shuts down,
		// everything referenced from this class will never be garbage collected.
		_sessionByProcessId = null;
		actionProvider = null;
		parameters = null;
		super.tearDown();
	}

}
