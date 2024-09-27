/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import java.io.IOException;
import java.util.Arrays;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.NumbersOnlyConstraint;
import com.top_logic.layout.form.model.DeckField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.model.TreeField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.DefaultStructureTreeUIModel;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.renderer.ConfigurableTreeContentRenderer;
import com.top_logic.layout.tree.renderer.ConfigurableTreeRenderer;
import com.top_logic.layout.tree.renderer.DefaultTreeImageProvider;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder}, that creates certain {@link FormField}s, that may produce exceptions during
 * rendering.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class FaultyFormFieldModelBuilder implements ModelBuilder {

	/**
	 * Singleton {@link FaultyFormFieldModelBuilder} instance.
	 */
	public static final FaultyFormFieldModelBuilder INSTANCE = new FaultyFormFieldModelBuilder();

	private FaultyFormFieldModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		FormContext formContext = new FormContext(aComponent);
		formContext.addMember(createSelectFieldFaulty("faultySingleSelectControl", false, false));
		formContext.addMember(createSelectFieldFaulty("faultyMultiSelectControl", false, true));
		formContext.addMember(createSelectFieldFaulty("faultyMultiImmutableSelectControl", true, true));
		formContext.addMember(createSelectFieldFaulty("faultyMultiSelectionControl", false, true));
		formContext.addMember(createSelectFieldFaulty("faultyMultiImmutableSelectionControl", true, true));
		formContext.addMember(createFaultyTabBar(aComponent.getResPrefix()));
		formContext.addMember(createFaultyTreeField());
		formContext.addMember(createFaultyTableField());
		formContext.addMember(createFaultyStringField());
		return formContext;
	}

	private FormMember createSelectFieldFaulty(String fieldName, boolean immutable, boolean multiSelection) {
		final String faultyOption = "Option 2";
		SelectField selectField =
			FormFactory.newSelectField(fieldName, Arrays.asList("Option 1", faultyOption, "Option 3"), multiSelection,
				immutable);
		if (multiSelection) {
			selectField.setAsSelection(Arrays.asList(faultyOption, "Option 3"));
		} else {
			selectField.setAsSingleSelection(faultyOption);
		}
		selectField.setOptionLabelProvider(new LabelProvider() {

			@Override
			public String getLabel(Object object) {
				if (faultyOption.equals(object)) {
					throw new NullPointerException("Faulty label");
				}
				return (String) object;
			}
		});
		return selectField;
	}

	private FormMember createFaultyTabBar(ResPrefix resPrefix) {
		DeckField deckField = new DeckField("faultyDeckField", resPrefix);
		deckField.addMember(
			new FormGroup("tab1", resPrefix, new FormMember[] { FormFactory.newBooleanField("checkboxControl1") }));
		StringField justNumbersField =
			FormFactory.newStringField("justNumbers", false, false, NumbersOnlyConstraint.INSTANCE);
		deckField.addMember(new FormGroup("faultyTab", resPrefix, new FormMember[] { justNumbersField }) {
			@Override
			public ResKey getLabel() {
				throw new UnsupportedOperationException("Faulty label");
			}
		});
		deckField.addMember(new FormGroup("tab3", resPrefix,
			new FormMember[] { FormFactory.newSelectField("selectControl1", Arrays.asList("a", "b", "c")) }));
		return deckField;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private FormMember createFaultyTreeField() {
		TreeUIModel uiModel = new DefaultStructureTreeUIModel(createTLTree());
		uiModel.setExpanded(uiModel.getRoot(), true);
		DefaultSingleSelectionModel selectionModel = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
		TreeField treeField =
			FormFactory.newTreeField("faultyTreeField", uiModel, selectionModel,
				new ConfigurableTreeRenderer(HTMLConstants.DIV, HTMLConstants.DIV,
					new ConfigurableTreeContentRenderer(DefaultTreeImageProvider.INSTANCE, new ResourceProvider() {

					@Override
					public String getLabel(Object object) {
							return ((TLTreeNode) object).getBusinessObject().toString();
					}

					@Override
					public String getType(Object anObject) {
						return null;
					}

					@Override
					public String getTooltip(Object anObject) {
						return null;
					}

					@Override
					public String getLink(DisplayContext context, Object anObject) {
						return null;
					}

					@Override
					public ThemeImage getImage(Object anObject, Flavor aFlavor) {
						return null;
					}

					@Override
					public String getCssClass(Object anObject) {
						return null;
					}
					})));
		return treeField;
	}

	@SuppressWarnings({ "unused" })
	private DefaultMutableTLTreeModel createTLTree() {
		DefaultMutableTLTreeModel tree = new DefaultMutableTLTreeModel();
		DefaultMutableTLTreeNode root = tree.getRoot();
		root.setBusinessObject("Root");

		DefaultMutableTLTreeNode a1 = root.createChild(new Object() {
			@Override
			public String toString() {
				throw new UnsupportedOperationException("Faulty label");
			}
		});
		DefaultMutableTLTreeNode a11 = a1.createChild("A1.1");
		DefaultMutableTLTreeNode a12 = a1.createChild("A1.2");
		DefaultMutableTLTreeNode a121 = a12.createChild("A1.2.1");
		DefaultMutableTLTreeNode a122 = a12.createChild("A1.2.2");
		DefaultMutableTLTreeNode a13 = a1.createChild("A1.3");
		DefaultMutableTLTreeNode a14 = a1.createChild("A1.4");
		DefaultMutableTLTreeNode a2 = root.createChild("A2");
		DefaultMutableTLTreeNode a3 = root.createChild("A3");
		DefaultMutableTLTreeNode a31 = a3.createChild("A3.1");
		DefaultMutableTLTreeNode a32 = a3.createChild("A3.2");

		return tree;
	}

	private FormMember createFaultyTableField() {
		String firstColumnName = "column 1";
		String secondColumnName = "column 2";
		String thirdColumnName = "column 3";
		Accessor<String> regularAccessor = new Accessor<>() {

			@Override
			public Object getValue(String object, String property) {
				return object + " - " + property;
			}

			@Override
			public void setValue(String object, String property, Object value) {
				throw new UnsupportedOperationException();
			}
		};
		
		Accessor<String> faultyAccessor = new Accessor<>() {
			
			@Override
			public Object getValue(String object, String property) {
				throw new UnsupportedOperationException("Faulty object");
			}
			
			@Override
			public void setValue(String object, String property, Object value) {
				throw new UnsupportedOperationException();
			}
		};
		String[] columnNames = { firstColumnName, secondColumnName, thirdColumnName };
		String tableName = "faultyTableField";
		TableConfiguration config = TableConfigurationFactory.table();
		config.setTableName(tableName);
		declareColumnConfiguration(config, firstColumnName, regularAccessor);
		declareColumnConfiguration(config, secondColumnName, faultyAccessor);
		declareColumnConfiguration(config, thirdColumnName, regularAccessor);
		TableModel tableModel = new ObjectTableModel(columnNames, config, Arrays.asList("Row 1", "Row 2", "Row 3"));
		TableField formTable = FormFactory.newTableField(tableName, tableModel, false);
		return formTable;
	}

	private void declareColumnConfiguration(TableConfiguration config, String firstColumnName,
			Accessor<String> regularAccessor) {
		ColumnConfiguration firstColumn = config.declareColumn(firstColumnName);
		firstColumn.setAccessor(regularAccessor);
		firstColumn.setColumnLabel(firstColumnName);
	}

	private FormMember createFaultyStringField() {
		StringField faultyStringField = FormFactory.newStringField("faultyStringField");
		faultyStringField.setControlProvider(new DefaultFormFieldControlProvider() {
			@Override
			public Control visitStringField(StringField member, Void arg) {
				return new ConstantControl<>(member) {

					@Override
					protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
						throw new NullPointerException("Faulty string");
					}
				};
			}
		});
		return faultyStringField;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

}
