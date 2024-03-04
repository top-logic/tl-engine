/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.FaviconTag;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.util.TLMimeTypes;

/**
 * {@link View} that displays a link to a {@link File} download.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TextFileView extends ConstantControl<File> implements ContentHandler {

	/**
	 * The encoding to read the file in.
	 */
	private final String _encoding;

	/**
	 * Creates a new {@link TextFileView} which displays the given text file in the given encoding.
	 */
	protected TextFileView(File textFile, String encoding) {
		super(textFile);
		_encoding = encoding;
	}

	@Override
	protected String getTypeCssClass() {
		return "cTextFileView";
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		String name = getModel().getName();
		FrameScope frameScope = getScope().getFrameScope();
		frameScope.registerContentHandler(name, this);
		
		out.beginBeginTag(ANCHOR);
		writeControlAttributes(context, out);
		out.writeAttribute(HREF_ATTR, frameScope.getURL(context, this).getURL());
		out.writeAttribute(TARGET_ATTR, "logWindow");
		out.endBeginTag();
		{
			ThemeImage icon = ThemeImage.typeIcon(TLMimeTypes.getInstance().getMimeType(name));

			icon.writeWithCss(context, out, "tbl");
			out.writeText(NBSP);
			out.writeText(name);
		}
		out.endTag(ANCHOR);
	}

	@Override
	protected void internalDetach() {
		super.internalDetach();
		getScope().getFrameScope().deregisterContentHandler(this);
	}
	
	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException {
		final HttpServletResponse response = context.asResponse();
		response.setContentType(HTMLConstants.CONTENT_TYPE_TEXT_HTML_UTF_8);
		TagWriter out = MainLayout.getTagWriter(context.asRequest(), response);
		File file = getModel();
		String fileName = file.getName();

		MainLayout.writeHTMLStructureStart(context, out);
		{
			out.beginTag(HEAD);
			{
				out.beginTag(TITLE);
				writeTitle(out, fileName);
				out.endTag(TITLE);

				writeStyleSheets(context, out);

				FaviconTag.write(context, out);
			}
			out.endTag(HEAD);
			
			out.beginTag(BODY);
			{
				writeHeading(out, file, fileName);
				writeContent(out, file);
			}
			out.endTag(BODY);
		}
		out.endTag(HTML);

		out.flushBuffer();
	}

	protected void writeTitle(TagWriter out, String fileName) throws IOException {
		out.writeText("Log File: ");
		out.writeText(fileName);
	}

	protected void writeStyleSheets(DisplayContext context, TagWriter out) throws IOException {
		String contextPath = context.getContextPath();
		out.beginBeginTag(LINK);
		out.writeAttribute(REL_ATTR, STYLESHEET_REL_VALUE);
		out.writeAttribute(TYPE_ATTR, CSS_TYPE_VALUE);
		out.writeAttribute(HREF_ATTR, contextPath + ThemeFactory.getTheme().getStyleSheet());
		out.endEmptyTag();
	}

	protected void writeHeading(TagWriter out, File file, String fileName) throws IOException {
		out.beginBeginTag(PARAGRAPH);
		out.endBeginTag();
		{
			out.writeText(fileName);
			out.writeText(" ");
			out.writeText(HTMLFormatter.getInstance().formatDateTime(new Date(file.lastModified())));
		}
		out.endTag(PARAGRAPH);
	}

	protected void writeContent(TagWriter out, File file) throws IOException, FileNotFoundException {
		out.beginTag(PRE);
		{
			BufferedReader in =
				new BufferedReader(new InputStreamReader(new FileInputStream(file), _encoding));
			try {		  				  		
				String line;
				while (( line = in.readLine() ) != null) {
					out.writeText(line); 
					out.writeText("\r\n"); 
				}
			} finally{
				StreamUtilities.close(in);
			}
		}
		out.endTag(PRE);
	}

}