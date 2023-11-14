/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;

/**
 * {@link ControlProvider} that produces a layout within a width-adjustable table column.
 * 
 * <p>
 * The {@link FlexibleCellControlProvider} can be used to render more than a single field in a table
 * with width-adjustable columns. It is parametrized with an array of field names interleaving with
 * width styles, see {@link FlexibleCellControlProvider#FlexibleCellControlProvider(String...)}.
 * </p>
 * 
 * <p>
 * The model is expected to be a {@link FormContainer} containing {@link FormMember}s with names
 * referenced by the configuration passed to the constructor.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FlexibleCellControlProvider implements ControlProvider {

	private final String[] _fieldsAndWidths;

	/**
	 * Creates a {@link FlexibleCellControlProvider}.
	 * 
	 * @param fieldsAndWidths
	 *        Array of interleaving field names and width styles. E.g.
	 *        <code>{"field1", "40%", "field2", "0px", "field3", "60%"}</code> renders three fields
	 *        in a single column using 40% of the column width for the field <code>field1</code>,
	 *        only as much space as required for <code>field2</code>, and 60% of the space for
	 *        <code>field3</code>.
	 */
	public FlexibleCellControlProvider(String... fieldsAndWidths) {
		if (fieldsAndWidths.length % 2 != 0) {
			throw new IllegalArgumentException("A list of field names and width specifications is expected.");
		}
		_fieldsAndWidths = fieldsAndWidths;
	}

	@Override
	public Control createControl(Object model, String style) {
		final FormContainer container = (FormContainer) model;
		final String[] fieldsAndWidths = _fieldsAndWidths;
		return new AbstractVisibleControl() {

			@Override
			public Object getModel() {
				return null;
			}

			@Override
			protected String getTypeCssClass() {
				return "cDecoratedCell";
			}

			@Override
			protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
				ControlProvider cp = DefaultFormFieldControlProvider.INSTANCE;

				out.beginBeginTag(SPAN);
				writeControlAttributes(context, out);
				out.endBeginTag();
				for (int n = 0, cnt = fieldsAndWidths.length; n < cnt; n += 2) {
					String name = fieldsAndWidths[n];
					String widthStyle = fieldsAndWidths[n + 1];

					FormMember member = container.getMember(name);

					out.beginBeginTag(SPAN);
					out.writeAttribute(CLASS_ATTR, "lFlexibleCustom");
					out.writeAttribute(STYLE_ATTR, "width:" + widthStyle + ";");
					out.endBeginTag();
					HTMLFragment inner = cp.createFragment(member);
					inner.write(context, out);

					HTMLFragment error = cp.createFragment(member, FormTemplateConstants.STYLE_ERROR_VALUE);
					error.write(context, out);
					out.endTag(SPAN);
				}
				out.endTag(SPAN);
			}
		};
	}

}
