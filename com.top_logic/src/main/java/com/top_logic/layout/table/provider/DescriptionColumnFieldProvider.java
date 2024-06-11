/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import com.top_logic.basic.Logger;
import com.top_logic.common.webfolder.model.FolderContent;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.table.model.AbstractFieldProvider;
import com.top_logic.layout.table.model.FieldProvider;

/**
 * {@link FieldProvider} creating {@link FormFactory#newStringField(String, Object, boolean)} with
 * {@link ValueListener} to commit changes directly.
 * 
 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
 */
public class DescriptionColumnFieldProvider extends AbstractFieldProvider {

	/**
	 * Singleton {@link DescriptionColumnFieldProvider} instance.
	 */
	public static final DescriptionColumnFieldProvider INSTANCE = new DescriptionColumnFieldProvider();

	private static final String DESCRIPTION_SIZE_CONSTRAINT_CSS_CLASS = "descriptionSizeConstraint";

	private final boolean _immutable;

	private DescriptionColumnFieldProvider() {
		_immutable = false;
	}

	private DescriptionColumnFieldProvider(boolean immutable) {
		_immutable = immutable;

	}

	@Override
	public FormMember createField(Object model, Accessor accessor, String property) {
		Object description = accessor.getValue(model, property);
		StringField descriptionField =
			FormFactory.newStringField(getFieldName(model, accessor, property), description, _immutable);
		descriptionField.setCssClasses(DESCRIPTION_SIZE_CONSTRAINT_CSS_CLASS);

		if (!descriptionField.isImmutable()) {
			setValueListener(descriptionField, model);
		}
		return descriptionField;
	}

	/**
	 * Sets a {@link ValueListener} to the {@link StringField} if the model content is a
	 * {@link Document} or a {@link WebFolder}. Otherwise the {@link StringField} has to be
	 * immutable.
	 * 
	 * @param descriptionField
	 *        The {@link StringField} containing the description of the object.
	 * @param model
	 *        The model of the table cell value.
	 * 
	 */
	private void setValueListener(StringField descriptionField, Object model) {
		boolean immutable = true;
		if(model instanceof FolderContent){
			Object content = ((FolderContent) model).getContent();
			if (content instanceof Document) {
				addValueListener(descriptionField, ((Document) content).getDocumentVersion(), false);
				immutable = false;
			} else if (content instanceof WebFolder) {
				addValueListener(descriptionField, content, true);
				immutable = false;
			}
		}
		descriptionField.setImmutable(immutable);
	}

	/**
	 * Adds a {@link ValueListener} to the field to commit changes.
	 * 
	 * @param descriptionField
	 *        The {@link StringField} containing the description.
	 * @param content
	 *        The {@link DocumentVersion} or {@link WebFolder} to update.
	 * @param isWebFolder
	 *        content is a {@link WebFolder}
	 */
	private void addValueListener(StringField descriptionField, Object content, boolean isWebFolder) {

		descriptionField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				try (Transaction tx =
					PersistencyLayer.getKnowledgeBase().beginTransaction()) {
					if (isWebFolder) {
						((WebFolder) content).setDescription(newValue.toString());
					} else {
						((DocumentVersion) content).setDescription(newValue.toString());
					}

					tx.commit();

				} catch (KnowledgeBaseException ex) {
					Logger.error("Failed to update description of " + content, ex, this);
				}
			}
		});

	}
}
