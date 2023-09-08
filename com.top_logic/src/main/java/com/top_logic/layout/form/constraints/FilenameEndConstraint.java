/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import static com.top_logic.basic.util.Utils.*;

import java.util.List;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.util.Resources;

/**
 * Checks the filename end against a given string.
 * <p>
 * The filename and the given file ending are both converted to lower-case before the check.
 * </p>
 * <p>
 * The value to check has to be a {@link BinaryData} or null. Otherwise, a {@link RuntimeException}
 * is thrown.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public final class FilenameEndConstraint extends AbstractConstraint {

	private final String _acceptedFileEnding;

	private final ResKey _errorKey;

	/**
	 * Creates a {@link FilenameEndConstraint}.
	 * 
	 * @param expectedFileEnding
	 *        The filename extension to accept. For example ".config.xml". Is converted to
	 *        lower-case and checked against the lower-case filename end. Is not allowed to be
	 *        null.
	 * @param errorKey
	 *        The error message for wrong filenames. Gets the actual filename as argument.
	 */
	public FilenameEndConstraint(String expectedFileEnding, ResKey errorKey) {
		_acceptedFileEnding = expectedFileEnding.toLowerCase();
		_errorKey = requireNonNull(errorKey);
	}

	@Override
	public boolean check(Object value) throws CheckException {
		List<BinaryData> items = DataField.toItems(value);
		for (BinaryData item : items) {
			if (!item.getName().toLowerCase().endsWith(_acceptedFileEnding)) {
				String message = Resources.getInstance().getMessage(_errorKey, item.getName());
				throw new CheckException(message);
			}
		}
		return true;
	}

}
