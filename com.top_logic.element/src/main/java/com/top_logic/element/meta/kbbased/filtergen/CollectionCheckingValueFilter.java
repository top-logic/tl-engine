/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.List;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.model.TLObject;

/**
 * Filter for multiple values in a certain context.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
@Deprecated
public interface CollectionCheckingValueFilter extends AttributedValueFilter {

	/**
	 * Check on a collection of MandatorAware objects and return the matching objects
	 * 
	 * @param anAttributed
	 *        the attibuted (e.g. a Contract). May be <code>null</code> in a search (update
	 *        container must be given then)
	 * @param editContext
	 *        the update container (may be <code>null</code>)
	 * @param aList
	 *        the objects to be checked
	 * @return the matching objects.
	 */
	public List filter(TLObject anAttributed, EditContext editContext, FormContext aContext,
			List aList);

}
