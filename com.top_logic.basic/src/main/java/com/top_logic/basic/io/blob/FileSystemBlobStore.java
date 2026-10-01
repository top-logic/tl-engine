/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.blob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.LimitedInputStream;

/**
 * {@link BlobStore} keeping each blob in a file of a local (or mounted) file system.
 *
 * <p>
 * The file of a blob is located in two levels of shard directories named after the first and
 * second pair of hex digits of its key: <code>&lt;root&gt;/ab/cd/abcd1234-...</code>. This limits
 * the number of entries per directory to the number of blobs divided by 65,536. Shard directories
 * are created on demand and never removed.
 * </p>
 *
 * <p>
 * Content is written to a temporary file in the directory <code>&lt;root&gt;/.tmp</code>, flushed
 * to the storage device and then atomically renamed to its final name, so a blob is either
 * completely visible under its key or not at all. Temporary files left over by a crash are removed
 * by the cleanup of the store.
 * </p>
 *
 * <p>
 * The content type of a blob is not stored. The store does not encrypt the content; encryption at
 * rest is a matter of the volume the root directory resides on.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FileSystemBlobStore extends AbstractBlobStore<FileSystemBlobStore.Config<?>> {

	/**
	 * Name of the directory below the root directory that holds files being written.
	 */
	public static final String TEMP_DIR_NAME = ".tmp";

	private static final String TEMP_FILE_PREFIX = "blob-";

	private static final String TEMP_FILE_SUFFIX = ".tmp";

	/**
	 * Number of hex digits of the key that name a shard directory.
	 */
	private static final int SHARD_LENGTH = 2;

	/**
	 * Names of shard directories.
	 */
	private static final Pattern SHARD_PATTERN = Pattern.compile("[0-9a-f]{" + SHARD_LENGTH + "}");

	/**
	 * Configuration of a {@link FileSystemBlobStore}.
	 */
	public interface Config<I extends FileSystemBlobStore> extends BlobStore.Config<I> {

		/**
		 * Configuration name of {@link #getRoot()}.
		 */
		String ROOT = "root";

		/**
		 * The directory in which the blobs are stored.
		 *
		 * <p>
		 * The directory is created when the first blob is stored. It must not be shared with other
		 * stores or other content.
		 * </p>
		 */
		@Name(ROOT)
		@Mandatory
		String getRoot();

		/**
		 * @see #getRoot()
		 */
		void setRoot(String value);

		/**
		 * Implementation class of the store.
		 */
		@Override
		@ClassDefault(FileSystemBlobStore.class)
		Class<? extends I> getImplementationClass();

	}

	private final Path _root;

	private final Path _tempDir;

	/**
	 * Creates a {@link FileSystemBlobStore} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FileSystemBlobStore(InstantiationContext context, Config<?> config) {
		super(context, config);
		_root = Path.of(config.getRoot()).toAbsolutePath().normalize();
		_tempDir = _root.resolve(TEMP_DIR_NAME);
	}

	/**
	 * The directory in which the blobs are stored.
	 */
	public Path getRoot() {
		return _root;
	}

	/**
	 * The file holding the content of the blob with the given key.
	 *
	 * @param key
	 *        The key of the blob.
	 * @throws IllegalArgumentException
	 *         If the key is not a valid key of this store.
	 */
	public Path getFile(String key) {
		checkKey(key);
		return _root
			.resolve(key.substring(0, SHARD_LENGTH))
			.resolve(key.substring(SHARD_LENGTH, 2 * SHARD_LENGTH))
			.resolve(key);
	}

	@Override
	public String put(InputStream content, long size, String contentType) throws IOException {
		String key = newKey();
		Path target = getFile(key);

		Files.createDirectories(_tempDir);
		Path temp = Files.createTempFile(_tempDir, TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
		boolean success = false;
		try {
			try (FileChannel channel = FileChannel.open(temp, StandardOpenOption.WRITE)) {
				OutputStream out = Channels.newOutputStream(channel);
				long written = content.transferTo(out);
				checkSize(size, written);
				channel.force(true);
			}
			Files.createDirectories(target.getParent());
			Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
			success = true;
		} finally {
			if (!success) {
				deleteTemp(temp);
			}
		}
		return key;
	}

	private static void deleteTemp(Path temp) {
		try {
			Files.deleteIfExists(temp);
		} catch (IOException ex) {
			Logger.warn("Cannot delete temporary blob file '" + temp + "'.", ex, FileSystemBlobStore.class);
		}
	}

	@Override
	public InputStream get(String key) throws IOException {
		Path file = getFile(key);
		try {
			return Files.newInputStream(file);
		} catch (NoSuchFileException ex) {
			throw new NoSuchBlobException(getName(), key, ex);
		}
	}

	@Override
	public InputStream get(String key, long offset, long length) throws IOException {
		checkRange(offset, length);
		Path file = getFile(key);
		FileChannel channel;
		try {
			channel = FileChannel.open(file, StandardOpenOption.READ);
		} catch (NoSuchFileException ex) {
			throw new NoSuchBlobException(getName(), key, ex);
		}
		try {
			channel.position(offset);
		} catch (IOException ex) {
			channel.close();
			throw ex;
		}
		return new LimitedInputStream(Channels.newInputStream(channel), length);
	}

	@Override
	public void delete(String key) throws IOException {
		Files.deleteIfExists(getFile(key));
	}

	@Override
	public Stream<BlobInfo> list() throws IOException {
		return sortedEntries(_root, SHARD_PATTERN)
			.flatMap(level1 -> sortedEntries(level1, SHARD_PATTERN))
			.flatMap(level2 -> sortedEntries(level2, KEY_PATTERN))
			.map(this::info)
			.filter(info -> info != null);
	}

	/**
	 * The entries of the given directory whose names match the given pattern, in lexicographic
	 * order of their names.
	 *
	 * <p>
	 * The directory is read completely and closed before the result is returned, so the result
	 * stream holds no open file handles. A missing directory has no entries.
	 * </p>
	 */
	private static Stream<Path> sortedEntries(Path dir, Pattern namePattern) {
		List<Path> result = new ArrayList<>();
		try (DirectoryStream<Path> entries = Files.newDirectoryStream(dir)) {
			for (Path entry : entries) {
				if (namePattern.matcher(entry.getFileName().toString()).matches()) {
					result.add(entry);
				}
			}
		} catch (NoSuchFileException ex) {
			return Stream.empty();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		Collections.sort(result, (p1, p2) -> p1.getFileName().toString().compareTo(p2.getFileName().toString()));
		return result.stream();
	}

	/**
	 * The {@link BlobInfo} of the given blob file, or <code>null</code> if it has vanished since
	 * it was listed.
	 */
	private BlobInfo info(Path file) {
		BasicFileAttributes attributes;
		try {
			attributes = Files.readAttributes(file, BasicFileAttributes.class);
		} catch (NoSuchFileException ex) {
			return null;
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		if (!attributes.isRegularFile()) {
			return null;
		}
		return new BlobInfo(file.getFileName().toString(), attributes.size(),
			attributes.lastModifiedTime().toInstant());
	}

	@Override
	public void cleanup(Instant olderThan) throws IOException {
		try (DirectoryStream<Path> entries = Files.newDirectoryStream(_tempDir)) {
			for (Path entry : entries) {
				try {
					BasicFileAttributes attributes = Files.readAttributes(entry, BasicFileAttributes.class);
					if (attributes.isRegularFile() && attributes.lastModifiedTime().toInstant().isBefore(olderThan)) {
						Files.deleteIfExists(entry);
					}
				} catch (NoSuchFileException ex) {
					// Concurrently moved to its final location or deleted.
				}
			}
		} catch (NoSuchFileException ex) {
			// Nothing written yet.
		}
	}

}
