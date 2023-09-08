/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;
import com.top_logic.model.TLObject;

/**
 * Access to an object attribute by attribute label.
 * 
 * @see ObjectAttribute
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface ObjectAttributeByLabel extends ValueRef {

	/**
	 * The target object to access, has to be a reference to an {@link TLObject}.
	 */
	ModelName getTarget();

	/** @see #getTarget() */
	void setTarget(ModelName target);

	/** The GUI label of the attribute to access. */
	String getAttributeLabel();

	/** @see #getAttributeLabel() */
	void setAttributeLabel(String attributeLabel);

	/**
	 * Should the {@link #getAttributeLabel()} be matched fuzzy?
	 * <p>
	 * <b>Usage discouraged, as multiple attribute labels can fuzzy match the same string.</b>
	 * </p>
	 */
	@BooleanDefault(false)
	boolean isFuzzy();

	/**
	 * Usage discouraged, as multiple attribute labels can fuzzy match the same string.
	 * 
	 * @see #isFuzzy()
	 */
	void setFuzzy(boolean fuzzy);

}
