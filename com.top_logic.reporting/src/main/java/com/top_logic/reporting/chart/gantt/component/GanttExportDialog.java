/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.component;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.mig.html.I18NResourceProvider;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.chart.gantt.I18NConstants;
import com.top_logic.reporting.chart.gantt.ui.GanttChartExporter;
import com.top_logic.reporting.chart.gantt.ui.GraphicData;
import com.top_logic.reporting.report.util.ScalingTypeListener;
import com.top_logic.tool.boundsec.AbstractDownloadHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Export dialog for Gantt chart that allows choosing between one-page and multi-page export.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class GanttExportDialog extends FormComponent {
	
	/** Name of selector for scaling type */
	public static final String SCALING_TYPE = "scalingType";
	/** Scaling option for native (as big as it gets) scaling */
	public static final String SCALING_OPTION_NATIVE = "Native";
	/** Scaling option for using "A3" page format */
	public static final String CHOOSE_SCALING_A3 = "A3";
	
	/** The options for the scaling type selector field */
	public static final List<String> SCALING_TYPE_OPTIONS = CollectionUtil.createList(SCALING_OPTION_NATIVE,
		CHOOSE_SCALING_A3);
	
    /** Name of chooseScalingField */
	public static final String CHOOSE_FORMAT_FIELD = "chooseFormatField";
    /** Scaling option for scaling to one page */
    public static final String SCALING_OPTION_ONE_PAGE = "OnePage";
    /** Scaling option for automatic scaling */
    public static final String SCALING_OPTION_MULTI_PAGE_AUTOMATIC = "MultiPageAutomatic";

	/** The options for the timeRange selector field. */
	public static final List<String> SCALING_OPTIONS = CollectionUtil.createList(SCALING_OPTION_ONE_PAGE,
		SCALING_OPTION_MULTI_PAGE_AUTOMATIC);

	/**
	 * Configuration for the {@link GanttExportDialog}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FormComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/** Export command handler name to use. */
		@StringDefault(PDFExportCommand.COMMAND_ID)
		String getExportCommand();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(getExportCommand());
		}

	}

	/**
	 * Creates a {@link GanttExportDialog} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GanttExportDialog(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		// component doesn't need a model so we only support null
		return object == null;
	}

	@Override
	public FormContext createFormContext() {
		FormContext context = new FormContext("FormContext", getResPrefix());

		SelectField a3Options = FormFactory.newSelectField(CHOOSE_FORMAT_FIELD, SCALING_OPTIONS, /* multiple */false, Arrays.asList(SCALING_OPTION_ONE_PAGE), /* immutable */false);
		a3Options.setOptionLabelProvider(new I18NResourceProvider(getResPrefix()));
		a3Options.setDisabled(true);// will be set to enabled by ScalingTypeListener
		a3Options.setMandatory(true);
		context.addMember(a3Options);
		
		SelectField scalingType = FormFactory.newSelectField(SCALING_TYPE, SCALING_TYPE_OPTIONS, false, Arrays.asList(SCALING_OPTION_NATIVE), false);
		scalingType.setMandatory(true);
		scalingType.setOptionLabelProvider(new I18NResourceProvider(getResPrefix()));
		scalingType.addValueListener(new ScalingTypeListener(a3Options, CHOOSE_SCALING_A3));
		context.addMember(scalingType);

		return context;
	}


	/**
	 * PDF export of the gantt chart.
	 */
	public static class PDFExportCommand extends AbstractDownloadHandler {

		/**
		 * Configuration options for {@link PDFExportCommand}
		 */
		public interface Config extends AbstractDownloadHandler.Config {

			@Override
			@ListDefault(ExportExecutability.class)
			public List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

		}

		/** The command ID of this command. */
		public static final String COMMAND_ID = "ganttPdfExport";

		/** Command argument property for {@link DisplayContext}. */
		public static final String CONTEXT_INFO_ARGUMENT = "contextInfo";

		public static final class ExportExecutability implements ExecutabilityRule {

			public static final ExecutabilityRule INSTANCE = new PDFExportCommand.ExportExecutability();

			@Override
			public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
				if (component instanceof GanttExportDialog) {
					component = component.getDialogParent();
				}
				if (component instanceof GanttComponent) {
					GraphicData graphicData = ((GanttComponent) component).getGraphicData();
					if (graphicData != null) {
						if (graphicData.hasMessage()) {
							// there is an error message displayed
							return disabledNoGanttDisplayed(component);
						} else {
							// there is no error message but the Gantt chart displayed
							return ExecutableState.EXECUTABLE;
						}
					}
				}
				if (model == null) {
					return disabledNoGanttDisplayed(component);
				}
				return ExecutableState.EXECUTABLE;
			}

			private ExecutableState disabledNoGanttDisplayed(LayoutComponent component) {
				return ExecutableState.createDisabledState(component.getResPrefix().key("noGanttChartDisplayed"));
			}
		}

		/**
		 * Creates a {@link GanttExportDialog.PDFExportCommand} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public PDFExportCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		@Deprecated
		protected ResKey getDefaultI18NKey() {
			return I18NConstants.PDF_EXPORT_COMMAND;
		}

		@Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent component,
				Object model, Map<String, Object> someArguments) {
			fillArgumentsMapForExport(context, component, someArguments);
			return super.handleCommand(context, component, model, someArguments);
		}

		@Override
		protected Object prepareDownload(LayoutComponent component, DefaultProgressInfo progressInfo, Map<String, Object> arguments) throws Exception {
			LayoutComponent dialogParentComp = component.getDialogParent();
			try {
				File outputPDF = null;
				String selectedScalingOption = null;
				if (component instanceof GanttExportDialog) {
					selectedScalingOption = getSelectedScalingOption((GanttExportDialog)component);
				}
				if (selectedScalingOption != null && dialogParentComp instanceof GanttComponent) {
					Pair<String, String> contextInfo = (Pair<String, String>) arguments.get(CONTEXT_INFO_ARGUMENT);
					if (contextInfo == null) {
						throw new IllegalArgumentException("CONTEXT_INFO_ARGUMENT must not be null.");
					}
					outputPDF = ((GanttComponent) dialogParentComp).generateChartPDF(contextInfo, selectedScalingOption);
				}
				return outputPDF;
			} catch (Exception ex) {
				Logger.error("Failed to create gantt export.", ex, PDFExportCommand.class);
				throw new TopLogicException(I18NConstants.PDF_EXPORT_FAILED, ex);
			}
		}

		@Override
		public String getDownloadName(LayoutComponent aComponent, Object download) {
			return Resources.getInstance().getString(I18NConstants.PDF_EXPORT_NAME);
		}

		@Override
		public BinaryDataSource getDownloadData(Object download) throws Exception {
			return download instanceof File ? BinaryDataFactory.createBinaryData((File) download) : null;
		}

		@Override
		public void cleanupDownload(Object model, Object download) {
			if (download instanceof File) {
				((File) download).delete();
			}
		}

		private String getSelectedScalingOption(GanttExportDialog component) {
			String selectedScalingOption;
			
			SelectField scalingTypeField = (SelectField) component.getFormContext().getMember(GanttExportDialog.SCALING_TYPE);
			Object scalingType = scalingTypeField.getSingleSelection();
			if (GanttExportDialog.SCALING_OPTION_NATIVE.equals(scalingType)) {
				selectedScalingOption = GanttExportDialog.SCALING_OPTION_NATIVE;
			}else{
				SelectField a3OptionsField = (SelectField) component.getFormContext().getMember(GanttExportDialog.CHOOSE_FORMAT_FIELD);
				selectedScalingOption = (String)a3OptionsField.getSingleSelection();
			}
			
			return selectedScalingOption;
		}

		/**
		 * PDF generation needs context infos. So this infos must be stored in the arguments map to
		 * gain access in prepareDownload method.
		 */
		public static void fillArgumentsMapForExport(DisplayContext context, LayoutComponent component, Map<String, Object> someArguments) {
			someArguments.put(CONTEXT_INFO_ARGUMENT, GanttChartExporter.extractContextInfoForExport(context));
		}

		@Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
			return ExportExecutability.INSTANCE;
		}

	}

}
