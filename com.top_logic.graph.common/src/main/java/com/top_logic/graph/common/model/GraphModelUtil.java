/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model;

import static java.util.Objects.*;

import java.util.Collection;
import java.util.Objects;

/**
 * Useful methods for working with {@link GraphModel} and related classes.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class GraphModelUtil {

	/**
	 * Returns the first {@link Label} or creates it if there is none.
	 * 
	 * @param labelOwner
	 *        Is not allowed to be null.
	 * @param tag
	 *        The tag to find/create the label for.
	 * @return Never null.
	 */
	public static Label getLabel(LabelOwner labelOwner, Object tag) {
		Collection<? extends Label> labels = labelOwner.getLabels();
		for (Label label : labels) {
			if (Objects.equals(label.getTag(), tag)) {
				return label;
			}
		}
		Label newLabel = labelOwner.createLabel();
		newLabel.setTag(tag);
		return requireNonNull(newLabel);
	}

}
