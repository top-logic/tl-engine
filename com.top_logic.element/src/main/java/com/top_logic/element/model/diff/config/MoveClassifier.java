/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;

/**
 * Description of changing the index of a {@link TLClassifier} within its {@link TLEnumeration}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("move-classifier")
public interface MoveClassifier extends Update {

	/**
	 * Qualified name of the {@link TLClassifier} to move.
	 */
	@Mandatory
	String getClassifier();
	
	/** @see #getClassifier() */
	void setClassifier(String value);

	/**
	 * Local name of the {@link TLClassifier} of the same {@link TLEnumeration} as
	 * {@link #getClassifier()} to insert the moved one before.
	 * 
	 * <p>
	 * A value of <code>null</code> means to move the {@link #getClassifier()} to the end of the
	 * list of {@link TLEnumeration#getClassifiers() classifiers of the modified enumeration}.
	 * </p>
	 */
	@Nullable
	String getBefore();
	
	/** @see #getBefore() */
	void setBefore(String value);

}
