/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.dnd;

import java.util.Map;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.dnd.DnD;
import com.top_logic.layout.dnd.DndData;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractFormMemberControl;
import com.top_logic.layout.form.control.I18NConstants;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ControlCommand} for {@link AbstractFormMemberControl}s to accept drop operations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FieldDrop extends ControlCommand {

	private static final Property<FieldDropTarget> FIELD_DROP_TARGET =
		TypedAnnotatable.property(FieldDropTarget.class, "fieldDrop");

	private static final String COMMAND = "dndFieldDrop";

	/**
	 * Singleton {@link FieldDrop} instance.
	 */
	public static final FieldDrop INSTANCE = new FieldDrop();

	private FieldDrop() {
		super(COMMAND);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.DND_FIELD_DROP;
	}

	@Override
	protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
		DndData data = DnD.getDndData(context, arguments);
		if (data != null) {
			FormMember field = (FormMember) control.getModel();
			FieldDropTarget target = getDropTarget(field);
			if (target.dropEnabled(field)) {
				target.handleDrop(field, data);
			}
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Assigns a {@link FieldDropTarget} to a {@link FormMember}.
	 * 
	 * @param field
	 *        The {@link FormMember} that should accept drops.
	 * @param value
	 *        The {@link FieldDropTarget} implementation.
	 */
	public static void setDropTarget(FormMember field, FieldDropTarget value) {
		field.set(FIELD_DROP_TARGET, value);
	}

	/**
	 * The {@link FieldDropTarget} assigned to a {@link FormMember}.
	 * 
	 * @param field
	 *        The {@link FormMember} to lookup its {@link FieldDropTarget}.
	 * @return The assigned {@link FieldDropTarget}.
	 */
	public static FieldDropTarget getDropTarget(FormMember field) {
		FieldDropTarget result = field.get(FIELD_DROP_TARGET);
		if (result != null) {
			return result;
		}
		if (field instanceof SelectField) {
			return DefaultSelectFieldDrop.INSTANCE;
		}
		return NoFieldDrop.INSTANCE;
	}

}