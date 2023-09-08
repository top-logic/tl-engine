/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.storage.azure.blob;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;

import com.top_logic.basic.StringServices;
import com.top_logic.dsa.repos.BaseEntryInfo;
import com.top_logic.dsa.repos.RepositoryException;

/**
 * Information for a cloud based entry.
 * 
 * @author Michael Gänsler
 */
public class CloudBlobEntryInfo extends BaseEntryInfo {

	/**
	 * Creates a {@link CloudBlobEntryInfo}.
	 */
	public CloudBlobEntryInfo(CloudContainerObject relPath, CloudContainerObject absPath) throws RepositoryException {
		super(relPath, absPath);
	}

	/** 
	 * Creates a {@link CloudBlobEntryInfo}.
	 */
	public CloudBlobEntryInfo(CloudContainerObject relPath, CloudContainerObject absPath, String aFile) throws RepositoryException {
		super(relPath, absPath, aFile);
	}

	@Override
	public void writeInfo() throws RepositoryException {
		CloudBlob theBlob = ((CloudLeafObject) verFile).getBlob();

	    try {
			byte[] theData = this.getInfoString();

			theBlob.upload(new ByteArrayInputStream(theData), theData.length);
	    }
		catch (IOException | StorageException iox) {
	        throw new RepositoryException(iox);
	    } 
	}

	@Override
	public String getPath() {
		return StringServices.replace(super.getPath(), "%20", " ");
	}

	@Override
	protected String normalizePath(String aPath) {
		return (aPath != null) ? StringServices.replace(super.normalizePath(aPath), "%20", " ") : null;
	}

	@Override
	protected String createSystemPath(String aPath, String aName) {
		return aPath + '/' + aName;
	}

	@Override
	protected long getModifiedFromFile() {
		return (super.getModifiedFromFile() / 1000l) * 1000l;
	}
}
