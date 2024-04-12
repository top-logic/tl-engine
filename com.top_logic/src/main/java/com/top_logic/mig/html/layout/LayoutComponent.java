/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import static java.util.Collections.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.InlineMap;
import com.top_logic.basic.col.LazyTypedAnnotatableMixin;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Id;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.func.Function2;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.listener.PropertyListeners;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.basic.shared.collection.map.MapUtilShared;
import com.top_logic.basic.thread.StackTrace;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.gui.JSFileCompiler;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.gui.layout.ButtonComponent;
import com.top_logic.knowledge.gui.layout.LayoutConfig;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.InvalidationListener;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResPrefixNoneDefault;
import com.top_logic.layout.URLBuilder;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.View;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.ModelChannel;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.component.IComponent;
import com.top_logic.layout.component.SaveScrollPosition;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.component.configuration.ViewConfiguration;
import com.top_logic.layout.component.dnd.ComponentDropTarget;
import com.top_logic.layout.component.dnd.NoComponentDrop;
import com.top_logic.layout.form.control.AbstractButtonControl;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.model.VisibilityModel;
import com.top_logic.layout.form.tag.FormTag;
import com.top_logic.layout.form.tag.I18NConstants;
import com.top_logic.layout.scripting.recorder.DynamicRecordable;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.layout.structure.ContentLayouting;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.Expandable;
import com.top_logic.layout.structure.ExpandableConfig;
import com.top_logic.layout.structure.InlineLayoutControlProvider;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.structure.LayoutControlProvider.Layouting;
import com.top_logic.layout.structure.PersonalizingExpandable;
import com.top_logic.layout.structure.ToolbarOptions;
import com.top_logic.layout.tabbar.TabInfo.TabConfig;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolBarChangeListener;
import com.top_logic.layout.toolbar.ToolBarGroup;
import com.top_logic.layout.toolbar.ToolBarGroupConfig;
import com.top_logic.layout.toolbar.ToolBarGroupConfig.ButtonConfig;
import com.top_logic.layout.toolbar.ToolBarOwner;
import com.top_logic.layout.window.OpenWindowCommand;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.layout.window.WindowTemplate;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.UserAgent;
import com.top_logic.mig.html.layout.tiles.GroupTileComponent;
import com.top_logic.mig.html.layout.tiles.TileInfo;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CloseModalDialogCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.CommandHandlerUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.boundsec.assistent.AssistentStepInfo;
import com.top_logic.tool.boundsec.assistent.CommandChain;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.ReferenceManager;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;
import com.top_logic.util.error.ErrorHandlingHelper;

