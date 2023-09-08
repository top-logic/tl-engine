/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.listener.PropertyObservableBase;

/**
 * Default {@link LayoutModel} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultLayoutModel extends PropertyObservableBase implements LayoutModel {

	private LayoutData layoutData;

	public DefaultLayoutModel(LayoutData layoutData) {
		this.layoutData = layoutData;
	}

	@Override
	public LayoutData getLayoutData() {
		return layoutData;
	}

	@Override
	public void setLayoutData(LayoutData newValue, boolean layoutSizeUpdate) {
		LayoutData oldValue = this.layoutData;
		this.layoutData = newValue;
		if (layoutSizeUpdate) {
			notifyListeners(LAYOUT_SIZE_UPDATE_PROPERTY, this, oldValue, newValue);
		} else {
			notifyListeners(LAYOUT_DATA_PROPERTY, this, oldValue, newValue);
		}
	}
	

}
