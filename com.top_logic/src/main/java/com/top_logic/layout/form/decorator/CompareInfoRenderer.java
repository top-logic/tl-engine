/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.decorator.DetailDecorator.Context;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * Render information from a {@link CompareInfo} to a {@link TagWriter}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CompareInfoRenderer extends AbstractCellRenderer {

	private Context _context;
	private DetailDecorator _decorator;

	/**
	 * Creates a {@link CompareInfoRenderer}.
	 * 
	 * @param decorator
	 *        The {@link DetailDecorator} to delegate to.
	 * @param context
	 *        The {@link Context} in which the {@link DetailDecorator} is rendered.
	 */
	public CompareInfoRenderer(DetailDecorator decorator, Context context) {
		_context = context;
		_decorator = decorator;
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, final Cell cell) throws IOException {
		final Object value = cell.getValue();
		if (value == null) {
			return;
		}

		out.beginBeginTag(SPAN);
		out.beginAttribute(ONCLICK_ATTR);
		out.append("BAL.eventStopPropagation(event); BAL.cancelEvent(event);");
		out.endAttribute();
		out.endBeginTag();
		new ButtonControl(CommandModelFactory.commandModel(
			new OpenChangeDetailDialog(_decorator, _context, cell.getView().getTableData(), cell.getRowObject(),
				(CompareInfo) value)),
			new ChangeDetailDialogRenderer(_decorator, _context, value))
			.write(context, out);
		out.endTag(SPAN);
	}
}
