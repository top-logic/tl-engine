/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.RevisionNamingScheme;

/**
 * Reference to {@link Revision} objects.
 * 
 * @see RevisionNamingScheme
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RevisionName extends ModelName {

	/**
	 * The commit number of the referenced {@link Revision}.
	 * 
	 * <p>
	 * This is either {@link Revision#getCommitNumber() the commit number of the revision} for
	 * stable revisions, or <code>null</code> for the current revision.
	 * </p>
	 * 
	 * <p>
	 * Note: The value for the current revision is not {@link Revision#CURRENT_REV} for simplicity.
	 * When this {@link RevisionName} is used in XML it is easier to omit the commit number than
	 * typing <code>9223372036854775807</code>.
	 * </p>
	 */
	Long getCommitNumber();

	/** @see #getCommitNumber() */
	void setCommitNumber(Long commitNumber);

}
