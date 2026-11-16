/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * The description a label carries over and above its own text.
 *
 * <p>
 * A label explains itself through its {@link ResKey#tooltipOptional() tooltip}: the resource
 * {@code <label key>.tooltip} of a resource key, or the nested {@code <tooltip>} of an inline
 * label. Wherever a label is shown - the label of a form field, the title of a table column - this
 * description is offered as its tooltip and, where the label offers help, as the help text.
 * </p>
 */
public class LabelDescription {

	private LabelDescription() {
		// Static utility.
	}

	/**
	 * The key of the description of the given label, {@code null} for no label.
	 *
	 * <p>
	 * The key resolves to nothing when the label has no description.
	 * </p>
	 *
	 * @param label
	 *        The label to describe, may be {@code null}.
	 */
	public static ResKey descriptionKey(ResKey label) {
		return label == null ? null : label.tooltipOptional();
	}

	/**
	 * The description of the given label in the current user's language, {@code null} when the
	 * label has none.
	 *
	 * @param label
	 *        The label to describe, may be {@code null}.
	 */
	public static String description(ResKey label) {
		return description(Resources.getInstance(), label);
	}

	/**
	 * The description of the given label in the language of the given resources, {@code null} when
	 * the label has none.
	 *
	 * @param resources
	 *        The resources to resolve the description with.
	 * @param label
	 *        The label to describe, may be {@code null}.
	 */
	public static String description(Resources resources, ResKey label) {
		ResKey key = descriptionKey(label);
		if (key == null) {
			return null;
		}
		String description = resources.getString(key);
		return description == null || description.isEmpty() ? null : description;
	}

}
