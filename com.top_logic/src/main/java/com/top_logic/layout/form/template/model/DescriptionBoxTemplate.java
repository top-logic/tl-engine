/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.boxes.reactive_tag.DescriptionCellControl;
import com.top_logic.layout.form.template.model.internal.TemplateRenderer;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.model.annotate.LabelPosition;
import com.top_logic.model.form.definition.LabelPlacement;

/**
 * {@link HTMLTemplateFragment} creating a label and content box.
 * 
 * @see #getLabel()
 * @see #getContent()
 * @see #getLabelPosition()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DescriptionBoxTemplate implements Template {

	private final HTMLTemplateFragment _label;

	private final HTMLTemplateFragment _content;

	private LabelPosition _labelPosition;

	/** Width of the first column for created {@link FormMember}s. */
	public static final String FIRST_COLUMN_WIDTH = "15em";

	private LabelPlacement _labelPlacement;

	/**
	 * Creates a {@link DescriptionBoxTemplate}.
	 * 
	 * @param label
	 *        See {@link #getLabel()}.
	 * @param content
	 *        See {@link #getContent()}.
	 * @param labelPosition
	 *        See {@link #getLabelPosition()}.
	 * @param labelPlacement
	 *        Where the label is placed.
	 */
	DescriptionBoxTemplate(HTMLTemplateFragment label, HTMLTemplateFragment content, LabelPosition labelPosition,
			LabelPlacement labelPlacement) {
		_label = label;
		_content = content;
		_labelPosition = labelPosition;
		_labelPlacement = labelPlacement;
	}

	/**
	 * The label box containing the description for the {@link #getContent()} box.
	 */
	public HTMLTemplateFragment getLabel() {
		return _label;
	}

	/**

	 * The position of the label.
	 */
	public LabelPosition getLabelPosition() {
		return _labelPosition;
	}

	/**
	 * The content box containing the main content.
	 * 
	 * @see #getLabel()
	 */
	public HTMLTemplateFragment getContent() {
		return _content;
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out, WithProperties properties) throws IOException {
		FormMember model = (FormMember) TemplateRenderer.model(properties);
		DescriptionCellControl descriptionCell =
			new DescriptionCellControl(model, TemplateRenderer.toFragmentInline(properties, getContent()));

		descriptionCell.setDescription(TemplateRenderer.toFragmentInline(properties, getLabel()));

		String firstColumnWidth = model != null ? model.get(AbstractMember.FIRST_COLUMN_WIDTH) : null;
		if (firstColumnWidth != null) {
			descriptionCell.setLabelWidth(firstColumnWidth);
		} else {
			descriptionCell.setLabelWidth(FIRST_COLUMN_WIDTH);
		}

		Boolean renderWholeLine = model != null ? model.get(AbstractMember.RENDER_WHOLE_LINE) : null;
		if (renderWholeLine != null) {
			descriptionCell.setWholeLine(renderWholeLine.booleanValue());
		}
		descriptionCell.setLabelPosition(getLabelPosition());
		descriptionCell.setLabelPlacement(_labelPlacement);

		TemplateRenderer.renderControl(displayContext, out, descriptionCell);
	}
}
