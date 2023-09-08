/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.memory;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.knowledge.monitor.UserSession;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.monitoring.session.SessionSearchComponent;
import com.top_logic.reporting.flex.chart.config.datasource.ChartDataSource;
import com.top_logic.reporting.flex.chart.config.datasource.DataContext;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;
import com.top_logic.util.Resources;
import com.top_logic.util.sched.MemoryObserverThread;
import com.top_logic.util.sched.MemoryObserverThread.MemoryUsageEntry;


/**
 * {@link ChartDataSource} for {@link UserSession}s according to the current settings in the
 * {@link SessionSearchComponent}.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class MemoryObserverDataSource implements ChartDataSource<DataContext>,
		InteractiveBuilder<MemoryObserverDataSource, ChartContextObserver>,
		ConfiguredInstance<MemoryObserverDataSource.Config> {

	private static final TagTemplate TEMPLATE = div(verticalBox(fieldBox(Config.RANGES)));

	/**
	 * Config-interface for {@link MemoryObserverDataSource}.
	 */
	public interface Config extends PolymorphicConfiguration<MemoryObserverDataSource> {

		public static final String RANGES = "ranges";

		@Name(RANGES)
		public List<TimeRange> getRanges();

	}

	public interface TimeRange extends ConfigurationItem {

		public int getSeconds();

		public String getKey();

	}


	private final Config _config;

	private int _seconds;

	/**
	 * Config-Constructor for {@link MemoryObserverDataSource}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public MemoryObserverDataSource(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver arg) {

		List<TimeRange> options = getConfig().getRanges();
		List<TimeRange> init = options.subList(1, 2);
		SelectField rangeField = FormFactory.newSelectField(Config.RANGES, options, false, init, true, false, null);
		final Resources resources = Resources.getInstance();
		rangeField.setOptionLabelProvider(new LabelProvider() {

			@Override
			public String getLabel(Object object) {
				return resources.getString(I18NConstants.LABEL
					.key(((TimeRange) object).getKey()));
			}
		});
		container.addMember(rangeField);

		template(container, TEMPLATE);
	}

	@Override
	public MemoryObserverDataSource build(FormContainer container) {
		TimeRange range = (TimeRange) CollectionUtil.getFirst(container.getField(Config.RANGES).getValue());
		_seconds = range.getSeconds();
		return this;
	}

	@Override
	public Collection<? extends MemoryUsageEntry> getRawData(DataContext context) {
		MemoryObserverThread mot = MemoryObserverThread.getInstance();
		long millis = _seconds * 1000;
		int entries = (int) (millis / mot.getLoggingInterval());
		entries = entries > 1 ? entries : 2;
		return mot.getLatestEntries(entries);
	}

}
