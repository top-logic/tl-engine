/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.base.accesscontrol.LoginPageServlet;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLInteractionContext;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.multipart.MultipartRequest;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.logging.LogConfigurator;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.RunnableEx2;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ProcessingInfo;
import com.top_logic.layout.ProcessingKind;
import com.top_logic.layout.URLPathBuilder;
import com.top_logic.layout.admin.component.PerformanceMonitor;
import com.top_logic.util.filter.CompressionFilter;
import com.top_logic.util.filter.CompressionServletResponseWrapper;

/**
 * This is the upper class for Dispatcher- and LoginPageServlet. It provides
 * the main functionality to forward requests to other system components.
 * 
 * TODO refactor into Filters
 *
 *                                      invalidate
 * @author    <a href="mailto:karsten.buch@top-logic.com">Karsten Buch</a>
 */
public class TopLogicServlet extends HttpServlet {

	/**
	 * The name of log mark for the session id.
	 * <p>
	 * <em>When the value is changed, the Log4j configuration files have to be updated, as they use
	 * the value.</em>
	 * </p>
	 * <p>
	 * The value of this constant is used in Log4J configuration files to extract and log the
	 * session id with every log statement.
	 * </p>
	 */
	private static final String TL_SESSION_ID_LOG_MARK = "tl-session-id";

	/**
	 * Servlet-parameter for enabling adaptive compression.
	 */
    public static final String ALWAYS_ZIP_PARAM = "alwaysZip";

    /** Used when logging without actual session*/
    public static final String NO_SESSION = "*** No Session ***";

    /** Default {@link #maxServiceTime} if nothing else is configured */
    public static final long MAX_SERVICE_TIME = 1000 * 2; // 2 Seconds
    
    /** Subclasses may configure there maximal Service time here  */
    public static final String MAX_SERVICE_ATTR = TopLogicServlet.class.getName() + "maxServiceTime";

    /** When this amount of milli-seconds is exceeded a warning will be logged. */
    protected long maxServiceTime;

    /** Always use gzip compression, if possible */
	private boolean alwaysZip;
	
	/**
	 * Configuration for {@link TopLogicServlet}.
	 */
	public interface Config extends ConfigurationItem {
		/**
		 * Maximal service time according to configuration. See {@link Config#getMaxServiceTime}.
		 */
		String MAX_SERVICE_TIME_CONFIG = "maxServiceTime";

		/** Getter for {@link Config#MAX_SERVICE_TIME_CONFIG}. */
		@Name(MAX_SERVICE_TIME_CONFIG)
		@StringDefault(StringServices.EMPTY_STRING)
		String getMaxServiceTime();
	}

	/**
	 * Getter for the configuration.
	 */
	public Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	/**
	 * Getter for {@link Config#MAX_SERVICE_TIME_CONFIG}.
	 */
	public String getMaxServiceTime() {
		return getConfig().getMaxServiceTime();
	}

    /**
	 * Configure maxServiceTime by web.xml or top-logic.config.xml.
	 */
    @Override
	public void init(ServletConfig aConfig) throws ServletException {
        super.init(aConfig);
        
        alwaysZip     = StringServices.parseBoolean(aConfig.getInitParameter(ALWAYS_ZIP_PARAM));
        initMaxServiceTime();
    }

    /**
	 * Setup maxServiceTime for this {@link TopLogicServlet}.
	 */
    public void initMaxServiceTime() throws NumberFormatException {
        String maxServiceTimeS = this.getInitParameter("maxServiceTime"); 
        maxServiceTime = MAX_SERVICE_TIME;
        if (StringServices.isEmpty(maxServiceTimeS)) {
			maxServiceTimeS = getMaxServiceTime();
        }
        if (!StringServices.isEmpty(maxServiceTimeS)) {
            maxServiceTime = Long.parseLong(maxServiceTimeS);
        }
    }

	/**
	 * Validate request and set context information for current thread.
	 * 
	 * @param request
	 *        The send request.
	 * @param response
	 *        The send response.
	 * 
	 * @throws ServletException
	 *         If the request could not be handled.
	 * @throws IOException
	 *         If an input or output error is detected.
	 */
	@Override
	public final void service(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		/* The type parameters are necessary here. Without them, Eclipse reports an error. */
		TopLogicServlet.<IOException, ServletException> withSessionIdLogMark(request,
			() -> serviceWithLogMark(request, response));
	}

