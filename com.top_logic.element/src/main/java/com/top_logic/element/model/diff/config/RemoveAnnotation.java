/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.TLAnnotation;

/**
 * Describes deleting a {@link TLAnnotation}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("delete-annotation")
public interface RemoveAnnotation extends PartUpdate {

	/**
	 * The annotation type to delete.
	 */
	@Mandatory
	Class<? extends TLAnnotation> getAnnotation();

	/** @see #getAnnotation() */
	void setAnnotation(Class<? extends TLAnnotation> value);

}
