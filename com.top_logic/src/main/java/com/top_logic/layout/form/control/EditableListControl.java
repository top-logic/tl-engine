/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;

import javax.swing.ListModel;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Control displaying a {@link ListField}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EditableListControl extends AbstractFormFieldControl {

	private static final Map COMMANDS = 
		createCommandMap(
			AbstractFormFieldControl.COMMANDS,
			new ControlCommand[] {
				EditList.INSTANCE
			});
	
	private Renderer<Object> elementRenderer = ResourceRenderer.INSTANCE;
	
	private ActionListener onEdit;
	
	public EditableListControl(ListField model) {
		super(model, COMMANDS);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
	}
	
	public final ListField getListField() {
		return ((ListField) getModel());
	}
	
	public void setOnEdit(ActionListener onEdit) {
		this.onEdit = onEdit;
	}

	@Override
	protected String getTypeCssClass() {
		return "cEditableList";
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		ListModel listModel = getListField().getListModel();
		writeList(context, out, listModel);
		if (listModel.getSize() > 0) {
			out.writeText(NBSP);
		}
		SelectionControl.writePopupButton(context, out, this, getModel().isDisabled(), getEditButtonId(),
			EditList.INSTANCE, getModel().getLabel());
	}

	@Override
	protected void writeBlocked(DisplayContext context, TagWriter out) throws IOException {
		writeBlocked(context, out, this, I18NConstants.BLOCKED_LIST_TEXT);
	}
	
	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		writeList(context, out, getListField().getListModel());
	}

	private String getEditButtonId() {
		return getID() + "-edit";
	}
	
	private void writeList(DisplayContext context, TagWriter out, ListModel listModel) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			int cnt = listModel.getSize();
			for (int n = 0; n < cnt; n++) {
				if (n > 0)
					out.writeText(", ");
				elementRenderer.write(context, out, listModel.getElementAt(n));
			}
		}
		out.endTag(SPAN);
	}
	
	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		requestRepaint();
	}

	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		// Deactivate buttons. Could be also formulated as incremental update.
		requestRepaint();
	}

	public static class EditList extends ControlCommand {
		
		public static final EditList INSTANCE = new EditList();

		public EditList() {
			super("editList");
		}

		/** 
		 * @see com.top_logic.layout.basic.ControlCommand#execute(com.top_logic.layout.DisplayContext, com.top_logic.layout.Control, Map)
		 */
		@Override
		protected HandlerResult execute(DisplayContext commandContext,
				Control control, Map<String, Object> arguments) {
			EditableListControl listControl = (EditableListControl) control;
			
			listControl.handleListEdit();
			
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return com.top_logic.layout.form.control.I18NConstants.EDIT_LIST;
		}
	}

	protected void handleListEdit() {
		if (onEdit != null) 
			onEdit.actionPerformed(
				new ActionEvent(this, ActionEvent.ACTION_PERFORMED, EditList.INSTANCE.getID()));
	}
}
