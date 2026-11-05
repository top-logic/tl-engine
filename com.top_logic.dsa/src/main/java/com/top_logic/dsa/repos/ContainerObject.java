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

	/** The name of this container. */
	public String getName();

	/** The path of this container. */
	public String getPath();

	/** The container this container is located in. */
	public ContainerObject getParentContainer() throws RepositoryException;

	/** The path of the parent container. */
	public String getParent();

	/** The absolute path of this container. */
	public String getAbsolutePath();

	/** The names of the contents of this container. */
	public List<String> list() throws RepositoryException;

	/** The leaves contained in this container. */
	public List<LeafObject> listChildren() throws RepositoryException;

	/** The time of the last modification of this container. */
	public long lastModified() throws RepositoryException;

	/** Creates a new temporary container with a name using the given prefix and suffix. */
	public ContainerObject createTempFile(String aPrefix, String aSuffix) throws IOException;

	/**
	 * Deletes this container.
	 *
	 * @return Whether the container was deleted.
	 */
	public boolean delete() throws RepositoryException;

	/**
	 * Creates this container.
	 *
	 * @return Whether the container was created.
	 */
	public boolean mkdir() throws RepositoryException;

	/**
	 * Creates this container including all missing parent containers.
	 *
	 * @return Whether the container was created.
	 */
	public boolean mkdirs() throws RepositoryException;

	/** Whether this container exists. */
	public boolean exists() throws RepositoryException;

	/** Whether this is a directory. */
	public boolean isDirectory();

	/**
	 * Renames this container to the given one.
	 *
	 * @return Whether the renaming was successful.
	 */
	public boolean renameTo(ContainerObject aNew);

	/** Creates a reference to the container with the given name within this container. */
	public ContainerObject createContainer(String aName) throws RepositoryException;

	/** Creates a reference to the leaf with the given name within this container. */
	public LeafObject createLeafObject(String aName) throws RepositoryException;
}

