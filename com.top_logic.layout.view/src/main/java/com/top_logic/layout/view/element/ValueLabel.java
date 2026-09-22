/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.util.Resources;

/**
 * The text a value becomes where a view displays it.
 *
 * <p>
 * A {@link ResKey} - what an internationalized literal of a TL-Script expression answers - is the
 * text it stands for in the language of the session, a {@link String} is that text itself, and any
 * other value is named by the {@link MetaLabelProvider}, which is how the model says what an object
 * is called.
 * </p>
 *
 * @see TextElement
 * @see ProgressElement
 * @see JobStatusElement
 */
public class ValueLabel {

	/**
	 * Not instantiated: a namespace for {@link #label(Object)}.
	 */
	private ValueLabel() {
		// No instances.
	}

	/**
	 * The text naming the given value.
	 *
	 * @param value
	 *        What is to be displayed, {@code null} for no value at all.
	 * @return The text the reader sees, {@code null} for no value at all.
	 */
	public static String label(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof ResKey key) {
			return Resources.getInstance().getString(key);
		}
		if (value instanceof String text) {
			return text;
		}
		return MetaLabelProvider.INSTANCE.getLabel(value);
	}

}
