/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;

/**
 * The class {@link DecoratedControlRenderer} is a renderer for controls which
 * is additional decorated by an {@link ContentDecorator}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DecoratedControlRenderer<T extends Control> extends DefaultControlRenderer<T> {

	private final ContentDecorator decorator;

	private final Renderer<? super T> contentRenderer;
	private final String controlTag;

	/**
	 * creates a new {@link DecoratedControlRenderer}.
	 * 
	 * @param controlTag
	 *        must not be <code>null</code>.
	 * @param decorator
	 *        the decorator for the control to render. must not be
	 *        <code>null</code>.
	 * @param contentRenderer
	 *        the renderer which writes the actual content of the control. must
	 *        not be <code>null</code>. Must also not be an instance of
	 *        {@link DefaultControlRenderer} as those would also write the ID of
	 *        the rendered control.
	 */
	public DecoratedControlRenderer(String controlTag, ContentDecorator decorator,
			Renderer<? super T> contentRenderer) {
		assert !(contentRenderer instanceof DefaultControlRenderer) : "This renderer is itself a " + DefaultControlRenderer.class.getName()
				+ " and writes the ID of the control. The inner one must not write the ID which is done by a "
				+ DefaultControlRenderer.class.getName();
		this.controlTag = controlTag;
		this.decorator = decorator;
		this.contentRenderer = contentRenderer;
	}

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, T control) throws IOException {
		decorator.startDecoration(context, out, control);
		contentRenderer.write(context, out, control);
		decorator.endDecoration(context, out, control);
	}

	@Override
	protected String getControlTag(Control control) {
		return controlTag;
	}

}
