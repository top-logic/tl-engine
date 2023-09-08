/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.table;


import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.I18NResourceProvider;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.export.ExcelExportHandler;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class ExportModalDialogComponent extends FormComponent{
	
	public static final String EXPORT_OPTIONS_FIELD  = "exportOptions";
	public static final String NORMAL_EXPORT         = "normalExport";
	
	/**
	 * Configuration of the {@link ExportModalDialogComponent}.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FormComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/** Configuration name for the value of {@link #getExportOptions()}. */
		String EXPORT_COMMAND_GROUPS_NAME = "exportOptions";

		/**
		 * The displayed export options.
		 */
		@Key(ExportOption.NAME_ATTRIBUTE)
		@Name(EXPORT_COMMAND_GROUPS_NAME)
		List<ExportOption> getExportOptions();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			registry.registerButton(ChoiceExcelExportCommandHandler.COMMAND_ID);
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
		}

		@Override
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			FormComponent.Config.super.addAdditionalCommandGroups(additionalGroups);
			for (ExportOption exportOption : getExportOptions()) {
				additionalGroups.add(exportOption.getGroup());
			}
		}

	}

	/**
	 * An option in the export dialog
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ExportOption extends NamedConfigMandatory {

		/**
		 * The command group for this option.
		 */
		BoundCommandGroup getGroup();

	}

	public ExportModalDialogComponent(InstantiationContext context, Config someAtts) throws ConfigurationException{
		super(context, someAtts);
    }

	@Override
	public FormContext createFormContext() {
		FormContext theCtx = new FormContext("default", this.getResPrefix());
		SelectField theExportOptionsField = FormFactory.newSelectField(EXPORT_OPTIONS_FIELD, getDialogOptions());
		theExportOptionsField.setOptionLabelProvider(new I18NResourceProvider(getResPrefix()));
		theExportOptionsField.setAsSingleSelection(NORMAL_EXPORT);
		theCtx.addMember(theExportOptionsField);
	    return theCtx;
    }

	private TableComponent getOpener() {
		return (TableComponent) getDialogParent();
	}

	/**
	 * Returns the List of export options
	 */
	public List<String> getDialogOptions() {
		return config().getExportOptions()
			.stream()
			.filter(configuredGroup -> getOpener().allow(configuredGroup.getGroup()))
			.map(ExportOption::getName)
			.collect(Collectors.toList());
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
	    return true;
    }
	
	/**
	 * @deprecated Use {@link com.top_logic.layout.table.export.ExcelExportHandler}
	 */
	@Deprecated
	public static class ChoiceExcelExportCommandHandler extends ExcelExportHandler {
		 /** ID for this command handler */
        public static final String COMMAND_ID = "choiceExportExcel";
        
        public ChoiceExcelExportCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }
        
        @Override
		protected Object prepareDownload(LayoutComponent component, DefaultProgressInfo progressInfo, Map<String, Object> arguments) throws Exception {
        	ExportModalDialogComponent theComponent = (ExportModalDialogComponent)component;
        	FormContext theCtx = theComponent.getFormContext();
        	if(theCtx.hasMember(EXPORT_OPTIONS_FIELD)) {
        		String theSelection = (String)((SelectField)theCtx.getMember(EXPORT_OPTIONS_FIELD)).getSingleSelection();
        		if(arguments.isEmpty()) {
        			arguments = new HashMap(1);
        		}
        		arguments.put(EXPORT_OPTIONS_FIELD, theSelection);
        	}
        	LayoutComponent theParent = theComponent.getDialogParent();
            return super.prepareDownload(theParent, progressInfo, arguments);
        }
	}
}
