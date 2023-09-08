/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.saveas;

import java.io.File;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.gui.layout.upload.ModularFileNameConstraint;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.SelectableFormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Creates the {@link FormContext} for entering the file name.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SaveScriptAsFormBuilder<C extends SaveScriptAsFormBuilder.Config<?>>
		extends AbstractConfiguredInstance<C> implements ModelBuilder {

	/** The name of the {@link StringField} for the file name. */
	public static final String FIELD_FILE_NAME = "file-name";

	/** {@link ConfigurationItem} for the {@link SaveScriptAsFormBuilder}. */
	public interface Config<T extends SaveScriptAsFormBuilder<?>> extends PolymorphicConfiguration<T> {

		/** Property name of {@link #getAllowedEndings()}. */
		String ALLOWED_ENDINGS = "allowedEndings";

		/** The file name has to end with one of these. */
		@Name(ALLOWED_ENDINGS)
		@Format(CommaSeparatedStrings.class)
		List<String> getAllowedEndings();

	}

	private final List<String> _allowedEndings;

	/** {@link TypedConfiguration} constructor for {@link SaveScriptAsFormBuilder}. */
	public SaveScriptAsFormBuilder(InstantiationContext context, C config) {
		super(context, config);
		_allowedEndings = config.getAllowedEndings();
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent component) {
		return createFormContext((SelectableFormComponent) component, (File) businessModel);
	}

	private Object createFormContext(SelectableFormComponent formComponent, File selectedFileOrFolder) {
		FormContext formContext = new FormContext(formComponent);
		formContext.addMember(createFileNameField(formComponent, selectedFileOrFolder));
		return formContext;
	}

	private StringField createFileNameField(SelectableFormComponent formComponent, File selectedFileOrFolder) {
		StringField fileNameField = FormFactory.newStringField(FIELD_FILE_NAME);
		if (!selectedFileOrFolder.isDirectory()) {
			fileNameField.initializeField(selectedFileOrFolder.getName());
			formComponent.setSelected(selectedFileOrFolder);
		}
		fileNameField.setMandatory(true);
		addFileNameConstraint(fileNameField);
		fileNameField.addValueListener(getFileNameFieldListener(formComponent));
		return fileNameField;
	}

	private void addFileNameConstraint(StringField field) {
		if (!CollectionUtil.isEmptyOrNull(_allowedEndings)) {
			field.addConstraint(new ModularFileNameConstraint(_allowedEndings));
		}
	}

	private ValueListener getFileNameFieldListener(SelectableFormComponent component) {
		return (field, oldValue, newValue) -> onFileNameInput(component, (String) newValue);
	}

	private boolean onFileNameInput(SelectableFormComponent component, String fileName) {
		File folderViewSelection = (File) component.getModel();
		return component.setSelected(getSelectedFile(folderViewSelection, fileName));
	}

	private File getSelectedFile(File selectedFileOrFolder, String fileName) {
		if (selectedFileOrFolder.isDirectory()) {
			return new File(selectedFileOrFolder, fileName);
		}
		File currentDirectory = selectedFileOrFolder.getParentFile();
		return new File(currentDirectory, fileName);
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return model instanceof File;
	}

}
