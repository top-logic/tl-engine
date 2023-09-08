/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo.model;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;

import org.w3c.dom.Document;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormListControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.form.template.SimpleListControlProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The {@link DemoFormTemplate} is used to render a dynamic table of {@link StringField}s. One
 * easily select the number of rows and columns which has to be displayed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DemoFormTemplate extends FormComponent {

	public static final String CONTEXT_NAME = "formTemplateContext";

	public static DemoFormTemplateControlProvider CONTROL_PROVIDER = new DemoFormTemplateControlProvider(ResPrefix.GLOBAL);

	/**
	 * Configuration for the {@link DemoFormTemplate}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FormComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerCommand(ApplyCommand.COMMAND);
		}

	}
	public DemoFormTemplate(InstantiationContext context, Config someAttrs) throws ConfigurationException {
		super(context, someAttrs);
	}

	@Override
	public FormContext createFormContext() {
		return new DemoFormTemplateContext(CONTEXT_NAME, getResPrefix());
	}

	public SimpleListControlProvider getSimpleListControlProvider() {
		return new SimpleListControlProvider(getResPrefix());
	}

	/**
	 * The {@link ApplyCommand} sets the selected numbers of rows and columns, respectively.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class ApplyCommand extends AbstractCommandHandler {

		public static final String COMMAND = "demoFormApply";

		public ApplyCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model, Map<String, Object> someArguments) {
			DemoFormTemplate theComp = (DemoFormTemplate) component;
			DemoFormTemplateContext theContext = (DemoFormTemplateContext) theComp.getFormContext();
			FormGroup controlGroup = (FormGroup) theContext.getMember(DemoFormTemplateContext.CONTROL_GROUP);
			// new number of rows
			Integer selectedRows = (Integer) ((SelectField) controlGroup.getMember(DemoFormTemplateContext.SELECT_NUMBER_OF_ROWS))
					.getSingleSelection();
			// new number of columns
			Integer selectedColumns = (Integer) ((SelectField) controlGroup.getMember(DemoFormTemplateContext.SELECT_NUMBER_OF_COLUMNS))
					.getSingleSelection();
			if (selectedRows == null) {
				theContext.setNumberOfRows(0);
			} else {
				theContext.setNumberOfRows(selectedRows.intValue());
			}
			if (selectedColumns == null) {
				theContext.setNumberOfColumns(0);
			} else {
				theContext.setNumberOfColumns(selectedColumns.intValue());
			}
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	private static final class DemoFormTemplateControlProvider extends DefaultFormFieldControlProvider {

		private final Document groupPattern;

		private final Document tableFrameworkPattern;

		private final Document tableRowsTemplate;

		private final ResPrefix resourcePrefix;

		/* package protected */DemoFormTemplateControlProvider(ResPrefix resourcePrefix) {
			super(false);
			this.resourcePrefix = resourcePrefix;

			this.tableRowsTemplate = DOMUtil.parseThreadSafe(
				"<tr xmlns='" + HTMLConstants.XHTML_NS + "' xmlns:t='" + FormTemplateConstants.TEMPLATE_NS
					+ "' xmlns:p='"
					+ FormPatternConstants.PATTERN_NS + "'>"
					+ "<t:items><td><p:self style=\"label\"/></td><td><p:self/></td></t:items>" + "</tr>");

			this.tableFrameworkPattern = DOMUtil.parseThreadSafe(
				"<table xmlns='" + HTMLConstants.XHTML_NS + "' xmlns:t='" + FormTemplateConstants.TEMPLATE_NS
					+ "' xmlns:p='" + FormPatternConstants.PATTERN_NS + "'>" + "<t:items/>" + "</table>");

			this.groupPattern = DOMUtil.parseThreadSafe(
				"<table xmlns='" + HTMLConstants.XHTML_NS + "' xmlns:t='" + FormTemplateConstants.TEMPLATE_NS
					+ "' xmlns:p='"
					+ FormPatternConstants.PATTERN_NS + "'>"
					+ "<p:field name='field1' style='label'/><p:field name='field1'/>" + "</table>");
		}

		@Override
		public Control visitFormContainer(FormContainer member, Void arg) {
			if (member.getName().equals(DemoFormTemplateContext.CONTROLLED_GROUP)) {
				// Outermost FormContainer which represents the table
				return new FormListControl(member, this, tableFrameworkPattern, resourcePrefix);
			} else if (member.getName().startsWith(DemoFormTemplateContext.ROW_GROUP_PREFIX)) {
				// FormContainer represents a row of the table
				return new FormListControl(member, this, tableRowsTemplate, resourcePrefix);
			} else if (member.getName().equals(DemoFormTemplateContext.FORM_GROUP)) {
				return new FormGroupControl((FormGroup) member, this, groupPattern, resourcePrefix);
			} else {
				throw new IllegalArgumentException("Unknown member '" + member + "'.");
			}
		}
	}
}
