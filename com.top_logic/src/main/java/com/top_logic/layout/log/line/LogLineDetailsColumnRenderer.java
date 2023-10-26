/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.log.line;

import static com.top_logic.layout.structure.DefaultDialogModel.*;
import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.renderers.Icons;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The renderer for the "details" column of the {@link LogLine} table.
 * <p>
 * The "details" are usually the stacktrace of the message. But if the logged message consists of
 * multiple lines, i.e. if it contains a line break, the "details" contain everything below the
 * first line.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogLineDetailsColumnRenderer implements Renderer<String> {

	/** The {@link LogLineDetailsColumnRenderer} instance. */
	public static final LogLineDetailsColumnRenderer INSTANCE = new LogLineDetailsColumnRenderer();

	/** The CSS class for this dialog. */
	public static final String DIALOG_CSS_CLASS = "tl-log-lines-table__details-dialog";

	@Override
	public void write(DisplayContext context, TagWriter out, String value) throws IOException {
		if (!StringServices.isEmpty(value)) {
			out.beginBeginTag(SPAN);
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, value);
			out.endBeginTag();
			{
				createButtonControl(context, value).write(context, out);
			}
			out.endTag(SPAN);
		}
	}

	private ButtonControl createButtonControl(DisplayContext context, String value) {
		WindowScope scope = context.getExecutionScope().getFrameScope().getWindowScope();
		CommandModel button = CommandModelFactory.commandModel(displayContext -> openDialog(scope, value));
		button.setImage(Icons.STACKTRACE);
		return new ButtonControl(button, ImageButtonRenderer.INSTANCE);
	}

	private HandlerResult openDialog(WindowScope scope, String details) {
		// 60% width on a 1920*1080 screen is enough to display almost every stacktrace without a scrollbar.
		// 80% height is a good trade-off. Stacktraces are long. But using more would not look good.
		LayoutData layoutData = new DefaultLayoutData(
			DisplayDimension.dim(60, DisplayUnit.PERCENT), 100,
			DisplayDimension.dim(80, DisplayUnit.PERCENT), 100,
			Scrolling.AUTO);
		DisplayValue title = new ResourceText(I18NConstants.TABLE_COLUMN_DETAILS_DIALOG_TITLE);
		DialogModel dialog = new DefaultDialogModel(layoutData, title, RESIZABLE, CLOSE_BUTTON, NO_HELP_ID);
		CommandModel okButton = MessageBox.button(ButtonType.OK, dialog.getCloseAction());
		HTMLFragment content = (displayContext, out) -> writeDetails(out, details);
		return MessageBox.open(scope, dialog, content, List.of(okButton));
	}

	private void writeDetails(TagWriter out, String details) {
		out.beginTag(PRE, CLASS_ATTR, DIALOG_CSS_CLASS);
		out.writeText(details);
		out.endTag(PRE);
	}

}
