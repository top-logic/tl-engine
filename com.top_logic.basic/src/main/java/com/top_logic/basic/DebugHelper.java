/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.text.DateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.StopWatch;

/**
 * Some utility methods you may need when Debugging or Logging.
 *
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public abstract class DebugHelper {

	private static final String PASSWORD = "password";

	/**
	 * Pattern finding the password entry within a {@link HttpServletRequest#getQueryString()}.
	 * 
	 * @see #replacePassword(String, String)
	 */
	public static final Pattern PASSWORD_QUERY_PART = Pattern.compile("(?<=\\b" + PASSWORD + "=)[^&]*");

    /** Need this for {@link #assertEquals(double, double)} assertions */ 
    public static final double EPSILON = 1E-4;

	/**
	 * A message describing the current request can be added as
	 * {@link HttpServletRequest#setAttribute(String, Object)}.
	 * 
	 * @see #logTiming(HttpServletRequest, String, long, Class)
	 */
	public static final String SERVICE_MSG_ATTR = DebugHelper.class.getName() + "maxServiceMsg";

    /**
	 * Convert millisecond time duration to minutes, seconds and milliseconds.
	 *
	 * @param time
	 *        duration in milliseconds as given by {@link System#currentTimeMillis()}, usually
	 *        <code>System.currentTimeMillis() - startTime</code>
	 * @return a human readable representation of that duration for absurd reasons ending with a '.'
	 *         character, see {@link #getTimeOnly(long)}.
	 */
    public static String getTime(long time) {
		StringBuilder result = new StringBuilder(64);

		appendTime(result, time);

		// For absurd compatibility reasons, the "time" always ends with a dot. KHA used this only
		// at the end of a logger message???
		result.append('.');

		return result.toString();
	}

	/**
	 * Convert millisecond time duration to minutes, seconds and milliseconds.
	 *
	 * @param time
	 *        duration in milliseconds as given by {@link System#currentTimeMillis()}, usually
	 *        <code>System.currentTimeMillis() - startTime</code>
	 * @return a human readable representation of that duration.
	 */
	public static String getTimeOnly(long time) {
		StringBuilder result = new StringBuilder(64);
		appendTime(result, time);
		return result.toString();
	}

	/**
	 * Appends millisecond time duration as minutes, seconds and milliseconds to the given output.
	 *
	 * @param result
	 *        The result buffer.
	 * @param time
	 *        duration in milliseconds
	 */
	public static void appendTime(StringBuilder result, long time) {
		long millis = time;

		long hours = millis / (1000 * 60 * 60);
		millis -= hours * 1000 * 60 * 60;

		long minutes = millis / (1000 * 60);
		millis -= minutes * 1000 * 60;

		long seconds = millis / 1000;
		millis -= seconds * 1000;

		boolean hasValue = false;
		if (hours > 0) {
			result.append(hours);
			result.append(" h");
			hasValue = true;
		}
        if (minutes > 0) {
			if (hasValue) {
				result.append(' ');
			}
			result.append(minutes);
			result.append(" min");
			hasValue = true;
        }
        if (seconds > 0) {
			if (hasValue) {
				result.append(' ');
			}
			result.append(seconds);
			result.append(" s");
			hasValue = true;
        }
		if (millis > 0 || !hasValue) {
			if (hasValue) {
				result.append(' ');
			}
			result.append(millis);
			result.append(" ms");
		}
	}

    /**
     * Convert long time to a time with minutes, seconds and milliseconds.
     *
     * @param time
     *            time in milliseconds as given by {@link System#currentTimeMillis()}
     * @return a human readable representation of that time
     */
    public static String formatTime(long time) {
        return getTimeFormat().format(new Date(time));
    }

    /**
     * Convert the given duration in milliseconds to a excel readable time format ("mm:ss,SSS").
     *
     * @param    duration    A duration in milliseconds.
     * @return   A human and excel readable representation of that duration.
     */
    public static String toDuration(long duration) {
        StringBuilder result = new StringBuilder(64);
        if (duration < 0) {
        	result.append('-');
        	duration = - duration;
        }

        long minutes  = duration / (1000 * 60);
        long seconds  = (duration - minutes * 1000 * 60) / 1000;
        long millisec = (duration - minutes * 1000 * 60) - seconds * 1000;

        if (minutes < 10) {
        	result.append('0');
        }
        result.append(minutes).append(':');
        if (seconds < 10) {
        	result.append('0');
        };
        result.append(seconds).append(',');
        if (millisec < 100) {
        	result.append('0');
        	if (millisec < 10) {
        		result.append('0');
        	}
        }
        result.append(millisec);

        return result.toString();
    }

    /**
     * Logs current memory usage of the Java VM.
     */
    public static void logMemory() {
        Runtime rt = Runtime.getRuntime();
        StringBuilder sb = new StringBuilder(256);
        sb.append("Memory usage:\n");
        sb.append("Used Memory in VM: " + toMB(rt.totalMemory() - rt.freeMemory()) + " MB\n");
        sb.append("Free Memory in VM: " + toMB(rt.freeMemory()) + " MB\n");
        sb.append("Current VM Size: " + toMB(rt.totalMemory()) + " MB\n");
        sb.append("Still available to VM: " + toMB(rt.maxMemory() - rt.totalMemory()) + " MB\n");
        sb.append("Max VM Size: " + toMB(rt.maxMemory()) + " MB\n");
        Logger.info(sb.toString(), DebugHelper.class);
    }



    /**
     * Converts bytes to megabytes.
     *
     * @param bytes
     *        the byte value to convert
     * @return the given value as megabyte value
     */
    public static long toMB(long bytes) {
        return bytes / (1024 * 1024);
    }



    /**
     * Ignores the given object. Does nothing with it.
     *
     * @param aObject
     *            the Object to ignore
     * @return always <code>true</code> as proof that the given object was ignored
     *         successfully
     */
    public static boolean ignore(Object aObject) {
        if (false) aObject.toString();
        return true;
    }



    /**
     * Gets the line number of the file this method was directly called from.
     *
     * @return the line number of the file this method was directly called from
     */
    public static int getLineNumber() {
        return new Exception().getStackTrace()[1].getLineNumber();
    }



    /**
     * Gets the actual stack depth of the method this method was directly called from.
     *
     * @return the actual stack depth of the method this method was directly called from
     */
    public static int getStackDepth() {
        return new Exception().getStackTrace().length - 1;
    }



    /**
     * Checks whether the given Object is an instance of the given class.
     *
     * @param aObject
     *        the object to check for instance
     * @param aFullClassName
     *        the full qualified name of the class
     * @return <code>true</code>, if the given object is an instance of the given class,
     *         <code>false</code> otherwise
     * @throws ClassNotFoundException
     *         if the given class was not found
     */
    public static boolean isInstanceOf(Object aObject, String aFullClassName) throws ClassNotFoundException {
        return Class.forName(aFullClassName).isInstance(aObject);
    }

    /**
     * Checks whether the given Object is an instance of the given class.
     *
     * @param aObject
     *        the object to check for instance
     * @param aClass
     *        the class to check
     * @return <code>true</code>, if the given object is an instance of the given class,
     *         <code>false</code> otherwise
     */
    public static boolean isInstanceOf(Object aObject, Class aClass) {
        return aClass.isInstance(aObject);
    }

    /**
     * Checks whether the given Object is a <b>direct</b> instance of the given class.
     *
     * @param aObject
     *        the object to check for instance
     * @param aShortOrFullClassName
     *        the name of the class (either full class name or a suffix of it, whereas the
     *        short class name is a suffix of the full class name)
     * @return <code>true</code>, if the given object is a <b>direct</b> instance of the
     *         given class, <code>false</code> otherwise
     */
    public static boolean isDirectInstanceOf(Object aObject, String aShortOrFullClassName) {
        return aObject == null ? false : aObject.getClass().getName().endsWith(aShortOrFullClassName);
    }

    /**
     * Checks whether the given Object is a <b>direct</b> instance of the given class.
     *
     * @param aObject
     *        the object to check for instance
     * @param aClass
     *        the class to check for <b>direct</b> instance
     * @return <code>true</code>, if the given object is a <b>direct</b> instance of the
     *         given class, <code>false</code> otherwise
     */
    public static boolean isDirectInstanceOf(Object aObject, Class aClass) {
        return aObject == null ? false : aObject.getClass().getName().equals(aClass.getName());
    }


    /**
     * Checks whether this method is called directly or indirectly from a method with the
     * given name (whether a method with the given name occurs in the stack trace).<br/>
     * For example: DebugHelper.calledBy("setModel");<br/>
     * <br/>
     * This method may be useful for Eclipse breakpoint conditions or maybe for (security)
     * restrictions of public methods to omit method calls from unauthorized points.
     *
     * @param aMethodName
     *            the name of the method to search for in the stack trace
     * @return <code>true</code>, if this method was called directly or indirectly from a
     *         method with the given name, <code>false</code> otherwise
     */
    public static boolean calledBy(String aMethodName) {
        StackTraceElement[] theStackTrace = new Exception().getStackTrace();
        for (int i = 1; i < theStackTrace.length; i++) {
            if (theStackTrace[i].getMethodName().equals(aMethodName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether this method is called directly or indirectly from a method with the
     * given name within a file at the given line number.<br/>
     * For example: DebugHelper.calledBy("supportsObject", 215);<br/>
     * <br/>
     * This method may be useful for Eclipse breakpoint conditions or maybe for (security)
     * restrictions of public methods to omit method calls from unauthorized points.
     *
     * @param aMethodName
     *            the name of the method to search for in the stack trace
     * @param aLineNumber
     *            the line number of the file to search for in the stack trace
     * @return <code>true</code>, if this method was called directly or indirectly from a
     *         method with the given name within a file with the given line number,
     *         <code>false</code> otherwise
     */
    public static boolean calledBy(String aMethodName, int aLineNumber) {
        StackTraceElement[] theStackTrace = new Exception().getStackTrace();
        for (int i = 1; i < theStackTrace.length; i++) {
            if (theStackTrace[i].getMethodName().equals(aMethodName)
                    && theStackTrace[i].getLineNumber() == aLineNumber) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether this method is called directly or indirectly from a method with the
     * given name within the given file.<br/>
     * For example: DebugHelper.calledBy("attach", "TextInputControl.java");<br/>
     * <br/>
     * This method may be useful for Eclipse breakpoint conditions or maybe for (security)
     * restrictions of public methods to omit method calls from unauthorized points.
     *
     * @param aMethodName
     *            the name of the method to search for in the stack trace
     * @param aFileName
     *            the name of the file to search for in the stack trace
     * @return <code>true</code>, if this method was called directly or indirectly from a
     *         method with the given name within the given file, <code>false</code>
     *         otherwise
     */
    public static boolean calledBy(String aMethodName, String aFileName) {
        StackTraceElement[] theStackTrace = new Exception().getStackTrace();
        for (int i = 1; i < theStackTrace.length; i++) {
            if (theStackTrace[i].getMethodName().equals(aMethodName)
                    && theStackTrace[i].getFileName().equals(aFileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether this method is called directly or indirectly from a method with the
     * given name within the given file at the given line number.<br/>
     * For example: DebugHelper.calledBy("allow", "BoundComponent.java", 248);<br/>
     * <br/>
     * This method may be useful for Eclipse breakpoint conditions or maybe for (security)
     * restrictions of public methods to omit method calls from unauthorized points.
     *
     * @param aMethodName
     *            the name of the method to search for in the stack trace
     * @param aFileName
     *            the name of the file to search for in the stack trace
     * @param aLineNumber
     *            the line number of the file to search for in the stack trace
     * @return <code>true</code>, if this method was called directly or indirectly from a
     *         method with the given name within the given file at the given line number,
     *         <code>false</code> otherwise
     */
    public static boolean calledBy(String aMethodName, String aFileName, int aLineNumber) {
        StackTraceElement[] theStackTrace = new Exception().getStackTrace();
        for (int i = 1; i < theStackTrace.length; i++) {
            if (theStackTrace[i].getMethodName().equals(aMethodName)
                    && theStackTrace[i].getFileName().equals(aFileName)
                    && theStackTrace[i].getLineNumber() == aLineNumber) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether this method is called directly or indirectly from a method within a
     * file at the given line number (whether a file with the given line number occurs in
     * the stack trace).<br/>
     * For example: DebugHelper.calledBy(512);<br/>
     * <br/>
     * This method may be useful for Eclipse breakpoint conditions or maybe for (security)
     * restrictions of public methods to omit method calls from unauthorized points.
     *
     * @param aLineNumber
     *            the line number of the file to search for in the stack trace
     * @return <code>true</code>, if this method was called directly or indirectly from a
     *         method within a file at the given line number, <code>false</code> otherwise
     */
    public static boolean calledBy(int aLineNumber) {
        StackTraceElement[] theStackTrace = new Exception().getStackTrace();
        for (int i = 1; i < theStackTrace.length; i++) {
            if (theStackTrace[i].getLineNumber() == aLineNumber) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether this method is called directly or indirectly from a file with the
     * given name (whether a file with the given name occurs in the stack trace).<br/>
     * For example: DebugHelper.calledByFile("StringServices.java");<br/>
     * <br/>
     * This method may be useful for Eclipse breakpoint conditions or maybe for (security)
     * restrictions of public methods to omit method calls from unauthorized points.
     *
     * @param aFileName
     *            the name of the file to search for in the stack trace
     * @return <code>true</code>, if this method was called directly or indirectly from a
     *         file with the given name, <code>false</code> otherwise
     */
    public static boolean calledByFile(String aFileName) {
        StackTraceElement[] theStackTrace = new Exception().getStackTrace();
        for (int i = 1; i < theStackTrace.length; i++) {
            if (theStackTrace[i].getFileName().equals(aFileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether this method is called directly or indirectly from a file with the
     * given name at the given line number.<br/>
     * For example: DebugHelper.calledByFile("EditComponent.java", 317);<br/>
     * <br/>
     * This method may be useful for Eclipse breakpoint conditions or maybe for (security)
     * restrictions of public methods to omit method calls from unauthorized points.
     *
     * @param aFileName
     *            the name of the file to search for in the stack trace
     * @param aLineNumber
     *            the line number of the file to search for in the stack trace
     * @return <code>true</code>, if this method was called directly or indirectly from a
     *         file with the given name, <code>false</code> otherwise
     */
    public static boolean calledByFile(String aFileName, int aLineNumber) {
        StackTraceElement[] theStackTrace = new Exception().getStackTrace();
        for (int i = 1; i < theStackTrace.length; i++) {
            if (theStackTrace[i].getFileName().equals(aFileName)
                    && theStackTrace[i].getLineNumber() == aLineNumber) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether this method is called directly or indirectly from a class with the
     * given name (whether a class with the given name occurs in the stack trace).<br/>
     * For example: DebugHelper.calledByClass("com.top_logic.layout.form.tag.ErrorTag");<br/>
     * <br/>
     * This method may be useful for Eclipse breakpoint conditions or maybe for (security)
     * restrictions of public methods to omit method calls from unauthorized points.
     *
     * @param aFullQualifiedClassName
     *            the full qualified name of the class to search for in the stack trace
     * @return <code>true</code>, if this method was called directly or indirectly from a
     *         class with the given name, <code>false</code> otherwise
     */
    public static boolean calledByClass(String aFullQualifiedClassName) {
        StackTraceElement[] theStackTrace = new Exception().getStackTrace();
        for (int i = 1; i < theStackTrace.length; i++) {
            if (theStackTrace[i].getClassName().equals(aFullQualifiedClassName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether this method is called directly or indirectly from a method with the
     * given name within the given class.<br/>
     * For example: DebugHelper.calledByClass("getRoles",
     *              "com.top_logic.element.boundsec.manager.StorageAccessManager");<br/>
     * <br/>
     * This method may be useful for Eclipse breakpoint conditions or maybe for (security)
     * restrictions of public methods to omit method calls from unauthorized points.
     *
     * @param aMethodName
     *            the name of the method to search for in the stack trace
     * @param aFullQualifiedClassName
     *            the full qualified name of the class to search for in the stack trace
     * @return <code>true</code>, if this method was called directly or indirectly from a
     *         method with the given name within the given class, <code>false</code>
     *         otherwise
     */
    public static boolean calledByClass(String aMethodName, String aFullQualifiedClassName) {
        StackTraceElement[] theStackTrace = new Exception().getStackTrace();
        for (int i = 1; i < theStackTrace.length; i++) {
            if (theStackTrace[i].getMethodName().equals(aMethodName)
                    && theStackTrace[i].getClassName().equals(aFullQualifiedClassName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Display startup info for the System on standard out.
     * Information displayed:
     * <ul>
     * <li>OS</li>
     * <li>User</li>
     * <li>Java</li>
     * </ul>
     */
    public static void infoAndStarter() {
        System.out.println ();
        System.out.println ("             OS: " + getOS ());
        System.out.println ("           User: " + getCurrentUser ());
        printJavaVersion();
    }

    /**
     * Displays information about the used java system on standard out. There are
     * several interesting aspects, which will be collected here. If
     * you are using a security manager, you probably run into problems.
     */
    public static void printJavaVersion () {

        System.out.println ();
        System.out.println ("           Java:");

        try {
            String theVersion = System.getProperty ("java.vm.version");
            String theRuntime = System.getProperty ("java.runtime.name");
            String theName    = System.getProperty ("java.vm.name");
            String theVendor  = System.getProperty ("java.vm.vendor");
            String theInfo    = System.getProperty ("java.vm.info");
            String theClasses = System.getProperty ("java.class.version");
            String theHome    = System.getProperty ("java.home");

            System.out.println ("        Version: " + theVersion + " (" + theName + ')');
            System.out.println ("        Runtime: " + theRuntime);
            System.out.println ("         Vendor: " + theVendor);
            System.out.println ("        Classes: V" + theClasses);
            System.out.println ("           Info: " + theInfo);
            System.out.println ("           Home: " + theHome);
        } catch (SecurityException se) {
            System.out.println("Access denied");
        }
    }

    /**
     * Get the current operating system.
     *
     * @return a string representing the operating system.
     */
    private static String getOS () {
        try {
            return System.getProperty ("os.name")
                + " V" + System.getProperty ("os.version")
                + " ("  + System.getProperty ("os.arch") + ")";
        }
        catch (SecurityException se) {
            return "Access denied";
        }
    }

    /**
     * Get the current user.
     *
     * @return a string representing the user.
     */
    private static String getCurrentUser () {
        try {
            return System.getProperty ("user.name");
        }
        catch (SecurityException se) {
            return "Access denied";
        }
    }
    
    /**
	 * {@link #assertEquals(double, double, double)} with {@link #EPSILON} range.
	 * 
	 * @throws AssertionError
	 *         when d1 and d2 are not equal +/- {@link #EPSILON}
	 */
    public static boolean assertEquals(double d1, double d2) {
        return assertEquals(d1, d2, EPSILON);
    }

    /**
	 * Throws an {@link AssertionError}, if the given values differ more than the given epsilon
	 * range.
	 * 
	 * @throws AssertionError
	 *         If d1 and d2 are not equal +/- epsilon.
	 */
    public static boolean assertEquals(double d1, double d2, double epsilon) {
        double delta = Math.abs(d2-d1);
        if (delta > epsilon) {
            throw new AssertionError("Expected " + d1 + " got " + d2);
        }
        return true;
    }

	public static DateFormat getTimeFormat() {
		return CalendarUtil.newSimpleDateFormat("HH:mm:ss.SSS");
	}

	/**
	 * Creates a textual description of a problem by concatenating all chained exception reasons.
	 * 
	 * @param message
	 *        The top-level description of the problem. If <code>null</code>, the top-level
	 *        exception is the main reason.
	 * @param reason
	 *        The top-level exception that caused the problem. If <code>null</code>, only the given
	 *        message is returned.
	 * @return A chain of reasons for the problem starting with the given message and ending with
	 *         all chained exception messages.
	 */
	public static String fullMessage(String message, Throwable reason) {
		StringBuilder buffer = new StringBuilder();
		if (!StringServices.isEmpty(message)) {
			buffer.append(message);
		}
		while (reason != null) {
			if (!StringServices.isEmpty(reason.getMessage())) {
				dropTailingPunctuation(buffer);
				if (buffer.length() > 0) {
					buffer.append(": ");
				}
				buffer.append(reason.getMessage());
			}
			reason = reason.getCause();
		}
		return buffer.toString();
	}

	private static void dropTailingPunctuation(StringBuilder buffer) {
		int length;
		if ((length = buffer.length()) > 0 && isPunctuation(buffer.charAt(length - 1))) {
			buffer.setLength(length - 1);
		}
	}

	private static boolean isPunctuation(char ch) {
		return ch == '.' || ch == '!';
	}

	/**
	 * Write an info to the log about the duration of the given action.
	 * 
	 * @param request
	 *        The request to get the session from, may be <code>null</code>.
	 * @param action
	 *        The action performed.
	 * @param watch
	 *        The {@link StopWatch} to take the elapsed time from.
	 * @param limit
	 *        The time in milliseconds that must elapse before writing a log entry.
	 * @param sender
	 *        The location where the timing is logged.
	 */
	public static void logTiming(HttpServletRequest request, String action, StopWatch watch, long limit, Class<?> sender) {
		logTiming(request, action, watch.getElapsedMillis(), limit, sender);
	}

	/**
	 * Write an info to the log about the duration of the given action.
	 * 
	 * @param request
	 *        The request to get the session from, may be <code>null</code>.
	 * @param action
	 *        The action performed.
	 * @param elapsed
	 *        The number of milliseconds measured.
	 * @param limit
	 *        The time in milliseconds that must elapse before writing a log entry.
	 * @param sender
	 *        The location where the timing is logged.
	 */
	public static void logTiming(HttpServletRequest request, String action, long elapsed, long limit, Class<?> sender) {
		if (elapsed > limit || Logger.isDebugEnabled(sender)) {
			logTiming(request, action, elapsed, sender);
		}
	}

	/**
	 * Write an info to the log about the duration of the given action.
	 * 
	 * @param request
	 *        The request to get the session from, may be <code>null</code>.
	 * @param action
	 *        The action performed.
	 * @param watch
	 *        The {@link StopWatch} to take the elapsed time from.
	 * @param sender
	 *        The location where the timing is logged.
	 */
	public static void logTiming(HttpServletRequest request, String action, StopWatch watch, Class<?> sender) {
		logTiming(request, action, watch.getElapsedMillis(), sender);
	}

	/**
	 * Write an info to the log about the duration of the given action.
	 * 
	 * @param request
	 *        The request to get the session from, may be <code>null</code>.
	 * @param action
	 *        The action performed.
	 * @param elapsed
	 *        The time in milliseconds the given action took.
	 * @param sender
	 *        The location where the timing is logged.
	 */
	public static void logTiming(HttpServletRequest request, String action, long elapsed, Class<?> sender) {
		StringBuilder message = new StringBuilder();
		message.append(action);

		if (request != null) {
			String serviceMessage = (String) request.getAttribute(SERVICE_MSG_ATTR);
			if (StringServices.isEmpty(serviceMessage)) {
				message.append(' ');
				message.append(request.getRequestURI());

				String query = request.getQueryString();
				if (!StringServices.isEmpty(query)) {
					message.append('~');
					message.append(replacePassword(query, "***removed***"));
				}
			}

			HttpSession session = request.getSession(false);
			if (session != null) {
				message.append(" (session: ");
				message.append(session.getId());
				message.append(')');
			}
		}

		message.append(" took ");
		appendTime(message, elapsed);
		message.append(".");
		Logger.info(message.toString(), sender);
	}

	/**
	 * Replaces the {@link #PASSWORD password value} within a
	 * {@link HttpServletRequest#getQueryString()} by the given replacement value.
	 * 
	 * @param query
	 *        The query returned by {@link HttpServletRequest#getQueryString()}.
	 * @param replacement
	 *        The string to include into the result set instead of the password.
	 * @return Either the original string if it does not contain a password or the string where the
	 *         {@link #PASSWORD password value} is replaced by the given replacement.
	 */
	public static String replacePassword(String query, String replacement) {
		Matcher matcher = PASSWORD_QUERY_PART.matcher(query);
		if (matcher.find()) {
			StringBuffer sb = new StringBuffer();
			do {
				matcher.appendReplacement(sb, replacement);
			} while (matcher.find());
			matcher.appendTail(sb);
			return sb.toString();
		} else {
			return query;
		}
	}

}
