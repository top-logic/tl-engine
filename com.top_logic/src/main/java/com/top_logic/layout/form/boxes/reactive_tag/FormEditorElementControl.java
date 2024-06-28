/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.provider.ImageResourceProvider;
import com.top_logic.model.form.ReactiveFormCSS;

/**
 * Control rendering a container for an attribute (icon and label) and a field (label and input)
 * where only one is visible at the same time.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormEditorElementControl extends ConstantControl<HTMLFragment> {

	private final String ATTRIBUTE_CSS = "attribute";

	private final String FIELD_CSS = "field";

	private final String HIDDEN_CSS = "hidden";

	private final String DRAG_ELEMENT_CSS = "rf_dragElement";

	private final String WHOLE_LINE_CSS = ReactiveFormCSS.RF_LINE;

	private ImageProvider _imageProvider;

	private String _id;

	private boolean _attributeHidden = false;

	private boolean _draggable = false;

	private boolean _wholeLine = false;

	private boolean _isTool = false;

	private String _formEditorControl;

	private Object _value;

	/**
	 * Creates a new {@link FormEditorElementControl}.
	 * 
	 * @param model
	 *        The {@link HTMLFragment} containing the element to display.
	 */
	public FormEditorElementControl(HTMLFragment model) {
		super(model);
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		if (getModel() != null) {
			String formEditorControlJS =
				!StringServices.isEmpty(getFormEditorControl()) ? "'" + getFormEditorControl() + "'" : null;

			out.beginBeginTag(DIV);
			writeControlAttributes(context, out);
			out.writeAttribute(DRAGGABLE_ATTR, isDraggable());
			out.writeAttribute(DATA_ATTRIBUTE_PREFIX + "id", _id);
			out.writeAttribute(DATA_ATTRIBUTE_PREFIX + "tool", getIsTool());
			if (isDraggable()) {
				out.writeAttribute(ONDBLCLICK_ATTR,
					"services.form.FormEditorControl.editElement(event, " + formEditorControlJS + ", '" + _id + "')");
			}
			out.endBeginTag();

			writeAttributeField(context, out);
			writeFormCell(context, out);

			out.endTag(DIV);
		}
	}

	private void writeAttributeField(DisplayContext context, TagWriter out) throws IOException {
		String cssClass = ATTRIBUTE_CSS + (isAttributeHidden() ? " " + HIDDEN_CSS : "");

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, cssClass);
		out.endBeginTag();
		writeResource(context, out, _imageProvider);
		out.endTag(DIV);
	}

	private void writeFormCell(DisplayContext context, TagWriter out) throws IOException {
		String cssClass = FIELD_CSS + (!isAttributeHidden() ? " " + HIDDEN_CSS : "");

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, cssClass);
		out.endBeginTag();

		getModel().write(context, out);

		out.endTag(DIV);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		if (isDraggable()) {
			out.append(DRAG_ELEMENT_CSS);
		}
		if (getRenderWholeLine()) {
			out.append(WHOLE_LINE_CSS);
			out.append(FormConstants.OVERFLOW_AUTO_CLASS);
		}
	}

	/**
	 * Sets the {@link ImageProvider} for the icon-label-tuple.
	 */
	public void setImageProvider(ImageProvider imageProvider) {
		_imageProvider = imageProvider;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *        The data-id.
	 */
	public void setID(String id) {
		_id = id;
	}

	/**
	 * Sets whether the attribute is hidden and the field is visible.
	 * 
	 * @param attributeHidden
	 *        <code>true</code> attribute is hidden, field is visable
	 */
	public void setAttributeHidden(boolean attributeHidden) {
		_attributeHidden = attributeHidden;
	}

	private boolean isAttributeHidden() {
		return _attributeHidden;
	}

	/**
	 * Sets whether the field tuple is draggable.
	 * 
	 * @param draggable
	 *        <code>true</code> is draggable.
	 */
	public void setDraggable(boolean draggable) {
		_draggable = draggable;
	}

	private boolean isDraggable() {
		return _draggable;
	}

	/**
	 * Sets whether the element is rendered over the whole line.
	 */
	public void setRenderWholeLine(boolean wholeLine) {
		_wholeLine = wholeLine;
	}

	private Boolean getRenderWholeLine() {
		return _wholeLine;
	}

	/**
	 * Sets whether the element is a tool of the toolbox.
	 */
	public void setIsTool(boolean isTool) {
		_isTool = isTool;
	}

	private Boolean getIsTool() {
		return _isTool;
	}

	/**
	 * Sets the id of the form editor.
	 */
	public void setFormEditorControl(String formEditorControl) {
		_formEditorControl = formEditorControl;
	}

	private String getFormEditorControl() {
		return _formEditorControl;
	}

	/**
	 * Sets the value of the element to display additional information.
	 * 
	 * @see ResourceRenderer#write(DisplayContext, TagWriter, Object, boolean, boolean, boolean,
	 *      boolean)
	 */
	public void setValue(Object value) {
		_value = value;
	}

	private Object getValue() {
		return _value;
	}

	private void writeResource(DisplayContext context, TagWriter out, ImageProvider imageProvider) throws IOException {
		if (imageProvider != null) {
			ResourceRenderer.newResourceRenderer(new ImageResourceProvider(imageProvider)).write(context, out,
				getValue(), !ResourceRenderer.USE_LINK, ResourceRenderer.USE_TOOLTIP, ResourceRenderer.USE_IMAGE,
				!ResourceRenderer.USE_CONTEXT_MENU);
		}
	}

	@Override
	protected String getTypeCssClass() {
		return "cFormEditorElement";
	}
}