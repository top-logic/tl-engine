/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.controlprovider;

import java.io.IOException;
import java.util.Collections;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.MetaControlProvider;
import com.top_logic.element.meta.form.fieldprovider.CompositionFieldProvider;
import com.top_logic.element.meta.form.fieldprovider.CompositionFieldProvider.Composite;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.basic.GenericCommandModelOwner;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.SimpleConstantControl;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.tag.Icons;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ControlProvider} creating a {@link Control} to display a {@link Composite} within a
 * dialog.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompositionControlProvider implements ControlProvider {

	/**
	 * Control rendering the dialog opener.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class DialogOpener extends ConstantControl<Composite> {

		final Runnable _closeListener;

		DialogOpener(Composite model, Runnable closeListener) {
			super(model);
			_closeListener = closeListener;
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			out.beginTag(SPAN);
			out.writeText(context.getResources().getString(I18NConstants.COMPOSITE_FIELD_PROVIDER_DETAILS));
			CommandModel command = openingModel();
			if (ScriptingRecorder.isEnabled()) {
				GenericCommandModelOwner.setOwner(command, getModel());
			}
			new ButtonControl(command).write(context, out);
			out.endTag(SPAN);
		}

		private CommandModel openingModel() {
			CommandModel commandModel = new AbstractCommandModel() {

				@Override
				protected HandlerResult internalExecuteCommand(DisplayContext context) {
					DisplayDimension width = DisplayDimension.dim(80, DisplayUnit.PERCENT);
					DisplayDimension height = DisplayDimension.dim(80, DisplayUnit.PERCENT);

					AttributeUpdate update = AttributeFormFactory.getAttributeUpdate(getModel());
					HTMLFragment title;
					if (update != null) {
						TLObject baseObject = update.getObject();
						TLStructuredTypePart attribute = update.getAttribute();
						if (baseObject == null) {
							title = Fragments
								.message(I18NConstants.COMPOSITE_FIELD_DIALOG_TITLE__ATTRIBUTE.fill(attribute));
						} else {
							title = Fragments.message(
								I18NConstants.COMPOSITE_FIELD_DIALOG_TITLE__OBJECT__ATTRIBUTE.fill(baseObject,
									attribute));
						}
					} else {
						title = Fragments.empty();
					}
					DefaultDialogModel dialogModel = new DefaultDialogModel(
						new DefaultLayoutData(width, 100, height, 100, Scrolling.NO), title,
						true,
						true, null);
					dialogModel.addListener(DialogModel.CLOSED_PROPERTY, new DialogClosedListener() {

						@Override
						public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
							getModel().checkAll();
							_closeListener.run();
						}
					});

					hideTableLabel(getModel());

					Control content = MetaControlProvider.INSTANCE.createControl(getModel());
					CommandModel closeButton = MessageBox.button(ButtonType.CLOSE, dialogModel.getCloseAction());
					dialogModel.setDefaultCommand(closeButton);
					MessageBox.open(context, dialogModel, content, Collections.singletonList(closeButton));
					return HandlerResult.DEFAULT_RESULT;
				}

				/**
				 * The composite is displayed in table form. The table is the single element in the
				 * opened dialog which already has a reasonable title.
				 * 
				 * <p>
				 * This is actually only a workaround, since the title line of the dialog and that
				 * of the table header should actually merge.
				 * </p>
				 */
				private void hideTableLabel(Composite composite) {
					TableField table = (TableField) composite.getMember(CompositionFieldProvider.TABLE_FIELD_NAME);
					table.getTableModel().getTableConfiguration().setTitleKey(null);
				}
			};
			commandModel.setImage(Icons.OPEN_CHOOSER);
			return commandModel;
		}
	}

	/** Singleton {@link CompositionControlProvider} instance. */
	public static final CompositionControlProvider INSTANCE = new CompositionControlProvider();

	private CompositionControlProvider() {
		// singleton instance
	}

	@Override
	public Control createControl(Object model, String style) {
		if (model instanceof Composite) {
			Composite composite = (Composite) model;
			if (composite.isActive()) {
				ErrorControl error = new ErrorControl(composite, true);
				BlockControl block = new BlockControl();
				/* Workaround: When the dialog is closed, some inner field may have an error. The
				 * error control for the Composite is not repainted, because the error state is not
				 * transfered. */
				Runnable closeListener = error::requestRepaint;
				block.addChild(new DialogOpener(composite, closeListener));
				block.addChild(error);
				return block;
			} else {
				return new SimpleConstantControl<>(composite.getRowBusinessObjects(), ResourceRenderer.INSTANCE);
			}
		} else {
			return new SimpleConstantControl<>(model, ResourceRenderer.INSTANCE);
		}
	}

}

