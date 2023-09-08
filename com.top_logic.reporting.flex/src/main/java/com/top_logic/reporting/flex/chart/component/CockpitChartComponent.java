/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.Command.CommandChain;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.reporting.flex.chart.config.ChartConfig;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.datasource.ChartDataSource;
import com.top_logic.reporting.flex.chart.config.util.Configs;
import com.top_logic.reporting.flex.search.NewStoredConfigChartReportComponent;
import com.top_logic.reporting.flex.search.handler.DisplayDetailsCommand;
import com.top_logic.reporting.flex.search.model.FlexReport;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Chart display in the cockpits.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CockpitChartComponent extends AbstractChartComponent {

	/**
	 * Cockpit chart needs to know the meta elements for finding the correct flexible reports.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface Config extends AbstractChartComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * Configuration name for value of {@link Config#getChartConfigFiles()}.
		 */
		String CHART_CONFIGS = "chart-configs";

		/**
		 * Configuration name for value of {@link Config#getTypes()}.
		 */
		String TYPES = "types";

		/**
		 * Configuration name for value of {@link Config#getChartDataSource()}.
		 */
		String CHART_DATA_SOURCE = "chart-data-source";

		/**
		 * Qualified type names of the {@link TLObject}s.
		 */
		@Mandatory
		@Format(CommaSeparatedStrings.class)
		@Name(TYPES)
		List<String> getTypes();

		/**
		 * data source to get the raw values from.
		 */
		@InstanceFormat
		@Name(CHART_DATA_SOURCE)
		ChartDataSource<?> getChartDataSource();

		/**
		 * The name of additional serialized {@link ChartConfig} files.
		 */
		@Name(CHART_CONFIGS)
		@Format(CommaSeparatedStrings.class)
		List<String> getChartConfigFiles();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			AbstractChartComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(DisplayDetailsCommand.COMMAND_ID);
		}

	}

	/** Constant to identify no chart. */
	private static final ChartDescription NO_CHART = new ChartDescription("NO CONTENT", null, false);

	private final List<String> _chartFiles;

	private final List<TLClass> _types;

	private final ChartDataSource<?> _dataSource;

	private ChartDescription _currentChart = NO_CHART;

	/**
	 * Creates a {@link CockpitChartComponent}.
	 */
	public CockpitChartComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_chartFiles = config.getChartConfigFiles();
		_types = getTypes(config);
		_dataSource = config.getChartDataSource();
	}

	@Override
	protected CommandHandler getDetailsHandler() {
		return getCommandById(DisplayDetailsCommand.COMMAND_ID);
	}

	@Override
	protected ChartModel createChartModel(Object businessModel) {
		ChartConfig config = readChartConfig();
		if (config != null) {
			return new ChartModel(config, businessModel);
		} else {
			return null;
		}
	}

	/**
	 * This method returns the chartFiles.
	 * 
	 * @return Returns the chartFiles.
	 */
	public List<String> getChartFiles() {
		return _chartFiles;
	}

	/**
	 * This method returns the currentChart.
	 * 
	 * @return Returns the currentChart.
	 */
	public ChartDescription getCurrentChart() {
		if (_currentChart == CockpitChartComponent.NO_CHART) {
			_currentChart = createInitialChart();
		}
		return _currentChart;
	}

	private ChartDescription createInitialChart() {
		ChartDescription initialChart;
		if (!_chartFiles.isEmpty()) {
			initialChart = createChartDescription(_chartFiles.get(0));
		} else {
			List<FlexReport> theReports = getStoredReports();
			if (!theReports.isEmpty()) {
				initialChart = createChartDescription(theReports.get(0));
			} else {
				initialChart = null;
			}
		}
		return initialChart;
	}

	/**
	 * This method sets the currentChart.
	 *
	 * @param description
	 *        The currentChart to set.
	 */
	public void setCurrentChart(ChartDescription description) {
		_currentChart = description;
		resetChart();
	}

	/**
	 * The types this cockpit displays.
	 */
	protected List<TLClass> getTypes() {
		return _types;
	}

	/**
	 * Create a chart description for a given {@link FlexReport}.
	 * 
	 * @param report
	 *        Report to get the description for.
	 * 
	 * @return Chart description for given report.
	 */
	protected ChartDescription createChartDescription(FlexReport report) {
		@SuppressWarnings("deprecation")
		String content = (String) report.getValue(FlexReport.ATTRIBUTE_REPORT);

		return new ChartDescription(report.getName(), content, false);
	}

	/**
	 * Create a chart description for a given chart XML file.
	 * 
	 * @param fileName
	 *        Name of the XML file.
	 * 
	 * @return Chart description for that file based chart description.
	 */
	protected ChartDescription createChartDescription(String fileName) {
		Resources resources = Resources.getInstance();
		String chartName = resources.getString(I18NConstants.CHART_FILE_PREFIX.key(fileName));

		return new ChartDescription(chartName, fileName, true);
	}

	/**
	 * Return the stored reports for the current user.
	 * 
	 * @return The known stored reports.
	 */
	protected List<FlexReport> getStoredReports() {
		List<TLClass> types = getTypes();

		if (types.isEmpty()) {
			return Collections.emptyList();
		}

		Set<Object> reports = new HashSet<>();
		Person currentUser = TLContext.getContext().getCurrentPersonWrapper();

		for (TLClass type : getTLClasses(types)) {
			List<?> theReports =
				FlexReport.getStoredReports(currentUser, true, Collections.singleton(type),
				NewStoredConfigChartReportComponent.STORED_REPORT_VERSION);

			reports.addAll(theReports);
		}

		return CollectionUtil.toList(CollectionUtil.dynamicCastView(FlexReport.class, reports));
	}

	/**
	 * Read the file named in the given description into an instance of a {@link ConfigurationItem}.
	 * 
	 * @param description
	 *        The description of a chart.
	 * 
	 * @return The read instance of the configured interface.
	 */
	protected ChartConfig readFileConfig(ChartDescription description) {
		ChartConfig config = Configs.readChartConfig(description.getContent());
		// Note: The config is modified.
		return setChartTitle(description, config);
	}

	/**
	 * Parse the content contained in the given description into an instance of a
	 * {@link ConfigurationItem}.
	 * 
	 * @param description
	 *        The description of a chart.
	 * 
	 * @return The read instance of the configured interface.
	 */
	protected ChartConfig readNoneFileConfig(ChartDescription description) {
		try {
			ChartConfig config =
				readConfig(description.getContent(), ChartConfig.class, Configs.CHART_CONFIG_ROOT_TAG);
			// Note: The config is modified.

			if (_dataSource != null) {
				config.setDataSource(_dataSource);
			}

			return setChartTitle(description, config);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	/**
	 * Parse the given content into an instance of a {@link ConfigurationItem}.
	 * 
	 * @param content
	 *        XML description of the configured instance.
	 * @param clazz
	 *        Expected class to read.
	 * @param rootTag
	 *        Expected name of the root element in the given XML string.
	 * 
	 * @return The read instance of the configured interface.
	 * 
	 * @throws ConfigurationException
	 *         When configuration cannot be read.
	 */
	@SuppressWarnings("unchecked")
	protected <C extends ConfigurationItem> C readConfig(String content, Class<C> clazz, String rootTag)
			throws ConfigurationException {
		ConfigurationDescriptor theDesc = TypedConfiguration.getConfigurationDescriptor(clazz);
		Map<String, ConfigurationDescriptor> theDescMap = Collections.singletonMap(rootTag, theDesc);
		ConfigurationReader theReader =
			new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, theDescMap);

		return (C) theReader.setSource(CharacterContents.newContent(content)).read();
	}

	/**
	 * Modified parts of the given {@link ChartConfig}, by setting the title of the chart.
	 * 
	 * @param description
	 *        The {@link ChartDescription} to get chart title from.
	 * @param config
	 *        The configruation to modify.
	 * 
	 * @return The given configuration for chaining.
	 */
	private ChartConfig setChartTitle(ChartDescription description, ChartConfig config) {
		String chartName = description.getName();

		if (!StringServices.isEmpty(chartName)) {
			JFreeChartBuilder<?> chartBuilder = config.getChartBuilder();
			chartBuilder.getConfig().setTitleKey(ResKey.text(chartName));
		}

		return config;
	}

	private Collection<TLClass> getTLClasses(Collection<? extends TLClass> types) {
		return TLModelUtil.getGeneralizationsOfConcreteSpecializations(ModelService.getApplicationModel(), types);
	}

	private ChartConfig readChartConfig() {
		ChartDescription chart = getCurrentChart();

		if (chart != null) {
			return chart.isFile() ? readFileConfig(chart) : readNoneFileConfig(chart);
		} else {
			return null;
		}
	}

	private List<TLClass> getTypes(Config config) throws ConfigurationException {
		List<TLClass> mes = new ArrayList<>();

		for (String typeSpec : config.getTypes()) {
			TLType type = TLModelUtil.findType(typeSpec);
			if (!(type instanceof TLClass)) {
				ResKey msg = I18NConstants.ERROR_NO_TL_CLASS__TYPE_NAME.fill(typeSpec);
				throw new ConfigurationException(msg, Config.TYPES, null);
			}
			mes.add((TLClass) type);
		}

		return mes;
	}

	/**
	 * Description of the chart (external file or direct description of a chart).
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class ChartDescription {

		/**
		 * @see #getName()
		 */
		private final String _name;

		/**
		 * @see #getContent()
		 */
		private final String _content;

		/**
		 * @see #isFile()
		 */
		private final boolean _isFile;

		/**
		 * Creates a new {@link ChartDescription}.
		 */
		public ChartDescription(String name, String content, boolean isFile) {
			_name = name;
			_content = content;
			_isFile = isFile;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_content == null) ? 0 : _content.hashCode());
			result = prime * result + (_isFile ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChartDescription other = (ChartDescription) obj;
			if (_content == null) {
				if (other._content != null)
					return false;
			} else if (!_content.equals(other._content))
				return false;
			if (_isFile != other._isFile)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return new NameBuilder(this)
				.add("name", _name)
				.add("file", _isFile)
				.build();
		}

		/**
		 * Returns the name of the chart.
		 */
		public String getName() {
			return _name;
		}

		/**
		 * <b>Note: </b> It is essential that the content is just the name of the file or the XML
		 * content which is parsed to {@link ConfigurationItem} and not the
		 * {@link ConfigurationItem} iself.The reason is that the config item derived from the
		 * content is later modified in
		 * {@link CockpitChartComponent#setChartTitle(ChartDescription, ChartConfig)}.
		 * 
		 * @return Returns the content of the chart configuration. Either the direct XML content, or
		 *         the name of the file containing the chart.
		 * 
		 * @see #isFile()
		 */
		public String getContent() {
			return _content;
		}

		/**
		 * Returns <code>true</code> when content is a file name.
		 */
		public boolean isFile() {
			return _isFile;
		}
	}

	/**
	 * {@link LabelProvider} for a {@link ChartDescription}.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class ChartDescriptionLabelProvider implements LabelProvider {

		/** Singleton {@link CockpitChartComponent.ChartDescriptionLabelProvider} instance. */
		public static final ChartDescriptionLabelProvider INSTANCE = new ChartDescriptionLabelProvider();

		private ChartDescriptionLabelProvider() {
			// singleton instance
		}

		@Override
		public String getLabel(Object anObject) {
			return (anObject instanceof ChartDescription) ? ((ChartDescription) anObject).getName() : null;
		}
	}

	/**
	 * Dialog for selecting a chart type of the held {@link CockpitChartComponent}.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class SelectChartTypeDialog extends SimpleFormDialog {

		private static final Document TEMPLATE = DOMUtil.parseThreadSafe(
			"<div"
				+ " xmlns='" + HTMLConstants.XHTML_NS + "'"
				+ " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
				+ " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
				+ ">"
				+ " <div class='mboxImage'>"
				+ "  <t:img key='icon' alt=''/>"
				+ " </div>"
				+ " <div class='mboxHeader'>"
				+ "  <t:text key='header'/>"
				+ " </div>"
				+ " <div class='mboxMessage'>"
				+ "  <t:text key='message'/>"
				+ " </div>"
				+ " <div class='mboxMessage' style='padding-top: 5px;'>"
				+ "  <t:text key='additionalMessage'/>"
				+ " </div>"
				+ " <div class='mboxInput' style='padding-top: 15px;'>"
				+ "  <p:field name='" + INPUT_FIELD + "' />"
				+ HTMLConstants.NBSP
				+ "  <p:field name='" + INPUT_FIELD + "' style='" + FormTemplateConstants.STYLE_ERROR_VALUE + "' />"
				+ " </div>"
				+ "</div>");


		CockpitChartComponent _cockpit;

		/**
		 * Creates a new {@link SelectChartTypeDialog}.
		 * 
		 * @param opener
		 *        Component calling this dialog.
		 */
		public SelectChartTypeDialog(CockpitChartComponent opener) {
			super(opener.getResPrefix(), DisplayDimension.dim(390, DisplayUnit.PIXEL),
				DisplayDimension.dim(225, DisplayUnit.PIXEL));
			_cockpit = opener;
		}

		@Override
		protected void fillButtons(List<CommandModel> someButtons) {
			final SelectField theField = (SelectField) getFormContext().getField(INPUT_FIELD);
			Command setSelection = new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					_cockpit.setCurrentChart((ChartDescription) theField.getSingleSelection());
					return HandlerResult.DEFAULT_RESULT;
				}
			};
			final Command continuation = setSelection;
			someButtons.add(MessageBox.button(ButtonType.OK, new CommandChain(continuation, getDiscardClosure())));
			addCancel(someButtons);
		}

		@Override
		protected void fillFormContext(FormContext context) {
			context.addMember(createSelectField());
		}

		@Override
		protected FormTemplate getTemplate() {
			return defaultTemplate(TEMPLATE, false, _cockpit.getResPrefix());
		}

		private SelectField createSelectField() {
			List<ChartDescription> options = getOptions();
			ChartDescription currentChart = getCurrentChart(options);
			SelectField selectField = FormFactory.newSelectField(SimpleFormDialog.INPUT_FIELD, options);
			int numberOptions = options.size();

			selectField.setAsSingleSelection(currentChart);
			selectField.setOptionLabelProvider(ChartDescriptionLabelProvider.INSTANCE);
			selectField.setMandatory(numberOptions > 0);
			selectField.setDisabled(numberOptions < 1);

			return selectField;
		}

		private ChartDescription getCurrentChart(List<? extends ChartDescription> someOptions) {
			ChartDescription currentChart = _cockpit.getCurrentChart();
			if (currentChart != null) {
				return currentChart;
			}
			if (!someOptions.isEmpty()) {
				return someOptions.get(0);
			}
			return null;
		}

		private List<ChartDescription> getOptions() {
			List<ChartDescription> options = new ArrayList<>();

			for (String theFile : _cockpit.getChartFiles()) {
				options.add(_cockpit.createChartDescription(theFile));
			}

			for (FlexReport report : _cockpit.getStoredReports()) {
				options.add(_cockpit.createChartDescription(report));
			}

			Collections.sort(options, LabelComparator.newInstance(ChartDescriptionLabelProvider.INSTANCE));

			return options;
		}
	}

	/**
	 * Open a {@link SelectChartTypeDialog} for selecting the chart to be displayed now.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class SelectChartTypeCommandHandler extends AbstractCommandHandler {

		/** Unique ID of this command. */
		public static final String COMMAND_ID = "selectChartType";

		/**
		 * Creates a {@link SelectChartTypeCommandHandler}.
		 */
		public SelectChartTypeCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
				Map<String, Object> someArguments) {
			if (aComponent instanceof CockpitChartComponent) {
				return new SelectChartTypeDialog((CockpitChartComponent) aComponent).open(aContext);
			} else {
				return HandlerResult.DEFAULT_RESULT;
			}
		}
	}
}
