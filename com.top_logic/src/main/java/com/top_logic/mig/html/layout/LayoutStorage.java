/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static com.top_logic.mig.html.layout.PersistentLayoutWrapper.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.NamedRegexp;
import com.top_logic.basic.io.FileSystemCache;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.PathUpdate;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.gui.layout.LayoutConfig;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.KBBasedManagedClass;
import com.top_logic.knowledge.service.db2.SimpleQuery;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.editor.DatabaseLayoutCache;
import com.top_logic.layout.editor.DynamicComponentService;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.layout.processor.LayoutResolver;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Storage of {@link LayoutComponent.Config}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies({
	// When the theme is restarted, all layouts must be parsed again.
	ThemeFactory.Module.class,
	CommandGroupRegistry.Module.class,
	DynamicComponentService.Module.class,
	FileSystemCache.Module.class,
})
public class LayoutStorage extends KBBasedManagedClass<LayoutStorage.Config> {

	/**
	 * Configuration options for {@link LayoutStorage}.
	 */
	public interface Config extends KBBasedManagedClass.Config<LayoutStorage> {

		/** Configuration name of the value of {@link #getExcludePatterns()}. */
		String EXCLUDE_PATTERNS = "excludePatterns";

		/**
		 * If a layout name matches one of these patterns, it is <b>not</b> loaded.
		 * 
		 * @see LayoutStorage#readLayouts()
		 */
		@Name(EXCLUDE_PATTERNS)
		@com.top_logic.basic.config.annotation.Key(NamedRegexp.NAME_ATTRIBUTE)
		List<NamedRegexp> getExcludePatterns();

	}

	/**
	 * Constant that is returned when this storage does not contain a valid
	 * {@link LayoutComponent.Config} for the given layout name.
	 */
	public static LayoutComponent.Config NO_VALID_CONFIGURATION = null;

	private final CompiledQuery<PersistentTemplateLayoutWrapper> _personalTemplateLayouts;

	private final CompiledQuery<PersistentTemplateLayoutWrapper> _globalLayouts;

	private final CompiledQuery<PersistentTemplateLayoutWrapper> _allPersonalTemplateLayouts;

	private final LayoutCache _dbCache = new DatabaseLayoutCache(kb());

	private LayoutCache _filesystemCache;

	private Map<String, List<BinaryData>> _overlays = Collections.emptyMap();

	private Iterator<PathUpdate> _updates;

	/**
	 * Whether the layouts are being loaded for one of the themes.
	 * <p>
	 * Is <code>false</code> between the themes: When loading for one theme is finished but loading
	 * for the next one has not yet started.
	 * </p>
	 */
	private volatile boolean _loading = false;

	/** Whether the layouts have been loaded for all themes. */
	private volatile boolean _everythingLoaded;

