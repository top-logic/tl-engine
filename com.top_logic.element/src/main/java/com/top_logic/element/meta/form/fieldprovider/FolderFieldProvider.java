/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.DefaultAttributeFormFactory;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FolderField;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of type {@link WebFolder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FolderFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember createFormField(EditContext editContext, String fieldName) {
		final TLObject attributedObject = editContext.getObject();

		if (attributedObject == null) {
			return null;
		}

		WebFolder folder = (WebFolder) editContext.getCorrectValues();
		if (folder == null) {
			return null;
		}
		ResKey label = editContext.getLabelKey();
		FolderField resultMember =
			FolderField.createFolderField(fieldName, folder, label,
				DefaultAttributeFormFactory.ATTRIBUTED_CONFIG_NAME_MAPPING);
		resultMember.setLabel(label);
		return resultMember;
	}

	@Override
	public boolean renderWholeLine(EditContext editContext) {
		return true;
	}

}
