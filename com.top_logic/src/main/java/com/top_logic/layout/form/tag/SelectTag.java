/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;


import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.CutLabelRenderer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.DropDownControl;
import com.top_logic.layout.form.control.SelectControl;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectionLabelProvider;
import com.top_logic.layout.form.tag.util.IntAttribute;
import com.top_logic.layout.form.tag.util.StringAttribute;

/**
 * View of a {@link SelectField} rendered as drop-down list.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectTag extends AbstractFormFieldControlTag {
	
	private static final String DEFAULT_DISPLAY_ONLY_SELECTION_SEPARATOR = ", ";

	private int maxLengthShown = -1;

	private final IntAttribute size = new IntAttribute();

	private DisplayDimension _width = null;

	/**
	 * Separator string that is used to concatenate the currently selected
	 * options in display-only mode of this view.
	 */
	public final StringAttribute separator = 
		new StringAttribute(DEFAULT_DISPLAY_ONLY_SELECTION_SEPARATOR);

	/** @see #setForceDisplayAsList(boolean) */
	private boolean _asList;

	private Renderer<Object> _selectionRenderer;

	private boolean _noEmptyLines;
	
	/**
	 * The amount of concurrently displayed options for a multi-selection view or the number of
	 * entries shown when opening the select drop-down on a single-selection.
	 */
	public void setSize(String value) {
		this.size.set(value);
	}

	/**
	 * The custom size of the select field (CSS style value).
	 */
	public void setWidth(String width) {
		initWidth(DisplayDimension.parseDimension(width));
	}

	/**
	 * Internal setter for the width attribute.
	 */
	public void initWidth(DisplayDimension value) {
		_width = value;
	}

	/**
	 * If set to <code>true</code>, no empty lines in the drop down box are rendered, i.e. if the
	 * {@link SelectField} is {@link SelectField#isMultiple()} and the size is larger than the
	 * actual number of options, only as much lines as options exists are rendered.
	 */
	public void setNoEmptyLines(boolean noEmptyLines) {
		_noEmptyLines = noEmptyLines;
	}

	/**
	 * JSP tag setter for {@link #_asList}.
	 * 
	 * @see SelectControl#forceDisplayAsList(boolean)
	 */
	public void setForceDisplayAsList(boolean asList) {
		_asList = asList;
	}

	/**
	 * JSP tag setter for {@link #separator}.
	 */
	public void setSeparator(String value) {
		this.separator.set(value);
	}

	/**
	 * Activates a special {@link #setSelectionRenderer(Renderer) selection renderer} that writes
	 * the selection as text but limits the number of characters to the given value.
	 * 
	 * <p>
	 * Note: Must not be used in combination with {@link #setSelectionRenderer(Renderer)}.
	 * </p>
	 */
	public void setMaxLengthShown(int maxLengthShown) {
		this.maxLengthShown = maxLengthShown;
	}

	/**
	 * Sets the renderer for the selection in view mode.
	 * 
	 * <p>
	 * Note: Must not be used in combination with {@link #setMaxLengthShown(int)}.
	 * </p>
	 * 
	 * @see SelectControl#setSelectionRenderer(Renderer)
	 */
	public void setSelectionRenderer(Renderer<Object> selectionRenderer) {
		_selectionRenderer = selectionRenderer;
	}

	@Override
	protected void teardown() {
		super.teardown();
		size.reset();
		_width = null;
		separator.reset();
		maxLengthShown = -1;
		_asList = false;
		_noEmptyLines = false;
		_selectionRenderer = null;
	}
	
	@Override
	public Control createControl(final FormMember member, String displayStyle) {
		final FormField field = (FormField) member;
		DropDownControl result = new DropDownControl(field);
		
		if (_selectionRenderer != null) {
			result.setSelectionRenderer(_selectionRenderer);
		} else if (maxLengthShown > 0) {
			result.setSelectionRenderer(
				new CutLabelRenderer(new SelectionLabelProvider(field, separator.get()), maxLengthShown));
		}
		return result;
	}
}
