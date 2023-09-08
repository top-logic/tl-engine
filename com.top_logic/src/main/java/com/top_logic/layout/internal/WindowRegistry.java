/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.internal;

import static com.top_logic.mig.html.HTMLConstants.*;
import static com.top_logic.mig.html.HTMLUtil.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.base.accesscontrol.LoginPageServlet;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.FaviconTag;
import com.top_logic.layout.CompositeContentHandler;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ErrorPage;
import com.top_logic.layout.URLBuilder;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.URLPathBuilder;
import com.top_logic.layout.WindowContext;
import com.top_logic.layout.servlet.CacheControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.LayoutConstants;

/**
 * {@link ContentHandler} that manages window name creations in browser windows handled by
 * {@link WindowHandler}s.
 * 
 * @param <W>
 *        The {@link WindowContext} type, this registry can handle.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WindowRegistry<W extends WindowContext & ContentHandler> implements CompositeContentHandler {

	/**
	 * Suffix of a subsession ID that marks the ID be constructed from a client-provided window
	 * name.
	 */
	private static final String CLIENT_PROVIDED_SUBSESSION_PREFIX = "-" + WindowRegistry.CLIENT_PROVIDED_RENDER_TOKEN;

	private static final String CLIENT_PROVIDED_RENDER_TOKEN = "client";

	private final Class<W> _windowType;

	private final ConcurrentHashMap<String, W> _windowHandlers = new ConcurrentHashMap<>();

	/**
	 * Creates a {@link WindowRegistry}.
	 * 
	 * @param windowType
	 *        The concrete type of the {@link WindowContext}s this registry can operate on.
	 */
	public WindowRegistry(Class<W> windowType) {
		_windowType = windowType;
	}

	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException {
		if (url.isEmpty()) {
			handleWindowNameCreation(context);
			return;
		}

		WindowId windowId = WindowId.fromEncodedForm(url.removeResource());
		if (WindowRegistry.CLIENT_PROVIDED_RENDER_TOKEN.equals(windowId.getRenderToken())) {
			windowId.updateRenderToken();

			redirectToWindowContent(context, windowId);
			return;
		}

		dispatch(context, windowId, url);
	}

	private void dispatch(DisplayContext context, WindowId windowId, URLParser url) throws IOException {
		W rootHandler = getContentHandler(windowId.getWindowName());

		dispatch(context, rootHandler, windowId, url);
	}

	/**
	 * Redirects a request for the top-level URL of a window to the rendering URL with render token.
	 * 
	 * @param context
	 *        See {@link #handleContent(DisplayContext, String, URLParser)}.
	 * @param windowId
	 *        The {@link WindowId} of the window.
	 * @throws IOException
	 *         See {@link #handleContent(DisplayContext, String, URLParser)}
	 */
	protected void redirectToWindowContent(DisplayContext context, WindowId windowId) throws IOException {
		HttpServletRequest request = context.asRequest();

		// Remove constant request token from location.
		URLPathBuilder url = URLPathBuilder.newEmptyBuilder();
		url.appendRaw(request.getContextPath());
		url.appendRaw(request.getServletPath());
		url.appendRaw("/");
		url.appendRaw(windowId.getEncodedForm());
		LoginPageServlet.appendCustomParameters(url, request);

		context.asResponse().sendRedirect(url.getURL());
		return;
	}

	/**
	 * Actually deliver the contents of the window.
	 * 
	 * @param context
	 *        See {@link #handleContent(DisplayContext, String, URLParser)}.
	 * @param rootHandler
	 *        The {@link WindowContext} that produces the content.
	 * @param windowId
	 *        The identifier for the rendered window.
	 * @param url
	 *        The URL of the rendered content.
	 * @throws IOException
	 *         See {@link #handleContent(DisplayContext, String, URLParser)}.
	 */
	protected void dispatch(DisplayContext context, W rootHandler, WindowId windowId, URLParser url) throws IOException {
		if (rootHandler == null) {
			ErrorPage.showPage(context, "srcNotFoundErrorPage");
			return;
		}

		rootHandler.handleContent(context, windowId.getEncodedForm(), url);
	}

	/**
	 * Delivers script content to install a window name in the client-side browser window.
	 * 
	 * @param context
	 *        See {@link #handleContent(DisplayContext, String, URLParser)}.
	 * @throws IOException
	 *         See {@link #handleContent(DisplayContext, String, URLParser)}.
	 */
	protected void handleWindowNameCreation(DisplayContext context) throws IOException {
		String rawWindowName;
		WindowId initialSubsessionId;
		String stableRawWindowName = getPreferredRawWindowName();

		makeIds:
		{
			if (stableRawWindowName != null) {
				String stableEncodedWindowName = encodeWindowName(stableRawWindowName);
				if (!hasWindow(stableEncodedWindowName)) {
					rawWindowName = stableRawWindowName;
					initialSubsessionId = new WindowId(stableEncodedWindowName);
					break makeIds;
				}
			}

			initialSubsessionId = new WindowId();
			rawWindowName = initialSubsessionId.getWindowName();
		}

		writeWindowNameInstaller(context, initialSubsessionId, rawWindowName);
	}

	/**
	 * Algorithm to produce a stabel window name, if required by subclasses.
	 */
	protected String getPreferredRawWindowName() {
		return null;
	}

	private void writeWindowNameInstaller(DisplayContext context, WindowId windowId, String rawWindowName)
			throws IOException {
		HttpServletResponse response = context.asResponse();
		response.setCharacterEncoding(LayoutConstants.UTF_8);
		response.setContentType(HTMLConstants.CONTENT_TYPE_TEXT_HTML);
		CacheControl.setNoCache(response);

		TagWriter out = new TagWriter(response.getWriter());
		out.writeXMLHeader(LayoutConstants.UTF_8);
		out.beginTag(HTML);
		out.beginTag(HEAD);
		HTMLUtil.writeEdgeDocumentMode(out);
		out.beginScript();
		SubsessionHandler.writeEncodeFunction(out);
		writeJavaScriptContent(out, "function doOnRender() {");
		writeJavaScriptContent(out, "   var oldWindowName = window.name;");
		writeJavaScriptContent(out, "   var newSubsessionId;");
		writeJavaScriptContent(out, "   if (oldWindowName != '') {");
		writeJavaScriptContent(out, "      newSubsessionId = encodeWindowName(oldWindowName) + " + "'"
			+ WindowRegistry.CLIENT_PROVIDED_SUBSESSION_PREFIX + "';");
		writeJavaScriptContent(out, "   } else {");

		out.writeIndent();
		out.append("      var newWindowName = ");
		out.writeJsString(rawWindowName);
		out.append(";");

		writeJavaScriptContent(out, "      window.name = newWindowName;");
		// Note: The application test framework depends on the following line to find the subsession
		// ID.
		out.writeIndent();
		out.append("      newSubsessionId = ");
		out.writeJsString(windowId.getEncodedForm());
		out.append(";");

		writeJavaScriptContent(out, "   }");

		writeJavaScriptContent(out, "   var oldLocation = '' + window.location;");
		writeJavaScriptContent(out, "   var paramStart = oldLocation.lastIndexOf('?');");
		writeJavaScriptContent(out, "   var fragmentStart = oldLocation.lastIndexOf('#');");
		writeJavaScriptContent(out, "   var suffixStart = paramStart;");
		writeJavaScriptContent(out, "   if (fragmentStart >= 0 && (suffixStart < 0 || fragmentStart < suffixStart)) {");
		writeJavaScriptContent(out, "      suffixStart = fragmentStart;");
		writeJavaScriptContent(out, "   }");
		writeJavaScriptContent(out, "   var suffix;");
		writeJavaScriptContent(out, "   if (suffixStart >= 0) {");
		writeJavaScriptContent(out, "      suffix = oldLocation.substring(suffixStart);");
		writeJavaScriptContent(out, "      oldLocation = oldLocation.substring(0, suffixStart);");
		writeJavaScriptContent(out, "   } else {");
		writeJavaScriptContent(out, "      suffix = '';");
		writeJavaScriptContent(out, "   }");
		writeJavaScriptContent(out, "   var newLocation = oldLocation + '/' + newSubsessionId + suffix;");
		writeJavaScriptContent(out, "   window.location = newLocation;");
		writeJavaScriptContent(out, "}");
		writeJavaScriptContent(out, "doOnRender();");
		out.endScript();

		FaviconTag.write(context, out);

		out.endTag(HEAD);
		out.beginBeginTag(BODY);
		out.endBeginTag();
		out.beginBeginTag(DIV);
		out.endBeginTag();
		out.endTag(DIV);
		out.endTag(BODY);
		out.endTag(HTML);
		out.flushBuffer();
	}

	/**
	 * Server-side implementation of {@link SubsessionHandler#writeEncodeFunction(TagWriter)}.
	 */
	public static String encodeWindowName(String s) {
		try {
			return URLEncoder.encode(s, LayoutConstants.UTF_8)
				.replaceAll("\\+", "%20")
				.replaceAll("\\%21", "!")
				.replaceAll("\\%27", "'")
				.replaceAll("\\%28", "(")
				.replaceAll("\\%29", ")")
				.replaceAll("\\%7E", "~")
				.replaceAll("[-%'\\.]", "_");
		} catch (UnsupportedEncodingException ex) {
			throw new UnreachableAssertion(LayoutConstants.UTF_8 + " not supported.");
		}
	}

	@Override
	public void registerContentHandler(String id, ContentHandler handler) {
		registerIfAbsent(id, cast(handler));
	}

	@Override
	public boolean deregisterContentHandler(ContentHandler handler) {
		return internalDeregisterWindow(cast(handler));
	}

	/**
	 * Whether a window with the given name is already loaded, or is being loaded.
	 */
	public boolean hasWindow(String windowName) {
		return _windowHandlers.containsKey(windowName);
	}

	/**
	 * Whether there are registerd windows.
	 */
	protected boolean hasWindows() {
		return !_windowHandlers.isEmpty();
	}

	/**
	 * The number of registered windows.
	 */
	protected int getWindowCount() {
		return _windowHandlers.size();
	}

	/**
	 * The handler for the subsession with in the window with the given name.
	 */
	public W getContentHandler(String windowName) {
		return _windowHandlers.get(windowName);
	}

	/**
	 * Registers a new window in this registry, if no handler was already registered for the given
	 * name.
	 * 
	 * @param handlerId
	 *        The identifier (window name) of the new window.
	 * @param handler
	 *        The handler of the new window.
	 * @return The handler of the already registered window, if there was already a window handler
	 *         registered for the given name.
	 */
	protected W registerIfAbsent(final String handlerId, W handler) {
		return _windowHandlers.putIfAbsent(handlerId, handler);
	}

	private boolean internalDeregisterWindow(W handler) {
		return internalDeregisterWindow(getID(handler)) != null;
	}

	/**
	 * Unregisters the window with the given name.
	 * 
	 * @param id
	 *        The window name to unregister.
	 * @return The formerly registered handler for the given name, <code>null</code> if none was
	 *         registered under the given name.
	 */
	protected W internalDeregisterWindow(String id) {
		return _windowHandlers.remove(id);
	}

	@Override
	public Collection<? extends ContentHandler> inspectSubHandlers() {
		return Collections.unmodifiableCollection(_windowHandlers.values());
	}

	@Override
	public URLBuilder getURL(DisplayContext context, ContentHandler handler) {
		URLBuilder result = getURL(context);
		result.addResource(getID(handler));
		return result;
	}

	@Override
	public URLBuilder getURL(DisplayContext context) {
		return URLPathBuilder.newLayoutServletURLBuilder(context);
	}

	private String getID(ContentHandler handler) {
		return cast(handler).getWindowId().getWindowName();
	}

	private W cast(ContentHandler handler) {
		return CollectionUtil.dynamicCast(_windowType, handler);
	}

}