	/**
	 * Creates a {@link LayoutStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LayoutStorage(InstantiationContext context, Config config) {
		super(context, config);
		_personalTemplateLayouts = createPersonalTemplateLayoutQuery();
		_globalLayouts = createGlobalTemplateLayoutQuery();
		_allPersonalTemplateLayouts = createAllPersonalTemplateLayoutsQuery();
	}

	private CompiledQuery<PersistentTemplateLayoutWrapper> createAllPersonalTemplateLayoutsQuery() {
		return createPersonalLayoutsQuery();
	}

	private CompiledQuery<PersistentTemplateLayoutWrapper> createGlobalTemplateLayoutQuery() {
		String layoutKeyParam = "layoutKey";
		Expression personCheck =
			isNull(reference(PersistentTemplateLayoutWrapper.KO_NAME_TEMPLATE_LAYOUTS, PERSON_ATTR));
		SimpleQuery<PersistentTemplateLayoutWrapper> query =
			createTemplateLayoutSearchQuery(personCheck, layoutKeyParam);
		query.setQueryParameters(CollectionFactory.list(paramDecl(MOPrimitive.STRING, layoutKeyParam)));
		return kb().compileSimpleQuery(query);
	}

	private CompiledQuery<PersistentTemplateLayoutWrapper> createPersonalTemplateLayoutQuery() {
		String layoutKeyParam = "layoutKey";
		String personParam = "person";
		Expression personCheck =
			eqBinary(reference(PersistentTemplateLayoutWrapper.KO_NAME_TEMPLATE_LAYOUTS, PERSON_ATTR),
				param(personParam));
		SimpleQuery<PersistentTemplateLayoutWrapper> query = createTemplateLayoutSearchQuery(personCheck, layoutKeyParam);

		query.setQueryParameters(CollectionFactory.list(paramDecl(Person.OBJECT_NAME, personParam),
			paramDecl(MOPrimitive.STRING, layoutKeyParam)));
		return kb().compileSimpleQuery(query);
	}

	private CompiledQuery<PersistentTemplateLayoutWrapper> createPersonalLayoutsQuery() {
		String personParam = "person";
		Expression personCheck =
			eqBinary(reference(PersistentTemplateLayoutWrapper.KO_NAME_TEMPLATE_LAYOUTS, PERSON_ATTR),
				param(personParam));

		MetaObject type =
			kb().getMORepository().getMetaObject(PersistentTemplateLayoutWrapper.KO_NAME_TEMPLATE_LAYOUTS);
		SimpleQuery<PersistentTemplateLayoutWrapper> query =
			SimpleQuery.queryResolved(PersistentTemplateLayoutWrapper.class, type, personCheck);

		query.setQueryParameters(CollectionFactory.list(paramDecl(Person.OBJECT_NAME, personParam)));
		return kb().compileSimpleQuery(query);
	}

	private SimpleQuery<PersistentTemplateLayoutWrapper> createTemplateLayoutSearchQuery(Expression personCheck,
			String layoutKeyParam) {
		MetaObject type =
			kb().getMORepository().getMetaObject(PersistentTemplateLayoutWrapper.KO_NAME_TEMPLATE_LAYOUTS);
		Expression layoutCheck =
			eqBinary(attribute(PersistentTemplateLayoutWrapper.KO_NAME_TEMPLATE_LAYOUTS, LAYOUT_KEY_ATTR),
				param(layoutKeyParam));
		Expression search = and(personCheck, layoutCheck);
		return SimpleQuery.queryResolved(PersistentTemplateLayoutWrapper.class, type, search);
	}

	private MetaObject templateLayoutType() {
		return kb().getMORepository().getMetaObject(PersistentTemplateLayoutWrapper.KO_NAME_TEMPLATE_LAYOUTS);
	}

	@Override
	protected void startUp() {
		super.startUp();

		setEverythingLoaded(false);
		_updates = FileSystemCache.getCache().getUpdates();
		initFilesystemCache();
	}

	@Override
	protected synchronized void shutDown() {
		while (_loading) {
			try {
				Logger.info("Waiting for loading thread to terminate.", LayoutStorage.class);
				wait(1000);
			} catch (InterruptedException ex) {
				// Ignore.
			}
		}

		_updates = null;

		super.shutDown();
	}

	private void initFilesystemCache() {
		_filesystemCache = new DefaultLayoutCache();
		_overlays = readLayouts();
		fetchAvailableLayouts();
	}

	private void fetchAvailableLayouts() {
		Collection<String> layoutNames = _overlays.keySet();
		ThemeFactory themeFactory = ThemeFactory.getInstance();
		List<Theme> choosableThemes =
			CollectionUtil.topsort(theme -> theme.getParentThemes(), themeFactory.getChoosableThemes(), false);

		SchedulerService.Module schedulerService = SchedulerService.Module.INSTANCE;
		if (schedulerService.isActive()) {
			loadLayoutsForDefaultTheme(layoutNames, choosableThemes);

			/* Schedule delayed to ensure that "isStarted" is set. */
			schedulerService.getImplementationInstance()
				.schedule(() -> ThreadContextManager.inSystemInteraction(LayoutStorage.class,
					() -> loadLayoutsForNonDefaultThemes(layoutNames, choosableThemes)), 10, TimeUnit.MILLISECONDS);
		} else {
			loadLayoutsForAllThemes(layoutNames, choosableThemes);
		}

	}

	private void loadLayoutsForNonDefaultThemes(Collection<String> layoutNames, Iterable<Theme> choosableThemes) {
		Protocol log = newLog();
		log.info("Loading layouts for non default themes.");
		StopWatch timer = StopWatch.createStartedWatch();
		for (Theme theme : choosableThemes) {
			if (!lock()) {
				log.info("Skip loading of remaining themes, as this service is not started.");
				return;
			}
			try {
				fetchAvailableLayouts(log, layoutNames, theme, true);
			} finally {
				unlock();
			}
		}
		setEverythingLoaded(true);
		log.info("Loading layouts for non default themes needed " + timer + ".");
	}

	private synchronized boolean lock() {
		if (!isStarted()) {
			return false;
		}
		_loading = true;
		return true;
	}

	private synchronized void unlock() {
		_loading = false;
		notifyAll();
	}

	private synchronized void setEverythingLoaded(boolean value) {
		_everythingLoaded = value;
		notifyAll();
	}

	/** Wait until the layouts have been loaded for every theme. */
	public synchronized void awaitEverythingLoaded() throws InterruptedException {
		while (!_everythingLoaded) {
			wait();
		}
	}

	private void loadLayoutsForDefaultTheme(Collection<String> layoutNames, List<Theme> choosableThemes) {
		Theme defaultTheme = ThemeFactory.getInstance().getDefaultTheme();
		Protocol log = newLog();
		StopWatch timer = StopWatch.createStartedWatch();
		loadLayoutsThemeRecursivly(log, layoutNames, defaultTheme, choosableThemes);
		log.info("Loading layouts for default theme " + defaultTheme.getName() + " and parents needed " + timer + ".");
	}

	private void loadLayoutsThemeRecursivly(Protocol log, Collection<String> layoutNames, Theme currentTheme,
			List<Theme> choosableThemes) {
		for (Theme parentTheme : CollectionUtilShared.nonNull(currentTheme.getParentThemes())) {
			loadLayoutsThemeRecursivly(log, layoutNames, parentTheme, choosableThemes);
		}
		if (!choosableThemes.remove(currentTheme)) {
			// was already processed or is not choosable.
			return;
		}
		fetchAvailableLayouts(log, layoutNames, currentTheme, false);
	}

	private void loadLayoutsForAllThemes(Collection<String> layoutNames, Collection<Theme> themes) {
		Protocol log = newLog();
		StopWatch timer = StopWatch.createStartedWatch();
		themes.forEach(theme -> fetchAvailableLayouts(log, layoutNames, theme, false));
		log.info("Loading layouts for all themes needed " + timer + ".");
	}

	static LogProtocol newLog() {
		return new LogProtocol(LayoutStorage.class);
	}

	/**
	 * Reads layout files from the file system and fill the arguments with the result.
	 */
	public static Map<String, List<BinaryData>> readLayouts() {
		Map<String, List<BinaryData>> overlaysByLayoutKey = new HashMap<>();
		Set<String> mainLayouts =
			CollectionUtil.toSet(ApplicationConfig.getInstance().getConfig(LayoutConfig.class).getLayouts());
		List<NamedRegexp> excludePatterns = getExcludePatterns();

		FileManager fileManager = FileManager.getInstance();
		Set<String> layoutResources = fileManager.getResourcePaths(ModuleLayoutConstants.LAYOUT_RESOURCE_PREFIX);
		Set<String> readLayouts = new HashSet<>();

		for (String layoutResource : layoutResources) {
			// Do not read theme specific layouts.
			if (layoutResource.endsWith(Theme.LAYOUT_PREFIX + '/')) {
				continue;
			}

			if (fileManager.isDirectory(layoutResource)) {
				read(readLayouts, overlaysByLayoutKey, excludePatterns, mainLayouts,
					FileUtilities.getAllResourcePaths(layoutResource));
			}

			read(readLayouts, overlaysByLayoutKey, excludePatterns, mainLayouts, layoutResource);
		}

		/* Overlays are sorted in FileManager order i.e. from more specific to less specific.
		 * Inverse order is needed. */
		overlaysByLayoutKey.values().forEach(Collections::reverse);
		for (String layoutName : readLayouts) {
			if (!overlaysByLayoutKey.containsKey(layoutName)) {
				overlaysByLayoutKey.put(layoutName, Collections.emptyList());
			}
		}

		return overlaysByLayoutKey;

	}

	private static void read(Set<String> readLayouts, Map<String, List<BinaryData>> overlaysByLayoutKey,
			List<NamedRegexp> excludePatterns, Set<String> mainLayouts,
			Set<String> allResourcePaths) {
		for (String resource : allResourcePaths) {
			read(readLayouts, overlaysByLayoutKey, excludePatterns, mainLayouts, resource);
		}
	}

	private static void read(Set<String> readLayouts, Map<String, List<BinaryData>> overlaysByLayoutKey,
			List<NamedRegexp> excludePatterns, Set<String> mainLayouts,
			String layoutResource) {
		if (FileManager.getInstance().isDirectory(layoutResource)) {
			return;
		}
		String layoutKey = LayoutUtils.getLayoutKey(layoutResource);
		if (excludeLayout(excludePatterns, layoutKey)) {
			return;
		}

		if (layoutKey.endsWith(LayoutModelConstants.LAYOUT_XML_FILE_SUFFIX) || mainLayouts.contains(layoutKey)) {
			readLayouts.add(layoutKey);
		}

		if (layoutKey.endsWith(LayoutModelConstants.LAYOUT_XML_OVERLAY_FILE_SUFFIX)) {
			String overlayedLayoutKey = getOverlayedLayoutKey(layoutKey);
			try {
				List<BinaryData> layoutOverlays = FileManager.getInstance().getDataOverlays(layoutResource);
				overlaysByLayoutKey.put(overlayedLayoutKey, layoutOverlays);
			} catch (IOException exception) {
				Logger.error("Failed to load overlays for layout '" + overlayedLayoutKey + "'.", exception,
					LayoutStorage.class);
			}

		}
	}

	private static String getOverlayedLayoutKey(String layoutKey) {
		return StringServices.changeSuffix(layoutKey, LayoutModelConstants.LAYOUT_XML_OVERLAY_FILE_SUFFIX,
			LayoutModelConstants.LAYOUT_XML_FILE_SUFFIX);
	}

	private static List<NamedRegexp> getExcludePatterns() {
		Config conf;
		if (LayoutStorage.Module.INSTANCE.isActive()) {
			conf = LayoutStorage.getInstance().getConfig();
		} else {
			try {
				conf = (Config) ApplicationConfig.getInstance().getServiceConfiguration(LayoutStorage.class);
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}
		}
		return conf.getExcludePatterns();
	}

	private static boolean excludeLayout(List<NamedRegexp> excludePatterns, String layoutKey) {
		if (excludePatterns.isEmpty()) {
			return false;
		}
		for (NamedRegexp excludePattern : excludePatterns) {
			if (excludePattern.getPattern().matcher(layoutKey).matches()) {
				return true;
			}
		}
		return false;
	}

	private void fetchAvailableLayouts(Protocol log, Collection<String> layoutNames, Theme theme,
			boolean calledDelayed) {
		BufferingProtocol bufferProtocol = new BufferingProtocol();
		LayoutResolver resolver = LayoutResolver.newRuntimeResolver(bufferProtocol, theme);
		ThemeFactory.getInstance().withTheme(theme, new Computation<Void>() {

			@Override
			public Void run() {
				BufferingProtocol bufferProtocolTmp = bufferProtocol;

				for (String layoutName : layoutNames) {
					if (calledDelayed && !isStarted()) {
						return null;
					}
					installLayout(resolver, layoutName);
					try {
						bufferProtocolTmp.checkErrors();
					} catch (Exception ex) {
						log.error("Unable to fetch layout '" + layoutName + "' for theme '" + theme.getName() + "'.",
							ex);
						/* Set new protocol that next layout is loaded without the problems from
						 * current layout. */
						bufferProtocolTmp = new BufferingProtocol();
						resolver.setProtocol(bufferProtocolTmp);
					}
				}
				return null;
			}
		});

	}

	final LayoutComponent.Config installLayout(LayoutResolver resolver, String layoutName) {
		Protocol log = resolver.getProtocol();
		InstantiationContext context = new DefaultInstantiationContext(log);
		CreateComponentParameter parameters = CreateComponentParameter.newParameter(layoutName);
		parameters.setLayoutResolver(resolver);
		return installLayout(context, parameters);
	}

	private void putToFilesystemCache(Theme theme, String layoutKey, TLLayout layout) {
		_filesystemCache.putLayout(theme, null, layoutKey, layout);
	}

	/**
	 * Installs a layout read from the filesystem by updating the corresponding cache.
	 * 
	 * @see DefaultLayoutCache
	 */
	public LayoutComponent.Config installLayout(InstantiationContext context, CreateComponentParameter parameters) {
		updateFilesystemCache();
		{
			List<BinaryData> overlays = _overlays.getOrDefault(parameters.getLayoutName(), Collections.emptyList());

			try {
				TLLayout layout = createLayout(context, parameters, overlays);

				if (!context.hasErrors()) {
					putToFilesystemCache(parameters.getLayoutResolver().getTheme(), parameters.getLayoutName(), layout);
				}

				return layout.get();
			} catch (ConfigurationException | IOException exception) {
				log(exception, parameters);

				return null;
			}
		}
	}

	private void log(Exception exception, CreateComponentParameter parameters) {
		LayoutResolver resolver = parameters.getLayoutResolver();

		StringBuilder error = new StringBuilder();
		error.append("Unable to install configuration for layout '");
		error.append(parameters.getLayoutName());
		error.append("' and theme '");
		error.append(resolver.getTheme());
		error.append("'.");
		resolver.getProtocol().error(error.toString(), exception);
	}

	/**
	 * Creates a component layout.
	 * 
	 * @param context
	 *        The context for configuration instantiations.
	 * @param parameters
	 *        Parameter object to get necessary informations from.
	 * @param overlays
	 *        Overlay layout files for {@link CreateComponentParameter#getLayoutName()}.
	 */
	public static TLLayout createLayout(InstantiationContext context, CreateComponentParameter parameters,
			List<BinaryData> overlays) throws ConfigurationException, IOException {
		ComponentConfigurationBuilder builder = new ComponentConfigurationBuilder();

		builder.setContext(context);
		// check layoutresolver has always a theme!
		builder.setLayoutResolver(parameters.getLayoutResolver());
		builder.setLayoutName(parameters.getLayoutName());
		builder.setLayoutScope(parameters.getLayoutNameScope());
		builder.setTemplateArguments(parameters.getTemplateArguments());
		builder.setOverlays(overlays);

		return builder.build();
	}

	/**
	 * Removes the {@link TLLayout} for the given identifier from the cache holding all layouts read
	 * from the filesystem.
	 * 
	 * @param theme
	 *        {@link Theme} containing the layout.
	 * 
	 * @param layoutKey
	 *        Layout identifier.
	 * 
	 * @see DefaultLayoutCache
	 */
	public void removeLayout(Theme theme, String layoutKey) {
		updateFilesystemCache();
		{
			_filesystemCache.removeLayout(theme, null, layoutKey);
			_overlays.remove(layoutKey);
		}
	}

	/**
	 * Returns the component configuration of the looked up {@link TLLayout} for the given layout
	 * identifier. If the layout does not exist, try to create it.
	 * 
	 * @param layoutKey
	 *        Layout identifier.
	 * 
	 * @return null if the layout does not exist and could not be created.
	 * 
	 * @see #getOrCreateLayoutConfig(Theme, Person, String)
	 */
	public LayoutComponent.Config getOrCreateLayoutConfig(String layoutKey) throws ConfigurationException {
		return getOrCreateLayoutConfig(ThemeFactory.getTheme(), TLContext.getContext().getPerson(), layoutKey);
	}

	/**
	 * Returns the component configuration of the looked up {@link TLLayout} for the given layout
	 * identifier. If the layout does not exist, try to create it.
	 * 
	 * @param theme
	 *        {@link Theme} containing the layout.
	 * 
	 * @param person
	 *        Owner of the layouts. <code>null</code> is used for global layouts.
	 * 
	 * @param layoutKey
	 *        Layout identifier.
	 * 
	 * @return null if the layout does not exist and could not be created.
	 */
	public LayoutComponent.Config getOrCreateLayoutConfig(Theme theme, Person person, String layoutKey)
			throws ConfigurationException {
		TLLayout layout = getOrCreateLayout(theme, person, layoutKey);

		if (layout == null) {
			return null;
		}

		return layout.get();
	}

	/**
	 * Lookup the {@link TLLayout} for the given layout identifier. If it does not exist, try to
	 * create it.
	 * 
	 * @param layoutKey
	 *        Layout identifier.
	 * 
	 * @return null if the layout could not be created.
	 */
	public TLLayout getOrCreateLayout(String layoutKey) {
		return getOrCreateLayout(ThemeFactory.getTheme(), TLContext.getContext().getPerson(), layoutKey);
	}

	/**
	 * Lookup the {@link TLLayout} for the given layout identifier. If it does not exist, try to
	 * create it.
	 * 
	 * @param theme
	 *        {@link Theme} containing the layout.
	 * 
	 * @param person
	 *        Owner of the layouts. <code>null</code> is used for global layouts.
	 * 
	 * @param layoutKey
	 *        Layout identifier.
	 * 
	 * @return null if the layout could not be created.
	 */
	public TLLayout getOrCreateLayout(Theme theme, Person person, String layoutKey) {
		updateFilesystemCache();
		{
			TLLayout layout = getLayout(theme, person, layoutKey);

			if (layout != null) {
				return layout;
			}

			return createLayout(theme, layoutKey);
		}
	}

	/**
	 * Lookup the {@link TLLayout} for the given layout identifier.
	 * 
	 * @param layoutKey
	 *        Layout identifier.
	 * 
	 * @return null if the layout does not exist.
	 * 
	 * @see #getLayout(Theme, Person, String)
	 */
	public TLLayout getLayout(String layoutKey) {
		return getLayout(ThemeFactory.getTheme(), TLContext.getContext().getPerson(), layoutKey);
	}

	/**
	 * Lookup the {@link TLLayout} for the given layout identifier.
	 * 
	 * @param theme
	 *        {@link Theme} containing the layout.
	 * 
	 * @param person
	 *        Owner of the layouts. <code>null</code> is used for global layouts.
	 * 
	 * @param layoutKey
	 *        Layout identifier.
	 * 
	 * @return null if the layout does not exist.
	 */
	public TLLayout getLayout(Theme theme, Person person, String layoutKey) {
		TLLayout storageLayout = getLayoutFromDatabase(theme, person, layoutKey);
		if (storageLayout != null) {
			return storageLayout;
		}

		return getLayoutFromFilesystem(theme, layoutKey);
	}

	private void updateFilesystemCache() {
		FileSystemCache.getCache().fetchUpdates();
		while (_updates.hasNext()) {
			processFilesystemUpdate(_updates.next());
		}
	}

	private void processFilesystemUpdate(PathUpdate update) {
		LayoutUpdate layoutUpdate = new LayoutUpdate(update);

		if (layoutUpdate.hasChanges()) {
			if (layoutUpdate.canIncrementalUpdate()) {
				updateFilesystemCache(layoutUpdate);
			} else {
				initFilesystemCache();
			}
		}
	}

	private void updateFilesystemCache(LayoutUpdate update) {
		updateOverlays(update.getOverlaysToAdd(), update.getOverlaysToDelete());
		List<Theme> choosableThemes = getSortedChoosableThemes();
		loadLayoutsForAllThemes(update.getLayoutKeysToUpdate(), choosableThemes);

		for (String layoutKeyToDelete : update.getLayoutKeysToDelete()) {
			for (Theme theme : choosableThemes) {
				removeLayout(theme, layoutKeyToDelete);
			}
		}
	}

	private List<Theme> getSortedChoosableThemes() {
		Collection<Theme> choosableThemes = ThemeFactory.getInstance().getChoosableThemes();

		return CollectionUtil.topsort(theme -> theme.getParentThemes(), choosableThemes, false);
	}

	private void updateOverlays(Map<String, Set<BinaryData>> overlaysToAdd,
			Map<String, Set<BinaryData>> overlaysToDelete) {
		for (Entry<String, Set<BinaryData>> entry : overlaysToAdd.entrySet()) {
			_overlays.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
		}

		for (Entry<String, Set<BinaryData>> entry : overlaysToDelete.entrySet()) {
			_overlays.computeIfPresent(entry.getKey(), (k, overlays) -> {
				overlays.removeAll(entry.getValue());
				return overlays;
			});
		}
	}

	private TLLayout createLayout(Theme theme, String layoutKey) {
		InstantiationContext context = new DefaultInstantiationContext(LayoutStorage.class);
		TLLayout layout = createLayoutInternal(context, theme, layoutKey);

		if (!context.hasErrors()) {
			putToFilesystemCache(theme, layoutKey, layout);

			return layout;
		} else {
			return null;
		}
	}

	private TLLayout createLayoutInternal(InstantiationContext context, Theme theme, String layoutKey) {
		LayoutResolver resolver = LayoutResolver.newRuntimeResolver(new LogProtocol(LayoutStorage.class), theme);

		ComponentConfigurationBuilder builder = new ComponentConfigurationBuilder();

		builder.setContext(context);
		builder.setLayoutResolver(resolver);
		builder.setOverlays(_overlays.getOrDefault(layoutKey, Collections.emptyList()));
		builder.setLayoutName(layoutKey);
		builder.setLayoutScope(layoutKey);

		try {
			return builder.build();
		} catch (ConfigurationException exception) {
			context.error("Unable to create configuration definied in " + layoutKey, exception);
		} catch (IOException exception) {
			context.error("Problem reading " + layoutKey, exception);
		}

		return null;
	}

	/**
	 * Retrieves the customized component configuration from the database.
	 * 
	 * <p>
	 * The layout retrieved is either the version stored for the current user or the version
	 * published to all users, if no user-local version exists.
	 * </p>
	 * 
	 * @param person
	 *        Owner of the layouts. Use {@link #getGlobalLayoutFromDatabase(Theme, String)} to the
	 *        the version published to all users.
	 * @param layoutKey
	 *        Layout identifier.
	 * 
	 * @see #getLayout(Theme, Person, String)
	 */
	public TLLayout getLayoutFromDatabase(Theme theme, Person person, String layoutKey) {
		TLLayout databaseLayout = getUserLayoutFromDatabase(theme, person, layoutKey);

		if (databaseLayout != null) {
			return databaseLayout;
		}

		return getGlobalLayoutFromDatabase(theme, layoutKey);
	}

	/**
	 * Retrieves the customized component configuration that is published to all users from the
	 * database.
	 * 
	 * @see #getLayoutsFromDatabase(Theme, Person)
	 */
	public TLLayout getGlobalLayoutFromDatabase(Theme theme, String layoutKey) {
		return _dbCache.getLayout(theme, null, layoutKey);
	}

	/**
	 * Retrieves the customized component configuration stored locally for the current user.
	 * 
	 * @see #getLayoutsFromDatabase(Theme, Person)
	 */
	public TLLayout getUserLayoutFromDatabase(Theme theme, Person person, String layoutKey) {
		return _dbCache.getLayout(theme, person, layoutKey);
	}

	private Map<String, TLLayout> getGlobalLayoutsFromDatabase(Theme theme) {
		return CollectionUtil.nonNull(_dbCache.getLayouts(theme, null));
	}

	private Map<String, TLLayout> getUserLayoutsDatabase(Theme theme, Person person) {
		return CollectionUtil.nonNull(_dbCache.getLayouts(theme, person));
	}

	/**
	 * Returning all layouts personalized by the given user.
	 * 
	 * @param theme
	 *        {@link Theme} containing the layouts.
	 * 
	 * @param person
	 *        Owner of the layouts.
	 */
	public Map<String, TLLayout> getLayoutsFromDatabase(Theme theme, Person person) {
		Map<String, TLLayout> result = new HashMap<>();

		result.putAll(getGlobalLayoutsFromDatabase(theme));
		result.putAll(getUserLayoutsDatabase(theme, person));

		return result;
	}

	/**
	 * Retrieves the application base layout that is not in-app customized.
	 * 
	 * @see #getLayout(Theme, Person, String)
	 */
	public TLLayout getLayoutFromFilesystem(Theme theme, String layoutName) {
		updateFilesystemCache();
		return _filesystemCache.getLayout(theme, null, layoutName);
	}

	/**
	 * Returning all layouts for the given theme i.e. mapping of each {@link TLLayout} by his layout
	 * identifier.
	 * 
	 * @param theme
	 *        {@link Theme} containing the layouts.
	 */
	public Map<String, TLLayout> getLayouts(Theme theme) {
		Person person = TLContext.getContext().getPerson();

		HashMap<String, TLLayout> layoutsByKey = new HashMap<>();

		layoutsByKey.putAll(getLayoutsFromFilesystem(theme, person));
		layoutsByKey.putAll(getLayoutsFromDatabase(theme, person));

		return layoutsByKey;
	}

	private Map<String, TLLayout> getLayoutsFromFilesystem(Theme theme, Person person) {
		updateFilesystemCache();
		return _filesystemCache.getLayouts(theme, person);
	}

	/**
	 * Release the personal {@link TLLayout}s i.e. change the owning {@link Person} to null
	 * (global).
	 * 
	 * @param person
	 *        Owner of the layouts.
	 */
	public void releaseLayouts(Person person) {
		internalReleaseLayouts(person);
	}

	private void internalReleaseLayouts(Person person) {
		Collection<PersistentTemplateLayoutWrapper> existingPersonalLayouts = searchAllTemplateLayouts(person);
		if (existingPersonalLayouts.isEmpty()) {
			return;
		}

		for (PersistentTemplateLayoutWrapper personalLayout : existingPersonalLayouts) {
			PersistentTemplateLayoutWrapper globalLayout =
					searchTemplateLayout(null, personalLayout.getLayoutKey());
			if (globalLayout != null) {
				globalLayout.setArguments(personalLayout.getArguments());
				globalLayout.setTemplate(personalLayout.getTemplate());
				personalLayout.tDelete();
			} else {
				personalLayout.setPerson(null);
			}
		}
	}

	/**
	 * Search all layout templates for the given person with the given layout key.
	 * 
	 * @param person
	 *        Person owning the layout.
	 * @param layoutName
	 *        The name of the layout.
	 * 
	 * @return Wrapper for a template layout stored in the database.
	 */
	private PersistentTemplateLayoutWrapper searchTemplateLayout(Person person, String layoutName) {
		CompiledQuery<PersistentTemplateLayoutWrapper> query;
		/* This must currently done with two queries because "null" is not allowed as parameter
		 * argument. */
		RevisionQueryArguments queryArgs;
		if (person != null) {
			query = _personalTemplateLayouts;
			queryArgs = revisionArgs().setArguments(person.tHandle(), layoutName);
		} else {
			query = _globalLayouts;
			queryArgs = revisionArgs().setArguments(layoutName);
		}
		try (CloseableIterator<PersistentTemplateLayoutWrapper> searchResult = query.searchStream(queryArgs)) {
			if (searchResult.hasNext()) {
				return searchResult.next();
			} else {
				return null;
			}
		}
	}

	private Collection<PersistentTemplateLayoutWrapper> searchAllTemplateLayouts(Person person) {
		CompiledQuery<PersistentTemplateLayoutWrapper> query;
		/* This must currently done with two queries because "null" is not allowed as parameter
		 * argument. */
		RevisionQueryArguments queryArgs;
		if (person != null) {
			query = _allPersonalTemplateLayouts;
			queryArgs = revisionArgs().setArguments(person.tHandle());
		} else {
			throw new TopLogicException(ResKey.NONE);
		}
		try (CloseableIterator<PersistentTemplateLayoutWrapper> searchResult = query.searchStream(queryArgs)) {
			ArrayList<PersistentTemplateLayoutWrapper> result = new ArrayList<>();
			while (searchResult.hasNext()) {
				result.add(searchResult.next());
			}
			return result;
		}
	}

	/**
	 * Delete all layouts from the database for the given person.
	 * 
	 * @param person
	 *        Owner of the layouts. <code>null</code> is used for global layouts.
	 * 
	 * @return Number of reset layouts.
	 */
	public static int deleteLayoutsFromDatabase(Person person) {
		return deleteLayoutsFromDatabaseInternal(person, null);
	}

	/**
	 * Delete all layouts from the database for the given person.
	 * 
	 * @param person
	 *        Owner of the layouts. <code>null</code> is used for global (released) layouts.
	 * 
	 * @param layoutKey
	 *        Layout identifier.
	 * 
	 * @return Whether there was a personal layout which was reset.
	 */
	public static boolean deleteLayoutFromDatabase(Person person, String layoutKey) {
		Objects.requireNonNull(layoutKey, "layoutKey must not be null");
		int numberDeletedLayouts = deleteLayoutsFromDatabaseInternal(person, layoutKey);
		switch (numberDeletedLayouts) {
			case 0:
				return false;
			case 1:
				return true;
			default:
				Logger.error("Multiple layouts for person '" + person + "' and key '" + layoutKey + "'.",
					LayoutStorage.class);
				return true;
		}
	}

	private static int deleteLayoutsFromDatabaseInternal(Person person, String layoutKey) {
		try {
			return getInstance()._resetPersistentLayout(person, layoutKey);
		} catch (Throwable ex) {
			String msg;
			if (person == null) {
				if (layoutKey == null) {
					msg = "Unable to reset all global layouts.";
				} else {
					msg = "Unable to reset global layout with key '" + layoutKey + "'.";
				}
			} else {
				if (layoutKey == null) {
					msg = "Unable to reset all layouts for '" + person + "'.";
				} else {
					msg = "Unable to reset layoutwith key '" + layoutKey + "' for '" + person + "'.";
				}
			}
			Logger.error(msg, LayoutStorage.class);
			throw ex;
		}
	}

	private int _resetPersistentLayout(Person person, String layoutKey) {
		Expression reference =
			reference(PersistentTemplateLayoutWrapper.KO_NAME_TEMPLATE_LAYOUTS, PERSON_ATTR);
		Expression search;
		if (person == null) {
			search = isNull(reference);
		} else {
			search = eqBinaryLiteral(reference, person.tId());
		}
		if (layoutKey != null) {
			Expression layoutAttr =
				attribute(PersistentTemplateLayoutWrapper.KO_NAME_TEMPLATE_LAYOUTS, LAYOUT_KEY_ATTR);
			search = and(search, eqBinaryLiteral(layoutAttr, layoutKey));
		}
		List<KnowledgeObject> persistentLayouts =
			kb().compileSimpleQuery(SimpleQuery.queryUnresolved(templateLayoutType(), search)).search();
		int numberLayouts = persistentLayouts.size();
		kb().deleteAll(persistentLayouts);
		return numberLayouts;
	}

	/**
	 * Updates the database layout.
	 * 
	 * @param person
	 *        Owner of the layout. <code>null</code> is used for global layouts.
	 * 
	 * @param layoutKey
	 *        Layout identifier.
	 * 
	 * @param arguments
	 *        Values for the properties of the general template.
	 */
	public void updateLayout(Person person, String layoutKey, ConfigurationItem arguments) {
		PersistentTemplateLayoutWrapper layout = searchTemplateLayout(person, layoutKey);

		if (layout != null) {
			layout.setArguments(LayoutTemplateUtils.getSerializedTemplateArguments(arguments));
		} else {
			Logger.error("Database layout " + layoutKey + " for person " + person + " not found.", LayoutStorage.class);
		}
	}

	/**
	 * Stores the layout into the database.
	 * 
	 * @param person
	 *        Owner of the layout. <code>null</code> is used for global layouts.
	 * 
	 * @param layoutKey
	 *        Layout identifier.
	 * 
	 * @param template
	 *        Name of the instantiated template.
	 * 
	 * @param arguments
	 *        Values for the properties of the general template.
	 */
	public static void storeLayout(Person person, String layoutKey, String template, ConfigurationItem arguments) {
		try {
			getInstance().internalStorePersistentTemplateLayout(person, layoutKey, template, arguments);
		} catch (Throwable ex) {
			Logger.error("Unable to update dynamic layout " + layoutKey + " for person " + person, ex,
				LayoutStorage.class);
			throw ex;
		}
	}

	/**
	 * Stores the given layout template into the database for the given person with the given layout
	 * key.
	 * 
	 * @param person
	 *        Person owning the layout.
	 * @param layoutName
	 *        The name of the layout to store.
	 * @param template
	 *        Template name to instantiate.
	 * @param arguments
	 *        Arguments used for instantiation of the given template.
	 */
	protected void internalStorePersistentTemplateLayout(Person person, String layoutName,
			String template, ConfigurationItem arguments) {
		PersistentTemplateLayoutWrapper layout = searchTemplateLayout(person, layoutName);

		if (layout == null) {
			layout = createTemplateLayoutWrapper(person, layoutName);
		}

		layout.setTemplate(template);
		layout.setArguments(LayoutTemplateUtils.getSerializedTemplateArguments(arguments));
	}

	private PersistentTemplateLayoutWrapper createTemplateLayoutWrapper(Person person, String layoutName) {
		PersistentTemplateLayoutWrapper layout = createTemplateLayoutWrapper();

		layout.setLayoutKey(layoutName);
		layout.setPerson(person);

		return layout;
	}

	private PersistentTemplateLayoutWrapper createTemplateLayoutWrapper() {
		String typeName = PersistentTemplateLayoutWrapper.KO_NAME_TEMPLATE_LAYOUTS;

		return kb().createKnowledgeItem(typeName, KnowledgeItem.class).getWrapper();
	}

	/**
	 * Singleton instance of {@link LayoutStorage}.
	 */
	public static LayoutStorage getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference for the {@link LayoutStorage}.
	 */
	public static final class Module extends TypedRuntimeModule<LayoutStorage> {

		/**
		 * Singleton {@link LayoutStorage.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<LayoutStorage> getImplementation() {
			return LayoutStorage.class;
		}

	}

}

