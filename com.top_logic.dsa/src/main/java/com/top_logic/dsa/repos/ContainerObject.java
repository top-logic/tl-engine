/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos;

import java.io.IOException;
import java.util.List;

/**
 * Interface for a container representation in the repository implementation.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface ContainerObject {

	public String getName();

	public String getPath();

	public ContainerObject getParentContainer() throws RepositoryException;

	public String getParent();

	public String getAbsolutePath();

	public List<String> list() throws RepositoryException;

	public List<LeafObject> listChildren() throws RepositoryException;

	public long lastModified() throws RepositoryException;

	public ContainerObject createTempFile(String aPrefix, String aSuffix) throws IOException;

	public boolean delete() throws RepositoryException;

	public boolean mkdir() throws RepositoryException;

	public boolean mkdirs() throws RepositoryException;

	public boolean exists() throws RepositoryException;

	public boolean isDirectory();

	public boolean renameTo(ContainerObject aNew);

	public ContainerObject createContainer(String aName) throws RepositoryException;

	public LeafObject createLeafObject(String aName) throws RepositoryException;
}

