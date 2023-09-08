/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.edit;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.demo.model.types.DemoTypesA;
import com.top_logic.demo.model.types.DemoTypesFactory;
import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.element.layout.meta.DefaultFormContextModificator;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.constraints.RangeConstraint;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.selection.SelectDialogProvider;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Adds warning to a {@link DemoTypesA#DATE_ATTR}, if the value is before 2000-01-01.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoFormContextModificator extends DefaultFormContextModificator {

	/** Name of the field and table displaying the children of components model. */
	public static final String CHILDREN_TABLE = "childrenTable";

	/**
	 * Singleton {@link DemoFormContextModificator} instance.
	 */
	public static final DemoFormContextModificator INSTANCE = new DemoFormContextModificator();

	@Override
	public void postModify(LayoutComponent component, TLClass type, TLObject anAttributed,
			AttributeFormContext aContext, FormContainer currentGroup) {
		FormField field = getField(type, anAttributed, currentGroup, DemoTypesA.DATE_ATTR);
		if (field != null) {
			RangeConstraint range = new RangeConstraint(getLowerDate(), null);
			field.addWarningConstraint(range);
		}
		addChildrenField(component, anAttributed, currentGroup);
	}

	@Override
	public void modify(LayoutComponent component, String aName, FormMember aMember, TLStructuredTypePart aMA,
			TLClass type, TLObject aAnAttributed, AttributeUpdate aAnUpdate, AttributeFormContext aContext,
			FormContainer currentGroup) {
		super.modify(component, aName, aMember, aMA, type, aAnAttributed, aAnUpdate, aContext, currentGroup);
		if(aName.equals("priorityTable")) {
			SelectField field = (SelectField) aMember;
			field.setSelectDialogProvider(SelectDialogProvider.newTableInstance());
		}
		if (aName.equals("priorityTableDialogConfig")) {
			SelectField field = (SelectField) aMember;
			field.setSelectDialogProvider(SelectDialogProvider.newTableInstance());
			// use own personal config for select dialogs
			field.setDialogTableConfigurationProvider(field.getTableConfigurationProvider());
		}
	}
	/**
	 * Creates a {@link Date} which the attribute must have to avoid warnings.
	 * 
	 * <p>
	 * The lower bound can not be a static variable: For example when the class is loaded before the
	 * {@link TimeZones} are loaded, the {@link Date} is created in the VM time zone, which is not
	 * the <i>TopLogic</i> or user time zone.
	 * </p>
	 * 
	 * @return The expected lower bound, e.g. for a {@link RangeConstraint}.
	 */
	public static Date getLowerDate() {
		Calendar calendar = CalendarUtil.createCalendar();
		return DateUtil.createDate(calendar, 2000, Calendar.JANUARY, 1);
	}

	private void addChildrenField(LayoutComponent component, TLObject attributed, FormContainer container) {
		if (container.hasMember(CHILDREN_TABLE)) {
			// Do not add member twice.
			return;
		}
		if (component instanceof GridComponent) {
			// TODO #21088: TableField is not needed in GridComponent. Moreover it is currently not
			// possible to have a TableField when switching from edit to view mode in grid.
			return;
		}

		TableField tableField = FormFactory.newTableField(CHILDREN_TABLE);
		List<? extends StructuredElement> children;
		Set<TLClass> childrenTypes;
		if (attributed instanceof StructuredElement) {
			// Create object in grid component or demoPlain types.
			StructuredElement attributedElement = (StructuredElement) attributed;
			children = attributedElement.getChildren();
			childrenTypes = attributedElement.getChildrenTypes();
		} else {
			children = Collections.emptyList();
			childrenTypes = Collections.emptySet();
		}
		if (childrenTypes.isEmpty()) {
			childrenTypes = Collections.singleton(DemoTypesFactory.getDemoTypesAllType());
		}
		GenericTableConfigurationProvider genericTable = new GenericTableConfigurationProvider(childrenTypes);
		TableConfigurationProvider customTable =
			((FormComponent) component).lookupTableConfigurationBuilder(CHILDREN_TABLE);
		TableConfigurationProvider config;
		if (customTable != null) {
			config = TableConfigurationFactory.combine(genericTable, customTable);
		} else {
			config = genericTable;
		}
		tableField.setTableModel(
			new ObjectTableModel(ArrayUtil.EMPTY_STRING_ARRAY, TableConfigurationFactory.build(config), children));
		container.addMember(tableField);
	}

	private FormField getField(TLClass type, TLObject anAttributed, FormContainer currentGroup,
			String attributeName) {

		TLStructuredTypePart theMA = MetaElementUtil.getMetaAttributeOrNull(type, attributeName);
		if (theMA == null) {
			return null;
		}

		String fieldName = MetaAttributeGUIHelper.getAttributeID(theMA, anAttributed);
		if (!currentGroup.hasMember(fieldName)) {
			return null;
		}

		return currentGroup.getField(fieldName);
	}

	@Override
	public void clear(LayoutComponent component, TLClass type, TLObject anAttributed,
			AttributeUpdateContainer aContainer, FormContainer currentGroup) {
		if (currentGroup.hasMember(CHILDREN_TABLE)) {
			currentGroup.removeMember(currentGroup.getMember(CHILDREN_TABLE));
		}
	}
}
