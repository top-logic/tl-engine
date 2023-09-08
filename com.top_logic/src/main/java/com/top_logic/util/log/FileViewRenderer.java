/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.log;

import java.io.File;
import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.mig.html.layout.LayoutConstants;

/**
 * {@link Renderer} that renders a {@link File} through a link to a download.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FileViewRenderer implements Renderer<File> {

	public interface Config extends PolymorphicConfiguration<Renderer<?>> {
		@Name(XML_ATTRIBUTE_ENCODING)
		@StringDefault(LayoutConstants.UTF_8)
		String getEncoding();
	}

	/**
	 * XML property name of the encoding which must be used to read the displayed file.
	 */
	public static final String XML_ATTRIBUTE_ENCODING = "encoding";

	/**
	 * the encoding which must be used to render the file
	 */
	private final String _encoding;

	/**
	 * Creates a new {@link FileViewRenderer} from the given configuration. The value of the
	 * encoding is the value of the attribute {@link #XML_ATTRIBUTE_ENCODING}.
	 */
	public FileViewRenderer(InstantiationContext context, Config config) {
		this(config.getEncoding());
	}

	/**
	 * Creates a new {@link FileViewRenderer} with the given encoding.
	 * 
	 * @param encoding
	 *        the encoding to read the displayed file
	 */
	public FileViewRenderer(String encoding) {
		_encoding = encoding;
	}
	
	@Override
	public void write(DisplayContext context, TagWriter out, File value) throws IOException {
		new TextFileView(value, _encoding).write(context, out);
	}

}
