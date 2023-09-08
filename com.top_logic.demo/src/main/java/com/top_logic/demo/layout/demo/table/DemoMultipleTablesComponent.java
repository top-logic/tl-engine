/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.table;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.structure.SeparateTableControlProvider;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link FormComponent} with two {@link TableField} displayed with
 * {@link SeparateTableControlProvider}.
 * 
 * <p>
 * Regression test case for Ticket #19916.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoMultipleTablesComponent extends FormComponent {

	/**
	 * Creates a {@link DemoMultipleTablesComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DemoMultipleTablesComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public FormContext createFormContext() {
		FormContext fc = new FormContext(this);

		TableField table1 = FormFactory.newTableField("table1");
		ObjectTableModel model1 = new ObjectTableModel(Arrays.asList("col1", "col2"), getTableConfiguration("table1"),
			Collections.emptyList());
		table1.setTableModel(model1);
		fc.addMember(table1);

		TableField table2 = FormFactory.newTableField("table2");
		ObjectTableModel model2 = new ObjectTableModel(Arrays.asList("col1", "col2"), getTableConfiguration("table2"),
			Collections.emptyList());
		table2.setTableModel(model2);
		fc.addMember(table2);

		fc.addMember(FormFactory.newStringField("text"));

		return fc;
	}

	/**
	 * {@link CommandHandler} dropping a table field and building a new one.
	 */
	public static class RebuildTable extends AbstractCommandHandler {

		/**
		 * Creates a {@link DemoMultipleTablesComponent.RebuildTable} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public RebuildTable(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
				Object model, Map<String, Object> someArguments) {
			DemoMultipleTablesComponent formComponent = (DemoMultipleTablesComponent) aComponent;
			TableField table1 = (TableField) formComponent.getFormContext().getMember("table1");

			FormContainer parent = table1.getParent();
			parent.removeMember(table1);

			TableField newTable1 = FormFactory.newTableField("table1");
			ObjectTableModel model1 =
				new ObjectTableModel(Arrays.asList("col1", "col2", "col3"),
					formComponent.tableConfiguration("table1"), Collections.emptyList());
			newTable1.setTableModel(model1);
			parent.addMember(newTable1);

			return HandlerResult.DEFAULT_RESULT;
		}

	}

	/**
	 * Workaround for protected method in super class not callable from inner class.
	 */
	public TableConfiguration tableConfiguration(String name) {
		return getTableConfiguration(name);
	}

}
