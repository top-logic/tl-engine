/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.element.meta.AttributeUpdate.StoreAlgorithm;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.model.form.EditContextBase;

/**
 * Description of an atomic model edit operation such as editing a single attribute of some object.
 * 
 * <p>
 * A form presented to the user is composed of multiple edit contexts each of them representing a
 * single input field.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface EditContext extends EditContextBase {

	/**
	 * The {@link TLFormObject} this {@link EditContext} stores an attribute value for.
	 */
	TLFormObject getOverlay();

	/**
	 * Option provider relevant for the current context.
	 */
	Generator getOptions();

	/**
	 * Sets the algorithm to persist the value.
	 */
	@FrameworkInternal
	void setStoreAlgorithm(StoreAlgorithm storeAlgorithm);

	/**
	 * {@link AttributedValueFilter} relevant for the current context.
	 */
	@FrameworkInternal
	@Deprecated
	default AttributedValueFilter getFilter() {
		return null;
	}

}
