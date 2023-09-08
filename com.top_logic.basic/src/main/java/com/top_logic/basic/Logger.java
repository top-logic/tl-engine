/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import static com.top_logic.basic.StringServices.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.listener.Listener;
import com.top_logic.basic.listener.ListenerRegistry;
import com.top_logic.basic.listener.ListenerRegistryFactory;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.logging.LogConfigurator;
import com.top_logic.basic.thread.StackTrace;

/**
 * The class is responsible for logging.
 *
 * The user is independent of the used logging framework.
 * 
 * <p>
 * The logging can be reconfigured at runtime using the
 * {@link com.top_logic.basic.Logger#configure(URL)} method.
 * </p>
 *
 * @author Frank Mausz / Michael Erikson
 */
@Label("Application log")
public class Logger {

	private static final Marker FATAL_MARKER = MarkerFactory.getMarker("FATAL");

	/**
	 * Represents an entry in the log.
	 * 
	 * @author    <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
	 */
	public static class LogEntry {

		final private String message;
		final private Maybe<Throwable> exception;
		final private Object caller;
		final private Level priority;

		private LogEntry(String message, Maybe<Throwable> exception, Object caller, Level priority) {
			this.message = message;
			this.exception = exception;
			this.caller = caller;
			this.priority = priority;
		}

		/**
		 * Convenience shortcut for {@link #LogEntry(String, Maybe, Object, Level)}
		 * that creates a {@link Maybe} from the given {@link Throwable} but calling {@link Maybe#toMaybe(Object)}.
		 */
		LogEntry(String message, Throwable exception, Object caller, Level priority) {
			this(message, Maybe.toMaybe(exception), caller, priority);
		}

		public String getMessage() {
			return message;
		}

		/** There is not always a {@link Throwable} associated with a {@link LogEntry}.
		 * Therefore, this method returns {@link Maybe} of a {@link Throwable}. */
		public Maybe<Throwable> getException() {
			return exception;
		}

		public Object getCaller() {
			return caller;
		}

		public Level getPriority() {
			return priority;
		}

		@Override
		public String toString() {
			return "<log-entry><priority>" + getPriority() + "</priority><caller>" + getObjectDescription(getCaller())
				+ "</caller><message>" + getMessage() + "</message>" + (getException().hasValue() ?
				("<exception>" + printStackTraceToString(getException().get()) + "</exception>") : "<exception />") + "</log-entry>";
		}
		
		/**
		 * Joins the {@link #getMessage() messages} of the given {@link LogEntry} {@link Collection}.
		 */
		public static String joinMessages(Collection<LogEntry> logEntries, String separator) {
			StringBuilder result = new StringBuilder();
			for (LogEntry entry : logEntries) {
				result.append(entry.getMessage());
				result.append(separator);
			}
			if (!logEntries.isEmpty()) {
				result.delete(result.length() - separator.length(), result.length());
			}
			return result.toString();
		}

		private static String printStackTraceToString(Throwable exception) {
			Writer writer = new StringWriter();
			exception.printStackTrace(new PrintWriter(writer));
			return writer.toString();
		}
	}

	private static final ListenerRegistry<LogEntry> _listenerRegistry =
		ListenerRegistryFactory.getInstance().createWeakConcurrent();

	/**
	 * Logs a message as debug information.
	 * <p>
	 * The message is only produced if debug logging is enabled for the given caller.
	 * </p>
	 * 
	 * @param message
	 *        Is not allowed to be null.
	 * @param caller
	 *        The class from which the logger is called. Is not allowed to be null.
	 */
	public static void debug(Supplier<String> message, Class<?> caller) {
		if (isDebugEnabled(caller)) {
			log(message.get(), caller, Level.DEBUG);
		}
	}

    /**
     * Logs a message as debug information.
     * @param aMessage the message to log
     * @param aCaller the caller of the method
     */
    public static void debug (String aMessage, Object aCaller) {
		log(aMessage, aCaller, Level.DEBUG);
    }

   /**
    * Logs a message and a Throwable as debug information.
    *
    * @param aMessage the message to log
    * @param anException the exception to log
    * @param aCaller the caller of the method
    *
    * #author Klaus Halfmann, Gordian Gossen
    */
   public static void debug (String aMessage,
                                          Throwable anException,
                                          Object aCaller) {
		log(aMessage, anException, aCaller, Level.DEBUG);
   }


