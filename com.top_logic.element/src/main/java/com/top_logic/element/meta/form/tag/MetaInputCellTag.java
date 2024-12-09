/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.layout.form.boxes.reactive_tag.InputCellTag;
import com.top_logic.model.annotate.LabelPosition;

/**
 * Creates an {@link InputCellTag} for MetaAttributes.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class MetaInputCellTag extends AbstractMetaTag {

	private Boolean _colon;

	private Boolean _errorAsText;

	private String _cssClass;

	private String _cssStyle;

	private String _width;

	private String _firstColumnWidth;

	private LabelPosition _labelPosition;

	@Override
	protected Tag createImplementation() {
		InputCellTag tag = new InputCellTag();

		tag.setName(getFieldName());
		if (_colon != null) {
			tag.setColon(_colon);
		}
		if (_errorAsText != null) {
			tag.setErrorAsText(_errorAsText);
		}
		if (_cssClass != null) {
			tag.setCssClass(_cssClass);
		}
		if (_cssStyle != null) {
			tag.setCssStyle(_cssStyle);
		}
		if (_width != null) {
			tag.setWidth(_width);
		}
		if (_firstColumnWidth != null) {
			tag.setFirstColumnWidth(_firstColumnWidth);
		}
		if (_labelPosition != null) {
			tag.setLabelPosition(_labelPosition);
		}

		return tag;
	}

	@Override
	protected void teardown() {
		super.teardown();

		_labelPosition = null;
	}

	/**
	 * @see InputCellTag#setColon(boolean)
	 */
	public void setColon(boolean colon) {
		_colon = colon;
	}

	/**
	 * @see InputCellTag#setErrorAsText(boolean)
	 */
	public void setErrorAsText(boolean errorAsText) {
		_errorAsText = errorAsText;
	}

	/**
	 * @see InputCellTag#setCssClass(String)
	 */
	public void setCssClass(String cssClass) {
		_cssClass = cssClass;
	}

	/**
	 * @see InputCellTag#setCssStyle(String)
	 */
	public void setCssStyle(String cssStyle) {
		_cssStyle = cssStyle;
	}

	/**
	 * @see InputCellTag#setWidth(String)
	 */
	public void setWidth(String width) {
		_width = width;
	}

	/**
	 * @see InputCellTag#setFirstColumnWidth(String)
	 */
	public void setFirstColumnWidth(String firstColumnWidth) {
		_firstColumnWidth = firstColumnWidth;
	}

	/**
	 * Position of the label.
	 */
	@CalledFromJSP
	public void setLabelPosition(LabelPosition labelPosition) {
		_labelPosition = labelPosition;
	}

	/**
	 * @see #setLabelPosition(LabelPosition)
	 * @deprecated Use {@link #setLabelPosition(LabelPosition)}
	 */
	@Deprecated
	@CalledFromJSP
	public void setLabelFirst(boolean labelFirst) {
		if (!labelFirst) {
			_labelPosition = LabelPosition.AFTER_VALUE;
		}
	}

	/**
	 * @see #setLabelPosition(LabelPosition)
	 * @deprecated Use {@link #setLabelPosition(LabelPosition)}
	 */
	@Deprecated
	@CalledFromJSP
	public void setLabelAbove(boolean labelAbove) {
		_labelPosition = labelAbove ? LabelPosition.ABOVE : LabelPosition.INLINE;
	}
}