/**
 * Basic Building block of a (HTML) Layout.
 *
 * @author  <a href="mailto:skr@top-logic.com">Silvester Kras</a>
 * @author  <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
@Label("Component")
public abstract class LayoutComponent extends ModelEventAdapter
		implements IComponent, NamedModel, FrameScope,
		LayoutConstants, ToolBarOwner, Expandable, LazyTypedAnnotatableMixin, Cloneable, DynamicRecordable,
		VisibilityModel {

	/**
	 * {@link ConfigurationItem} containing some of the methods in {@link LayoutComponent.Config}
	 * which may be be configured globally, but also be overridden in some subclasses.
	 * 
	 * <p>
	 * This {@link ConfigurationItem} is for compatibility with 5.7.6 in which
	 * {@link LayoutComponent} may also be instantiated using {@link org.xml.sax.Attributes}. For
	 * these components it not easily possible to configure defaults for {@link LayoutComponent} but
	 * override it programatically.
	 * </p>
	 * 
	 * @since 5.7.6
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface GlobalConfig extends ConfigurationItem {
		// No options.
	}

	/**
	 * Resource key suffix for building the default component title.
	 */
	private static final String TITLE_SUFFIX = "title";

	/**
	 * Configuration options for {@link LayoutComponent}.
	 */
	public interface Config
			extends PolymorphicConfiguration<LayoutComponent>, LayoutComponentUIOptions, IComponent.ComponentConfig, ToolbarOptions,
			ExpandableConfig, WithGotoConfiguration, WithDefaultFor {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/** @see #getName() */
		String NAME = "name";

		/**
		 * @see #getComponentResolvers()
		 */
		String COMPONENT_RESOLVER_NAME = "component-resolvers";

		/**
		 * @see #getLayoutInfo()
		 */
		String LAYOUT_INFO = "layoutInfo";
		String DIALOG_INFO_NAME = "dialogInfo";

		/** @see #getTabInfo() */
		String TAB_INFO_NAME = "tabInfo";

		/** @see #getAssistantInfo() */
		String ASSISTANT_INFO_NAME = "assistantInfo";

		/** Property name of {@link #getDefaultAction()}. */
		String DEFAULT_ACTION = "defaultAction";

		/** Property name of {@link #getCancelAction()}. */
		String CANCEL_ACTION = "cancelAction";

		String COMMANDS_NAME = "commands";

		String BUTTONS_NAME = "buttons";

		String COMPONENT = "component";

		/** @see #getButtonComponent() */
		String BUTTON_COMPONENT_NAME = "buttonComponent";

		/**
		 * @see #hasToolbar()
		 */
		String TOOLBAR = "toolbar";

		/**
		 * @see #getMaximizeRoot()
		 */
		String MAXIMIZE_ROOT = "maximizeRoot";

		/** @see #getHelpId() */
		String HELP_ID = "helpID";

		/** @see #getResetInvisible() */
		String RESET_INVISIBLE = "resetInvisible";

		/** @see #getDropTarget() */
		String DROP_TARGET = "dropTarget";

		/** @see #getDisplayWithoutModel() */
		String DISPLAY_WITHOUT_MODEL = "displayWithoutModel";

		/** @see #getNoModelKey() */
		String NO_MODEL_KEY = "noModelKey";

		/** @see #getCommandGroups() */
		String COMMAND_GROUPS_NAME = "commandGroups";

		/** @see #isFinal() */
		String FINAL = "final";

		/**
		 * Additional {@link BoundCommandGroup}s that needed by the configured component.
		 */
		@Format(SimpleBoundCommandGroup.SetValueProvider.class)
		@Name(COMMAND_GROUPS_NAME)
		Set<BoundCommandGroup> getCommandGroups();

		/**
		 * Returns the name of the configured component.
		 *
		 * If nothing is configured a synthetic name is returned.
		 * 
		 * @see LayoutConstants#isSyntheticName(ComponentName)
		 */
		@Name(NAME)
		@Id
		@NonNullable
		@ComplexDefault(SyntheticNameDefault.class)
		ComponentName getName();

		/** @see #getName() */
		void setName(ComponentName value);

		/**
		 * If <code>true</code>, this no overlay for this configuration in sub projects are applied.
		 */
		@Name(FINAL)
		@FrameworkInternal
		@Hidden
		boolean isFinal();

		/**
		 * Setter for {@link #isFinal()}.
		 */
		void setFinal(boolean isFinal);

		/**
		 * CSS class to add to the top-level component view.
		 */
		@Name(ATT_BODY_CLASS)
		@Nullable
		String getBodyClass();

		@Name(ATT_SAVE_SCROLL_POSITION)
		@BooleanDefault(true)
		boolean getSaveScrollPosition();

		@Name(ATT_RENDER_INLINE)
		@BooleanDefault(false)
		boolean getRenderInline();

		/**
		 * The {@link ResPrefix} for the configured component.
		 */
		@Name(ATT_RES_PREFIX)
		@InstanceFormat
		@ComplexDefault(ResPrefixNoneDefault.class)
		@NonNullable
		ResPrefix getResPrefix();

		/** @see #getResPrefix() */
		void setResPrefix(ResPrefix value);

		/**
		 * Name of the {@link ButtonComponent} to place command in.
		 */
		@Name(BUTTON_COMPONENT_NAME)
		ComponentName getButtonComponent();

		/**
		 * @see #getButtonComponent()
		 */
		void setButtonComponent(ComponentName value);

		@Name(ATT_USE_CHANGE_HANDLING)
		Boolean getUseChangeHandling();

		@Name(ATT_CLOSE_HANDLER_NAME)
		String getCloseHandlerName();

		@Name(ATT_DONT_RECORD)
		@BooleanDefault(DEFAULT_FOR_DONT_RECORD)
		boolean getDontRecord();

		@Name(XML_TAG_COMPONENT_CONTROLPROVIDER_NAME)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		@Derived(fun = ComponentControlProviderSelection.class, args = {
			@Ref(XML_TAG_COMPONENT_CONTROLPROVIDER_NAME), @Ref(ATT_RENDER_INLINE) })
		LayoutControlProvider getActiveComponentControlProvider();

		/**
		 * Implementation of {@link Config#getActiveComponentControlProvider()}.
		 * 
		 * <p>
		 * Selects either {@link Config#getComponentControlProvider()}, if set, or used
		 * {@link InlineLayoutControlProvider}, otherwise.
		 * </p>
		 */
		class ComponentControlProviderSelection
				extends Function2<LayoutControlProvider, PolymorphicConfiguration<LayoutControlProvider>, Boolean> {
			@Override
			public LayoutControlProvider apply(PolymorphicConfiguration<LayoutControlProvider> customProvider,
					Boolean renderInline) {
				if (customProvider == null && Utils.isTrue(renderInline)) {
					return InlineLayoutControlProvider.INSTANCE;
				}
				return TypedConfigUtil.createInstance(customProvider);
			}
		}

		/**
		 * Information for the layouting algorithm of the parent layout.
		 */
		@Name(LAYOUT_INFO)
		@ItemDefault
		@NonNullable
		LayoutInfo getLayoutInfo();

		/**
		 * Setter for {@link #getLayoutInfo()}.
		 */
		void setLayoutInfo(LayoutInfo layoutInfo);

		/**
		 * When the configured component has to be opened as the toplayout of a dialog, here are all
		 * the settings for it.
		 */
		@Name(DIALOG_INFO_NAME)
		DialogInfo getDialogInfo();

		/**
		 * Setter for {@link #getDialogInfo()}.
		 */
		void setDialogInfo(DialogInfo dialogInfo);

		/**
		 * When the configured component is part of a {@link TabComponent}, here are all the
		 * settings for it.
		 *
		 * @return May be <code>null</code>, when this component is not the top level component in a
		 *         {@link TabComponent}.
		 */
		@Name(TAB_INFO_NAME)
		TabConfig getTabInfo();

		/**
		 * Setter for {@link #getTabInfo()}.
		 */
		void setTabInfo(TabConfig tabInfo);

		/**
		 * When the configured component is part of an {@link AssistentComponent}, here are all the
		 * settings for it.
		 *
		 * @return May be <code>null</code>, when this component is not the top level component in
		 *         an {@link AssistentComponent}.
		 */
		@Name(ASSISTANT_INFO_NAME)
		AssistentStepInfo getAssistantInfo();

		/**
		 * Setter for {@link #getAssistantInfo()}.
		 */
		void setAssistantInfo(AssistentStepInfo tabInfo);

		/**
		 * When the configured component is part of an {@link GroupTileComponent}, here are all the
		 * settings for it.
		 *
		 * @return May be <code>null</code>, when this component is not the top level component in
		 *         an {@link GroupTileComponent}.
		 */
		TileInfo getTileInfo();

		/**
		 * Setter for {@link #getTileInfo()}.
		 */
		void setTileInfo(TileInfo tileInfo);

		/**
		 * Dialog components.
		 * 
		 * @see SubComponentConfig#getComponents()
		 * @see LayoutComponent.Config#getDialogs()
		 * @see MainLayout#COMPONENT_DESCRIPTORS
		 */
		@Name(XML_TAG_DIALOGS_NAME)
		@EntryTag(COMPONENT)
		List<LayoutComponent.Config> getDialogs();

		@Name(XML_TAG_WINDOWS_NAME)
		List<WindowTemplate.Config> getWindows();

		@Name(XML_TAG_VIEWS_NAME)
		@Key(ViewConfiguration.Config.NAME_ATTRIBUTE)
		List<ViewConfiguration.Config<?>> getViews();

		/**
		 * The {@link CommandHandler} that should be executed when 'Enter' or 'Return' are pressed.
		 * <p>
		 * It will be registered as a {@link #getCommands() command} of this component.
		 * </p>
		 * 
		 * @implNote The name is not the normative <code>getDefaultCommand</code> as that would
		 *           conflict with legacy properties in subclasses. A migration to delete or rename
		 *           them would be too much effort.
		 * 
		 * @see #getCancelAction()
		 * @see DialogModel#getDefaultCommand()
		 */
		@Name(DEFAULT_ACTION)
		CommandHandler.ConfigBase<? extends CommandHandler> getDefaultAction();

		/**
		 * The {@link CommandHandler} that should be executed when the "Escape" key is pressed.
		 * <p>
		 * It will be registered as a {@link #getCommands() command} of this component.
		 * </p>
		 * 
		 * @implNote The name is not the normative <code>getCancelCommand</code> as that would
		 *           conflict with legacy properties in subclasses. A migration to delete or rename
		 *           them would be too much effort.
		 * 
		 * @see #getDefaultAction()
		 * @see DialogModel#getCloseAction()
		 */
		@Name(CANCEL_ACTION)
		CommandHandler.ConfigBase<? extends CommandHandler> getCancelAction();

		@Name(COMMANDS_NAME)
		@EntryTag("command")
		List<CommandHandler.ConfigBase<? extends CommandHandler>> getCommands();

		@Name(BUTTONS_NAME)
		@EntryTag("button")
		List<CommandHandler.ConfigBase<? extends CommandHandler>> getButtons();

		/**
		 * Plugins that executes additional resolve operations on this {@link LayoutComponent}
		 */
		@Name(COMPONENT_RESOLVER_NAME)
		List<PolymorphicConfiguration<ComponentResolver>> getComponentResolvers();

		/**
		 * Whether a toolbar should be allocated for this component automatically.
		 * 
		 * <p>
		 * Not allocating a toolbar prevents this component from being maximizable and removes the
		 * possibility of collapsing the component within a flexible layout.
		 * </p>
		 * 
		 * @see com.top_logic.layout.structure.LayoutControlFactory.Config#getAutomaticToolbars()
		 */
		@Name(TOOLBAR)
		boolean hasToolbar();

		/**
		 * Name of the ancestor component that is maximized, if the maximize button in this
		 * component's {@link #hasToolbar() toolbar} is pressed.
		 */
		@Name(MAXIMIZE_ROOT)
		ComponentName getMaximizeRoot();

		/**
		 * Configuration of {@link ToolBarGroup}s for this component.
		 */
		@Key(ToolBarGroupConfig.NAME_ATTRIBUTE)
		Map<String, ToolBarGroupConfig> getToolbarGroups();

		/**
		 * When set, references a help page for this component (the value is a legacy resource key,
		 * from which a tailing part is stripped to get the actual help ID).
		 * 
		 * @see #getHelpId()
		 */
		@Deprecated
		@Name("helpIDLegacy")
		@Nullable
		String getHelpIdLegacy();

		/**
		 * When set, references a help page for this component.
		 */
		@Name(HELP_ID)
		@Nullable
		String getHelpId();

		/**
		 * @see #getHelpId()
		 */
		void setHelpId(String value);

		/**
		 * Whether the component is {@link LayoutComponent#invalidate() invalidated}, when the it
		 * becomes invisible.
		 * 
		 * <p>
		 * This allows to release resources when this component is no longer visible.
		 * </p>
		 */
		@Name(RESET_INVISIBLE)
		boolean getResetInvisible();

		/**
		 * {@link ComponentDropTarget} that handles drop operations over the configured component.
		 */
		@Name(DROP_TARGET)
		@InstanceFormat
		@InstanceDefault(NoComponentDrop.class)
		ComponentDropTarget getDropTarget();

		/** @see #getDropTarget() */
		void setDropTarget(ComponentDropTarget value);

		/**
		 * Whether this component should be displayed, also when the component has no model.
		 */
		@Name(DISPLAY_WITHOUT_MODEL)
		boolean getDisplayWithoutModel();

		/**
		 * Optional {@link ResKey} to display when this component does not have a model.
		 * 
		 * @return May be <code>null</code>.
		 */
		@Name(NO_MODEL_KEY)
		ResKey getNoModelKey();

		/**
		 * General {@link Layouting} algorithm for leaf components.
		 */
		@InstanceFormat
		@InstanceDefault(ContentLayouting.class)
		Layouting getContentLayouting();
		
		/**
		 * Adds {@link BoundCommandGroup}s to the given collection, to be able to configure rights
		 * for them.
		 * 
		 * <p>
		 * It may be that for the configured component not all {@link BoundCommandGroup} can be
		 * derived from the configured commands. For this reason the component has the possibility
		 * to add additional {@link BoundCommandGroup}s for security configuration using this
		 * method.
		 * </p>
		 * 
		 * @param additionalGroups
		 *        Collection to add additional {@link BoundCommandGroup}s that are needed by the
		 *        configured component to.
		 */
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			additionalGroups.addAll(getCommandGroups());
		}

		/**
		 * I18N key for the component title.
		 * 
		 * <p>
		 * As default key, the {@link LayoutComponent#TITLE_SUFFIX} is used to derive a key from the
		 * {@link #getResPrefix()}.
		 * </p>
		 */
		static ResKey getEffectiveTitleKey(LayoutComponent.Config config) {
			return ResKey.fallback(config.getTitleKey(), getEffectiveResPrefix(config).key(TITLE_SUFFIX));
		}

		/**
		 * Wrapper for {@link Config#getResPrefix()} adding context informations if the ResPrefix on
		 * the component configuration is missing, i.e. {@link ResPrefix#NONE}.
		 */
		static ResPrefix getEffectiveResPrefix(Config config) {
			ResPrefix resPrefix = config.getResPrefix();

			if (resPrefix == ResPrefix.NONE) {
				ComponentName name = config.getName();

				if (LayoutConstants.isSyntheticName(name)) {
					return ResPrefix.none(config.getImplementationClass().getSimpleName());
				} else {
					return ResPrefix.none(name.qualifiedName());
				}
			}

			return resPrefix;
		}

	}

	private static final Property<ComponentName> COMMAND_MODEL_OWNER =
		TypedAnnotatable.property(ComponentName.class, "commandModelOwner");

	private static final String ATT_RES_PREFIX = "resPrefix";

	public static final String ATT_SAVE_SCROLL_POSITION = "saveScrollPosition";

	/**
	 * Attribute to indicate, whether this component shall be rendered within main layout, or as
	 * iframe
	 */
	protected static final String ATT_RENDER_INLINE = "renderInline";

    /** Commands are dispatched using this Parameter. */
	public static final String COMMAND = "_tl_" + "command";

    /** name of xml tag encapsulating the dialogs of a component */
    public static final String XML_TAG_DIALOGS_NAME = "dialogs";

    /** name of xml tag encapsulating the {@link WindowComponent}s of a component. */
    public static final String XML_TAG_WINDOWS_NAME = "windows";

    /** name of xml tag defining a single {@link LayoutComponent} of a component. */
    public static final String XML_TAG_COMPONENT_NAME = "component";
    
    public static final String XML_TAG_COMPONENT_CONTROLPROVIDER_NAME = "componentControlProvider";

	public static final String XML_TAG_VIEWS_NAME = "views";

	/**
	 * XML attribute defining the name of the close command if this component is
	 * opened as dialog.
	 */
    private static final String ATT_CLOSE_HANDLER_NAME = "closeHandlerName";

    /** XML definition for body css class */
    public static final String ATT_BODY_CLASS = "bodyClass";

	private static final String ATT_USE_CHANGE_HANDLING = "useChangeHandling";

	/**
	 * @see #shouldRecord()
	 */
	private static final String ATT_DONT_RECORD = "dontRecord";

	private static final boolean DEFAULT_FOR_DONT_RECORD = false;

	/**
	 * Prefix for command parameters. Command parameters are prefixed to avoid conficts with other
	 * parameters
	 */
	public static final String COMMAND_PREFIX = "_c_";

	/** Parameters used when dispatching to a command in a request */
	public static final Class<?> CMD_PARAMS[] = new Class[] { ServletContext.class, HttpServletRequest.class,
		HttpServletResponse.class };

	/**
	 * {@link EventType} type for changing {@link #isVisible()} property.
	 * 
	 * @see VisibilityListener
	 */
	public static final EventType<VisibilityListener, Object, Boolean> VISIBILITY_EVENT =
		new EventType<>("visibility") {

			@Override
			public Bubble dispatch(VisibilityListener listener, Object sender, Boolean oldValue, Boolean newValue) {
				return listener.handleVisibilityChange(sender, oldValue, newValue);
			}
		};

	/** @see #getButtonComponent() */
	private ButtonComponent _buttons;

    /**
     * A snipplet of js to be executed in the onScroll Handler.
     */
    private String executeOnscroll;

    /** The direct Parent of this component. */
	@Inspectable
	private LayoutComponent _parent;

	/**
	 * When component is shown as dialog, this is the top level component of the dialog. When a
	 * layout is opened, each inner component knows about the dialog.
	 */
	@Inspectable
	private LayoutComponent _dialog;

    /** The cached Top-Level Component, when available. */
	@Inspectable
    private MainLayout main;

	@Inspectable
	private Object _model;

	private LayoutComponent _window;

	@Inspectable
	private PropertyListeners _listeners;

	@Inspectable
	private ValidationListeners _validationListeners;

    /** Indicates that this component's model was changed after it was drawn.*/
    private boolean invalid;

	/** All {@link LayoutComponent}s of this LayoutComponent */
	@Inspectable
	private List<LayoutComponent> _dialogs = Collections.emptyList();

    /**
	 * All {@link WindowTemplate}s of this LayoutComponent.
	 */
	@Inspectable
	private List<WindowTemplate> windowTemplates;

    /** Indicates that this is scheduled for reload */
    private boolean forReload;

    /** The submitCount that should match the current component.
     *
     * Negated after validation (so when debugging you can check for its value).
     */
    private transient int submitNumber = -1;

	/**
	 * The configuration, this component was constructed with.
	 */
	@Inspectable
	protected final Config _config;

    /** Indicates the visibility of this component.*/
    private boolean visible = false;

	/** Indicates that the position of scrollbars should be restored after loading. */
	private final boolean saveScrollPosition;

    private boolean useChangeHandling;

	/**
	 * Lazy, because there are many {@link LayoutComponent}s, but only very few will ever have a
	 * property.
	 */
	@Inspectable
	private InlineMap<Property<?>, Object> _properties = InlineMap.empty();

	private final CommandHandler _defaultCommand;

	private final CommandHandler _cancelCommand;

	/** The Commands found at this component, indexed by their Id */
	private Map<String, CommandHandler> commandsById;

	/**
	 * All commands that have been registered for direct display at the UI.
	 */
	private Map<String, List<CommandHandler>> _buttonsByCliqueLazy;

	/**
	 * Set to true when the buttons should always be reloaded.
	 * 
	 * You should only set this if you cant invalidate the buttons yourself correctly. Before doing
	 * so review your command functions, the <code>modelChanges()</code> and
	 * <code>setInvalid()</code> functions.
	 */
	protected boolean alwaysReloadButtons;

	private LayoutComponent currentDialog;

	private HashMap<String, HTMLFragment> viewByName;

	/** Flag to indicate that there are command models to attach. */
	private boolean _commandModelsToAttach = false;

	/**
	 * Returned by {@link #getEnclosingFrameScope()}
	 * 
	 * @see #getEnclosingFrameScope()
	 */
	private LayoutComponentScope scope;

	/**
	 * @see #getExpansionState()
	 */
	private static final Property<ExpansionState> EXPANSION_STATE =
		TypedAnnotatable.property(ExpansionState.class, "expansionState");

	/**
	 * Convenience value to return from an overridden {@link #channels()} implementation for a
	 * component that has no channels at all.
	 */
	protected static final Map<String, ChannelSPI> NO_CHANNELS = Collections.<String, ChannelSPI> emptyMap();

	/**
	 * Default result of {@link #channels()}.
	 */
	public static final Map<String, ChannelSPI> MODEL_CHANNEL = channels(NO_CHANNELS, ModelChannel.INSTANCE);

	private static final ChannelListener ON_MODEL_SET = new ChannelListener() {
		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			sender.getComponent().handleModelChange(oldValue, newValue);
		}
	};

	/**
	 * Whether a new model has been set and {@link #doValidateModel(DisplayContext)} must be
	 * triggered.
	 */
	private boolean _validationRequested = true;

	private ToolBar _contextToolbar;

	private final boolean _initiallyMinimized;

	private boolean _resetScrollPosition;

	private boolean _resolved;

	private Map<TLType, LayoutComponent> _gotoTargets;

	private final Map<ModelSpec, ChannelLinking> _channelLinkingByConfig = new HashMap<>();

	private boolean _observeAllTypes = false;

	private Set<TLStructuredType> _observedTypes = Set.of();

	private Set<TLObject> _observedObjects = Set.of();

	/**
	 * When <code>true</code> this will result in some extra comments written to the HTML-header.
	 * 
	 * @see LayoutConfig#isDebugHeadersEnabled()
	 */
	public static boolean isDebugHeadersEnabled(){
		return ApplicationConfig.getInstance().getConfig(LayoutConfig.class).isDebugHeadersEnabled();
	}

	/**
	 * Utility for extending {@link #channels()} maps.
	 * 
	 * @param base
	 *        The base channels.
	 * @param channels
	 *        The additional channels.
	 * @return The new value for a channels constant.
	 */
	public static Map<String, ChannelSPI> channels(Map<String, ChannelSPI> base, ChannelSPI... channels) {
		HashMap<String, ChannelSPI> result = new HashMap<>(base);
		for (ChannelSPI channel : channels) {
			result.put(channel.getName(), channel);
		}
		return Collections.unmodifiableMap(result);
	}

	/**
	 * Global flag for enabling or disabling client-side logging.
	 */
	public static boolean isLoggingEnabled() {
		return ApplicationConfig.getInstance().getConfig(LayoutConfig.class).isClientSideLoggingEnabled();
	}
	
	/**
	 * Constructor for XML de-serialization.
	 * 
	 * Per default only the "id", "name" and "visible" attributes are cared for.
	 * 
	 * @param atts
	 *        the attributes of the component xml-tag.
	 * @throws ConfigurationException
	 *         iff the configuration is invalid
	 */
    public LayoutComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		_config = atts;

        saveScrollPosition = atts.getSaveScrollPosition();

        useChangeHandling = (atts.getUseChangeHandling() == null) ? isChangeHandlingDefault() : atts.getUseChangeHandling();

		List<WindowTemplate.Config> windowConfigs = atts.getWindows();
		if (!windowConfigs.isEmpty()) {
			windowTemplates = (List) TypedConfiguration.getInstanceList(context, windowConfigs);
		} else {
			windowTemplates = Collections.emptyList();
		}
		_initiallyMinimized = atts.isInitiallyMinimized();
		doResetScrollPosition(false);
		_defaultCommand = context.getInstance(atts.getDefaultAction());
		_cancelCommand = context.getInstance(atts.getCancelAction());
    }

	@Override
	public boolean shouldRecord() {
		return !getConfig().getDontRecord();
	}

	/**
	 * Creates components configured as parts.
	 */
	public void createSubComponents(InstantiationContext context) {
		List<Config> dialogConfigs = getConfig().getDialogs();
		if (!dialogConfigs.isEmpty()) {
			_dialogs = TypedConfiguration.getInstanceList(context, dialogConfigs);
			initDialogParents();

			for (LayoutComponent child : _dialogs) {
				ComponentInstantiationContext.createSubComponents(context, child);
			}
		}
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	private static final LayoutComponent.GlobalConfig getGlobalConfig() {
		return ApplicationConfig.getInstance().getConfig(LayoutComponent.GlobalConfig.class);
	}

	/**
	 * the {@link CommandHandler#getID()} of the handler used to close this component
	 */
	protected String getDefaultCloseDialogHandlerName() {
		return CloseModalDialogCommandHandler.HANDLER_NAME;
	}

	/**
	 * This method is a hook for subclasses to determine whether change handling for this component
	 * is default. If nothing is configured using {@link #ATT_USE_CHANGE_HANDLING} the return value
	 * determines whether dirty handling happens.
	 * <p>
	 * <b> Be careful: this method is called from within a constructor. The return value must be
	 * constantly <code>true</code> or <code>false</code> </b>
	 * </p>
	 * 
	 * @return <code>true</code> for this super class
	 */
    protected boolean isChangeHandlingDefault() {
		return true;
	}

    /**
     * Returns the name, id and class of this component.
     */
    @Override
	public String toString() {
		return getName() + "(" + getClass().getName() + " in " + getLocation() + ")";
    }

	/**
	 * Checks whether the given object can be used to call {@link #setModel(Object)}.
	 * 
	 * <p>
	 * This method transforms the given object to a model which is the potential value of
	 * {@link #getModel()} and checks whether this component supports the model.
	 * </p>
	 * 
	 * @param object
	 *        A potential input of {@link #setModel(Object)}.
	 * @return <code>true</code> when this model can be used to call {@link #setModel(Object)}.
	 */
	public final boolean supportsModel(Object object) {
		return supportsInternalModel(object);
	}

	/**
	 * Determines whether the given object is supported as {@link #getModel()} in this component.
	 * 
	 * @param object
	 *        The potential model. May be <code>null</code>.
	 * 
	 * @return Whether the given object is accepted as model for this component.
	 */
	protected boolean supportsInternalModel(Object object) {
		return true;
	}

    /**
     * the top dialog component knows its creation component.
     */
	protected final void setDialogParent(LayoutComponent newDialogParent) {
		setParent(newDialogParent);

		if (newDialogParent == null) {
			initDialog(null);
		} else {
			initDialog(this);
		}
    }

	protected void initDialog(LayoutComponent newDialog) {
		_dialog = newDialog;
	}

    /**
     * Accessor to set the parentLayout. <b>Always set the id after calling this
     * method!</b>
     */
	public void setParent(LayoutComponent newParent) {
		if (_parent == newParent) {
            return;
        }

		LayoutComponent oldParent = _parent;
		if (oldParent != null) {
			_parent = null; // avoid infinite recursion
			MainLayout oldMain = oldParent.getMainLayout();
			if (oldMain != null && ((newParent == null) || !oldMain.equals(newParent.getMainLayout()))) {
				registerMainLayout(null);
			}
        }

		_parent = newParent;

		if (newParent != null) {
			if (!isTopLevelDialog()) {
				initDialog(newParent.getDialogTopLayout());
			}
			MainLayout newMain = newParent.getMainLayout();
			if (newMain != null && ((oldParent == null) || !newMain.equals(oldParent.getMainLayout()))) {
				registerMainLayout(newMain);
			}
		} else {
			main = null;
			_window = null;
		}
    }

	/**
	 * Makes this component subtree part of a (new) {@link MainLayout}.
	 * 
	 * @param newMainLayout
	 *        The new top-level component of this component tree. <code>null</code> means to
	 *        deregister this component tree from the current {@link MainLayout}.
	 */
	protected void registerMainLayout(MainLayout newMainLayout) {
		MainLayout oldMain = main;
		if (oldMain != null) {
			notifyRemoveFromMainLayout();
			oldMain.unregisterComponent(this);
		}
		main = newMainLayout;
		if (newMainLayout != null) {
			newMainLayout.registerComponent(this);
			LayoutComponent parent = getParent();
			if (parent != null) {
				_window = parent.getWindow();
			}
			notifyAddToMainLayout();
		} else {
			setToolBar(null);
		}
		getDialogs().forEach(dialog -> dialog.registerMainLayout(newMainLayout));
	}

	/**
	 * Informs this component that it will be removed from its {@link #getMainLayout()}. When the
	 * method is called, this component is still part of the layout tree.
	 * 
	 * <p>
	 * Implementors of this method must clean up its non local state, such that no garbage remains
	 * in the layout tree.
	 * </p>
	 * 
	 * @see #registerMainLayout(MainLayout)
	 */
	protected void notifyRemoveFromMainLayout() {
		deregisterListeners();
		/* Unlink all channels to other channels: It may be that e.g. a formerly opened window
		 * is registered at a component in the main window. Unlinking all channels ensures that
		 * the component in the base does not inform a (now) closed window component. */
		channels().values().forEach(channel -> {
			channel.lookup(this).unlinkAll();
		});
	}

	/**
	 * This component was just added to a new {@link MainLayout}.
	 */
	protected void notifyAddToMainLayout() {
		registerListeners();
	}

	/**
	 * Hook for subclasses for registering listeners.
	 * <p>
	 * Don't forget to override {@link #deregisterListeners()} to remove them again. And don't
	 * forget to call the super implementation in overrides.
	 * </p>
	 * <p>
	 * This is called when this component is added to a component tree.
	 * </p>
	 */
	protected void registerListeners() {
		/* Store which listeners were registered in fields for the deregistration. The ensures no
		 * listener is forgotten, should the method output change until deregistration. */
		_observeAllTypes = observeAllTypes();
		if (_observeAllTypes) {
			getModelScope().addModelListener(this);
			return;
		}
		_observedTypes = Set.copyOf(getTypesToObserve());
		_observedTypes.forEach(type -> getModelScope().addModelListener(type, this));
		_observedObjects = Set.copyOf(getObjectsToObserve());
		_observedObjects.forEach(object -> getModelScope().addModelListener(object, this));

		registerListenersForModel(getModel());
	}

	/**
	 * Hook for subclasses for deregistering the listeners registered with
	 * {@link #registerListeners()}.
	 * <p>
	 * This is called when this component is removed from its component tree.
	 * </p>
	 */
	protected void deregisterListeners() {
		if (_observeAllTypes) {
			getModelScope().removeModelListener(this);
			return;
		}
		_observedObjects.forEach(object -> getModelScope().removeModelListener(object, this));
		_observedObjects = Set.of(); /* Prevent memory leaks. */
		_observedTypes.forEach(type -> getModelScope().removeModelListener(type, this));
		_observedTypes = Set.of();

		deregisterListenersForModel(getModel());
	}

	/**
	 * Whether this component needs to register itself as {@link ModelListener} for everything, i.e.
	 * every type.
	 * <p>
	 * These methods are evaluated only when this component is added to the component tree. Their
	 * result has to be constant. They cannot be used for temporarily relevant objects like the
	 * model of the component.
	 * </p>
	 * <p>
	 * Try to use {@link #getObjectsToObserve()} or {@link #getTypesToObserve()} instead. Observe as
	 * little as possible. Unnecessary observations use unnecessary processing time.
	 * </p>
	 */
	protected boolean observeAllTypes() {
		return false;
	}

	/**
	 * The {@link TLStructuredType}s for which this component needs to register itself as
	 * {@link ModelListener}.
	 * <p>
	 * These methods are evaluated only when this component is added to the component tree. Their
	 * result has to be constant. They cannot be used for temporarily relevant objects like the
	 * model of the component.
	 * </p>
	 * 
	 * @see #getObjectsToObserve()
	 * @see #observeAllTypes()
	 */
	protected Set<? extends TLStructuredType> getTypesToObserve() {
		return emptySet();
	}

	/**
	 * The {@link TLObject}s for which this component needs to register itself as
	 * {@link ModelListener}.
	 * <p>
	 * These methods are evaluated only when this component is added to the component tree. Their
	 * result has to be constant. They cannot be used for temporarily relevant objects like the
	 * model of the component.
	 * </p>
	 * 
	 * @see #getTypesToObserve()
	 * @see #observeAllTypes()
	 */
	protected Set<? extends TLObject> getObjectsToObserve() {
		return emptySet();
	}

	/** Register as listener for {@link #getModel()} when it is a {@link TLObject}. */
	private void registerListenersForModel(Object model) {
		if (model == null) {
			return;
		}
		if (model instanceof TLObject) {
			/* Optimization for the most common case. */
			getModelScope().addModelListener((TLObject) model, this);
		} else {
			extractTLObjects(model).forEach(tlObject -> getModelScope().addModelListener(tlObject, this));
		}
	}

	/** @see #registerListenersForModel(Object) */
	private void deregisterListenersForModel(Object model) {
		if (model == null) {
			return;
		}
		if (model instanceof TLObject) {
			getModelScope().removeModelListener((TLObject) model, this);
		} else {
			extractTLObjects(getModel()).forEach(tlObject -> getModelScope().removeModelListener(tlObject, this));
		}
	}

	/**
	 * Extracts every {@link TLObject} from the given {@link Object}.
	 * <p>
	 * Subclasses which support a {@link #getModel() model} that is not an {@link Iterable} but can
	 * contain a {@link TLObject} in any way have to override this method appropriately. Without
	 * that, they won't be registered for updates of the {@link TLObject}.
	 * </p>
	 */
	protected Set<TLObject> extractTLObjects(Object model) {
		if (model instanceof Iterable) {
			return FilterUtil.filterSet(TLObject.class, (Iterable<?>) model);
		}
		return emptySet();
	}

    /**
     * Accessor to the parent.
     * @return LayoutComponent, null if we are the root of the layout hierarchy
     */
	public final LayoutComponent getParent() {
		return _parent;
    }

    /**
     * If we are a dialog, here comes our opener.
     *
     * @return the opener of this dialog.
     */
    public final LayoutComponent getDialogParent() {
		LayoutComponent dialogTopLayout = getDialogTopLayout();
		if (dialogTopLayout == null) {
			// Not opened as dialog
			return null;
		}
		return dialogTopLayout.getParent();
    }

	@Override
	public final boolean openedAsDialog() {
		return getDialogTopLayout() != null;
    }

	/**
	 * Layout information for the parent layout.
	 */
	public LayoutInfo getLayoutInfo() {
		return getConfig().getLayoutInfo();
	}

	/**
	 * When the configured component has to be opened as the top layout of a dialog, here are all
	 * the settings for it.
	 * 
	 * @return May be <code>null</code>, when the component is not the top layout in an dialog.
	 */
	public final DialogInfo getDialogInfo() {
		return getConfig().getDialogInfo();
	}

	private boolean isTopLevelDialog() {
		return getDialogTopLayout() == this;
	}

	/**
	 * If this {@link LayoutComponent} was opened as dialog, this method returns
	 * the component which was opened, i.e. it is the outermost component in its
	 * dialog. If this component is not opened in a dialog it returns
	 * <code>null</code>.
	 */
	public final LayoutComponent getDialogTopLayout() {
		return _dialog;
	}

    /**
	 * All dialogs of this component.
	 */
	public List<? extends LayoutComponent> getDialogs() {
		return _dialogs;
    }

	/**
	 * Updates the current list of {@link #getDialogs()}.
	 */
	@FrameworkInternal
	public void setDialogs(List<LayoutComponent> dialogs) {
		for (LayoutComponent oldDialog : _dialogs) {
			oldDialog.setDialogParent(null);
		}
		_dialogs = dialogs;
		initDialogParents();
	}

	private void initDialogParents() {
		for (int n = 0, cnt = _dialogs.size(); n < cnt; n++) {
			LayoutComponent dialog = _dialogs.get(n);

			dialog.setDialogParent(this);
		}
	}

    /**
     * Gets the dialog associated with this component with the given name.
     *
     * @param aName the name of the dialog as registered by xml.
     * @return the dialog.
     */
    public LayoutComponent getDialog(ComponentName aName) {
		List<? extends LayoutComponent> dialogs = getDialogs();

        int count = dialogs.size();
        for(int i=0; i<count; i++) {
			LayoutComponent theDialog = dialogs.get(i);
            if (theDialog.getName().equals(aName)) {
                return theDialog;
            }
        }
        return null;
    }

    /**
     * Gets the separate window template associated with this component with the given name.
     *
     * @param aName the name of the separate window as registered by xml.
     * @return the separate window.
     */
    public LayoutComponent getWindowTemplate(String aName) {
        int count = windowTemplates.size();
        for(int i=0; i<count; i++) {
			WindowTemplate theSepWindow = this.windowTemplates.get(i);
			if (theSepWindow.getTemplate().equals(aName)) {
                return theSepWindow;
            }
        }
        return null;
    }

	/**
     * Accessor to set the invalid flag to true, usually done at end of a page.
     * This ensures, that this component will be redrawn.
     */
    public void invalidate() {
        safelyCheckUpdate(this);

        /* it is not imported if we were valid or invalid.
         * when changing to invalid we _must_ set forReload to true!
         * in other case a crashed component which had not the chance to
         * become valid again will not be redrawn.
         * (e.g in case when clicking very very fast through all tabbers.)
         */
        this.forReload = true;      // Schedule for reload
        this.invalid = true;

		_validationRequested = true;

        if (alwaysReloadButtons) {
            invalidateButtons();
        }
        
		firePropertyChanged(InvalidationListener.INVALIDATION_PROPERTY, this, Boolean.FALSE, Boolean.TRUE);
    }
    
    /**
	 * Service method to add a listener to be informed if this component becomes invalid.
	 * 
	 * @implNote Calls {@link #addListener(EventType, PropertyListener)} with
	 *           {@link InvalidationListener#INVALIDATION_PROPERTY}.
	 * 
	 * @see #removeInvalidationListener(InvalidationListener)
	 */
	public final void addInvalidationListener(InvalidationListener listener) {
		addListener(InvalidationListener.INVALIDATION_PROPERTY, listener);
	}
    
    /**
	 * Service method to remove a listener to be not longer informed if this component becomes
	 * invalid.
	 * 
	 * @implNote Calls {@link #removeListener(EventType, PropertyListener)} with
	 *           {@link InvalidationListener#INVALIDATION_PROPERTY}.
	 * 
	 * @see #addInvalidationListener(InvalidationListener)
	 */
	public final void removeInvalidationListener(InvalidationListener listener) {
		removeListener(InvalidationListener.INVALIDATION_PROPERTY, listener);
    }

	private void safelyCheckUpdate(Object updater) {
		MainLayout theMainLayout = this.getMainLayout();
		if (theMainLayout != null) {
        	theMainLayout.checkUpdate(updater);
        }
//        else {
//            // Some components have inner tree components without MainLayout, so this is no error
//            Logger.error("Component has no MainLayout: '" + this + "')!", new Exception(), this);
//        }
	}

    /**
     * Accessor to set the invalid flag to false.
     *
     * The semantic is that the client model is equal with the server model.
     *
     * This method is only called by the framework!
     * Don't call it yourself or override it!
     */
    public void markAsValid() {
        this.invalid = false;
    }

    /** Accessor to read the invalid flag */
    public final boolean isInvalid() {
        return invalid;
    }

    /**
     * reset the forReload flag usually reset by parent components.
     *
     * The problem with a call to this method is:
     *
     * 1. Early call to this method.
     *    If you call this method very early, it is possible, that a very fast clicking user
     *    clicks so, that this method will be called, but the invalid flag will not be
     *    set to false. In this case, this component will not be reloaded by the reload mechanism
     *    anytime more, until you reload the whole site.
     *
     * 2. Late call to this method.
     *    If you call this method at a late point in time, it is possible, that
     *    two components will render a third component. Flickering would be the result.
     */
    public void resetForReload() {
        this.forReload = false;
    }

    /**
     * <code>true</code> if a reload script for this component was written.
     */
    public boolean wasReloadScriptWritten() {
        return !this.forReload;
    }

    /**
	 * Search and show the component that is capable to display this object.
	 * 
	 * @param anObject
	 *        The object to display
	 * @return Whether the goto succeeded.
	 */
	public final boolean gotoTarget(Object anObject) {
		return gotoTarget(anObject, null) != null;
    }

	/**
	 * Display component with given name, if it is is capable to display this object. Otherwise the
	 * default component for given object is displayed.
	 * 
	 * @param object
	 *        The object to display
	 * @param targetComponentName
	 *        Name of the component to display.
	 * @return The {@link LayoutComponent} which is finally used as target for the goto and the goto
	 *         succeeded; <code>null</code> if the goto does not succeeded.
	 */
	public final LayoutComponent gotoTarget(Object object, ComponentName targetComponentName) {
		GotoHandler gotoCmd = (GotoHandler) handler(GotoHandler.COMMAND);
		if (gotoCmd == null) {
			// no Goto handler found.
			return null;
		}
		return gotoCmd.gotoLayout(this, object, targetComponentName);
	}
    
	/**
	 * @deprecated Model events for persistent objects are fired automatically.
	 */
	@Deprecated
	public final void fireModelCreatedEvent(Wrapper aModel, Object sender) {
		throw new UnsupportedOperationException("Events for persistent objects are generated automatically.");
	}

    /**
	 * Fires a {@link ModelEventListener#MODEL_CREATED} event to all components
	 * of the component tree, this component is part of.
	 * 
	 * @param aModel The created business object.
	 * @param sender Undefined. TODO #294: Remove from signature.
	 */
	public final void fireModelCreatedEvent(Object aModel, Object sender) {
		fireModelEvent(aModel, sender, ModelEventListener.MODEL_CREATED);
    }
    
	/**
	 * @deprecated Model events for persistent objects are fired automatically.
	 */
	@Deprecated
	public final void fireModelDeletedEvent(Wrapper aModel, Object sender) {
		throw new UnsupportedOperationException("Events for persistent objects are generated automatically.");
	}

	/**
	 * Fires a {@link ModelEventListener#MODEL_DELETED} event to all components
	 * of the component tree, this component is part of.
	 * 
	 * @param aModel The deleted business object.
	 * @param sender Undefined. TODO #294: Remove from signature.
	 */
	public final void fireModelDeletedEvent(Object aModel, Object sender) {
		fireModelEvent(aModel, sender, ModelEventListener.MODEL_DELETED);
    }
    
	/**
	 * @deprecated Model events for persistent objects are fired automatically.
	 */
	@Deprecated
	public final void fireModelModifiedEvent(Wrapper aModel, Object sender) {
		throw new UnsupportedOperationException("Events for persistent objects are generated automatically.");
	}

	/**
	 * Fires a {@link ModelEventListener#MODEL_MODIFIED} event to all components
	 * of the component tree, this component is part of.
	 * 
	 * @param aModel The updated business object.
	 * @param sender Undefined. TODO #294: Remove from signature.
	 */
	public final void fireModelModifiedEvent(Object aModel, Object sender) {
		fireModelEvent(aModel, sender, ModelEventListener.MODEL_MODIFIED);
    }

	/**
	 * Pluggable algorithm of model change event firing.
	 * 
	 * <p>
	 * The algorithm implementation is provided in
	 * {@link #internalFireModelEvent(Object, Object, int)} by subclasses.
	 * </p>
	 * 
	 * @see LayoutComponent#fireModelCreatedEvent(Object, Object)
	 * @see LayoutComponent#fireModelModifiedEvent(Object, Object)
	 * @see LayoutComponent#fireModelDeletedEvent(Object, Object)
	 */
	final void fireModelEvent(Object model, Object sender, int eventType) {
		assert isModelChangeEvent(eventType) : "Only change events may be fired through an event forwarder.";

		if (model instanceof TLObject) {
			// Event dropped, will be synthesized.
		} else {
			internalFireModelEvent(model, sender, eventType);
		}
	}
	
	private static boolean isModelChangeEvent(int eventType) {
		switch (eventType) {
			case ModelEventListener.MODEL_CREATED:
			case ModelEventListener.MODEL_MODIFIED:
			case ModelEventListener.MODEL_DELETED:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Fires the given event from this component.
	 * 
	 * @deprecated Events must not be fired externally. Instead, a changing
	 *             method must be called on the component, which in turn fires
	 *             the event. See {@link #fireModelEvent(Object, int)}.
	 */
    @Deprecated
	public final void doFireModelEvent(Object aModel, Object changedBy, int eventType) {
    	assert noModelChangeEvent(eventType) : "Must use specific fireModelXxxxEvent() for change events.";
    	
		enqueueEvent(aModel, changedBy, eventType);
    }

	private static boolean noModelChangeEvent(int eventType) {
    	switch (eventType) {
    	case MODEL_CREATED:
    	case MODEL_MODIFIED:
    	case MODEL_DELETED:
    		return false;
    	default:
    		return true;
    	}
	}

	/**
	 * Fires the model event given through the model and the event type from
	 * this component.
	 * 
	 * @param aModel
	 *        The model that was changed.
	 * @param eventType
	 *        The flag, which kind of change occurred on the model.
	 *         
	 * @see #fireModelCreatedEvent(Object, Object) for externally signalling a creation event to a component hierarchy.
	 * @see #fireModelDeletedEvent(Object, Object) for externally signalling a deletion event to a component hierarchy.
	 * @see #fireModelModifiedEvent(Object, Object) for externally signalling a modification event to a component hierarchy.
	 * 
	 * @see #doFireModelEvent(Object, Object, int) for externally firing an internal component event for backwards compatibility only.
	 */
	protected final void fireModelEvent(Object aModel, int eventType) {
		enqueueEvent(aModel, this, eventType);
	}

	private void enqueueEvent(Object model, Object sender, int eventType) {
		MainLayout mainLayout = getMainLayout();
		if (mainLayout == null) {
			// Construction phase.
			return;
		}
		LayoutContext layoutContext = mainLayout.getLayoutContext();
		if (layoutContext == null) {
			// Construction phase.
			return;
		}
		layoutContext.enqueueAction(new ComponentEvent(this, model, sender, eventType));
	}

    /**
     * Initiate the direct dispatch of the given event.
     */
	void internalFireModelEvent(Object aModel, Object changedBy, int eventType) {
		MainLayout mainLayout = getMainLayout();
    	if (mainLayout != null) {
			mainLayout.broadcastModelEvent(aModel, changedBy, eventType);
    	}
	}
    
	/**
	 * @see #fireModelEvent(Object, int) for how to initiate event dispatching.
	 * 
	 * @see #internalBroadcastModelEvent(Object, Object, int)
	 * 
	 * @deprecated A component must use
	 *             {@link #fireModelEvent(Object, int)} to globally
	 *             announce a change to its own model. Externally, an event on a
	 *             component must not be triggered.
	 */
    @Deprecated
	public void modelChanged(Object aModel, Object changedBy, int eventType) {
		internalBroadcastModelEvent(aModel, changedBy, eventType);
    }

	/**
	 * Dispatches the model event given through the model, the sender and the
	 * event type to this component and all of its children components.
	 * 
	 * <p>
	 * <b>Note:</b> This method must not be overridden outside the layout
	 * framework. It is not declared package protected, because the Eclipse
	 * compiler seems to have a bug and reports an error in
	 * {@link LayoutContainer}.
	 * </p>
	 * 
	 * @param aModel
	 *        The model that was changed.
	 * @param changedBy
	 *        The component sender triggering the change (not declared to be of
	 *        type {@link LayoutComponent} due to a design flaw).
	 * @param eventType
	 *        The flag, which kind of change occurred on the model.
	 */
    protected void internalBroadcastModelEvent(Object aModel, Object changedBy, int eventType) {
        safelyCheckUpdate(changedBy);

        /* delegate events also to dialogs */
		List<? extends LayoutComponent> dialogs = getDialogs();
		if (!dialogs.isEmpty()) {
			for (LayoutComponent theDialog : dialogs) {
				theDialog.internalBroadcastModelEvent(aModel, changedBy, eventType);
            }
        }
        
        if (changedBy != this) {
			long timeBefore = System.currentTimeMillis();
            try {
				handleModelEvent(aModel, changedBy, eventType);
			} catch (Exception ex) {
				Logger.error("Problems sending event " + eventType + " for " + aModel + " (component "
					+ getClass().getName() + ": '" + this.getName() + "').", ex, LayoutComponent.class);
            }
			long elapsed = System.currentTimeMillis() - timeBefore;
			if (elapsed > 100) {
				if (LayoutComponent.isDebugHeadersEnabled()) {
					Logger.warn("Broadcast modelChanged lasts " + elapsed + " ms. (component " + getClass().getName()
						+ ": '" + this.getName() + "')", LayoutComponent.class);
				}
			}
        }
    }

	/**
	 * Gets the relative URL of this LayoutComponent.
	 * 
	 * @param context
	 *        the {@link DisplayContext} in which rendering occurs
	 * @return An {@link URLBuilder} to construct links to this component.
	 */
	public final URLBuilder getComponentURL(DisplayContext context) {
		return getEnclosingFrameScope().getURL(context);
	}

	/**
	 * Gets the relative URL of this LayoutComponent.
	 * 
	 * @param page
	 *        The calling JSP page context.
	 */
	@CalledFromJSP
	public final URLBuilder getComponentURL(PageContext page) {
		return getComponentURL(DefaultDisplayContext.getDisplayContext(page));
	}

	@Override
	public MainLayout getMainLayout() {
        return main;
    }
    
	/**
	 * The current user's {@link Theme}.
	 */
	protected Theme getTheme() {
		return ThemeFactory.getTheme();
	}

	/**
	 * Searches the enclosing window, where this component resides in. If this
	 * component is a window itself, this method returns this component.
	 * 
	 * @return enclosing window, where this component resides in, or
	 *         <code>null</code>, if this component is part of the main
	 *         component tree.
	 */
    public final WindowComponent getEnclosingWindow() {
        LayoutComponent theAncestor = this;
        while ((theAncestor != null) && (! (theAncestor instanceof WindowComponent))) {
			if (theAncestor.openedAsDialog()) {
				theAncestor = theAncestor.getDialogParent();
			} else {
				theAncestor = theAncestor.getParent();
			}
        }
        return (WindowComponent) theAncestor;
    }

	/**
     * Gets the current action number.
     * @return int
     */
    public int getSubmitNumber() {
        return submitNumber;
    }

    /** Write some default Javascript part, override as needed, but call super first. */
    public void writeJavaScript(String contextPath, TagWriter out, HttpServletRequest req)
                throws IOException {

		this.writeFunctionLessJs(contextPath, out, req);

		HTMLUtil.writeJavaScriptContent(out, "window.addEventListener('load', function() {");
        out.increaseIndent();

        this.writeInOnload(contextPath, out, req);
        out.decreaseIndent();
		HTMLUtil.writeJavaScriptContent(out, "});");

		HTMLUtil.writeJavaScriptContent(out, "window.addEventListener('unload', function() {");
        out.increaseIndent();
        this.writeInOnUnload(contextPath, out, req);
        out.decreaseIndent();
		HTMLUtil.writeJavaScriptContent(out, "});");


        if (hasOnscroll()) {
            HTMLUtil.writeJavaScriptContent(out, "function onScroll() {");
            out.increaseIndent();
            this.writeInOnscroll(contextPath, out, req);
            out.decreaseIndent();
            HTMLUtil.writeJavaScriptContent(out, "}");
        }

        if (hasCommands()) {
			out.nl();
			PrintWriter pOut = out.getPrinter();
            Resources   res      = Resources.getInstance();
			for (CommandHandler command : getCommands()) {
                writeCommandScript(contextPath, res, pOut, command);
            }
        }
    }

	/**
	 * Optionally called to write the part between the &lt;head&gt; tags.
	 */
    public void writeHeader(String contextPath, TagWriter out, HttpServletRequest req)
                throws IOException {
		HTMLUtil.writeEdgeDocumentMode(out);

    	if (LayoutComponent.isDebugHeadersEnabled()) {
    		writeDebugHeader(out);
    	}
		UserAgent userAgent = UserAgent.getUserAgent(req);
		AJAXCommandHandler.writeBALInclude(out, contextPath, userAgent);

        this.writeJSTags(contextPath, out, req);
		// Inlude the <i>TopLogic</i> AJAX library in the page header. This
		// automatically enables client-side logging of JavaScript errors.
		// Therefore, this should be done before writing any other
		// JavaScript.
		AJAXCommandHandler.writeComponentHeader(userAgent, contextPath, out, this);
        out.beginScript();
        this.writeJavaScript(contextPath, out, req);
        out.endScript();
        this.writeStyles(contextPath, out);
    }

	/**
	 * This method write additional information about this component for debugging.
	 * 
	 * @param out
	 *            The {@link TagWriter} to write to.
	 */
	public void writeDebugHeader(TagWriter out) throws IOException {
		// Used to for debugging grinder scripts
		out.writeComment("ComponentName='" + getName() + "'");
		out.writeComment("ComponentClass='" + this.getClass().getName() + "'");
		out.writeComment("ComponentLocation='" + StringServices.nonNull(getLocation()) + "'");
	}

	/**
	 * Creates all attributes of the <code>body</code> tag of the document.
	 * 
	 * <p>
	 * Must not be overridden, because the risk of duplicate attributes is to
	 * high.
	 * </p>
	 */
	public final void writeBodyAttributes(HttpServletRequest req, TagWriter out,
											boolean disableScrolling)
                throws IOException, ServletException {
		UserAgent userAgent = UserAgent.getUserAgent(req);

        // TODO Workaround for IE6 and IE7. Can be removed, if support runs out.
        //
        // Workaround removes phantom scrollbars as described in #3555.
        // Cause is a known bug within interpretation of html DOCTYPE
        // using standard compliance rendering mode.
		if (disableScrolling) {
			out.writeAttribute("scroll", "no");
        }

		out.beginCssClasses();
		writeBodyCssClassesContent(out);
		userAgent.writeBrowserClass(out);
		out.endCssClasses();
	}

    /** Always called to write the actual body start tag.
     *
     * Override to write additon attributes as needed.
     */
	public void writeBodyTag(ServletContext context, HttpServletRequest req, HttpServletResponse resp, TagWriter out,
								boolean disableScrolling)
                throws IOException, ServletException {

		out.beginBeginTag(HTMLConstants.BODY);
		writeBodyAttributes(req, out, disableScrolling);
        out.endBeginTag();
    }

    /**
	 * Called immediately before the rendering process of this component starts.
	 *
	 * <p>
	 * Sub-classes may add functionality by overriding, if the super-class
	 * method is guaranteed to be called.
	 * </p>
	 */
    public void beforeRendering(DisplayContext displayContext) {
    	this.increaseSubmitNumber();
    }

    /**
     * Called immediately after the rendering of this component has finished.
	 *
	 * <p>
	 * Sub-classes may add functionality by overriding, if the super-class
	 * method is guaranteed to be called.
	 * </p>
     */
    public void afterRendering() {
    	this.markAsValid();
    }

    /**
     * Always called to write the actual body part.
     *
     * <strong>This method must set the <code>invalid</code> attribute
     * to <code>false</code> when done. </strong>
     */
    public void writeBody(ServletContext context,
                          HttpServletRequest req,
                          HttpServletResponse resp,
                          TagWriter out)
                throws IOException, ServletException {
        // no body here
    }

    /**
     * Increases submitCount and set submitNumber. Should be called right after
     * the body-tag has been written, so the <code>prepare</code> method has
     * been called before.
     */
    public void increaseSubmitNumber() {
		int nextSubmitNumber = submitNumber + 1;
		if (nextSubmitNumber < 0) { // overflow ?
			nextSubmitNumber = 1;
        }
		setSubmitNumber(nextSubmitNumber);
    }

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(this);
	}

    /**
	 * Returns the name of this component.
	 *
	 * If nothing is configured a (unique) synthetic name is returned.
	 */
	public final ComponentName getName() {
		return getConfig().getName();
    }

	/**
	 * Is this Component currently visible to the user?
	 * 
	 * @return true when component has a Parent and is marked as visible.
	 */
	@Override
	public boolean isVisible() {
        /* defense programming... it is nice to have it... believe me (skr) */
		return getParent() != null && _isVisible();
    }

	/**
	 * Check visibility without checking for parent, too
	 *
	 * @return true when component is visible.
	 */
	protected final boolean _isVisible() {
        return visible;
    }

    /**
     * Sets the visibility.
     *
     * @param aVisible The visibility to set
     */
	@Override
	public void setVisible(boolean aVisible) {

//        /* ensure to set all dialogs to invisible when opener component
//         * becomes invisible.
//         *
//         * this is not really correct... a dialog should become invisible
//         * when the dialogwindow is closed.
//         */
//        if(aVisible == false) {
//            if(this.getDialogs() != null) {
//                Iterator iter = this.dialogs.iterator();
//                while (iter.hasNext()) {
//                    LayoutComponent theDialog = (LayoutComponent) iter.next();
//                    theDialog.setVisible(false);
//                }
//            }
//        }

        /* when we crashed, enure the next time we gets visible
         * (direct rendering without writeReloadJs), this flag is
         * set correct, so writeReloadJs works the next time.
         */
		if (aVisible) {
        	this.forReload = true;
        }

		if (aVisible == _isVisible()) {
            return; // no actual change
        }

        try {
			if (aVisible) {
			    this.becomingVisible();
			} else {
			    this.becomingInvisible();
			}
		} catch (RuntimeException ex) {
			InfoService.logError(com.top_logic.layout.form.component.I18NConstants.ERROR_VIEW_CREATION,
				"Making component '" + getName() + "' visible failed: " + getLocation(), ex,
				LayoutComponent.class);
		}

        // else no need to become invalid
        this.visible = aVisible;

		fireVisibilityChange(aVisible);
    }

    /**
     * Decides whether this LayoutComponent renders the content complete. If
     * <code>false</code>, an HTML-Structure will be produced. Overwrite to
     * adjust to your component type.
     *
     * @see PageComponent for a usage of this feature.
     *
     * @return boolean <code>false</code>
     */
    public boolean isCompleteRenderer() {
        return false;
    }

    /**
     * @see #useChangeHandling
     */
    public boolean getUseChangeHandling() {
    	return useChangeHandling;
    }
    
	protected final void setUseChangeHandling(boolean useChangeHandling) {
    	this.useChangeHandling = useChangeHandling;
    }

    /**
	 * Gets the first child component matching the given name. Iterates recursively through the this
	 * layout's list to find the component.
	 * 
	 * @param skipSet
	 *        a Set of LayoutComponents that are not returned.
	 * @param aName
	 *        the Name of the component searched for.
	 * @return a LayoutComponent with the given name or <code>null</code>, if none was found.
	 */
	public LayoutComponent getComponentByName(String aName, Set<? extends LayoutComponent> skipSet) {

        LayoutComponent theResult;

		List<? extends LayoutComponent> theDialogs = this.getDialogs();
		if (!theDialogs.isEmpty()) {
			for (LayoutComponent theDialog : theDialogs) {
                if (CollectionUtil.equals(aName, theDialog.getName()) && !skipSet.contains(theDialog)) {
                    return theDialog;
                }
                theResult = theDialog.getComponentByName(aName, skipSet);
                if(theResult != null) {
                	return theResult;
                }
			}
        }
        return null;
    }

    /**
     * Gets the first (in PreOrder) child component matching the given name.
     * Convenience method for <code>getChildComponentByName(String,Set)</code>
     */
    public LayoutComponent getComponentByName(ComponentName aName) {
		return getMainLayout().getComponentByName(aName);
    }

    /**
	 * Check if this component handles the given type.
	 *
	 * The type may either be a classname or (in TopLogic) some other meta-type. When this function
	 * returns true, supports Objects shoud be true, too.
	 * 
	 * @param type
	 *        the type. If <code>null</code> or empty false is returned
	 * @return true if the component handles the type
	 */
    public final boolean isDefaultFor(String type) {
		return getDefaultForTypes().contains(type);
    }
    
	/**
	 * Delegates to {@link Config#getDefaultFor()}.
	 * 
	 * @see Config#getDefaultFor()
	 */
	public final List<String> getDefaultForTypes() {
		return getConfig().getDefaultFor();
    }

	/**
	 * Mapping of {@link TLType} to {@link LayoutComponent} which must be used as goto target for
	 * that type (or any specialization).
	 * 
	 * @see Config#getGotoTargets()
	 */
	public Map<TLType, LayoutComponent> getGotoTargets() {
		return _gotoTargets;
	}

	/**
	 * Callback hook that is called during {@link #validateModel(DisplayContext) model validation}
	 * if a new {@link #getModel() model} was set into this component.
	 * 
	 * <p>
	 * This model is called during validation of the new model.
	 * </p>
	 * 
	 * @param newModel
	 *        The new model of this component.
	 * 
	 * @see #afterModelSet(Object, Object)
	 */
	protected void handleNewModel(Object newModel) {
		// Hook for subclasses.
	}

	/**
	 * The model of this component, <code>null</code> in case of an unsupported model.
	 * 
	 * @see #supportsModel(Object)
	 * @see #internalModel()
	 */
	@Override
	public final Object getModel() {
		return _model;
    }

	/**
	 * The input model currently delivered by the {@value ModelChannel#NAME} channel.
	 * 
	 * <p>
	 * In contrast to {@link #getModel()}, the result may not be
	 * {@link #supportsInternalModel(Object) supported} by this component, if an invalid model was
	 * set previously.
	 * </p>
	 */
	protected final Object internalModel() {
		ComponentChannel modelChannel = getChannelOrNull(ModelChannel.NAME);
		if (modelChannel == null) {
			return null;
		}
		return modelChannel.get();
	}

	/**
	 * Linking of the given {@link ModelSpec}.
	 */
	public ChannelLinking getChannelLinking(ModelSpec linkingConfig) {
		ChannelLinking channelLinking = _channelLinkingByConfig.get(linkingConfig);
		if (channelLinking == null) {
			channelLinking = TypedConfigUtil.createInstance(linkingConfig);
			_channelLinkingByConfig.put(linkingConfig, channelLinking);
		}

		return channelLinking;
	}

	/**
	 * All registered {@link ComponentChannel}s of this {@link LayoutComponent}.
	 */
	public final Collection<ComponentChannel> getChannels() {
		return channels().values().stream().map(spi -> spi.lookup(this)).collect(Collectors.toList());
	}

	@Override
	public final ComponentChannel getChannel(String kind) {
		ComponentChannel result = getChannelOrNull(kind);
		if (result == null) {
			throw new IllegalArgumentException(
				"Component of type '" + getClass().getName() + "' does not support a '" + kind
					+ "' channel. Available channels are " + channels().keySet() + " in " + getLocation());
		}
		return result;
	}

	/**
	 * Same as {@link #getChannel(String)}, but <code>null</code>, if no such channel exists.
	 */
	public final ComponentChannel getChannelOrNull(String kind) {
		ChannelSPI channelSPI = channelSPI(kind);
		if (channelSPI == null) {
			return null;
		}
		return channelSPI.lookup(this);
	}

	/**
	 * The {@link ChannelSPI} of the {@link ComponentChannel} with the given name, or
	 * <code>null</code>, if this component does not support such channel.
	 */
	public final ChannelSPI channelSPI(String name) {
		return channels().get(name);
	}

	/**
	 * Names of all defined component channels.
	 * 
	 * @see #getChannel(String)
	 */
	public final Set<String> getChannelNames() {
		return channels().keySet();
	}

	/**
	 * Available channel kinds of this component implementation.
	 */
	protected Map<String, ChannelSPI> channels() {
		if (this instanceof Selectable) {
			return Selectable.MODEL_AND_SELECTION_CHANNEL;
		} else {
			return MODEL_CHANNEL;
		}
	}

	/**
	 * Set the new model to be used by this component.
	 * 
	 * @param newModel
	 *        The new model to be used, may be <code>null</code>.
	 * @return Whether a new model was set.
	 */
	public final boolean setModel(Object newModel) {
		return modelChannel().set(newModel);
	}

	/**
	 * Marker for upgrading legacy components with invalid {@link Selectable} implementation.
	 * 
	 * @deprecated When the selection of a component changes, {@link Selectable#setSelected(Object)}
	 *             must be called. This implicitly transfers the new selection to all dependent
	 *             components.
	 */
	@Deprecated
	protected void fireSelection(Object newSelection) {
		Logger.error("Invalid call to fireSelection(" + newSelection + ") from " + getClass().getName() + ".",
			new StackTrace(), LayoutComponent.class);
	}

	/**
	 * Handler method invoked, when the {@value ModelChannel#NAME} channel changes its value.
	 *
	 * @param oldModel
	 *        The old value.
	 * @param newModel
	 *        The newly delivered model value.
	 */
	final void handleModelChange(Object oldModel, Object newModel) {
		// Must only check for "supports", if model is valid, because the method accesses the
		// object (e.g. its type or values) which is not possible on a deleted object.
		if (ComponentUtil.isValid(newModel) && supportsInternalModel(newModel)) {
			_model = newModel;
		} else {
			_model = null;
		}

		afterModelSet(oldModel, newModel);
	}

	/**
	 * Callback method to react on setting a new model to the component's {@value ModelChannel#NAME}
	 * channel.
	 * 
	 * <p>
	 * This method is called directly after the model was set (before component re-validation).
	 * </p>
	 * 
	 * @param oldModel
	 *        Former value of {@link #internalModel()}
	 * @param newModel
	 *        New value of {@link #internalModel()}
	 * @see LayoutComponent#handleNewModel(Object)
	 */
	protected void afterModelSet(Object oldModel, Object newModel) {
		if (!_observeAllTypes) {
			deregisterListenersForModel(oldModel);
			registerListenersForModel(newModel);
		}
		invalidate();
	}

	/**
	 * Correctly react when model is removed.
	 */
	@Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> aModel, Object changedBy) {
		/* has anyone deleted the model we are currently editing? */
		boolean becameInvalid;
		if (changedBy != this && aModel.contains(getModel())) {
			if (!hasMaster()) {
				ModelSpec modelSpec = getConfig().getModelSpec();
				if (modelSpec != null) {
					setModel(TypedConfigUtil.createInstance(modelSpec).eval(this));
				}
			}
			this.invalidate();
			becameInvalid = true;
		} else {
			becameInvalid = false;
		}

		boolean superInvalidated = super.receiveModelDeletedEvent(aModel, changedBy);
		return becameInvalid || superInvalidated;
	}

	@Override
    protected boolean receiveModelChangedEvent(Object aModel, Object changedBy ) {
        if (! isInvalid()) {
			if (aModel != null && changedBy != this && aModel == getModel()) {
				return receiveMyModelChangeEvent(changedBy);
           }
       }
       return false;
   }

	/**
	 * Hook for individual model change event handling.
	 * 
	 * @param changedBy
	 *        The modifier called this method.
	 * 
	 * @see #receiveModelChangedEvent(Object, Object)
	 */
	protected boolean receiveMyModelChangeEvent(Object changedBy) {
		this.invalidate();

		return true;
	}

	@Override
	protected final boolean receiveWindowOpenedEvent(Object aModel, Object changedBy) {
		boolean handleTypeResult;
		if (this.isVisible()) {
			handleTypeResult = this.receiveWindowEvent(aModel, changedBy, true);
		} else {
			handleTypeResult = false;
		}
		return handleTypeResult;
	}

	@Override
	protected final boolean receiveWindowClosedEvent(Object aModel, Object changedBy) {
		boolean handleTypeResult;
		if (this.isVisible()) {
			handleTypeResult = this.receiveWindowEvent(aModel, changedBy, false);
		} else {
			handleTypeResult = false;
		}
		return handleTypeResult;
	}

	@Override
	protected final boolean receiveDialogOpenedEvent(Object aModel, Object changedBy) {
		boolean handleTypeResult;
		if (this.isVisible()) {
			handleTypeResult = this.receiveDialogEvent(aModel, changedBy, true);
		} else {
			handleTypeResult = false;
		}
		return handleTypeResult;
	}

	@Override
	protected final boolean receiveDialogClosedEvent(Object aModel, Object changedBy) {
		boolean handleTypeResult;
		if (this.isVisible()) {
			handleTypeResult = this.receiveDialogEvent(aModel, changedBy, false);
		} else {
			handleTypeResult = false;
		}
		return handleTypeResult;
	}

    /**
     * Signal, that a dialog was opened or closed.
     *
     * Will only be called when componet is visible.
     * At this time the dialog is no yet visible or not yet removed.
     * Intended to disable parts (e.E. forms ...) of the GUI that conflict with the dialog.
     * In contrast to other events in this case the owener == this may be true.
     *
     * @param    aDialog      The dialog opened or closed.
     * @param    anOwner      The component that opend / closed the dialog
     * @param    isOpen       true when dialog was opened
     * @return   <code>true</code> to indicate that this part (or subparts)
     *           have become invalid.
     */
    public boolean receiveDialogEvent(Object aDialog, Object anOwner, boolean isOpen) {
        return false;
    }

    /**
     * Signal, that a separate window was opened or closed.
     *
     * Will only be called when componet is visible.
     * At this time the separate window is no yet visible or not yet removed.
     * In contrast to other events in this case the owener == this may be true.
     *
     * @param    aWindow      The window that was opened or closed.
     * @param    anOwner      The component that opend / closed the separate window
     * @param    isOpen       true when separate window was opened
     * @return   <code>true</code> to indicate that this part (or subparts)
     *           have become invalid.
     */
    public boolean receiveWindowEvent(Object aWindow, Object anOwner, boolean isOpen) {
        return false;
    }

    /**
	 * Write the JavaScript content of the onLoad() function.
	 */
	protected void writeInOnload(String aContext, TagWriter anOut, HttpServletRequest aRequest) throws IOException {

		if (hasOnscroll()) {
			HTMLUtil.writeJavaScriptContent(anOut, "window.onscroll = onScroll;");
		}

		if (this.saveScrollPosition) {
			String scrollPositionKey = this.getScrollPositionKey();
			if (shallResetScrollPosition()) {
				anOut.writeIndent();
				SaveScrollPosition.writeResetScrollPositionScript(anOut, scrollPositionKey);
				doResetScrollPosition(false);
			} else {
				HTMLUtil.writeJavaScriptContent(anOut,
					SaveScrollPosition.getPositionViewportCommand(scrollPositionKey));
			}
		}
		/*
		 * hide loading must happen in top Window not in mainLayout, since it
		 * doesn't know anything about frames in opened windows
		 */
		HTMLUtil.writeJavaScriptContent(anOut, "services.ajax.topWindow.services.ajax.hideLoading(this);");
		HTMLUtil.writeJavaScriptContent(anOut, "services.form.installKeyEventHandler('" + getWindowId() + "');");
	}

	private String getWindowId() {
		if (getEnclosingWindow() == null) {
			return getMainLayout().getLayoutControl().getID();
		} else {
			return getEnclosingWindow().getLayoutControl().getID();
		}
	}

	/**
     * Write the JavaScript content of the onUnload() function. Override if needed...
     */
    protected void writeInOnUnload(String aContext, TagWriter anOut, HttpServletRequest aRequest) throws IOException {
        // Empty implementation. We don't need to write anything to onUnload here. Override if needed...
    }


    /**
     * true when a onScroll Handler should be generated.
     */
    public boolean hasOnscroll() {
        return this.saveScrollPosition || executeOnscroll != null;
    }

    /**
     * Write the JavaScript content of the onLoad() function.
     */
    protected void writeInOnscroll(String aContext, TagWriter anOut, HttpServletRequest aRequest) throws IOException {

        if (this.saveScrollPosition) {
			/*
			 * window.saveScrollPosition == null may happen in IE8 when using mousewheel
			 * during loading page.
			 */
			anOut.writeIndent();
			SaveScrollPosition.writePushScrollPositionScript(anOut, getScrollPositionKey());
        }
        if (executeOnscroll != null) {
            HTMLUtil.writeJavaScriptContent(anOut, executeOnscroll);
        }
    }

	/**
     * Mutator of the action number.
     */
    protected void setSubmitNumber(int number) {
        submitNumber = number;
    }

    /**
     * Here all the tuning happens to prepare dialogs to be displayed.
     */
	private void initDialogs(InstantiationContext context) {
        this.registerDialogOpeners(context);
    }

    /**
     * Make me the owner of my {@link WindowTemplate}s and register opening commands
     */
    private void initWindows(InstantiationContext context) {
        final int count = this.windowTemplates.size();
        for (int i=0; i<count; i++) {
			WindowTemplate windowTemplate = this.windowTemplates.get(i);
        	windowTemplate.setOwner(this);
			try {
				registerWindowOpener(context, (WindowTemplate.Config) windowTemplate.getConfig());
			} catch (Exception e) {
				Logger.error("unable to build handler for opening separate window", e, LayoutComponent.class);
			}
        }

	}

	private void registerWindowOpener(InstantiationContext context, WindowTemplate.Config aSepWin) {
		PolymorphicConfiguration<? extends CommandHandler> config = OpenWindowCommand.createWindowOpenHandler(aSepWin);
		CommandHandler handler = CommandHandlerFactory.getInstance().getCommand(context, config);
		registerCommandHandler(handler, aSepWin.getWindowInfo().getCreateOpenerButtons());
    }

	/**
	 * At this time all components have been loaded and the mainLayout may be available.
	 *
	 * Your components can override this method. After call to super it is guaranteed, that your
	 * parent and MainLayout are set properly.
	 *
	 * Use it to make a very late lowlevel postinitialization. To set and init your model use
	 * {@link LayoutComponent#becomingVisible()}.
	 *
	 * This method is called only once for each component.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate additional confiurations.
	 */
	protected final void resolveComponent(InstantiationContext context) {
		if (_resolved) {
			if (Logger.isDebugEnabled(LayoutComponent.class)) {
				Logger.debug("Component " + this + " already resolved.", LayoutComponent.class);
			}
			return;
		}
		try {
			componentsResolved(context);
		} catch (Throwable ex) {
			Logger.error("Error resolving component " + getName() + ": " + ex.getMessage(), ex, LayoutComponent.class);
		}
		_resolved = true;
	}

	/**
	 * Actual implementation of {@link #resolveComponent(InstantiationContext)}.
	 * 
	 * <p>
	 * Do not call this method to resolve other components. Use
	 * {@link #resolveComponent(InstantiationContext)} instead.
	 * </p>
	 */
    protected void componentsResolved(InstantiationContext context) {
		// must install enclosing scope here, as MainLayout registers the
		// enclosing scope during construction at TLContext which is necessary
		// before the first rendering.
    	installEnclosingFrameScope();

		this.initDialogs(context);
        this.initWindows(context);

        /* delegate to dialogs */
		List<? extends LayoutComponent> dialogs = getDialogs();
		if (!dialogs.isEmpty()) {
			final int count = dialogs.size();
            for (int i=0; i<count; i++) {
				LayoutComponent theDialog = dialogs.get(i);
				theDialog.resolveComponent(context);
            }
        }

		ComponentName buttonComponentName = _config.getButtonComponent();
		if (buttonComponentName != null) {
			LayoutComponent buttonComponent = getComponentByName(buttonComponentName);
			if (buttonComponent == null) {
				throw new ConfigurationError("Undefined button component reference '" + buttonComponentName + "' in '"
					+ _config.location() + "'.");
			}
			if (!(buttonComponent instanceof ButtonComponent)) {
				throw new ConfigurationError("Not a button component '" + buttonComponentName + "' in '"
					+ _config.location() + "'.");
			}
			setButtonComponent((ButtonComponent) buttonComponent);
        }

		CommandHandlerFactory factory = CommandHandlerFactory.getInstance();
		/* Register only the _configured_ default command, as it is guaranteed to be constant. If
		 * getDefaultCommand is overridden its result might not be constant, which might require
		 * dynamic registration and deregistration, complicating it a lot. Therefore, the overriding
		 * class has to take care of the registration in that case. */
		if (getConfiguredDefaultCommand() != null) {
			registerCommand(getConfiguredDefaultCommand());
		}
		if (getConfiguredCancelCommand() != null) {
			registerCommand(getConfiguredCancelCommand());
		}
		List<CommandHandler.ConfigBase<? extends CommandHandler>> commandConfigs = _config.getCommands();
		if (!commandConfigs.isEmpty()) {
			for (CommandHandler.ConfigBase<? extends CommandHandler> commandConfig : commandConfigs) {
				registerCommand(factory.getCommand(context, commandConfig));
        	}
		}

		// must add buttons before calling {@link #registerButtons(ButtonComponent)} as in this
		// method commands are added to button component
		List<CommandHandler.ConfigBase<? extends CommandHandler>> buttonConfigs = _config.getButtons();
		if (!buttonConfigs.isEmpty()) {
			for (CommandHandler.ConfigBase<? extends CommandHandler> commandConfig : buttonConfigs) {
				registerButtonCommand(factory.getCommand(context, commandConfig));
        	}
        }

		SimpleCommandRegistry registry = new SimpleCommandRegistry();
		_config.modifyIntrinsicCommands(registry);
		registry.getCommands().forEach(command -> {
			registerCommandHandler(command, false);
		});

		registry.getButtons().forEach(command -> {
			registerCommandHandler(command, true);
		});

        this.registerAdditionalNonConfigurableCommands(); 
        registerButtons();
        
		initConfiguredViews(context);
		loadExpansionState();

		List<PolymorphicConfiguration<ComponentResolver>> resolvers = _config.getComponentResolvers();
		for (int index = 0, size = resolvers.size(); index < size; index++) {
			PolymorphicConfiguration<ComponentResolver> config = resolvers.get(index);
			context.getInstance(config).resolveComponent(context, this);
		}

		_gotoTargets = LayoutUtils.resolveGotoTargets(context, getMainLayout(), getConfig().getGotoTargets().values());
	}

    /** Try to make this component visible.
     *
     * This will usually involve making the parent visible.
     * It may fail in case the componenet is oprhaned or
     * otherwise unable to become visible.
     *
     * @return true when component is visible after the call.
     */
    public boolean makeVisible() {
		if (!_isVisible()) {
            LayoutComponent  theDialogParent = getDialogParent();
			LayoutComponent parent;
            if (theDialogParent != null) {
                
                LayoutComponent topDialog = getDialogTopLayout();
				if (topDialog == this) {
					parent = null;
				} else {
					parent = getParent();
				}
				/*
				 * If this is a LayoutComponent in a dialog and the dialog is not already opened, open
				 * that dialog.
				 */
				if (!topDialog.isVisible()) {
					List<? extends LayoutComponent> allDialogs = theDialogParent.getDialogs();
					DialogSupport dialogSupport = getDialogSupport();
					// Close any potential open Sibling Dialogs
					for (int index = 0, size = allDialogs.size(); index < size; index++) {
						if (dialogSupport.isDialogOpened(allDialogs.get(index))) {
							allDialogs.get(index).closeDialog();
						}
					}
					dialogSupport.openDialogContaining(this);
				}
			} else {
				parent = getParent();
			}

			if (parent != null) {
				return ((LayoutContainer) parent).makeVisible(this);
			} else {
				/* "this" is either the top level layout in a dialog, the main layout or a window,
				 * i.e. visible */
			}
        }
        return true;
    }

    /**
     * (makes only sense, when this component was opened as a dialog.)
     * @return <code>true</code> if the changer is the component opened this component.
     */
    public boolean changedByDialogOpener(Object changedBy) {
        if(!this.openedAsDialog()) {
            return false;
        }
        else {
            return this.getDialogParent() == changedBy;
        }
    }

    /**
     * <code>true</code> when the given Object is a dialog of this component.
     */
    public boolean changedByDialog(Object changedBy) {
		if (!(changedBy instanceof LayoutComponent) || getDialogs().isEmpty()) {
            return false;
        }
        LayoutComponent theChanger = (LayoutComponent) changedBy;
        return (theChanger.getDialogParent() == this);
    }

    /**
     * @see #getMasters()
     */
    public LayoutComponent getMaster() {
		Collection<? extends LayoutComponent> masters = getMasters();
		switch (masters.size()) {
			case 0:
				return null;

			case 1:
				return masters.iterator().next();

			default:
				throw new IllegalStateException(
					"There is no single master of '" + getName() + "', masters: " + masters.stream()
						.map(LayoutComponent::getName).map(Object::toString).collect(Collectors.joining(", ")) + ".");
		}
    }

	/**
	 * The {@link ComponentChannel} providing the {@link #getModel()}.
	 */
	public final ComponentChannel modelChannel() {
		return getChannel(ModelChannel.NAME);
	}

	/**
	 * Whether this component has at least one master component.
	 */
	public final boolean hasMaster() {
		return !getMasters().isEmpty();
	}

    /**
	 * {@link LayoutComponent}s that are sources of the {@link #modelChannel()} of this component.
	 * 
	 * @see #getSlaves()
	 * @see #getMaster()
	 */
	public Set<? extends LayoutComponent> getMasters() {
		return sourceComponents(new HashSet<>(), modelChannel().sources());
    }

	private Set<? extends LayoutComponent> sourceComponents(Set<ComponentChannel> processed,
			Collection<ComponentChannel> sources) {
		switch (sources.size()) {
			case 0:
				return Collections.emptySet();
			case 1:
				return sourceComponents(processed, sources.iterator().next());
			default:
				return sources
					.stream()
					.flatMap(source -> sourceComponents(processed, source).stream())
					.collect(Collectors.toSet());
		}
	}

	private Set<? extends LayoutComponent> sourceComponents(Set<ComponentChannel> processed, ComponentChannel ch) {
		if (!processed.add(ch)) {
			// already processed
			return Collections.emptySet();
		}
		LayoutComponent component = ch.getComponent();
		if (component != this) {
			return Collections.singleton(component);
		}
		return sourceComponents(processed, ch.sources());
	}

	/**
	 * All components related to the {@link #modelChannel()}.
	 * 
	 * <p>
	 * A component is related to this component, if it may affect the currently displayed model of
	 * this component.
	 * </p>
	 */
	public Set<LayoutComponent> getModelRelated() {
		return related(modelChannel());
	}

	/**
	 * All components related to the channel with the given name.
	 *
	 * <p>
	 * A component is related a certain channel, if it may affect the value of this channel.
	 * </p>
	 * 
	 * @param channelName
	 *        Name of a channel of this component.
	 * @return All related components.
	 * 
	 * @see #getRelatedChannels(String)
	 */
	public Set<LayoutComponent> getRelated(String channelName) {
		ComponentChannel channel = getChannelOrNull(channelName);
		if (channel == null) {
			return Collections.emptySet();
		}
		return related(channel);
	}

	/**
	 * The reflexive-transitive hull of all components that affect the given
	 * {@link ComponentChannel}.
	 *
	 * @param channel
	 *        The affected channel.
	 * @return All components whose channels are somehow linked to the given channel.
	 */
	protected static Set<LayoutComponent> related(ComponentChannel channel) {
		HashSet<ComponentChannel> result = new HashSet<>();
		fillRelated(channel, result);
		return result.stream().map(ch -> ch.getComponent()).collect(Collectors.toSet());
	}

	/**
	 * The reflexive-transitive hull of all channels that affect the channel with eth given name.
	 *
	 * @param channelName
	 *        The affected channel.
	 * @return All {@link ComponentChannel} that are somehow linked to the given channel.
	 * 
	 * @see #getRelated(String)
	 */
	public Set<ComponentChannel> getRelatedChannels(String channelName) {
		ComponentChannel channel = getChannelOrNull(channelName);
		if (channel == null) {
			return Collections.emptySet();
		}
		HashSet<ComponentChannel> result = new HashSet<>();
		fillRelated(channel, result);
		return result;
	}

	private static void fillRelated(ComponentChannel ch, Set<ComponentChannel> result) {
		if (!result.add(ch)) {
			return;
		}
		fillSources(ch, result);
		fillDestinations(ch, result);
	}

	private static void fillDestinations(ComponentChannel ch, Set<ComponentChannel> result) {
		for (ComponentChannel dest : ch.destinations()) {
			fillRelated(dest, result);
		}
	}

	private static void fillSources(ComponentChannel ch, Set<ComponentChannel> result) {
		for (ComponentChannel source : ch.sources()) {
			fillRelated(source, result);
		}
	}

	/**
	 * All {@link LayoutComponent}s that receive their model from this component.
	 * 
	 * <p>
	 * Opposite direction of {@link #getMasters()}.
	 * </p>
	 * 
	 * @see #getMasters()
	 */
	public Set<? extends LayoutComponent> getSlaves() {
		return collectModelDestinations(new HashSet<>(), new HashSet<>(), getChannels());
	}

	private Set<? extends LayoutComponent> collectModelDestinations(Set<ComponentChannel> processed,
			Set<LayoutComponent> result, Collection<ComponentChannel> channels) {
		for (ComponentChannel ch : channels) {
			if (!processed.add(ch)) {
				// channel already processed.
				continue;
			}
			for (ComponentChannel destination : ch.destinations()) {
				LayoutComponent destComponent = destination.getComponent();
				if (destComponent != this && destination == destComponent.modelChannel()) {
					result.add(destComponent);
				} else {
					collectModelDestinations(processed, result, destination.destinations());
				}
			}
		}
		return result;
	}

	/** 
     * Return our Master in case it is a Selectable.
     */
	public final Selectable getSelectableMaster() {
        LayoutComponent myMaster = getMaster();
        if (myMaster instanceof Selectable) {
            return ((Selectable) myMaster);
        }
        return null;
    }

    /**
     * Return our Selectable Masters as Collection
     *
     */
	public Collection<? extends LayoutComponent> getSelectableMasters() {
		return modelChannel().sources().stream().map(ch -> ch.getComponent()).filter(c -> c instanceof Selectable)
			.collect(Collectors.toList());
    }

	/**
	 * Write the stylesheet for my theme.
	 *
	 * @param    aPath    The current context path.
	 * @param    anOut    The writer to write the stylesheet to.
	 */
	protected void writeStyles(String aPath, TagWriter anOut) throws IOException {
        this.getTheme().writeStyles(aPath, anOut);
	}

    /**
     * Add js that this component has to execute in the onscroll method.
     *
     * @param aJsCommand   snipplet of js to add to js to be executed, should contain ';'
     */
    public void addOnscroll(String aJsCommand) {
        if (this.executeOnscroll == null) {
            this.executeOnscroll = aJsCommand;
        } else {
            this.executeOnscroll += '\n' + aJsCommand;
        }
    }

	/**
	 * @see Config#getEffectiveTitleKey(Config)
	 */
	public final ResKey getTitleKey() {
		return Config.getEffectiveTitleKey(getConfig());
	}

    /**
	 * Return the prefix to use for all translated elements.
	 */
	public final ResPrefix getResPrefix() {
		return Config.getEffectiveResPrefix(getConfig());
    }

    /**
     * Returns the I18Ned string for the given key, prefixed with the resource prefix.
     *
     * @param aKey
     *        the I18N lookup key end
     * @return the translated string for the key 'getResPrefix() + aKey'
     */
    public String getResString(String aKey) {
		return Resources.getInstance().getString(getResPrefix().key(aKey));
    }

    /**
	 * Returns the I18Ned string for the given key, prefixed with the resource prefix.
	 *
	 * @param aKey
	 *        the I18N lookup key end
	 * @return the translated string for the key 'getResPrefix() + aKey'
	 * 
	 * @deprecated Either use {@link #getResString(String)}, or
	 *             {@link #getResMessage(String, Object...)}.
	 */
	@Deprecated
    public String getResMessage(String aKey) {
		return Resources.getInstance().decodeMessageFromKeyWithEncodedArguments(getResPrefix().key(aKey));
    }

    /**
     * Returns the I18Ned string for the given key, prefixed with the resource
     * prefix. Additionally, the specified parameters are resolved and injected
     * appropriately.
     * 
     * @param key
     *            the I18N lookup key suffix
     * @param params
     *            the parameters to be resolved and injected
     * @return the translated string for the key 'getResPrefix() + key' along
     *         with all the resolved parameters
     */
    public String getResMessage(final String key, final Object... params) {
		return Resources.getInstance().getMessage(getResPrefix().key(key), params);
    }

	/**
	 * Execute the command (Commandhandler). May be overriden by sub classes to do some extra stuff
	 * besides the command handler's actions.
	 * 
	 * @param aContext
	 *        - the display context
	 * @param aCommand
	 *        - the registered and triggered command handler
	 * @param someArguments
	 *        - argument's map
	 */
    public HandlerResult dispatchCommand(DisplayContext aContext,
                                         CommandHandler aCommand,
                                         Map<String, Object> someArguments) {
		return CommandHandlerUtil.handleCommand(aCommand, aContext, this, someArguments);
    }

    /**
     * All {@link #registerCommand(CommandHandler) registered} commands.
     */
	public Collection<? extends CommandHandler> getCommands() {
        if (hasCommands()) {
            return commandsById.values();
        }
        return null;
    }

	/**
	 * See: {@link Config#getDefaultAction()}
	 * <p>
	 * Classes overriding this method have to take care themselves of registering it as command or
	 * button.
	 * </p>
	 */
	public CommandHandler getDefaultCommand() {
		return getConfiguredDefaultCommand();
	}

	/** The result is guaranteed to be constant, i.e. always the configured object. */
	private CommandHandler getConfiguredDefaultCommand() {
		return _defaultCommand;
	}

	/**
	 * See: {@link Config#getCancelAction()}
	 * <p>
	 * Classes overriding this method have to take care themselves of registering it as command or
	 * button.
	 * </p>
	 */
	public CommandHandler getCancelCommand() {
		return getConfiguredCancelCommand();
	}

	/** The result is guaranteed to be constant, i.e. always the configured object. */
	private CommandHandler getConfiguredCancelCommand() {
		return _cancelCommand;
	}

	/**
	 * Whether {@link #getCommands()} is non-empty.
	 */
	public boolean hasCommands() {
		return commandsById != null && (! commandsById.isEmpty());
	}
	
	/**
	 * Removes all registered {@link #getCommands() commands}.
	 */
	protected void clearCommands() {
		if (hasCommands()) {
			if (_buttonsByCliqueLazy != null) {
				_buttonsByCliqueLazy.clear();
			}
			commandsById.clear();
		}
	}

    /**
     * Return a command parameter value from the given argument map
     */
    public static String getParameter(Map someArguments, String aParameterName) {
    	Object result = someArguments.get(aParameterName);
    	
    	if ((result instanceof String) && AbstractCommandHandler.OBJECT_ID.equals(aParameterName)) {  
    		result = ReferenceManager.getSessionInstance().getSource((String) result);  
    	}  
    	
    	// Note: valueOf(null) == "null"!
    	if (result == null) {
    		return null;
    	}
		return String.valueOf(result);
    }

    /**
     * Get the prefixed name of a command parameter.
     * This methods mangles the command parameter name to avoid conficts with other parameters.
     *
     * @param  aCommandParameterName  the name of the command parameter
     *         as declared in the command handler.
     * @return a prefixed command parameter name
     *         (hopefully unique within the parameters used in the page
     *         used for the component).
     * @deprecated command parameters are not prefixed anymore
     */
    public static String getPrefixedCommandParameterName(String aCommandParameterName) {
        return COMMAND_PREFIX + aCommandParameterName;
    }

    /**
     * Registers the given command.
     *
	 * @return true if the registration of the command has been successful, otherwise false.
     */
	public boolean registerCommand(CommandHandler newCommand) {
        if (commandsById == null) {
			commandsById = new LinkedHashMap<>();
        }
		CommandHandler previousCommand = this.commandsById.put(newCommand.getID(), newCommand);
        boolean hasChanged = newCommand != previousCommand;
		if (hasChanged && previousCommand != null) {
			// An old command was removed.
			unregisterButtonCommand(previousCommand);
        }
		return hasChanged;
    }

    /**
	 * Override to register commands which could not configured via
	 * {@link Config#modifyIntrinsicCommands(CommandRegistry)}.
	 * 
	 * @see Config#modifyIntrinsicCommands(CommandRegistry)
	 */
    protected void registerAdditionalNonConfigurableCommands() {
		if (this.openedAsDialog()) {
            registerDialogCloseCommand();
        }
    }

	/**
	 * Register #getDialogCloseCommand via {@link #registerButtonCommand(CommandHandler)}.
	 * 
	 * You may wish to override this to register the command not as Button, but as simple command
	 */
    protected void registerDialogCloseCommand() {
		String closeHandlerName = _config.getCloseHandlerName();
		if (closeHandlerName.isEmpty()) {
			closeHandlerName = getDefaultCloseDialogHandlerName();
		}
		if (!StringServices.isEmpty(closeHandlerName)) {
			this.registerCommandHandler(closeHandlerName, getButtonComponent() != null);
		}
    }

	/**
	 * Remove a command from the component
	 * 
	 * @param commandId
	 *        the ID of the command to remove
	 */
	public final void unregisterCommand(String commandId) {
		unregisterCommand(getCommandById(commandId));
	}

	/**
	 * Remove a command from the component
	 * 
	 * @param command
	 *        the command to remove
	 */
	private void unregisterCommand(CommandHandler command) {
		if (command != null) {
			unregisterButtonCommand(command);
			commandsById.remove(command.getID());
    	}
	}

	/**
	 * Removes the given button without {@link #unregisterCommand(String) unregistering} the command
	 * as such.
	 * 
	 * @param command
	 *        the visible command to unregister.
	 */
	protected void unregisterButtonCommand(CommandHandler command) {
		if (_buttonsByCliqueLazy != null) {
			List<CommandHandler> buttons = _buttonsByCliqueLazy.get(command.getClique());
			if (buttons != null) {
				buttons.remove(command);
			}
		}
	}

    /**
     * Register commands to allow opening of all dialogs.
     * If the open to component does not prevent it, also buttons are created.
     */
	private void registerDialogOpeners(InstantiationContext context) {
		Config config = getConfig();
		for (Config dialog : config.getDialogs()) {
			dialog = LayoutUtils.resolveComponentReference(context, dialog);
			if (dialog == null) {
				continue;
			}
			PolymorphicConfiguration<? extends CommandHandler> openhandler =
				OpenModalDialogCommandHandler.createDialogOpenHandler(context, config, dialog);
			if (openhandler == null) {
				continue;
			}
			CommandHandler command = CommandHandlerFactory.getInstance().getCommand(context, openhandler);
			registerCommandHandler(command, dialog.getDialogInfo().getCreateOpenerButtons());
		}
    }

	/**
	 * Register a command which is registered in the {@link CommandHandlerFactory}.
	 *
	 * <p>
	 * If the named command is not found, an error is logged.
	 * </p>
	 *
	 * @param aKey
	 *        The key of the command in the {@link CommandHandlerFactory}. If the key is
	 *        <code>null</code> or empty, nothing happens.
	 * @param asButton
	 *        Whether the the command is to be registered as a visible button.
	 */
	protected void registerCommandHandler(String aKey, boolean asButton) {
		if (StringServices.isEmpty(aKey)) {
			// Allow calling with null or empty without any effect. This happens when removing a
			// command in the configuration (configuring the handler name to empty).
			return;
		}

        CommandHandler aHandler = handler(aKey);
        if (aHandler == null) {
			Logger.warn("No handler found for ID '" + aKey + "' to register in component '" + getName() + ".",
				LayoutComponent.class);
			return;
        }

		registerCommandHandler(aHandler, asButton);
    }

	/**
	 * Registers the given command handler at this component.
	 * 
	 * @param handler
	 *        The {@link CommandHandler} to register.
	 * @param asButtonCommand
	 *        indicates that the command is to be registered as button command
	 * @return Whether the given {@link CommandHandler} was not registered before.
	 */
	protected final boolean registerCommandHandler(CommandHandler handler, boolean asButtonCommand) {
		if (asButtonCommand) {
			return registerButtonCommand(handler);
		} else {
			return registerCommand(handler);
		}
	}

    /**
     * Register a ButtonCommand which is configured via the CommandHandlerFactory.
     *
     * There may be no command configured in the current context. In this case
     * the method has no effect. The command will result in a Button.
     *
     * @param    aKey   The key of the command in the CommandHandelFactory
     *                  config, must not be <code>null</code>.
     */
	protected void registerCommandHandler(String aKey) {
		registerCommandHandler(aKey, /* asButtonCommand */ true);
    }

	private CommandHandler handler(String aKey) {
		// The command could be locally declared in the component's layout XML.
		CommandHandler localHandler = getCommandById(aKey);
		if (localHandler != null) {
			return localHandler;
		}
		return CommandHandlerFactory.getInstance().getHandler(aKey);
	}

	@Override
	public final CommandHandler getCommandById(String anID) {
    	if (commandsById == null) {
    		return null;
    	}
		return commandsById.get(anID);
    }

    /**
	 * Write tags to include some javascript.
	 * 
	 * @param contextPath
	 *            The context path of the application.
	 * @param out
	 *            The writer to use for writing.
	 * @param aRequest
	 *            The processed request.
	 * @throws IOException
	 *             If writing fails.
	 */
	protected void writeJSTags(String contextPath, TagWriter out, HttpServletRequest aRequest) throws IOException {
		JSFileCompiler.getInstance().writeJavascriptRef(out, contextPath);
		if (saveScrollPosition) {
			HTMLUtil.writeJavascriptRef(out, contextPath, SaveScrollPosition.SCRIPT_SAVE_SCROLL_POSITION);
		}
	}

    /**
     * Register current button components to the given button component.
     *
     * Override this method in your subclasses for special behavior,
     * e.g. adding buttons and not replacing buttons.
     */
	private final void registerButtons() {
		ButtonComponent buttonComponent = getButtonComponent();
		if (buttonComponent != null) {
			List<? extends CommandModel> buttons = createButtonCommandModels();
			buttonComponent.addTransientButtons(buttons);
		}
	}

    /**
     * Create {@link CommandModel}s from buttonCommands.
     *
     * @return a List of {@link CommandModel}s for the {@link ButtonComponent}, may be null
     */
	final protected List<? extends CommandModel> createButtonCommandModels() {
		Map<String, List<CommandHandler>> buttonsByClique = buttonsByClique();
		if (buttonsByClique.isEmpty()) {
			return Collections.emptyList();
        }

		List<CommandModel> result = new ArrayList<>();

		final CommandHandlerFactory factory = CommandHandlerFactory.getInstance();
		Comparator<CommandHandler> commandOrder = factory.getCommandOrder();
		Resources res = Resources.getInstance();

		List<Entry<String, List<CommandHandler>>> cliqueEntries = new ArrayList<>(buttonsByClique.entrySet());
		Collections.sort(cliqueEntries, factory.getCliqueButtonBarOrder());
		for (Entry<String, List<CommandHandler>> entry : cliqueEntries) {
			String clique = entry.getKey();
			String cliqueGroup = factory.getCliqueGroup(clique);
			CommandHandler.Display display = factory.getDisplay(cliqueGroup);
			if (display == CommandHandler.Display.COMMANDS) {
				List<CommandHandler> commands = entry.getValue();
				Collections.sort(commands, commandOrder);
				for (CommandHandler command : commands) {
					result.add(createCommandModel(res, command, CommandHandler.NO_ARGS));
				}
			}
		}

        return result;
    }

	/**
	 * Creates a {@link CommandModel} for a {@link CommandHandler} invocation in the context of this
	 * component.
	 * 
	 * @param res
	 *        The current {@link Resources}.
	 * @param command
	 *        The command to execute.
	 * @param arguments
	 *        The arguments to the command, see
	 *        {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @return The {@link CommandModel} that can be displayed through a {@link ButtonControl}.
	 */
	public final CommandModel createCommandModel(Resources res, CommandHandler command, Map<String, Object> arguments) {
		// TODO KHA refactor this hack to use an interface ...
		LayoutComponent theTarget = this;
		if (command instanceof CommandChain) {
			theTarget = ((CommandChain) command).getTarget();
			if (theTarget == null) {
				theTarget = this;
			}
		}
		return modelForCommand(command, arguments, theTarget);
	}

	/**
	 * Short-cut for {@link #modelForCommand(CommandHandler, Map, LayoutComponent)} with
	 * {@link CommandHandler#NO_ARGS}.
	 */
	protected final CommandModel modelForCommand(CommandHandler command, LayoutComponent targetComponent) {
		return modelForCommand(command, CommandHandler.NO_ARGS, targetComponent);
	}

	/**
	 * Create a {@link CommandModel} for the given {@link CommandHandler} for being displayed in the
	 * {@link ButtonComponent}.
	 * @param command
	 *        The Command to create a CommandModel for.
	 * @param targetComponent
	 *        the {@link LayoutComponent} to execute the command.
	 * 
	 * @return A {@link CommandModel} to be displayed in the {@link ButtonComponent}.
	 */
	protected CommandModel modelForCommand(CommandHandler command, Map<String, Object> arguments, LayoutComponent targetComponent) {
		return command.createCommandModel(targetComponent, arguments);
	}

	/**
	 * Registers the given command and registers it as automatically displayed button.
	 * 
	 * @param command
	 *        the command to register.
	 * @return Whether the given command was not already registered at this component.
	 */
	public boolean registerButtonCommand(CommandHandler command) {
    	boolean newlyAdded = registerCommand(command);
    	
		if (! newlyAdded) {
        	// Remove the potentially already registered button to be able to
			// move buttons.
			unregisterButtonCommand(command);
		}
		
		if (_buttonsByCliqueLazy == null) {
			_buttonsByCliqueLazy = new HashMap<>();
        }
        
		String clique = command.getClique();
		List<CommandHandler> commands = _buttonsByCliqueLazy.get(clique);
		if (commands == null) {
			commands = new ArrayList<>();
			_buttonsByCliqueLazy.put(clique, commands);
		}
		commands.add(command);

        return newlyAdded;
    }

	/**
     * Reload the Buttons (ususally when Button state has changed).
     *
     * This is done by invalidating the ComponentProxy.
     */
    public void invalidateButtons() {
		if (_buttons != null) {
			_buttons.invalidate();
        }
    }

    /**
     * Write a JScript function to submit the form for the given command.
     *
     * @param    aPath       The context path of the application.
     * @param    aRes        The resources for resolving I18N tags.
     * @param    anOut       The writer to write to.
     * @param    aCommand    The command to write the script for.
     */
	private final void writeCommandScript(String aPath, Resources aRes, PrintWriter anOut, CommandHandler aCommand) {
    	aCommand.getCommandScriptWriter(this).writeCommandScript(aPath, aRes, anOut, this, aCommand);
    }

    /**
     * Getter for ButtonComponent identified by group name.
     *
     * @return null if not found
     */
    public ButtonComponent getButtonComponent() {
		return _buttons;
    }

    /**
     * Allow direct setting of the ButtonComponent.
     */
	public void setButtonComponent(ButtonComponent aComponent) {
		if (_buttons != null && aComponent != null && aComponent != _buttons) {
			throw new ConfigurationError("Inconsistent button component '" + _buttons.getName() + "' vs. '"
				+ aComponent.getName() + "' in '" + _config.location() + "'.");
		}
		_buttons = aComponent;
    }

	/**
	 * This method writes some js without a function around it. It ensures to execute the js without
	 * having a body-tag with a onload handler. This is nessesary to trigger js on bodyless sites as
	 * framesets.
	 */
	protected void writeFunctionLessJs(String aContext, TagWriter anOut, HttpServletRequest aRequest)
			throws IOException {
	}

	/**
	 * Is called in case the Component is becoming visible.
	 * 
	 * Use it to prepare your internal state for soon to happen rendering (e.g. create the list for
	 * a table). You <strong>must</strong> call super.becomingVisible();
	 * 
	 * Use it to fire model events to other components here, during first initialization. e.g. init
	 * your model by creating it (don't take a model you got by a event.), which fires the proper
	 * events or fire the events by your self.
	 */
    protected void becomingVisible() {
		// Hook for subclasses.
    }

    /**
	 * Whether the scroll position is kept between re-draws.
	 */
	public final boolean saveScrollPosition() {
		return saveScrollPosition;
	}

	/**
     * Call this to reset the saved scroll position on next load.
     *
     * Can be useful when resetting the model,
     * or in case the length of your components output varies a lot,
     * so there is no sense in scrolling back.
     */
    protected final void resetScrollPosition() {
		if (saveScrollPosition) {
			doResetScrollPosition(true);
		}
    }

    /**
     * Is called in case the Component is becoming invisible.
     *
     * You may release resources used for rendering only. But not these
     * representing your internal state. You <strong>must</strong>
     * class becomingInvisible();
     */
    protected void becomingInvisible() {
		if (getExpansionState() == ExpansionState.MAXIMIZED) {
			setExpansionState(ExpansionState.NORMALIZED);
		}

		List<? extends LayoutComponent> theDialogs = this.getDialogs();
		if (!theDialogs.isEmpty()) {
			DialogSupport dialogSupport = getDialogSupport();
            int    size      = theDialogs.size();
			if (dialogSupport != null) {
				for (int i = 0; i < size; i++) {
					LayoutComponent theDialog = theDialogs.get(i);
					if (dialogSupport.isDialogOpened(theDialog)) {
						theDialog.closeDialog();
					}
				}
            }
        }
		boolean commandModelsDetached = detachCommandModels();
		_commandModelsToAttach = commandModelsDetached;
		doResetScrollPosition(true);

		if (_config.getResetInvisible()) {
			invalidate();
		}
    }

	/**
	 * Informs the component that there are command models to attach.
	 * 
	 * This method should be called by subclasses if {@link #attachCommandModels()} must be called
	 * due to change of inner state, e.g. if there is a new command model to attach.
	 * 
	 * @see #attachCommandModel(CommandModel)
	 * @see #attachCommandModels()
	 */
	protected final void setHasCommandModelToAttach() {
		_commandModelsToAttach = true;
	}

	/**
	 * Attaches the command models used by this component.
	 * 
	 * <p>
	 * Note: This method must not be called from the application code. It is designed to be
	 * overwritten when a subclass needs to attach own command models. Attaching must be done via
	 * {@link #attachCommandModel(CommandModel)}. This method is called in
	 * {@link #validateModel(DisplayContext)} when the model becomes visible.
	 * </p>
	 * 
	 * @return Whether a command model has been attached.
	 * 
	 * @see #attachCommandModel(CommandModel)
	 * @see #detachCommandModels()
	 */
	protected boolean attachCommandModels() {
		boolean attached = false;
		if (viewByName != null) {
			Collection<? extends HTMLFragment> values = viewByName.values();
			attached = attachCommandModels(values);
		}
		return attached;
	}

	private boolean attachCommandModels(Collection<? extends HTMLFragment> views) {
		boolean attached = false;
		for (HTMLFragment view : views) {
			if (view instanceof ButtonControl) {
				ButtonControl button = (ButtonControl) view;
				attachCommandModel(button.getModel());
				attached = true;
			}
			if (view instanceof CompositeControl) {
				boolean childrenAttached = attachCommandModels(((CompositeControl) view).getChildren());
				if (childrenAttached) {
					attached = true;
				}
			}
		}
		return attached;
	}

	/**
	 * Attaches the given {@link CommandModel}.
	 * 
	 * Must only be called from subclasses in {@link #attachCommandModels()}
	 * 
	 * @param model
	 *        The model to attach.
	 * 
	 * @see #attachCommandModels()
	 * @see #detachCommandModel(CommandModel)
	 */
	protected final void attachCommandModel(CommandModel model) {
		model.addListener(PropertyObservable.GLOBAL_LISTENER_TYPE, GenericPropertyListener.IGNORE_EVENTS);
	}

	/**
	 * Attaches the command models used by this component.
	 * 
	 * <p>
	 * Note: This method must not be called from the application code. It is designed to be
	 * overwritten when a subclass needs to attach own command models. Detaching must be done via
	 * {@link #detachCommandModel(CommandModel)}. This method is called when the component
	 * {@link #becomingInvisible() becomes invisible}.
	 * </p>
	 * 
	 * @return <code>true</code> if any {@link CommandModel} was detached.
	 * 
	 * @see #detachCommandModel(CommandModel)
	 * @see #attachCommandModels()
	 */
	protected boolean detachCommandModels() {
		if (viewByName != null) {
			Collection<HTMLFragment> views = viewByName.values();
			return detachCommandModels(views);
		}
		return false;
	}

	private boolean detachCommandModels(Collection<? extends HTMLFragment> views) {
		boolean detached = false;
		for (HTMLFragment view : views) {
			if (view instanceof ButtonControl) {
				ButtonControl button = (ButtonControl) view;
				detachCommandModel(button.getModel());
				detached = true;
			}
			if (view instanceof CompositeControl) {
				detachCommandModels(((CompositeControl) view).getChildren());
				detached = true;
			}
		}
		return detached;
	}

	/**
	 * Detaches the given {@link CommandModel}.
	 * 
	 * Must only be called from subclasses in {@link #detachCommandModels()}
	 * 
	 * @param model
	 *        The model former attached.
	 * 
	 * @see #detachCommandModels()
	 * @see #attachCommandModel(CommandModel)
	 */
	protected final void detachCommandModel(CommandModel model) {
		model.removeListener(PropertyObservable.GLOBAL_LISTENER_TYPE, GenericPropertyListener.IGNORE_EVENTS);
	}

	/**
	 * Notify registered listeners about the change of the visibility of this component.
	 * 
	 * @param becomingVisible
	 *        Whether this component is becoming visible or invisible.
	 */
	protected void fireVisibilityChange(boolean becomingVisible) {
		firePropertyChanged(VISIBILITY_EVENT, this, !becomingVisible, becomingVisible);
	}

	/**
	 * Fires the property change of the given type.
	 * 
	 * @param propertyType
	 *        The event type.
	 * @param oldValue
	 *        The old property value.
	 * @param newValue
	 *        the new property value.
	 */
	protected final <T extends PropertyListener, S, V> void firePropertyChanged(
			EventType<T, ? super S, V> propertyType, S sender, V oldValue, V newValue) {
		LayoutComponent receiver = this;
		while (receiver != null) {
			Bubble bubble = receiver.dispatchEvent(propertyType, sender, oldValue, newValue);
			if (!propertyType.canBubble() || bubble == Bubble.CANCEL_BUBBLE) {
				break;
			}
			receiver = receiver.getParent();
		}
	}

	private <T extends PropertyListener, S, V> Bubble dispatchEvent(EventType<T, ? super S, V> type, S sender,
			V oldValue, V newValue) {
		if (_listeners == null) {
			return Bubble.BUBBLE;
		}
		return _listeners.notify(type, sender, oldValue, newValue);
	}

	@Override
	public <L extends PropertyListener, S, V> boolean addListener(EventType<L, S, V> type, L listener) {
		if (_listeners == null) {
			_listeners = new PropertyListeners();
		}
		return _listeners.addListener(type, listener);
	}

	@Override
	public <L extends PropertyListener, S, V> boolean removeListener(EventType<L, S, V> type, L listener) {
		if (_listeners == null) {
			return false;
		}
		return _listeners.removeListener(type, listener);
	}

	/**
	 * Check if a listener for given event type was registered.
	 */
	protected final<T extends PropertyListener, S, V> boolean hasListeners(EventType<T, S, V> type) {
		if (_listeners == null) {
			return false;
		}
		return _listeners.has(type);
	}

	@Override
	public ExpansionState getExpansionState() {
		return nonNull(get(EXPANSION_STATE));
	}

	@Override
	public void setExpansionState(ExpansionState newState) {
		assert newState != null;

		ExpansionState oldState = computeOldState(newState);

		saveExpansionState(newState);

		firePropertyChanged(Expandable.STATE, this, nonNull(oldState), newState);
	}

	private ExpansionState computeOldState(ExpansionState newState) throws UnreachableAssertion {
		switch (newState) {
			case NORMALIZED:
				if (_initiallyMinimized) {
					return set(EXPANSION_STATE, newState);
				} else {
					return reset(EXPANSION_STATE);
				}
			case HIDDEN:
			case MAXIMIZED:
				return set(EXPANSION_STATE, newState);
			case MINIMIZED:
				if (_initiallyMinimized) {
					return reset(EXPANSION_STATE);
				} else {
					return set(EXPANSION_STATE, newState);
				}
		}
		throw ExpansionState.noSuchExpansionState(newState);
	}

	private void saveExpansionState(ExpansionState state) {
		PersonalizingExpandable.saveExpansionState(expansionPersonalizationKey(), state, _initiallyMinimized);
	}

	private void loadExpansionState() {
		loadExpansionState(expansionPersonalizationKey());
	}

	private void loadExpansionState(String personalizationKey) {
		if (_initiallyMinimized) {
			if (!PersonalizingExpandable.loadCollapsed(personalizationKey, _initiallyMinimized)) {
				set(EXPANSION_STATE, ExpansionState.NORMALIZED);
			}
		} else {
			if (PersonalizingExpandable.loadCollapsed(personalizationKey, _initiallyMinimized)) {
				set(EXPANSION_STATE, ExpansionState.MINIMIZED);
			}
		}
	}

	private String expansionPersonalizationKey() {
		return getName() + ".collapsedState";
	}

	private ExpansionState nonNull(ExpansionState state) {
		if (state == null) {
			return defaultState();
		}
		return state;
	}

	private ExpansionState defaultState() {
		if (_initiallyMinimized) {
			return ExpansionState.MINIMIZED;
		} else {
			return ExpansionState.NORMALIZED;
		}
	}

	/**
	 * Adds a {@link GenericPropertyListener} to this component.
	 * 
	 * @param listener
	 *        The listener to add.
	 * @return Whether the given listener was not already registered.
	 */
	public final boolean addPropertyListener(GenericPropertyListener listener) {
		return this.addListener(GLOBAL_LISTENER_TYPE, listener);
	}

	/**
	 * Whether the given listener was not registered.
	 * 
	 * @see #addPropertyListener(GenericPropertyListener)
	 */
	public final boolean removePropertyListener(GenericPropertyListener listener) {
		return this.removeListener(GLOBAL_LISTENER_TYPE, listener);
	}

     /**
	 * Get the key used for storing the scroll positions in the java script maps. See
	 * {@link #writeInOnload(String, TagWriter, HttpServletRequest)} for usage of the returned
	 * value.
	 * 
	 * @return a key used to store the scroll positions; must not be null, must be unique;
	 */
	private String getScrollPositionKey() {
		return this.getName().qualifiedName();
	}

	/**
	 * Whether the scroll position of this component shall be resetted at next rendering, or not.
	 */
	public boolean shallResetScrollPosition() {
		return _resetScrollPosition;
	}

	/**
	 * @see #shallResetScrollPosition()
	 */
	public void doResetScrollPosition(boolean resetScrollPosition) {
		_resetScrollPosition = resetScrollPosition;
	}

    /**
	 * Implementation of the the visitor pattern.
	 *
	 * <p>
	 * To learn more about the visitor pattern refer to e.g.
	 * <code>http://en.wikipedia.org/wiki/Visitor_pattern</code>
	 * </p>
	 *
	 * <p>
	 * This method must be overriden in every component sub-class of
	 * {@link LayoutComponent} that has its own typ-specific visit method
	 * declared in {@link LayoutComponentVisitor}.
	 * </p>
	 *
	 * @see #acceptVisitorRecursively(LayoutComponentVisitor) for descending
	 *      recursively through a component tree.
	 *
	 * @see DefaultDescendingLayoutVisitor for a base implementation to
	 *      sub-class a custom visitor from.
	 */
	public boolean acceptVisitor(LayoutComponentVisitor aVisitor) {
		return aVisitor.visitLayoutComponent(this);
	}

    /**
	 * Accept the given visitor, call
	 * {@link LayoutComponentVisitor#visitLayoutComponent(LayoutComponent) visit} on it, and
	 * (if the visitor's visit method returns <code>true</code>) recursively
	 * call this method on all children of this component.
	 *
	 * <p>
	 * Note: This method is final, because the only "customization option" that
	 * is compatible with the contract of this method is to vary the algorithm
	 * of determining the children of this component. The customization can be
	 * done be overriding
	 * {@link #visitChildrenRecursively(LayoutComponentVisitor)}.
	 * </p>
	 *
	 * @param aVisitor
	 *     The visitor that should visit the component tree.
	 */
    public final void acceptVisitorRecursively(LayoutComponentVisitor aVisitor) {
        boolean shouldDescend = this.acceptVisitor(aVisitor);

        if (shouldDescend) {
        	visitChildrenRecursively(aVisitor);
        }
    }

    /**
	 * Customization point for
	 * {@link #acceptVisitorRecursively(LayoutComponentVisitor)}.
	 *
	 * @see #acceptVisitorRecursively(LayoutComponentVisitor)
	 */
	public void visitChildrenRecursively(LayoutComponentVisitor aVisitor) {
		List<? extends LayoutComponent> theDialogs = getDialogs();
		if (!theDialogs.isEmpty()) {
            int size = theDialogs.size();
            for (int i = 0; i <size; i++) {
				LayoutComponent theChild = theDialogs.get(i);
                theChild.acceptVisitorRecursively(aVisitor);
            }
        }
	}

    /**
     * Set the dialog, which is currently opened and bound to this component.
     *
     * @param    aDialog    The dialog currently opened by this component, may be <code>null</code>.
     */
    public void setDialog(LayoutComponent aDialog) {
        this.currentDialog = aDialog;
    }

    /**
     * Get the dialog, which is currently opened and bound to this component.
     *
     * @return    The dialog currently opened, may be <code>null</code>.
     */
    public LayoutComponent getDialog() {
        return (this.currentDialog);
    }

	/**
	 * Appends all CSS classes for the document body to the given buffer.
	 * 
	 * <p>
	 * An (overriding) implementation must be aware that some predefined classes are already in the
	 * given buffer and sparate added classes by a space character.
	 * </p>
	 * 
	 * @param out
	 *        The buffer to append CSS classes to.
	 */
	protected void writeBodyCssClassesContent(TagWriter out) throws IOException {
		out.append(getConfig().getBodyClass());
    }

    /**
     * Provides the location where the XML source this component was read from can be found.
     */
    public String getLocation() {
		return _config.location().toString();
    }

    /**
	 * Whether this component can be rendered without any further processing.
	 */
    public boolean isModelValid() {
		return !_validationRequested;
    }

    /**
	 * Bring the model of this component into a consistent state that can directly be rendered
	 * without any further processing.
	 * 
	 * TODO #21901: Remove.
	 * 
	 * @return whether non-local changes might have occurred. This is always true, if events have
	 *         potentially been sent.
	 * 
	 * @deprecated Implement {@link #doValidateModel(DisplayContext)} instead. Take care since the
	 *             semantics of the return value have changed.
	 */
	@Deprecated
    public boolean validateModel(DisplayContext context) {
		boolean globalChanges = false;
		if (_isVisible() && _commandModelsToAttach) {
			boolean newleyAttached = attachCommandModels();
			_commandModelsToAttach = false;
			globalChanges = newleyAttached;
		}

		if (_validationRequested) {
			_validationRequested = false;
			handleNewModel(getModel());
			return true;
		} else {
			return globalChanges;
		}
    }

	/**
	 * Adds a {@link ValidationListener} to this component.
	 * 
	 * @see #removeValidationListener(ValidationListener)
	 */
	public boolean addValidationListener(ValidationListener listener) {
		if (_validationListeners == null) {
			_validationListeners = new ValidationListeners();
		}
		return _validationListeners.addListener(listener);
	}

	/**
	 * Removes the given {@link ValidationListener} from this component.
	 * 
	 * @see #addValidationListener(ValidationListener)
	 */
	public boolean removeValidationListener(ValidationListener listener) {
		if (_validationListeners == null) {
			return false;
		}
		return _validationListeners.removeListener(listener);
	}

	/**
	 * Writes a JS object containing the 'componentId' and the 'submitNumber' of this
	 * {@link LayoutComponent}.
	 */
    public final String getJSContextInformation() {
    	StringBuffer theContextInformation = new StringBuffer();
    	theContextInformation.append("{componentId:'");
    	theContextInformation.append(getName());
    	theContextInformation.append("', submitNumber: ");
    	theContextInformation.append(getSubmitNumber());
    	theContextInformation.append('}');
    	return theContextInformation.toString();
    }
    
    /**
	 * This method returns a {@link ControlScope} to register controls for this
	 * {@link LayoutComponent} if this component supports controls or <code>null</code> if no
	 * controls can be used in this component.
	 */
    public ControlScope getControlScope() {
    	return null;
    }
    
	@Override
	public ToolBar getToolBar() {
		return _contextToolbar;
	}

	/**
	 * Installs a {@link ToolBar} from the current context.
	 * 
	 * @param newValue
	 *        The {@link ToolBar} defined by the context. This may be used by the component to place
	 *        commands.
	 */
	@Override
	public final void setToolBar(ToolBar newValue) {
		ToolBar oldValue = _contextToolbar;
		if (newValue == oldValue) {
			return;
		}
		_contextToolbar = newValue;
		onSetToolBar(oldValue, newValue);
		dispatchEvent(ToolBarOwner.TOOLBAR_PROPERTY, this, oldValue, newValue);
	}

	/**
	 * Short-cut for registering a {@link ToolBarChangeListener} on this component.
	 * 
	 * @param oldValue
	 *        The previously registered {@link ToolBar}.
	 * @param newValue
	 *        The newly registered {@link ToolBar}.
	 */
	protected void onSetToolBar(ToolBar oldValue, ToolBar newValue) {
		Map<String, List<CommandHandler>> handlersByClique = buttonsByClique();

		// Handle configured toolbar groups.
		Set<Entry<String, List<CommandHandler>>> commandCliques = handlersByClique.entrySet();
		if (oldValue != null) {
			for (ToolBarGroupConfig groupConfig : getConfig().getToolbarGroups().values()) {
				removeCommandsFromToolbar(oldValue, groupConfig.getName());
			}

			CommandHandlerFactory factory = CommandHandlerFactory.getInstance();
			for (Entry<String, List<CommandHandler>> entry : commandCliques) {
				String clique = entry.getKey();
				String cliqueGroup = factory.getCliqueGroup(clique);
				CommandHandler.Display display = factory.getDisplay(cliqueGroup);
				if (display == CommandHandler.Display.TOOLBAR || display == CommandHandler.Display.MENU
					|| (display == CommandHandler.Display.COMMANDS && getButtonComponent() == null)) {
					removeCommandsFromToolbar(oldValue, cliqueGroup);
				}
			}
		}

		if (newValue != null) {
			for (ToolBarGroupConfig groupConfig : getConfig().getToolbarGroups().values()) {
				ToolBarGroup group = newValue.defineGroup(groupConfig.getName());
				for (ButtonConfig buttonConfig : groupConfig.getButtons()) {
					CommandModel button = ButtonConfig.Factory.createButton(buttonConfig, this);
					if (button != null) {
						button.set(COMMAND_MODEL_OWNER, getName());
						group.addButton(button);
					}
				}
			}

			Resources res = Resources.getInstance();
			CommandHandlerFactory factory = CommandHandlerFactory.getInstance();

			Comparator<CommandHandler> commandOrder = factory.getCommandOrder();
			List<Entry<String, List<CommandHandler>>> cliqueEntries = new ArrayList<>(commandCliques);
			Collections.sort(cliqueEntries, factory.getCliqueToolBarOrder());
			for (Entry<String, List<CommandHandler>> entry : cliqueEntries) {
				String clique = entry.getKey();
				String cliqueGroup = factory.getCliqueGroup(clique);
				CommandHandler.Display display = factory.getDisplay(cliqueGroup);
				if (display == CommandHandler.Display.TOOLBAR || display == CommandHandler.Display.MENU
						|| (display == CommandHandler.Display.COMMANDS && getButtonComponent() == null)) {
					List<CommandHandler> handlers = entry.getValue();
					Collections.sort(handlers, commandOrder);

					ToolBarGroup group = newValue.defineGroup(cliqueGroup);
					for (CommandHandler command : handlers) {
						CommandModel button = createCommandModel(res, command, CommandHandler.NO_ARGS);
						button.set(COMMAND_MODEL_OWNER, getName());
						group.addButton(button);
					}
				}
			}
		}
	}

	private void removeCommandsFromToolbar(ToolBar toolBar, String groupName) {
		ToolBarGroup group = toolBar.getGroup(groupName);
		if (group == null) {
			return;
		}
		List<HTMLFragment> views = group.getViews();
		boolean onlyOwnCommands = true;
		for (int index = 0, size = views.size(); index < size; index++) {
			if (!isOwnToolbarView(views.get(index))) {
				onlyOwnCommands = false;
				break;
			}
		}
		if (onlyOwnCommands) {
			// All commands belong to this component.
			toolBar.removeGroup(groupName);
		} else {
			// Remove only commands of this component.
			for (int index = views.size() - 1; index >= 0; index--) {
				if (isOwnToolbarView(views.get(index))) {
					group.removeView(index);
				}
			}
		}
	}

	private boolean isOwnToolbarView(HTMLFragment view) {
		return view instanceof AbstractButtonControl
			&& getName().equals(((AbstractButtonControl<?>) view).getModel().get(COMMAND_MODEL_OWNER));
	}

	private Map<String, List<CommandHandler>> buttonsByClique() {
		if (_buttonsByCliqueLazy == null) {
			return Collections.emptyMap();
		}
		return _buttonsByCliqueLazy;
	}

	/**
	 * The custom {@link LayoutControlProvider} to use for transforming this component into a
	 * {@link LayoutControl}, or <code>null</code> to use the default transformation.
	 */
	public final LayoutControlProvider getComponentControlProvider() {
		return getConfig().getActiveComponentControlProvider();
    }
    	
    /**
	 * This method returns a configured {@link View}.
	 * 
	 * @param aName
	 *            the name which was used to configure the {@link View}
	 * @return may be <code>null</code> if no {@link View} was configured under the given name.
	 */
	public HTMLFragment getViewByName(String aName) {
    	if (viewByName == null) {
    		return null;
    	}
    	else {
			return viewByName.get(aName);
    	}
    }

    /**
	 * This method returns a {@link Map} of configured views. The keys are the names given in the
	 * configuration.
	 */
	public Map<String, ? extends HTMLFragment> getConfiguredViews() {
    	if (viewByName == null) {
			return Collections.emptyMap();
    	}
    	return Collections.unmodifiableMap(viewByName);
    }
    
    /**
	 * This method evaluates the configured view and fills the {@link Map} given by
	 * {@link #getConfiguredViews()}.
	 */
	private void initConfiguredViews(InstantiationContext context) {
		List<ViewConfiguration.Config<?>> viewConfigs = _config.getViews();
		if (viewConfigs.isEmpty()) {
			return;
		}

		int size = viewConfigs.size();
		viewByName = MapUtilShared.newMap(size);
		for (int index = 0; index < size; index++) {
			ViewConfiguration currentConfiguration = context.getInstance(viewConfigs.get(index));
			HTMLFragment view = currentConfiguration.createView(this);
			Object formerlyAddedView = viewByName.put(currentConfiguration.getName(), view);
			if (formerlyAddedView != null) {
				Logger.warn("Added configured view '" + view + "' under name '"
					+ currentConfiguration.getName() + "' to component '" + this.getName()
					+ "' but there was already the view '"
					+ formerlyAddedView + "' added under the name!", this);
			}
		}
		setHasCommandModelToAttach();
	}

	/**
	 * Quirks base method only called from various special sub classes in their
	 * {@link #componentsResolved(InstantiationContext)} methods.
	 * 
	 * <p>
	 * TODO: Introduce general hook called from {@link #componentsResolved(InstantiationContext)}.
	 * </p>
	 * 
	 * @param context
	 *        Context for configuration instantiations.
	 */
    protected void initProgramaticDialogs(InstantiationContext context) {
    	// Ignore.
    }

	/**
	 * Returns the {@link FrameScope} to get informations about the client side representation of
	 * this business component.
	 * 
	 * @return never <code>null</code>
	 */
	public final LayoutComponentScope getEnclosingFrameScope() {
		// It may happen that the enclosing scope was not created in
		// componentsResolved, if some component (e.g. WindowTemplate) doesn't
		// call super.componentsResolved(DisplayContext)
		installEnclosingFrameScope();
		return this.scope;
	}

	/**
	 * checks whether {@link #scope} is <code>null</code> and sets it via
	 * {@link #createEnclosingFrameScope()} if necessary.
	 */
	private void installEnclosingFrameScope() {
		if (this.scope == null) {
	    	this.scope = createEnclosingFrameScope();
		}
	}

	/**
	 * Creates the {@link FrameScope} which represents that business component
	 * at client side.
	 * 
	 * @retun the {@link LayoutComponentScope} which will returned by
	 *        {@link #getEnclosingFrameScope()}
	 * @see #getEnclosingFrameScope()
	 */
	protected LayoutComponentScope createEnclosingFrameScope() {
		return new LayoutComponentScope(this);
	}

    @Override
	public WindowScope getWindowScope() {
    	return getEnclosingFrameScope().getWindowScope();
    }
    
	@Override
	public ModelScope getModelScope() {
		return getMainLayout().getModelScope();
	}

	@Override
	public Collection<? extends ContentHandler> inspectSubHandlers() {
		return getEnclosingFrameScope().inspectSubHandlers();
	}

    /**
     * Display a dialog with a message that informs the user about the current selection being deleted.
     */
    protected final void showErrorSelectedObjectDeleted() {
		HandlerResult error = new HandlerResult();
		error.setException(LayoutUtils.createErrorSelectedObjectDeleted());
		openErrorDialog(error);
	}

    /**
	 * Utility to show an error dialog from a context that does not return a {@link HandlerResult}.
	 */
	protected void openErrorDialog(HandlerResult error) {
		for (ClientAction action : ErrorHandlingHelper.transformHandlerResult(getWindowScope(), error)) {
			getEnclosingFrameScope().addClientAction(action);
		}
	}

    @Override
	@Deprecated
    public final FrameScope getEnclosingScope() {
    	return getEnclosingFrameScope().getEnclosingScope();
    }
    
    @Override
	@Deprecated
    public final void addClientAction(ClientAction update) {
    	getEnclosingFrameScope().addClientAction(update);
    }
    
    @Override
	@Deprecated
    public final <T extends Appendable> T appendClientReference(T out) throws IOException {
    	return getEnclosingFrameScope().appendClientReference(out);
    }
    
    @Override
	@Deprecated
    public final String createNewID() {
    	return getEnclosingFrameScope().createNewID();
    }
    
    @Override
	@Deprecated
    public final void registerContentHandler(String id, ContentHandler handler) {
    	getEnclosingFrameScope().registerContentHandler(id, handler);
    }
    
    @Override
	@Deprecated
    public final boolean deregisterContentHandler(ContentHandler handler) {
    	return getEnclosingFrameScope().deregisterContentHandler(handler);
    }
    
    @Override
	@Deprecated
    public final void handleContent(DisplayContext context, String id, URLParser url) throws IOException {
		getEnclosingFrameScope().handleContent(context, id, url);
    }
    
    @Override
	@Deprecated
	public final URLBuilder getURL(DisplayContext context) {
		return getComponentURL(context);
	}

	@Override
	@Deprecated
    public final URLBuilder getURL(DisplayContext context, ContentHandler handler) {
    	return getEnclosingFrameScope().getURL(context, handler);
    }
    
    @Override
	@Deprecated
    public void addCommandListener(CommandListener listener) {
    	getEnclosingFrameScope().addCommandListener(listener);
    }
    
    @Override
	@Deprecated
    public boolean removeCommandListener(CommandListener listener) {
    	return getEnclosingFrameScope().removeCommandListener(listener);
    }
    
    @Override
	@Deprecated
    public void clear() {
    	getEnclosingFrameScope().clear();
    }
    
    @Override
	@Deprecated
    public CommandListener getCommandListener(String id) {
    	return getEnclosingFrameScope().getCommandListener(id);
    }

	@Override
	@Deprecated
	public Collection<CommandListener> getCommandListener() {
		return getEnclosingFrameScope().getCommandListener();
	}

	@Override
	public InlineMap<Property<?>, Object> internalAccessLazyPropertiesStore() {
		return _properties;
	}

	@Override
	public void internalUpdateLazyPropertiesStore(InlineMap<Property<?>, Object> newProperties) {
		_properties = newProperties;
	}

	/**
	 * Returns the support for opening and closing a {@link LayoutComponent} as dialog.
	 * 
	 * @return <code>null</code> if and only if the {@link MainLayout} is not yet resolved.
	 * 
	 * @see #componentsResolved(InstantiationContext)
	 */
	public DialogSupport getDialogSupport() {
		return getWindow().getDialogSupport();
	}

	/**
	 * The enclosing {@link WindowComponent} if it is an external window, otherwise the
	 * {@link MainLayout}.
	 */
	public LayoutComponent getWindow() {
		return _window;
	}

	/**
	 * Entry point of the model validation.
	 * 
	 * @see ModelValidator
	 * 
	 * @param context
	 *        The validation context.
	 * @return Whether it is safe to assume that no further validation iteration must be performed.
	 *         It is safe to return <code>true</code>, if no validation actions were necessary (the
	 *         component was already valid before the call). It is safe to return
	 *         <code>false</code>, if some validation actions were required (the component was not
	 *         valid before the call). It is safe to return <code>true</code>, if some validation
	 *         was necessary. Returning <code>false</code> causes the validation fixpoint iteration
	 *         to continue. Therefore, returning <code>false</code> unconditionally, causes an
	 *         endless validation.
	 */
	final boolean performValidation(DisplayContext context) {
		return doValidateModel(context);
	}

	/**
	 * Validates the UI model of this component.
	 * 
	 * <p>
	 * This hook is called for all visible components in a fixpoint iteration, until all components
	 * return <code>true</code>, see below.
	 * </p>
	 * 
	 * @param context
	 *        The validation context.
	 * @return Whether it is safe to assume that no further validation iteration must be performed.
	 *         It is safe to return <code>true</code>, if no validation actions were necessary (the
	 *         component was already valid before the call). It is safe to return
	 *         <code>false</code>, if some validation actions were required (the component was not
	 *         valid before the call). Returning <code>false</code> causes the validation fixpoint
	 *         iteration to continue. Therefore, returning <code>false</code> unconditionally,
	 *         causes an endless validation.
	 */
	protected boolean doValidateModel(DisplayContext context) {
		boolean alreadyValid = isModelValid();
		if (!alreadyValid) {
			validateModel(context);
		}
		if (_validationListeners != null) {
			_validationListeners.doValidateModel(context, this);
		}
		return alreadyValid;
	}

	/**
	 * Internal setup of channel linkage.
	 * 
	 * @param log
	 *        Log to write messages and problems to.
	 * @see #linkChannels(Log)
	 */
	@Override
	public void linkChannels(Log log) {
		Config config = getConfig();

		ComponentChannel modelChannel = getChannelOrNull(ModelChannel.NAME);
		if (modelChannel != null) {
			modelChannel.addListener(ON_MODEL_SET);

			ModelSpec modelSpec = config.getModelSpec();
			if (modelSpec != null) {
				ChannelLinking channelLinking = getChannelLinking(modelSpec);
				modelChannel.linkChannel(log, this, channelLinking);
			}
		}

		if (this instanceof Selectable) {
			((Selectable) this).linkSelectionChannel(log);
		}
	}

	@Override
	public final void closeDialog() {
		LayoutComponent dialog = getDialogTopLayout();
		LayoutComponent opener = getDialogParent();
		DialogSupport dialogSupport = opener.getDialogSupport();
		if (!dialogSupport.isDialogOpened(dialog)) {
			return;
		}

		opener.setDialog(null);
		dialogSupport.deregisterOpenedDialog(dialog);

		// Needed to refresh button component in case the dialog was aborted
		ButtonComponent buttons = opener.getButtonComponent();
		if (buttons != null) {
			buttons.invalidate();
		}

		dialog.setVisible(false);
		dialog.invalidate();
		// Notify Opener, too (will be suppressed by default Event logic)
		opener.receiveDialogEvent(this, opener, /* isOpen */ false);
		// Notify GUI of Dialog closing
		opener.doFireModelEvent(this, opener, ModelEventListener.DIALOG_CLOSED);
	}

	/**
	 * Returns a {@link ResKey} to display when this component has no model.
	 * 
	 * @return A {@link ResKey} to display when this component has no model.
	 */
	public ResKey noModelKey() {
		return ResKey.fallback(getConfig().getNoModelKey(),
			ResKey.fallback(getResPrefix().key(FormTag.DEFAULT_NO_MODEL_KEY_SUFFIX),
				I18NConstants.NO_MODEL));
	}

	/**
	 * Calls {@link #resolveComponent(InstantiationContext)} on each component.
	 */
	protected static void resolveComponents(InstantiationContext context,
			Iterable<? extends LayoutComponent> components) {
		components.forEach(component -> LayoutComponent.resolveComponent(context, component));
	}

	/**
	 * Calls {@link #resolveComponent(InstantiationContext)} on the given component.
	 */
	protected static void resolveComponent(InstantiationContext context, LayoutComponent component) {
		component.resolveComponent(context);
	}

}
