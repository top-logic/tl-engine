/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.storage.azure.blob;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.Date;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.core.PathUtility;

import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.dsa.repos.LeafObject;
import com.top_logic.dsa.repos.RepositoryException;

/**
 * Cloud based leaf. 
 * 
 * @author    <a href="mailto:Michael Gänsler@top-logic.com">Michael Gänsler</a>
 */
public class CloudLeafObject implements LeafObject {

	private final String _name;

	private final CloudBlob _blob;

	/**
	 * Creates a {@link LeafObject}.
	 * 
	 * @param aContainer
	 *        Container we live in.
	 * @param aName
	 *        Name of this leaf object.
	 * @throws StorageException
	 *         If a storage service error occurred.
	 * @throws URISyntaxException
	 *         If the resource URI is invalid.
	 */
	public CloudLeafObject(CloudBlobDirectory aContainer, String aName) throws URISyntaxException, StorageException {
		_name = aName;
		_blob = aContainer.getBlockBlobReference(aName);
	}

	/**
	 * Creates a {@link LeafObject}.
	 * 
	 * @param aBlob
	 *        Blob represented by this leaf object.
	 */
	public CloudLeafObject(CloudBlob aBlob) {
		_name = PathUtility.getFileNameFromURI(aBlob.getUri(), false);
		_blob = aBlob;
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("name", _name)
			.add("uri", _blob.getUri().toString())
			.build();
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public void copy(LeafObject aDest) throws IOException {
		try {
			((CloudLeafObject) aDest).getBlob().upload(this.getInputStream(), this.length());
		} 
		catch (StorageException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void copy(InputStream aStream) throws IOException {
		try {
			this.getBlob().upload(aStream, -1);
		} 
		catch (StorageException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public boolean delete() {
		try {
			this.getBlob().delete();

			return true;
		} 
		catch (StorageException ex) {
			return false;
		}
	}

	@Override
	public long lastModified() {
		Date theDate = this.getBlob().getProperties().getLastModified();

		if (theDate == null) {
			theDate = new Date();
		}

		return theDate.getTime();
	}

	@Override
	public long length() {
		CloudBlob theBlob = this.getBlob();

		if (theBlob != null) {
			return theBlob.getProperties().getLength();
		}
		else {
			return 0l;
		}
	}

	@Override
	public String getAbsolutePath() {
		return this.getBlob().getUri().toString();
	}

	@Override
	public InputStream getInputStream() throws FileNotFoundException {
		try {
			return this.getBlob().openInputStream();
		} 
		catch (StorageException ex) {
			throw new FileNotFoundException(ex.toString());
		}
	}

	@Override
	public OutputStream getOutputStream() throws FileNotFoundException, RepositoryException {
		CloudBlob theBlob = this.getBlob();

		if (theBlob instanceof CloudBlockBlob) {
			try {
				return ((CloudBlockBlob) theBlob).openOutputStream();
			} 
			catch (StorageException ex) {
				throw new RepositoryException(ex);
			}
		}
		else { 
			throw new UnsupportedOperationException("getOutputStream() not supported for " + ((theBlob != null) ? theBlob.getClass().toString() : "null"));
		}
	}

	@Override
	public Writer getWriter() throws IOException {
		throw new UnsupportedOperationException("getWriter() not supported");
	}

	/**
	 * The represented blob.
	 */
	protected CloudBlob getBlob() {
		return _blob;
	}
}

