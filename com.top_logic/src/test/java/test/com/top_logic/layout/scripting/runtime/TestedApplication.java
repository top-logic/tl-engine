/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.runtime;

import static java.nio.file.Files.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import junit.framework.AssertionFailedError;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.xml.sax.SAXException;

import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.jsp.CompileJSP;
import test.com.top_logic.layout.scripting.runtime.TestedApplicationSession.ApplicationRequest;

import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletContextFactory;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.LoginHook;
import com.top_logic.base.accesscontrol.LoginPageServlet;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.services.simpleajax.AJAXServlet;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.XMLProperties.XMLPropertiesConfig;
import com.top_logic.basic.col.iterator.IteratorUtil;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.tooling.WebXmlBuilder;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.ContentHandlersRegistry;
import com.top_logic.layout.internal.WindowId;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.DynamicAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.Application;
import com.top_logic.layout.scripting.runtime.ApplicationSession;
import com.top_logic.layout.scripting.runtime.action.DynamicActionOp;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.util.Resources;
import com.top_logic.util.TopLogicServlet;

/**
 * An application under test.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestedApplication implements Application {
	
	/**
	 * Configuration for the {@link TestedApplication}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends ConfigurationItem {

		/**
		 * Additional servlets to register at resulting {@link ServletRunner}.
		 */
		@MapBinding(key = "resource-name", attribute = "servlet-class")
		Map<String, Class<HttpServlet>> getAdditionalServlets();

		/**
		 * Context path of the application.
		 * 
		 * <p>
		 * The context path must start but not end with "/", e.g. <code>/part1/part2</code>.
		 * </p>
		 */
		@RegexpConstraint("(?:/[\\w%-\\.]+)*")
		String getContextPath();

	}

	static final Pattern SUBSESSION_PATTERN = Pattern.compile("\\bnewSubsessionId = '([^']+)';");

	/**
	 * location which is used if the displayed layout is not taken from the file system.
	 */
	static final String SYNTHETIC_LOCATION = "_syntheticLoc_";

	private static final String AJAX_SERVLET_PATH = "/servlet/AJAXServlet";
	private static final String LAYOUT_SERVLET_PATH = "/servlet/LayoutServlet";
	private static Field requestField;

	static {
		try {
			requestField = Class.forName("com.meterware.servletunit.ServletUnitHttpRequest").getDeclaredField("_request");
			requestField.setAccessible(true);
		} catch (SecurityException e) {
			throw new UnreachableAssertion(e);
		} catch (NoSuchFieldException e) {
			throw new UnreachableAssertion(e);
		} catch (IllegalArgumentException e) {
			throw new UnreachableAssertion(e);
		} catch (ClassNotFoundException e) {
			throw new UnreachableAssertion(e);
		}
	}

	static ApplicationRequest getWebRequest(HttpServletRequest servletRequest) {
		try {
			return (ApplicationRequest) requestField.get(servletRequest);
		} catch (SecurityException e) {
			throw new UnreachableAssertion(e);
		} catch (IllegalArgumentException e) {
			throw new UnreachableAssertion(e);
		} catch (IllegalAccessException e) {
			throw new UnreachableAssertion(e);
		}
	}

	/**
	 * {@link Computation} that creates a new {@link ApplicationSession}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private final class LoginAction implements Computation<ApplicationSession> {
		
		final Person user;

		final Locale locale;

		private final String layoutName;

		final TimeZone _timeZone;

		/**
		 * Creates a {@link LoginAction}.
		 * 
		 * @param user
		 *        The {@link Person} to log in.
		 * @param timeZone
		 *        The {@link TimeZone} to use for the session.
		 * @param locale
		 *        The {@link Locale} to use for the session.
		 * @param layoutName
		 *        The name/location of the layout to load.
		 */
		public LoginAction(Person user, TimeZone timeZone, Locale locale, String layoutName) {
			this.user = user;
			_timeZone = timeZone;
			this.locale = locale;
			this.layoutName = layoutName;
		}

		@Override
		public ApplicationSession run() {
			try {
				ServletUnitClient client = getServletRunner().newClient();
				
				String initUrl = getLayoutInitUrl(layoutName, locale, _timeZone);
				final InvocationContext invocation = client.newInvocation(initUrl);
				
				final HttpServletRequest request = invocation.getRequest();
				final HttpServletResponse response = invocation.getResponse();
				
				ThreadContextManager.inSystemInteraction(TestedApplication.class, new InContext() {
					@Override
					public void inContext() {
						try {
							Login.getInstance().loginFromExternalAuth(request, response, user);
						} catch (Exception ex) {
							throw new Login.LoginFailedException("Unable to login " + user.getName(), ex);
						}

						LoginHook loginHook = LoginPageServlet.getConfiguredLoginHook();
						if (loginHook != null) {
							try {
								ResKey reason = loginHook.check(request, response);
								if (reason != null) {
									throw new Login.LoginFailedException(Resources.getInstance().getString(reason));
								}
							} catch (ServletException e) {
								throw (AssertionError) new AssertionError("Servlet unit request failed.").initCause(e);
							}
						}
					}
				});

				HttpSession session = request.getSession(false);

				// HttpUnit does for some reason not remember its session.
				client.setHeaderField("Cookie", /* ServletUnitHttpSession.SESSION_COOKIE_NAME */
					"JSESSION=" + session.getId());

				invocation.service();
				String windowNameInstaller = invocation.getServletResponse().getText();
				WindowId windowId = getSubsessionId(windowNameInstaller);
				String windowNameCheck = processRedirect(client, windowId).getText();
				WindowId subsessionId = getSubsessionId(windowNameCheck);
				processRedirect(client, subsessionId);

				TLSessionContext sessionContext = SessionService.getInstance().getSession(session);
				MainLayout masterFrame =
					sessionContext.getHandlersRegistry()
						.getContentHandler(subsessionId.getWindowName()).getMainLayout();
				if (masterFrame == null) {
					throw new AssertionError("No main layout in session.");
				}

				TLSubSessionContext subSessionContext = sessionContext.getSubSession(subsessionId.getWindowName());

				TestedApplicationSession result =
					new TestedApplicationSession(TestedApplication.this, client, session, subSessionContext,
						masterFrame);

				// The session must be rendered at least directly after login to give the browser
				// window control a chance to attach and produce updates for further actions.
				try {
					result.render();
				} catch (Throwable exception) {
					String enrichedMessage =
						"Rendering after login failed: " + exception.getMessage();
					throw (AssertionFailedError) new AssertionFailedError(enrichedMessage).initCause(exception);
				}

				return result;
			} catch (MalformedURLException e) {
				throw (AssertionError) new AssertionError("Invalid application URL.").initCause(e);
			} catch (IOException e) {
				throw (AssertionError) new AssertionError("Servlet unit request failed.").initCause(e);
			} catch (ServletException e) {
				throw (AssertionError) new AssertionError("Servlet unit request failed.").initCause(e);
			}
		}

		private WebResponse processRedirect(ServletUnitClient client, WindowId subsessionId) throws IOException,
				MalformedURLException, ServletException {
			String renderUrl = getLayoutInitUrl(subsessionId, layoutName, locale, _timeZone);
			final InvocationContext invocation = client.newInvocation(renderUrl);

			invocation.service();

			return invocation.getServletResponse();
		}

		private WindowId getSubsessionId(String responseText) throws AssertionError {
			Matcher matcher = SUBSESSION_PATTERN.matcher(responseText);
			if (!matcher.find()) {
				throw new AssertionError("No subsession in response: " + responseText);
			}
			WindowId subsessionId = WindowId.fromEncodedForm(matcher.group(1));
			return subsessionId;
		}
	}

	/**
	 * Test servlet that executes an {@link ApplicationAction}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class TestExecutor extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
			ApplicationRequest request = getWebRequest(servletRequest);
			
			TestedApplicationSession sessionStub = request.getSessionStub();
			ApplicationAction action = request.getStubRequest();

			Object result = sessionStub.process(action, getServletContext(), servletRequest, servletResponse);
			request.setResult(result);
		}
		
		@Override
		protected void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
			ApplicationRequest request = getWebRequest(servletRequest);
		            
			TestedApplicationSession sessionStub = request.getSessionStub();
			ApplicationAction  action = request.getStubRequest();
		
			Object result = sessionStub.process(action, getServletContext(), servletRequest, servletResponse);
			request.setResult(result);
		}
	}

	/**
	 * Test-internal servlet to process {@link DynamicActionOp#createActions(ActionContext)} in the
	 * context of the application.
	 */
	public static class ResolveExecutor extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
				throws ServletException, IOException {
			ApplicationRequest request = getWebRequest(servletRequest);

			TestedApplicationSession sessionStub = request.getSessionStub();
			DynamicAction action = (DynamicAction) request.getStubRequest();

			List<ApplicationAction> result =
				sessionStub.resolve(action, getServletContext(), servletRequest, servletResponse);

			request.setResult(result);
		}
	}

	/**
	 * Test servlet that performs resource lookup in the context of a
	 * ServletUnit simulated server.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class ResourceServlet extends HttpServlet {
		
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			String path = req.getRequestURI().substring(req.getContextPath().length());
			if (path == null) {
				throw new AssertionError("No path in resource lookup of URI '"  + req.getRequestURI() + "'.");
			}
			try (InputStream in = FileManager.getInstance().getStream(path)) {
				ServletOutputStream out = resp.getOutputStream();
				StreamUtilities.copyStreamContents(in, out);
			}
		}
		
	}
	
	/**
	 * Wrapper servlet to lazily load the JSP compiler for an external class path.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class JSPServletWrapper extends HttpServlet {

		/**
		 * Initial parameter used by
		 * org.apache.jasper.EmbeddedServletOptions.EmbeddedServletOptions(ServletConfig,
		 * ServletContext) to determine version of VM to use as target VM for the compiled JSP's.
		 */
		private static final String COMPILER_TARGET_VM = "compilerTargetVM";

		/**
		 * Initial parameter used by
		 * org.apache.jasper.EmbeddedServletOptions.EmbeddedServletOptions(ServletConfig,
		 * ServletContext) to determine version of VM to use as source VM for the JSP's to compile.
		 */
		private static final String COMPILER_SOURCE_VM = "compilerSourceVM";

		/**
		 * Option to set the temporary directory for the JSP compiler.
		 */
		private static final String COMPILER_SCRATCH_DIR = "scratchdir";

		private static final class CompilerVMChangingServletConfig extends ServletConfigProxy {

			private final ServletConfig _config;

			CompilerVMChangingServletConfig(ServletConfig config) {
				_config = config;
			}

			@Override
			protected ServletConfig impl() {
				return _config;
			}

			@Override
			public String getInitParameter(String name) {
				String param;
				if (COMPILER_SOURCE_VM.equals(name)) {
					param = CompileJSP.SOURCE_CODE_VERSION;
				} else if (COMPILER_TARGET_VM.equals(name)) {
					param = CompileJSP.TARGET_VM_VERSION;
				} else if (COMPILER_SCRATCH_DIR.equals(name)) {
					File tmpDir = new File("tmp/jsp");
					tmpDir.mkdirs();
					param = tmpDir.getAbsolutePath();
				} else {
					param = super.getInitParameter(name);
				}
				return param;
			}

			@Override
			public Enumeration<String> getInitParameterNames() {
				ArrayList<String> tmp = new ArrayList<>();
				Enumeration<String> initParameterNames = super.getInitParameterNames();
				while (initParameterNames.hasMoreElements()) {
					tmp.add(initParameterNames.nextElement());
				}
				tmp.add(COMPILER_SOURCE_VM);
				tmp.add(COMPILER_TARGET_VM);
				tmp.add(COMPILER_SCRATCH_DIR);
				return IteratorUtil.toEnumeration(tmp.iterator());
			}
		}

		abstract static class ServletConfigProxy implements ServletConfig {

			@Override
			public String getServletName() {
				return impl().getServletName();
			}

			protected abstract ServletConfig impl();

			@Override
			public ServletContext getServletContext() {
				return impl().getServletContext();
			}

			@Override
			public String getInitParameter(String name) {
				return impl().getInitParameter(name);
			}

			@Override
			public Enumeration<String> getInitParameterNames() {
				return impl().getInitParameterNames();
			}

		}

		private HttpServlet _impl;

		/**
		 * Creates a new {@link JSPServletWrapper}.
		 */
		public JSPServletWrapper() {
			super();
		}

		@Override
		public void init(ServletConfig config) throws ServletException {
			try {
				Class<?> jspServletClass = Class.forName("org.apache.jasper.servlet.JspServlet");
				_impl = (HttpServlet) jspServletClass.newInstance();
			} catch (ClassNotFoundException ex) {
				throw new ServletException(ex);
			} catch (InstantiationException ex) {
				throw new ServletException(ex);
			} catch (IllegalAccessException ex) {
				throw new ServletException(ex);
			} catch (IllegalArgumentException ex) {
				throw new ServletException(ex);
			} catch (SecurityException ex) {
				throw new ServletException(ex);
			}
			
			try {
				ServletContainerInitializer initializer =
					(ServletContainerInitializer) Class.forName("org.apache.jasper.servlet.JasperInitializer")
						.newInstance();

				// Prevent scanning JARs referenced from other JARs in their manifest.
				StandardJarScanner jarScanner = new StandardJarScanner();
				jarScanner.setScanManifest(false);
				ServletContext servletContext = config.getServletContext();
				servletContext.setAttribute("org.apache.tomcat.JarScanner", jarScanner);

				// Fake initialization, since HTTPUnit is not Servlet 3.0 aware.
				initializer.onStartup(Collections.emptySet(), servletContext);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
				throw new ServletException(ex);
			}

			_impl.init(new CompilerVMChangingServletConfig(config));
		}

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			/* The type parameters are necessary here. Without them, Eclipse reports an error. */
			TopLogicServlet.<IOException, ServletException> withSessionIdLogMark(req,
				() -> serviceWithLogMark(req, resp));
		}

		private void serviceWithLogMark(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			// Initialize implementation with the container loader installed in the
			// current thread. This is required to have the corresponding
			// classes in class path at the time compiled JSPs are loaded.
			try {
				_impl.service(req, resp);
			} catch (IOException exception) {
				String errorMessage =
					"JSP rendering failed! Reason: " + ExceptionUtil.getFullMessage(exception);
				throw (AssertionFailedError) new AssertionFailedError(errorMessage).initCause(exception);
			} catch (ServletException exception) {
				String errorMessage =
					"JSP rendering failed! Reason: " + ExceptionUtil.getFullMessage(exception);
				throw (AssertionFailedError) new AssertionFailedError(errorMessage).initCause(exception);
			}
		}

	}
	
	private static final String TEST_EXECUTOR_PATH = "/servlets/TestExecutor";

	private static final String RESOLVE_EXECUTOR_PATH = "/servlets/ResolveExecutor";

	/**
	 * Factory for creating test execution threads.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class ThreadFactory implements PoolableObjectFactory {

		/**
		 * The singleton instance of {@link ThreadFactory}.
		 */
		public static final ThreadFactory INSTANCE = new ThreadFactory();

		private ThreadFactory() {
			// Singleton constructor.
		}

		@Override
		public void activateObject(Object obj) throws Exception {
			// Ignore.
		}

		@Override
		public void destroyObject(Object obj) throws Exception {
			ComputationThread thread = (ComputationThread) obj;
			
			thread.interrupt();
		}

		@Override
		public Object makeObject() throws Exception {
			ComputationThread result = new ComputationThread();
			result.setDaemon(true);
			result.start();
			
			return result;
		}

		@Override
		public void passivateObject(Object obj) throws Exception {
			// Ignore.
		}

		@Override
		public boolean validateObject(Object obj) {
			ComputationThread thread = (ComputationThread) obj;
			
			return thread.isIdle();
		}
		
	}

	private String mainLayoutDefinitionName;

	private ServletRunner servletRunner;

	private File applicationRoot;

	private GenericObjectPool threadPool;

	private String contextPath;

	private File webXmlFile;

	String rootUrl;

	private String testServletUrl;

	private String resolveServletUrl;

	private String layoutServletUrl;

	private boolean started;

	/**
	 * Creates the application instance.
	 * 
	 * <p>
	 * Before the application can be used, {@link #startup()} must be called.
	 * </p>
	 * 
	 * @param applicationRoot
	 *        See {@link #getApplicationRoot()}.
	 * @param mainLayoutDefinition
	 *        See {@link #getMainLayoutDefinitionName()}.
	 */
	public TestedApplication(File applicationRoot, String mainLayoutDefinition) {
		assert applicationRoot.isDirectory() : "Application root is not a directory.";
		
		this.applicationRoot = applicationRoot;
		this.mainLayoutDefinitionName = mainLayoutDefinition;
		
		this.threadPool = new GenericObjectPool(ThreadFactory.INSTANCE);
		this.threadPool.setTestOnReturn(true);
	}
	
	/**
	 * Starts up the application.
	 * 
	 * <p>
	 * This method must be called, before {@link #login(Person, TimeZone, Locale)} can be
	 * called.
	 * </p>
	 */
	public void startup() throws IOException, SAXException {
		webXmlFile = createWebXml();
		BinaryContent containerWebXML = getDefaultWebXML();

		/* Fetch context path from system property to allow setting context path for tests. If
		 * nothing is set, use the root context. */
		contextPath = XMLPropertiesConfig.resolveContextFromSystemProperty(config().getContextPath());
		rootUrl = "http://localhost" + contextPath;
		testServletUrl = rootUrl + TEST_EXECUTOR_PATH;
		resolveServletUrl = rootUrl + RESOLVE_EXECUTOR_PATH;
		layoutServletUrl = rootUrl + LAYOUT_SERVLET_PATH;
		
		ServletContextFactory.setInstance(
			new MultiloaderAwareServletContextFactory(BinaryDataFactory.createBinaryData(webXmlFile), containerWebXML));

		servletRunner = new ServletRunner(webXmlFile, applicationRoot, contextPath);

		workaroundForMissingSessionBindingListeners();
		
        servletRunner.registerServlet(LAYOUT_SERVLET_PATH, TLLayoutServlet.class.getName());
		servletRunner.registerServlet(LAYOUT_SERVLET_PATH + "/*", TLLayoutServlet.class.getName());
        servletRunner.registerServlet(AJAX_SERVLET_PATH, AJAXServlet.class.getName());
        servletRunner.registerServlet(TEST_EXECUTOR_PATH, TestExecutor.class.getName());
		servletRunner.registerServlet(RESOLVE_EXECUTOR_PATH, ResolveExecutor.class.getName());
        servletRunner.registerServlet("/script/*", ResourceServlet.class.getName());
        servletRunner.registerServlet("/help/*", ResourceServlet.class.getName());
        servletRunner.registerServlet("/html/*", ResourceServlet.class.getName());
        servletRunner.registerServlet("/images/*", ResourceServlet.class.getName());
        servletRunner.registerServlet("/style/*", ResourceServlet.class.getName());
        servletRunner.registerServlet("/themes/*", ResourceServlet.class.getName());
        servletRunner.registerServlet("/xml/*", ResourceServlet.class.getName());
        
        servletRunner.registerServlet("*.jsp", JSPServletWrapper.class.getName());
        
		for (Entry<String, Class<HttpServlet>> servletConf : config().getAdditionalServlets().entrySet()) {
			servletRunner.registerServlet(servletConf.getKey(), servletConf.getValue().getName());
		}

		this.started = true;
	}

	private Config config() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	/**
	 * Returns the container's default "web.xml".
	 */
	public static BinaryData getDefaultWebXML() {
		return BinaryDataFactory.createBinaryData(TestedApplication.class, "/com/top_logic/ide/jetty/webdefault.xml");
	}

	private File createWebXml() throws IOException {
		Path tmpDir = createDirectories(Paths.get("tmp"));
		Path webXml = createTempFile(tmpDir, "web", ".xml");
		return WebXmlBuilder.createFromClassPath(applicationRoot)
			.dumpTo(webXml.toFile());
	}

	/**
	 * Add session listener to the application that forwards the logout to the listeners of the
	 * framework (workaround for HttpUnit not supporting HttpSessionBindingListener implementations
	 * as session attributes).
	 */
	private void workaroundForMissingSessionBindingListeners() {
		Object httpUnitApp = ReflectionUtils.getValue(servletRunner, "_application");
		List<HttpSessionListener> listeners = ReflectionUtils.getValue(httpUnitApp, "_sessionListeners", List.class);
        listeners.add(new HttpSessionListener() {
			@Override
			public void sessionDestroyed(HttpSessionEvent evt) {
				HttpSession session = evt.getSession();
				for (Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements();) {
					String attr = e.nextElement();

					Object value = session.getAttribute(attr);
					if (value instanceof HttpSessionBindingListener) {
						HttpSessionBindingListener listener = (HttpSessionBindingListener) value;
						HttpSessionBindingEvent bindingEvent = new HttpSessionBindingEvent(session, attr, value);
						listener.valueUnbound(bindingEvent);
					}
				}
			}
			
			@Override
			public void sessionCreated(HttpSessionEvent evt) {
				// Ignore.
			}
		});
	}
	
	/**
	 * The webapp directory of the application.
	 */
	public final File getApplicationRoot() {
		return applicationRoot;
	}
	
	/**
	 * The name of the applications main layout definition.
	 */
	public final String getMainLayoutDefinitionName() {
		return mainLayoutDefinitionName;
	}

	final ServletRunner getServletRunner() {
		return servletRunner;
	}

	/**
	 * The context path of the application.
	 */
	public final String getContextPath() {
		return this.contextPath;
	}

	/**
	 * The root URL of the application.
	 * 
	 * <p>
	 * The root URL is the absolute URL of the application ending with the
	 * {@link #getContextPath()}.
	 * </p>
	 */
	public final String getRootUrl() {
		return rootUrl;
	}

	@Override
	public final ApplicationSession login(final Person user, TimeZone timeZone, final Locale locale) throws IOException, MalformedURLException, ServletException {
		return login(user, timeZone, locale, getMainLayoutDefinitionName());
	}

	@Override
	public ApplicationSession login(Person user, TimeZone timeZone, Locale locale, String layoutName) throws IOException,
			MalformedURLException, ServletException {
		if (! this.started) {
			throw new IllegalStateException("Application not started.");
		}
		return runInThread(new LoginAction(user, timeZone, locale, layoutName));
	}

	/**
	 * Executes the given {@link Computation} within a different {@link Thread}.
	 */
	protected <T> T runInThread(final Computation<T> computation) {
		ComputationThread thread = borrowThread();
		try {
			// Processor computes given computation, but cannot be parameterized
			// with T.
			return (T) thread.compute(computation);
		} catch (InterruptedException e) {
			throw (AssertionError) new AssertionError("Computation failed.").initCause(e);
		} finally {
			releaseThread(thread);
		}
	}

	/**
	 * Releases a formerly borrowed {@link ComputationThread}.
	 * 
	 * @see #borrowThread()
	 */
	protected void releaseThread(ComputationThread thread) {
		try {
			threadPool.returnObject(thread);
		} catch (Exception e) {
			throw threadPoolError(e);
		}
	}

	/**
	 * Borrows a {@link ComputationThread} from the pool.
	 * 
	 * @see #releaseThread(ComputationThread)
	 */
	protected ComputationThread borrowThread() {
		try {
			return (ComputationThread) threadPool.borrowObject();
		} catch (Exception e) {
			throw threadPoolError(e);
		}
	}

	/**
	 * Terminates the application.
	 */
	public void terminate() {
		this.started = false;
		this.servletRunner.shutDown();
		try {
			threadPool.close();
		} catch (Exception e) {
			throw threadPoolError(e);
		}
	}

	private AssertionError threadPoolError(Exception e) {
		return (AssertionError) new AssertionError("Thread pool operation failed.").initCause(e);
	}

	/*package protected*/ String getTestServletUrl() {
		return testServletUrl;
	}

	/* package protected */String getResolveServletUrl() {
		return resolveServletUrl;
	}

	/* package protected */ String getLayoutInitUrl(String layoutId, Locale locale, TimeZone timeZone) {
		return layoutServletUrl + parameters(layoutId, locale, timeZone);
	}

	private String parameters(String layoutId, Locale locale, TimeZone timeZone) {
		try {
			StringBuilder url = new StringBuilder();
			url.append("?");
			url.append(ContentHandlersRegistry.LAYOUT_PARAMETER);
			url.append("=");
			url.append(URLEncoder.encode(layoutId, LayoutConstants.UTF_8));
			if (locale != null) {
				url.append("&");
				url.append(ContentHandlersRegistry.LANG_PARAMETER);
				url.append("=");
				url.append(locale.getLanguage());
			}
			if (timeZone != null) {
				url.append("&");
				url.append(ContentHandlersRegistry.ZONE_PARAMETER);
				url.append("=");
				url.append(timeZone.getID());
			}
			return url.toString();
		} catch (UnsupportedEncodingException ex) {
			throw new UnreachableAssertion("UTF-8 not supported.", ex);
		}
	}

	/* package protected */String getLayoutInitUrl(WindowId subsessionId, String layoutId, Locale locale,
			TimeZone timeZone) {
		return getLayoutRenderUrl(subsessionId) + parameters(layoutId, locale, timeZone);
	}

	/* package protected */String getLayoutRenderUrl(WindowId subsessionId) {
		return layoutServletUrl + "/" + subsessionId.getEncodedForm();
	}

}
