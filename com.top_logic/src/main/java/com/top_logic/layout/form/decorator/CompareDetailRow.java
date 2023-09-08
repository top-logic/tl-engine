/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

/**
 * Row object of detail dialog of compare view, which contains besides {@link CompareInfo}, the
 * label of the attribute, to which the {@link CompareInfo} belongs to.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class CompareDetailRow {

	private String _attributeLabel;
	private CompareInfo _compareInfo;

	/**
	 * Create a new {@link CompareDetailRow}.
	 */
	public CompareDetailRow(String attributeLabel, CompareInfo compareInfo) {
		_attributeLabel = attributeLabel;
		_compareInfo = compareInfo;
	}

	/**
	 * label of attribute, that is shown in row.
	 */
	public String getAttributeLabel() {
		return _attributeLabel;
	}

	/**
	 * compare values of attribute, that is shown in row.
	 */
	public CompareInfo getCompareInfo() {
		return _compareInfo;
	}

}
