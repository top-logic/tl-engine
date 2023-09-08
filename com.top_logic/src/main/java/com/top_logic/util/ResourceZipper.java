/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * Creates a zip archive for a collection of resource paths.
 * 
 * <p>
 * After the data has been added, the zipper should be closed.
 * </p>
 * 
 * @see FileManager#getResourcePaths(String)
 * @see FileManager#getData(String)
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ResourceZipper implements Closeable {

	/**
	 * The stream to write the zipped data to.
	 */
	private ZipOutputStream _stream;

	/**
	 * Creates a {@link ResourceZipper}.
	 * 
	 * @param destination
	 *        Zip File.
	 */
	public ResourceZipper(File destination) throws IOException {
		_stream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destination)));
	}

	/**
	 * Adds the resources given by this collection of paths to the zip.
	 * 
	 * @throws IOException
	 *         If the data could not be written to the zip.
	 */
	public void addAll(Collection<String> resourcePaths) throws IOException {
		for (String path : resourcePaths) {
			add(path);
		}
	}

	/**
	 * Adds the resource given by this path to the zip.
	 * 
	 * @throws IOException
	 *         If the data could not be written to the zip.
	 */
	public void add(String resourcePath) throws IOException {
		if (FileManager.getInstance().isDirectory(resourcePath)) {
			_stream.putNextEntry(new ZipEntry(getNormalizedEntryName(resourcePath)));
			_stream.closeEntry();
		} else {
			BinaryData data = FileManager.getInstance().getDataOrNull(resourcePath);

			if (data != null) {
				add(data.getStream(), getNormalizedEntryName(resourcePath));
			}
		}
	}

	/**
	 * Adds the underlying data of the given input stream to the zip.
	 * 
	 * @throws IOException
	 *         If the data could not be written to the zip.
	 */
	public void add(InputStream data, String entryName) throws IOException {
		_stream.putNextEntry(new ZipEntry(getNormalizedEntryName(entryName)));

		try (InputStream in = data) {
			StreamUtilities.copyStreamContents(in, _stream);
		}

		_stream.closeEntry();
	}

	private String getNormalizedEntryName(String entryName) {
		if (entryName.startsWith(FileUtilities.PATH_SEPARATOR)) {
			return entryName.substring(1);
		}

		return entryName;
	}

	@Override
	public void close() throws IOException {
		_stream.close();
	}

	/**
	 * Adds the underlying data of the resources given by this collection of paths to the
	 * destination zip file.
	 */
	public static void zip(File destination, Collection<String> resourcePaths) throws IOException {
		try (ResourceZipper zipper = new ResourceZipper(destination)) {
			for (String resourcePath : resourcePaths) {
				zipper.add(resourcePath);
			}
		}
	}

}
