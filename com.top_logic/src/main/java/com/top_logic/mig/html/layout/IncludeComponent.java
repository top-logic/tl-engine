/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.xml.TagWriter;

/**
 * Component displaying the content of one or more (HTML) files in its body.
 * 
 * <p>
 * Use this component when the framework should render the scripts and styles of the page and only
 * some static HTML is to be shown. A page component using static files is usually the better choice
 * regarding performance and memory consumption.
 * </p>
 * 
 * @implNote TODO SKR: Specify what happens when a file changes. Cache the file instead of the file
 *           name. Use {@link com.top_logic.basic.io.FileUtilities#copyFile(java.io.File,
 *           java.io.File)} when files are too large. Check the character encoding in evil cases
 *           (SUN-OS, AS4000, Linux ...).
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class IncludeComponent extends SimpleComponent {

	/**
	 * Configuration of an {@link IncludeComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
    public interface Config extends SimpleComponent.Config {

		/** Name of property {@link #getFiles()}. */
		String FILES_ATTRIBUTE = "files";

		/** Name of property {@link #getFiles()}. */
		String ENCODINGS_ATTRIBUTE = "encodings";

		/**
		 * The files to include.
		 */
		@Name(FILES_ATTRIBUTE)
		@Mandatory
		@Format(CommaSeparatedStrings.class)
		List<String> getFiles();

		/**
		 * the encodings of the included files.
		 */
		@Name(ENCODINGS_ATTRIBUTE)
		@Mandatory
		@Format(CommaSeparatedStrings.class)
		List<String> getEncodings();
	}

	/**
	 * The filenames of files which has to be included.
	 */
	private List<String> _fileNames;

	private List<String> _encodings;
    
	/**
	 * Creates a new {@link IncludeComponent}.
	 */
	public IncludeComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_fileNames = config.getFiles();
		_encodings = config.getEncodings();
		checkSameSize();
	}

	private void checkSameSize() throws ConfigurationException {
		if (_encodings.size() == _fileNames.size()) {
			return;
		}
		StringBuilder differentSize = new StringBuilder();
		differentSize.append("Encodings and files in '" + this + "' must have same size: files '");
		differentSize.append(_fileNames);
		differentSize.append("', encodings: '");
		differentSize.append(_encodings);
		differentSize.append("'");
		throw new ConfigurationException(differentSize.toString());
	}
    
	@Override
	public void writeBody(ServletContext context, HttpServletRequest req,
			HttpServletResponse resp, TagWriter out) throws IOException, ServletException {
    
        initContent();
		super.writeBody(context, req, resp, out);
	}
    
    /**
	 * fetch the content put of file.
	 */
	private void initContent() {

		StringWriter buf = new StringWriter(1024 * this._fileNames.size());

		for (int i = 0; i < _fileNames.size(); i++) {
			String theFileName = _fileNames.get(i);
			String fileEncoding = _encodings.get(i);
			try {
				FileManager theManager = FileManager.getInstance();
				File theFile = theManager.getIDEFileOrNull(theFileName);
				if (theFile != null) {
					FileInputStream stream = new FileInputStream(theFile);
					try {
						Reader input = new InputStreamReader(stream, fileEncoding);
						try {
							StreamUtilities.copyReaderWriterContents(input, buf);
						} finally {
							input.close();
						}
					} finally {
						stream.close();
					}
				} else {
					Logger.info("unable to find file for : " + theFileName, this);
				}
			} catch (Exception e) {
				Logger.error("unable to read file: " + theFileName, e, this);
				this.content = e.getMessage();
			}
		}
		this.content = buf.toString();
	}
}