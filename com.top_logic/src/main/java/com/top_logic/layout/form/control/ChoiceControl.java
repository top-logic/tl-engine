/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.model.utility.OptionModelListener;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * {@link Control} displaying a {@link SelectField} as collection of radio buttons or checkboxes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ChoiceControl extends AbstractSelectControl implements OptionModelListener {

	private static final Map<String, ControlCommand> CHOICE_COMMANDS = createCommandMap(
		AbstractFormFieldControl.COMMANDS,
		new ControlCommand[] {
		});

	/**
	 * Default orientation, if nothing was set explicit.
	 */
	private static final Orientation DEFAULT_ORIENTATION = null;
	
	private Orientation orientation = DEFAULT_ORIENTATION;
	
	private boolean labelsLeft;
	private boolean indent;

	private OptionModel<?> _optionModel;

	/**
	 * Creates a new {@link ChoiceControl}.
	 * 
	 * @param model
	 *        The model this {@link ChoiceControl} displays.
	 */
	public ChoiceControl(SelectField model) {
		super(model, CHOICE_COMMANDS);
	}
	
	@Override
	protected String getTypeCssClass() {
		return "cChoice";
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);

		out.append(getOrientation() == Orientation.HORIZONTAL ? "cChoice-horizontal" : "cChoice-vertical");
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		SelectField field = getSelectField();

		initOptionModel(field);

		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();

		Set<?> selection = field.getSelectionSet();
		List<?> options = getOptionsListSorted(field);
		int optionsCnt = options.size();
		for (int optionIndex = 0; optionIndex < optionsCnt; optionIndex++) {
			Object option = options.get(optionIndex);
			String optionName = "o" + optionIndex;
			String widgetID = getID() + "-" + optionName;

			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "cChoice-option");
			out.endBeginTag();
			{
				if (labelsLeft) {
					out.beginBeginTag(SPAN);
					out.writeAttribute(CLASS_ATTR, "cChoice-labelLeft");
					out.endBeginTag();
					{
						writeLabel(context, out, field, option, widgetID);
					}
					out.endTag(SPAN);
				}

				out.beginBeginTag(INPUT);
				out.writeAttribute(ID_ATTR, widgetID);
				out.writeAttribute(TYPE_ATTR, field.isMultiple() ? CHECKBOX_TYPE_VALUE : RADIO_TYPE_VALUE);

				if (field.isMultiple()) {
					out.writeAttribute(CLASS_ATTR, FormConstants.IS_CHECKBOX_CSS_CLASS);
				} else {
				    out.writeAttribute(CLASS_ATTR, FormConstants.IS_RADIO_CSS_CLASS);
				}

				// Make checkboxes of the same choice group exclusively
				// selectable by assigning them the same name.
				writeQualifiedNameAttribute(out);

				out.writeAttribute(VALUE_ATTR, field.getOptionID(option));
				writeOnClick(out, FormConstants.CHOICE_CONTROL_CLASS, this, null);
				if (selection.contains(option)) {
					out.writeAttribute(CHECKED_ATTR, CHECKED_CHECKED_VALUE);
				}
				if (field.isDisabled() || this.isFixed(field, option)) {
					out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
				}
				if (hasTabIndex()) {
					out.writeAttribute(TABINDEX_ATTR, getTabIndex() + optionIndex);
				}
				out.endEmptyTag();

				if (! labelsLeft) {
                    if (indent) {
						out.beginBeginTag(SPAN);
						out.writeAttribute(CLASS_ATTR, "cChoice-labelRight");
						out.endBeginTag();
						{
							writeLabel(context, out, field, option, widgetID);
						}
						out.endTag(SPAN);
                    } else {
						writeLabel(context, out, field, option, widgetID);
                    }
				}
			}
			out.endTag(DIV);
		}
		out.endTag(DIV);
	}

	@Override
	protected void detachInvalidated() {
		super.detachInvalidated();

		resetOptionModel();
	}

	private void initOptionModel(SelectField field) {
		resetOptionModel();

		_optionModel = SelectFieldUtils.getOptionModel(field);
		_optionModel.addOptionListener(this);
	}

	@Override
	public void notifyOptionsChanged(OptionModel<?> sender) {
		resetOptionModel();
		requestRepaint();
	}

	private void resetOptionModel() {
		if (_optionModel != null) {
			_optionModel.removeOptionListener(this);
		}
	}

	private boolean isFixed(SelectField field, Object option) {
        Filter theFixedOptions = field.getFixedOptions();
        if (theFixedOptions != null) {
            return theFixedOptions.accept(option);
        } else {
            return false;
        }
    }

	private Orientation getOrientation() {
		return (orientation == DEFAULT_ORIENTATION)
			? (((SelectField) getFieldModel()).isMultiple() ? Orientation.VERTICAL : Orientation.HORIZONTAL)
			: orientation;
	}

	/**
	 * Sets the orientation for this {@link ChoiceControl}.
	 * 
	 * @see #resetOrientation()
	 */
	public void setOrientation(Orientation newOrientation) {
		boolean change = newOrientation != this.orientation;
		this.orientation = newOrientation;
		if (change) requestRepaint();
	}

	/**
	 * Resets the orientation to default orientation (based on displayed field).
	 * 
	 * @see #setOrientation(Orientation)
	 */
	public void resetOrientation() {
		boolean change = this.orientation != DEFAULT_ORIENTATION;
		this.orientation = DEFAULT_ORIENTATION;
		if (change) requestRepaint();
	}

	/**
	 * Whether the labels should be written left from the choice boxes.
	 * 
	 * @param labelsLeft
	 *        if <code>true</code> the labels are written left from the boxes, otherwise right.
	 */
	public void setLabelsLeft(boolean labelsLeft) {
		boolean change = labelsLeft != this.labelsLeft;
		this.labelsLeft = labelsLeft;
		if (change) requestRepaint();
	}

	/**
	 * Whether some space should be written between the label and the corresponding choice box.
	 */
	public void setIndent(boolean newIndent) {
		boolean change = newIndent != this.indent;
		this.indent = newIndent;
		if (change) requestRepaint();
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		SelectField field = (SelectField) getModel();

		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
				SelectFieldUtils.writeSelectionAsTextImmutable(out, field);
		}
		out.endTag(DIV);
	}

	private void writeLabel(DisplayContext context, TagWriter out, SelectField field, Object option, String widgetID)
			throws IOException {
		out.beginBeginTag(LABEL);
		out.writeAttribute(FOR_ATTR, widgetID);
		LabelProvider theProvider = field.getOptionLabelProvider();
		if (theProvider instanceof ResourceProvider) {
		    String theTool = ((ResourceProvider) theProvider).getTooltip(option);
		    if (! StringServices.isEmpty(theTool)) {
				OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, theTool);
		    }	    
		}
		if (this.isFixed(field, option) || field.isDisabled()) {
		    out.writeAttribute(CLASS_ATTR, "disabled");
		}
		out.endBeginTag();
		{
			out.writeText(field.getOptionLabel(option));
		}
		out.endTag(LABEL);
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		requestRepaint();
	}

	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		// Could be optimized.
		requestRepaint();
	}

	private List<?> getOptionsListSorted(SelectField field) {
		return SelectFieldUtils.getOptionAndSelectionOuterJoinOrdered(field);
	}

	private List<?> getSelectionListOrdered(SelectField field) {
		return SelectFieldUtils.getSelectionListSorted(field);
	}
}
