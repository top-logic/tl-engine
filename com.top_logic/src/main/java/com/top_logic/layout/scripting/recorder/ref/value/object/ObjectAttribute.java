/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;
import com.top_logic.model.TLObject;

/**
 * Access to the value of an object attribute.
 * 
 * @see ObjectAttributeByLabel
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface ObjectAttribute extends ValueRef {

	/**
	 * The target object to access, has to be a reference to an {@link TLObject}.
	 */
	ModelName getTarget();

	/** @see #getTarget() */
	void setTarget(ModelName target);

	/** The technical attribute name. */
	String getAttribute();

	/** @see #getAttribute() */
	void setAttribute(String attribute);

}
