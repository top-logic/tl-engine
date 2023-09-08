/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.text.Format;
import java.util.Date;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import com.top_logic.base.security.util.Password;
import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandBuilder;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;

/**
 * {@link FormFactorySpi} creates {@link AbstractFormField}. Used as implementation of
 * {@link FormFactory}
 * 
 * @see FormFactory
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class FormFactorySpi {

	/**
	 * Creates a {@link BooleanField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param mandatory
	 *        see {@link FormField#isMandatory()}
	 * @param immutable
	 *        see {@link FormMember#isImmutable()}
	 * @param constraint
	 *        see {@link FormField#getConstraints()}
	 */
	public abstract BooleanField newBooleanField(String name, boolean mandatory, boolean immutable,
			Constraint constraint);

	/**
	 * Creates a {@link ComplexField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param format
	 *        see {@link ComplexField#getFormat()}
	 * @param ignoreWhiteSpace
	 *        see {@link ComplexField#isWhiteSpaceIgnored()}
	 * @param mandatory
	 *        see {@link FormField#isMandatory()}
	 * @param immutable
	 *        see {@link FormMember#isImmutable()}
	 * @param constraint
	 *        see {@link FormField#getConstraints()}
	 */
	public abstract ComplexField newComplexField(String name, Format format, boolean ignoreWhiteSpace,
			boolean mandatory, boolean immutable, Constraint constraint);

	/**
	 * Creates a {@link GalleryField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param width
	 *        Width of the {@link GalleryField}
	 * @param height
	 *        Height the {@link GalleryField}
	 */
	public abstract GalleryField newGalleryField(String name, DisplayDimension width, DisplayDimension height);

	/**
	 * Creates a {@link GalleryField} with default size.
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 */
	public abstract GalleryField newGalleryField(String name);

	/**
	 * Creates a {@link BooleanField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param mandatory
	 *        see {@link FormField#isMandatory()}
	 * @param immutable
	 *        see {@link FormMember#isImmutable()}
	 * @param constraint
	 *        see {@link FormField#getConstraints()}
	 */
	public abstract IntField newIntField(String name, boolean mandatory, boolean immutable, Constraint constraint);

	/**
	 * Creates a {@link HiddenField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 */
	public abstract HiddenField newHiddenField(String name);

	/**
	 * Creates a {@link ListField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param listModel
	 *        see {@link ListField#getListModel()}
	 * @param selectionModel
	 *        see {@link ListField#getSelectionModel()}
	 */
	public abstract ListField newListField(String name, ListModel listModel, ListSelectionModel selectionModel);

	/**
	 * Creates a {@link TableField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param configNameMapping
	 *        The algorithm to compute the field name for storing personalization information.
	 */
	public abstract TableField newTableField(String name, Mapping<FormMember, String> configNameMapping);

	/**
	 * Creates a {@link TableField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 */
	public abstract TableField newTableField(String name, ConfigKey configKey);

	/**
	 * Creates a {@link TreeTableField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 */
	public final TreeTableField newTreeTableField(String name) {
		return newTreeTableField(name, FormMember.QUALIFIED_NAME_MAPPING);
	}

	/**
	 * Creates a {@link TreeTableField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param configNameMapping
	 *        The algorithm to compute the field name for storing personalization information.
	 */
	public abstract TreeTableField newTreeTableField(String name, Mapping<FormMember, String> configNameMapping);

	/**
	 * Creates a {@link TreeTableField}.
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 */
	public abstract TreeTableField newTreeTableField(String name, ConfigKey configKey);

	/**
	 * Creates a {@link TreeField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param treeModel
	 *        see {@link TreeField#getTreeModel()}
	 * @param selectionModel
	 *        see {@link TreeField#getSelectionModel()}
	 * @param renderer
	 *        see {@link TreeField#getTreeRenderer()}
	 */
	public abstract TreeField newTreeField(String name, TreeUIModel treeModel, SelectionModel selectionModel,
			TreeRenderer renderer);

	/**
	 * Creates a {@link DataField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param multiple
	 *        see {@link DataField#isMultiple()}
	 */
	public abstract DataField newDataField(String name, boolean multiple);

	/**
	 * Creates a {@link SelectField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param optionModel
	 *        see {@link SelectField#getOptionModel()}
	 * @param multiple
	 *        see {@link SelectField#isMultiple()}
	 * @param mandatory
	 *        see {@link FormField#isMandatory()}
	 * @param immutable
	 *        see {@link FormMember#isImmutable()}
	 * @param constraint
	 *        see {@link FormField#getConstraints()}
	 */
	public abstract SelectField createSelectField(String name, OptionModel<?> optionModel, boolean multiple,
			boolean mandatory, boolean immutable, Constraint constraint);

	/**
	 * Creates a {@link SelectionTableField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param options
	 *        see {@link SelectField#getOptions()}
	 * @param columnAccessor
	 *        see {@link SelectionTableField#getOptionAccessor()}
	 * @param multiple
	 *        see {@link SelectField#isMultiple()}
	 * @param mandatory
	 *        see {@link FormField#isMandatory()}
	 * @param immutable
	 *        see {@link FormMember#isImmutable()}
	 * @param constraint
	 *        see {@link FormField#getConstraints()}
	 */
	public abstract SelectionTableField newSelectionTableField(String name, List options, Accessor columnAccessor,
			boolean multiple, boolean mandatory, boolean immutable, Constraint constraint);

	/**
	 * Creates a {@link StringField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param mandatory
	 *        see {@link FormField#isMandatory()}
	 * @param immutable
	 *        see {@link FormMember#isImmutable()}
	 * @param constraint
	 *        see {@link FormField#getConstraints()}
	 */
	public abstract StringField newStringField(String name, boolean mandatory, boolean immutable, Constraint constraint);

	/**
	 * Creates a {@link FormField} for entering a password.
	 * 
	 * <p>
	 * The value of such field is of type {@link Password}.
	 * </p>
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param mandatory
	 *        see {@link FormField#isMandatory()}
	 * @param immutable
	 *        see {@link FormMember#isImmutable()}
	 * @param constraint
	 *        see {@link FormField#getConstraints()}
	 */
	public abstract PasswordField newPasswordField(String name, boolean mandatory, boolean immutable,
			Constraint constraint);

	/**
	 * Creates a {@link ExpandableStringField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param mandatory
	 *        see {@link FormField#isMandatory()}
	 * @param immutable
	 *        see {@link FormMember#isImmutable()}
	 * @param constraint
	 *        see {@link FormField#getConstraints()}
	 */
	public abstract ExpandableStringField newExpandableStringField(String name, boolean mandatory, boolean immutable,
			Constraint constraint);

	/**
	 * Initializes the value of the field with <code>null</code>. This ensures that fields which
	 * normalize <code>null</code> have initially the normalized value.
	 * 
	 * @param field
	 *        the field to set <code>null</code> as value.
	 */
	protected final void setInitialValue(AbstractFormField field) {
		field.initializeField(null);
	}

	/**
	 * Field to hold and parse numbers.
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param format
	 *        see {@link ComplexField#getFormat()}
	 * @param ignoreWhiteSpace
	 *        see {@link ComplexField#isWhiteSpaceIgnored()}
	 * @param mandatory
	 *        see {@link FormField#isMandatory()}
	 * @param immutable
	 *        see {@link FormMember#isImmutable()}
	 * @param constraint
	 *        see {@link FormField#getConstraints()}
	 */
	public abstract ComplexField newNumberField(String name, Format format, boolean ignoreWhiteSpace,
			boolean mandatory, boolean immutable, Constraint constraint);

	/**
	 * Creates a number field with a long format
	 * 
	 * @see #newNumberField(String, Format, boolean, boolean, boolean, Constraint)
	 */
	public abstract ComplexField newLongField(String name, boolean ignoreWhiteSpace, boolean mandatory,
			boolean immutable, Constraint constraint);

	/**
	 * Creates a number field with a double format
	 * 
	 * @see #newNumberField(String, Format, boolean, boolean, boolean, Constraint)
	 */
	public abstract ComplexField newDoubleField(String name, boolean ignoreWhiteSpace, boolean mandatory,
			boolean immutable, Constraint constraint);

	/**
	 * Creates a field holding a {@link Date}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param ignoreWhiteSpace
	 *        see {@link ComplexField#isWhiteSpaceIgnored()}
	 * @param mandatory
	 *        see {@link FormField#isMandatory()}
	 * @param immutable
	 *        see {@link FormMember#isImmutable()}
	 * @param constraint
	 *        see {@link FormField#getConstraints()}
	 */
	public abstract ComplexField newDateField(String name, boolean ignoreWhiteSpace, boolean mandatory,
			boolean immutable, Constraint constraint);

	/**
	 * Creates a field holding the timeaspect of a {@link Date}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param ignoreWhiteSpace
	 *        see {@link ComplexField#isWhiteSpaceIgnored()}
	 * @param mandatory
	 *        see {@link FormField#isMandatory()}
	 * @param immutable
	 *        see {@link FormMember#isImmutable()}
	 * @param constraint
	 *        see {@link FormField#getConstraints()}
	 */
	public abstract ComplexField newTimeField(String name, boolean ignoreWhiteSpace, boolean mandatory,
			boolean immutable, Constraint constraint);

	/**
	 * Creates a {@link CommandField} for the given {@link Command} delegate.
	 * 
	 * @param name
	 *        See {@link FormMember#getName()}.
	 * @param command
	 *        The {@link Command} to invoke.
	 * @param executability
	 *        The {@link ExecutabilityModel} determining whether the command can be triggered by the
	 *        user.
	 * @param checkScope
	 *        The {@link CheckScope} to validate before executing the command.
	 */
	public abstract ExecutableCommandField newCommandField(String name, Command command,
			ExecutabilityModel executability, CheckScope checkScope);

	/**
	 * Creates a {@link CommandField} for the given {@link Command} delegate.
	 * 
	 * @param name
	 *        See {@link FormMember#getName()}.
	 * @param commandBuilder
	 *        The builder for the {@link Command} to invoke.
	 * @param executability
	 *        The {@link ExecutabilityModel} determining whether the command can be triggered by the
	 *        user.
	 * @param checkScope
	 *        The {@link CheckScope} to validate before executing the command.
	 */
	public abstract ExecutableCommandField newCommandField(String name, CommandBuilder commandBuilder,
			ExecutabilityModel executability, CheckScope checkScope);

}

