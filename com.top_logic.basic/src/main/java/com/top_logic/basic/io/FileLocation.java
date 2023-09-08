/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.URL;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.tools.NameBuilder;

/**
 * Encapsulation and lazy accessor for the various ways how a {@link File} can be found:
 * 
 * <ul>
 * <li>via the constructor: {@link File#File(String)}</li>
 * <li>via the {@link FileManager#getFile(String)}</li>
 * <li>next to a context {@link Class} with {@link Class#getResource(String)}</li>
 * </ul>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class FileLocation {

	static final class ConstructorFileLocation extends FileLocation {

		protected ConstructorFileLocation(String path) {
			super(path);
		}

		@Override
		protected File internalGet() {
			return new File(getName());
		}

		@Override
		public String toString() {
			return new NameBuilder(this).add("path", getName()).buildName();
		}

	}

	static final class FileManagerFileLocation extends FileLocation {

		protected FileManagerFileLocation(String path) {
			super(path);
		}

		@Override
		protected File internalGet() {
			return FileManager.getInstance().getIDEFileOrNull(getName());
		}

		@Override
		public String toString() {
			return new NameBuilder(this).add("path", getName()).buildName();
		}

	}

	static final class ContextClassFileLocation extends FileLocation {

		private final Class<?> _contextClass;

		protected ContextClassFileLocation(String path, Class<?> contextClass) {
			super(path);
			_contextClass = contextClass;
		}

		@Override
		protected File internalGet() {
			String name = getName();
			URL resource = _contextClass.getResource(name);
			if (resource == null) {
				throw new IOError(new IOException(
					"Resource '" + name + "' does not exist relative to '" + _contextClass.getName() + "'."));
			}
			return FileUtilities.urlToFile(resource);
		}

		@Override
		public String toString() {
			return new NameBuilder(this).add("contextClass", _contextClass).add("name", getName()).buildName();
		}

	}

	private final String _name;

	/**
	 * @see #fileFromConstructor(String)
	 * @see #fileFromFileManager(String)
	 * @see #fileByContextClass(String, Class)
	 */
	protected FileLocation(String path) {
		_name = path;
	}

	/** Create the {@link File} by calling {@link File#File(String)} */
	public static FileLocation fileFromConstructor(String path) {
		return new ConstructorFileLocation(path);
	}

	/** Create the {@link File} by calling {@link FileManager#getFile(String)} */
	public static FileLocation fileFromFileManager(String path) {
		return new FileManagerFileLocation(path);
	}

	/** Create the {@link File} by using {@link Class#getResource(String)}. */
	public static FileLocation fileByContextClass(String name, Class<?> contextPath) {
		return new ContextClassFileLocation(name, contextPath);
	}

	/**
	 * Build the {@link File} meant by this {@link FileLocation}.
	 * <p>
	 * The returned {@link File} is not guaranteed to exist. <br/>
	 * </p>
	 * 
	 * @return Never <code>null</code>.
	 * 
	 * @throws RuntimeException
	 *         If the file access failed.
	 */
	public File get() {
		File result = internalGet();
		if (result == null) {
			throw new RuntimeException("Failed to access the file: '" + _name + "'");
		}
		return result;
	}

	/** The name (and path) to the {@link File}. */
	protected String getName() {
		return _name;
	}

	/**
	 * The actual creation of the {@link File}.
	 * 
	 * @return Never <code>null</code>.
	 */
	protected abstract File internalGet();

}
