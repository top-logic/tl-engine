/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a source for a template.
 * <p>
 * This interface is an abstraction over the various possible sources of templates like the
 * filesystem, a database or a byte array. <br/>
 * Instances of this interface are created via the {@link TemplateSourceFactory}.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public interface TemplateSource extends TemplateLocator {

	/**
	 * Pseudo template source for templates calling themselves.
	 */
	public static final String SELF_INVOCATION = "self";

	/**
	 * Returns a new {@link InputStream} to the template.
	 * <p>
	 * Has to create a new {@link InputStream} on every call, so that the template can be read
	 * multiple times.
	 * </p>
	 */
	InputStream getContent() throws IOException;

	/**
	 * Creates an {@link OutputStream} for updating this {@link TemplateSource}.
	 */
	ResourceTransaction update() throws IOException;

	/**
	 * Deletes the underlying data.
	 * 
	 * @throws IOException
	 *         If deletion failed.
	 */
	void delete() throws IOException;

}
