/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.log;

import java.io.File;
import java.util.Date;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DispatchingAccessor;
import com.top_logic.layout.PropertyAccessor;
import com.top_logic.layout.ReadOnlyPropertyAccessor;
import com.top_logic.layout.SelfPropertyAccessor;

/**
 * {@link Accessor} for {@link File} objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FileAccessor extends DispatchingAccessor<File> {

	/**
	 * {@link PropertyAccessor} accessing {@link File#length()}
	 */
	public static final PropertyAccessor<File> FILE_LENGTH = new ReadOnlyPropertyAccessor<>() {
		@Override
		public Object getValue(File target) {
			if (target.isDirectory()) {
				/* File.length() is unspecified for directories, as stated in the JavaDoc.
				 * Furthermore, directories have no common definition of "size". Some systems return
				 * the number of bytes used by internal data structures (like inodes), others return
				 * the numbers of files within that folder. And the <i>TopLogic</i> Webfolder never
				 * displays a size for directories. Therefore, this accessor reports no size, too. */
				return null;
			}
			return target.length();
		}
	};
	
	/**
	 * {@link PropertyAccessor} accessing {@link File#lastModified()} as {@link Date}.
	 */
	public static final PropertyAccessor<File> FILE_DATE = new ReadOnlyPropertyAccessor<>() {
		@Override
		public Object getValue(File target) {
			return new Date(target.lastModified());
		}
	};

	/**
	 * Singleton {@link FileAccessor} instance.
	 */
	public static final FileAccessor INSTANCE = new FileAccessor();
	
	
	/**
	 * Creates a {@link FileAccessor}.
	 */
	private FileAccessor() {
		super(new MapBuilder<String, PropertyAccessor<? super File>>()
			.put("name", SelfPropertyAccessor.INSTANCE)
			.put("size", FILE_LENGTH)
			.put("date", FILE_DATE)
			.toMap());
	}

}
