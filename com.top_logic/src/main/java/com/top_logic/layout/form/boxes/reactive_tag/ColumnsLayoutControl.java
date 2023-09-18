/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.definition.Columns;

/**
 * Control creating a containers with a configurable number of columns for reactive columns.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ColumnsLayoutControl extends ConstantControl<HTMLFragment> {

	private int _count = Columns.ONE.getValue().intValue();

	private boolean _keep = false;

	/**
	 * Creates a {@link GroupCellControl}.
	 * 
	 * @param model
	 *        The content of this ColumnsLayout.
	 */
	public ColumnsLayoutControl(HTMLFragment model) {
		super(model);
	}

	/**
	 * Maximum number of columns to use. Default is 1.
	 * 
	 * @param count
	 *        Number of columns.
	 */
	public void setCount(int count) {
		_count = count;
	}

	/**
	 * Whether the number of columns is kept instead of adjust to the viewport size. Default is
	 * <code>false</code>.
	 * 
	 * @param keep
	 *        If <code>true</code> the number is kept.
	 */
	public void setKeep(boolean keep) {
		_keep = keep;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		getModel().write(context, out);
		out.endTag(DIV);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		HTMLUtil.appendCSSClass(out, ReactiveFormCSS.RF_COLUMNS_LAYOUT);
		HTMLUtil.appendCSSClass(out, Columns.getColsCSS(_count));
		if (_keep) {
			HTMLUtil.appendCSSClass(out, ReactiveFormCSS.CSS_CLASS_KEEP);
		}
	}

}