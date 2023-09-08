/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.layout.tree.model.DefaultTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;

/**
 * {@link DefaultTreeTableBuilder} for detail dialog of comparison changes.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ChangeDetailBuilder extends DefaultTreeTableBuilder {

	private final CompareInfo _compareInfo;

	/**
	 * Create a new {@link ChangeDetailBuilder}.
	 */
	public ChangeDetailBuilder(CompareInfo compareInfo) {
		_compareInfo = compareInfo;
	}

	@Override
	public List<DefaultTreeTableModel.DefaultTreeTableNode> createChildList(
			DefaultTreeTableModel.DefaultTreeTableNode node) {
		CompareDetailRow compareDetailRow = (CompareDetailRow) node.getBusinessObject();
		if (compareDetailRow.getCompareInfo() == _compareInfo) {
			ArrayList<DefaultTreeTableNode> children =
				new ArrayList<>();
			if (compareDetailRow.getCompareInfo() instanceof AttributeCompareInfo) {
				AttributeCompareInfo attributeCompareInfo = (AttributeCompareInfo) compareDetailRow.getCompareInfo();
				Collection<String> attributeNames = attributeCompareInfo.getAttributeNames();
				for (String attributeName : attributeNames) {
					children.add(createNode(node.getModel(), node,
						new CompareDetailRow(attributeCompareInfo.getAttributeNameLabel(attributeName),
							attributeCompareInfo.getAttributeCompareInfo(attributeName))));
				}
			}
			return children;
		} else {
			return Collections.emptyList();
		}
	}
}