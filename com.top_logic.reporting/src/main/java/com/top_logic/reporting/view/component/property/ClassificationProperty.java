/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.property;

import java.util.Comparator;
import java.util.List;

import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.LabelComparator;
import com.top_logic.tool.boundsec.BoundComponent;

/**
 * {@link FilterProperty} for {@link FastList}s. Primary use are classifications.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class ClassificationProperty extends WrapperListProperty {

	private FastList list;

	/**
	 * Create a new ClassificationProperty using the {@link LabelComparator} as option comparator.
	 */
	public ClassificationProperty(String name, List<FastListElement> initialValue, FastList aList, boolean multiSelect, BoundComponent aComponent) {
		this(name, initialValue, aList, multiSelect, aComponent, LabelComparator.newCachingInstance());
	}
	
	/**
	 * Create a new ClassificationProperty ...
	 */
	public ClassificationProperty(String name, List<FastListElement> initialValue, FastList aList, boolean multiSelect, BoundComponent aComponent, Comparator<Object> comparator) {
		super(name, initialValue, multiSelect, aComponent, comparator);
		list = aList;
	}

	@Override
	protected List<FastListElement> getAllElements() {
		return list.elements();
	}

}
