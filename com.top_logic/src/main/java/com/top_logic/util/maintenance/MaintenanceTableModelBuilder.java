/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.maintenance;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link MaintenanceTableModelBuilder} builds the list of files for the maintenance table
 * component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MaintenanceTableModelBuilder extends AbstractConfiguredInstance<MaintenanceTableModelBuilder.Config>
		implements ListModelBuilder {

	/**
	 * Configuration of {@link MaintenanceTableModelBuilder}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<MaintenanceTableModelBuilder> {

		/**
		 * The name of the folder containing the maintenance pages.
		 */
		@Name("dir")
		@StringDefault("/jsp/administration/maintenance/")
		String getDir();

		/**
		 * The extension of the file names of the maintenance pages.
		 */
		@Name("extension")
		@StringDefault(".jsp")
		String getExtension();
	}

	private final String _extension;

	private final String _maintenancePath;

	/**
	 * Creates a new {@link MaintenanceTableModelBuilder} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link MaintenanceTableModelBuilder}.
	 * 
	 */
	public MaintenanceTableModelBuilder(InstantiationContext context, Config config) {
		super(context, config);
		_extension = config.getExtension();
		_maintenancePath = config.getDir();
		checkMaintenancePath(context, config.location(), _maintenancePath);
	}

	private void checkMaintenancePath(Log log, Location location, String maintenancePath) {
		if (maintenancePath.isEmpty()) {
			log.error("Maintenance path must not be empty at " + location);
			return;
		}
		if (maintenancePath.charAt(0) != '/') {
			log.error("Maintenance path " + maintenancePath + " must start with '/' at " + location);
		}
		if (maintenancePath.charAt(maintenancePath.length() - 1) != '/') {
			log.error("Maintenance path " + maintenancePath + " must end with '/' at " + location);
		}
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent component) {
		Set<String> resources = FileManager.getInstance().getResourcePaths(_maintenancePath);

		return resources.stream().filter(resource -> resource.endsWith(_extension)).collect(Collectors.toList());
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof String;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return contextComponent.getModel();
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return true;
	}

}
