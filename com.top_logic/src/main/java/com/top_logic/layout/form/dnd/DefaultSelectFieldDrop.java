/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.dnd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.dnd.DndData;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.tree.model.TLTreeModelUtil;

/**
 * {@link FieldDropTarget} for {@link SelectField}s accepting the dropped object, if it is in the
 * options list of the field.
 * 
 * <p>
 * This handler is used by default for {@link SelectField}s.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultSelectFieldDrop implements FieldDropTarget {

	/**
	 * Singleton {@link DefaultSelectFieldDrop} instance.
	 */
	public static final DefaultSelectFieldDrop INSTANCE = new DefaultSelectFieldDrop();

	private DefaultSelectFieldDrop() {
		// Singleton constructor.
	}

	@Override
	public boolean dropEnabled(FormMember field) {
		return field instanceof SelectField;
	}

	@Override
	public void handleDrop(FormMember field, DndData data) {
		SelectField select = (SelectField) field;
		OptionModel<?> optionModel = select.getOptionModel();
		if (optionModel instanceof ListOptionModel) {
			List<?> options = ((ListOptionModel<?>) optionModel).getBaseModel();
			Collection<Object> droppedObjects = data.getDragData();
			droppedObjects.forEach(element -> TLTreeModelUtil.getInnerBusinessObject(element));
			if (options.containsAll(droppedObjects)) {
				if (select.isMultiple()) {
					List<?> currentSelection = select.getSelection();
					if (!currentSelection.containsAll(droppedObjects)) {
						ArrayList<Object> newSelection = new ArrayList<>(currentSelection);
						newSelection.addAll(droppedObjects);
						select.setAsSelection(newSelection);
					} else {
						InfoService.showWarning(I18NConstants.NO_DROP_ALREADY_SELECTED);
					}
				} else {
					if (droppedObjects.size() == 1) {
						select.setAsSingleSelection(CollectionUtils.extractSingleton(droppedObjects));
					}
				}
				return;
			}
		}

		InfoService.showWarning(com.top_logic.layout.dnd.I18NConstants.DROP_NOT_POSSIBLE);
	}

}
