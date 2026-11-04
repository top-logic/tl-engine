/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Interface for a leaf representation in the repository implementation.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface LeafObject {

	/** Copies the contents of this leaf to the given leaf. */
	public void copy(LeafObject aDest) throws IOException;

	/** Replaces the contents of this leaf with the data of the given stream. */
	public void copy(InputStream aStream) throws IOException;

	/**
	 * Deletes this leaf.
	 *
	 * @return Whether the leaf was deleted.
	 */
	public boolean delete();

	/** The time of the last modification of this leaf. */
	public long lastModified();

	/** The size of the contents of this leaf in bytes. */
	public long length();

	/** The name of this leaf. */
	public String getName();

	/** The absolute path of this leaf. */
	public String getAbsolutePath();

	/** Opens a stream for writing the contents of this leaf. */
	public OutputStream getOutputStream() throws FileNotFoundException, RepositoryException;

	/** Opens a stream for reading the contents of this leaf. */
	public InputStream getInputStream() throws FileNotFoundException;

	/** Opens a writer for writing the contents of this leaf. */
	public Writer getWriter() throws IOException;
}

