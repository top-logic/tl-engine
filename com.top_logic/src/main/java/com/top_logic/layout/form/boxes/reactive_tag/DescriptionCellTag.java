/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.boxes.tag.BoxContentTag;
import com.top_logic.layout.form.boxes.tag.JSPLayoutedControls;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.control.IconSelectControl;
import com.top_logic.layout.form.control.LabelControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.tag.ControlTagUtil;
import com.top_logic.layout.form.tag.FormGroupTag;
import com.top_logic.layout.form.tag.FormTag;
import com.top_logic.layout.form.tag.FormTagUtil;

/**
 * Tag creating a description/content cell for reactive forms.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class DescriptionCellTag extends AbstractBodyTag implements BoxContentTag {

	private String _memberName;

	private JSPLayoutedControls _contents;

	private String _cssClass;

	private String _style;

	private String _width;

	private String _firstColumnWidth;

	private Boolean _labelAbove;

	private boolean _keepInline = false;

	private boolean _wholeLine = false;

	private boolean _labelFirst = true;

	private boolean _splitControls = false;

	private JSPLayoutedControls _description;

	private String _firstColumnStyle;

	private String _firstColumnCssClass;

	/**
	 * XML name of this tag.
	 */
	public static final String DESCRIPTION_CELL_TAG = "form:descriptionCell";

	/**
	 * The name of the {@link FormMember} to listen to the visibility. If nothing is set, this cell
	 * is always visible.
	 * 
	 * @param name
	 *        The name of the {@link FormMember}.
	 */
	public void setFormMemberName(String name) {
		_memberName = name;
	}

	@Override
	public void setColumns(int columns) {
		// ignore
	}

	@Override
	public void setRows(int rows) {
		// ignore
	}

	@Override
	public void setCssClass(String cssClass) {
		_cssClass = cssClass;
	}

	@Override
	public void setStyle(String style) {
		_style = style;
	}

	@Override
	public void setWidth(String widthSpec) {
		_width = widthSpec;
	}

	/**
	 * Returns the CSS width of this cell.
	 * 
	 * @return The CSS width.
	 */
	public String getWidth() {
		return _width;
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

	private String getFirstColumnWidth() {
		if (_firstColumnWidth != null) {
			return _firstColumnWidth;
		} else {
			GroupCellTag groupCellParent = getGroupCellParent();
			if (groupCellParent != null) {
				return groupCellParent.getFirstColumnWidth();
			}
		}
		return null;
	}

	/**
	 * Sets whether the label is rendered above the input-field or before.
	 * 
	 * @param labelAbove
	 *        Label is rendered above.
	 */
	public void setLabelAbove(boolean labelAbove) {
		_labelAbove = labelAbove;
		if (!labelAbove) {
			_keepInline = true;
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
			}

			if (labelAbove == null) {
				labelAbove = Icons.LABEL_ABOVE.get();
			}
		}

		return labelAbove.booleanValue();
	}

	/**
	 * Sets whether the {@link DescriptionCellTag} is rendered over the whole line.
	 * 
	 * @param wholeLine
	 *        {@link DescriptionCellTag} is rendered over the whole line.
	 */
	public void setWholeLine(boolean wholeLine) {
		_wholeLine = wholeLine;
	}

	/**
	 * Sets whether the label is rendered before the input or behind it.
	 * 
	 * @param labelFirst
	 *        If <code>false</code> the input is rendered before the label.
	 * @see #getLabelFirst()
	 */
	public void setLabelFirst(boolean labelFirst) {
		_labelFirst = labelFirst;
	}

	/**
	 * Returns whether the label is rendered before the input or behind it.
	 * 
	 * @see DescriptionCellTag#setLabelFirst(boolean)
	 */
	public boolean getLabelFirst() {
		return (_labelFirst || _splitControls);
	}

	/**
	 * Sets whether label and input are always rendered in one line.
	 */
	public void setKeepInline(boolean keepInline) {
		_keepInline = keepInline;
	}

	/**
	 * Returns whether label and input are always rendered in one line.
	 */
	public boolean getKeepInline() {
		return _keepInline || (!_labelFirst && _splitControls);
	}

	/**
	 * Whether the controls which belong to the descriptionCell are splitted instead of rendered one
	 * after each other.
	 * 
	 * @param splitControls
	 *        If <code>true</code> the controls are splitted.
	 */
	public void setSplitControls(boolean splitControls) {
		_splitControls = splitControls;
	}

	private FormContainer getParentFormContainer(FormTag formTag) {
		if (formTag != null) {
			return FormTagUtil.findParentFormContainer(this);
		} else {
			return null;
		}
	}

	private FormMember getMemberByRelativeName(FormContainer formContainer) {
		if (formContainer != null) {
			return FormContext.getMemberByRelativeName(formContainer, _memberName);
		} else {
			return null;
		}
	}

	private FormMember getFormMember() {
		if(_memberName != null) {
			FormTag formTag = FormTagUtil.findFormTag(this);
			FormContainer formContainer = getParentFormContainer(formTag);
			return getMemberByRelativeName(formContainer);
		} 
		
		return null;
	}

	@Override
	protected void setup() {
		super.setup();
		_contents = new JSPLayoutedControls();
	}

	@Override
	protected void tearDown() {
		_contents = null;
		_labelAbove = null;
		super.tearDown();
	}

	@Override
	protected String getTagName() {
		return DESCRIPTION_CELL_TAG;
	}

	/**
	 * Returns if a colon is rendered after the label. It is rendered if the label is rendered first
	 * and the theme says it is rendered.
	 * 
	 * @return Whether a colon is rendered or not.
	 */
	public boolean getColon() {
		return (getLabelFirst() && Icons.COLON.get());
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
	 * Sets the description of this cell. It is rendered like a label.
	 * 
	 * @param description
	 *        The description of this cell.
	 */
	public void setDescription(JSPLayoutedControls description) {
		_description = description;
	}

	/**
	 * Sets the CSS style of the description.
	 * 
	 * @param cssStyle
	 *        The CSS style.
	 */
	public void setFirstColumnStyle(String cssStyle) {
		_firstColumnStyle = cssStyle;
	}

	/**
	 * Sets the CSS class of the description.
	 * 
	 * @param cssClass
	 *        The CSS class.
	 */
	public void setFirstColumnCssClass(String cssClass) {
		_firstColumnCssClass = cssClass;
	}

	@Override
	public String addControl(HTMLFragment childControl) {
		return _contents.addControl(childControl);
	}

	@Override
	public int doAfterBody() throws JspException {
		int doAfterBody = super.doAfterBody();
		if (getBodyContent() != null) {

			String contentPattern = getBodyContent().getString();
			_contents.setContentPattern(contentPattern);
		}

		return doAfterBody;
	}

	@Override
	public int doEndTag() throws JspException {
		boolean descriptionFirst = getLabelFirst();

		boolean errorIsRendered = false;
		if (_description != null) {
			for (HTMLFragment control : _description.getControls()) {
				if (control instanceof ErrorControl) {
					errorIsRendered = true;
					break;
				}
			}
		}

		Boolean labelFirst = null;
		boolean forceWholeLine = false;
		List<HTMLFragment> filteredControls = new ArrayList<>();
		List<HTMLFragment> controls = _contents.getControls();
		String contentPattern = _contents.getContentPattern();
		String[] placedControls = contentPattern.split(Pattern.quote(FormGroupTag.PLACEHOLDER), -1);
		String contentPatternNew = StringServices.EMPTY_STRING;
		HTMLFragment firstControl = null;
		for (int i = 0; i < controls.size(); i++) {
			contentPatternNew += placedControls[i];
			if (i == 0 && controls.get(0) instanceof IconSelectControl && _splitControls) {
				firstControl = controls.get(0);
			} else {
				if (!(controls.get(i) instanceof ErrorControl && errorIsRendered)) {
					// error is already rendered in description
					filteredControls.add(controls.get(i));
					contentPatternNew += FormGroupTag.PLACEHOLDER;
					if (labelFirst == null) {
						labelFirst = DescriptionCellControl.controlLabelFirst(controls.get(i));
					} else {
						labelFirst = labelFirst || DescriptionCellControl.controlLabelFirst(controls.get(i));
					}
					forceWholeLine = forceWholeLine || DescriptionCellControl.controlWholeLine(controls.get(i));
				}
			}
		}

		if (placedControls.length > 0) {
			contentPatternNew += placedControls[placedControls.length - 1];
		}

		HTMLFragment beforeContent = null;
		if (!descriptionFirst) {
			if (firstControl != null && _splitControls) {
				beforeContent = firstControl;
			}
		}
		JSPLayoutedControls filteredContents = new JSPLayoutedControls();
		filteredContents.setContentPattern(contentPatternNew);
		for (HTMLFragment control : filteredControls) {
			filteredContents.addControl(control);
		}

		HTMLFragment finalContents =
			beforeContent == null ? filteredContents : Fragments.concat(beforeContent, filteredContents);

		if (_description != null) {
			for (HTMLFragment control : _description.getControls()) {
				if (control instanceof LabelControl) {
					((LabelControl) control).setColon(descriptionFirst);
				}
			}
		}

		HTMLFragment beforeDescription = null;
		if (descriptionFirst) {
			if (firstControl != null && _splitControls) {
				beforeDescription = firstControl;
			}
		}

		HTMLFragment finalDescription;
		if (beforeDescription == null) {
			finalDescription = _description;
		} else {
			finalDescription = Fragments.concat(beforeDescription, _description);
		}

		DescriptionCellControl descriptionCellControl = new DescriptionCellControl(getFormMember(), finalContents);

		descriptionCellControl.setCellClass(_cssClass);
		descriptionCellControl.setCellStyle(_style);
		descriptionCellControl.setCellWidth(_width);
		descriptionCellControl.setLabelAbove(getLabelAbove());
		descriptionCellControl.setKeepInline(getKeepInline());
		descriptionCellControl.setWholeLine(_wholeLine || forceWholeLine);
		descriptionCellControl.setLabelFirst(labelFirst == null || labelFirst);
		descriptionCellControl.setDescription(finalDescription);
		descriptionCellControl.setLabelWidth(getFirstColumnWidth());
		descriptionCellControl.setLabelStyle(_firstColumnStyle);
		descriptionCellControl.setLabelClass(_firstColumnCssClass);

		try {
			ControlTagUtil.writeControl(getOut(), this, pageContext, descriptionCellControl);
		} catch (IOException ex) {
			throw new JspException(ex);
		}

		return super.doEndTag();
	}
}
