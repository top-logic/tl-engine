/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.layout.meta.search.PublishableFieldSupport;
import com.top_logic.element.layout.meta.search.QueryUtils;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.layout.Expandable;
import com.top_logic.reporting.flex.search.handler.CreateReportCommand;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class NewStoredConfigChartReportComponent extends AbstractCreateComponent implements Expandable {

	/** The name to be used for creating the new StoredReport. */
	static final int NAME_LENGTH = 40;

	/** <code>NAME_ATTRIBUTE</code>: Name of the name-field in create-dialog */
	public static final String NAME_ATTRIBUTE = "name";

	/**
	 * Configuration for the {@link NewStoredConfigChartReportComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCreateComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@StringDefault(CreateReportCommand.COMMAND_ID)
		@Override
		String getCreateHandler();

		@Override
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			AbstractCreateComponent.Config.super.addAdditionalCommandGroups(additionalGroups);
			additionalGroups.add(CommandGroupRegistry.resolve(QueryUtils.PUBLISH_NAME));
		}

	}

	/**
	 * Config-Constructor for {@link NewStoredConfigChartReportComponent}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	@CalledByReflection
	public NewStoredConfigChartReportComponent(InstantiationContext context, Config config)
			throws ConfigurationException {
		super(context, config);
	}

	@Override
	public FormContext createFormContext() {
		StringField theName =
			FormFactory.newStringField(NAME_ATTRIBUTE, "", false, false,
				new StringLengthConstraint(1, NAME_LENGTH));
		theName.setMandatory(true);

        FormContext theContext = new FormContext("default", this.getResPrefix(), new FormField[] {theName});
		
		theContext = PublishableFieldSupport.createFormContext(this, theContext, this.getResPrefix());
	    
		return theContext;
	}

	@Override
	public boolean isExpanded() {
		return allow(CommandGroupRegistry.resolve(QueryUtils.PUBLISH_NAME));
    }
	
	/** Format version of stored reports using chart-config */
	public static final double STORED_REPORT_VERSION = 2d;
}
