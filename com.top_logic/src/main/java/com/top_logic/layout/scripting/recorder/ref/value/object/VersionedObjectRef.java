/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;

/**
 * Abstract description of branch and revision aware objects.
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface VersionedObjectRef extends ObjectRef {

	/**
	 * Description of the branch this object resides in.
	 * 
	 * <p>
	 * <code>null</code> means trunk.
	 * </p>
	 */
	ModelName getBranch();

	/** @see #getBranch() */
	void setBranch(ModelName value);

	/**
	 * Description of the revision this object resides in.
	 * 
	 * <p>
	 * <code>null</code> means current.
	 * </p>
	 */
	ModelName getRevision();

	/** @see #getRevision() */
	void setRevision(ModelName value);

}
