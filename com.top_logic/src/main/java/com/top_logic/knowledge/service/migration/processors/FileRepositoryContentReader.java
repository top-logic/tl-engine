/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;

/**
 * {@link RepositoryContentReader} for a repository stored in a directory of the file system.
 *
 * <p>
 * Each document is stored in a directory named after the document with the prefix
 * {@value #ENTRY_PREFIX}, located in the directories of its path. A path element (other than the
 * document name) that starts with {@value #ESCAPE} is stored with a doubled {@value #ESCAPE}. The
 * directory of a document contains one file for each version, named
 * <code>_&lt;version&gt;_n_&lt;author&gt;</code>; a version deleted in place is marked by a file
 * <code>_&lt;version&gt;_d_&lt;author&gt;</code> and has no content.
 * </p>
 *
 * <p>
 * A document that was deleted from the repository is moved to the attic: its directory of versions
 * is located in the attic under the unescaped path of the document.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FileRepositoryContentReader extends AbstractConfiguredInstance<FileRepositoryContentReader.Config<?>>
		implements RepositoryContentReader {

	/** Character escaping the bookkeeping names of the repository. */
	public static final String ESCAPE = "_";

	/** Prefix of the directory holding the versions of a document. */
	public static final String ENTRY_PREFIX = ESCAPE + "f";

	/** Infix of a version file holding content. */
	public static final String NORMAL_VERSION_INFIX = ESCAPE + "n" + ESCAPE;

	/** Separator of path elements. */
	public static final String PATH_SEPARATOR = "/";

	/**
	 * Configuration options of {@link FileRepositoryContentReader}.
	 */
	public interface Config<I extends FileRepositoryContentReader> extends PolymorphicConfiguration<I> {

		/** Configuration name of {@link #getPath()}. */
		String PATH = "path";

		/** Configuration name of {@link #getAttic()}. */
		String ATTIC = "attic";

		/**
		 * The root directory of the repository.
		 */
		@Name(PATH)
		@Mandatory
		String getPath();

		/**
		 * @see #getPath()
		 */
		void setPath(String value);

		/**
		 * The directory holding documents deleted from the repository.
		 *
		 * <p>
		 * If not given, deleted documents are not looked up.
		 * </p>
		 */
		@Name(ATTIC)
		@Nullable
		String getAttic();

		/**
		 * @see #getAttic()
		 */
		void setAttic(String value);

	}

	private final File _root;

	private final File _attic;

	/**
	 * Creates a {@link FileRepositoryContentReader} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FileRepositoryContentReader(InstantiationContext context, Config<?> config) {
		super(context, config);
		_root = new File(config.getPath());
		String attic = config.getAttic();
		_attic = StringServices.isEmpty(attic) ? null : new File(attic);
	}

	@Override
	public BinaryData read(String path, int version) throws IOException {
		if (version <= 0) {
			return null;
		}
		File versions = entryDirectory(path);
		if (versions == null) {
			return null;
		}
		String prefix = ESCAPE + version + NORMAL_VERSION_INFIX;
		String[] names = versions.list();
		if (names == null) {
			throw new IOException("Cannot list versions of '" + path + "' in '" + versions + "'.");
		}
		for (String name : names) {
			if (name.startsWith(prefix)) {
				File content = new File(versions, name);
				if (content.isFile()) {
					return BinaryDataFactory.createBinaryData(content);
				}
			}
		}
		return null;
	}

	/**
	 * The directory holding the versions of the document with the given path, <code>null</code> if
	 * there is no such document.
	 */
	private File entryDirectory(String path) {
		String[] elements = elements(path);
		if (elements.length == 0) {
			return null;
		}

		File parent = _root;
		for (int n = 0; n < elements.length - 1; n++) {
			parent = new File(parent, escape(elements[n]));
		}
		File entry = new File(parent, ENTRY_PREFIX + elements[elements.length - 1]);
		if (entry.isDirectory()) {
			return entry;
		}

		if (_attic != null) {
			File deleted = new File(_attic, String.join(File.separator, elements));
			if (deleted.isDirectory()) {
				return deleted;
			}
		}
		return null;
	}

	private static String[] elements(String path) {
		return Arrays.stream(path.split(PATH_SEPARATOR))
			.filter(element -> !element.isEmpty())
			.toArray(String[]::new);
	}

	private static String escape(String element) {
		return element.startsWith(ESCAPE) ? ESCAPE + element : element;
	}

}