   /**
    * Logs a message as information.
    *
    * @param aMessage the message to log
    * @param aCaller the caller of the method
    */
   public static void info (String aMessage, Object aCaller) {
		log(aMessage, aCaller, Level.INFO);
   }

   /**
    * Logs a message and a Throwable as information.
    *
    * @param aMessage the message to log
    * @param anException the exception to log
    * @param aCaller the caller of the method
    *
    * #author Gordian Gossen
    */
   public static void info (String aMessage,
                                         Throwable anException,
                                         Object aCaller) {
		log(aMessage, anException, aCaller, Level.INFO);
   }


   /**
    * Logs a message as warning.
    *
    * @param aMessage the message to log
    * @param aCaller the caller of the method
    */
    public static void warn (String aMessage, Object aCaller) {
		log(aMessage, aCaller, Level.WARN);
   }

   /**
    * Logs a message and a Throwable as warning.
    *
    * @param aMessage the message to log
    * @param anException the exception to log
    * @param aCaller the caller of the method
    *
    * #author Gordian Gossen
    */
    public static void warn (String aMessage,
                                          Throwable anException,
                                          Object aCaller) {
		log(aMessage, anException, aCaller, Level.WARN);
   }


   /**
    * Logs a message as error.
    *
    * @param aMessage the message to log
    * @param aCaller the caller of the method
    */
    public static void error (String aMessage, Object aCaller) {
		log(aMessage, aCaller, Level.ERROR);
   }

   /**
    * Logs a message and a Throwable as error.
    *
    * @param aMessage the message to log
    * @param anException the exception to log
    * @param aCaller the caller of the method
    *
    * #author Gordian Gossen
    */
    public static void error (String aMessage,
                                           Throwable anException,
                                           Object aCaller) {
		log(aMessage, anException, aCaller, Level.ERROR);
   }

   /**
    * Logs a message as fatal.
    *
    * @param aMessage the message to log
    * @param aCaller the caller of the method
    */
   public static void fatal (String aMessage, Object aCaller) {
		log(aMessage, aCaller, Level.FATAL);
   }

    /**
     * Logs a message and a Throwable as fatal.
     *
     * @param aMessage the message to log
     * @param anException the exception to log
     * @param aCaller the caller of the method
     *
     * #author Gordian Gossen
     */
   public static void fatal (String aMessage,
                                          Throwable anException,
                                          Object aCaller) {
		log(aMessage, anException, aCaller, Level.FATAL);
   }

    /**
     * Returns if debugging for the aCaller is enabled.
     *
     * @param aCaller the caller of the method
     *
     * @return true if debugging for the caller is enabled, else false
     */
    public static boolean isDebugEnabled (Object aCaller) {
		return isEnabledFor(LoggerFactory.getLogger(getCallerName(aCaller)), Level.DEBUG);
    }

    /**
    * Reset the settings of the Logger to their default values.
    */
    public static synchronized void reset () {
		LogConfigurator.getInstance().reset();
    }

	/**
	 * Changes to configuration defined by the given resource.
	 *
	 * @param configuration
	 *        Pointer to the configuration, e.g. a {@link URI} describing a log configuration file.
	 */
	public static synchronized void configure(URL configuration) {
		LogConfigurator.getInstance().configure(configuration);
	}

    /**
     * Configure logging to STDOUT using the given log level.
     * <p>
     * This method is mainly intended to manipulate the amount
     * of logging in test cases.
     * </p>
     * <p>
     * You should normally use the no-parameter version of
     * this method.
     * </p>
     * <p>
     * N.B. This method should only be used in main-methods and similar
     * situations.
     * </p>
     *
     * @param aLevel one of  DEBUG, INFO, WARN, ERROR, FATAL
     *
     * @see #configureStdout()
     */
    public static void configureStdout (String aLevel) {
		LogConfigurator.getInstance().configureStdout(aLevel);
    }

    /**
	 * Configure logging to STDOUT using the log level specified in the system property
	 * <code>com.top_logic.basic.Logger.stdoutLogLevel</code> or <code>ERROR</code> if not present.
	 * <p>
	 * This method is prefered since it allows the user more flexibility and avoids hardcoding a
	 * log-level overall.
	 * </p>
	 * <p>
	 * N.B. This method should only be used in main-methods and similar situations.
	 * </p>
	 *
	 * @see #configureStdout(String)
	 */
    public static void configureStdout () {
		LogConfigurator.getInstance().configureStdout();
    }

