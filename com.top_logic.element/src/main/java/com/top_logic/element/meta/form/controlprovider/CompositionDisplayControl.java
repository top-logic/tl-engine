/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.element.meta.form.fieldprovider.CompositionFieldProvider.Composite;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.GenericCommandModelOwner;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractFormFieldControl;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.tag.Icons;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.provider.MetaLabelProvider;
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
 * {@link Control} for displaying composition reference value with a pop-up edit dialog.
 */
public class CompositionDisplayControl extends AbstractFormFieldControl {

	/**
	 * Creates a {@link CompositionDisplayControl}.
	 */
	public CompositionDisplayControl(Composite model) {
		super(model);
	}

	/**
	 * The {@link Composite} model.
	 */
	public Composite getComposite() {
		return (Composite) getModel();
	}

	@Override
	protected String getTypeCssClass() {
		return "cPopupSelect";
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();

		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, FormConstants.FLEXIBLE_CSS_CLASS);
		out.endBeginTag();
		{
			// context.getResources().getString(I18NConstants.COMPOSITE_FIELD_PROVIDER_DETAILS)
			out.writeText(
				MetaLabelProvider.INSTANCE.getLabel(getComposite().getTableField().getTableModel().getAllRows()));
		}
		out.endTag(SPAN);

		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, FormConstants.FIXED_RIGHT_CSS_CLASS);
		out.endBeginTag();
		{
			CommandModel command = openingModel();
			if (ScriptingRecorder.isEnabled()) {
				GenericCommandModelOwner.setOwner(command, getModel());
			}
			new ButtonControl(command).write(context, out);
		}
		out.endTag(SPAN);

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
						getComposite().checkAll();

						// Value might have changed.
						CompositionDisplayControl.this.requestRepaint();
					}
				});

				hideTableLabel(getComposite());

				Control content = MetaControlProvider.INSTANCE.createControl(getModel());
				CommandModel closeButton = MessageBox.button(ButtonType.OK, dialogModel.getCloseAction());
				dialogModel.setDefaultCommand(closeButton);
				MessageBox.open(context, dialogModel, content, Collections.singletonList(closeButton));
				return HandlerResult.DEFAULT_RESULT;
			}

			/**
			 * The composite is displayed in table form. The table is the single element in the
			 * opened dialog which already has a reasonable title.
			 * 
			 * <p>
			 * This is actually only a workaround, since the title line of the dialog and that of
			 * the table header should actually merge.
			 * </p>
			 */
			private void hideTableLabel(Composite composite) {
				TableField table = (TableField) composite.getTableField();
				table.getTableModel().getTableConfiguration().setTitleKey(null);
			}
		};
		commandModel.setImage(Icons.OPEN_CHOOSER);
		return commandModel;
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		ResourceRenderer.INSTANCE.write(context, out, getComposite().getRowBusinessObjects());
		out.endTag(SPAN);
	}

	@Override
	protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		requestRepaint();
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		requestRepaint();
	}

}
