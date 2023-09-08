/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.fragments;

import java.io.IOException;
import java.util.Iterator;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;

/**
 * {@link MemberRenderer} that iterates over the members of a {@link FormContainer}.
 * 
 * @see Fragments#loop(ContainerRenderer)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ContainerRenderer extends MemberRenderer {

	/**
	 * Called when iteration starts.
	 * 
	 * @param context
	 *        See {@link #write(DisplayContext, TagWriter, FormMember)}.
	 * @param out
	 *        See {@link #write(DisplayContext, TagWriter, FormMember)}.
	 * @param container
	 *        The {@link FormContainer} to be iterated.
	 * @return The {@link FormMember}s of the given container that should be rendered.
	 */
	Iterator<? extends FormMember> begin(DisplayContext context, TagWriter out, FormContainer container)
			throws IOException;

	/**
	 * Called after iteration has successfully finished.
	 * 
	 * @see #begin(DisplayContext, TagWriter, FormContainer)
	 */
	void end(DisplayContext context, TagWriter out, FormContainer container) throws IOException;

}
