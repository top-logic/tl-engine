package com.top_logic.layout.themeedit.browser.providers.resources;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.themeedit.browser.resource.ThemeResource;
import com.top_logic.layout.tree.component.TreeComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.ModelEventListener;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/**
 * Handler to delete a resource folder.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DeleteResourceFolderHandler extends AbstractCommandHandler {

	/**
	 * Creates a {@link DeleteResourceFolderHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DeleteResourceFolderHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		TreeComponent tree = (TreeComponent) component;
		ThemeResource themeResource = (ThemeResource) tree.getSelected();

		deleteThemeResourceTransient(component, tree, themeResource);
		deleteThemeResourcePersistent(themeResource);

		return HandlerResult.DEFAULT_RESULT;
	}

	private void deleteThemeResourcePersistent(ThemeResource themeResource) {
		Set<String> resourcePaths = getResourcePaths(themeResource);
		List<File> idePaths = FileManager.getInstance().getIDEPaths();

		for (File idePath : idePaths) {
			for (String resourcePath : resourcePaths) {
				File resourceFile = new File(idePath, resourcePath);

				if (resourceFile.exists()) {
					FileUtilities.deleteR(resourceFile);
				}
			}
		}
	}

	Set<String> getResourcePaths(ThemeResource themeResource) {
		return themeResource.getThemeFiles()
			.stream()
			.map(themeFile -> themeFile.getResourcePath())
			.collect(Collectors.toSet());
	}

	private void deleteThemeResourceTransient(LayoutComponent component, TreeComponent tree, ThemeResource resource) {
		tree.handleModelEvent(resource, component, ModelEventListener.MODEL_DELETED);
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(new ExecutabilityRule() {

			@Override
			public ExecutableState isExecutable(LayoutComponent component, Object model,
					Map<String, Object> someValues) {
				ThemeResource themeResource = (ThemeResource) ((TreeComponent) component).getSelected();
				Set<String> resourcePaths = getResourcePaths(themeResource);
				List<File> idePaths = FileManager.getInstance().getIDEPaths();

				for (File idePath : idePaths) {
					for (String resourcePath : resourcePaths) {
						File resourceFile = new File(idePath, resourcePath);

						if (resourceFile.exists()) {
							return ExecutableState.EXECUTABLE;
						}
					}
				}

				return ExecutableState.NO_EXEC_NOT_SUPPORTED;
			}

		}, super.intrinsicExecutability());
	}
}
