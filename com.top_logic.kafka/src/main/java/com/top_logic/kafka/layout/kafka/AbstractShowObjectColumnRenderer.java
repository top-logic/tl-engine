/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.kafka;

import java.io.IOException;
import java.util.Arrays;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.fragments.Fragments;
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
 * {@link Renderer} writing a button opening a message box with a formatted display of the object
 * provided.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractShowObjectColumnRenderer<T> implements Renderer<T> {

	/**
	 * Provide the content renderer to be used render value.
	 * 
	 * @return The requested renderer, never <code>null</code>.
	 */
	protected abstract Renderer<? super T> getContentRenderer();

	/**
	 * Icon to be used for the command in the UI.
	 * 
	 * @return The requested icon, never <code>null</code>.
	 */
	protected abstract ThemeImage getIcon();

	/**
	 * Return the title for the message boxes title, e.g. a {@link ResourceText} for an
	 * {@link ResKey}.
	 * 
	 * @return The requested {@link DisplayValue} never <code>null</code>.
	 */
	protected abstract DisplayValue getTitle();

	@Override
	public void write(DisplayContext context, TagWriter out, T value) throws IOException {
		if (value != null) {
			ShowObjectCommand command = new ShowObjectCommand(getContentRenderer(), getTitle(), value);
			CommandModel commandModel = CommandModelFactory.commandModel(command);
			commandModel.setImage(getIcon());

			new ButtonControl(commandModel, ImageButtonRenderer.INSTANCE).write(context, out);
		}
	}

	/**
	 * Show the given object in a message box.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	private final class ShowObjectCommand implements Command {

		private final T _value;

		private final Renderer<? super T> _renderer;

		private final DisplayValue _title;

		/**
		 * Creates a {@link ShowObjectCommand}.
		 */
		public ShowObjectCommand(Renderer<? super T> renderer, DisplayValue title, T value) {
			_renderer = renderer;
			_title = title;
			_value = value;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext commandContext) {
			LayoutData layout = createLayoutData();
			DialogModel dialogModel = new DefaultDialogModel(layout, _title, true, true, null);
			CommandModel okButton = MessageBox.button(ButtonType.OK, dialogModel.getCloseAction());
			dialogModel.setDefaultCommand(okButton);
			return MessageBox.open(commandContext, dialogModel, createDisplay(), Arrays.asList(okButton));
		}

		private HTMLFragment createDisplay() {
			return Fragments.rendered(_renderer, _value);
		}

		private LayoutData createLayoutData() {
			return new DefaultLayoutData(
				DisplayDimension.dim(800, DisplayUnit.PIXEL), 100,
				DisplayDimension.dim(600, DisplayUnit.PIXEL), 100,
				Scrolling.AUTO);
		}
	}
}