	private void serviceWithLogMark(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		setCachePolicy(response);

		final TLSessionContext session = this.getSession(request, response);
		if (session == null) {
			// User has no session.
			redirectToLogout(request, response);
			return;
		}

		TLSubSessionContext subession = TLContextManager.getSubSession();
		if (subession == null) {
			enterContext(session, request, response);
			return;
		}

		if (subession.getSessionContext() != session) {
			// There is something really wrong. A per-thread context is already set up that does not
			// match the current session context.
			Logger.error(
				"Enforcing logout due to session missmatch: " + subession.getPerson() + " vs. "
					+ session.getOriginalUser(), TopLogicServlet.class);
			invalidateSession(request);
			redirectToLogout(request, response);
			return;
		}

		// Recursive call, through a request dispatcher include call.
		inContext(request, response);
	}

	/** Sets a log mark with the session id while executing the runnable. */
	public static <E1 extends Throwable, E2 extends Throwable> void withSessionIdLogMark(HttpServletRequest request,
			RunnableEx2<E1, E2> runnable) throws E1, E2 {
		LogConfigurator logConfigurator = LogConfigurator.getInstance();
		String oldLogMark = logConfigurator.getLogMark(TL_SESSION_ID_LOG_MARK);
		logConfigurator.addLogMark(TL_SESSION_ID_LOG_MARK, getSessionIdForLog(request));
		try {
			runnable.run();
		} finally {
			if (oldLogMark == null) {
				logConfigurator.removeLogMark(TL_SESSION_ID_LOG_MARK);
			} else {
				logConfigurator.addLogMark(TL_SESSION_ID_LOG_MARK, oldLogMark);
			}
		}
	}

	private static String getSessionIdForLog(HttpServletRequest request) {
		if (request == null) {
			/* This happens in tests. */
			return null;
		}
		String sessionId = getSessionId(request);
		if (StringServices.isEmpty(sessionId) || sessionId.equals(NO_SESSION)) {
			return null;
		}
		return hashSessionIdForLog(sessionId);
	}

