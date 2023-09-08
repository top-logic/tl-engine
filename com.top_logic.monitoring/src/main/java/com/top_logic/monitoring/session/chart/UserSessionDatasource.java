/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.session.chart;

import java.util.Collection;
import java.util.Date;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.knowledge.monitor.UserMonitor;
import com.top_logic.knowledge.monitor.UserSession;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.monitoring.session.SessionSearchComponent;
import com.top_logic.reporting.flex.chart.config.datasource.ChartDataSource;
import com.top_logic.reporting.flex.chart.config.datasource.ComponentDataContext;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;


/**
 * {@link ChartDataSource} for {@link UserSession}s according to the curreint settings in the
 * {@link SessionSearchComponent}.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class UserSessionDatasource implements ChartDataSource<ComponentDataContext>,
		InteractiveBuilder<UserSessionDatasource, ChartContextObserver>,
		ConfiguredInstance<UserSessionDatasource.Config> {

	/**
	 * Config-interface for {@link UserSessionDatasource}.
	 */
	public interface Config extends PolymorphicConfiguration<UserSessionDatasource> {
		// necessary for ConfiguredInstance
	}

	private final Config _config;

	/**
	 * Config-Constructor for {@link UserSessionDatasource}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public UserSessionDatasource(InstantiationContext context, Config config) {
		_config = config;
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
	public UserSessionDatasource build(FormContainer container) {
		return this;
	}

	@Override
	public Collection<UserSession> getRawData(ComponentDataContext context) {

		SessionSearchComponent master = (SessionSearchComponent) context.getComponent().getMaster().getMaster();
		
	    FormContext formContext = master.getFormContext();
	    ComplexField startField = (ComplexField)formContext.getMember(SessionSearchComponent.START_DATE_FIELD);
	    ComplexField endField = (ComplexField)formContext.getMember(SessionSearchComponent.END_DATE_FIELD);

	    Date start = (Date)startField.getValue();
	    Date end = (Date)endField.getValue();

		return UserMonitor.userSessions(start, end);
	}

}
