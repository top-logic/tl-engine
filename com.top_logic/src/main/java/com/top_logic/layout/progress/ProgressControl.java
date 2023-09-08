/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.progress;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractFormFieldControl;
import com.top_logic.layout.messagebox.ProgressDialog;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Display a progress defined by a numerical value handed over in a {@link FormField}.
 * 
 * The progress will normally be a value between 0 and 100 and result in a progress bar at the UI.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 * 
 * @deprecated Use {@link com.top_logic.layout.messagebox.ProgressControl} or
 *             {@link ProgressDialog}.
 */
@Deprecated
public class ProgressControl extends AbstractFormFieldControl {

	private static final String FILLED_COLOR_VALUE = "background-color:#8280ff;";

	private static final String PROGRESS_BORDER_CSS_CLASS = "progressBorder";

	/**
	 * Creates a {@link ProgressControl}.
	 */
	public ProgressControl(FormField aModel) {
		this(aModel, Collections.EMPTY_MAP);
	}

	/**
	 * Creates a {@link ProgressControl}.
	 */
	public ProgressControl(FormField aModel, Map aCommands) {
		super(aModel, aCommands);
	}

	@Override
	protected String getTypeCssClass() {
		return "cProgress";
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		FormField field = this.getFieldModel();
		int[] rule = this.getFractions(field);

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();

		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, PROGRESS_BORDER_CSS_CLASS);
		out.endBeginTag();

		out.beginBeginTag(SPAN);
		out.writeAttribute(STYLE_ATTR, FILLED_COLOR_VALUE);
		out.endBeginTag();

		for (int i = 0; i < rule[0]; i++) {
			out.writeText(HTMLConstants.NBSP);
		}

		out.endTag(SPAN);

		for (int i = 0; i < rule[1]; i++) {
			out.writeText(HTMLConstants.NBSP);
		}

		out.endTag(SPAN);

		out.writeText(HTMLConstants.NBSP);
		out.writeText(Integer.toString(rule[0]) + NBSP + "%");

		out.endTag(SPAN);
	}

	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
	}

	@Override
	protected void writeEditable(DisplayContext aContext, TagWriter aOut) throws IOException {
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		requestRepaint();
	}

	/**
	 * Return the fractions to be displayed at the UI.
	 * 
	 * @param aField
	 *        The field containing a {@link Number}.
	 * @return The fractions to display (normally values between 0 and 100, where the sum of
	 *         fractions is 100 again).
	 */
	protected int[] getFractions(FormField aField) {
		Object theValue = aField.getValue();

		if (theValue instanceof Number) {
			int theInt = Math.min(100, ((Number) theValue).intValue());

			return new int[] { theInt, 100 - theInt };
		}
		else {
			return new int[] { 0, 100 };
		}
	}
}
