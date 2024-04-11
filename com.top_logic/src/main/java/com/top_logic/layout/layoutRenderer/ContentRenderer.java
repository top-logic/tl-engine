/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.structure.ContentControl;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link ContentRenderer} is used to render the content of a {@link ContentControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ContentRenderer extends LayoutControlRenderer<ContentControl> {

	/**
	 * Singleton {@link ContentRenderer} instance.
	 */
	public static final ContentRenderer INSTANCE = new ContentRenderer();

	private ContentRenderer() {
		// Singleton constructor.
	}

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, ContentControl control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);

		ContentControl contentControl = control;
		contentControl.writeLayoutSizeAttribute(out);
		writeLayoutInformationAttribute(Orientation.VERTICAL, 100, out);

		if (contentControl.dropEnabled()) {
			writeOnDropAttributes(out, contentControl);
		}
	}

	/**
	 * Writes drag and drop attributes for a drop-enabled component.
	 * 
	 * @param contentControl
	 *        The {@link ContentControl} being rendered.
	 */
	protected void writeOnDropAttributes(TagWriter out, ContentControl contentControl) throws IOException {
		out.beginAttribute(ONDROP_ATTR);
		out.append("return services.form.ContentControl.handleOnDrop(event, this);");
		out.endAttribute();
		out.beginAttribute(ONDRAGOVER_ATTR);
		out.append("return services.form.ContentControl.handleOnDragOver(event, this);");
		out.endAttribute();
		out.beginAttribute(ONDRAGENTER_ATTR);
		out.append("return services.form.ContentControl.handleOnDragEnter(event, this);");
		out.endAttribute();
		out.beginAttribute(ONDRAGLEAVE_ATTR);
		out.append("return services.form.ContentControl.handleOnDragLeave(event, this);");
		out.endAttribute();
	}

	@Override
	public void writeControlContents(DisplayContext context, TagWriter out, ContentControl value) throws IOException {
		ContentControl contentControl = value;
		if (LayoutComponent.isDebugHeadersEnabled()) {
			LayoutComponent businessComponent = contentControl.getModel();
			businessComponent.writeDebugHeader(out);
		}

		if (contentControl.dropEnabled()) {
			out.beginBeginTag(DIV);
			out.beginCssClasses();
			out.append("lcViewport");
			contentControl.writeScrollingClass(out);
			out.endCssClasses();
			writeLayoutConstraintInformation(100.0f, DisplayUnit.PERCENT, out);
			writeLayoutInformationAttribute(Orientation.VERTICAL, 100, out);
			out.endBeginTag();
			{
				contentControl.getView().write(context, out);
			}
			out.endTag(DIV);

			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "dropPane");
			out.endBeginTag();
			out.endTag(DIV);
		} else {
			contentControl.getView().write(context, out);
		}
	}

}
