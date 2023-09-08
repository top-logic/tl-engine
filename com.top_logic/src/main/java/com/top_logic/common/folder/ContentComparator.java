/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder;

import java.util.Comparator;

import com.top_logic.basic.Named;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * Compares two {@link Named}: {@link BinaryData} are larger than ordinary {@link Named}. If both ore
 * non are {@link BinaryData} then the names are compared.
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ContentComparator implements Comparator {

	public static final ContentComparator INSTANCE = new ContentComparator();

	@Override
	public int compare(Object aNode1, Object aNode2) {

		Named content1 = (Named) aNode1;
		Named content2 = (Named) aNode2;

		boolean isLeaf1 = content1 instanceof BinaryData;
		boolean isLeaf2 = content2 instanceof BinaryData;

		if (isLeaf1 && !isLeaf2) {
			return 1;
		}
		else if (isLeaf2 && !isLeaf1) {
			return -1;
		}

		return content1.getName().compareTo(content2.getName());
	}
}
