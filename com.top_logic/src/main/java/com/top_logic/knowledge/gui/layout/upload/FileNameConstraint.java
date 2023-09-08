/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.upload;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.util.Resources;

/**
 * This constraint checks with a {@link FileNameStrategy} if the corresponding 
 * field contains a valid file name.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class FileNameConstraint extends StringLengthConstraint {

	private FileNameStrategy fileNameStrategy;

	/**
	 * Creates a new {@link FileNameConstraint} with the given
	 * {@link FileNameStrategy}.
	 * 
	 * @param aFileNameStrategy
	 *            A {@link FileNameStrategy} must NOT be <code>null</code>.
	 */
	public FileNameConstraint(FileNameStrategy aFileNameStrategy, int minSize, int maxSize) {
        super(minSize, maxSize);
        this.fileNameStrategy = aFileNameStrategy;
    }
	
	/**
	 * Creates a new {@link FileNameConstraint} with the given {@link FileNameStrategy}.
	 * 
	 * Filenames must not be empty and may at most be 150 character long.
	 * (The actual limit is imposed by DC_TITLE which is 150 characters long)
	 * 
	 * @param aFileNameStrategy {@link FileNameStrategy} must NOT be <code>null</code>.
	 */
	public FileNameConstraint(FileNameStrategy aFileNameStrategy) {
		this(aFileNameStrategy, 1, 150); 
	}

	/**
	 * Returns the fileNameStrategy.
	 */
	public FileNameStrategy getFileNameStrategy() {
		return this.fileNameStrategy;
	}

	/**
	 * @param aFileNameStrategy The fileNameStrategy to set.
	 */
	public void setFileNameStrategy(FileNameStrategy aFileNameStrategy) {
		this.fileNameStrategy = aFileNameStrategy;
	}
	
	/** 
	 * @see com.top_logic.layout.form.constraints.AbstractStringConstraint#checkString(java.lang.String)
	 */
	@Override
	protected boolean checkString(String aValue) throws CheckException {
		boolean lengthOK = super.checkString(aValue);
		if (!lengthOK) {
			return false;
		}

		ResKey errorMessage = this.fileNameStrategy.checkFileName(aValue);
		if (errorMessage != null) {
			throw new CheckException(Resources.getInstance().decodeMessageFromKeyWithEncodedArguments(errorMessage));
		}
		
		return true;
	}
}
