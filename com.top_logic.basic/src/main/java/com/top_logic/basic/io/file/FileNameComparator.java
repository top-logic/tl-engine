/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.file;

import java.io.File;
import java.text.Collator;
import java.util.Comparator;

/**
 * {@link Comparator} that compares {@link File}s by their name, using the given {@link Collator}.
 * <p>
 * This {@link Comparator} is null-safe. Null is less than everything else.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public final class FileNameComparator implements Comparator<File> {

	private final Collator _collator;

	/**
	 * Creates a {@link FileNameComparator} for the given {@link Collator}.
	 */
	public FileNameComparator(Collator collator) {
		_collator = collator;
	}

	@Override
	public int compare(File left, File right) {
		if ((left == null) && (right == null)) {
			return 0;
		}
		if (left == null) {
			return -1;
		}
		if (right == null) {
			return 1;
		}
		return compareNames(left.getName(), right.getName());
	}

	private int compareNames(String left, String rightright) {
		return _collator.compare(left, rightright);
	}

}
