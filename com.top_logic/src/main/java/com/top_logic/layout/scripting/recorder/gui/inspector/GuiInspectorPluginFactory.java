/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.decorator.CompareInfo;
import com.top_logic.layout.form.decorator.CompareService;
import com.top_logic.layout.form.decorator.DecorateInfo;
import com.top_logic.layout.form.decorator.DecorateService;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.scripting.recorder.gui.AssertionTreeNode;
import com.top_logic.layout.scripting.recorder.gui.TableCell;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.AssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.CompareValueFormPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.CompareValueTablePlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.ComponentModelAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.FieldErrorAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.FieldLabelAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.FieldMandatoryAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.FieldModeAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.FieldNotExistsAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.FieldOptionsAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.FieldValidityAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.FieldValueAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.TableCellFullTextAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.TableCellValueAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.TableColumnLabelAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.TableRowAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.TableRowCountAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.TableRowSelectedAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.TreeNodeChildCountAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.TreeNodeExpansionAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.TreeNodeLeafAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.TreeNodeSelectionAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.field.FieldTooltipAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.field.FieldTooltipCaptionAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.table.TableColumnFilterabilityAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.table.TableColumnSortabilityAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.table.TableColumnsAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo.ComponentInfoPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo.FieldDefaultValueInfoPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo.StaticInfoPlugin;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.LiveActionContext;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.component.DefaultTableDataName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Creates {@link AssertionPlugin}s available for a certain UI model.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class GuiInspectorPluginFactory {

	/**
	 * Creates assertions on {@link FormMember}s.
	 */
	public static void createFormMemberAssertions(InspectorModel inspector, FormMember model) {
		inspector.add(new FieldLabelAssertionPlugin(model));
		inspector.add(new FieldTooltipAssertionPlugin(model));
		inspector.add(new FieldTooltipCaptionAssertionPlugin(model));
		inspector.add(new FieldModeAssertionPlugin(model));
		inspector.add(new FieldNotExistsAssertionPlugin(model));

		inspector.add(
			new StaticInfoPlugin(model.getQualifiedName(), I18NConstants.QUALIFIED_NAME, "qualifiedName", model));
		inspector.add(
			new StaticInfoPlugin(model.getClass().getCanonicalName(), I18NConstants.FIELD_IMPLEMENTATION,
			"fieldImplementation"));

		createComponentInformation(inspector, model);
	}

	private static void createComponentInformation(InspectorModel inspector, FormMember model) {
		FormHandler formHandler = model.getFormContext().getOwningModel();
		if (formHandler instanceof LayoutComponent) {
			LayoutComponent component = (LayoutComponent) formHandler;

			createComponentInformation(inspector, component);
		}
	}

	/**
	 * Creates information about a {@link LayoutComponent}.
	 */
	public static void createComponentInformation(InspectorModel inspector, LayoutComponent component) {
		inspector.add(new ComponentInfoPlugin(component, I18NConstants.COMPONENT, "component"));
		inspector.add(new ComponentModelAssertionPlugin(component));
		if (component instanceof FormComponent) {
			inspector.add(new StaticInfoPlugin(((FormComponent) component).getPage(), I18NConstants.COMPONENT_JSP,
				"componentJSP"));
		}
	}

	/**
	 * Creates assertions on {@link FormField}s.
	 */
	public static void createFieldAssertions(InspectorModel inspector, FormField model) {
		inspector.add(new FieldValueAssertionPlugin(model));
		if (model instanceof SelectField) {
			inspector.add(new FieldOptionsAssertionPlugin((SelectField) model));
		}
		inspector.add(new FieldValidityAssertionPlugin(model));
		inspector.add(new FieldErrorAssertionPlugin(model));
		inspector.add(new FieldMandatoryAssertionPlugin(model, false));

		inspector.add(new FieldDefaultValueInfoPlugin(model));

		createFormMemberAssertions(inspector, model);
		addCompareAssertions(inspector, model);
	}

	private static void addCompareAssertions(InspectorModel inspector, FormField model) {
		FormContext formContext = model.getFormContext();
		if (!DecorateService.isDecorated(formContext)) {
			return;
		}
		DecorateService<DecorateInfo> decorator = DecorateService.getDecorator(formContext);
		if (!(decorator instanceof CompareService<?>)) {
			return;
		}
		CompareService<?> compareService = (CompareService<?>) decorator;
		FormField historicModel = compareService.findBaseValueField(model);
		if (historicModel == null) {
			return;
		}
		CompareInfo compareInfo =
			compareService.createCompareInfo(historicModel, model, DecorateService.getDefaultLabels(model));
		inspector.add(new CompareValueFormPlugin(model, historicModel, compareInfo));

	}

	/**
	 * Creates assertions on {@link TableCell}s.
	 */
	public static void createTableCellAssertions(InspectorModel inspector, TableCell model) {
		if (model.hasCellInformation()) {
			inspector.add(new TableCellValueAssertionPlugin(model));
			inspector.add(new TableCellFullTextAssertionPlugin(model));
		}
		if (model.hasRowInformation()) {
			inspector.add(new TableRowAssertionPlugin(model));
		}
		if (model.hasCellInformation()) {
			inspector.add(new TableColumnLabelAssertionPlugin(model));
			inspector.add(new TableColumnFilterabilityAssertionPlugin(model));
			inspector.add(new TableColumnSortabilityAssertionPlugin(model));
		}
		if (model.hasRowInformation()) {
			inspector.add(new TableRowSelectedAssertionPlugin(model));
		}
		inspector.add(new TableColumnsAssertionPlugin(model, true));
		inspector.add(new TableColumnsAssertionPlugin(model, false));
		inspector.add(TableRowCountAssertionPlugin.assertDisplayedRows(model));
		inspector.add(TableRowCountAssertionPlugin.assertAllRows(model));
		TableData tableData = model.getTableData();
		if (tableData instanceof FormMember) {
			createComponentInformation(inspector, (FormMember) tableData);
		} else {
			ModelName tableName = tableData.getModelName();
			if (tableName instanceof DefaultTableDataName) {
				ModelName ownerName = ((DefaultTableDataName) tableName).getDefaultTableDataOwner();
				DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
				ActionContext context =
					new LiveActionContext(displayContext.asRequest().getSession(), displayContext
						.getSubSessionContext()
						.getLayoutContext().getMainLayout(), displayContext);
				Object owner = ModelResolver.locateModel(context, ownerName);
				if (owner instanceof LayoutComponent) {
					createComponentInformation(inspector, (LayoutComponent) owner);
				}
			}
		}

		if (model instanceof AssertionTreeNode<?>) {
			createTreeNodeAssertions(inspector, (AssertionTreeNode<?>) model);
		}
		if (model.getValue() instanceof CompareInfo) {
			createCompareValueAssertions(inspector, model);
		}
	}

	private static void createCompareValueAssertions(InspectorModel inspector, TableCell model) {
		inspector.add(new CompareValueTablePlugin(model));
	}

	/**
	 * Creates assertions on {@link AssertionTreeNode}s.
	 */
	public static <N> void createTreeNodeAssertions(InspectorModel inspector, AssertionTreeNode<N> model) {
		inspector.add(new TreeNodeExpansionAssertionPlugin<>(model));
		inspector.add(new TreeNodeSelectionAssertionPlugin<>(model));
		inspector.add(new TreeNodeLeafAssertionPlugin<>(model));
		inspector.add(new TreeNodeChildCountAssertionPlugin<>(model));

		inspector.add(new StaticInfoPlugin(model.getNodeLabel(), I18NConstants.NODE_LABEL, "nodeLabel"));
	}
}
