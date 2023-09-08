/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.list;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;

/**
 * @author    <a href="mailto:tdi@top-logic.com">tdi</a>
 */
public class FastListElementFilter implements Filter<Object> {

    private String   propertyName;

	private Accessor<Object> accessor;

	private List<String> accepted;

    /** 
     * Creates a {@link FastListElementFilter}.
     */
    public FastListElementFilter(String aPropertyName, String[] acceptedElementNames) {
        this(aPropertyName, Arrays.asList(acceptedElementNames));
    }
    
    /** 
     * Creates a {@link FastListElementFilter}.
     */
	public FastListElementFilter(String aPropertyName, String[] acceptedElementNames, Accessor<Object> anAccessor) {
        this(aPropertyName, Arrays.asList(acceptedElementNames), anAccessor);
    }
    
    /** 
     * Creates a {@link FastListElementFilter}.
     */
	public FastListElementFilter(String aPropertyName, List<String> acceptedElementNames) {
        this(aPropertyName, acceptedElementNames, WrapperAccessor.INSTANCE);
    }

    /** 
     * Creates a {@link FastListElementFilter}.
     */
	public FastListElementFilter(String aPropertyName, List<String> acceptedElementNames, Accessor<Object> anAccessor) {
        this.propertyName = aPropertyName;
        this.accepted     = acceptedElementNames;
        this.accessor     = anAccessor;
    }
    
	@Override
	public boolean accept(Object anObject) {
		Object rawValue = this.accessor.getValue(anObject, this.propertyName);
		if (rawValue instanceof Collection) {
			return acceptElementCollection((Collection) rawValue);
		} else if (rawValue instanceof FastListElement) {
			return acceptElement((FastListElement) rawValue);
		}
		return false;
	}

	private boolean acceptElementCollection(Collection<? extends FastListElement> selected) {
		Iterator<? extends FastListElement> iterator = selected.iterator();

		while (iterator.hasNext()) {
			boolean elementAccepted = acceptElement(iterator.next());
			if (elementAccepted) {
				return true;
			}
		}
		return false;
	}

	private boolean acceptElement(FastListElement element) {
		return this.accepted.contains(element.getName());
	}

}

