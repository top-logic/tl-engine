/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.jfree.data.general.Dataset;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.reporting.flex.chart.component.ChartConfigComponent;
import com.top_logic.reporting.flex.chart.config.ChartConfig;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveChartBuilder;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.model.DefaultModelPreparation;
import com.top_logic.reporting.flex.search.chart.GenericModelPreparationBuilder;
import com.top_logic.reporting.flex.search.chart.SearchChartBuilder;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class SearchResultChartConfigComponent extends ChartConfigComponent {

	/**
	 * Configuration options for {@link SearchResultChartConfigComponent}.
	 */
	public interface Config extends ChartConfigComponent.Config {

		/**
		 * Name of the query selector component.
		 */
		@Mandatory
		ComponentName getQuerySelector();
		
	}

	/**
	 * Creates a {@link SearchResultChartConfigComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SearchResultChartConfigComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	/**
	 * Query selector component.
	 */
	public Selectable getSelector() {
		return (Selectable) getMainLayout().getComponentByName(((Config) getConfig()).getQuerySelector());
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return object == null || object instanceof AttributedSearchResultSet;
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		if (metaElementChange(oldModel, newModel)) {
			resetBaseConfig();
		}
		super.afterModelSet(oldModel, newModel);
	}

	private boolean metaElementChange(Object oldModel, Object newModel) {
		return !types(oldModel).equals(types(newModel));
	}

	private Set<? extends TLClass> types(Object model) {
		if (model instanceof AttributedSearchResultSet) {
			return ((AttributedSearchResultSet) model).getTypes();
		}
		return Collections.emptySet();
	}

	@Override
	public FormContext createFormContext() {
		final FormContext result = super.createFormContext();
		return result;
	}

	/**
	 * Resets the GUI by resetting the base config to the initial state and removing the
	 * {@link FormContext}.
	 */
	public void resetGUI() {
		resetBaseConfig();
	}

	/**
	 * Initializes the config GUI based on the given XML serialized {@link ChartConfig}.
	 * 
	 * @param string
	 *        the serialized {@link ChartConfig} to initialize the config-GUI with.
	 */
	public void loadConfig(String string) {
		Map<String, ConfigurationDescriptor> map = Collections.singletonMap("chart", TypedConfiguration.getConfigurationDescriptor(ChartConfig.class));
		Content content = CharacterContents.newContent(string);
		try {
			ConfigurationReader reader = new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, map);
			ChartConfig config = (ChartConfig) reader.setSource(content).read();
			loadConfig(config);
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Initializes the config-GUI based on the given {@link ChartConfig}.
	 */
	public void loadConfig(ChartConfig newConfig) {
		resetBaseConfig();

		DefaultModelPreparation newModelPreparation = (DefaultModelPreparation) newConfig.getModelPreparation();
		JFreeChartBuilder<?> newChartBuilder = newConfig.getChartBuilder();

		InteractiveChartBuilder.Config baseConfig = getBaseConfig();
		GenericModelPreparationBuilder modelPreparationBuilder =
			(GenericModelPreparationBuilder) baseConfig.getModelPreparation();
		modelPreparationBuilder.loadModelPreparation(newModelPreparation.getConfig());
		SearchChartBuilder chartBuilder = (SearchChartBuilder) baseConfig.getChartBuilder();
		chartBuilder.setChartBuilder(newChartBuilder);

		setBaseConfig(baseConfig);

		removeFormContext();
	}

	/**
	 * the serialized String of the current {@link ChartConfig}
	 */
	public String getConfigString() {
		ChartConfig config = createChartConfig();

		Writer writer = new StringWriter();
		try {
			new ConfigurationWriter(writer).write("chart", TypedConfiguration.getConfigurationDescriptor(ChartConfig.class), config);
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				writer.close();
			} catch (IOException ex) {
				// should not happen
			}
		}
		return writer.toString();
	}

	/**
	 * Updates the {@link ChartContextObserver} with the new created {@link Dataset} and
	 * {@link ChartTree}.
	 */
	public void notify(Dataset dataSet, ChartTree tree) {
		ChartContextObserver observer = ChartContextObserver.getObserver(this);
		if (observer != null) {
			observer.updateResult(dataSet, tree);
		}
	}

	/**
	 * {@link CommandHandler} opening the "Save as..." dialog.
	 */
	public static class SaveAs extends OpenModalDialogCommandHandler {

		/**
		 * Creates a {@link SearchResultChartConfigComponent.SaveAs} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public SaveAs(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
				Object model, Map<String, Object> someArguments) {

			// Make sure that the saved configuration does not contain errors.
			FormContext configContext = ((FormComponent) aComponent).getFormContext();
			if (!configContext.checkAll()) {
				HandlerResult result = new HandlerResult();
				AbstractApplyCommandHandler.fillHandlerResultWithErrors(null, configContext, result);
				return result;
			}

			return super.handleCommand(aContext, aComponent, model, someArguments);
		}

	}

	/**
	 * {@link ExecutabilityRule} that controls the "Save as..." button.
	 */
	public static class CanSaveSettings implements ExecutabilityRule {

		private static final ExecutableState DISABLED_NO_CONFIG =
			ExecutableState.createDisabledState(I18NConstants.NO_CHART_CONFIG);

		/**
		 * Singleton {@link SearchResultChartConfigComponent.CanSaveSettings} instance.
		 */
		public static final SearchResultChartConfigComponent.CanSaveSettings INSTANCE =
			new SearchResultChartConfigComponent.CanSaveSettings();

		private CanSaveSettings() {
			// Singleton constructor.
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			AttributedSearchResultSet resultSet = (AttributedSearchResultSet) model;
			if (resultSet == null) {
				return DISABLED_NO_CONFIG;
			}
			if (resultSet.getTypes().isEmpty()) {
				return DISABLED_NO_CONFIG;
			}

			return ExecutableState.EXECUTABLE;
		}

	}

}
