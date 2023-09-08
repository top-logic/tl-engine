/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import javax.servlet.jsp.tagext.Tag;

import com.top_logic.layout.form.boxes.reactive_tag.InputCellTag;

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

	private Boolean _labelFirst;

	private Boolean _labelAbove;

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
		if (_labelFirst != null) {
			tag.setLabelFirst(_labelFirst);
		}
		if (_labelAbove != null) {
			tag.setLabelAbove(_labelAbove);
		}

		return tag;
	}

	@Override
	protected void teardown() {
		super.teardown();
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
	 * @see InputCellTag#setLabelFirst(boolean)
	 */
	public void setLabelFirst(boolean labelFirst) {
		_labelFirst = labelFirst;
	}

	/**
	 * @see InputCellTag#setLabelAbove(boolean)
	 */
	public void setLabelAbove(boolean labelAbove) {
		_labelAbove = labelAbove;
	}
}