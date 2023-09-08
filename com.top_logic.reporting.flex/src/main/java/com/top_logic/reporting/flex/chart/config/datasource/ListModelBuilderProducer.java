/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;

/**
 * Adapter to use an existing {@link ListModelBuilder} as {@link ChartDataSource}
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class ListModelBuilderProducer implements ChartDataSource<ComponentDataContext>,
		InteractiveBuilder<ListModelBuilderProducer, ChartContextObserver>,
		ConfiguredInstance<ListModelBuilderProducer.Config> {

	/**
	 * Config-interface for {@link ListModelBuilderProducer}.
	 */
	public interface Config extends PolymorphicConfiguration<ListModelBuilderProducer> {

		@Override
		@ClassDefault(ListModelBuilderProducer.class)
		public Class<ListModelBuilderProducer> getImplementationClass();

		/**
		 * the name of the component the list-model-builder creates the objects for
		 */
		ComponentName getMaster();

		/**
		 * Configuration of {@link ListModelBuilder} that provides the objects
		 */
		public PolymorphicConfiguration<ListModelBuilder> getListModelBuilder();
	}

	private final Config _config;

	private final ListModelBuilder _modelBuilder;

	/**
	 * Config-Constructor for {@link ListModelBuilderProducer}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public ListModelBuilderProducer(InstantiationContext aContext, Config aConfig) {
		_config = aConfig;
		_modelBuilder = aContext.getInstance(aConfig.getListModelBuilder());
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Collection<?> getRawData(ComponentDataContext context) {
		Config           theConfig    = getConfig();
		MainLayout       theLayout    = context.getComponent().getMainLayout();
		LayoutComponent  theComponent = theLayout.getComponentByName(theConfig.getMaster());
		Object model = ((Selectable) theComponent).getSelected();
		ListModelBuilder theBuilder = _modelBuilder;

		if (theBuilder.supportsModel(model, theComponent)) {
			Collection<? extends Wrapper> result =
				CollectionUtil.dynamicCastView(Wrapper.class,
					(Collection<?>) theBuilder.getModel(model, theComponent));
			return result;
		}
		else {
			return Collections.emptyList();
		}

	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver observer) {
		// no gui-elements necessary
	}

	@Override
	public ListModelBuilderProducer build(FormContainer container) {
		return this;
	}

}