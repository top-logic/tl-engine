/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml.source;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A {@link ResourceTransaction} allows safely updating a template resource.
 * 
 * <p>
 * After {@link TemplateSource#update() creating} a {@link ResourceTransaction}, it must be {@link #open()
 * opened}. The resulting stream can be used to create the new content. If creating the content
 * succeeds, the {@link ResourceTransaction} must be {@link #commit() committed}. Finally, the Transaction
 * must be {@link #close() closed}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ResourceTransaction extends AutoCloseable {

	/**
	 * Creates an {@link OutputStream} to write the new contents to.
	 * 
	 * <p>
	 * This method must be called only exactly once per {@link ResourceTransaction}.
	 * </p>
	 */
	OutputStream open() throws IOException;

	/**
	 * If creating the new contents succeeded, committing the {@link ResourceTransaction} publishes the
	 * result.
	 */
	void commit() throws IOException;

	/**
	 * Closes this {@link ResourceTransaction}.
	 * 
	 * <p>
	 * A closed {@link ResourceTransaction} can no longer be used. If an uncommitted {@link ResourceTransaction} is
	 * closed, any content produced so far is dropped.
	 * </p>
	 * 
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	void close() throws IOException;
}
