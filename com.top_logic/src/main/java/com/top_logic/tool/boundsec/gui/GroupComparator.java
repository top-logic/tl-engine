/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui;

import java.text.Collator;
import java.util.Comparator;

import com.top_logic.knowledge.gui.GroupResourceProvider;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContext;

/**
 * {@link Comparator} for {@link Group}s based on their labels.
 * 
 * @author <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
public class GroupComparator implements Comparator {
	private Comparator baseComparator;

	boolean asc;
	public GroupComparator(boolean asc){
		this.asc = asc;
		this.baseComparator = Collator.getInstance(TLContext.getLocale());
	}
	
	@Override
	public int compare(Object o1, Object o2) {
		if(o1 instanceof Group && o2 instanceof Group){
			
			int i = baseComparator.compare(GroupResourceProvider.INSTANCE.getLabel(o1), GroupResourceProvider.INSTANCE.getLabel(o2));
//			int i = GroupResourceProvider.INSTANCE.getLabel(o1).compareTo(GroupResourceProvider.INSTANCE.getLabel(o2));
			if(!asc){
				i=-1*i;
			}
			return i;
		}
		return 0;
	}

}
