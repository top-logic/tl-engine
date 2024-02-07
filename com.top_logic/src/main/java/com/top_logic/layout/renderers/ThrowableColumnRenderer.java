/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.renderers;

import java.io.IOException;
import java.util.Arrays;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Renderer} writing a button opening a message box with a formatted display of the stack
 * trace of a {@link Throwable} value.
 * 
 * @see ThrowableRenderer
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ThrowableColumnRenderer implements Renderer<Throwable> {

	@Override
	public void write(DisplayContext context, TagWriter out, Throwable value) throws IOException {
		if (value != null) {
			WindowScope scope = context.getExecutionScope().getFrameScope().getWindowScope();
			CommandModel button = CommandModelFactory.commandModel(new ShowStacktrace(scope, value));
			button.setImage(Icons.STACKTRACE);
			new ButtonControl(button, ImageButtonRenderer.INSTANCE).write(context, out);
		}
	}

	private final class ShowStacktrace implements Command {
		private final WindowScope _scope;

		private final Throwable _ex;

		public ShowStacktrace(WindowScope scope, Throwable ex) {
			_scope = scope;
			_ex = ex;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext commandContext) {
			LayoutData layoutData = new DefaultLayoutData(
				DisplayDimension.dim(800, DisplayUnit.PIXEL), 100,
				DisplayDimension.dim(500, DisplayUnit.PIXEL), 100,
				Scrolling.AUTO);
			DisplayValue title = new ResourceText(I18NConstants.STACKTRACE);
			DialogModel dialogModel = new DefaultDialogModel(layoutData, title, true, true, null);
			CommandModel okButton = MessageBox.button(ButtonType.OK, dialogModel.getCloseAction());
			dialogModel.setDefaultCommand(okButton);
			return MessageBox.open(_scope, dialogModel, new ThrowableDisplay(_ex), Arrays.asList(okButton));
		}
	}

	private final class ThrowableDisplay implements HTMLFragment {

		private final Throwable _ex;

		public ThrowableDisplay(Throwable ex) {
			_ex = ex;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			ThrowableRenderer.INSTANCE.write(context, out, _ex);
		}
	}

}
