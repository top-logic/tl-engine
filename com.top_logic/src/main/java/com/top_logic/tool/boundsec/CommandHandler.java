/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.ListConfigValueProvider;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.format.JavaIdentifier;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.IFunction0;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ComponentCommandModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.check.CheckScopeProvider;
import com.top_logic.layout.channel.ModelChannel;
import com.top_logic.layout.channel.linking.Channel;
import com.top_logic.layout.channel.linking.Null;
import com.top_logic.layout.channel.linking.ref.ComponentRef;
import com.top_logic.layout.channel.linking.ref.ComponentRelation;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.annotation.CollapseEntries;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.annotation.TitleProperty;
import com.top_logic.layout.form.values.edit.initializer.UUIDInitializer;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandlerFactory.Config.CliqueConfig;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRuleSPI;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.RuleReference;
import com.top_logic.util.Resources;

/**
 * Implementation of a command/button in the context of a {@link LayoutComponent}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public interface CommandHandler
		extends ConfiguredInstance<CommandHandler.ConfigBase<? extends CommandHandler>>, BoundCommand,
		ExecutabilityRuleSPI {

	/**
	 * Configuration of an executability definition.
	 */
	public interface ExecutabilityConfig extends ConfigurationItem {

		/**
		 * @see #getExecutability()
		 */
		String EXECUTABILITY_PROPERTY = "executability";

		/**
		 * The configuration of the {@link ExecutabilityRule} to use.
		 * 
		 * @see CommandHandler#isExecutable(LayoutComponent, Object, Map)
		 */
		@Name(EXECUTABILITY_PROPERTY)
		@EntryTag("rule")
		@Format(SimplifiedFormat.class)
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

		/**
		 * Parser for a simplified {@link ExecutabilityRule} configuration consisting of a
		 * comma-separated list of either rule IDs or {@link ExecutabilityRule} class names.
		 */
		class SimplifiedFormat extends ListConfigValueProvider<PolymorphicConfiguration<? extends ExecutabilityRule>> {

			/**
			 * Singleton {@link SimplifiedFormat} instance.
			 */
			public static final SimplifiedFormat INSTANCE = new SimplifiedFormat();

			private SimplifiedFormat() {
				super();
			}

			@Override
			protected List<PolymorphicConfiguration<? extends ExecutabilityRule>> getValueNonEmpty(String propertyName,
					CharSequence propertyValue) throws ConfigurationException {
				List<String> ruleSpecs = StringServices.toList(propertyValue, ',');
				List<PolymorphicConfiguration<? extends ExecutabilityRule>> ruleConfigs =
					new ArrayList<>(ruleSpecs.size());
				for (String ruleSpec : ruleSpecs) {
					PolymorphicConfiguration<? extends ExecutabilityRule> execRuleConfig;
					if (ruleSpec.indexOf('.') >= 0) {
						// implementation class is configured
						execRuleConfig =
							newRuleConfig(ConfigUtil.lookupClassForName(ExecutabilityRule.class, ruleSpec));
					} else {
						execRuleConfig = newRuleReference(ruleSpec);
					}
					ruleConfigs.add(execRuleConfig);
				}
				return ruleConfigs;
			}

			private static PolymorphicConfiguration<ExecutabilityRule> newRuleConfig(
					Class<? extends ExecutabilityRule> ruleClass) throws ConfigurationException {
				Factory factory = DefaultConfigConstructorScheme.getFactory(ruleClass);
				@SuppressWarnings("unchecked")
				PolymorphicConfiguration<ExecutabilityRule> ruleConfig =
					(PolymorphicConfiguration<ExecutabilityRule>) TypedConfiguration
						.newConfigItem((Class<? extends PolymorphicConfiguration<?>>) factory
						.getConfigurationInterface());
				ruleConfig.setImplementationClass(ruleClass);
				return ruleConfig;
			}

			private static RuleReference.Config newRuleReference(String ruleId) {
				RuleReference.Config ruleConfig = TypedConfiguration.newConfigItem(RuleReference.Config.class);
				ruleConfig.setRuleId(ruleId);
				return ruleConfig;
			}

			@Override
			protected String getSpecificationNonNull(
					List<PolymorphicConfiguration<? extends ExecutabilityRule>> configValue) {
				switch (configValue.size()) {
					case 0: {
						return StringServices.EMPTY_STRING;
					}
					case 1: {
						return ruleId(configValue.get(0));
					}
					default: {
						StringBuilder specification = new StringBuilder();
						for (PolymorphicConfiguration<? extends ExecutabilityRule> rule : configValue) {
							if (specification.length() > 0) {
								specification.append(',');
							}
							specification.append(ruleId(rule));
						}
						return specification.toString();
					}
				}
			}

			private static String ruleId(PolymorphicConfiguration<? extends ExecutabilityRule> config) {
				return asReference(config).getRuleId();
			}

			private static RuleReference.Config asReference(
					PolymorphicConfiguration<? extends ExecutabilityRule> config) {
				if (isNotReference(config)) {
					throw new IllegalArgumentException(
						"Serialization of illegal value: In general, an executability rule cannot be serialized into a plain string.");
				}
				return (RuleReference.Config) config;
			}

			@Override
			public boolean isLegalValue(Object value) {
				// In general, only references can be serialized
				return super.isLegalValue(value) && value instanceof List<?>
					&& !((List<?>) value).stream().filter(SimplifiedFormat::isNotReference).findAny().isPresent();
			}

			private static boolean isNotReference(Object config) {
				return !(config instanceof RuleReference.Config);
			}

		}

	}

	/**
	 * Configuration of a command target model.
	 */
	public interface TargetConfig extends ConfigurationItem {

		/**
		 * @see #getTarget()
		 */
		String TARGET = "target";

		/**
		 * The default {@link #getTarget()} specification: The model of the context component.
		 */
		String TARGET_MODEL_SELF = "model(self())";

		/**
		 * Option for {@link #getTarget()} using the selection of the context component as target
		 * object.
		 */
		String TARGET_SELECTION_SELF = "selection(self())";

		/**
		 * Option for {@link #getTarget()} using the model of the dialog parent of the context
		 * component as target object.
		 */
		String TARGET_MODEL_DIALOG_PARENT = "model(dialogParent())";

		/**
		 * Option for {@link #getTarget()} using no target object at all.
		 * 
		 * @see NullModelSpecConstant
		 */
		String TARGET_NULL = "null()";

		/**
		 * The specification of the target object to operate on.
		 * 
		 * <p>
		 * The evaluation of the configured {@link ModelSpec} is passed as context object to
		 * {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map) the
		 * command execution}.
		 * </p>
		 * 
		 * <p>
		 * If nothing is configured, the default is to use the model of the context component as
		 * target model for the executed command.
		 * </p>
		 * 
		 * @implNote If nothing is configured, the value of the property is <code>null</code>. This
		 *           is equivalent with the value {@link #TARGET_MODEL_SELF}. For in-app component
		 *           configuration, a <code>null</code> default is more appropriate, because it lets
		 *           look the initial configuration GUI less bloated.
		 * 
		 * @see #TARGET_MODEL_SELF
		 * @see #TARGET_SELECTION_SELF
		 * @see #TARGET_MODEL_DIALOG_PARENT
		 * @see #TARGET_NULL
		 */
		@Name(TARGET)
		@Label("model")
		ModelSpec getTarget();

		/**
		 * Constant function providing a {@link com.top_logic.layout.channel.linking.Null} item.
		 * 
		 * @see #TARGET_NULL
		 */
		class NullModelSpecConstant extends Function0<ModelSpec> {

			private static ModelSpec NULL = TypedConfiguration.newConfigItem(Null.class);

			@Override
			public ModelSpec apply() {
				return NULL;
			}
		}
	}

	/**
	 * Configuration options for the confirm message of a command.
	 */
	public interface ConfirmConfig extends TargetConfig {

		/**
		 * @see #getConfirm()
		 */
		String CONFIRM_PROPERTY = "confirm";

		/**
		 * @see #getConfirmMessage()
		 */
		String CONFIRM_MESSAGE = "confirmMessage";

		/**
		 * Whether the user is asked for confirmation before the command is actually executed.
		 * 
		 * @see CommandHandler#needsConfirm()
		 */
		@Name(CONFIRM_PROPERTY)
		boolean getConfirm();

		/**
		 * The message to display to the user requesting for confirmation that the command should
		 * really be executed.
		 * 
		 * <p>
		 * The message can refer to the {@link #getTarget() target model} of the command using the
		 * <code>{0}</code> placeholder.
		 * </p>
		 * 
		 * <p>
		 * The message is only displayed, if {@link #getConfirm()} is set. If no value is set, but
		 * {@link #getConfirm()} is checked, a generic confirmation message is displayed.
		 * </p>
		 */
		@Name(CONFIRM_MESSAGE)
		@DynamicMode(fun = VisibleIf.class, args = @Ref(CONFIRM_PROPERTY))
		ResKey getConfirmMessage();

		/**
		 * Dynamic field mode that displays a field only if a referenced checkbox is checked.
		 */
		class VisibleIf extends Function1<FieldMode, Boolean> {
			@Override
			public FieldMode apply(Boolean arg) {
				return arg != null && arg.booleanValue() ? FieldMode.ACTIVE : FieldMode.INVISIBLE;
			}
		}

	}

	/**
	 * Configuration options for the image aspect of a {@link CommandHandler}.
	 */
	public interface ImageConfig extends ConfigurationItem {

		/**
		 * Configuration option for {@link #getImage()}.
		 */
		String IMAGE_PROPERTY = "image";

		/**
		 * Configuration option for {@link #getDisabledImage()}.
		 */
		String DISABLED_IMAGE_PROPERTY = "disabledImage";

		/**
		 * The icon for this command, if displayed as image button.
		 * 
		 * <p>
		 * Note: If no icon is defined, the default icon from the {@link Config#getClique() command
		 * clique} is used.
		 * </p>
		 * 
		 * @see #getDisabledImage()
		 * @see CommandHandler#getImage(LayoutComponent)
		 */
		@Name(IMAGE_PROPERTY)
		@Nullable
		ThemeImage getImage();

		/**
		 * @see #getImage()
		 */
		void setImage(ThemeImage value);

		/**
		 * The icon to display for this command, if the command is disabled.
		 * 
		 * <p>
		 * Note: With icon font icons, there is no need to customize the disabled icon. By default
		 * the {@link #getImage() regular button icon} is also used for disabled buttons but
		 * rendered in another style.
		 * </p>
		 * 
		 * @see Config#getExecutability()
		 * @see CommandHandler#getNotExecutableImage(LayoutComponent)
		 */
		@Name(DISABLED_IMAGE_PROPERTY)
		@Nullable
		ThemeImage getDisabledImage();

		/**
		 * @see #getDisabledImage()
		 */
		void setDisabledImage(ThemeImage value);
	}

	/**
	 * {@link CommandHandler} properties that have defaults in the {@link CliqueConfig command
	 * clique}.
	 */
	@Abstract
	public interface CommandDefaults extends ImageConfig {
		/**
		 * @see #getCssClasses()
		 */
		String CSS_CLASSES_PROPERTY_NAME = "cssClasses";

		/**
		 * @see CommandHandler#getCssClasses(LayoutComponent)
		 */
		@Name(CSS_CLASSES_PROPERTY_NAME)
		@Nullable
		String getCssClasses();

		/**
		 * @see #getCssClasses()
		 */
		void setCssClasses(String value);
	}

	/**
	 * Base configuration for all {@link CommandHandler}'s including proxy ones.
	 */
	@Abstract
	@TitleProperty(name = ConfigBase.UI_TITLE)
	@CollapseEntries
	public interface ConfigBase<T extends CommandHandler> extends PolymorphicConfiguration<T> {

		/**
		 * @see #getCollapsedTitle()
		 */
		String UI_TITLE = "collapsedTitle";

		/**
		 * The title displayed for a collapsed {@link CommandHandler} configuration.
		 */
		@Name(UI_TITLE)
		@Hidden
		@Abstract
		String getCollapsedTitle();
	}

	/**
	 * Configuration of a {@link CommandHandler}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	@DisplayInherited(DisplayStrategy.IGNORE)
	@DisplayOrder({
		Config.RESOURCE_KEY_PROPERTY_NAME,
		Config.IMAGE_PROPERTY,
		Config.DISABLED_IMAGE_PROPERTY,
		Config.CLIQUE_PROPERTY,
		Config.GROUP_PROPERTY,
		Config.TARGET,
		Config.EXECUTABILITY_PROPERTY,
		Config.CONFIRM_PROPERTY,
		Config.CONFIRM_MESSAGE
	})
	public interface Config extends ConfigBase<CommandHandler>, ExecutabilityConfig, ConfirmConfig, CommandDefaults {

		/**
		 * @see #getId()
		 */
		String ID_PROPERTY = "id";
		
		/**
		 * @see #getGroup()
		 */
		String GROUP_PROPERTY = "group";
		
		/**
		 * @see #getClique()
		 */
		String CLIQUE_PROPERTY = "clique";

		/**
		 * @see #getResourceKey()
		 */
		String RESOURCE_KEY_PROPERTY_NAME = "resourceKey";

		/**
		 * @see #getCheckScopeProvider()
		 */
		String CHECK_SCOPE_PROVIDER = "checkScopeProvider";

		/**
		 * @see CommandHandler#getID()
		 */
		@Name(ID_PROPERTY)
		@Mandatory
		@Format(JavaIdentifier.class)
		@ValueInitializer(UUIDInitializer.class)
		String getId();
	
		/**
		 * {@link BoundCommandGroup} of this {@link CommandHandler}.
		 * 
		 * <p>
		 * If not set (the value is <code>null</code>), {@link SimpleBoundCommandGroup#READ_NAME} is
		 * used implicitly.
		 * </p>
		 * 
		 * @implNote No default must be annotated to this property.
		 *           <p>
		 *           Many {@link CommandHandler}s have their own default for this property, but do
		 *           not declare it in their specific configuration interface. Instead, the default
		 *           is set in the constructor, if the configuration provides no value. To be able
		 *           to detect the absence of a configuration in those constructors the default
		 *           value has to be <code>null</code>. Applying the defaults in the constructor and
		 *           not declaring them in the configuration interface is a bug, but fixing it would
		 *           require to change more than 200 {@link CommandHandler}s, which is not worth the
		 *           effort.
		 *           </p>
		 * 
		 * @implNote Technically, the type of the property is {@link CommandGroupReference} instead
		 *           of {@link BoundCommandGroup}.
		 *           <p>
		 *           Since both, {@link CommandHandler}s and {@link BoundCommandGroup}s are defined
		 *           in the {@link ApplicationConfig} in their respective services, the
		 *           {@link BoundCommandGroup} instances are not available while parsing the
		 *           configuration.
		 *           </p>
		 * 
		 * @see CommandHandler#getCommandGroup()
		 */
		@Name(GROUP_PROPERTY)
		CommandGroupReference getGroup();

		/**
		 * Option provider function returning all registered {@link BoundCommandGroup}s.
		 * 
		 * @see CommandGroupRegistry#getAllCommandGroups()
		 */
		class AllCommandGroups extends Function0<Collection<BoundCommandGroup>> {
			@Override
			public Collection<BoundCommandGroup> apply() {
				return CommandGroupRegistry.getInstance().getAllCommandGroups();
			}
		}

		/**
		 * {@link OptionMapping} that translates between {@link BoundCommandGroup}s and
		 * {@link CommandGroupReference}s.
		 */
		class ToCommandGroupReference implements OptionMapping {

			@Override
			public Object toSelection(Object option) {
				if (option == null) {
					return null;
				}
				return new CommandGroupReference(((BoundCommandGroup) option).getID());
			}

			@Override
			public Object asOption(Iterable<?> allOptions, Object selection) {
				return selection == null ? null : ((CommandGroupReference) selection).resolve();
			}

		}

		/**
		 * Name of the command {@link CommandHandlerFactory.Config.CliqueConfig clique}, this
		 * {@link CommandHandler} is part of.
		 * 
		 * <p>
		 * If no clique is assigned explicitly, the default in
		 * {@link CommandHandlerFactory.Config#getDefaultClique()} is used.
		 * </p>
		 */
		@Name(CLIQUE_PROPERTY)
		@Nullable
		@Options(fun = AllCliques.class)
		String getClique();

		/**
		 * {@link IFunction0} providing all clique names.
		 * 
		 * @see CommandHandlerFactory#getAllCliqueNames()
		 * @see Config#getClique()
		 */
		class AllCliques extends Function0<Collection<String>> {
			@Override
			public Collection<String> apply() {
				return CommandHandlerFactory.getInstance().getAllCliqueNames();
			}
		}

		/**
		 * @see #getClique()
		 */
		void setClique(String value);
	
		/**
		 * The internationalized command label.
		 */
		@Label("label")
		@Name(RESOURCE_KEY_PROPERTY_NAME)
		ResKey getResourceKey();

		@Override
		@Derived(fun = Internationalize.class, args = @Ref(RESOURCE_KEY_PROPERTY_NAME))
		String getCollapsedTitle();

		/**
		 * The configured {@link CheckScopeProvider}.
		 * 
		 * @see CommandHandler#checkScopeProvider()
		 */
		@Name(CHECK_SCOPE_PROVIDER)
		PolymorphicConfiguration<CheckScopeProvider> getCheckScopeProvider();

		/**
		 * Function computing the internationalization of a {@link ResKey} in the current user's
		 * language.
		 */
		class Internationalize extends Function1<String, ResKey> {
			@Override
			public String apply(ResKey arg) {
				if (arg == null) {
					return null;
				}
				if (!ResourcesModule.Module.INSTANCE.isActive()) {
					return arg.toString();
				}
				return Resources.getInstance().getString(arg);
			}
		}
	}

	/**
	 * Strategy to display a command.
	 * 
	 * @see CliqueConfig#getDisplay()
	 */
	public enum Display implements ExternallyNamed {

		/**
		 * Display the commands in the regular button bar.
		 */
		COMMANDS("commands"),

		/**
		 * Display the component's toolbar (icon-only).
		 */
		TOOLBAR("toolbar"),

		/**
		 * Display the commands in the component's burger menu.
		 */
		MENU("menu"),

		/**
		 * Display the commands only in the context menu of elements.
		 */
		CONTEXT_MENU("context-menu"),

		/**
		 * No not display the commands directly in the UI.
		 */
		HIDDEN("hidden"),

		;

		private final String _externalName;

		/**
		 * Creates a {@link Display}.
		 * 
		 * @param externalName
		 *        See {@link #getExternalName()}.
		 */
		private Display(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

	}

	/**
	 * can be used to call {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)} if no
	 * arguments are necessary.
	 */
	public static final Map<String, Object> NO_ARGS = Collections.emptyMap();

	/**
	 * Value to enable {@link Config#getConfirm()}
	 *
	 * @see #needsConfirm()
	 */
	public static final boolean NEEDS_CONFIRM = true;

   /**
    * Return the command name of this handler.
    * 
    * @return    The name of the command processed by this handler, must 
    *            not be <code>null</code>.
    */
   @Override
public String getID();
   
   /**
    * Get the command group responsible for this command. A
    * 
    * @return the command group, may be null if command
    *         does not belong to any group
    */
   @Override
public BoundCommandGroup getCommandGroup();
   
   	/**
	 * The name of the {@link CommandHandlerFactory.Config.CliqueConfig clique}, this
	 * {@link CommandHandler} is part of.
	 */
	String getClique();

   	/**
	 * Whether the user is asked for confirmation before the command is executed.
	 */
	@Override
	public boolean needsConfirm();

	/**
	 * The message displayed to the user requesting for confirmation that the command should really
	 * be executed.
	 * 
	 * <p>
	 * The confirmation happens only, if {@link #needsConfirm()} returns <code>true</code>.
	 * </p>
	 *
	 * @param component
	 *        The {@link LayoutComponent} the command is executed on.
	 * @param arguments
	 *        The command arguments, see
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @return The internationalized text to display in the confirmation dialog.
	 */
	public ResKey getConfirmKey(LayoutComponent component, Map<String, Object> arguments);

	/**
	 * True if the command is a concurrent command.
	 */
   @Override
public boolean isConcurrent();
   
   /**
	 * Return the command script writer that renders the JavaScriptFunction for this command
	 * 
	 * @param component
	 *            the component at which this {@link BoundCommand} is registered.
	 * @return a {@link CommandScriptWriter}
	 */
   @Override
public CommandScriptWriter getCommandScriptWriter(LayoutComponent component);

   /**
     * Return the names of the attributes needed to generate a link, which can
     * be processed by this handler.
     * 
     * @return    The array of attributes, may be <code>null</code>.
     */
    public String[] getAttributeNames();

    /**
	 * Checks whether this command is allowed to operate on the given model in the given component.
	 * 
	 * @param component
	 *        The component asking for the command, must not be <code>null</code>.
	 * @param model
	 *        See {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param someValues
	 *        The map of values defined by {@link #getAttributeNames()}, must not be
	 *        <code>null</code>.
	 * @return Whether this command is allowed to be executed.
	 */
	boolean checkSecurity(LayoutComponent component, Object model, Map<String, Object> someValues);

    /**
	 * Hook for the component to start the processing from this command.
	 * 
	 * All parameters must not be <code>null</code>.
	 * 
	 * Implementors shall not put logic into handleCommand, but only parse request or other params
	 * and pass to "inner" working method.
	 * 
	 * @param aContext
	 *        The DisplayContext holding request related data
	 * @param aComponent
	 *        The calling component.
	 * @param model
	 *        The model to operate on. The argument is not required to be the given component's
	 *        model, but is determined by {@link #getTargetModel(LayoutComponent, Map)}.
	 * @param someArguments
	 *        The map of values defined by {@link #getAttributeNames()}, must not be
	 *        <code>null</code>.
	 * @return The result of the processing, must not be <code>null</code>.
	 */
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments);
    
	/**
	 * The {@link CheckScopeProvider} that identifies components that are checked for unsaved
	 * changes before this command is executed.
	 */
	public CheckScopeProvider checkScopeProvider();

	/**
	 * Whether the execution of this command with the given arguments must not be recorded by the
	 * {@link ScriptingRecorder}.
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
	 * @return Whether the following command execution must no be recorded.
	 */
	boolean mustNotRecordCommand(DisplayContext context, LayoutComponent component,
			Map<String, Object> someArguments);

	/**
	 * The model object this {@link CommandHandler} will operate on.
	 * 
	 * <p>
	 * <b>Note:</b> Must be exclusively called through
	 * {@link CommandHandlerUtil#getTargetModel(CommandHandler, LayoutComponent, Map)}.
	 * </p>
	 * 
	 * <p>
	 * The target object is not required to be the given component's model. It can be its selection
	 * or otherwise derived from the component's model.
	 * </p>
	 * 
	 * <p>
	 * The target model to use can be configured for the concrete {@link CommandHandler} using
	 * {@link Config#getTarget()}.
	 * </p>
	 * 
	 * @param component
	 *        The context component.
	 * @param arguments
	 *        The arguments of the current command invokation, see
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @return The model that is target by this operation.
	 * 
	 * @see Config#getTarget()
	 * @see #handleCommand(DisplayContext, LayoutComponent, Object, Map)
	 * @see ExecutabilityRule#isExecutable(LayoutComponent, Object, Map)
	 */
	@FrameworkInternal
	public Object getTargetModel(LayoutComponent component, Map<String, Object> arguments);

	/**
	 * Determines whether this command is executable on the given component with the target model
	 * determined by {@link #getTargetModel(LayoutComponent, Map)}.
	 * 
	 * @see ExecutabilityRule#isExecutable(LayoutComponent, Object, Map)
	 */
	default ExecutableState isExecutable(LayoutComponent aComponent, Map<String, Object> arguments) {
		return isExecutable(aComponent, CommandHandlerUtil.getTargetModel(this, aComponent, arguments), arguments);
	}

	/**
	 * Whether {@link Config#getTarget()} references the component channel with the given name of
	 * the component this handler is registered on.
	 */
	default boolean operatesOn(String channelName) {
		PolymorphicConfiguration<? extends CommandHandler> config = getConfig();
		if (!(config instanceof AbstractCommandHandler.Config)) {
			return false;
		}

		AbstractCommandHandler.Config handlerConfig = (AbstractCommandHandler.Config) config;
		ModelSpec target = handlerConfig.getTarget();
		if (target == null) {
			return ModelChannel.NAME.equals(channelName);
		}
		if (!(target instanceof Channel)) {
			return false;
		}

		Channel channel = (Channel) target;
		if (!channel.getName().equals(channelName)) {
			return false;
		}

		ComponentRef componentRef = channel.getComponentRef();
		if (!(componentRef instanceof ComponentRelation)) {
			return false;
		}

		ComponentRelation relation = (ComponentRelation) componentRef;
		return relation.getKind() == ComponentRelation.Kind.self;
	}

	/**
	 * Creates a {@link CommandModel} for a button representing this {@link CommandHandler}.
	 * 
	 * @see ButtonControl#ButtonControl(CommandModel)
	 */
	default CommandModel createCommandModel(LayoutComponent component, Map<String, Object> arguments) {
		ResKey label = getResourceKey(component);
		return new ComponentCommandModel(this, component, arguments, label);
	}

}
