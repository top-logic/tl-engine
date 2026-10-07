/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.List;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCompositeControl;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} that renders a responsive form grid via the {@code TLFormLayout} React
 * component.
 *
 * <p>
 * The layout uses CSS Grid with {@code auto-fit} to provide responsive columns up to
 * {@code maxColumns}. Label positioning can be fixed ("side", "top") or automatic ("auto"), where
 * the React component measures column width and switches between side-by-side and stacked labels.
 * </p>
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@link #MAX_COLUMNS} - maximum number of columns</li>
 * <li>{@link #LABEL_POSITION} - {@link LabelPosition#SIDE}, {@link LabelPosition#TOP}, or
 * {@link LabelPosition#AUTO}</li>
 * <li>{@link #READ_ONLY} - whether the form is read-only</li>
 * <li>{@code children} - child controls (TLFormGroup or TLFormField)</li>
 * </ul>
 */
public class ReactFormLayoutControl extends ReactCompositeControl {

	private static final String REACT_MODULE = "TLFormLayout";

	/** State key holding the maximum number of columns the fields are laid out in. */
	public static final String MAX_COLUMNS = "maxColumns";

	/**
	 * State key holding the {@link LabelPosition#getExternalName() name} of the
	 * {@link LabelPosition} the fields take.
	 */
	public static final String LABEL_POSITION = "labelPosition";

	/** State key telling whether the fields are displayed but cannot be changed. */
	public static final String READ_ONLY = "readOnly";

	/**
	 * Creates a form layout with full configuration.
	 *
	 * @param maxColumns
	 *        Maximum number of columns (e.g. 3).
	 * @param labelPosition
	 *        {@link LabelPosition#SIDE}, {@link LabelPosition#TOP}, or {@link LabelPosition#AUTO}.
	 * @param readOnly
	 *        Whether the form is read-only.
	 * @param children
	 *        The child controls ({@code TLFormGroup} or {@code TLFormField}).
	 */
	public ReactFormLayoutControl(ReactContext context, int maxColumns, LabelPosition labelPosition, boolean readOnly,
			List<? extends ReactControl> children) {
		super(context, null, REACT_MODULE, children);
		putState(MAX_COLUMNS, Integer.valueOf(maxColumns));
		putState(LABEL_POSITION, labelPosition.getExternalName());
		putState(READ_ONLY, readOnly);
	}

	/**
	 * Creates a form layout with default settings (3 columns, auto labels, editable).
	 *
	 * @param children
	 *        The child controls.
	 */
	public ReactFormLayoutControl(ReactContext context, List<? extends ReactControl> children) {
		this(context, 3, LabelPosition.AUTO, false, children);
	}

	/**
	 * Creates a form layout with default settings and no initial children.
	 *
	 * <p>
	 * Children can be added later via {@link #addChild(ReactControl)}.
	 * </p>
	 */
	public ReactFormLayoutControl(ReactContext context) {
		this(context, 3, LabelPosition.AUTO, false, List.of());
	}

	/**
	 * Updates the read-only state.
	 *
	 * @param readOnly
	 *        Whether the form is read-only.
	 */
	public void setReadOnly(boolean readOnly) {
		putState(READ_ONLY, readOnly);
	}

}
