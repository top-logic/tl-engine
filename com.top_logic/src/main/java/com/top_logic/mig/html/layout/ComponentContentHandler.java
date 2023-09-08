/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.AbstractScopedContentHandler;
import com.top_logic.layout.CompositeContentHandler;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ProcessingInfo;
import com.top_logic.layout.ProcessingKind;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.URLPathParser;
import com.top_logic.layout.admin.component.PerformanceMonitor;
import com.top_logic.layout.basic.RenderErrorUtil;
import com.top_logic.layout.servlet.CacheControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.I18NConstants;

/**
 * {@link ContentHandler} for a {@link LayoutComponent business component} that
 * is displayed as separate document.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComponentContentHandler extends AbstractScopedContentHandler {

	/**
	 * the represented businessComponent
	 * 
	 * @see #getComponent()
	 */
	protected final LayoutComponent businessComponent;

	/**
	 * @see #getPathName()
	 */
	private final String id;

	/**
	 * Creates a {@link ComponentContentHandler}.
	 * 
	 * @param urlContext
	 *        The parent URL under which the given component is displayed.
	 * @param businessComponent
	 *        The component that is rendered.
	 */
	public ComponentContentHandler(CompositeContentHandler urlContext, LayoutComponent businessComponent) {
		super(urlContext);
		
		this.businessComponent = businessComponent;
		
		// Note: Components resolved by references may contain invalid characters in their name.
		this.id = businessComponent.getName().qualifiedName()
			.replace(URLPathParser.PATH_SEPARATOR_CHAR, '_')
			.replace(ComponentName.SCOPE_SEPARATOR, '_'); // # is the reference to an id in page
	}


	/**
	 * Returns the represented business component
	 */
	public LayoutComponent getComponent() {
		return businessComponent;
	}

	@Override
	protected String getPathName() {
		return id;
	}
	
	/**
	 * Writes content for the represented business component, by dispatching to
	 * {@link MainLayout#renderPage(ServletContext, HttpServletRequest, HttpServletResponse, TagWriter, LayoutComponent)}
	 * 
	 * @see ContentHandler#handleContent(DisplayContext, String, URLParser)
	 * @see MainLayout#renderPage(ServletContext, HttpServletRequest,
	 *      HttpServletResponse, TagWriter, LayoutComponent)
	 */
	@Override
	protected void internalHandleContent(DisplayContext context, URLParser url) throws IOException {
		final HttpServletRequest request = context.asRequest();
		final HttpServletResponse response = context.asResponse();
		final ServletContext servletContext = context.asServletContext();

		LayoutComponent oldLayout = setup(context, request, response);
		TagWriter out = null;
		int currentDepth = 0;
		try {

			/*
			 * Must get the TagWriter after setting setting content type, as
			 * getting writer gets the writer from the response, so changing
			 * content type of the response should not longer be done. (see
			 * Ticket #2703)
			 */
			out = MainLayout.getTagWriter(request, response);
			currentDepth = out.getDepth();
			
			MainLayout.renderPage(servletContext, request, response, out, getComponent());

		} catch (ServletException ex) {
			RenderErrorUtil.reportComponentRenderingError(context, out, businessComponent, currentDepth, ex);
		} finally {
			tearDown(context, out, oldLayout);
		}
	}

	private LayoutComponent setup(DisplayContext context, final HttpServletRequest request,
			final HttpServletResponse response) {
		final LayoutComponent oldLayout = MainLayout.getComponent(context);
		LayoutUtils.setContextComponent(context, getComponent());
		response.setContentType(HTMLConstants.CONTENT_TYPE_TEXT_HTML_UTF_8);
		CacheControl.setNoCache(response);
		return oldLayout;
	}

	private void tearDown(DisplayContext context, TagWriter out, final LayoutComponent oldLayout) throws IOException {
		if (out != null) {
			out.flushBuffer();
		}

		LayoutUtils.setContextComponent(context, oldLayout);

		if (PerformanceMonitor.isEnabled()) {
			ProcessingInfo processingInfo = context.getProcessingInfo();
			processingInfo.setCommandName(I18NConstants.RENDERING_COMMAND);
			processingInfo.setCommandID(null);
			processingInfo.setJSPName(null);
			processingInfo.setComponentName(getComponent().getName());
			processingInfo.setProcessingKind(ProcessingKind.COMPONENT_RENDERING);
		}
	}


	@Override
	protected void appendToString(StringBuilder stringRepresentation) {
		super.appendToString(stringRepresentation);
		stringRepresentation.append(", component:'").append(businessComponent).append('\'');
		stringRepresentation.append(", pathName:'").append(id).append('\'');
	}


}
