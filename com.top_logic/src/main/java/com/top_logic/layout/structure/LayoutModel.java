/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.NoBubblingEventType;
import com.top_logic.basic.listener.PropertyObservable;

/**
 * Model of a {@link LayoutControl} that has modifyable {@link LayoutData layout
 * information}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LayoutModel extends PropertyObservable {

	/**
	 * @see #getLayoutData()
	 * @see LayoutDataListener
	 */
	EventType<LayoutDataListener, Object, LayoutData> LAYOUT_DATA_PROPERTY =
		new NoBubblingEventType<>("layoutData") {

			@Override
			protected void internalDispatch(LayoutDataListener listener, Object sender, LayoutData oldValue,
					LayoutData newValue) {
				listener.handleLayoutDataChanged(sender, oldValue, newValue, false);
			}

		};

	/**
	 * Property for layout size update events
	 * 
	 * @see LayoutDataListener
	 */
	EventType<LayoutDataListener, Object, LayoutData> LAYOUT_SIZE_UPDATE_PROPERTY =
		new NoBubblingEventType<>("layoutSizeUpdate") {

			@Override
			protected void internalDispatch(LayoutDataListener listener, Object sender, LayoutData oldValue,
					LayoutData newValue) {
				listener.handleLayoutDataChanged(sender, oldValue, newValue, true);
			}

		};

	/**
	 * The {@link LayoutData} that describes the current layout information.
	 */
	LayoutData getLayoutData();

	/**
	 * @param layoutSizeUpdate
	 *        - whether layout data has been changed, due to client triggered size adjustments, or
	 *        not
	 * @see #getLayoutData()
	 */
	void setLayoutData(LayoutData newValue, boolean layoutSizeUpdate);

}
