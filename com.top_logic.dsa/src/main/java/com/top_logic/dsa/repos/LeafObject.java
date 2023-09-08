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

	public void copy(LeafObject aDest) throws IOException;

	public void copy(InputStream aStream) throws IOException;

	public boolean delete();

	public long lastModified();

	public long length();

	public String getName();

	public String getAbsolutePath();

	public OutputStream getOutputStream() throws FileNotFoundException, RepositoryException;

	public InputStream getInputStream() throws FileNotFoundException;

	public Writer getWriter() throws IOException;
}

