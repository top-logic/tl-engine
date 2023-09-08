/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.awt.Dimension;
import java.text.Format;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Configuration;
import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.knowledge.gui.layout.upload.FileNameConstraint;
import com.top_logic.knowledge.gui.layout.upload.FileNameStrategy;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandBuilder;
import com.top_logic.layout.basic.ComponentCommand;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.basic.check.CheckScopeProvider;
import com.top_logic.layout.basic.check.DefaultCheckScope;
import com.top_logic.layout.basic.check.NoCheckScopeProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.DisplayImageControlProvider;
import com.top_logic.layout.form.control.ValueDisplayControl;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.DefaultTreeOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.SelectionTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link FormFactory} is a service class used to create new instances of
 * {@link AbstractFormField}. The actual creation is delegated to an instance of
 * {@link FormFactorySpi}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FormFactory {

	/**
	 * Configuration attribute to configure the class of the actual {@link FormFactorySpi}
	 * implementation.
	 */
	private static final String ATTRIBUTE_IMPLEMENTATION_CLASS = "implementationClass";

	/** Implementation to dispatch actual {@link FormField} creation to. */
	private static final FormFactorySpi implementation;

	static {
		IterableConfiguration configuration = Configuration.getConfiguration(FormFactory.class);
		FormFactorySpi configuredImplementation;
		try {
			configuredImplementation =
				ConfigUtil.getInstanceWithClassDefault(FormFactorySpi.class, configuration.getProperties(),
					ATTRIBUTE_IMPLEMENTATION_CLASS, DefaultFormFactory.class);
		} catch (ConfigurationException ex) {
			String errorMsg = "Unable to instantiate " + FormFactorySpi.class.getName() + " implementation";
			Logger.error(errorMsg, ex, FormFactory.class);
			configuredImplementation = new DefaultFormFactory();
		}
		implementation = configuredImplementation;
	}

	/** @see AbstractFormField#MANDATORY */
	public static final boolean MANDATORY = AbstractFormField.MANDATORY;

	/** @see AbstractFormField#IMMUTABLE */
	public static final boolean IMMUTABLE = AbstractFormField.IMMUTABLE;

	/** @see AbstractFormField#NO_CONSTRAINT */
	public static final Constraint NO_CONSTRAINT = AbstractFormField.NO_CONSTRAINT;

	/** @see SelectField#MULTIPLE */
	public static final boolean MULTIPLE = SelectField.MULTIPLE;

	/** @see ComplexField#IGNORE_WHITE_SPACE */
	public static final boolean IGNORE_WHITE_SPACE = ComplexField.IGNORE_WHITE_SPACE;

	/**
	 * Creates a new {@link BooleanField} with the given name
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 */
	public static BooleanField newBooleanField(String name) {
		return newBooleanField(name, !IMMUTABLE);
	}

	/**
	 * Creates a new {@link BooleanField} with the given name
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param immutable
	 *        see {@link FormMember#isImmutable()}
	 */
	public static BooleanField newBooleanField(String name, boolean immutable) {
		return newBooleanField(name, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static BooleanField newBooleanField(String name, Object initialValue, boolean immutable) {
		return newBooleanField(name, initialValue, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static BooleanField newBooleanField(String name, Object initialValue, boolean mandatory, boolean immutable,
			Constraint constraint) {
		BooleanField booleanField = newBooleanField(name, mandatory, immutable, constraint);
		booleanField.initializeField(initialValue);
		return booleanField;
	}

	/**
	 * Creates a new {@link GalleryField} with the given name.
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param width
	 *        Width of the {@link GalleryField}.
	 * @param height
	 *        Height of the {@link GalleryField}.
	 */
	public static GalleryField newGalleryField(String name, DisplayDimension width, DisplayDimension height) {
		return implementation.newGalleryField(name, width, height);
	}

	/**
	 * Creates a new {@link GalleryField} with the given name and default size.
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 */
	public static GalleryField newGalleryField(String name) {
		return implementation.newGalleryField(name);
	}

	/**
	 * Creates a new {@link BooleanField} with the given name
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
	public static BooleanField newBooleanField(String name, boolean mandatory, boolean immutable,
			Constraint constraint) {
		return implementation.newBooleanField(name, mandatory, immutable, constraint);
	}

	/**
	 * Creates a new {@link IntField} with the given name
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 */
	public static IntField newIntField(String name) {
		return newIntField(name, !IMMUTABLE);
	}

	/**
	 * Creates a new {@link IntField} with the given name
	 * 
	 * @param name
	 *        see {@link FormMember#getName()} see {@link FormField#isMandatory()}
	 * @param immutable
	 *        see {@link FormMember#isImmutable()}
	 */
	public static IntField newIntField(String name, boolean immutable) {
		return newIntField(name, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static IntField newIntField(String name, Object initialValue, boolean mandatory, boolean immutable,
			Constraint constraint) {
		IntField intField = newIntField(name, mandatory, immutable, constraint);
		intField.initializeField(initialValue);
		return intField;
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static IntField newIntField(String name, Object initialValue, boolean immutable) {
		return newIntField(name, initialValue, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Creates a new {@link IntField} with the given name
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
	public static IntField newIntField(String name, boolean mandatory, boolean immutable, Constraint constraint) {
		return implementation.newIntField(name, mandatory, immutable, constraint);
	}

	/**
	 * Creates a new {@link HiddenField} with the given name
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 */
	public static HiddenField newHiddenField(String name) {
		return implementation.newHiddenField(name);
	}

	/**
	 * Creates a new {@link HiddenField} with a pre-initialized value.
	 * 
	 * @param name
	 *        See {@link FormMember#getName()}
	 * @param value
	 *        The value of the created field.
	 * @return A reference to the new field.
	 */
	public static HiddenField newHiddenField(String name, Object value) {
		HiddenField result = newHiddenField(name);
		result.initializeField(value);
		return result;
	}

	/**
	 * Creates a {@link FormField} that is display-only without an input element.
	 * 
	 * @param name
	 *        See {@link FormMember#getName()}
	 * @param value
	 *        The value of the created field.
	 * @return A reference to the new field.
	 */
	public static FormField newDisplayField(String name, Object value) {
		HiddenField result = newHiddenField(name);
		result.initializeField(value);
		result.setControlProvider(ValueDisplayControl.ValueDisplay.INSTANCE);
		return result;
	}

	/**
	 * Creates a {@link FormField} that is displaying a picture.
	 * 
	 * @param name
	 *        See {@link FormMember#getName()}
	 * @param picture
	 *        The binary data object to be displayed as picture in an image tag.
	 * @param dimension
	 *        Requested maximum dimension of the picture.
	 * 
	 * @return A reference to the new field.
	 */
	public static FormField newPictureField(String name, BinaryData picture, Dimension dimension) {
		FormField result = FormFactory.newHiddenField(name, picture);
		result.setControlProvider(new DisplayImageControlProvider(dimension));
		return result;
	}

	/**
	 * Creates a {@link FormField} that is display-only without an input element.
	 * 
	 * @param name
	 *        See {@link FormMember#getName()}
	 * @param value
	 *        The value of the created field.
	 * @param renderer
	 *        The custom renderer to use.
	 * @return A reference to the new field.
	 */
	public static <T> FormField newDisplayField(String name, T value, Renderer<T> renderer) {
		HiddenField result = newHiddenField(name);
		result.initializeField(value);
		result.setControlProvider(new ValueDisplayControl.ValueDisplay(renderer));
		return result;
	}

	/**
	 * Creates a new {@link ComplexField} with the given name
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param format
	 *        see {@link ComplexField#getFormat()}
	 */
	public static ComplexField newComplexField(String name, Format format) {
		return newComplexField(name, format, !IMMUTABLE);
	}

	/**
	 * Creates a new {@link ComplexField} with the given name
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param format
	 *        see {@link ComplexField#getFormat()}
	 * @param immutable
	 *        see {@link FormMember#isImmutable()}
	 */
	public static ComplexField newComplexField(String name, Format format, boolean immutable) {
		return newComplexField(name, format, IGNORE_WHITE_SPACE, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Creates a new {@link ComplexField} with the given name
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
	public static ComplexField newComplexField(String name, Format format, boolean ignoreWhiteSpace,
			boolean mandatory, boolean immutable, Constraint constraint) {
		return implementation.newComplexField(name, format, ignoreWhiteSpace, mandatory, immutable, constraint);
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static ComplexField newComplexField(String name, Format format, Object initialValue, boolean immutable) {
		return newComplexField(name, format, initialValue, IGNORE_WHITE_SPACE, !MANDATORY, immutable,
			NO_CONSTRAINT);
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static ComplexField newComplexField(String name, Format format, Object initialValue,
			boolean ignoreWhiteSpace, boolean mandatory, boolean immutable, Constraint constraint) {
		ComplexField complexField = newComplexField(name, format, ignoreWhiteSpace, mandatory, immutable, constraint);
		complexField.initializeField(initialValue);
		return complexField;
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static ComplexField newDateField(String name, Object initialValue, boolean immutable) {
		return newDateField(name, initialValue, IGNORE_WHITE_SPACE, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static ComplexField newDateField(String name, Object initialValue, boolean ignoreWhiteSpace,
			boolean mandatory, boolean immutable, Constraint constraint) {
		ComplexField dateField = implementation.newDateField(name, ignoreWhiteSpace, mandatory, immutable, constraint);
		dateField.initializeField(initialValue);
		return dateField;
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static ComplexField newTimeField(String name, Object initialValue, boolean immutable) {
		return newTimeField(name, initialValue, IGNORE_WHITE_SPACE, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static ComplexField newTimeField(String name, Object initialValue, boolean ignoreWhiteSpace,
			boolean mandatory, boolean immutable, Constraint constraint) {
		ComplexField timeField = implementation.newTimeField(name, ignoreWhiteSpace, mandatory, immutable, constraint);
		timeField.initializeField(initialValue);
		return timeField;
	}

	/**
	 * Factory method for creating an input field for double values.
	 */
	public static ComplexField newDoubleField(String name, Object initialValue, boolean immutable) {
		return FormFactory.newDoubleField(name, initialValue, IGNORE_WHITE_SPACE, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Factory method for creating an input field for double values.
	 */
	public static ComplexField newDoubleField(String name, Object initialValue, boolean ignoreWhiteSpace,
			boolean mandatory, boolean immutable, Constraint constraint) {
		ComplexField doubleField =
			implementation.newDoubleField(name, ignoreWhiteSpace, mandatory, immutable, constraint);
		doubleField.initializeField(initialValue);
		return doubleField;
	}

	/**
	 * Factory method for creating an input field for long values.
	 */
	public static ComplexField newLongField(String name, Object initialValue, boolean immutable) {
		return FormFactory.newLongField(name, initialValue, IGNORE_WHITE_SPACE, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Factory method for creating an input field for long values.
	 */
	public static ComplexField newLongField(String name, Object initialValue, boolean ignoreWhiteSpace,
			boolean mandatory, boolean immutable, Constraint constraint) {
		ComplexField longField =
			implementation.newLongField(name, ignoreWhiteSpace, mandatory, immutable, constraint);
		longField.initializeField(initialValue);
		return longField;
	}

	/**
	 * Factory method for creating an input field for numeric values.
	 */
	public static ComplexField newNumberField(String name, Format aFormat, Object initialValue, boolean immutable) {
		return FormFactory.newNumberField(name, aFormat, initialValue, IGNORE_WHITE_SPACE, !MANDATORY, immutable,
			NO_CONSTRAINT);
	}

	/**
	 * Factory method for creating an input field for numeric values.
	 */
	public static ComplexField newNumberField(String name, Format format, Object initialValue,
			boolean ignoreWhiteSpace, boolean mandatory, boolean immutable, Constraint constraint) {
		ComplexField numberField =
			implementation.newNumberField(name, format, ignoreWhiteSpace, mandatory, immutable, constraint);

		numberField.initializeField(initialValue);
		return numberField;
	}

	/**
	 * Creates a new {@link ListField} with the given name and a {@link DefaultListModel}
	 * 
	 * @see FormFactory#newListField(String, ListModel)
	 * 
	 */
	public static ListField newListField(String name) {
		return newListField(name, new DefaultListModel());
	}

	/**
	 * Creates a new {@link ListField} with the given name, the given {@link ListModel}, and a
	 * {@link DefaultListSelectionModel}.
	 * 
	 * @see FormFactory#newListField(String, ListModel, ListSelectionModel)
	 */
	public static ListField newListField(String name, ListModel listModel) {
		return newListField(name, listModel, new DefaultListSelectionModel());
	}

	/**
	 * Creates a new {@link ListField} with the given name
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param listModel
	 *        see {@link ListField#getListModel()}
	 * @param selectionModel
	 *        see {@link ListField#getSelectionModel()}
	 */
	public static ListField newListField(String name, ListModel listModel, ListSelectionModel selectionModel) {
		return implementation.newListField(name, listModel, selectionModel);
	}

	public static TableField newTableField(String aName, SelectField aMember, FormContainer container,
			FieldProvider aFieldProvider, String[] someColumns, ControlProvider[] someProviders, Accessor anAccessor) {
		TableConfiguration theManager = TableConfiguration.table();
		theManager.getDefaultColumn().setAccessor(anAccessor);
		SelectionTableModel theModel = new SelectionTableModel(aMember, someColumns, theManager);

		for (int thePos = 0; thePos < someColumns.length; thePos++) {
			ControlProvider theProvider = someProviders[thePos];

			if (theProvider == null) {
				theProvider = DefaultFormFieldControlProvider.INSTANCE;
			}

			TableField.addField(someColumns[thePos], theManager, aFieldProvider, theProvider);
		}

		return newTableField(aName, new FormTableModel(theModel, container));
	}

	public static TableField newTableField(String aName, FormContainer container, List someObjects,
			FieldProvider aFieldProvider, String[] someColumns, ControlProvider[] someProviders, Accessor anAccessor) {
		SelectField selectField = newSelectField(aName, someObjects, MULTIPLE, someObjects, !IMMUTABLE);
		return FormFactory.newTableField(aName, selectField, container, aFieldProvider, someColumns,
			someProviders, anAccessor);
	}

	/**
	 * Creates a new {@link TableField} with the given name and the given {@link TableModel}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param tableModel
	 *        see {@link TableField#getTableModel()}
	 */
	public static TableField newTableField(String name, TableModel tableModel) {
		TableField tableField = newTableField(name);
		tableField.setTableModel(tableModel);
		return tableField;
	}

	/**
	 * Creates a new {@link TableField} with the given name and the given {@link TableModel}
	 * 
	 * @param selectable
	 *        see {@link TableField#isSelectable()}
	 * 
	 * @see FormFactory#newTableField(String, TableModel)
	 */
	public static TableField newTableField(String name, TableModel tableModel, boolean selectable) {
		TableField tableField = newTableField(name, tableModel);
		tableField.setSelectable(selectable);
		return tableField;
	}

	/**
	 * Creates a new {@link TableField} with the given name
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 */
	public static TableField newTableField(String name) {
		return newTableField(name, FormMember.QUALIFIED_NAME_MAPPING);
	}

	/**
	 * Creates a new {@link TableField} with the given name and the given {@link ConfigKey}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param configKey
	 *        used to store values in the personal configuration.
	 */
	public static TableField newTableField(String name, ConfigKey configKey) {
		return implementation.newTableField(name, configKey);
	}

	/**
	 * Creates a new {@link TableField} with the given name and the given {@link ConfigKey}
	 * computation algorithm.
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param configNameMapping
	 *        The algorithm to compute the field name for storing personalization information.
	 */
	public static TableField newTableField(String name, Mapping<FormMember, String> configNameMapping) {
		return implementation.newTableField(name, configNameMapping);
	}

	/**
	 * Creates a new {@link TreeTableField} with the given name and the given
	 * {@link AbstractTreeTableModel}.
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param configKey
	 *        The {@link ConfigKey} used to store the personal settings of the result field.
	 * @param treeModel
	 *        The {@link AbstractTreeTableModel tree model} to display as table.
	 */
	public static TreeTableField newTreeTableField(String name, ConfigKey configKey, AbstractTreeTableModel<?> treeModel) {
		TreeTableField tableField = newTreeTableField(name, configKey);
		tableField.setTree(treeModel);
		return tableField;
	}

	/**
	 * Creates a new {@link TreeTableField} with the given name and the given
	 * {@link AbstractTreeTableModel}.
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param treeModel
	 *        The {@link AbstractTreeTableModel tree model} to display as table.
	 */
	public static TreeTableField newTreeTableField(String name, AbstractTreeTableModel<?> treeModel) {
		TreeTableField tableField = newTreeTableField(name);
		tableField.setTree(treeModel);
		return tableField;
	}

	/**
	 * Creates a new {@link TreeTableField} with the given name.
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 */
	public static TreeTableField newTreeTableField(String name) {
		return implementation.newTreeTableField(name);
	}

	/**
	 * Creates a new {@link TreeTableField} with the given name and the given {@link ConfigKey}.
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param configKey
	 *        used to store values in the personal configuration.
	 */
	public static TreeTableField newTreeTableField(String name, ConfigKey configKey) {
		return implementation.newTreeTableField(name, configKey);
	}

	/**
	 * /** Creates a new {@link TreeField} with the given name
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
	public static TreeField newTreeField(String name, TreeUIModel treeModel, SelectionModel selectionModel,
			TreeRenderer renderer) {
		return implementation.newTreeField(name, treeModel, selectionModel, renderer);
	}

	/**
	 * Creates a {@link DataField} with the given name
	 * 
	 * @param name
	 *        the name of the returned field
	 */
	public static DataField newDataField(String name) {
		return newDataField(name, !MULTIPLE);
	}

	/**
	 * Creates a {@link DataField} with the given name
	 * 
	 * @param name
	 *        the name of the returned field
	 * @param multiple
	 *        Whether the field should allow multi-upload.
	 */
	public static DataField newDataField(String name, boolean multiple) {
		return implementation.newDataField(name, multiple);
	}

	/**
	 * 
	 * Creates a {@link DataField} with the given name, and sets a
	 * {@link DataField#setFileNameConstraint(Constraint) filename constraint} based on the given
	 * {@link FileNameStrategy}, actually an instance of {@link FileNameConstraint}.
	 * 
	 * <p>
	 * The result field does not support multi-upload.
	 * </p>
	 * 
	 * @see #newDataField(String, boolean, FileNameStrategy)
	 */
	public static DataField newDataField(String aName, FileNameStrategy strategy) {
		return newDataField(aName, !MULTIPLE, strategy);
	}

	/**
	 * Creates a {@link DataField} with the given name, and sets a
	 * {@link DataField#setFileNameConstraint(Constraint) filename constraint} based on the given
	 * {@link FileNameStrategy}, actually an instance of {@link FileNameConstraint}.
	 * 
	 * @param aName
	 *        the name for the field
	 * @param isMultiple
	 *        Whether the field should allow multi-upload.
	 * @param strategy
	 *        the Strategy for the constraint
	 * 
	 * @see FileNameStrategy#checkFileName(String)
	 */
	public static DataField newDataField(String aName, boolean isMultiple, FileNameStrategy strategy) {
		final DataField dataField = newDataField(aName, isMultiple);
		dataField.setFileNameConstraint(new FileNameConstraint(strategy));
		return dataField;
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static SelectField newSelectField(String name, Iterable<?> optionModel, boolean multiple,
			List<?> initialSelection,
			boolean mandatory, boolean immutable, Constraint constraint) {
		SelectField selectField = newSelectField(name, optionModel, multiple, mandatory, immutable, constraint);
		initSelectField(selectField, initialSelection);
		
		return selectField;
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static <N> SelectField newSelectField(String name, TLTreeModel<N> options, boolean multiple,
			List<?> initialSelection,
			boolean mandatory, boolean immutable, Constraint constraint) {
		SelectField selectField = newSelectField(name, options, multiple, mandatory, immutable, constraint);
		initSelectField(selectField, initialSelection);
		return selectField;
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static SelectField newSelectField(String name, Iterable<?> optionModel, boolean multiple,
			List<?> initialSelection,
			boolean immutable) {
		return newSelectField(name, optionModel, multiple, initialSelection, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Creates a mutable, single select {@link SelectField} with the given name and the given
	 * options.
	 * 
	 * @see FormFactory#newSelectField(String, Iterable, boolean, boolean)
	 */
	public static SelectField newSelectField(String aName, Iterable<?> optionModel) {
		return newSelectField(aName, optionModel, !MULTIPLE, !IMMUTABLE);
	}

	/**
	 * Creates a mutable, single select {@link SelectField} with the given name and the given
	 * options.
	 * 
	 * @see FormFactory#newSelectField(String, TLTreeModel, boolean, boolean)
	 */
	public static <N> SelectField newSelectField(String aName, TLTreeModel<N> options) {
		return newSelectField(aName, options, !MULTIPLE, !IMMUTABLE);
	}

	/**
	 * Creates a {@link SelectField} with the given name and the given options which is not
	 * mandatory without constraints.
	 * 
	 * @see FormFactory#newSelectField(String, Iterable, boolean, boolean, boolean, Constraint)
	 */
	public static SelectField newSelectField(String aName, Iterable<?> optionModel, boolean multiple, boolean immutable) {
		return newSelectField(aName, optionModel, multiple, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Creates a {@link SelectField} with the given name and the given options which is not
	 * mandatory without constraints.
	 * 
	 * @see FormFactory#newSelectField(String, TLTreeModel, boolean, boolean, boolean,
	 *      Constraint)
	 */
	public static <N> SelectField newSelectField(String aName, TLTreeModel<N> options, boolean multiple,
			boolean immutable) {
		return newSelectField(aName, options, multiple, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Creates a new {@link SelectField}
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
	public static SelectField newSelectField(String name, Iterable<?> optionModel, boolean multiple, boolean mandatory,
			boolean immutable, Constraint constraint) {
		return newSelectField(name, options(optionModel), multiple, mandatory, immutable, constraint);
	}

	private static OptionModel<?> options(Iterable<?> options) {
		if (options instanceof OptionModel) {
			return (OptionModel<?>) options;
		}
		else if (options instanceof List<?>) {
			return new DefaultListOptionModel<>((List<?>) options);
		}
		else {
			return new DefaultListOptionModel<>(CollectionUtil.toListIterable(options));
		}
	}

	/**
	 * Creates a new {@link SelectField}
	 * 
	 * @param name
	 *        see {@link FormMember#getName()}
	 * @param options
	 *        see {@link SelectField#getOptionsAsTree()}
	 * @param multiple
	 *        see {@link SelectField#isMultiple()}
	 * @param mandatory
	 *        see {@link FormField#isMandatory()}
	 * @param immutable
	 *        see {@link FormMember#isImmutable()}
	 * @param constraint
	 *        see {@link FormField#getConstraints()}
	 */
	public static <N> SelectField newSelectField(String name, TLTreeModel<N> options, boolean multiple,
			boolean mandatory, boolean immutable, Constraint constraint) {
		return newSelectField(name, options(options), multiple, mandatory, immutable, constraint);
	}

	private static <N> OptionModel<N> options(TLTreeModel<N> options) {
		return new DefaultTreeOptionModel<>(options);
	}

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
	public static SelectField newSelectField(String name, OptionModel<?> optionModel, boolean multiple,
			boolean mandatory, boolean immutable, Constraint constraint) {
		return implementation.createSelectField(name, optionModel, multiple, mandatory, immutable, constraint);
	}

	public static SelectionTableField newSelectionTableField(
			String name, List<?> options, Accessor optionAccessor,
			boolean multiple, List<?> initialSelection, boolean mandatory, boolean immutable,
			Constraint constraint) {
		SelectionTableField result =
			newSelectionTableField(name, options, optionAccessor, multiple, mandatory, immutable, constraint);
		initSelectField(result, initialSelection);
		return result;
	}

	/**
	 * Creates a {@link SelectionTableField} with the given initial selection as selection
	 * 
	 * @param initialSelection
	 *        see {@link SelectionTableField#getSelection()}
	 * 
	 * @see FormFactory#newSelectionTableField(String, List, Accessor, boolean, boolean)
	 */
	public static SelectionTableField newSelectionTableField(String name, List<?> options, Accessor optionAccessor,
			boolean multiple, List<?> initialSelection, boolean immutable) {
		SelectionTableField result =
			newSelectionTableField(name, options, optionAccessor, multiple, immutable);
		initSelectField(result, initialSelection);
		return result;
	}

	private static void initSelectField(SelectField field, List<?> initialSelection) {
		field.setAsSelection(initialSelection);
		
		// Make the programatically set value the field's initial value.
		field.setDefaultValue(field.getValue());
	}

	/**
	 * Creates a single select, mutable {@link SelectionTableField}.
	 * 
	 * @see FormFactory#newSelectionTableField(String, List, Accessor, boolean, boolean)
	 */
	public static SelectionTableField newSelectionTableField(String aName, List<?> options, Accessor optionAccessor) {
		return newSelectionTableField(aName, options, optionAccessor, !MULTIPLE, !IMMUTABLE);
	}

	/**
	 * Creates a {@link SelectionTableField} without constraints which is not mandatory.
	 * 
	 * @see FormFactory#newSelectionTableField(String, List, Accessor, boolean, boolean,
	 *      boolean, Constraint)
	 */
	public static SelectionTableField newSelectionTableField(String aName, List<?> options, Accessor columnAccessor,
			boolean multiple, boolean immutable) {
		return newSelectionTableField(aName, options, columnAccessor, multiple, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Creates a new {@link SelectionTableField}
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
	public static SelectionTableField newSelectionTableField(String name, List<?> options, Accessor columnAccessor,
			boolean multiple, boolean mandatory, boolean immutable, Constraint constraint) {
		return implementation.newSelectionTableField(name, options, columnAccessor, multiple, mandatory, immutable,
			constraint);
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static StringField newStringField(String name, Object initialValue, boolean immutable) {
		return newStringField(name, initialValue, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static StringField newStringField(String name, Object initialValue, boolean mandatory, boolean immutable,
			Constraint constraint) {
		StringField stringField = newStringField(name, mandatory, immutable, constraint);
		stringField.initializeField(initialValue);
		return stringField;
	}

	/**
	 * Creates a mutable {@link StringField}
	 * 
	 * @see FormFactory#newStringField(String, boolean)
	 */
	public static StringField newStringField(String aName) {
		return newStringField(aName, !IMMUTABLE);
	}

	/**
	 * Creates a {@link StringField} without {@link Constraint} which is not mandatory.
	 * 
	 * @see FormFactory#newStringField(String, boolean, boolean, Constraint)
	 */
	public static StringField newStringField(String name, boolean immutable) {
		return newStringField(name, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Creates a {@link StringField}.
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
	public static StringField newStringField(String name, boolean mandatory, boolean immutable, Constraint constraint) {
		return implementation.newStringField(name, mandatory, immutable, constraint);
	}

	/**
	 * Creates a {@link PasswordField}.
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
	public static PasswordField newPasswordField(String name, boolean mandatory, boolean immutable, Constraint constraint) {
		return implementation.newPasswordField(name, mandatory, immutable, constraint);
	}

	/**
	 * Creates an {@link ExpandableStringField}.
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
	public static ExpandableStringField newExpandableStringField(String name, boolean mandatory,
			boolean immutable, Constraint constraint) {
		return implementation.newExpandableStringField(name, mandatory, immutable, constraint);
	}

	/**
	 * Creates an {@link ExpandableStringField} without {@link Constraint} which is not mandatory.
	 * 
	 * @see FormFactory#newExpandableStringField(String, boolean, boolean, Constraint)
	 */
	public static ExpandableStringField newExpandableStringField(String name, boolean immutable) {
		return newExpandableStringField(name, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Creates a mutable {@link ExpandableStringField}
	 * 
	 * @see FormFactory#newExpandableStringField(String, boolean)
	 */
	public static ExpandableStringField newExpandableStringField(String name) {
		return newExpandableStringField(name, !IMMUTABLE);
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static ExpandableStringField newExpandableStringField(String name, Object initialValue, boolean immutable) {
		return newExpandableStringField(name, initialValue, !MANDATORY, immutable, NO_CONSTRAINT);
	}

	/**
	 * Factory method for creating new instances of this class with a pre-initialized value. This
	 * cannot be implemented as constructor (see {@link FormField#getValue()}).
	 */
	public static ExpandableStringField newExpandableStringField(String name, Object initialValue, boolean mandatory,
			boolean immutable, Constraint constraint) {
		ExpandableStringField stringField = newExpandableStringField(name, mandatory, immutable, constraint);
		stringField.initializeField(initialValue);
		return stringField;
	}

	/**
	 * Calls {@link FormFactory#newCommandField(String, Command, CheckScope)} with the given arguments and
	 * {@link CheckScope#NO_CHECK}.
	 * 
	 * <p>
	 * If the given {@link Command} requires access to the UI aspects of the button (the resulting
	 * {@link CommandField} instance, there are two options:
	 * </p>
	 * 
	 * <ol>
	 * <li>Create a subclass of {@link CommandField}, implementing
	 * {@link CommandField#executeCommand(DisplayContext)} directly. This is the preferred way, if
	 * the given {@link Command} instance is a trivial implementation of {@link Command}, e.g. an
	 * anonymous class.</li>
	 * <li>Implement a {@link CommandBuilder} and use {@link FormFactory#newCommandField(String, CommandBuilder)}
	 * instead. The callback that actually creates the {@link Command} then gets a reference to the
	 * created {@link CommandField} and may store this together with the {@link Command}
	 * implementation.</li>
	 * </ol>
	 * 
	 * @param name
	 *        The name of the resulting {@link CommandField}, see {@link CommandField#getName()}.
	 * @param command
	 *        The {@link Command} to delegate the execution aspect to.
	 */
	public static CommandField newCommandField(String name, Command command) {
		return FormFactory.newCommandField(name, command, CheckScope.NO_CHECK);
	}

	/**
	 * Calls {@link FormFactory#newCommandField(String, Command, CheckScope)} with the given arguments and
	 * {@link CheckScope#NO_CHECK}.
	 * 
	 * @param name
	 *        The name of the resulting {@link CommandField}, see {@link CommandField#getName()}.
	 * @param commandBuilder
	 *        The {@link CommandBuilder} creating the delegate {@link Command}.
	 */
	public static CommandField newCommandField(String name, CommandBuilder commandBuilder) {
		return FormFactory.newCommandField(name, commandBuilder, CheckScope.NO_CHECK);
	}

	/**
	 * Creates a new {@link ExecutableCommandField}. All arguments must not be <code>null</code>.
	 * 
	 * @param name
	 *        the name for this {@link FormMember}. see also {@link FormMember#getName()}
	 * @param command
	 *        the Executable to which this {@link ExecutableCommandField} dispatches the
	 *        {@link CommandField#executeCommand(DisplayContext)} method.
	 * @param checkScope
	 *        the {@link CheckScope} whose {@link CheckScope#getAffectedFormHandlers() form
	 *        handlers} will be checked for changes.
	 * 
	 * @see #newCommandField(String, Command) Hint accessing the UI aspects of the button from
	 *      within the given {@link Command}.
	 */
	public static CommandField newCommandField(String name, Command command, CheckScope checkScope) {
		return FormFactory.newCommandField(name, command, null, checkScope);
	}

	/**
	 * Creates a new {@link ExecutableCommandField}. All arguments must not be <code>null</code>.
	 * 
	 * @param name
	 *        the name for this {@link FormMember}. see also {@link FormMember#getName()}
	 * @param commandBuilder
	 *        the Executable to which this {@link ExecutableCommandField} dispatches the
	 *        {@link CommandField#executeCommand(DisplayContext)} method.
	 * @param checkScope
	 *        the {@link CheckScope} whose {@link CheckScope#getAffectedFormHandlers() form
	 *        handlers} will be checked for changes.
	 */
	public static CommandField newCommandField(String name, CommandBuilder commandBuilder, CheckScope checkScope) {
		return FormFactory.newCommandField(name, commandBuilder, null, checkScope);
	}

	/**
	 * Creates a new {@link ExecutableCommandField}. All arguments must not be <code>null</code>.
	 * 
	 * @param name
	 *        the name for this {@link FormMember}. see also {@link FormMember#getName()}
	 * @param command
	 *        the Executable to which this {@link ExecutableCommandField} dispatches the
	 *        {@link CommandField#executeCommand(DisplayContext)} method.
	 * @param executability
	 *        The dynamic executability.
	 * 
	 * @see #newCommandField(String, Command) Hint accessing the UI aspects of the button from
	 *      within the given {@link Command}.
	 */
	public static CommandField newCommandField(String name, Command command, ExecutabilityModel executability) {
		return FormFactory.newCommandField(name, command, executability, CheckScope.NO_CHECK);
	}

	/**
	 * Creates a new {@link ExecutableCommandField}. All arguments must not be <code>null</code>.
	 * 
	 * @param name
	 *        the name for this {@link FormMember}. see also {@link FormMember#getName()}
	 * @param commandBuilder
	 *        the Executable to which this {@link ExecutableCommandField} dispatches the
	 *        {@link CommandField#executeCommand(DisplayContext)} method.
	 * @param executability
	 *        The dynamic executability.
	 */
	public static CommandField newCommandField(String name, CommandBuilder commandBuilder,
			ExecutabilityModel executability) {
		return FormFactory.newCommandField(name, commandBuilder, executability, CheckScope.NO_CHECK);
	}

	/**
	 * Creates a {@link ExecutableCommandField} for an {@link Command} with dynamic executability.
	 * 
	 * @param name
	 *        the name for this {@link FormMember}. see also {@link FormMember#getName()}
	 * @param command
	 *        the Executable to which this {@link ExecutableCommandField} dispatches the
	 *        {@link CommandField#executeCommand(DisplayContext)} method.
	 * @param executability
	 *        The dynamic executability.
	 * @param checkScope
	 *        the {@link CheckScope} whose {@link CheckScope#getAffectedFormHandlers() form
	 *        handlers} will be checked for changes.
	 * 
	 * @see #newCommandField(String, Command) Hint accessing the UI aspects of the button from
	 *      within the given {@link Command}.
	 */
	public static CommandField newCommandField(String name, Command command, ExecutabilityModel executability,
			CheckScope checkScope) {
		return implementation.newCommandField(name, command, executability, checkScope);
	}

	/**
	 * Creates a {@link ExecutableCommandField} for an {@link Command} with dynamic executability.
	 * 
	 * @param name
	 *        the name for this {@link FormMember}. see also {@link FormMember#getName()}
	 * @param commandBuilder
	 *        the Executable to which this {@link ExecutableCommandField} dispatches the
	 *        {@link CommandField#executeCommand(DisplayContext)} method.
	 * @param executability
	 *        The dynamic executability.
	 * @param checkScope
	 *        the {@link CheckScope} whose {@link CheckScope#getAffectedFormHandlers() form
	 *        handlers} will be checked for changes.
	 */
	public static CommandField newCommandField(String name, CommandBuilder commandBuilder,
			ExecutabilityModel executability, CheckScope checkScope) {
		return implementation.newCommandField(name, commandBuilder, executability, checkScope);
	}

	/**
	 * Creates a CommandFiled whose command needs no arguments
	 * 
	 * @see FormFactory#newCommandField(String, CommandHandler, LayoutComponent, Map)
	 */
	public static CommandField newCommandField(String name, CommandHandler command, LayoutComponent component) {
		return FormFactory.newCommandField(name, command, component, CommandHandler.NO_ARGS);
	}

	/**
	 * Creates a new {@link ExecutableCommandField}. All arguments must not be <code>null</code>.
	 * 
	 * @param name
	 *        the name for this {@link FormMember}. see also {@link FormMember#getName()}
	 * @param command
	 *        the command which will be executed by calling {
	 *        {@link CommandField#executeCommand(DisplayContext)} .
	 * @param component
	 *        the component on which the command can be executed
	 * @param arguments
	 *        the arguments which are needed by the command
	 */
	public static CommandField newCommandField(String name, CommandHandler command, LayoutComponent component,
			Map<String, Object> arguments) {
	
		CommandBuilder builder = ComponentCommand.newInstance(command, component, arguments);
		CheckScope scopeProvider =
			initCheckScopeProvider(AbstractCommandHandler.getCheckScopeProvider(command), component);
		ExecutableCommandField result = implementation.newCommandField(name, builder, null, scopeProvider);
	
		((ComponentCommand) result.getExecutable()).build(result);
	
		result.setImage(command.getImage(component));
		result.setNotExecutableImage(command.getNotExecutableImage(component));
		return result;
	}

	private static CheckScope initCheckScopeProvider(CheckScopeProvider checkScope, LayoutComponent aComponent) {
		if (checkScope == NoCheckScopeProvider.INSTANCE) {
			return CheckScope.NO_CHECK;
		}
		return new DefaultCheckScope(checkScope, aComponent);
	}

}
