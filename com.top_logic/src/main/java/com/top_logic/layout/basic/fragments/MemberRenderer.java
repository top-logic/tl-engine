/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.fragments;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.FormMember;

/**
 * Renderer for a {@link FormMember}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MemberRenderer {

	/**
	 * Renders the given {@link FormMember}.
	 * 
	 * @param context
	 *        See {@link Renderer#write(DisplayContext, TagWriter, Object)}.
	 * @param out
	 *        See {@link Renderer#write(DisplayContext, TagWriter, Object)}.
	 * @param member
	 *        The {@link FormMember} to display.
	 * @throws IOException
	 *         If writing fails.
	 */
	public abstract void write(DisplayContext context, TagWriter out, FormMember member) throws IOException;

}
