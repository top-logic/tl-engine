/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.boxes.model.Boxes;
import com.top_logic.layout.form.boxes.model.FragmentBox;
import com.top_logic.layout.form.tag.ControlBodyTag;
import com.top_logic.layout.form.tag.FormGroupTag;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link HTMLFragment} that layouts dynamic contents within some static XML fragment.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSPLayoutedControls implements HTMLFragment, ControlBodyTag {

	private String _contentPattern = "";

	private List<HTMLFragment> _controls = new ArrayList<>();

	/**
	 * The controls referenced from static content.
	 * 
	 * @see #getContentPattern()
	 */
	public List<HTMLFragment> getControls() {
		return _controls;
	}

	@Override
	public String addControl(HTMLFragment childControl) {
		if (childControl == null) {
			return "";
		}
		_controls.add(childControl);
		return FormGroupTag.PLACEHOLDER;
	}

	/**
	 * The pattern containing {@link FormGroupTag#PLACEHOLDER} references to {@link #getControls()}.
	 */
	public String getContentPattern() {
		return _contentPattern;
	}

	/**
	 * @see #getContentPattern()
	 */
	public void setContentPattern(String contentPattern) {
		// For legacy code:
		contentPattern = contentPattern.replace("&nbsp;", HTMLConstants.NBSP);

		assert FormGroupTag.assertWellformedXML(contentPattern);
		_contentPattern = contentPattern;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		FormGroupTag.writeControls(context, out, _contentPattern, _controls);
	}

	/**
	 * Utility to create a {@link FragmentBox} with {@link JSPLayoutedControls} contents.
	 */
	public static FragmentBox createJSPContentBox() {
		return Boxes.contentBox(new JSPLayoutedControls());
	}

	/**
	 * Utility to add a control to a box created with {@link #createJSPContentBox()}.
	 */
	public static String addControl(FragmentBox box, HTMLFragment childControl) {
		return ((JSPLayoutedControls) box.getContentRenderer()).addControl(childControl);
	}

	/**
	 * Utility to set the content pattern to a box created with {@link #createJSPContentBox()}.
	 */
	public static void setContentPattern(FragmentBox jspContentBox, String contentPattern) {
		((JSPLayoutedControls) jspContentBox.getContentRenderer()).setContentPattern(contentPattern);
	}

}