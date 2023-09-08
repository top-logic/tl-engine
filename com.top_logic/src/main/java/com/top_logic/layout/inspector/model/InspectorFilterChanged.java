/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.model;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.layout.inspector.model.nodes.InspectorTreeNode;

/**
 * {@link PropertyListener} handling change of filter in an {@link InspectorModel}.
 * 
 * @see InspectorModel#PROP_FILTER_CHANGED
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface InspectorFilterChanged extends PropertyListener {

	/**
	 * Handles change of {@link InspectorModel#getFilter()}.
	 */
	void handleFilterChanged(InspectorModel sender, Filter<? super InspectorTreeNode> oldFilter,
			Filter<? super InspectorTreeNode> newFilter);

}