    /**
     * Log the specified information.
     *
     * @param aMessage       the message to log if not null
     * @param anException    the Throwable to log if not null
     * @param aCaller        the caller
     * @param aPriority      the priority to log with
     *
     */
    private static void log(String aMessage, Throwable anException, Object aCaller, Level aPriority) {
		String callerName = getCallerName(aCaller);
		org.slf4j.Logger logger = LoggerFactory.getLogger(callerName);

		if (isTraceExceptions()) {
			if (anException == null && isTraceMessages()) {
				anException = new StackTrace(null).truncate(2);
			}
		} else {
			if (anException != null) {
				if (!isEnabledFor(logger, aPriority)) {
					// Early exit without transforming message.
					return;
				}

				if (aMessage != null) {
					aMessage += " : " + anException.toString();
				}
				else {
					aMessage = anException.toString();
				}
			}
        }

		log(logger, aPriority, aMessage, anException);

		LogEntry theEntry;

		if (!isTraceExceptions(aPriority)) {
			theEntry = new LogEntry(aMessage, (Throwable) null, aCaller, aPriority);
		} else {
			theEntry = new LogEntry(aMessage, anException, aCaller, aPriority);
		}

		_listenerRegistry.notify(theEntry);
    }

    /**
     * Log the specified information.
     *
     * @param aMessage       the message to log
     * @param aCaller        the caller
     * @param aPriority      the priority to log with
     */
    private static void log(String aMessage, Object aCaller, Level aPriority) {
		log(aMessage, null, aCaller, aPriority);
    }

	private static boolean isEnabledFor(org.slf4j.Logger logger, Level aPriority) {
		switch (aPriority) {
			case DEBUG:
				return logger.isDebugEnabled();
			case INFO:
				return logger.isInfoEnabled();
			case WARN:
				return logger.isWarnEnabled();
			case ERROR:
				return logger.isErrorEnabled();
			case FATAL:
				return logger.isErrorEnabled(FATAL_MARKER);
			default:
				throw new UnreachableAssertion("No such level: " + aPriority);
		}
	}

	private static void log(org.slf4j.Logger logger, Level aPriority, String aMessage, Throwable anException) {
		if ((anException != null) && !Logger.isTraceExceptions(aPriority)) {
			anException = null;
		}

		switch (aPriority) {
			case DEBUG:
				logger.debug(aMessage, anException);
				break;
			case INFO:
				logger.info(aMessage, anException);
				break;
			case WARN:
				logger.warn(aMessage, anException);
				break;
			case ERROR:
				logger.error(aMessage, anException);
				break;
			case FATAL:
				logger.error(FATAL_MARKER, aMessage, anException);
				break;
			default:
				throw new UnreachableAssertion("No such level: " + aPriority);
		}
	}

    /**
     * Get an output name for the given object
     *
     * @param aCaller the calling object
     *
     * @return an output name
     */
    private static String getCallerName (Object aCaller) {
        if (aCaller == null) {
           return "null";
        }
        if (aCaller instanceof String) {
            return (String) aCaller;
        }
        if (aCaller instanceof Class) {
            return ((Class) aCaller).getName ();
        }
        return aCaller.getClass ().getName ();
    }

	public static boolean isTraceExceptions(Level requestedLevel) {
		return requestedLevel.ordinal() >= LogConfigurator.getInstance().getExceptionLevel().ordinal();
	}

	public static String getExceptionLevel() {
		return LogConfigurator.getInstance().getExceptionLevel().name();
	}

	public static void setExceptionLevel(String aNewLevel) {
		LogConfigurator.getInstance().setExceptionLevel(aNewLevel);
	}

	public static boolean isTraceExceptions() {
		return LogConfigurator.getInstance().isTraceExceptions();
    }

    public static void setTraceExceptions(boolean aTraceExceptions) {
		LogConfigurator.getInstance().setTraceExceptions(aTraceExceptions);
    }

    public static boolean isTraceMessages() {
		return LogConfigurator.getInstance().isTraceMessages();
    }

    public static void setTraceMessages(boolean aTraceMessages) {
		LogConfigurator.getInstance().setTraceMessages(aTraceMessages);
    }

	/**
	 * Getter for the {@link ListenerRegistry}.
	 * <p>
	 * If a {@link Listener} throws an exception, it propagates to the caller of the log and
	 * probably kills that part of the application. Therefore: <b>Be careful!</b>
	 * </p>
	 */
	public static ListenerRegistry<LogEntry> getListenerRegistry() {
		return _listenerRegistry;
	}

}
