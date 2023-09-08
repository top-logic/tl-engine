/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.boxes.reactive_tag.DescriptionCellTag;
import com.top_logic.layout.form.boxes.reactive_tag.DescriptionTag;
import com.top_logic.layout.form.boxes.reactive_tag.GroupCellTag;
import com.top_logic.layout.form.tag.FormTag;
import com.top_logic.layout.form.tag.util.BooleanAttribute;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Short-cut for rendering <code>meta:label</code> and <code>meta:attribute</code> within a
 * <code>form:descriptionCell</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MetaInputBoxTag extends GroupedMetaInputTag {

	private final DescriptionCellTag _cell = new DescriptionCellTag();

	private final DescriptionTag _description = new DescriptionTag();

	private final GroupedMetaLabelTag _label = new GroupedMetaLabelTag();

	private final GroupedMetaErrorTag _error = new GroupedMetaErrorTag();

	private boolean _colon = com.top_logic.layout.form.boxes.reactive_tag.Icons.COLON.get();

	private Boolean _labelAbove;

	private BooleanAttribute _wholeLine = new BooleanAttribute();

	private String _firstColumnWidth;

	private int _inputSize = -1;

	private Boolean _splitControls;

	/**
	 * Creates a {@link MetaInputBoxTag}.
	 */
	public MetaInputBoxTag() {
		super();

		_description.setParent(_cell);
		_label.setParent(_description);
		_error.setParent(_description);
	}

	@Override
	public void setParent(Tag parent) {
		// Fake that the cell tag is the real parent.
		_cell.setParent(parent);
		super.setParent(_cell);
	}

	@Override
	public void setPageContext(PageContext pageContext) {
		super.setPageContext(pageContext);
		_label.setPageContext(pageContext);
		_error.setPageContext(pageContext);
	}

	@Override
	public void setAttributed(Wrapper anAttributed) {
		super.setAttributed(anAttributed);
		_label.setAttributed(anAttributed);
		_error.setAttributed(anAttributed);
	}

	@Override
	public void setAttributeUpdate(AttributeUpdate anAttributeUpdate) {
		super.setAttributeUpdate(anAttributeUpdate);
		_label.setAttributeUpdate(anAttributeUpdate);
		_error.setAttributeUpdate(anAttributeUpdate);
	}

	@Override
	public void setMetaAttribute(TLStructuredTypePart aMetaAttribute) {
		super.setMetaAttribute(aMetaAttribute);
		_label.setMetaAttribute(aMetaAttribute);
		_error.setMetaAttribute(aMetaAttribute);
	}

	@Override
	public void setName(String aName) {
		super.setName(aName);
		_label.setName(aName);
		_error.setName(aName);
	}

	@Override
	public void setDomain(String aDomain) {
		super.setDomain(aDomain);
		_label.setDomain(aDomain);
		_error.setDomain(aDomain);
	}

	@Override
	public void setPart(String aPart) {
		super.setPart(aPart);
		_label.setPart(aPart);
		_error.setPart(aPart);
	}

	/**
	 * @see MetaLabelTag#setStyle(String)
	 */
	public void setLabelStyle(String style) {
		_label.setStyle(style);
	}

	/**
	 * @see MetaLabelTag#setColon(boolean)
	 */
	public void setColon(boolean colon) {
		_colon = colon;
	}

	/**
	 * @see DescriptionCellTag#setLabelAbove(boolean)
	 */
	public void setLabelAbove(boolean labelAbove) {
		_labelAbove = labelAbove;
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
				labelAbove = com.top_logic.layout.form.boxes.reactive_tag.Icons.LABEL_ABOVE.get();
			}
		}

		return labelAbove.booleanValue();
	}

	/**
	 * @see DescriptionCellTag#setFirstColumnWidth(String)
	 */
	public void setFirstColumnWidth(String firstColumnWidth) {
		_firstColumnWidth = firstColumnWidth;
	}

	private String getFirstColumnWidth() {
		String firstColumnWidth = null;

		GroupCellTag groupCellParent = getGroupCellParent();
		if (_firstColumnWidth != null) {
			firstColumnWidth = _firstColumnWidth;
		} else {
			if (groupCellParent != null) {
				firstColumnWidth = groupCellParent.getFirstColumnWidth();
			}
		}

		return firstColumnWidth;
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

	/**
	 * @see DescriptionCellTag#setWholeLine(boolean)
	 */
	public void setWholeLine(boolean wholeLine) {
		_wholeLine.setAsBoolean(wholeLine);
	}

	/**
	 * * @see DescriptionCellTag#setSplitControls(boolean)
	 */
	public void setSplitControls(boolean splitControls) {
		_splitControls = splitControls;
	}

	@Override
	public void setInputSize(int inputSize) {
		super.setInputSize(inputSize);
		_inputSize = inputSize;
	}

	@Override
	public int doStartTag() throws JspException {
		_label.setColon(_colon);
		if (_wholeLine.isSet()) {
			_cell.setWholeLine(_wholeLine.get());
		} else {
			_cell.setWholeLine(AttributeOperations.renderWholeLine(getMetaAttribute(), getAttributeUpdate()));
		}
		if (_labelAbove != null) {
			_cell.setLabelAbove(_labelAbove);
		}
		_cell.setFirstColumnWidth(getFirstColumnWidth());
		_cell.setFirstColumnWidth(getFirstColumnWidth());
		if (_inputSize != -1) {
			_cell.setCssClass("sizeSet");
		}
		if (_splitControls != null) {
			_cell.setSplitControls(_splitControls);
		}

		int cellStart = start(_cell);

		int descriptionStart = start(_description);
		int labelStart = start(_label);
		stop(labelStart, _label);

		int labelError = start(_error);
		stop(labelError, _error);
		stop(descriptionStart, _description);

		super.doStartTag();
		super.doEndTag();

		stop(cellStart, _cell);

		return SKIP_BODY;
	}

	private int start(BodyTag tag) throws JspException {
		tag.setPageContext(getPageContext());
		int startResult = tag.doStartTag();

		if (startResult == BodyTag.EVAL_BODY_BUFFERED) {
			tag.setBodyContent(getPageContext().pushBody());
			tag.doInitBody();
		}

		return startResult;
	}

	private void stop(int startResult, BodyTag tag) throws JspException {
		if (startResult == BodyTag.EVAL_BODY_BUFFERED) {
			tag.doAfterBody();
			getPageContext().popBody();
			tag.doEndTag();
		}
	}

	@Override
	protected void teardown() {
		super.teardown();
		_inputSize = -1;
		_wholeLine.reset();
	}

}
