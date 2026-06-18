/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.LabelPosition;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;

/**
 * Builds an ad-hoc form that renders through the same responsive layout and field chrome as a
 * model-bound form.
 *
 * <p>
 * Each field is a label plus an input control wrapped in {@link ReactFormFieldChromeControl}, and
 * all fields are placed in a {@link ReactFormLayoutControl}. Labels therefore switch between side
 * and top with the available width exactly like an attribute form, and the field chrome is
 * identical. The defaults mirror {@link ReactFormLayoutControl} (three columns, automatic label
 * position, editable).
 * </p>
 *
 * <p>
 * Use for programmatic forms that are not bound to a model object (e.g. the all-languages
 * {@link I18NStringDialog}). Attribute forms keep their attribute-aware field controls, but both
 * render through the same {@code TLFormLayout}/{@code TLFormField} pipeline.
 * </p>
 */
public final class ReactFormBuilder {

	private final ReactContext _context;

	private final List<ReactControl> _fields = new ArrayList<>();

	private int _maxColumns = 3;

	private LabelPosition _labelPosition = LabelPosition.AUTO;

	private boolean _readOnly = false;

	/**
	 * Creates a builder for a form in the given context.
	 */
	public ReactFormBuilder(ReactContext context) {
		_context = context;
	}

	/**
	 * Sets the maximum number of columns (default three).
	 */
	public ReactFormBuilder columns(int maxColumns) {
		_maxColumns = maxColumns;
		return this;
	}

	/**
	 * Sets the label position (default {@link LabelPosition#AUTO}).
	 */
	public ReactFormBuilder labelPosition(LabelPosition labelPosition) {
		_labelPosition = labelPosition;
		return this;
	}

	/**
	 * Sets whether the form renders read-only (default editable).
	 */
	public ReactFormBuilder readOnly(boolean readOnly) {
		_readOnly = readOnly;
		return this;
	}

	/**
	 * Adds a labelled field wrapping the given input control in field chrome.
	 */
	public ReactFormBuilder addField(String label, ReactControl input) {
		_fields.add(new ReactFormFieldChromeControl(_context, label, input));
		return this;
	}

	/**
	 * Combines an input control with a trailing fixed-size adornment (e.g. an icon button) into a
	 * single row in which the input fills the remaining width and the adornment stays inline at its
	 * natural size.
	 */
	public static ReactControl inputWithAdornment(ReactContext context, ReactControl input, ReactControl adornment) {
		ReactStackControl row = new ReactStackControl(context, StackDirection.ROW, StackGap.COMPACT,
			StackAlign.CENTER, false, List.of(input, adornment));
		row.setGrowFirst(true);
		return row;
	}

	/**
	 * Builds the {@link ReactFormLayoutControl} holding the added fields.
	 */
	public ReactFormLayoutControl build() {
		return new ReactFormLayoutControl(_context, _maxColumns, _labelPosition, _readOnly, _fields);
	}

}
