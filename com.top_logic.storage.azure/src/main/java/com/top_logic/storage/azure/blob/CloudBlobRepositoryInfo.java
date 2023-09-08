/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.storage.azure.blob;

import com.top_logic.dsa.repos.BaseRepositoryInfo;
import com.top_logic.dsa.repos.RepositoryException;

/**
 * Wrapper for an item (entry/container) in the cloud based repository.
 * 
 * @author Michael Gänsler
 */
public class CloudBlobRepositoryInfo extends BaseRepositoryInfo {

	/** 
	 * Creates a {@link CloudBlobRepositoryInfo}.
	 */
	public CloudBlobRepositoryInfo(CloudContainerObject relPath, CloudContainerObject absPath) throws RepositoryException {
		super(relPath, absPath);
	}

	/**
	 * Creates a {@link CloudBlobRepositoryInfo}.
	 */
	public CloudBlobRepositoryInfo(CloudContainerObject relPath, CloudContainerObject absPath, String aFile) throws RepositoryException {
		super(relPath, absPath, aFile);
	}

	@Override
	protected String createSystemPath(String aPath, String aName) {
		return aPath + '/' + aName;
	}

	@Override
	protected String normalizePath(String aPath) {
		return aPath;
	}
}

