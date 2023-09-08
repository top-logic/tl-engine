/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import java.util.Comparator;

import com.top_logic.basic.Named;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.util.TLCollator;

/**
 * Comparator for grouping web folders and other objects before looking at the name of the business
 * object.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WebFolderComparator implements Comparator<FolderNode> {

	private final TLCollator _collator;

	/**
	 * Creates a new {@link WebFolderComparator}.
	 */
	public WebFolderComparator() {
		_collator = new TLCollator();
	}

    @Override
	public int compare(FolderNode aNode1, FolderNode aNode2) {
		boolean isFolder1 = aNode1.isFolder();
		boolean isFolder2 = aNode2.isFolder();

		if (isFolder1) {
			if (isFolder2) {
				/* Compare folders by name */
				return compareByName(aNode1, aNode2);
			} else {
				/* Folder before ordinary files */
				return -1;
			}
		} else {
			if (!isFolder2) {
				/* Compare files by name */
				return compareByName(aNode1, aNode2);
			} else {
				/* Ordinary files after folder */
				return 1;
			}
		}
    }

	private int compareByName(FolderNode node1, FolderNode node2) {
		Named content1 = (Named) node1.getBusinessObject();
		Named content2 = (Named) node2.getBusinessObject();
		return _collator.compare(content1.getName(), content2.getName());
	}
}