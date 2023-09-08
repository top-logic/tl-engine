/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder;

import java.util.Comparator;

import com.top_logic.basic.Logger;
import com.top_logic.basic.Named;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.common.folder.model.FolderNode;

/**
 * Comparator for comparing log columns by name.
 * 
 * @author <a href=mailto:jes@top-logic.com>Jens Schäfer</a>
 */
public class LogColumnComparator implements Comparator<Object> {

	/** Singleton instance */
	public static final LogColumnComparator INSTANCE = new LogColumnComparator();

	@Override
	public int compare(Object aNode1, Object aNode2) {
		Named content1 = getContentDefinition(aNode1);
		Named content2 = getContentDefinition(aNode2);

		if (content1 == null) {
			Logger.warn("can not compare " + aNode1, this);
			return 0;
		}
		if (content2 == null) {
			Logger.warn("can not compare " + aNode2, this);
			return 0;
		}

		boolean isLeaf1 = content1 instanceof BinaryData;
		boolean isLeaf2 = content2 instanceof BinaryData;

		if (isLeaf1 && !isLeaf2) {
			return 1;
		} else if (isLeaf2 && !isLeaf1) {
			return -1;
		}

		return content1.getName().compareTo(content2.getName());
	}

	private Named getContentDefinition(Object node) {
		if (node instanceof Named) {
			return (Named) node;
		} else if (node instanceof FolderNode) {
			Object bo = ((FolderNode) node).getBusinessObject();
			if (bo instanceof Named) {
				return (Named) bo;
			}
		}
		return null;
	}

}
