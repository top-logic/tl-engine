/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactContext;
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
 * <li>{@code maxColumns} - maximum number of columns</li>
 * <li>{@code labelPosition} - "side", "top", or "auto"</li>
 * <li>{@code readOnly} - whether the form is read-only</li>
 * <li>{@code children} - child controls (TLFormGroup or TLFormField)</li>
 * </ul>
 */
public class ReactFormLayoutControl extends ReactControl {

	private static final String REACT_MODULE = "TLFormLayout";

	private static final String MAX_COLUMNS = "maxColumns";

	private static final String LABEL_POSITION = "labelPosition";

	private static final String READ_ONLY = "readOnly";

	private static final String CHILDREN = "children";

	private final List<ReactControl> _children;

	/**
	 * Creates a form layout with full configuration.
	 *
	 * @param maxColumns
	 *        Maximum number of columns (e.g. 3).
	 * @param labelPosition
	 *        "side", "top", or "auto".
	 * @param readOnly
	 *        Whether the form is read-only.
	 * @param children
	 *        The child controls ({@code TLFormGroup} or {@code TLFormField}).
	 */
	public ReactFormLayoutControl(ReactContext context, int maxColumns, String labelPosition, boolean readOnly,
			List<? extends ReactControl> children) {
		super(context, null, REACT_MODULE);
		_children = new ArrayList<>(children);
		putState(MAX_COLUMNS, Integer.valueOf(maxColumns));
		putState(LABEL_POSITION, labelPosition);
		putState(READ_ONLY, readOnly);
		putState(CHILDREN, _children);
	}

	/**
	 * Creates a form layout with default settings (3 columns, auto labels, editable).
	 *
	 * @param children
	 *        The child controls.
	 */
	public ReactFormLayoutControl(ReactContext context, List<? extends ReactControl> children) {
		this(context, 3, "auto", false, children);
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

	@Override
	protected void cleanupChildren() {
		for (ReactControl child : _children) {
			child.cleanupTree();
		}
	}

}
