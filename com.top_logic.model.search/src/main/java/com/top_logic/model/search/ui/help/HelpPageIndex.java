/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.help;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.util.ResourcesModule;

/**
 * Index of help pages for display in the TL-Script editor as context sensitive help.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies(SchedulerService.Module.class)
public class HelpPageIndex extends ConfiguredManagedClass<ConfiguredManagedClass.Config<HelpPageIndex>> {

	private Map<String, Index> _indexByLanguage;

	/**
	 * Creates a {@link HelpPageIndex} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public HelpPageIndex(InstantiationContext context, Config<HelpPageIndex> config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();

		createIndex();
	}

	private void createIndex() {
		_indexByLanguage = new HashMap<>();
		for (String locale : ResourcesModule.getInstance().getSupportedLocaleNames()) {
			Index index = new Index(locale);
			_indexByLanguage.put(locale, index);

			SchedulerService.getInstance().execute(index::load);
		}
	}

	/**
	 * Description of a help page.
	 */
	public interface Page {

		/**
		 * The {@link Page} title.
		 */
		String getTitle();

		/**
		 * The {@link Path} containing the HTML contents of the {@link Page}.
		 */
		Path getContent();

	}

	static class PageImpl implements Page {

		private final String _uuid;

		private final String _name;

		private final String _title;

		private final Path _content;

		/**
		 * Creates a {@link Page}.
		 */
		public PageImpl(String uuid, String name, String title, Path content) {
			_uuid = uuid;
			_name = name;
			_title = title;
			_content = content;
		}

		@Override
		public String getTitle() {
			return _title;
		}

		@Override
		public Path getContent() {
			return _content;
		}

		/**
		 * The UUID of the page.
		 */
		public String getUuid() {
			return _uuid;
		}

		/**
		 * The technical page name.
		 */
		public String getName() {
			return _name;
		}

	}

	static class Index {

		private String _locale;

		private Map<String, Page> _byName = new HashMap<>();

		private Map<String, Page> _byUuid = new HashMap<>();

		/**
		 * Creates a {@link Index}.
		 */
		public Index(String locale) {
			_locale = locale;
		}

		/**
		 * Loads and indexes all pages.
		 */
		void load() {
			try {
				tryLoad();
			} catch (RuntimeException | Error ex) {
				Logger.error("Failed to index help pages for language '" + _locale + "'.", ex, HelpPageIndex.class);
				throw ex;
			}
		}

		private void tryLoad() {
			for (Path path : FileManager.getInstance().getPaths()) {
				try {
					tryLoad(path);
				} catch (IOException ex) {
					Logger.error(
						"Failed to index help pages in path '" + path + "' for language '" + _locale + "'.", ex,
						HelpPageIndex.class);
				}
			}
		}

		private void tryLoad(Path path) throws IOException {
			Path localDocRoot = path.resolve("doc/" + _locale);
			if (Files.exists(localDocRoot)) {
				Files.walk(localDocRoot)
					.filter(Index::isPageProperties)
					.forEach(this::addPage);
			}
		}

		private static boolean isPageProperties(Path path) {
			return path.getFileName().toString().equals("page.properties");
		}

		private void addPage(Path path) {
			try {
				tryAddPage(path);
			} catch (IOException ex) {
				Logger.error("Failed to access help page '" + path + "' in locale '" + _locale + "'.", ex,
					HelpPageIndex.class);
			}
		}

		private void tryAddPage(Path path) throws IOException {
			Path parent = path.getParent();
			if (parent == null) {
				return;
			}
			Path content = parent.resolve("index.html");
			if (!Files.exists(content)) {
				return;
			}
			Properties properties = new Properties();
			try (InputStream in = Files.newInputStream(path)) {
				properties.load(in);
			}
			String name = path.getName(path.getNameCount() - 2).toString();
			add(new PageImpl(properties.getProperty("uuid"), name, properties.getProperty("title"), content));
		}

		private void add(PageImpl page) {
			_byName.put(page.getName(), page);
			_byUuid.put(page.getUuid(), page);
		}

		public Page getPage(String name) {
			return _byName.get(name);
		}
	}

	/**
	 * Find the {@link Page} with the given name in the given locale.
	 *
	 * @param language
	 *        The locale to search in.
	 * @param name
	 *        The technical page name.
	 * @return The {@link Page}, or <code>null</code> if no such {@link Page} exists.
	 */
	public static Page getPage(String language, String name) {
		if (!Module.INSTANCE.isActive()) {
			return null;
		}
		return Module.INSTANCE.getImplementationInstance().lookupPage(language, name);
	}

	private Page lookupPage(String language, String name) {
		Index index = _indexByLanguage.get(language);
		if (index == null) {
			return null;
		}

		return index.getPage(name);
	}

	@Override
	protected void shutDown() {
		_indexByLanguage = null;
		super.shutDown();
	}

	/**
	 * Singleton reference to {@link HelpPageIndex}.
	 */
	public static final class Module extends TypedRuntimeModule<HelpPageIndex> {

		/**
		 * {@link Module} singleton.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<HelpPageIndex> getImplementation() {
			return HelpPageIndex.class;
		}
	}

}
