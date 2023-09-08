/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.model.TLClassifier;

/**
 * Common Superclass for Filters based on a single or multiple {@link FastListElement}s. 
 * 
 * @author     <a href="mailto:jco@top-logic.com">jco</a>
 */
public abstract class AbstractTableFLElementFilter implements Filter<Object> {

	protected TLClassifier extractFastListElement(Object anObject) {
		if (anObject instanceof List<?> && ((List<?>) anObject).size() > 0) {
			anObject = ((List<?>) anObject).get(0);
		}
		if (anObject instanceof Collection<?> && ((Collection<?>)anObject).size() > 0) {
			anObject = ((Collection<?>) anObject).iterator().next();
		}
		if (anObject instanceof SelectField) {
		    anObject = ((SelectField)anObject).getSingleSelection();
		}
        if (anObject instanceof FastListElement) { 
            return (FastListElement) anObject;
        }
        return null;
	}

	protected Collection<? extends TLClassifier> extractFastListElements(Object anObject) {
		if (anObject instanceof Collection<?>) {
			return (Collection<TLClassifier>) anObject;
		}
		if (anObject instanceof SelectField) {
			return ((SelectField)anObject).getSelection();
		}
		return null;
	}

}
