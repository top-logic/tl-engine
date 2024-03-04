/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.tag.AbstractFormMemberControlTag;
import com.top_logic.layout.form.tag.AbstractTag;
import com.top_logic.layout.form.tag.ControlTagUtil;
import com.top_logic.layout.form.tag.FormTag;
import com.top_logic.layout.form.tag.LabelTag;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.model.annotate.LabelPosition;
import com.top_logic.model.form.definition.LabelPlacement;

/**
 * {@link AbstractTag} for creating a label/description cell for a single {@link FormMember}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class InputCellTag extends AbstractFormMemberControlTag {

	/**
	 * XML name of this tag.
	 */
	public static final String INPUT_CELL_TAG = "form:inputCell";

	private ControlProvider _controlProvider;

	private String _name;

	private boolean _errorAsText = false;
	
	private boolean _labelFirst = true;

	private Boolean _labelAbove;

	private Boolean _keepInline;

	private boolean _colon = Icons.COLON.get();

	private String _cssClass;

	private String _cssStyle;

	private String _width;

	private String _firstColumnWidth;

	/**
	 * Sets the {@link ControlProvider} for displaying the form field.
	 */
	public void setControlProvider(ControlProvider controlProvider) {
		_controlProvider = controlProvider;
	}

	/**
	 * @see LabelTag#setColon(boolean)
	 */
	public void setColon(boolean colon) {
		_colon = colon;
	}

	/**
	 * Whether a validation error should not be displayed as icon.
	 */
	public void setErrorAsText(boolean errorAsText) {
		_errorAsText = errorAsText;
	}

	/**
	 * Sets the CSS class.
	 * 
	 * @param css
	 *        The CSS class name.
	 */
	public void setCssClass(String css) {
		_cssClass = css;
	}

	/**
	 * Sets the CSS style.
	 * 
	 * @param cssStyle
	 *        The CSS style attribute and value.
	 */
	public void setCssStyle(String cssStyle) {
		_cssStyle = cssStyle;
	}

	/**
	 * Sets the CSS width of this cell.
	 * 
	 * @param widthSpec
	 *        CSS width of this cell.
	 */
	public void setWidth(String widthSpec) {
		_width = widthSpec;
	}

	/**
	 * The CSS width of the first column of the cell. By default its the cell for the label.
	 * 
	 * @param firstColumnWidth
	 *        CSS width
	 */
	public void setFirstColumnWidth(String firstColumnWidth) {
		_firstColumnWidth = firstColumnWidth;
	}

	/**
	 * Sets whether the label is rendered above the input.
	 * 
	 * @param labelAbove
	 *        Label above the input.
	 */
	public void setLabelAbove(boolean labelAbove) {
		_labelAbove = labelAbove;
	}

	/**
	 * Sets whether the label is rendered before the input or behind it.
	 * 
	 * @param labelFirst
	 *        Whether the label is rendered first.
	 */
	public void setLabelFirst(boolean labelFirst) {
		_labelFirst = labelFirst;
		if (!labelFirst) {
			_keepInline = true;
		}
	}

	/**
	 * Returns the {@link FormMember} name of the field to display.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns whether an input error should be rendered as text instead of icon with tooltip.
	 * 
	 * @return If error is rendered as text.
	 */
	public boolean getErrorAsText() {
		return _errorAsText;
	}

	/**
	 * Returns whether the label is rendered before the input or behind it.
	 * 
	 * @return Whether the label is rendered first.
	 */
	public boolean getLabelFirst() {
		return _labelFirst;
	}

	/**
	 * Returns if there is rendered a colon after the label.
	 * 
	 * @return True if there is rendered a colon.
	 */
	public boolean getColon() {
		return _colon;
	}

	private DescriptionContainerTag getDescriptionContainerParent() {
		Tag parent = getParent();

		if (parent instanceof DescriptionContainerTag) {
			return (DescriptionContainerTag) parent;
		} else {
			return null;
		}
	}

	private boolean getLabelAbove() {
		Boolean labelAbove = null;

		GroupCellTag groupCellParent = getGroupCellParent();
		if (_labelAbove != null) {
			labelAbove = _labelAbove;
		} else {
			if (groupCellParent != null) {
				labelAbove = groupCellParent.getLabelAbove();
			}

			if (labelAbove == null) {
				FormTag formParent = getFormParent();
				if (formParent != null) {
					labelAbove = formParent.getLabelAbove();
				}

				if (labelAbove == null) {
					labelAbove = Icons.LABEL_ABOVE.get();
				}
			}
		}

		return labelAbove.booleanValue();
	}

	private String getFirstColumnWidth() {
		if (_firstColumnWidth != null) {
			return _firstColumnWidth;
		} else {
			DescriptionContainerTag descriptionParent = getDescriptionContainerParent();
			if (descriptionParent != null) {
				return descriptionParent.getFirstColumnWidth();
			} else {
				GroupCellTag groupCellParent = getGroupCellParent();
				if (groupCellParent != null) {
					return groupCellParent.getFirstColumnWidth();
				} else {
					return null;
				}
			}
		}
	}

	private GroupCellTag getGroupCellParent() {
		Tag ancestor = getParent();

		while (ancestor != null) {
			if (ancestor instanceof GroupCellTag) {
				return (GroupCellTag) ancestor;
			}

			if (ancestor instanceof FormTag) {
				// Other FormTag that is not a GroupCellTag, stop search since the current tag must
				// be rendered in textual context mode.
				return null;
			}

			ancestor = ancestor.getParent();
		}

		// No container found.
		return null;
	}

	private FormTag getFormParent() {
		Tag ancestor = getParent();

		while (ancestor != null) {
			if (ancestor instanceof FormTag) {
				return (FormTag) ancestor;
			}

			ancestor = ancestor.getParent();
		}

		// No container found.
		return null;
	}

	/**
	 * The JSP local name of this tag handler for error reporting.
	 */
	protected String getTagName() {
		return INPUT_CELL_TAG;
	}

	@Override
	protected int startFormMember() throws IOException, JspException {
		DescriptionCellControl control = createControl(getMember(), null);

		try {
			ControlTagUtil.writeControl(getOut(), this, pageContext, control);
		} catch (IOException ex) {
			throw new JspException(ex);
		}

		return EVAL_BODY_INCLUDE;
	}

	@Override
	protected int endFormMember() throws IOException, JspException {
		return EVAL_PAGE;
	}

	@Override
	public DescriptionCellControl createControl(FormMember member, String displayStyle) {
		DescriptionCellControl result = DescriptionCellControl.createInputBox(member, _controlProvider, displayStyle, _colon, _errorAsText);
		result.setCellClass(_cssClass);
		result.setLabelWidth(getFirstColumnWidth());
		if (getLabelAbove()) {
			result.setLabelPlacement(LabelPlacement.ABOVE);
		}
		if (_keepInline != null) {
			result.setLabelPlacement(LabelPlacement.INLINE);
		}
		result.setLabelPosition(getLabelFirst() ? LabelPosition.DEFAULT : LabelPosition.AFTER_VALUE);
		result.setCellStyle(_cssStyle);
		result.setCellWidth(_width);

		return result;
	}
}