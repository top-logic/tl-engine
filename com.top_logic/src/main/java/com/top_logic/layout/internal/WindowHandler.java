/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.internal;

import static com.top_logic.mig.html.HTMLUtil.*;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.FaviconTag;
import com.top_logic.knowledge.gui.layout.LayoutConfig;
import com.top_logic.layout.CompositeContentHandler;
import com.top_logic.layout.CompositeHandlerAdapter;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.ContentHandlersRegistry;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.WindowContext;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.WithPropertiesDelegate;
import com.top_logic.layout.basic.WithPropertiesDelegateFactory;
import com.top_logic.layout.servlet.CacheControl;
import com.top_logic.layout.template.WithPropertiesBase;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.util.license.LicenseTool;

/**
 * {@link ContentHandler} that delivers script content for client-side window name checks.
 * 
 * <p>
 * A client-side window name check prevents duplicating a browser window by opening a fresh window
 * in the same session and entering an existing URL to that window.
 * </p>
 * 
 * <p>
 * {@link WindowHandler}s must be registered in a {@link WindowRegistry} for initial window name
 * installation.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WindowHandler extends CompositeHandlerAdapter implements WindowContext {

	private final WindowId _windowId;

	/**
	 * Whether this handler should {@link #deliverLocalContent(DisplayContext, String, URLParser)}.
	 * 
	 * <p>
	 * This is flag only set, if a client-side window name check was issued to ensure that the
	 * resulting request is fulfilled.
	 * </p>
	 * 
	 * <p>
	 * Note: Even directly after creation of a {@link WindowHandler}, a window name check must be
	 * issued to detect faked subsession IDs.
	 * </p>
	 */
	private boolean _renderToken = false;

	/**
	 * Creates a {@link WindowHandler}.
	 * 
	 * @param urlContext
	 *        See {@link #getUrlContext()}.
	 * @param windowId
	 *        See {@link #getWindowId()}.
	 */
	public WindowHandler(CompositeContentHandler urlContext, WindowId windowId) {
		super(urlContext);

		_windowId = windowId;
	}

	@Override
	public final WindowId getWindowId() {
		return _windowId;
	}

	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException {
		if (url.isEmpty()) {
			handleLocalContent(context, id, url);
		} else {
			handleCompositeContent(context, id, url);
		}
	}

	/**
	 * {@link #handleContent(DisplayContext, String, URLParser) Handler method} for top-level
	 * content (where there is no sub-path of the currently processed URL).
	 * 
	 * <p>
	 * Only for composite content, client side check scripts are necessary.
	 * </p>
	 * 
	 * @see #handleCompositeContent(DisplayContext, String, URLParser)
	 */
	protected void handleLocalContent(DisplayContext context, String id, URLParser url) throws IOException {
		WindowId windowId = getWindowId();
		if (!windowId.matches(id) || !popTopLevelToken()) {
			handleWindowIdMissmatch(context, windowId);
		} else {
			// Allow rendering the top-level URI.
			deliverLocalContent(context, id, url);
		}
	}

	/**
	 * {@link #handleContent(DisplayContext, String, URLParser) Handler method} invoked, if a
	 * potential reload or new window operation is detected.
	 */
	protected void handleWindowIdMissmatch(DisplayContext context, WindowId windowId) throws IOException {
		createRenderToken();
		writeWindowNameCheck(context, windowId);
	}

	/**
	 * Actually delivers the top-level content of this handler (after a successful window name
	 * check).
	 * 
	 * @see #handleContent(DisplayContext, String, URLParser)
	 */
	protected void deliverLocalContent(DisplayContext context, String id, URLParser url) throws IOException {
		super.handleContent(context, id, url);
	}

	/**
	 * {@link #handleContent(DisplayContext, String, URLParser) Handler method} for non-top-level
	 * content (where there is a sub-path of the currently processed URL).
	 * 
	 * <p>
	 * For composite content, no client side script is necessary.
	 * </p>
	 * 
	 * @see #handleLocalContent(DisplayContext, String, URLParser)
	 */
	protected void handleCompositeContent(DisplayContext context, String id, URLParser url) throws IOException {
		super.handleContent(context, id, url);
	}

	private void writeWindowNameCheck(DisplayContext context, WindowId newSubsessionId)
			throws IOException {
		HttpServletResponse response = context.asResponse();
		response.setCharacterEncoding(LayoutConstants.UTF_8);
		response.setContentType(HTMLConstants.CONTENT_TYPE_TEXT_HTML);
		CacheControl.setNoCache(response);

		TagWriter out = new TagWriter(response.getWriter());
		out.writeXMLHeader(LayoutConstants.UTF_8);
		Icons.LOADING_SCREEN.get().write(context, out, new LoadingModel(this, newSubsessionId));
		out.flushBuffer();
	}

	/**
	 * Model for the loading page.
	 * 
	 * @see Icons#LOADING_SCREEN
	 */
	public static class LoadingModel extends WithPropertiesBase {

		private static final WithPropertiesDelegate DELEGATE = WithPropertiesDelegateFactory.lookup(LoadingModel.class);

		private final WindowId _subsessionId;

		private WindowHandler _windowHandler;

		/**
		 * Creates a {@link LoadingModel}.
		 */
		public LoadingModel(WindowHandler windowHandler, WindowId subsessionId) {
			super(DELEGATE);
			_windowHandler = windowHandler;
			_subsessionId = subsessionId;
		}

		/**
		 * Value for the HTML meta tag <code>generator</code>.
		 */
		@TemplateVariable("generator")
		public String getGenerator() {
			return LicenseTool.productType();
		}

		/**
		 * Text that informs the user which application is being loaded.
		 */
		@TemplateVariable("loadingMessage")
		public void writeLoadingMessage(DisplayContext context, TagWriter out) {
			out.writeText(context.getResources().getString(
				I18NConstants.LOADING_MESSAGE__APPLICATION.fill(com.top_logic.layout.I18NConstants.APPLICATION_TITLE)));
		}

		/**
		 * A HTML meta tag pointing to the application's icon for the browser tab.
		 * 
		 * <p>
		 * Must be added to the page header.
		 * </p>
		 */
		@TemplateVariable("faviconTag")
		public void writeFaviconTag(DisplayContext context, TagWriter out) {
			FaviconTag.write(context, out);
		}

		/**
		 * Creates a <i>JavaScript</i> tag that defines the <code>onRender</code> function that must
		 * be called from the body's <code>onload</code> handler.
		 */
		@TemplateVariable("loadingScript")
		public void writeLloadingScript(DisplayContext context, TagWriter out) throws IOException {
			out.beginScript();
			writeEncodeFunction(out);
			writeJavaScriptContent(out, "function doOnRender() {");

			out.writeIndent();
			out.append("   var expectedWindowName = ");
			out.writeJsString(_subsessionId.getWindowName());
			out.append(";");
			
			writeJavaScriptContent(out, "   var encodedWindowName = encodeWindowName(window.name);");
			writeJavaScriptContent(out, "   if (encodedWindowName == expectedWindowName) {");
			writeJavaScriptContent(out, "      var oldLocation = '' + window.location;");

			out.writeIndent();
			out.append("      var newSubsessionId = ");
			out.writeJsString(_subsessionId.getEncodedForm());
			out.append(";");
			
			writeJavaScriptContent(out, "      var parameterSeparatorIndex = oldLocation.lastIndexOf('?');");
			writeJavaScriptContent(out, "      if (parameterSeparatorIndex < 0) {");
			writeJavaScriptContent(out, "         parameterSeparatorIndex = oldLocation.length");
			writeJavaScriptContent(out, "      }");
			writeJavaScriptContent(out,
				"      var subsessionSeparatorIndex = oldLocation.lastIndexOf('/', parameterSeparatorIndex);");
			writeJavaScriptContent(out,
				"      var newLocation = oldLocation.substring(0, subsessionSeparatorIndex) + '/' + newSubsessionId + oldLocation.substring(parameterSeparatorIndex);");
			writeJavaScriptContent(out, "      window.location = newLocation;");
			writeJavaScriptContent(out, "   } else {");
			out.writeIndent();
			out.append("      window.location = ");
			out.writeJsString(_windowHandler.getClonedWindowLocation(context));
			out.append(";");
			writeJavaScriptContent(out, "   }");
			writeJavaScriptContent(out, "}");
			out.endScript();
		}
	}

	/**
	 * Location of the page that is shown in a cloned window (to prevent loading the same
	 * server-side model in two different client-side views).
	 */
	protected String getClonedWindowLocation(DisplayContext context) {
		return getClonedExternalWindowLocation(context);
	}

	/**
	 * The configured location to redirect to, if a cloned application window is detected.
	 * 
	 * @see LayoutConfig#getClonedExternalWindowLocation()
	 */
	private static String getClonedExternalWindowLocation(DisplayContext context) {
		final LayoutConfig config = ApplicationConfig.getInstance().getConfig(LayoutConfig.class);
		return context.getContextPath() + "/" + config.getClonedExternalWindowLocation();
	}

	/**
	 * Client-side implementation of {@link ContentHandlersRegistry#encodeWindowName(String)}.
	 */
	static void writeEncodeFunction(TagWriter out) throws IOException {
		/* Note: The same encoding must be done in simpleajax.js (services.ajax.encodeWindowName())
		 * and encodeWindowName() below. */
		writeJavaScriptContent(out, "function encodeWindowName(s) {");
		writeJavaScriptContent(out, "	var regexp = /[-%'\\.]/g;");
		writeJavaScriptContent(out, "   return encodeURIComponent(s).replace(regexp, '_');");
		writeJavaScriptContent(out, "}");
	}

	/**
	 * Updates the {@link #getWindowId()} enabling a (single) rendering of this window.
	 * 
	 * @see #popTopLevelToken()
	 */
	protected void createRenderToken() {
		_windowId.updateRenderToken();
		provideRenderToken();
	}

	/**
	 * Enables rendering the top-level page without further window name checking.
	 */
	public void provideRenderToken() {
		_renderToken = true;
	}

	/**
	 * Whether a window with a matching {@link #getWindowId()} can be rendered.
	 * 
	 * <p>
	 * If {@link #getWindowId()} matches the transmitted one, but {@link #popTopLevelToken()}
	 * returns <code>false</code>, a check script must be passed to the client that ensures that the
	 * client-side window name matches the window name part of the subsession ID.
	 * </p>
	 */
	protected boolean popTopLevelToken() {
		boolean result = _renderToken;
		_renderToken = false;
		return result;
	}

}
