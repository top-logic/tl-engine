/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.format.CommonMark;

/**
 * {@link TextInputControl} rendering multiline content in <i>CommonMark</i> manner.
 * 
 * @see "https://commonmark.org/"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommonMarkControl extends TextInputControl {

	/**
	 * Creates a new {@link CommonMarkControl}.
	 */
	public CommonMarkControl(FormField model) {
		super(model);
	}

	@Override
	protected void writeMultiLineTextContent(TagWriter out, String value) throws IOException {
		CommonMark.writeCommonMark(out, value);
	}

}

