/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.resources;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.themeedit.browser.resource.ThemeResource;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Handler to delete a resource.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DeleteResourceHandler extends AbstractCommandHandler {

	/**
	 * Creates a {@link DeleteResourceHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DeleteResourceHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		ThemeResource selectedThemeResource = getSelectedThemeResource(component);
		if (selectedThemeResource != null) {
			Set<String> resourcePaths = getResourcePaths(selectedThemeResource);
			List<File> idePaths = FileManager.getInstance().getIDEPaths();

			for (File idePath : idePaths) {
				for (String resourcePath : resourcePaths) {
					File themeResourceFile = new File(idePath, resourcePath);

					if (themeResourceFile.exists()) {
						themeResourceFile.delete();
					}
				}
			}

			component.invalidate();
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	Set<String> getResourcePaths(ThemeResource themeResource) {
		return themeResource.getThemeFiles()
			.stream()
			.map(themeFile -> themeFile.getResourcePath())
			.collect(Collectors.toSet());
	}

	ThemeResource getSelectedThemeResource(LayoutComponent component) {
		return (ThemeResource) ((TableComponent) component).getSelected();
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(new ExecutabilityRule() {

			@Override
			public ExecutableState isExecutable(LayoutComponent component, Object model,
					Map<String, Object> someValues) {
				ThemeResource selectedThemeResource = getSelectedThemeResource(component);
				if (selectedThemeResource != null) {
					Set<String> resourcePaths = getResourcePaths(selectedThemeResource);
					List<File> idePaths = FileManager.getInstance().getIDEPaths();

					for (File idePath : idePaths) {
						for (String resourcePath : resourcePaths) {
							File themeResourceFile = new File(idePath, resourcePath);

							if (themeResourceFile.exists()) {
								return ExecutableState.EXECUTABLE;
							}
						}
					}
				}

				return ExecutableState.NO_EXEC_NOT_SUPPORTED;
			}

		}, super.intrinsicExecutability());
	}

}
