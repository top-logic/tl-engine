/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.FormMember;

/**
 * Default {@link Renderer} for {@link FormMember} labels.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DefaultFormMemberLabelRenderer implements Renderer<FormMember> {

	/**
	 * {@link DefaultFormMemberLabelRenderer} singleton. 
	 */
	public static final Renderer<FormMember> INSTANCE = new DefaultFormMemberLabelRenderer();
	
	private DefaultFormMemberLabelRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext context, TagWriter out, FormMember value) throws IOException {
		out.writeText(context.getResources().getString(value.getLabel()));
	}

}
