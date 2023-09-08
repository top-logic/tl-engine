/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.maintenance;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.component.ResourceReferenceFormat;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Will show pages select in its master using page as base directory.
 * 
 * @author    <a href=mailto:fma@top-logic.com>fma</a>
 */
public class MaintenanceComponent extends FormComponent{
    
	/**
	 * Configuration for {@link MaintenanceComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FormComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/** Property name of {@link #getNoSelectionPage()}. */
		String NO_SELECTION_PAGE = "noSelectionPage";

		/** The default value for {@link #getNoSelectionPage()}. */
		String DEFAULT_NO_SELECTION_PAGE = "/jsp/administration/noMaintenancePageSelected.jsp";

		/** The JSP to display when no maintenance page is selected. */
		@Name(NO_SELECTION_PAGE)
		@StringDefault(DEFAULT_NO_SELECTION_PAGE)
		@Format(ResourceReferenceFormat.class)
		String getNoSelectionPage();

		@Override
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			FormComponent.Config.super.addAdditionalCommandGroups(additionalGroups);
			/* SimpleBoundCommandGroup.WRITE is the command group used in AccessControlTag to refuse
			 * access if the user has not the right. For this reason the command group must be
			 * registered. */
			additionalGroups.add(SimpleBoundCommandGroup.WRITE);
		}

	}

    /**
	 * Construct a {@link MaintenanceComponent}.
	 */
	public MaintenanceComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		setPage(Config.DEFAULT_NO_SELECTION_PAGE);
    }

    @Override
	protected boolean supportsInternalModel(Object object) {
		return object instanceof String;
    }
    
	@Override
	public FormContext createFormContext() {
		return null;
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		if (newModel == null) {
			setPage(Config.DEFAULT_NO_SELECTION_PAGE);
		} else {
			setPage((String) newModel);
		}
		invalidate();
		super.afterModelSet(oldModel, newModel);
	}

}
