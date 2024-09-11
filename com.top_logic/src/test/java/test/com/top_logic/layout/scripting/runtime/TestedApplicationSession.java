/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.runtime;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.AssertionFailedError;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import test.com.top_logic.layout.scripting.ScriptedTest;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.parsing.HTMLParserListener;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletUnitClient;
import com.meterware.servletunit.ServletUnitHttpSession;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.services.simpleajax.AJAXConstants;
import com.top_logic.base.services.simpleajax.AJAXServlet;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.col.iterator.AppendIterator;
import com.top_logic.basic.col.iterator.IteratorUtil;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.DefaultNamespaceContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateWriter;
import com.top_logic.layout.basic.AbstractDisplayContext;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.internal.WindowHandler;
import com.top_logic.layout.scripting.action.ActionChain;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.ActionUtil;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.DynamicAction;
import com.top_logic.layout.scripting.runtime.AbstractActionContext;
import com.top_logic.layout.scripting.runtime.Application;
import com.top_logic.layout.scripting.runtime.ApplicationSession;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;
import com.top_logic.layout.scripting.runtime.ScriptingRuntimeUtil;
import com.top_logic.layout.scripting.runtime.action.ApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.DynamicActionOp;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponentScope;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.util.TLContextManager;

/**
 * A session in an {@link Application} under test.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestedApplicationSession implements ApplicationSession, HttpSessionBindingListener {

	private static final Property<TestedApplicationSession> APPLICATION_SESSION =
		TypedAnnotatable.property(TestedApplicationSession.class, "session");

	static final GlobalVariableStore VARIABLE_STORE = new GlobalVariableStore();

	private TestedApplication application;
	
	/* package protected */ ServletUnitClient client;

	/* package protected */ HttpSession session;
	/* package protected */ TLSubSessionContext subsession;
	/* package protected */ MainLayout masterFrame;

	private boolean invalid;

	private final XPathExpression _xPath = createXPathSearchFragment();

	private final class RequestProcessing implements Computation<WebResponse> {
		private final WebRequest request;

		public RequestProcessing(WebRequest request) {
			this.request = request;
		}

		@Override
		public WebResponse run() {
			try {
				WebResponse response = client.getResource(request);
				return response;
			} catch (IOException e) {
				throw (AssertionError) new AssertionError("Request processing failed.").initCause(e);
			}
		}
	}

	/**
	 * The context for processing an {@link ApplicationActionOp}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public class ActionContextImpl extends AbstractActionContext {

		/* package protected */ActionContextImpl(DisplayContext displayContext) {
			super(displayContext);
		}

		@Override
		public HttpSession getSession() {
			return session;
		}
		
		/*package protected*/ ServletUnitClient getClient() {
			return client;
		}
		
		@Override
		public MainLayout getMainLayout() {
			return masterFrame;
		}
		
		@Override
		public Application getApplication() {
			return TestedApplicationSession.this.getApplication();
		}

		@Override
		public ApplicationSession getApplicationSession() {
			return TestedApplicationSession.this;
		}

		@Override
		public GlobalVariableStore getGlobalVariableStore() {
			return VARIABLE_STORE;
		}

	}
	
	/*package protected*/ interface ApplicationRequest {
		
		TestedApplicationSession getSessionStub();
		
		ApplicationAction getStubRequest();

		void setResult(Object result);
		
	}
	
	/*package protected*/ class ApplicationGetRequest extends GetMethodWebRequest implements ApplicationRequest {

		private final ApplicationAction action;
		private final TestedApplicationSession sessionStub;

		private Object _result;

		public ApplicationGetRequest(String urlString, TestedApplicationSession sessionStub, ApplicationAction action) {
			super(urlString);

			this.sessionStub = sessionStub;
			this.action = action;
		}
		
		@Override
		public TestedApplicationSession getSessionStub() {
			return sessionStub;
		}
		
		@Override
		public ApplicationAction getStubRequest() {
			return action;
		}

		@Override
		public void setResult(Object result) {
			_result = result;
		}

		public Object getResult() {
			return _result;
		}
	}
	
	/*package protected*/ class ApplicationPostRequest extends PostMethodWebRequest implements ApplicationRequest {
	
		private final ApplicationAction action;
		private final TestedApplicationSession sessionStub;

		private Object _result;
	
		public ApplicationPostRequest(String urlString, TestedApplicationSession sessionStub, ApplicationAction stubRequest) {
			super(urlString, /* mimeEncoded */ true);
	
			this.sessionStub = sessionStub;
			this.action = stubRequest;
		}
	        
		@Override
		public TestedApplicationSession getSessionStub() {
			return sessionStub;
		}
	        
		@Override
		public ApplicationAction getStubRequest() {
			return action;
		}

		@Override
		public void setResult(Object result) {
			_result = result;
		}

		public Object getResult() {
			return _result;
		}

	}

	/* package protected */TestedApplicationSession(TestedApplication application, ServletUnitClient client,
			HttpSession session, TLSubSessionContext subSession, MainLayout masterFrame) {
		this.application = application;
		this.client = client;
		this.session = session;
		this.subsession = subSession;
		this.masterFrame = masterFrame;

		this.session.setAttribute(TestedApplicationSession.class.getName(), this);
		this.subsession.set(APPLICATION_SESSION, this);
	}

	@Override
	public void valueBound(HttpSessionBindingEvent event) {
		// Ignore.
	}

	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		invalid = true;
	}
	
	/**
	 * The {@link Application} of the current {@link ApplicationSession}.
	 */
	public Application getApplication() {
		return application;
	}
	
	@Override
	public TLSubSessionContext getSubSession() {
		return subsession;
	}

	@Override
	public void invalidate() {
		if (!invalid) {
			process(ActionFactory.logout());
		}
		client.clearContents(); // Prevent Memory Leak in ServletUnit
		// Prevent Memory Leaks through invalid but still referenced sessions:
		application = null;
		client = null;
		masterFrame = null;
		session = null;
		subsession = null;

	}
	
	@Override
	public void render() {
		// not doing this will result in a memory leak in ServletUnit:
		this.client.clearContents();
		// Re-add session that was dropped either via clearContents(), or for some other unknown reasons.
		reAddSessionCookie();
		retrieveFullPage();
	}

	private void reAddSessionCookie() {
		client.setHeaderField("Cookie", ServletUnitHttpSession.SESSION_COOKIE_NAME + "=" + session.getId());
	}

	/**
	 * Retrieves the application website and all its subpages (IFrames).
	 * 
	 * @return The {@link Document}s of the main page and the sub pages (IFrame).
	 */
	private List<Document> retrieveFullPage() {
		try {
			// Allow rendering without checking the current window name.
			SubsessionHandler rootHandler = ((SubsessionHandler) masterFrame.getLayoutContext());
			rootHandler.provideRenderToken();

			URL mainPageUrl = getApplicationUrl();
			return retrieveRecursive(mainPageUrl);
		} catch (MalformedURLException checkedException) {
			throw new RuntimeException(checkedException);
		} catch (IOException checkedException) {
			throw new RuntimeException(checkedException);
		}
	}

	private List<Document> retrieveRecursive(URL mainPageUrl) throws IOException {
		AppendIterator<URL> pageUrls = IteratorUtil.createAppendIterator();
		pageUrls.append(mainPageUrl);

		return retrieveAllRecursive(pageUrls);
	}

	private List<Document> retrieveAllRecursive(AppendIterator<URL> pageUrls) throws IOException {
		List<Document> doms = new ArrayList<>();
		while (pageUrls.hasNext()) {
			URL pageUrl = pageUrls.next();
			WebResponse response = retrieveUrl(pageUrl);

			final String responseText = response.getText();

			String contentType = response.getContentType();
			if (!contentType.contains("html") && !contentType.contains("xml")) {
				continue;
			}

			HTMLParserListener listener = new HTMLParserListener() {
				@Override
				public void warning(URL url, String msg, int line, int column) {
					Logger.error("Warning during rendering of '" + url + "': " + msg + ", line " + line
						+ ", column " + column + ", content: " + responseText, TestedApplicationSession.class);
				}

				@Override
				public void error(URL url, String msg, int line, int column) {
					Logger.error("Error during rendering of '" + url + "': " + msg + ", line " + line
						+ ", column " + column + ", content: " + responseText, TestedApplicationSession.class);
				}
			};

			client.setHTMLParserListener(listener);
			try {
				Document pageDom = response.getDOM();
				doms.add(pageDom);
				addIFrameUrls(pageUrls, pageDom);
			} catch (SAXException ex) {
				throw new RuntimeException("Problem during rendering of '" + pageUrl + "', content: " + responseText,
					ex);
			} finally {
				client.setHTMLParserListener(null);
			}
		}
		return doms;
	}

	private URL getApplicationRootUrl() {
		try {
			return new URL(application.getRootUrl());
		} catch (MalformedURLException checkedException) {
			throw new RuntimeException(checkedException);
		}
	}

	private URL getApplicationUrl() throws MalformedURLException {
		return new URL(application.getLayoutRenderUrl(
			masterFrame.getLayoutContext().getWindowId()));
	}

	private WebResponse retrieveUrl(URL url) {
		try {
			GetMethodWebRequest request = new GetMethodWebRequest(url.toString());
			return application.runInThread(new RequestProcessing(request));
		} catch (Throwable exception) {
			String enrichedMessage =
				"Retrieving URL failed! URL: '" + url + "' Error message: " + exception.getMessage();
			throw new RuntimeException(enrichedMessage, exception);
		}
	}

	private void addIFrameUrls(AppendIterator<URL> urls, Document dom) {
		try {
			// Note: Document.getDocumentElement() does not work for DOM instances produced by
			// HttpUnit.
			Element documentElement = DOMUtil.getFirstElementChild(dom);
			addIFrameUrlsRecursivly(urls, documentElement);
		} catch (MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void addIFrameUrlsRecursivly(AppendIterator<URL> urls, Element element)
			throws MalformedURLException {
		if (element.getLocalName().equalsIgnoreCase(HTMLConstants.IFRAME)) {
			String urlString;
			if (element.hasAttribute(HTMLConstants.TL_FRAME_SOURCE_ATTR)) {
				urlString = element.getAttribute(HTMLConstants.TL_FRAME_SOURCE_ATTR);
			} else {
				urlString = element.getAttribute(HTMLConstants.SRC_ATTR);
			}
			addURL(urls, urlString);
		} else {
			for (Element child : DOMUtil.elements(element)) {
				addIFrameUrlsRecursivly(urls, child);
			}
		}
	}

	private void addURL(AppendIterator<URL> urls, String urlString) throws MalformedURLException {
		URL url = new URL(getApplicationRootUrl(), urlString);
		if (isExternalUrl(url)) {
			return;
		}
		urls.append(url);
	}

	private boolean isExternalUrl(URL url) {
		return !startsWith(url, application.getRootUrl());
	}

	private boolean startsWith(URL url, String prefix) {
		return url.toExternalForm().startsWith(prefix);
	}

	@Override
	public Object process(ApplicationAction action) {
		checkInvalid();
		if (action instanceof ActionChain) {
			throw new UnreachableAssertion("The class " + ScriptedTest.class.getSimpleName() + " has to take care of "
				+ ActionChain.class.getSimpleName() + ".");
		}

		ApplicationPostRequest request = new ApplicationPostRequest(application.getTestServletUrl(), this, action);
		processRequest(request);

		/* Update the session revision of the ApplicationAction calling thread to ensure that
		 * fetching a person for a created person is successful. */
		try {
			HistoryUtils.updateSessionRevision();
		} catch (MergeConflictException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
		return request.getResult();
	}

	@Override
	public List<ApplicationAction> resolve(DynamicAction action) {
		checkInvalid();

		ApplicationGetRequest request = new ApplicationGetRequest(application.getResolveServletUrl(), this, action);
		processRequest(request);

		@SuppressWarnings("unchecked")
		List<ApplicationAction> result = (List<ApplicationAction>) request.getResult();
		return result;
	}

	private WebResponse processRequest(WebRequest request) {
		WebResponse response = this.application.runInThread(new RequestProcessing(request));
		assert response != null;
		return response;
	}

	private void checkInvalid() {
		if (invalid) {
			throw new IllegalStateException("Session already invalidated.");
		}
	}
	
	/* package protected */List<ApplicationAction> resolve(final DynamicAction action, ServletContext servletContext,
			HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		@SuppressWarnings("unchecked")
		final List<ApplicationAction>[] result = new List[1];
		TLContextManager.inInteraction(subsession, servletContext, servletRequest, servletResponse,
			new InContext() {

				@Override
				public void inContext() {
					AbstractDisplayContext displayContext = (AbstractDisplayContext) TLContextManager.getInteraction();
					LayoutUtils.setContextComponent(displayContext, masterFrame);

					DynamicActionOp<?> op =
						(DynamicActionOp<?>) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
							.getInstance(action);

					try {
						result[0] = op.createActions(new ActionContextImpl(displayContext));
					} catch (Throwable ex) {
						throw enhanceThrowable(action, ex);
					}
				}
			});

		return result[0];
	}

	/* package protected */Object process(final ApplicationAction action, ServletContext servletContext,
			HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		
		final Object[] result = new Object[1];
		TLContextManager.inInteraction(subsession, servletContext, servletRequest, servletResponse, new InContext() {
			@Override
			public void inContext() {
				AbstractDisplayContext displayContext = (AbstractDisplayContext) TLContextManager.getInteraction();
				LayoutUtils.setContextComponent(displayContext, masterFrame);
				final SubsessionHandler rootHandler = (SubsessionHandler) masterFrame.getLayoutContext();

				boolean before = rootHandler.enableUpdate(true);
				try {
					ApplicationActionOp<?> op =
						SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(action);

					ScriptingRuntimeUtil.INSTANCE.logActionExecution(action);

					try {
						result[0] = op.process(new ActionContextImpl(displayContext), null);
					} catch (Throwable ex) {
						throw enhanceThrowable(action, ex);
					}

					if (SessionService.getInstance().validateSession(displayContext.asRequest())) {
						// After logout, no context is available and no model validation can
						// happen.
						try {
							validate(displayContext, rootHandler);
						} catch (Throwable ex) {
							throw enhanceThrowable(action, ex);
						}
					}
				} finally {
					rootHandler.enableUpdate(before);
				}
			}
		});

		return result[0];
	}

	void validate(DisplayContext displayContext, SubsessionHandler rootHandler) throws IOException {
		// There is no real sequence number to set.
		int sequence = -1;
		StringWriter buffer = new StringWriter();
		TagWriter out = new TagWriter(buffer);
		UpdateWriter updateWriter = new UpdateWriter(displayContext, out, LayoutConstants.UTF_8, sequence);
		AJAXServlet.validate(displayContext, rootHandler, masterFrame, updateWriter, true);
		updateWriter.endResponse();
		out.flush();

		AppendIterator<URL> urls = new AppendIterator<>();
		addIFrameUrls(buffer, urls);
		addWindows(displayContext, urls);
		retrieveAllRecursive(urls);
	}

	private void addWindows(DisplayContext context, AppendIterator<URL> urls) throws IOException {
		List<WindowComponent> allWindows = getMasterFrame().getWindowManager().getWindows();
		for (WindowComponent window : allWindows) {
			addWindow(context, window, urls);
		}
	}

	private void addWindow(DisplayContext context, WindowComponent window, AppendIterator<URL> urls) throws IOException {
		LayoutComponentScope enclosingFrameScope = window.getEnclosingFrameScope();
		String urlString = enclosingFrameScope.getURL(context).getURL();
		addURL(urls, urlString);

		// Allow rendering without checking the current window name.
		((WindowHandler) enclosingFrameScope.getUrlContext()).provideRenderToken();
	}

	private void addIFrameUrls(StringWriter buffer, AppendIterator<URL> urls) {
		Document response = DOMUtil.parse(buffer.toString());
		NodeList ajaxFragments = getAjaxFragments(response);
		for (int i = 0; i < ajaxFragments.getLength(); i++) {
			String fragment = ajaxFragments.item(i).getTextContent();
			Document responseHtml = DOMUtil.parse(encloseWithRootTag(fragment));
			addIFrameUrls(urls, responseHtml);
		}
	}

	private String encloseWithRootTag(String fragment) {
		return "<div>" + fragment + "</div>";
	}

	private NodeList getAjaxFragments(Document response) {
		try {
			return (NodeList) _xPath.evaluate(response, XPathConstants.NODESET);
		} catch (XPathExpressionException ex) {
			throw new RuntimeException(ex);
		}
	}

	private XPathExpression createXPathSearchFragment() {
		try {
			XPath newXPath = XPathFactory.newInstance().newXPath();
			DefaultNamespaceContext namespaceContext = new DefaultNamespaceContext();
			namespaceContext.setPrefix("ajax", AJAXConstants.AJAX_NAMESPACE);
			newXPath.setNamespaceContext(namespaceContext);
			return newXPath.compile("//ajax:fragment");
		} catch (XPathExpressionException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Creates an {@link Error} with context information from the given {@link ApplicationAction} to
	 * the given {@link Throwable}.
	 */
	public static Error enhanceThrowable(ApplicationAction action, Throwable ex) {
		String enhancedMessage = ActionUtil.enhanceMessage(ex, action);
		if (ex instanceof AssertionError) {
			return (AssertionError) new AssertionError(enhancedMessage).initCause(ex);
		} else {
			return (AssertionFailedError) new AssertionFailedError(enhancedMessage).initCause(ex);
		}
	}

	/**
     * Call this as soon as you do not need this session any more.
     * 
     * This simulates a logout / timeout of Session. After this
     * call the ApplactionSession must not be used any more. 
     */
    @Override
	public void tearDown() {
        client.clearContents();
        session.invalidate();
    }
    
    /**
     * This method returns the masterFrame.
     * 
     * @return    Returns the masterFrame.
     */
    public MainLayout getMasterFrame() {
        return this.masterFrame;
    }
    
	/**
	 * Executes the given {@link WebRequest}.
	 */
	public InvocationContext newInvocation(WebRequest request) throws IOException, MalformedURLException {
		return client.newInvocation(request);
	}

	/**
	 * Returns the {@link TestedApplicationSession} attached to the current
	 * {@link SubSessionContext}.
	 */
	public static TestedApplicationSession get() {
		return ThreadContextManager.getSubSession().get(APPLICATION_SESSION);
	}

}
