/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.text.DateFormat;
import java.text.Format;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandBuilder;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.TimeControlProvider;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.mig.html.SelectionModel;

/**
 * {@link FormFactorySpi} which produces standard instances.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultFormFactory extends FormFactorySpi {

	/** The class class for fields whose value is a {@link Number}. */
	public static final String CSS_CLASS_NUMBER_FIELD = "num";

	@Override
	public BooleanField newBooleanField(String name, boolean mandatory, boolean immutable, Constraint constraint) {
		BooleanField result = new BooleanField(name, mandatory, immutable, constraint);
		setInitialValue(result);
		return result;
	}

	@Override
	public ComplexField newComplexField(String name, Format format, boolean ignoreWhiteSpace, boolean mandatory,
			boolean immutable, Constraint constraint) {
		ComplexField result = new ComplexField(name, format, ignoreWhiteSpace, mandatory, immutable, constraint);
		setInitialValue(result);
		return result;
	}

	@Override
	public GalleryField newGalleryField(String name, DisplayDimension width, DisplayDimension height) {
		GalleryField galleryField =
			new GalleryField(name, width, height);
		setInitialValue(galleryField);
		return galleryField;
	}

	@Override
	public GalleryField newGalleryField(String name) {
		GalleryField galleryField =
			new GalleryField(name);
		setInitialValue(galleryField);
		return galleryField;
	}

	@Override
	public IntField newIntField(String name, boolean mandatory, boolean immutable, Constraint constraint) {
		IntField result = new IntField(name, mandatory, immutable, constraint);
		setInitialValue(result);
		result.setCssClasses(CSS_CLASS_NUMBER_FIELD);
		return result;
	}

	@Override
	public HiddenField newHiddenField(String name) {
		HiddenField result = new HiddenField(name);
		setInitialValue(result);
		return result;
	}

	@Override
	public ListField newListField(String name, ListModel listModel, ListSelectionModel selectionModel) {
		ListField result = new ListField(name, listModel, selectionModel);
		setInitialValue(result);
		return result;
	}

	@Override
	public TableField newTableField(String name, Mapping<FormMember, String> configNameMapping) {
		TableField result = new TableField(name, configNameMapping, true);
		setInitialValue(result);
		return result;
	}

	@Override
	public TableField newTableField(String name, ConfigKey configKey) {
		TableField result = new TableField(name, configKey, true);
		setInitialValue(result);
		return result;
	}

	@Override
	public TreeTableField newTreeTableField(String name, Mapping<FormMember, String> configNameMapping) {
		TreeTableField result = new TreeTableField(name, configNameMapping);
		setInitialValue(result);
		return result;
	}

	@Override
	public TreeTableField newTreeTableField(String name, ConfigKey configKey) {
		TreeTableField result = new TreeTableField(name, configKey);
		setInitialValue(result);
		return result;
	}

	@Override
	public TreeField newTreeField(String name, TreeUIModel treeModel, SelectionModel selectionModel,
			TreeRenderer renderer) {
		TreeField result = new TreeField(name, treeModel, selectionModel, renderer);
		setInitialValue(result);
		return result;
	}

	@Override
	public DataField newDataField(String name, boolean multiple) {
		DataField result = new DataField(name, multiple);
		setInitialValue(result);
		return result;
	}

	@Override
	public SelectField createSelectField(String name, OptionModel<?> optionModel, boolean multiple, boolean mandatory,
			boolean immutable, Constraint constraint) {
		SelectField result =
			new SelectField(name, optionModel, multiple, mandatory, immutable, constraint);
		setInitialValue(result);
		return result;
	}

	@Override
	public SelectionTableField newSelectionTableField(String name, List options, Accessor columnAccessor,
			boolean multiple, boolean mandatory, boolean immutable, Constraint constraint) {
		SelectionTableField result =
			new SelectionTableField(name, options, columnAccessor, multiple, mandatory, immutable, constraint);
		setInitialValue(result);
		return result;
	}

	@Override
	public StringField newStringField(String name, boolean mandatory, boolean immutable, Constraint constraint) {
		StringField result = new StringField(name, mandatory, immutable, constraint);
		setInitialValue(result);
		return result;
	}

	@Override
	public PasswordField newPasswordField(String name, boolean mandatory, boolean immutable, Constraint constraint) {
		PasswordField result = new PasswordField(name, mandatory, immutable, constraint);
		setInitialValue(result);
		return result;
	}

	@Override
	public ExpandableStringField newExpandableStringField(String name, boolean mandatory, boolean immutable,
			Constraint constraint) {
		ExpandableStringField result = new ExpandableStringField(name, mandatory, immutable, constraint);
		setInitialValue(result);
		return result;
	}

	@Override
	public ComplexField newNumberField(String name, Format format, boolean ignoreWhiteSpace, boolean mandatory,
			boolean immutable, Constraint constraint) {
		ComplexField numberField = newComplexField(name, format, ignoreWhiteSpace, mandatory, immutable, constraint);

		numberField.setCssClasses(CSS_CLASS_NUMBER_FIELD);
		return numberField;
	}

	@Override
	public ComplexField newDateField(String name, boolean ignoreWhiteSpace, boolean mandatory, boolean immutable,
			Constraint constraint) {
		Formatter htmlFormatter = HTMLFormatter.getInstance();
		ComplexField dateField =
			newComplexField(name, htmlFormatter.getDateFormat(), ignoreWhiteSpace, mandatory, immutable, constraint);

		initDateInstance(dateField, htmlFormatter.getShortDateFormat());
		return dateField;
	}

	private static void initDateInstance(ComplexField field, DateFormat parser) {
		if (ComplexField.GLOBAL_DATE_CONSTRAINT != null) {
			field.addConstraint(ComplexField.GLOBAL_DATE_CONSTRAINT);
		}

		if (parser != null) {
			field.setParser(parser);
		}
	}

	@Override
	public ComplexField newTimeField(String name, boolean ignoreWhiteSpace, boolean mandatory, boolean immutable,
			Constraint constraint) {
		DateFormat format = HTMLFormatter.getInstance().getShortTimeFormat();
		ComplexField dateField =
			newComplexField(name, format, ignoreWhiteSpace, mandatory, immutable,
				constraint);
		dateField.setControlProvider(TimeControlProvider.INSTANCE);

		return dateField;
	}

	@Override
	public ComplexField newLongField(String name, boolean ignoreWhiteSpace, boolean mandatory, boolean immutable,
			Constraint constraint) {
		Format numberFormat = HTMLFormatter.getInstance().getLongFormat();
		return newNumberField(name, numberFormat, ignoreWhiteSpace, mandatory, immutable, constraint);
	}

	@Override
	public ComplexField newDoubleField(String name, boolean ignoreWhiteSpace, boolean mandatory, boolean immutable,
			Constraint constraint) {
		Format format = HTMLFormatter.getInstance().getDoubleFormat();
		return newNumberField(name, format, ignoreWhiteSpace, mandatory, immutable, constraint);
	}

	@Override
	public ExecutableCommandField newCommandField(String name, Command command, ExecutabilityModel executability,
			CheckScope checkScope) {
		return new ExecutableCommandField(name, command, executability, checkScope);
	}

	@Override
	public ExecutableCommandField newCommandField(String name, CommandBuilder commandBuilder,
			ExecutabilityModel executability,
			CheckScope checkScope) {
		return new ExecutableCommandField(name, commandBuilder, executability, checkScope);
	}

}

