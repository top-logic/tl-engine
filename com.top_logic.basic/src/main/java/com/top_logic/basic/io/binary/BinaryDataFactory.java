/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.http.Part;

import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.mime.MimeTypesModule;
import com.top_logic.basic.thread.StackTrace;

/**
 * Factory for {@link BinaryData} from various sources.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BinaryDataFactory {

	/** Maximum size of data allowed */
	public static final int MAX_MEMORY_SIZE = 4096;

	/**
	 * Creates a {@link BinaryData} from byte array contents with content-type
	 * {@link BinaryData#CONTENT_TYPE_OCTET_STREAM} and no name.
	 */
	public static BinaryData createBinaryData(byte[] content) {
		return createBinaryData(content, BinaryData.CONTENT_TYPE_OCTET_STREAM);
	}

	/**
	 * Creates a {@link BinaryData} from byte array contents with no name.
	 */
	public static BinaryData createBinaryData(byte[] content, String contentType) {
		return createBinaryData(content, contentType, BinaryData.NO_NAME);
	}

	/**
	 * Creates a {@link BinaryData} from byte array contents.
	 */
	public static BinaryData createBinaryData(byte[] content, String contentType, String name) {
		return new MemoryBinaryData(content, contentType, name);
	}

	/**
	 * Creates a {@link BinaryData} from byte array contents.
	 */
	public static BinaryData createBinaryData(byte[] content, int offset, int length) {
		return new MemoryBinaryData(content, offset, length, BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME);
	}

	/**
	 * Creates a {@link BinaryData} from byte array contents.
	 */
	public static BinaryData createBinaryData(byte[] content, int offset, int length, String contentType, String name) {
		return new MemoryBinaryData(content, offset, length, contentType, name);
	}

	/**
	 * Creates a {@link BinaryData} from {@link File} contents with
	 * {@link BinaryData#CONTENT_TYPE_OCTET_STREAM}.
	 * 
	 * <p>
	 * Note: To guess the content type from the file name, use
	 * <code>com.top_logic.dsa.util.DSABinaryDataFactory.createBinaryData(File)</code>.
	 * </p>
	 * 
	 * @deprecated Use {@link #createBinaryData(File)}
	 */
	@Deprecated
	public static BinaryData createBinaryDataOctetStream(File content) {
		return createBinaryDataWithContentType(content, BinaryData.CONTENT_TYPE_OCTET_STREAM);
	}

	/**
	 * Creates a {@link BinaryData} from {@link File} with a content type guessed from the file name.
	 * 
	 * <p>
	 * Note: To guess the content type from the file name, use
	 * <code>com.top_logic.dsa.util.DSABinaryDataFactory.createBinaryData(File)</code>.
	 * </p>
	 */
	public static BinaryData createBinaryData(File content) {
		return createBinaryDataWithName(content, content.getPath());
	}

	/**
	 * Creates a {@link BinaryData} from a {@link File} by guessing the content-type from the given
	 * file name extension.
	 */
	public static BinaryData createBinaryDataWithName(File content, String name) {
		// Resolve content type lazily, since this version is used from contexts, where no mime
		// types module is available and the content type is not used at all.
		return new FileBasedBinaryData(content, BinaryData.CONTENT_TYPE_OCTET_STREAM, name) {
			String _lazyContentType = null;

			@Override
			public String getContentType() {
				if (_lazyContentType == null) {
					_lazyContentType = guessContentType(getName());
				}
				return _lazyContentType;
			}

			private String guessContentType(String filename) {
				return MimeTypesModule.getInstance().getMimeType(filename);
			}
		};
	}

	/**
	 * Creates a {@link BinaryData} from a {@link Path} by guessing the content-type from the given
	 * file name extension.
	 */
	public static BinaryData createBinaryDataWithName(Path content, String name) {
		// Resolve content type lazily, since this version is used from contexts, where no mime
		// types module is available and the content type is not used at all.
		return new AbstractBinaryData() {
			String _lazyContentType = null;

			@Override
			public String getContentType() {
				if (_lazyContentType == null) {
					_lazyContentType = guessContentType(getName());
				}
				return _lazyContentType;
			}

			private String guessContentType(String filename) {
				return MimeTypesModule.getInstance().getMimeType(filename);
			}

			@Override
			public long getSize() {
				try {
					return Files.size(content);
				} catch (IOException ex) {
					return -1;
				}
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public InputStream getStream() throws IOException {
				return Files.newInputStream(content);
			}

			@Override
			public String toString() {
				return getName() + " (" + content + ")";
			}
		};
	}

	/**
	 * Creates a {@link BinaryData} from {@link File} contents.
	 */
	public static BinaryData createBinaryDataWithContentType(File content, String contentType) {
		return createBinaryData(content, contentType, content.getPath());
	}

	/**
	 * Creates a {@link BinaryData} from {@link File} contents.
	 */
	public static BinaryData createBinaryData(File content, String contentType, String name) {
		return new FileBasedBinaryData(content, contentType, name);
	}

	/**
	 * Creates {@link BinaryData} from a stream contents with content type
	 * {@link BinaryData#CONTENT_TYPE_OCTET_STREAM}.
	 */
	public static BinaryData createBinaryData(InputStream content, long size) throws IOException {
		return createBinaryData(content, size, BinaryData.CONTENT_TYPE_OCTET_STREAM);
	}

	/**
	 * Creates {@link BinaryData} from a stream contents.
	 */
	public static BinaryData createBinaryData(InputStream content, long size, String contentType) throws IOException {
		return createBinaryData(content, size, contentType, BinaryData.NO_NAME);
	}

	/**
	 * Creates {@link BinaryData} from a stream contents.
	 */
	public static BinaryData createBinaryData(InputStream content, long size, String contentType, String name)
			throws IOException {
		if (size > 0 && size <= MAX_MEMORY_SIZE) {
			return createMemoryBinaryData(content, (int) size, contentType, name);
		}
		return createFileBasedBinaryData(contentType, content, size, name);
	}

	/**
	 * Creates an in-memory {@link BinaryData} from a stream contents.
	 */
	public static BinaryData createMemoryBinaryData(InputStream content, long size) throws IOException {
		return createMemoryBinaryData(content, size, BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME);
	}

	/**
	 * Creates an in-memory {@link BinaryData} from a stream contents.
	 */
	public static BinaryData createMemoryBinaryData(InputStream content, long size, String contentType)
			throws IOException {
		return createMemoryBinaryData(content, size, contentType, BinaryData.NO_NAME);
	}

	/**
	 * Creates an in-memory {@link BinaryData} from a stream contents.
	 */
	public static BinaryData createMemoryBinaryData(InputStream content, long size, String contentType,
			String name) throws IOException {
		return createBinaryData(dumpToMem(content, size), contentType, name);
	}

	private static byte[] dumpToMem(InputStream content, long givenSize) throws IOException {
		if (givenSize < 0) {
			// Unknown size, use expanding buffer.
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			StreamUtilities.copyStreamContents(content, out);
			return out.toByteArray();
		}
		if (givenSize >= Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Size to large:" + givenSize);
		}
		int size = (int) givenSize;
		byte[] data = new byte[size];

		int contentSize = 0;
		while (contentSize < size) {
			int direct = content.read(data, contentSize, size - contentSize);
			if (direct < 0) {
				break;
			}
			if (direct > 0) {
				contentSize += direct;
			}
		}

		if (contentSize < size) {
			Logger.warn("Inconsistent blob data, expected " + size + " bytes, received " + contentSize + " bytes.",
				new StackTrace(), BinaryDataFactory.class);

			byte[] trimmed = new byte[contentSize];
			System.arraycopy(data, 0, trimmed, 0, contentSize);
			return trimmed;
		}
		else {
			int next = content.read();
			if (next >= 0) {
				Logger.warn("Inconsistent blob data, received extra data after expected " + size + " bytes.",
					new StackTrace(), BinaryDataFactory.class);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				out.write(data);
				out.write(next);
				StreamUtilities.copyStreamContents(content, out);
				return out.toByteArray();
			}
		}
		return data;
	}

	/**
	 * Creates an file-based {@link BinaryData} from a stream contents.
	 */
	public static BinaryData createFileBasedBinaryData(InputStream content) throws IOException {
		return createFileBasedBinaryData(content, BinaryData.CONTENT_TYPE_OCTET_STREAM);
	}

	/**
	 * Creates an file-based {@link BinaryData} from a stream contents.
	 */
	public static BinaryData createFileBasedBinaryData(InputStream content, String contentType)
			throws IOException {
		return createBinaryDataWithContentType(BinaryDataFactory.dumpToFile(content), contentType);
	}

	/**
	 * Creates an file-based {@link BinaryData} from a stream contents.
	 */
	public static BinaryData createFileBasedBinaryData(InputStream content, String contentType, String name)
			throws IOException {
		File tmpFile = BinaryDataFactory.dumpToFile(content);
		return createBinaryData(tmpFile, contentType, name == null ? tmpFile.getName() : name);
	}

	/**
	 * Creates an file-based {@link BinaryData} from a stream contents.
	 */
	public static BinaryData createFileBasedBinaryData(String contentType, InputStream content, long size)
			throws IOException {
		return createFileBasedBinaryData(contentType, content, size, null);
	}

	/**
	 * Creates an file-based {@link BinaryData} from a stream contents.
	 */
	public static BinaryData createFileBasedBinaryData(String contentType, InputStream content, long size, String name)
			throws IOException {
		BinaryData result = createFileBasedBinaryData(content, contentType, name);
		if (size >= 0 && result.getSize() != size) {
			Logger.warn("Inconsistent blob data, expected " + size + " bytes, received " + result.getSize() + " bytes.",
				new StackTrace(), BinaryDataFactory.class);
		}

		return result;
	}

	private static File dumpToFile(InputStream content) throws IOException, FileNotFoundException {
		File file = File.createTempFile("FileBasedBinaryData", ".bin", Settings.getInstance().getTempDir());
		try (OutputStream output = new FileOutputStream(file)) {
			StreamUtilities.copyStreamContents(content, output);
		}
		return file;
	}

	/**
	 * Creates a class-relative {@link BinaryData} representing a resource from the class path.
	 */
	public static BinaryData createBinaryData(Class<?> context, String resourceName) {
		return new AbstractBinaryData() {
			String _lazyContentType = null;

			@Override
			public InputStream getStream() throws IOException {
				InputStream result = context.getResourceAsStream(resourceName);
				if (result == null) {
					throw new IOException(
						"Resource '" + resourceName + "' does not exist relative to '" + context.getName() + "'.");
				}
				return result;
			}

			@Override
			public String getName() {
				return context.getResource(resourceName).toExternalForm();
			}

			@Override
			public long getSize() {
				return -1;
			}

			@Override
			public String getContentType() {
				if (_lazyContentType == null) {
					_lazyContentType = guessContentType(resourceName);
				}
				return _lazyContentType;
			}

			private String guessContentType(String filename) {
				return MimeTypesModule.getInstance().getMimeType(filename);
			}
		};
	}

	/**
	 * Wraps upload data as a {@link BinaryData}.
	 */
	public static BinaryData createUploadData(Part part) throws IOException {
		File tempDir = Settings.getInstance().getTempDir();
		File tempFile = File.createTempFile("upload", ".data", tempDir);
		part.write(tempFile.getAbsolutePath());

		String submittedFileName = part.getSubmittedFileName();
		String name = submittedFileName != null ? submittedFileName : part.getName();
	
		String contentType = AbstractBinaryData.nonNullContentType(part.getContentType());
	
		return createBinaryData(tempFile, contentType, name);
	}

}
