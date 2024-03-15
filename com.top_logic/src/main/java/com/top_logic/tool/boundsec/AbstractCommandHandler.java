/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import static com.top_logic.tool.execution.CombinedExecutabilityRule.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandScriptWriter;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ComponentCommand;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.check.CheckScopeProvider;
import com.top_logic.layout.basic.check.NoCheckScopeProvider;
import com.top_logic.layout.channel.ModelChannel;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.AlwaysExecutable;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRuleManager;
import com.top_logic.tool.execution.ExecutableState;


/**
 * Base for all component command handlers.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class AbstractCommandHandler implements CommandHandler {

	/**
	 * Configuration of an {@link AbstractCommandHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends CommandHandler.Config, SecurityObjectProviderConfig {

		@Override
		@Options(fun = AllInAppImplementations.class)
		@DisplayMinimized
		PolymorphicConfiguration<? extends SecurityObjectProvider> getSecurityObject();

	}

	/** Internal key in the arguments to annotate execution of the command as "do not record". */
	public static final String DO_NOT_RECORD = "___doNotRecord";

	private static final ResPrefix COMMAND_PREFIXES = I18NConstants.COMMAND;

    /** The parameter for the object ID. */
    public static final String OBJECT_ID = "id";

    /** The parameter for the object type. */
    public static final String TYPE = "type";
    
    /** The parameter for the object branch. */
    public static final String BRANCH = "branch";

    /** The parameter for the object revision. */
    public static final String REVISION = "revision";

    /** The group this command belongs to. */
    protected BoundCommandGroup commandGroup;

	private String _clique;

    /** Flag, if this command needs special confirmation by user. */
    private boolean confirm;

    private String commandID;

	private ThemeImage _image;

	private ThemeImage _disabledImage;

	private ResKey _resourceKey;

	private String _cssClasses;

	private ExecutabilityRule _rule;

	private final CheckScopeProvider _checkScopeProvider;

	/**
	 * Configuration option to specify the id of the command. must not be <code>null</code>.
	 */
	public static final String ATTRIBUTE_NAME_COMMAND_ID = "id";

	/**
	 * Configuration option to specify command group of the command
	 */
	public static final String ATTRIBUTE_NAME_COMMAND_GROUP = "cmdGroup";

	/**
	 * Configuration option to specify whether the command needs confirm before execution.
	 */
	public static final String ATTRIBUTE_NAME_WITH_CONFIRM = "withConfirm";

	private final Config _config;

	private final SecurityObjectProvider _securityObjectProvider;

	private final ChannelLinking _target;
    
	/**
	 * Creates a {@link AbstractCommandHandler} from typed configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} for instantiating sub-configurations.
	 * @param config
	 *        The handler configuration.
	 */
	@CalledByReflection
	public AbstractCommandHandler(InstantiationContext context, Config config) {
		_config = config;
		this.commandID = id(config);
		this.commandGroup = group(context, config);
		_clique = config.getClique();
		this.confirm = confirm(config);
		_image = config.getImage();
		_disabledImage = getImage(config.getDisabledImage(), _image);
		_resourceKey = resourceKey(config);
		_cssClasses = config.getCssClasses();
		_rule = rule(context, config);
		_checkScopeProvider = checkScopeProvider(context, config);
		_securityObjectProvider = context.getInstance(config.getSecurityObject());
		_target = context.getInstance(config.getTarget());

		assert _rule != null : "No executablity rule in handler '" + getID() + "'.";
		assert _checkScopeProvider != null : "No check scope provider in handler '" + getID() + "'.";
	}

	@Override
	public final AbstractCommandHandler.Config getConfig() {
		return _config;
	}

	private CheckScopeProvider checkScopeProvider(InstantiationContext context, CommandHandler.Config config) {
		PolymorphicConfiguration<CheckScopeProvider> providerConfig = config.getCheckScopeProvider();
		if (providerConfig == null) {
			return getCheckScopeProvider();
		} else {
			return context.getInstance(providerConfig);
		}
	}

	private ExecutabilityRule rule(InstantiationContext context, CommandHandler.Config config) {
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> ruleConfigs = config.getExecutability();
		int ruleCnt = ruleConfigs.size();
		if (ruleCnt == 0) {
			return combine(intrinsicExecutability(), createExecutabilityRule());
		}
		return combine(intrinsicExecutability(), ExecutabilityRuleManager.resolveRules(context, config));
	}

	private ResKey resourceKey(CommandHandler.Config config) {
		ResKey resourceKey = config.getResourceKey();
		if (resourceKey != null) {
			return resourceKey;
		}
		return ResKey.fallback(idBasedI18NKey(), getDefaultI18NKey());
	}

	private static ThemeImage getImage(ThemeImage image, ThemeImage defaultImage) {
		if (image == null) {
			return defaultImage;
		}
		return image;
	}

	/**
	 * @deprecated Use configuration:
	 *             {@link com.top_logic.tool.boundsec.CommandHandler.Config#getExecutability()}.
	 */
	@Deprecated
	protected final void setRule(ExecutabilityRule rule) {
		_rule = rule;
	}

	/**
	 * @deprecated Use configuration:
	 *             {@link com.top_logic.tool.boundsec.CommandHandler.Config#getResourceKey()}
	 */
	@Deprecated
	protected final void setResourceKey(ResKey resourceKey) {
		_resourceKey = resourceKey;
	}

    @Override
	public final String getID() {
    	return this.commandID;
    }
    
    @Override
	public final BoundCommandGroup getCommandGroup() {
        return (this.commandGroup);
    }

	@Override
	public String getClique() {
		return _clique;
	}

	/**
	 * Whether the configured or default confirmation message is shown.
	 */
	protected boolean needsConfirm() {
        return this.confirm;
    }
    
    @Override
    public ResKey getConfirmKey(LayoutComponent component, Map<String, Object> arguments) {
		if (!needsConfirm()) {
			return null;
		}
    	Object targetModel = arguments == null ? null : CommandHandlerUtil.getTargetModel(this, component, arguments);
    	
    	ResKey customKey = getConfig().getConfirmMessage();
    	if (customKey != null) {
			return ResKey.message(customKey, targetModel);
    	}
    	
		return getDefaultConfirmKey(component, arguments, targetModel);
	}

	/**
	 * Determines the {@link #getConfirmKey(LayoutComponent, Map) confirmation resource key} if no
	 * special {@link Config#getConfirmMessage() confirm message} is set.
	 * 
	 * @param component
	 *        The {@link LayoutComponent} the command is executed on.
	 * @param arguments
	 *        The command arguments, see
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param targetModel
	 *        The model on which this handler operates.
	 * 
	 * @return The internationalized text to display in the confirmation dialog.
	 */
	protected ResKey getDefaultConfirmKey(LayoutComponent component, Map<String, Object> arguments,
			Object targetModel) {
		ResKey commandKey = getResourceKey(component);

		ResKey componentKey;
		if (commandKey != null) {
			componentKey = ResKey.message(commandKey.suffix(".confirm"), targetModel);
		} else {
			componentKey = null;
		}
	
		ResKey genericKey = targetModel == null ? 
			I18NConstants.DEFAULT_CONFIRM_MESSAGE__COMMAND.fill(commandKey) : 
				I18NConstants.DEFAULT_CONFIRM_MESSAGE__COMMAND_MODEL.fill(commandKey, targetModel);
    	
		return componentKey == null ? genericKey : componentKey.fallback(genericKey);
	}

    @Override
	public String[] getAttributeNames() {
        return null;
    }

	@Override
	public final ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		return _rule.isExecutable(aComponent, model, someValues);
    }

    /**
     * Return the I18N key used for translation of buttons (when available)
     * 
     * @return    The default I18N key
     */
    @Override
	public ResKey getResourceKey(LayoutComponent component) {
		// Note: Dialog openers have already an explicit opener-local key.
		if (component == null || this instanceof OpenModalDialogCommandHandler) {
			return _resourceKey;
		} else {
			return ResKey.fallback(component.getResPrefix().key(getID()), _resourceKey);
		}
	}

	/**
	 * Hook to statically define {@link #getResourceKey(LayoutComponent)}.
	 * 
	 * @return A {@link ResKey} which is default for this command. {@link ResKey#NONE} when this
	 *         command has no default I18N.
	 */
	protected ResKey getDefaultI18NKey() {
		return ResKey.forClass(this.getClass());
	}

	/**
	 * Returns a generic {@link ResKey} containing the {@link #getID() id} of this command.
	 */
	protected final ResKey idBasedI18NKey() {
		return COMMAND_PREFIXES.key(commandID);
	}

	@Override
	public ThemeImage getImage(LayoutComponent component) {
		return _image;
	}

	@Override
	public ThemeImage getNotExecutableImage(LayoutComponent component) {
		return _disabledImage;
	}

	@Override
	public String getCssClasses(LayoutComponent component) {
		return _cssClasses;
	}

	/**
	 * @deprecated This method should no longer be overridden by application sub-classes. Instead,
	 *             the configuration option
	 *             {@link com.top_logic.tool.boundsec.CommandHandler.Config#getTarget()} should be
	 *             used. Callers must use
	 *             {@link CommandHandlerUtil#getTargetModel(CommandHandler, LayoutComponent, Map)}.
	 */
	@Override
	public Object getTargetModel(LayoutComponent component, Map<String, Object> arguments) {
		return baseModel(component);
	}

	private Object baseModel(LayoutComponent component) {
		if (component == null) {
			return null;
		}
		return ChannelLinking.eval(component, _target);
	}

    /**
     * Return the string representation of this instance.
     * 
     * @return    The string representation needed for debugging.
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " [" +
                "command: '" + this.getID() +
                "', confirm: " + this.confirm +
                ", group: " + this.commandGroup +
                ']');
    }

    /** 
     * Given object is equal, if it is a {@link CommandHandler command handler} and its ID is the same than mine.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object anObject) {
		if (anObject == this) {
			return true;
		}
        return (anObject instanceof CommandHandler) && this.getID().equals(((CommandHandler) anObject).getID());
    }

    /** 
     * Hash code is the same, than the hash code of the ID.
     * 
     * @return    The hash code for this instance.
     * @see       java.lang.Object#hashCode()
     */
    @Override
	public int hashCode() {
        return (this.getID().hashCode());
    }

    @Override
	public boolean isConcurrent() {
    	return false;
    }
    
	/**
	 * The default {@link ExecutabilityRule}, if none is configured.
	 * 
	 * @see #intrinsicExecutability()
	 * @see CommandHandler.Config#getExecutability()
	 * 
	 * @deprecated Use either configured rules, or implement {@link #intrinsicExecutability()}, or
	 *             even better use {@link PreconditionCommandHandler} as base class.
	 */
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
		return AlwaysExecutable.INSTANCE;
    }
    
	/**
	 * An {@link ExecutabilityRule} that is intrinsic to the command implementation (checking a
	 * technical precondition for the command).
	 * 
	 * <p>
	 * Instead of implementing this hook, consider using {@link PreconditionCommandHandler} as base
	 * class.
	 * </p>
	 */
	protected ExecutabilityRule intrinsicExecutability() {
		return AlwaysExecutable.INSTANCE;
	}

	@Override
	public CommandScriptWriter getCommandScriptWriter(LayoutComponent component) {
    	return AJAXCommandScriptWriter.INSTANCE;
    }
    
    // TODO KBU SEC CHECK intermediate code to make DisplayRenderer work with new security
    public static final String BOUND_OBJECT = "_bound_object_";
    
	/**
	 * Get the {@link BoundObject} this command operates on. In general this will be be the
	 * model/bound object of the layout.
	 * 
	 * @param aComponent
	 *        The component asking for the command, must not be <code>null</code>.
	 * @param model
	 *        See {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param arguments
	 *        The map of values defined by {@link #getAttributeNames()}, must not be
	 *        <code>null</code>.
	 * @return The {@link BoundObject}, may be <code>null</code>.
	 */
	protected BoundObject getBoundObject(LayoutComponent aComponent, Object model, Map<String, Object> arguments) {
		Object theBO = (arguments != null) ? arguments.get(BOUND_OBJECT) : null;
    	if (theBO instanceof BoundObject) {
    		return (BoundObject) theBO;
    	}
    	
		Object theObj = this.getObject(arguments);
    	if (theObj instanceof BoundObject) {
    		return (BoundObject) theObj;
    	}
    	
    	if (aComponent instanceof BoundChecker) {
			BoundChecker boundChecker = (BoundChecker) aComponent;
			if (_securityObjectProvider != null) {
				return _securityObjectProvider.getSecurityObject(boundChecker, model, getCommandGroup());
			} else {
				Object securityBaseModel = operatesOn(ModelChannel.NAME) ? model : aComponent.getModel();
				return boundChecker.getCurrentObject(this.getCommandGroup(), securityBaseModel);
			}
    	}
    	
		if (model instanceof BoundObject) {
			return (BoundObject) model;
		}

    	return null;
    }
    
	@Override
	public boolean checkSecurity(LayoutComponent component, Object model, Map<String, Object> someValues) {
		return ((BoundChecker) component).allow(getCommandGroup(), getBoundObject(component, model, someValues));
	}

	/**
	 * The default {@link CheckScopeProvider}, if not configured.
	 * 
	 * @deprecated Use configuration:
	 *             {@link com.top_logic.tool.boundsec.CommandHandler.Config#getCheckScopeProvider()}.
	 */
	@Deprecated
	protected CheckScopeProvider getCheckScopeProvider() {
    	return NoCheckScopeProvider.INSTANCE;
    }

    @Override
	public final CheckScopeProvider checkScopeProvider() {
		return _checkScopeProvider;
	}

	/**
	 * Searches for the object specified by the given arguments.
	 * 
	 * @param arguments
	 *        the arguments which are used to determine the object. The used keys are
	 *        {@link AbstractCommandHandler#OBJECT_ID}, {@link AbstractCommandHandler#TYPE},
	 *        {@link AbstractCommandHandler#BRANCH}, and {@link AbstractCommandHandler#REVISION}.
	 * 
	 * @return the desired object or <code>null</code> if not both
	 *         {@link AbstractCommandHandler#OBJECT_ID} and {@link AbstractCommandHandler#TYPE} are
	 *         given.
	 * 
	 * @throws ObjectNotFound
	 *         iff {@link AbstractCommandHandler#OBJECT_ID} and {@link AbstractCommandHandler#TYPE}
	 *         are given but no object could be found.
	 */
	protected Object getObject(Map<String, Object> arguments) throws ObjectNotFound {
		TLID theId =
			IdentifierUtil.fromExternalForm(LayoutComponent.getParameter(arguments, AbstractCommandHandler.OBJECT_ID));
		String   theType        = LayoutComponent.getParameter(arguments, AbstractCommandHandler.TYPE);
		String   theBranchStr   = LayoutComponent.getParameter(arguments, AbstractCommandHandler.BRANCH);
        String   theRevisionStr = LayoutComponent.getParameter(arguments, AbstractCommandHandler.REVISION);
        
		Branch theBranch;
		if (StringServices.isEmpty(theBranchStr)) {
			theBranch = null;
		} else {
			theBranch = HistoryUtils.getBranch(Long.parseLong(theBranchStr));
		}
		Revision theRevision =
			StringServices.isEmpty(theRevisionStr) ? null : HistoryUtils.getRevision(Long.parseLong(theRevisionStr));

        Wrapper  theWrapper     = null;
        
        if (theId != null && theType != null) {
			theWrapper = WrapperFactory.getWrapper(theBranch, theRevision, theId, theType);
			if (theWrapper == null) {
				throw new ObjectNotFound(I18NConstants.OBJECT_NOT_FOUND);
			}
        }

        return theWrapper;
	}

	private static String id(CommandHandler.Config config) {
		return config.getId();
	}

	private static BoundCommandGroup group(Log log, Config config) {
		/* Many CommandHandler have their own defaults for this property, which are not declared in
		 * their config interfaces, but set in the constructor. But to be able to detect in those
		 * constructors whether those defaults have to be applied or an explicit value has been set,
		 * the default value has to be null. Applying the defaults in the constructor and not
		 * declaring them is a bug, of course. But fixing it would require to change more than 200
		 * CommandHandlers, which is not worth the effort. */
		if (config.getGroup() == null) {
			// Use default
			return SimpleBoundCommandGroup.READ;
		} else {
			BoundCommandGroup group = config.getGroup().resolve();
			if (group == null) {
				log.error("Unknown command group reference '" + config.getGroup().id() + "' in handler '"
					+ config.getId() + "'.");
			}
			return group;
		}
	}

	private static boolean confirm(CommandHandler.Config config) {
		return config.getConfirm();
	}

	public static CheckScopeProvider getCheckScopeProvider(BoundCommand command) {
		if (command instanceof CommandHandler) {
			return ((CommandHandler) command).checkScopeProvider();
		}
		return NoCheckScopeProvider.INSTANCE;
	}

	public static <T extends CommandHandler> T newInstance(Class<T> handlerClass, String commandId) {
		return newInstance(createConfig(handlerClass, commandId));
	}

	public static <T extends CommandHandler> T newInstance(CommandHandler.Config config) {
		return (T) getInstance(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}

	public static <C extends CommandHandler.Config> C createConfig(Class<? extends CommandHandler> handlerClass,
			String commandId) {
		try {
			Factory factory = DefaultConfigConstructorScheme.getFactory(handlerClass);
			C config = (C) TypedConfiguration.newConfigItem((Class) factory.getConfigurationInterface());
			config.setImplementationClass(handlerClass);
			config.update(config.descriptor().getProperty(Config.ID_PROPERTY), commandId);
			return config;
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Wrong handler configuration.", ex);
		}
	}

	/**
	 * Sets {@link CommandHandler.Config#getConfirm()} to the given value in the given config
	 * interface.
	 * 
	 * @param config
	 *        Is not allowed to be <code>null</code>.
	 * @return The given configuration, returned for convenience.
	 */
	public static <C extends CommandHandler.Config> C updateConfirm(C config, boolean value) {
		return update(config, Config.CONFIRM_PROPERTY, value);
	}

	/**
	 * Sets {@link CommandHandler.Config#getGroup()} to the given value in the given config
	 * interface.
	 * 
	 * @param config
	 *        Is not allowed to be <code>null</code>.
	 * @param value
	 *        Is allowed to be <code>null</code>.
	 * @return The given configuration, returned for convenience.
	 */
	public static <C extends CommandHandler.Config> C updateGroup(C config, BoundCommandGroup value) {
		CommandGroupReference reference;
		if (value == null) {
			reference = null;
		} else {
			reference = new CommandGroupReference(value.getID());
		}
		return update(config, Config.GROUP_PROPERTY, reference);
	}

	public static <C extends CommandHandler.Config> C updateImage(C config, ThemeImage value) {
		return update(config, Config.IMAGE_PROPERTY, value);
	}

	public static <C extends CommandHandler.Config> C updateDisabledImage(C config, ThemeImage value) {
		return update(config, Config.DISABLED_IMAGE_PROPERTY, value);
	}

	/**
	 * Sets {@link CommandHandler.Config#getResourceKey()} to the given value in the given config
	 * interface.
	 * 
	 * @param config
	 *        Is not allowed to be <code>null</code>.
	 * @param value
	 *        Is allowed to be <code>null</code>.
	 * @return The given configuration, returned for convenience.
	 */
	public static <C extends CommandHandler.Config> C updateResourceKey(C config, ResKey value) {
		return update(config, Config.RESOURCE_KEY_PROPERTY_NAME, value);
	}

	/**
	 * Sets {@link CommandHandler.Config#getExecutability()} in the given config interface to a
	 * singleton list containing only the given value.
	 * 
	 * @param config
	 *        Is not allowed to be <code>null</code>.
	 * @param ruleConfig
	 *        List of rule configs, see
	 *        {@link com.top_logic.tool.boundsec.CommandHandler.ExecutabilityConfig#getExecutability()}
	 *        .
	 * @return The given configuration, returned for convenience.
	 */
	public static <C extends CommandHandler.Config> C updateExecutability(C config,
			List<PolymorphicConfiguration<? extends ExecutabilityRule>> ruleConfig) {
		return update(config, Config.EXECUTABILITY_PROPERTY, ruleConfig);
	}

	/**
	 * Sets the property with the given name to the given value in the given config interface.
	 * 
	 * @param config
	 *        Is not allowed to be <code>null</code>.
	 * @param propertyName
	 *        Is not allowed to be <code>null</code>.
	 * @param value
	 *        The value has to be compatible to the specified property.
	 * @return The given configuration, returned for convenience.
	 */
	protected static <C extends CommandHandler.Config> C update(C config, String propertyName, Object value) {
		PropertyDescriptor property = config.descriptor().getProperty(propertyName);
		config.update(property, value);
		return config;
	}

	/**
	 * Resolves a list of {@link CommandHandler} from the given list of configuration.
	 * 
	 * <p>
	 * This method cares for referenced {@link CommandHandler}s and returns the referenced handler
	 * instead of the reference handler.
	 * </p>
	 */
	public static List<CommandHandler> getInstanceList(InstantiationContext context,
			List<? extends PolymorphicConfiguration<? extends CommandHandler>> configs) {
		if (configs == null) {
			return new ArrayList<>();
		}
		List<CommandHandler> handlers = new ArrayList<>(configs.size());
		for (PolymorphicConfiguration<? extends CommandHandler> config : configs) {
			handlers.add(getInstance(context, config));
		}
		return handlers;
	}

	/**
	 * Resolves the {@link CommandHandler} for the given configuration.
	 * 
	 * <p>
	 * This method cares for referenced {@link CommandHandler}s and returns the referenced handler
	 * instead of the reference handler.
	 * </p>
	 * 
	 * @see CommandHandlerFactory#getCommand(InstantiationContext, PolymorphicConfiguration)
	 */
	public static CommandHandler getInstance(InstantiationContext context,
			PolymorphicConfiguration<? extends CommandHandler> config) {
		return CommandHandlerFactory.getInstance().getCommand(context, config);
	}

	/**
	 * Returns the {@link CommandModel} executing this command.
	 * 
	 * <p>
	 * This is a service method to get command model executing this command. It must only be called
	 * in {@link #handleCommand(com.top_logic.layout.DisplayContext, LayoutComponent, Object, Map)}.
	 * </p>
	 * 
	 * @param someArguments
	 *        Arguments given in
	 *        {@link #handleCommand(com.top_logic.layout.DisplayContext, LayoutComponent, Object, Map)}
	 * 
	 * @return The {@link CommandModel} executing this command.
	 */
	protected final CommandModel getCommandModel(Map<String, Object> someArguments) {
		return ComponentCommand.getCommandModel(someArguments);
	}

	@Override
	public final boolean mustNotRecordCommand(DisplayContext context, LayoutComponent component,
			Map<String, Object> someArguments) {
		return mustNotRecord(context, component, someArguments);
	}

	/**
	 * Actual implementation of {@link #mustNotRecordCommand(DisplayContext, LayoutComponent, Map)}.
	 * 
	 * @param context
	 *        {@link DisplayContext} which will be given in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param component
	 *        {@link LayoutComponent} which will be given in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param someArguments
	 *        Arguments which will be given in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * 
	 * @return Whether this command must no be recorded.
	 */
	protected boolean mustNotRecord(DisplayContext context, LayoutComponent component,
			Map<String, Object> someArguments) {
		if (ScriptingRecorder.mustNotRecord(component)) {
			return true;
		}

		Object doNotRecord = someArguments.get(DO_NOT_RECORD);
		if (doNotRecord == null) {
			return false;
		}
		if (doNotRecord instanceof String) {
			// This happens when the argument is given within a AbstractCommandModelConfiguration.
			// It allows only Strings as value.
			return Boolean.parseBoolean(((String) doNotRecord));
		} else {
			return ((Boolean) doNotRecord).booleanValue();
		}

	}

}
