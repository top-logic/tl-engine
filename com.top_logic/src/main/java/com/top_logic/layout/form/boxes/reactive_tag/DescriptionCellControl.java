/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.CheckboxControl;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.control.IconSelectControl;
import com.top_logic.layout.form.control.LabelControl;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.model.VisibilityModel;
import com.top_logic.layout.form.model.VisibilityModel.AlwaysVisible;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.definition.LabelPlacement;

/**
 * Control to write a tag creating a description/content cell. Its visibility is controlled by a
 * {@link VisibilityModel}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class DescriptionCellControl extends AbstractControlBase implements VisibilityListener {

	/**
	 * Creates an {@link DescriptionCellControl} with an <code>errorControl</code>,
	 * <code>inputControl</code> and <code>labelControl</code> by the given {@link ControlProvider}.
	 * 
	 * @param member
	 *        The {@link FormMember} to be rendered.
	 * @param cp
	 *        The optional {@link ControlProvider} to create the input element.
	 * @return A new {@link DescriptionCellControl}.
	 */
	public static DescriptionCellControl createInputBox(FormMember member, ControlProvider cp, String inputStyle,
			boolean colon, boolean errorAsText) {
		if (cp == null) {
			cp = DefaultFormFieldControlProvider.INSTANCE;
		}
	
		Control inputControl = cp.createControl(member, inputStyle);
		boolean labelFirst = controlLabelFirst(inputControl);
		boolean wholeLine = controlWholeLine(inputControl);
	
		LabelControl labelControl =
			(LabelControl) DefaultFormFieldControlProvider.INSTANCE.createControl(member,
				labelFirst && colon ? FormTemplateConstants.STYLE_LABEL_WITH_COLON_VALUE
					: FormTemplateConstants.STYLE_LABEL_VALUE);
		Control errorControl = DefaultFormFieldControlProvider.INSTANCE.createControl(member, FormTemplateConstants.STYLE_ERROR_VALUE);
		if (errorControl instanceof ErrorControl) {
			((ErrorControl) errorControl).setIconDisplay(!errorAsText);
		}
	
		DescriptionCellControl result = new DescriptionCellControl(member, inputControl);
		result.setDescription(Fragments.concat(labelControl, errorControl));
		result.setLabelFirst(labelFirst);
		result.setWholeLine(wholeLine);

		return result;
	}

	private HTMLFragment _model;

	private VisibilityModel _visibility;

	private String _cellClass;

	private String _cellStyle;

	private String _cellWidth;

	/**
	 * Whether the cell is rendered over the whole line.
	 */
	private boolean _wholeLine;

	/**
	 * Whether the label is rendered first.
	 */
	private boolean _labelFirst = true;

	private String _labelWidth;

	private String _labelStyle;

	private String _labelClass;

	private HTMLFragment _description;

	private LabelPlacement _labelPlacement = LabelPlacement.DEFAULT;

	/**
	 * Creates a {@link DescriptionCellControl}.
	 * 
	 * @param visibility
	 *        The {@link VisibilityModel} to use. If <code>null</code> it will use an
	 *        {@link AlwaysVisible}.
	 * @param model
	 *        The content to display.
	 */
	public DescriptionCellControl(VisibilityModel visibility, HTMLFragment model) {
		if (visibility != null) {
			setVisibilityModel(visibility);
		} else {
			setVisibilityModel(AlwaysVisible.INSTANCE);
		}
		_model = model;
	}

	@Override
	public boolean isVisible() {
		return _visibility.isVisible();
	}

	/**
	 * Sets the {@link VisibilityModel} for this cell. It listens to changes.
	 * 
	 * @param visibility
	 *        The {@link VisibilityModel}.
	 */
	public void setVisibilityModel(VisibilityModel visibility) {
		_visibility = visibility;
	}

	/**
	 * Whether the input cell should span the whole form width, even if the form has a multi-column
	 * layout.
	 */
	@TemplateVariable("wholeLine")
	public boolean getWholeLine() {
		return _wholeLine;
	}

	/**
	 * @see #getWholeLine()
	 */
	public void setWholeLine(boolean wholeLine) {
		_wholeLine = wholeLine;
	}

	/**
	 * Whether the label is rendered before the input element.
	 * 
	 * <p>
	 * This is normally <code>true</code> except for certain input types such as check-boxes that
	 * are displayed before their labels.
	 * </p>
	 */
	@TemplateVariable("labelFirst")
	public boolean isLabelFirst() {
		return _labelFirst;
	}

	/**
	 * @see #isLabelFirst()
	 */
	public void setLabelFirst(boolean labelFirst) {
		_labelFirst = labelFirst;
	}

	/**
	 * Sets the definition where the the label has to be rendered.
	 */
	public void setLabelPlacement(LabelPlacement labelPlacement) {
		_labelPlacement = labelPlacement;
	}

	/**
	 * Whether the label should be rendered strictly before the input element, even if the label
	 * would otherwise wrap to a separate line due to limited space.
	 */
	@TemplateVariable("keepInline")
	public boolean getKeepInline() {
		return _labelPlacement == LabelPlacement.INLINE;
	}

	/**
	 * Space separated list of CSS classes to add to the top-level tag of the cell.
	 */
	@TemplateVariable("cellClasses")
	public String getCellClasses() {
		String css = ReactiveFormCSS.RF_INPUT_CELL_ONE_LINE;
		String labelCSS = _labelPlacement.cssClass();
		if (labelCSS != null) {
			css = css + " " + labelCSS;
		}
		if (!StringServices.isEmpty(_cellClass)) {
			css = css + " " + _cellClass;
		}
		return css;
	}

	/**
	 * @see #getCellClasses()
	 */
	public void setCellClass(String cssClass) {
		_cellClass = cssClass;
	}

	/**
	 * A custom CSS style value to add to the top-level tag of the generated content.
	 * 
	 * <p>
	 * Note: This value is only relevant for legacy JSP-generated forms.
	 * </p>
	 */
	@TemplateVariable("cellStyle")
	public String getCellStyle() {
		return _cellStyle;
	}

	/**
	 * @see #getCellStyle()
	 */
	public void setCellStyle(String cssStyle) {
		_cellStyle = cssStyle;
	}

	/**
	 * A custom CSS <code>width</code> value for the complete cell.
	 * 
	 * <p>
	 * Note: This value is only relevant for legacy JSP-generated forms.
	 * </p>
	 */
	@TemplateVariable("cellWidth")
	public String getCellWidth() {
		return _cellWidth;
	}

	/**
	 * @see #getCellWidth()
	 */
	public void setCellWidth(String width) {
		_cellWidth = width;
	}

	/**
	 * The CSS width of the first column of the cell. By default its the cell for the label.
	 */
	@TemplateVariable("labelWidth")
	public String getLabelWidth() {
		return _labelWidth;
	}

	/**
	 * @see #getLabelWidth()
	 */
	public void setLabelWidth(String firstColumnWidth) {
		_labelWidth = firstColumnWidth;
	}

	/**
	 * Custom CSS style value to add to the label part of the cell.
	 */
	@TemplateVariable("labelStyle")
	public String getLabelStyle() {
		return _labelStyle;
	}

	/**
	 * @see #getLabelStyle()
	 */
	public void setLabelStyle(String cssStyle) {
		_labelStyle = cssStyle;
	}

	/**
	 * Custom CSS class for the label part of the cell.
	 */
	@TemplateVariable("labelClass")
	public String getLabelClass() {
		return _labelClass;
	}

	/**
	 * @see #getLabelClass()
	 */
	public void setLabelClass(String cssClass) {
		_labelClass = cssClass;
	}

	/**
	 * Checks whether the label is rendered first.
	 * 
	 * @param control
	 *        The HTMLFragment to check.
	 * @return If the HTMLFragment is not a {@link CheckboxControl} and not a
	 *         {@link IconSelectControl}.
	 */
	protected static boolean controlLabelFirst(HTMLFragment control) {
		return !(control instanceof CheckboxControl || control instanceof IconSelectControl);
	}

	/**
	 * Checks whether the cell is rendered over the whole line.
	 * 
	 * @param control
	 *        The HTMLFragment to check.
	 * @return If the HTMLFragment is a <code>multiLine</code> {@link TextInputControl}.
	 */
	protected static boolean controlWholeLine(HTMLFragment control) {
		if (control instanceof TextInputControl) {
			TextInputControl txtCtrl = (TextInputControl) control;
			return txtCtrl.isMultiLine();
		}

		return false;
	}

	@Override
	public Object getModel() {
		return _model;
	}

	@Override
	protected boolean hasUpdates() {
		return false;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// Ignore.
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		Icons.FORM_CELL_TEMPLATE.get().write(context, out, this);
	}

	@Override
	public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
		if (sender == _visibility) {
			requestRepaint();
		}
		return Bubble.BUBBLE;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		_visibility.addListener(VisibilityModel.VISIBLE_PROPERTY, this);
	}

	@Override
	protected void internalDetach() {
		_visibility.removeListener(VisibilityModel.VISIBLE_PROPERTY, this);

		super.internalDetach();
	}

	/**
	 * Sets the description of this cell. It is rendered like a label.
	 * 
	 * @param description
	 *        The description of this cell.
	 */
	public void setDescription(HTMLFragment description) {
		_description = description;
	}

	/**
	 * Whether the cell has a label at all.
	 */
	@TemplateVariable("hasLabel")
	public boolean hasLabel() {
		return _description != null;
	}

	/**
	 * Writes the label of the cell.
	 */
	@TemplateVariable("label")
	public void writeLabel(DisplayContext context, TagWriter out) throws IOException {
		_description.write(context, out);
	}

	/**
	 * Writes the content of the cell.
	 */
	@TemplateVariable("content")
	public void writeContent(DisplayContext context, TagWriter out) throws IOException {
		_model.write(context, out);
	}

}