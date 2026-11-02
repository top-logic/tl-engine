/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Objects;

import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * File holding a single secret, read with caching.
 *
 * <p>
 * The file is read as UTF-8 text. Leading and trailing white space, including the line break that
 * tools like <code>echo</code> or an editor append, is not part of the secret.
 * </p>
 *
 * <p>
 * The content is cached and read again when the file has been replaced. The file is identified by
 * its attributes (file key, last-modified time and size), read through symbolic links. Kubernetes
 * updates a mounted secret by writing the new content to a fresh directory and swapping the
 * <code>..data</code> symbolic link to it: the path then resolves to a different file with a
 * different file key (inode) and last-modified time, so the change is detected with a single
 * <code>stat</code> per use instead of reading the content each time.
 * </p>
 *
 * @see FileSecretSource
 * @see DirectorySecretStore
 */
final class SecretFile {

	private final Path _path;

	private volatile Snapshot _snapshot;

	/**
	 * Creates a {@link SecretFile}.
	 *
	 * @param path
	 *        The path of the file holding the secret.
	 */
	SecretFile(Path path) {
		_path = path;
	}

	/**
	 * The path of the file holding the secret.
	 */
	Path getPath() {
		return _path;
	}

	/**
	 * The current content of the file.
	 *
	 * @return The secret, never empty, or <code>null</code> if the file does not exist.
	 *
	 * @throws I18NRuntimeException
	 *         If the file exists but cannot be read or contains only white space.
	 */
	String lookup() {
		try {
			BasicFileAttributes attributes = Files.readAttributes(_path, BasicFileAttributes.class);
			Snapshot snapshot = _snapshot;
			if (snapshot == null || !snapshot.matches(attributes)) {
				String secret = new String(Files.readAllBytes(_path), StandardCharsets.UTF_8).strip();
				snapshot = new Snapshot(attributes, secret);
				_snapshot = snapshot;
			}
			if (snapshot.getSecret().isEmpty()) {
				throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_FILE_EMPTY__PATH.fill(_path.toString()));
			}
			return snapshot.getSecret();
		} catch (NoSuchFileException ex) {
			_snapshot = null;
			return null;
		} catch (IOException ex) {
			_snapshot = null;
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_FILE_UNREADABLE__PATH.fill(_path.toString()),
				ex);
		}
	}

	/**
	 * The content of the secret file together with the attributes of the file it was read from.
	 */
	private static final class Snapshot {

		private final Object _fileKey;

		private final FileTime _lastModified;

		private final long _size;

		private final String _secret;

		Snapshot(BasicFileAttributes attributes, String secret) {
			_fileKey = attributes.fileKey();
			_lastModified = attributes.lastModifiedTime();
			_size = attributes.size();
			_secret = secret;
		}

		/**
		 * Whether the file with the given attributes is the one this snapshot was read from.
		 */
		boolean matches(BasicFileAttributes attributes) {
			return Objects.equals(_fileKey, attributes.fileKey())
				&& _lastModified.equals(attributes.lastModifiedTime())
				&& _size == attributes.size();
		}

		String getSecret() {
			return _secret;
		}

	}

}
