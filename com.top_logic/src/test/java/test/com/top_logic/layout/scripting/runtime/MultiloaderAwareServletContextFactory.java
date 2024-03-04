/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.runtime;

import java.io.IOException;

import javax.activation.FileTypeMap;
import jakarta.servlet.ServletContext;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.meterware.servletunit.ServletContextFactory;
import com.meterware.servletunit.WebApplication;

import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.mime.MimetypesParser;

/**
 * Creates {@link MultiloaderAwareServletContext}s. The "real" {@link ServletContext} that the
 * {@link MultiloaderAwareServletContext} needs is created by the
 * {@link ServletContextFactory#DEFAULT_INSTANCE}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class MultiloaderAwareServletContextFactory extends ServletContextFactory {

	private FileTypeMap _mimeTypes;

	/**
	 * Creates a new {@link MultiloaderAwareServletContextFactory}.
	 * 
	 * @param webXMLs
	 *        Used web.xml files. Files with higher index serves as fallback for files with lower
	 *        index.
	 */
	public MultiloaderAwareServletContextFactory(BinaryContent... webXMLs) throws IOException, SAXException {
		_mimeTypes = parseMimeTypes(webXMLs);
	}

	private FileTypeMap parseMimeTypes(BinaryContent[] webXMLs) throws IOException, SAXException {
		FileTypeMap mimeTypes;
		try {
			mimeTypes = MimetypesParser.parse(webXMLs);
		} catch (ParserConfigurationException ex) {
			throw new SAXException("Unable to create mime type map", ex);
		}
		return mimeTypes;
	}

	@Override
	public ServletContext createInternal(WebApplication application) {
		return new MultiloaderAwareServletContext(_mimeTypes, ServletContextFactory.DEFAULT_INSTANCE.createInternal(application));
	}

}
