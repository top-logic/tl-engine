/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.misc.AbstractIndexer;
import com.top_logic.basic.func.misc.AbstractListIndexer;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.tool.boundsec.CommandHandler.CommandDefaults;
import com.top_logic.tool.boundsec.CommandHandler.Display;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.execution.ExecutabilityRuleManager;

/**
 * Registry of globally defined {@link CommandHandler}s.
 * 
 * @see CommandHandlerFactory.Config
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
@ServiceDependencies({
	ExecutabilityRuleManager.Module.class,
	CommandGroupRegistry.Module.class })
public final class CommandHandlerFactory extends ManagedClass {

	/**
	 * Configuration of {@link CommandHandlerFactory}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface Config extends ServiceConfiguration<CommandHandlerFactory> {

		/**
		 * @see #getDefaultClique()
		 */
		String DEFAULT_CLIQUE = "default-clique";

		/**
		 * @see #getCliques()
		 */
		String CLIQUES = "cliques";

		/**
		 * @see #getHandlers()
		 */
		String HANDLERS = "handlers";

		@ClassDefault(CommandHandlerFactory.class)
		@Override
		public Class<? extends CommandHandlerFactory> getImplementationClass();

		/**
		 * The command clique, if none is explicitly assined.
		 * 
		 * @see CommandHandler.Config#getClique()
		 */
		@Name(DEFAULT_CLIQUE)
		@Nullable
		String getDefaultClique();

		/**
		 * Definition of command cliques.
		 */
		@Name(CLIQUES)
		@Key(AbstractCliqueConfig.NAME_ATTRIBUTE)
		List<AbstractCliqueConfig> getCliques();
		
		/**
		 * Index of top-level {@link #getCliques() clique} name to its index in the
		 * {@link #getCliques()} list.
		 */
		@Derived(fun = CliqueIndexer.class, args = { @Ref(CLIQUES) })
		Mapping<String, Integer> getCliqueIndex();

		/**
		 * Index of a {@link #getCliques() clique} name to its index in the {@link #getCliques()}
		 * list.
		 */
		@Derived(fun = GroupDisplayIndexer.class, args = { @Ref(CLIQUES) })
		Mapping<String, CommandHandler.Display> getCliqueGroupDisplay();

		/**
		 * {@link Mapping} finding the clique group for a clique name.
		 */
		@Derived(fun = CliqueGroupIndexer.class, args = { @Ref(CLIQUES) })
		Map<String, String> getCliqueGroupMapping();

		/**
		 * {@link Mapping} finding default settings for all commands of a given clique.
		 * 
		 * @see CommandDefaults
		 */
		@Derived(fun = CliqueDefaultsIndexer.class, args = { @Ref(CLIQUES) })
		Mapping<String, CommandDefaults> getCliqueDefaults();

		/**
		 * Configurations for declared {@link CommandHandler}s.
		 */
		@Name(HANDLERS)
		@Key(CommandHandler.Config.ID_PROPERTY)
		List<CommandHandler.Config> getHandlers();

		/**
		 * Index of a {@link #getHandlers() handler} name to its index in the {@link #getHandlers()}
		 * list.
		 */
		@Derived(fun = HandlerIndexer.class, args = { @Ref(HANDLERS) })
		Mapping<String, Integer> getHandlerIndex();

		/**
		 * Base config for cliques and clique groups.
		 */
		@Abstract
		public interface AbstractCliqueConfig extends NamedConfigMandatory {

			/**
			 * {@link CommandHandler.Display Strategy} how to display the commands in this group.
			 */
			CommandHandler.Display getDisplay();

		}

		/**
		 * Definition of a command clique.
		 * 
		 * <p>
		 * A command clique is a group of commands displayed together.
		 * </p>
		 */
		@TagName("clique")
		public interface CliqueConfig extends AbstractCliqueConfig, CommandDefaults {
			// Pure sum interface.
		}

		/**
		 * Definition of a clique within a clique group.
		 * 
		 * <p>
		 * A grouped clique may define {@link CommandDefaults} but inherits its display properties
		 * form its group.
		 * </p>
		 */
		public interface GroupedCliqueConfig extends NamedConfigMandatory, CommandDefaults {
			// Pure sum interface.
		}

		/**
		 * Definition of a clique group.
		 * 
		 * <p>
		 * A clique group consists of a list of cliques with a common display setting.
		 * </p>
		 */
		@TagName("group")
		public interface CliqueGroupConfig extends AbstractCliqueConfig {
			/**
			 * Cliques in this group.
			 */
			@Key(GroupedCliqueConfig.NAME_ATTRIBUTE)
			List<GroupedCliqueConfig> getCliques();
		}

		/**
		 * {@link Function1} computing an index of a {@link String} list.
		 * 
		 * <p>
		 * The computed index maps the name of each {@link String} entry to its list index.
		 * </p>
		 */
		class CliqueIndexer extends Function1<Mapping<String, Integer>, Iterable<AbstractCliqueConfig>> {
			/**
			 * Singleton {@link CliqueIndexer} instance.
			 */
			public static final CliqueIndexer INSTANCE = new CliqueIndexer();

			private CliqueIndexer() {
				// Singleton constructor.
			}

			@Override
			public Mapping<String, Integer> apply(Iterable<AbstractCliqueConfig> arg) {
				Map<String, Integer> map = new HashMap<>();
				if (arg != null) {
					int index = 0;
					for (AbstractCliqueConfig config : arg) {
						String name = config.getName();
						map.put(name, Integer.valueOf(index++));
						if (config instanceof CliqueGroupConfig) {
							for (GroupedCliqueConfig clique : ((CliqueGroupConfig) config).getCliques()) {
								map.put(clique.getName(), Integer.valueOf(index++));
							}
						}
					}
				}
				return Mappings.createMapBasedMapping(map, (Integer) null);
			}
		}

		/**
		 * {@link AbstractIndexer} creating an index of {@link AbstractCliqueConfig#getName()} to
		 * {@link AbstractCliqueConfig#getDisplay()} for top-level cliques.
		 */
		class GroupDisplayIndexer extends AbstractIndexer<AbstractCliqueConfig, String, CommandHandler.Display> {
			/**
			 * Singleton {@link CliqueIndexer} instance.
			 */
			public static final GroupDisplayIndexer INSTANCE = new GroupDisplayIndexer();

			private GroupDisplayIndexer() {
				// Singleton constructor.
			}

			@Override
			protected String key(AbstractCliqueConfig entry) {
				return entry.getName();
			}

			@Override
			protected CommandHandler.Display value(AbstractCliqueConfig entry) {
				return entry.getDisplay();
			}
		}

		/**
		 * {@link Function1} creating a {@link Mapping} of a clique name to its group name.
		 * 
		 * <p>
		 * If the clique is a top-level clique, its name is mapped to itself.
		 * </p>
		 */
		class CliqueGroupIndexer extends Function1<Map<String, String>, Iterable<AbstractCliqueConfig>> {

			/**
			 * Singleton {@link CommandHandlerFactory.Config.CliqueGroupIndexer} instance.
			 */
			public static final CliqueGroupIndexer INSTANCE = new CliqueGroupIndexer();

			private CliqueGroupIndexer() {
				// Singleton constructor.
			}

			@Override
			public Map<String, String> apply(Iterable<AbstractCliqueConfig> arg) {
				Map<String, String> map = new HashMap<>();
				if (arg != null) {
					for (AbstractCliqueConfig config : arg) {
						String name = config.getName();
						if (config instanceof CliqueGroupConfig) {
							for (GroupedCliqueConfig clique : ((CliqueGroupConfig) config).getCliques()) {
								String clash = map.put(clique.getName(), name);
								checkClash(config, name, clash);
							}
						} else {
							String clash = map.put(name, name);
							checkClash(config, name, clash);
						}
					}
				}
				return Collections.unmodifiableMap(map);
			}

			private void checkClash(AbstractCliqueConfig config, String name, String clash) {
				if (clash != null) {
					throw new ConfigurationError("Ambiguous clique name '" + name
						+ "', there is another clique with the same name in group '" + clash + "': "
						+ config.location());
				}
			}

		}

		/**
		 * {@link Function1} mapping clique names to the {@link CommandDefaults} settings of the
		 * clique.
		 */
		class CliqueDefaultsIndexer extends Function1<Mapping<String, CommandDefaults>, Iterable<AbstractCliqueConfig>> {

			/**
			 * Singleton {@link CommandHandlerFactory.Config.CliqueGroupIndexer} instance.
			 */
			public static final CliqueDefaultsIndexer INSTANCE = new CliqueDefaultsIndexer();

			private CliqueDefaultsIndexer() {
				// Singleton constructor.
			}

			@Override
			public Mapping<String, CommandDefaults> apply(Iterable<AbstractCliqueConfig> arg) {
				Map<String, CommandDefaults> map = new HashMap<>();
				if (arg != null) {
					for (AbstractCliqueConfig config : arg) {
						if (config instanceof CliqueGroupConfig) {
							for (GroupedCliqueConfig clique : ((CliqueGroupConfig) config).getCliques()) {
								map.put(clique.getName(), clique);
							}
						} else {
							map.put(config.getName(), (CliqueConfig) config);
						}
					}
				}
				return Mappings.createMapBasedMapping(map, (CommandDefaults) null);
			}

		}

		/**
		 * {@link Function1} computing an index of a {@link CommandHandler.Config} list.
		 * 
		 * <p>
		 * The computed index maps the ID of each {@link CommandHandler.Config} entry to its list
		 * index.
		 * </p>
		 */
		class HandlerIndexer extends AbstractListIndexer<String, CommandHandler.Config> {
			/**
			 * Singleton {@link HandlerIndexer} instance.
			 */
			public static final HandlerIndexer INSTANCE = new HandlerIndexer();

			private HandlerIndexer() {
				// Singleton constructor.
			}

			@Override
			protected String key(CommandHandler.Config entry) {
				return entry.getId();
			}
		}
	}

	private static final Class<?>[] CONSTRUCTOR_ID_SIGNATURE = new Class[] { String.class };

    /** The map of known handlers. */
	private transient Map<String, CommandHandler> handlerById;

	private final String _defaultClique;

	private final Map<String, String> _cliqueGroupMapping;

	private final Mapping<String, CommandHandler.Display> _cliqueGroupDisplay;

	private final Mapping<String, Integer> _cliqueIndex;

	private final Comparator<CommandHandler> _commandOrder;

	private final Comparator<Entry<String, ?>> _cliqueToolBarOrder;

	private final Comparator<Entry<String, ?>> _cliqueButtonBarOrder;

	private final Mapping<String, CommandDefaults> _cliqueDefaults;

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for creation
	 * commands.
	 */
	public static final String CREATE_CLIQUE = "create";

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for deletion
	 * commands.
	 */
	public static final String DELETE_CLIQUE = "delete";

	/**
	 * Well-known {@link Config#getCliques() clique} for cancel commands.
	 */
	public static final String CANCEL_CLIQUE = "cancel";

	/**
	 * Well-known {@link Config#getCliques() clique} for apply-like commands.
	 */
	public static final String APPLY_CLIQUE = "apply";

	/**
	 * Well-known {@link Config#getCliques() clique} for save-like commands.
	 */
	public static final String SAVE_CLIQUE = "save";

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for configuring the
	 * table filter sidebar.
	 */
	public static final String FILTER_SELECT_GROUP = "filterSelect";

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for expand commands.
	 */
	public static final String EXPAND_CLIQUE = "expand";

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for collapse
	 * commands.
	 */
	public static final String COLLAPSE_CLIQUE = "collapse";

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for start/stop
	 * commands.
	 */
	public static final String STATE_GROUP = "state-handling";

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for refresh/reload
	 * commands.
	 */
	public static final String REFRESH_GROUP = "refresh";

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for expand/collapse
	 * commands.
	 */
	public static final String EXPAND_COLLAPSE_GROUP = "expand-collapse";

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for configuring a
	 * displayed chart.
	 */
	public static final String CHART_TYPES_GROUP = "chartTypes";

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for component export
	 * commands.
	 */
	public static final String EXPORT_BUTTONS_GROUP = "exportButtons";

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for component import
	 * commands.
	 */
	public static final String IMPORT_BUTTONS_GROUP = "importButtons";

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for default table
	 * buttons.
	 */
	public static final String TABLE_BUTTONS_GROUP = "tableButtons";

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for any other less
	 * frequently used additional commands.
	 */
	public static final String ADDITIONAL_GROUP = "additional";

	/**
	 * Well-known {@link Config#getCliques() clique} for additional apply commands.
	 */
	public static final String ADDITIONAL_APPLY_CLIQUE = "additional-apply";

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for help commands.
	 */
	public static final String HELP_GROUP = "help";

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for table
	 * configuration buttons.
	 */
	public static final String TABLE_CONFIG_GROUP = "tableConfig";

	/**
	 * Well-known toolbar group (top-level {@link Config#getCliques() clique}) for table selection
	 * commands.
	 */
	public static final String SELECT_GROUP = "tableSelection";

	/**
	 * Well-known {@link Config#getCliques() clique} for internal commands that should not be
	 * displayed.
	 */
	public static final String INTERNAL_GROUP = "internal";

    /**
	 * Creates a {@link CommandHandlerFactory} from configuration.
	 */
	@CalledByReflection
	public CommandHandlerFactory(InstantiationContext context, Config config) {
		super(context, config);

		_defaultClique = config.getDefaultClique();
		_cliqueGroupDisplay = config.getCliqueGroupDisplay();
		_cliqueGroupMapping = config.getCliqueGroupMapping();
		_cliqueDefaults = config.getCliqueDefaults();

		final Mapping<String, Integer> handlerIndex = config.getHandlerIndex();

		_commandOrder = new IndexOrder<>() {
			@Override
			protected Integer index(CommandHandler o1) {
				return handlerIndex.map(o1.getID());
			}
		};

		_cliqueIndex = config.getCliqueIndex();
		_cliqueToolBarOrder = createToolbarOrder();
		_cliqueButtonBarOrder = createButtonBarOrder();

		handlerById = MapUtil.newMap(config.getHandlers().size());
		for (CommandHandler.Config handlerConfig : config.getHandlers()) {
			String commandId = handlerConfig.getId();
			try {
				CommandHandler handler = createHandler(context, handlerConfig);

				handlerById.put(commandId, handler);
			} catch (InstantiationException ex) {
				error(commandId, ex);
			} catch (IllegalAccessException ex) {
				error(commandId, ex);
			} catch (InvocationTargetException ex) {
				error(commandId, ex);
			} catch (ConfigurationException ex) {
				error(commandId, ex);
			}
		}
		Logger.info("Registered " + handlerById.size() + " command handlers.", CommandHandlerFactory.class);
    }

	/**
	 * Factory method for creating the {@link #getCliqueToolBarOrder()} comparator.
	 * 
	 * <p>
	 * Note: This method is called from within the constructor and must not access any state.
	 * </p>
	 */
	protected Comparator<Entry<String, ?>> createToolbarOrder() {
		return new IndexOrder<>() {
			@Override
			protected Integer index(Entry<String, ?> o1) {
				return getCliqueIndex(o1.getKey());
			}
		};
	}

	/**
	 * Factory method for creating the {@link #getCliqueButtonBarOrder()} comparator.
	 * 
	 * <p>
	 * Note: This method is called from within the constructor and must not access any state.
	 * </p>
	 */
	protected Comparator<Entry<String, ?>> createButtonBarOrder() {
		return new Comparator<>() {
			@Override
			public int compare(Entry<String, ?> o1, Entry<String, ?> o2) {
				String clique1 = o1.getKey();
				String clique2 = o2.getKey();

				String group1 = getCliqueGroup(clique1);
				String group2 = getCliqueGroup(clique2);

				int groupIndex1 = getCliqueIndex(group1);
				int groupIndex2 = getCliqueIndex(group2);

				int groupCompare = Integer.compare(groupIndex1, groupIndex2);
				if (groupCompare == 0) {
					// Same group.
					int index1 = getCliqueIndex(clique1);
					int index2 = getCliqueIndex(clique2);
					return Integer.compare(index1, index2);
				} else {
					// Important groups last.
					return -groupCompare;
				}
			}
		};
	}

    /**
     * Return the handler for the given key.
     * 
     * If there is no matching handler for that key, this method will return
     * <code>null</code>.
     * 
     * @param    aKey    The key, the requested handler is registered to.
     * @return   The requested hander or <code>null</code>, if there is
     *           no such handler.
     */
    public CommandHandler getHandler(String aKey) {
		return handlerById.get(aKey);
    }

	/**
	 * Access to all defined {@link CommandHandler}s.
	 */
	public Collection<CommandHandler> getAllHandlers() {
		return Collections.unmodifiableCollection(handlerById.values());
	}

	private CommandHandler createHandler(InstantiationContext context, CommandHandler.Config handlerConfig)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, ConfigurationException {
		boolean copy = false;
		if (handlerConfig.getClique() == null) {
			handlerConfig = TypedConfiguration.copy(handlerConfig);
			copy = true;
			handlerConfig.setClique(_defaultClique);
		} else {
			checkClique(context, handlerConfig);
		}
		CommandDefaults defaults = _cliqueDefaults.map(handlerConfig.getClique());
		if (defaults != null) {
			if (handlerConfig.getImage() == null) {
				ThemeImage cliqueImage = defaults.getImage();
				if (cliqueImage != null) {
					if (!copy) {
						handlerConfig = TypedConfiguration.copy(handlerConfig);
						copy = true;
					}
					handlerConfig.setImage(cliqueImage);
					handlerConfig.setDisabledImage(defaults.getDisabledImage());
				}
			}
			if (handlerConfig.getCssClasses() == null) {
				String cssClasses = defaults.getCssClasses();
				if (cssClasses != null) {
					if (!copy) {
						handlerConfig = TypedConfiguration.copy(handlerConfig);
						copy = true;
					}
					handlerConfig.setCssClasses(cssClasses);
				}
			}
		}

		String commandId = handlerConfig.getId();
		Class<?> handlerClass = handlerConfig.getImplementationClass();

		Factory factory;
		try {
			factory = DefaultConfigConstructorScheme.getFactory(handlerClass);
		} catch (ConfigurationException ex1) {
			// Fall-back to legacy instantiation.
			try {
				return createHandlerWithIdConstructor(handlerClass, commandId);
			} catch (NoSuchMethodException ex2) {
				try {
					return createhandlerWithDefaultConstuctor(handlerClass);
				} catch (Exception ex) {
					throw ex1;
				}
			}
		}

		return (CommandHandler) factory.createInstance(context, handlerConfig);
    }

	private void checkClique(InstantiationContext context, CommandHandler.Config handlerConfig) {
		String cliqueGroup = _cliqueGroupMapping.get(handlerConfig.getClique());
		if (cliqueGroup == null) {
			context.error("CommandHandler '" + handlerConfig.getId() + "' with undefined clique '"
					+ handlerConfig.getClique() + "', see " + handlerConfig.location());
		}
	}

	private CommandHandler createhandlerWithDefaultConstuctor(Class<?> handlerClass) throws InstantiationException,
			IllegalAccessException {
		return (CommandHandler) handlerClass.newInstance();
	}

	private CommandHandler createHandlerWithIdConstructor(Class<?> handlerClass, String commandId)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Constructor<?> idConstructor = handlerClass.getConstructor(CONSTRUCTOR_ID_SIGNATURE);
		CommandHandler result = (CommandHandler) idConstructor.newInstance(new Object[] { commandId });
		return result;
	}

	private void error(String commandId, Throwable ex) {
		Logger.error("Unable to create CommandHandler for '" + commandId + "'.", ex, CommandHandlerFactory.class);
	}

    /**
     * Return the only instance of this class.
     * 
     * @return    The only instance (singleton).
     */
    public static CommandHandlerFactory getInstance() {
    	return Module.INSTANCE.getImplementationInstance();
    }
    
    /**
	 * The {@link Display} setting of top-level clique or clique group with the given name.
	 */
	public CommandHandler.Display getDisplay(String cliqueGroup) {
		CommandHandler.Display result = _cliqueGroupDisplay.map(cliqueGroup);
		if (result == null) {
			throw new IllegalArgumentException("Undefined command clique group: " + cliqueGroup);
		}
		return result;
	}

	/**
	 * {@link Comparator} of {@link CommandHandler} that sorts handlers in their definition order.
	 */
	public Comparator<CommandHandler> getCommandOrder() {
		return _commandOrder;
	}

	/**
	 * Resolves the {@link CommandHandler} for the given configuration.
	 * 
	 * <p>
	 * This method cares for referenced {@link CommandHandler}s and returns the referenced handler
	 * instead of the reference handler.
	 * </p>
	 */
	public CommandHandler getCommand(InstantiationContext context,
			PolymorphicConfiguration<? extends CommandHandler> config) {
		CommandHandler commandHandler;
		if (config instanceof CommandHandlerReference.Config) {
			String handlerId = ((CommandHandlerReference.Config) config).getCommandId();
			commandHandler = getHandler(handlerId);
		} else {
			commandHandler = createCommand(context, config);
		}
		return commandHandler;
	}

	/**
	 * Creates a {@link CommandHandler} from its configuration.
	 * 
	 * <p>
	 * Before actually instantiating the configuration, dynamic defaults are applied to the
	 * configuration.
	 * </p>
	 * 
	 * @see Config#getDefaultClique()
	 * @see Config.CliqueConfig#getImage()
	 * @see Config.CliqueConfig#getDisabledImage()
	 * 
	 * @return The instantiated {@link CommandHandler}.
	 */
	private CommandHandler createCommand(InstantiationContext context,
			PolymorphicConfiguration<? extends CommandHandler> handlerConfig) {
		if (handlerConfig instanceof CommandHandler.Config) {
			CommandHandler.Config commandConfig = (CommandHandler.Config) handlerConfig;
			try {
				return createHandler(context, commandConfig);
			} catch (ConfigurationException | InstantiationException | IllegalAccessException
					| InvocationTargetException ex) {
				throw new ConfigurationError("Command handler '" + commandConfig.getId() + "' instantiation failed.",
					ex);
			}
		}
		return context.getInstance(handlerConfig);
	}

	/**
	 * The clique to assign to a {@link CommandHandler}, if none is defined.
	 * 
	 * @see CommandHandler#getClique()
	 */
	public String getDefaultClique() {
		return _defaultClique;
	}

	/**
	 * All configured clique names.
	 * 
	 * @see #getCliqueGroup(String)
	 */
	public Set<String> getAllCliqueNames() {
		return _cliqueGroupMapping.keySet();
	}

	/**
	 * The name of the clique group for the given clique.
	 * 
	 * <p>
	 * For a top-level clique, the clique name is returned.
	 * </p>
	 */
	public String getCliqueGroup(String clique) {
		String group = _cliqueGroupMapping.get(clique);
		if (group == null) {
			throw new IllegalArgumentException("No such command clique: " + clique);
		}
		return group;
	}

	/**
	 * The sort index for the given clique or clique group name.
	 */
	public int getCliqueIndex(String cliqueOrGroup) {
		Integer index = _cliqueIndex.map(cliqueOrGroup);
		if (index == null) {
			return Integer.MAX_VALUE;
		}
		return index;
	}

	/**
	 * Order of cliques displayed in the toolbar.
	 * 
	 * <p>
	 * {@link Comparator} for {@link java.util.Map.Entry} objects having clique names as keys.
	 * </p>
	 * 
	 * <p>
	 * Comparison happens according to the {@link #getCliqueIndex(String)}.
	 * </p>
	 */
	public Comparator<Entry<String, ?>> getCliqueToolBarOrder() {
		return _cliqueToolBarOrder;
	}

	/**
	 * Order of cliques displayed in the button bar.
	 * 
	 * <p>
	 * {@link Comparator} for {@link java.util.Map.Entry} objects having clique names as keys.
	 * </p>
	 * 
	 * <p>
	 * High-priority cliques are ordered last, since the button bar is aligned to the right.
	 * </p>
	 */
	public Comparator<Entry<String, ?>> getCliqueButtonBarOrder() {
		return _cliqueButtonBarOrder;
	}

	@Override
    protected void shutDown() {
		this.handlerById = null;
		super.shutDown();
    }

	/**
	 * Singleton reference to the {@link CommandHandlerFactory}.
	 */
	public static final class Module extends TypedRuntimeModule<CommandHandlerFactory> {

		/**
		 * Singleton {@link CommandHandlerFactory.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<CommandHandlerFactory> getImplementation() {
			return CommandHandlerFactory.class;
		}

	}

}