	/**
	 * The session id in a form in which it can be logged.
	 * <p>
	 * The session id must not be logged directly. That would be a security hole. It would allow a
	 * reader of the log to take over the session.
	 * </p>
	 */
	public static String hashSessionIdForLog(String rawSessionId) {
		try {
			MessageDigest digester = MessageDigest.getInstance(MessageDigestAlgorithms.SHA3_256);
			byte[] digest = digester.digest(rawSessionId.getBytes(StandardCharsets.UTF_8));
			return "S(" + Base64.getEncoder().encodeToString(digest) + ") ";
		} catch (NoSuchAlgorithmException exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Invalidates the current session.
	 */
	@CalledFromJSP
	protected final void invalidateSession(final HttpServletRequest aRequest) {
		HttpSession session = aRequest.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

	void inContext(final HttpServletRequest aRequest, final HttpServletResponse aResponse) {
		try {
			{
				boolean debug = Logger.isDebugEnabled(this);
				if (debug) {
					Logger.debug("Going to super.service()...", TopLogicServlet.class);
				}
				doService(aRequest, aResponse);
				if (debug) {
					Logger.debug("End super.service()!", TopLogicServlet.class);
				}
			}
		} catch (IOException ex) {
			Throwable cause = ex;
			do {
				final String message = cause.getMessage();
				if (message != null
					&& (message.contains("Connection reset by peer") || message.contains("Output closed"))) {
					Logger
						.debug("Problem in service, stream was probably closed by browser", ex, TopLogicServlet.class);
					break;
				}
				cause = cause.getCause();
			} while (cause != null);

			// no inner exceptions contains text which causes us to ignore it
			if (cause == null) {
				Logger.error("Internal error.", ex, TopLogicServlet.class);
				throw new RuntimeException(ex); // will be handled by ErrorPage
			}
		} catch (ServletException ex) {
			// so it is logged here, too
			Logger.error("Internal error.", ex, TopLogicServlet.class);
			throw new RuntimeException(ex);
		} catch (RuntimeException ex) {
			// so it is logged here, too
			Logger.error("Internal error.", ex, TopLogicServlet.class);
			throw ex;
		} catch (Error ex) {
			// so it is logged here, too
			Logger.error("Internal error.", ex, TopLogicServlet.class);
			throw ex;
		}
	}

	private void enterContext(final TLSessionContext sessionContext, final HttpServletRequest rawRequest,
			final HttpServletResponse rawResponse) throws IOException {
		// The per-thread context has not yet been set up. This is the first hit of the request
		// to a servlet.

		/* Compress response if configured */
		final HttpServletResponse wrappedResponse;
		boolean wrapResponse =
			alwaysZip &&
				CompressionFilter.supportsCompression(rawRequest) &&
				!alreadyCompressed(rawResponse);
		if (wrapResponse) { // avoid
			CompressionServletResponseWrapper compressedResponse =
				new CompressionServletResponseWrapper(rawResponse);
			compressedResponse.setCompressionThreshold(CompressionFilter.MIN_THRESHOLD);
			wrappedResponse = compressedResponse;
		} else {
			wrappedResponse = rawResponse;
		}

		/* Variable is used in finally block to ensure that the IOException that might be thrown
		 * during finishing response doesn't mask the application specific exception. */
		boolean errorOccurred = true;
		try {
			/* Ensure a consistent handling of multi-part and simple requests. */
			final HttpServletRequest wrappedRequest;
			if ((!(rawRequest instanceof MultipartRequest)) && MultipartRequest.isMultipartContent(rawRequest)) {
				wrappedRequest = new MultipartRequest(rawRequest);
			} else {
				wrappedRequest = rawRequest;
			}

			TLContextManager.inInteraction(sessionContext, getServletContext(), wrappedRequest, wrappedResponse,
				new InContext() {
				@Override
				public void inContext() {
					long start = System.currentTimeMillis();
					TopLogicServlet.this.inContext(wrappedRequest, wrappedResponse);
					doPerformanceMeasuring(start, TLContextManager.getInteraction());
					logTiming(wrappedRequest, start);
				}
			});
			errorOccurred = false;
		} finally {
			if (wrapResponse) {
				try {
					((CompressionServletResponseWrapper) wrappedResponse).finishResponse();
				} catch (IOException ex) {
					/* Such an exception happens for example when a download was requested using
					 * IE, but it was canceled in the the dialog to select store, open, or
					 * cancel. In that case finishing will force an ClientAbortException in IE.
					 * (see #3654) */
					Logger.debug("Problem finishing compressed response.", ex, TopLogicServlet.class);
					if (!errorOccurred) {
						// No other exception thrown, so rethrow IOEx
						throw ex;
					}
				}
			}
		}
	}

	private boolean alreadyCompressed(ServletResponse response) {
		while (response instanceof ServletResponseWrapper) {
			if (response instanceof CompressionServletResponseWrapper) {
				return true;
			}
			response = ((ServletResponseWrapper) response).getResponse();
		}
		return false;
	}

	/**
	 * Record performance data
	 * 
	 * @param startTime
	 *        the time when the processing started
	 * @param aContext
	 *        the current {@link DisplayContext}
	 */
	protected void doPerformanceMeasuring(long startTime, TLInteractionContext aContext) {
		if (PerformanceMonitor.isEnabled() && aContext != null) {
			ProcessingInfo processingInfo = aContext.getProcessingInfo();
			ResKey commandInfo = processingInfo.getCommandName();
			Object contextInfo = processingInfo.getComponentName();

			String theContext = (contextInfo != null) ? contextInfo.toString() : "";
			ProcessingKind processingKind = processingInfo.getProcessingKind();
			if (contextInfo == null && processingKind == ProcessingKind.JSP_RENDERING) {
				theContext = processingInfo.getJSPName();
			}

			PerformanceMonitor.getInstance().stopMeasurement(startTime, processingKind, theContext, commandInfo, "");
		}
	}

	/**
	 * Does the actual work of this {@link HttpServlet}. This method is called
	 * from within {@link #service(HttpServletRequest, HttpServletResponse)} to
	 * ensure that all subclasses do the same security mechanism.
	 * 
	 * @see #service(HttpServletRequest, HttpServletResponse)
	 */
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// respond to request
		super.service(request, response);
	}

    /** 
     * In case the service method took longer than desired log a warning.
     * 
     * When debug is enabled it will always log a the timing message.
     * @param start time in milliseconds when {@link #service(HttpServletRequest, HttpServletResponse)} started.
     */
	protected void logTiming(HttpServletRequest aRequest, long start) {
        long   maxTime             = maxServiceTime;
        Number maxServiceReqNumber = (Number) aRequest.getAttribute(MAX_SERVICE_ATTR);
        if (maxServiceReqNumber != null) {
            maxTime = maxServiceReqNumber.longValue();
        }
		long elapsed = System.currentTimeMillis() - start;
		DebugHelper.logTiming(aRequest, "Processing request", elapsed, maxTime, TopLogicServlet.class);
    }

    /**
	 * Forwards the request to the configured logout page.
	 * 
	 * @exception ServletException
	 *            If an error in {@link HttpServlet} occurs.
	 * @exception IOException
	 *            If I/O operation fails.
	 * 
	 * @see com.top_logic.base.accesscontrol.ApplicationPages.Config#getLogoutPage()
	 */
	protected void redirectToLogout(HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException {
		redirectTo(ApplicationPages.getInstance().getLogoutPage(), req, res);
    }    

	/**
	 * Redirects the client to the given page.
	 *
	 * @param aRequest
	 *        The send request.
	 * @param aResponse
	 *        The send response.
	 * @param aPage
	 *        The page to be forwarded to.
	 * @exception IOException
	 *            If I/O operation fails.
	 * @exception ServletException
	 *            If an error in servlet occures.
	 */
	protected void redirectTo(String aPage, HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws IOException, ServletException {
		URLPathBuilder url = URLPathBuilder.newEmptyBuilder();
		url.appendRaw(aRequest.getContextPath());
		url.appendRaw(aPage);
		LoginPageServlet.appendCustomParameters(url, aRequest);

		aResponse.sendRedirect(url.getURL());
    }

	/**
     * Forward the request to the given page.
     *
     * @param        aRequest            The send request.
     * @param        aResponse           The send response.
     * @param        aPage               The page to be forwarded to.
     * @exception    IOException         If I/O operation fails.
     * @exception    ServletException    If an error in servlet occures.
     */
	protected void forwardPage(String aPage, HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws IOException, ServletException {
		getServletContext().getRequestDispatcher(aPage).forward(aRequest, aResponse);
    }

    /** Set the Cache policy at teh Response.
     * 
     * Some Subclasses override this to set theire own cache Policy
     */
    protected void setCachePolicy(HttpServletResponse aResponse)
    {
        CachePolicy.getInstance().setCachePolicy(aResponse);
    }

    /**
     * Set TLContext information in the current thread that can
     * be used in the services, i.e. the current User etc.
     *
     * @param     aRequest     The send request.
     * @param     aResponse    The send response.
     * @return    true, if user is logged in. 
     *
     * @throws    ServletException    If the request could not be handled.
     * @throws    IOException         If an input or output error is detected. 
     */
	protected TLSessionContext getSession(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws IOException, ServletException {
        // requires valid user
        boolean debug = Logger.isDebugEnabled (this);
        
        // Check session is session is valid, that means :
        // != null AND known to session service AND valid and a User is bound to it
        HttpSession theSession = SessionService.getInstance ().getSession (aRequest);

        if (theSession == null) {
            if (debug)
                Logger.debug ("Trying to set Contexts, but session was not valid", TopLogicServlet.class);
			return null;
        }

        if (debug) {
            Logger.debug ("Try to set TLContext...", TopLogicServlet.class);
        }
		TLSessionContext theContext = SessionService.getInstance().getSession(theSession);

        if (debug) {
            Logger.debug ("End of setContext() reached!", TopLogicServlet.class);
        }

		return theContext;
    }

	/**
	 * The current {@link HttpSession#getId()}, or {@link #NO_SESSION}, if no session
	 * exists.
	 * 
	 * <p>
	 * Note: For logging purposes only.
	 * </p>
	 * 
	 * @param req
	 *        The current {@link HttpServletRequest}.
	 * @return An identifier for the current session.
	 */
	protected static String getSessionId(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session == null) {
			return NO_SESSION;
		} else {
			return session.getId();
		}
	}
}
