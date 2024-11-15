/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLReference;

/**
 * Deletion of a {@link TLModelPart}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("delete")
public interface Delete extends DiffElement {

	/**
	 * The {@link TLNamedPart#getName() name} of the {@link TLModelPart} to delete.
	 */
	@Mandatory
	String getName();

	/** @see #getName() */
	void setName(String value);

	/**
	 * The kind of model element that is deleted.
	 * 
	 * <p>
	 * The value is <code>null</code> for other objects (e.g. singletons) that could also be updated
	 * during migration but have no dedicated {@link TLModelPart model type}.
	 * </p>
	 */
	ModelKind getKind();

	/**
	 * @see #getKind()
	 */
	void setKind(ModelKind modelKind);

	/**
	 * In case of a deleted {@link TLReference}, whether this reference is a
	 * {@link TLReference#isBackwards() backwards} reference.
	 * 
	 * @see #getKind()
	 */
	boolean getBackwards();

	/**
	 * @see #getBackwards()
	 */
	void setBackwards(boolean backwards);

}
