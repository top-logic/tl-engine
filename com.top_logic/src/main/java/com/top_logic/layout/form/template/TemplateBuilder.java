/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import org.w3c.dom.Document;

import com.top_logic.basic.col.Provider;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.layoutRenderer.LayoutControlRenderer;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Utility for constructing template documents.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TemplateBuilder implements Provider<Document> {

	private StringBuilder _buffer;

	@Override
	public Document get() {
		StringBuilder buffer = new StringBuilder();
		buildTemplateText(buffer);
		return DOMUtil.parse(buffer.toString());
	}

	private void buildTemplateText(StringBuilder buffer) {
		_buffer = buffer;
		internalBuild();
		_buffer = null;
	}

	/**
	 * Main method of template creation.
	 */
	protected abstract void internalBuild();

	/**
	 * Appends the given string the the template.
	 * 
	 * @return a reference to this {@link TemplateBuilder}
	 */
	protected final TemplateBuilder append(String s) {
		_buffer.append(s);
		return this;
	}

	protected final void layoutBeginBegin() {
		append("<div class='layoutControl'");
	}

	protected final void layoutEndBegin() {
		append(">");
	}

	/**
	 * Creates the end tag of a layout control.
	 */
	protected final void layoutEnd() {
		append("</div>");
	}

	/**
	 * Marks a {@link #layoutBeginBegin()} as complex widget.
	 */
	protected final void complexWidget() {
		append(" ").append(HTMLConstants.TL_COMPLEX_WIDGET_ATTR).append("=\"true\"");
	}

	/**
	 * Writes the layout information for vertical layout.
	 * 
	 * @param pixel
	 *        The height in pixels.
	 */
	protected final void layoutVerticalPixel(int pixel) {
		layoutBeginBegin();
		verticalPixel(pixel);
		layoutEndBegin();
	}

	protected final void verticalPixel(int pixel) {
		append(LayoutControlRenderer.getLayoutConstraintInformation(pixel, DisplayUnit.PIXEL));
		append(LayoutControlRenderer.getLayoutInformation(Orientation.VERTICAL, 100));
	}

	/**
	 * Writes the layout information for vertical layout.
	 * 
	 * @param percent
	 *        The height in percent.
	 */
	protected final void layoutVerticalPercent(int percent) {
		layoutBeginBegin();
		verticalPercent(percent);
		layoutEndBegin();
	}

	protected final void verticalPercent(int percent) {
		append(LayoutControlRenderer.getLayoutConstraintInformation(percent, DisplayUnit.PERCENT));
		append(LayoutControlRenderer.getLayoutInformation(Orientation.VERTICAL, 100));
	}

	/**
	 * Writes the layout information for horizontal layout.
	 * 
	 * @param pixel
	 *        The width in pixels.
	 */
	protected final void layoutHorizontalPixel(int pixel) {
		layoutBeginBegin();
		horizontalPixel(pixel);
		layoutEndBegin();
	}

	protected final void horizontalPixel(int pixel) {
		append(LayoutControlRenderer.getLayoutConstraintInformation(pixel, DisplayUnit.PIXEL));
		append(LayoutControlRenderer.getLayoutInformation(Orientation.HORIZONTAL, 100));
	}

	/**
	 * Write layout information for {@link DisplayDimension} of horizontal layout.
	 */
	protected final void horizontalDimension(DisplayDimension dimension) {
		append(LayoutControlRenderer.getLayoutConstraintInformation(dimension.getValue(), dimension.getUnit()));
		append(LayoutControlRenderer.getLayoutInformation(Orientation.HORIZONTAL, 100));
	}

	/**
	 * Writes the layout information for horizontal layout.
	 * 
	 * @param percent
	 *        The width in percent.
	 */
	protected final void layoutHorizontalPercent(int percent) {
		layoutBeginBegin();
		horizontalPercent(percent);
		layoutEndBegin();
	}

	protected final void horizontalPercent(int percent) {
		append(LayoutControlRenderer.getLayoutConstraintInformation(percent, DisplayUnit.PERCENT));
		append(LayoutControlRenderer.getLayoutInformation(Orientation.HORIZONTAL, 100));
	}

	/**
	 * Writes a form label.
	 * 
	 * @param name
	 *        The name of the field.
	 */
	protected final void label(String name) {
		append("<p:field name='").append(name).append("' style='label' />");
	}

	/**
	 * Writes a form error marker.
	 * 
	 * @param name
	 *        The name of the field.
	 */
	protected void error(String name) {
		append("<p:field name='").append(name).append("' style='error' />");
	}

	/**
	 * Writes non-breakable space.
	 */
	protected void space() {
		append(HTMLConstants.NBSP);
	}

	/**
	 * Writes a form input.
	 * 
	 * @param name
	 *        The name of the field.
	 */
	protected final void field(String name) {
		append("<p:field name=\"").append(name).append("\" />");
	}

	/**
	 * Writes the namespaces for HTML and templates.
	 */
	protected void appendNameSpaces() {
		append(" xmlns='").append(HTMLConstants.XHTML_NS).append("'");
		append(" xmlns:t='").append(FormTemplateConstants.TEMPLATE_NS).append("'");
		append(" xmlns:p='").append(FormPatternConstants.PATTERN_NS).append("'");
	}

}
