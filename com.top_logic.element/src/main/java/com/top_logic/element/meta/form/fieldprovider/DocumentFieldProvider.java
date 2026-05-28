/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import com.top_logic.common.webfolder.model.DocumentContext;
import com.top_logic.common.webfolder.ui.DocumentFieldFactory;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.element.meta.kbbased.storage.DocumentStorage;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of type {@link Document}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DocumentFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember createFormField(EditContext editContext, String fieldName) {
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();
		boolean isSearch = editContext.isSearchUpdate();

		if (isSearch) {
			return null;
		}

		FormField result;
		if (editContext.isDerived()) {
			DataField dataField = FormFactory.newDataField(fieldName);
			result = dataField;
		} else {
			DocumentContext context = getDocumentContext(editContext);
			result = DocumentFieldFactory.createDocumentField(fieldName, context);

			if (context.getFolder() == null) {
				isDisabled = true;
			}
		}
		result.setLabel(editContext.getLabelKey());
		result.setImmutable(isDisabled);
		result.setMandatory(isMandatory);
		return result;
	}

	private static DocumentContext getDocumentContext(EditContext editContext) {
		Document document = null;
		Document template = null;
		WebFolder theFolder = null;

		TLObject baseObject = editContext.getObject();

		if (baseObject != null) {
			document = (Document) editContext.getCorrectValues();
			// Note: This implementation does not work in generalized edit contexts, because it
			// requires access to the underlying attribute and its storage implementation.
			StorageImplementation storage =
				AttributeOperations.getStorageImplementation(((AttributeUpdate) editContext).getAttribute());
			if (storage instanceof DocumentStorage) {
				DocumentStorage<?> documentStorage = (DocumentStorage<?>) storage;

				template = documentStorage.locateTemplate(baseObject);
				theFolder = documentStorage.locateFolder(baseObject);
			}
		}

		DocumentContext theInfo =
			new DocumentContext(document, null, theFolder, template);

		return theInfo;
	}

}
