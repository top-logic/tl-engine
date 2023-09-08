/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.SetBuilder;
import com.top_logic.basic.config.DeclarativeConfigDescriptor;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.io.FileSystemCache;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.PathUpdate;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.editor.DynamicComponentDefinition.UnsupportedFormatException;
import com.top_logic.mig.html.layout.LayoutUtils;

/**
 * Module that holds all {@link DynamicComponentDefinition}s in the system.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies({
	FileSystemCache.Module.class,
	SafeHTML.Module.class,
})
public class DynamicComponentService extends ConfiguredManagedClass<DynamicComponentService.Config> {

	/**
	 * Typed configuration interface definition for {@link DynamicComponentService}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends ConfiguredManagedClass.Config<DynamicComponentService> {
		// No properties yet.
	}

	private static final String PROPERTIES_PREFIX_SUFFIX = "properties";

	/**
	 * Suffix for a dynamic component label.
	 */
	public static final String LABEL_KEY_SUFFIX = "label";

	private Map<String, DynamicComponentDefinition> _allDefinitions;

	private Iterator<PathUpdate> _updates;

	/**
	 * Create a {@link DynamicComponentService}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public DynamicComponentService(InstantiationContext context, Config config) {
		super(context, config);

		_allDefinitions = fetchDefinitions(context);
	}

	@Override
	protected void startUp() {
		super.startUp();

		_updates = FileSystemCache.getCache().getUpdates();
	}

	@Override
	protected void shutDown() {
		_updates = null;

		super.shutDown();
	}

	private Map<String, DynamicComponentDefinition> fetchDefinitions(Log context) {
			Map<String, DynamicComponentDefinition> definitions = new HashMap<>();
			FileManager fileManager = FileManager.getInstance();
			Set<String> dynamicLayoutNames = findRelativePathNames();
			for (String fileName : dynamicLayoutNames) {
				{
					DynamicComponentDefinition definition = parseTemplate(context, fileManager.getData(fileName));
					if (definition != null) {
						definitions.put(definition.definitionFile(), definition);
					}
				}
			}
			return definitions;
	}

	private DynamicComponentDefinition parseTemplate(Log context, BinaryData templateData) {
		DynamicComponentDefinition definition;
		try {
			definition = DynamicComponentDefinition.parseTemplate(context, templateData);
			if (definition == null) {
				return null;
			}
		} catch (IOException exception) {
			context.error("Unable to parse template file: " + templateData, exception);
			return null;
		} catch (UnsupportedFormatException ex) {
			context.info("File " + templateData + " does not contain dynamic layout configuration.");
			return null;
		}
		ResPrefix prefix = LayoutTemplateUtils.getPrefixResKey(definition.definitionFile());
		definition.setLabelKey(prefix.key(LABEL_KEY_SUFFIX));
		if (definition.descriptor() instanceof DeclarativeConfigDescriptor) {
			DeclarativeConfigDescriptor descriptor = (DeclarativeConfigDescriptor) definition.descriptor();
			if (descriptor.getResPrefix().isEmpty()) {
				descriptor.setResPrefix(prefix.append(PROPERTIES_PREFIX_SUFFIX).toPrefix());
			}
		}
		return definition;
	}

	private Set<String> findRelativePathNames() {
		Set<String> layoutResources = FileUtilities.getAllResourcePaths(ModuleLayoutConstants.LAYOUT_RESOURCE_PREFIX);

		return layoutResources.stream().filter(resource -> LayoutUtils.isTemplate(resource))
			.collect(Collectors.toSet());
	}

	/**
	 * All {@link DynamicComponentDefinition}s found in the file system. May be empty, if no layout
	 * templates are found in the file system, or something went wrong during startup.
	 */
	public Collection<DynamicComponentDefinition> getComponentDefinitions() {
		upateTemplateCache();
		return _allDefinitions.values();
	}

	/**
	 * The {@link DynamicComponentDefinition} loaded from the layout template with the given name.
	 * 
	 * @return The {@link DynamicComponentDefinition} loaded from the given template name, or
	 *         <code>null</code>, if no such template exists.
	 */
	public DynamicComponentDefinition getComponentDefinition(String templateName) {
		upateTemplateCache();
		return _allDefinitions.get(templateName);
	}

	private void upateTemplateCache() {
		FileSystemCache.getCache().fetchUpdates();
		while (_updates.hasNext()) {
			PathUpdate update = _updates.next();

			BufferingProtocol log = new BufferingProtocol();
			addChangedTemplates(log, update);
			removeDeletedTemplates(update);
			if (log.hasErrors()) {
				Logger.error(log.getError(), log.getFirstProblem(), DynamicComponentDefinition.class);
				log.getErrors().forEach(error -> InfoService.showError(ResKey.text(error)));
			}
		}
	}

	private void removeDeletedTemplates(PathUpdate update) {
		for (Path path : update.getDeletions()) {
			if (LayoutUtils.isTemplate(path.getFileName().toString())) {
				Path layoutDirectory = LayoutUtils.getLayoutDirectory(path);
				if (layoutDirectory == null) {
					continue;
				}
				String definitionFile = FileUtilities.getRelativizedPath(layoutDirectory, path);

				_allDefinitions.remove(definitionFile);
			}
		}
	}

	private void addChangedTemplates(BufferingProtocol log, PathUpdate update) {
		for (Path path : getChangedTemplates(update)) {
			if (LayoutUtils.isTemplate(path.toString())) {
				DynamicComponentDefinition definition =
					parseTemplate(log, BinaryDataFactory.createBinaryData(path.toFile()));

				if (definition != null) {
					_allDefinitions.put(definition.definitionFile(), definition);
				}
			}
		}
	}

	private Set<Path> getChangedTemplates(PathUpdate update) {
		return new SetBuilder<Path>().addAll(update.getChanges()).addAll(update.getCreations()).toSet();
	}

	/**
	 * The singleton {@link DynamicComponentService} instance.
	 */
	public static DynamicComponentService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton holder for the {@link DynamicComponentService}.
	 */
	public static final class Module extends TypedRuntimeModule<DynamicComponentService> {

		/**
		 * Singleton {@link DynamicComponentService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<DynamicComponentService> getImplementation() {
			return DynamicComponentService.class;
		}
	}

}
