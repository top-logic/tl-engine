/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;

/**
 * Simple {@link ChartDataSource} that provides objects for a configured ko-type.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class KOTypeProducer implements ChartDataSource<DataContext>,
		InteractiveBuilder<KOTypeProducer, ChartContextObserver>, ConfiguredInstance<KOTypeProducer.Config> {

	/**
	 * Config-interface for {@link KOTypeProducer}.
	 */
	public interface Config extends PolymorphicConfiguration<KOTypeProducer> {

		/**
		 * the ko-type of the objects this instance should provide
		 */
		public String getObjectType();

	}

	private final Config _config;

	/**
	 * Config-Constructor for {@link KOTypeProducer}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public KOTypeProducer(InstantiationContext aContext, Config aConfig) {
		_config = aConfig;
	}

	@Override
	public Collection<?> getRawData(DataContext context) {
		String type = _config.getObjectType();
		List<Object> result = new ArrayList<>();
		if (StringServices.isEmpty(type)) {
			return result;
		}
		KnowledgeBase kb = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
		result.addAll(WrapperFactory.getWrappersByType(type, kb));
		return result;

	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver arg) {
		// no gui-elements necessary
	}

	@Override
	public KOTypeProducer build(FormContainer container) {
		return this;
	}

}