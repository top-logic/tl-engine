/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.PropertyObservableBase;


/**
 * Simple implementation of {@link Collapsible} holding listener in a {@link PropertyObservableBase}
 * .
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultExpansionModel extends PropertyObservableBase implements Collapsible {

	private boolean _collapsed;

	/**
	 * Creates a new {@link DefaultExpansionModel}.
	 * 
	 * @param collapsed
	 *        Initial value of {@link #isCollapsed()}.
	 */
	public DefaultExpansionModel(boolean collapsed) {
		_collapsed = collapsed;
	}

	@Override
	public boolean isCollapsed() {
		return _collapsed;
	}

	@Override
	public void setCollapsed(boolean value) {
		boolean oldValue = isCollapsed();
		if (value == oldValue) {
			return;
		}
		_collapsed = value;

		notifyListeners(COLLAPSED_PROPERTY, this, oldValue, value);
	}

}
