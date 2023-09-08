/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo.model;

import java.util.ArrayList;
import java.util.Iterator;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.SimpleListControlProvider;

/**
 * The {@link DemoFormTemplateContext} is used to build the {@link FormMember}s
 * used in {@link DemoFormTemplate}.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public class DemoFormTemplateContext extends FormContext {

	/** {@link String}s to identify the {@link FormMember}s */
	public static final String	TEMPLATE_FIELD					= "templateField";
	public static final String	SELECT_NUMBER_OF_ROWS			= "selectNumberOfRows";
	public static final String	SELECT_NUMBER_OF_COLUMNS		= "selectNumberOfColumns";
	public static final String	CONTROL_GROUP					= "controlGroup";
	public static final String	CONTROLLED_GROUP				= "controlledGroup";
	public static final String	FORM_GROUP						= "formGroupDemo";
	public static final String 	SIMPLE_GROUP					= "simpleGroup";

	/** Prefix of the name of the {@link FormGroup}s representing a row */
	public static final String	ROW_GROUP_PREFIX				= "rowGroup";

	/**
	 * {@link FormGroup} which contains for each row a {@link FormGroup} which
	 * contains for each column a {@link StringField}
	 */
	private FormGroup			controlledGroup;

	/** number of columns */
	private int					numberOfColumns;
	/** number of rows */
	private int					numberOfRows;
	/** max of possible numbers for columnNumber and rowNumber, resp. */
	private final int			selectableNumbersOfStringFields	= 42;

	public DemoFormTemplateContext(String name, ResPrefix resPrefix) {
		super(name, resPrefix);
		addMember(createControlGroup());
		addMember(createControlledGroup());
		addMember(createFormGroupDemoGroup());
		addMember(createSimpleGroup());
	}

	/**
	 * This method creates a Demo Group for using
	 * {@link SimpleListControlProvider}.
	 * 
	 */
	private FormMember createSimpleGroup() {
		FormGroup group = new FormGroup(SIMPLE_GROUP,getResources());
		group.addMember(FormFactory.newStringField("stringfield", "This is a StringField", false));
		group.addMember(FormFactory.newBooleanField("booleanField", Boolean.TRUE, true));
		group.addMember(FormFactory.newStringField("stringfield2", "This is also a StringField", false));
		return group;
	}

	private FormGroup createFormGroupDemoGroup() {
		FormGroup group = new FormGroup(FORM_GROUP, getResources());
		group.addMember(FormFactory.newStringField("field1", "This is Field 1", false));
		return group;
	}

	private FormGroup createControlledGroup() {
		controlledGroup = new FormGroup(CONTROLLED_GROUP, getResources());
		return controlledGroup;
	}

	/**
	 * creates a {@link FormGroup} which contains the control elements.
	 */
	private FormGroup createControlGroup() {
		FormGroup controlGroup = new FormGroup(CONTROL_GROUP, getResources());

		// field for the template
		StringField templateField = FormFactory.newStringField(TEMPLATE_FIELD, null, false);
		controlGroup.addMember(templateField);

		// list for the selectFields
		ArrayList tmp = new ArrayList(selectableNumbersOfStringFields);
		for (int index = 0; index < selectableNumbersOfStringFields; index++) {
			tmp.add(Integer.valueOf(index + 1));
		}
		// field to select the number of rows in the table
		SelectField selField = FormFactory.newSelectField(SELECT_NUMBER_OF_ROWS, tmp, false, null,
				false);
		controlGroup.addMember(selField);
		// field to select the number of columns in the table
		selField = FormFactory.newSelectField(SELECT_NUMBER_OF_COLUMNS, tmp, false, null, false);
		controlGroup.addMember(selField);
		return controlGroup;
	}

	/**
	 * sets the number of rows and produces new {@link FormGroup}s containing
	 * for each column a {@link StringField} or removes unnecessary
	 * {@link FormGroup}s.
	 */
	public void setNumberOfRows(int rows) {
		if (numberOfRows < rows) { // new rows occur
			while (numberOfRows < rows) {
				FormGroup newGroup = new FormGroup(getRowGroupName(numberOfRows), getResources());
				controlledGroup.addMember(newGroup);
				// builds for each column a new StringField and adds it to the
				// new group
				for (int index = 0; index < numberOfColumns; index++) {
					String name = getFieldName(numberOfRows, index);
					StringField field = FormFactory.newStringField(name, null, false);
					newGroup.addMember(field);
					field.setAsString(field.getQualifiedName());
				}
				numberOfRows++; // successive increase the number of rows
			}
		} else { // rows must be deleted
			while (rows < numberOfRows) {
				controlledGroup.removeMember(getRowGroupName(numberOfRows - 1));
				numberOfRows--; // successive decrease the number of rows
			}
		}
	}

	/**
	 * This method returns the number of rows .
	 * 
	 * @return Returns the number of rows.
	 */
	public int getNumberOfRows() {
		return (numberOfRows);
	}

	/**
	 * sets the number of columns and produces for each row the new
	 * {@link StringField}s resp. removes unnecessary fields.
	 */
	public void setNumberOfColumns(int columns) {
		int min = Math.min(numberOfColumns, columns);
		int max = Math.max(numberOfColumns, columns);
		// iterator to get all FormGroups which represents a row
		Iterator rowsIter = controlledGroup.getMembers();
		int currentRow = 0; // number of the row
		while (rowsIter.hasNext()) {
			FormGroup currentMember = (FormGroup) rowsIter.next();
			for (int index = min; index < max; index++) {
				String name = getFieldName(currentRow, index);
				if (numberOfColumns < columns) { // new columns occur
					StringField field = FormFactory.newStringField(name, null, false);
					currentMember.addMember(field);
					field.setAsString(field.getQualifiedName());
				} else { // columns must be deleted
					currentMember.removeMember(name);
				}
			}
			currentRow++;
		}
		numberOfColumns = columns; // sets the number of columns
	}

	/**
	 * This method returns the number of columns.
	 * 
	 * @return Returns the number of columns.
	 */
	public int getNumberOfColumns() {
		return (numberOfColumns);
	}

	/**
	 * returns the name of the {@link StringField} in row <tt>row</tt> and
	 * column <tt>column</tt>.
	 */
	private String getFieldName(int row, int column) {
		return row + "/" + column;
	}

	/**
	 * returns the name of the {@link FormGroup} which contains the
	 * {@link StringField}s in row <tt>row</tt>
	 */
	private String getRowGroupName(int row) {
		return DemoFormTemplateContext.ROW_GROUP_PREFIX + row;
	}
}
