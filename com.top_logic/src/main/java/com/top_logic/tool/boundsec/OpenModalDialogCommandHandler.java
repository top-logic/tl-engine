/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandScriptWriter;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.component.title.TitleProvider;
import com.top_logic.layout.editor.AllChannelNames;
import com.top_logic.layout.editor.AllDialogNames;
import com.top_logic.layout.editor.RelativeComponentNames;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.ComponentNameLabelProvider;
import com.top_logic.mig.html.layout.AbstractWindowInfo;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.DialogInfo;
import com.top_logic.mig.html.layout.DialogSupport;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.ModelEventListener;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.InViewModeExecutable;
import com.top_logic.util.Resources;

/**
 * {@link CommandHandler} to open a dialog.
 * 
 * <p>
 * When defining a dialog, an open handler is also defined to create a button for opening that
 * dialog. Outside a dialog definition, this handler can also be used to open a dialog defined
 * somewhere else.
 * </p>
 */
@InApp
@Label("Open dialog")
public class OpenModalDialogCommandHandler extends AbstractCommandHandler implements WithPostCreateActions {

	private static final String OPEN_AS_DIALOG_SUFFIX = "openAsDialog";

	private static final String DIALOG_TITLE_SUFFIX = ".title";

	/**
	 * Config interface for {@link OpenModalDialogCommandHandler}.
	 * 
	 * @implNote This interface type is used as trigger for the layout editor when offering
	 *           component channels of a configured component. When a dialog opener is configured
	 *           (in the context of a dialog itself), the context component that is used e.g. to
	 *           look up the component channels must be the component, where the dialog is defined.
	 *           See {@link AllChannelNames}.
	 */
	public interface Config extends AbstractCommandHandler.Config, WithPostCreateActions.Config {

		/**
		 * @see #getDialogName()
		 */
		String DIALOG_NAME_PROPERTY = "dialog-name";

		/**
		 * @see #getTargetComponent()
		 */
		String TARGET_COMPONENT = "targetComponent";

		/**
		 * Name of the component to open as dialog.
		 * 
		 * <p>
		 * If not given, a dynamic default is applied (the name of the context component of the
		 * {@link DialogInfo} this handler is defined in, see {@link DialogInfo#getOpenHandler()}.
		 * </p>
		 */
		@Name(DIALOG_NAME_PROPERTY)
		@Options(fun = AllDialogNames.class)
		@OptionLabels(ComponentNameLabelProvider.class)
		ComponentName getDialogName();

		/**
		 * @see #getDialogName()
		 */
		void setDialogName(ComponentName value);

		/**
		 * Name of the {@link LayoutComponent} in the opened dialog that should receive the
		 * {@link #getTarget() command's target model}.
		 * 
		 * <p>
		 * This setting is usable to populate the opened dialog with a model object taken e.g. from
		 * the the component that opens the dialog (or any other component as specified as the
		 * {@link #getTarget() command's target model source}).
		 * </p>
		 * 
		 * <p>
		 * Note: The visibility of the dialog open button is implicitly affected by this setting.
		 * For the opener button to be visible, the target component must
		 * {@link LayoutComponent#supportsModel(Object) support} the model transferred in the
		 * opening process.
		 * </p>
		 * 
		 * <p>
		 * The setting is optional. If not specified, no model is transfered in the opening process.
		 * </p>
		 */
		@Name(TARGET_COMPONENT)
		@Label("model component")
		@Options(fun = RelativeComponentNames.class, args = @Ref(DIALOG_NAME_PROPERTY))
		ComponentName getTargetComponent();

		/**
		 * @see #getTargetComponent()
		 */
		void setTargetComponent(ComponentName value);

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.CREATE_NAME)
		CommandGroupReference getGroup();

		@Override
		ResKey getResourceKey();

		@Override
		@ListDefault(InViewModeExecutable.class)
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

	}

    /** the basic js command name */
    private static final String COMMAND = "displayDialog";

	private static final class OpenExecutabilityRule implements ExecutabilityRule {

		private OpenModalDialogCommandHandler _handler;

		/**
		 * Creates a {@link OpenModalDialogCommandHandler.OpenExecutabilityRule}.
		 */
		public OpenExecutabilityRule(OpenModalDialogCommandHandler handler) {
			_handler = handler;
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent targetComponent, Object model,
				Map<String, Object> someValues) {

			// Check that the model target component supports the model transferred in the opening
			// process.
			CommandHandler.Config config = _handler.getConfig();
			if (config instanceof OpenModalDialogCommandHandler.Config) {
				ComponentName modelTargetName = ((OpenModalDialogCommandHandler.Config) config).getTargetComponent();
				if (modelTargetName != null) {
					LayoutComponent modelTarget = targetComponent.getComponentByName(modelTargetName);
					if (modelTarget != null && !modelTarget.supportsModel(model)) {
						return ExecutableState.NOT_EXEC_HIDDEN;
					}
				}
			}

			// Check that the specified security component allows the target model.
			BoundComponent securityComponent = getSecurityComponent(targetComponent);
			if (securityComponent == null) {
				return ExecutableState.EXECUTABLE;
			}

			if (!allow(securityComponent, model)) {
				return ExecutableState.NO_EXEC_PERMISSION;
			}

			return ExecutableState.EXECUTABLE;
		}

		private boolean allow(BoundComponent securityComponent, Object model) {
			BoundCommandGroup handlerGroup = _handler.getCommandGroup();
			return securityComponent.allowPotentialModel(handlerGroup, model);
		}

		private BoundComponent getSecurityComponent(LayoutComponent targetComponent) {
			if (targetComponent == null) {
				return null;
			}
			LayoutComponent dialog = getDialog(targetComponent);
			if (dialog == null) {
				return null;
			}
			DialogInfo dialogInfo = dialog.getDialogInfo();
			ComponentName securityComponentName = dialogInfo.getSecurityComponentName();
			if (securityComponentName != null) {
				LayoutComponent result = dialog.getComponentByName(securityComponentName);
				if (!(result instanceof BoundComponent)) {
					Logger.warn("Security component '" + securityComponentName + "' not a bound component at "
						+ dialogInfo.location(), OpenModalDialogCommandHandler.class);
					return null;
				}
				return (BoundComponent) result;
			}
			return null;
		}

		private LayoutComponent getDialog(LayoutComponent targetComponent) {
			// Note: The dialog could be defined on another component, therefore
			// targetComponent.getDialog(...) is not sufficient.
			ComponentName componentName = _handler.getOpenToDialogName();
			LayoutComponent dialogComponent = targetComponent.getComponentByName(componentName);
			return dialogComponent == null ? null : dialogComponent.getDialogTopLayout();
		}
	}

    /** the dialog to be displayed */
	private final ComponentName _dialogName;

	private final List<PostCreateAction> _actions;
    
	/**
	 * Creates a {@link OpenModalDialogCommandHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public OpenModalDialogCommandHandler(InstantiationContext context, Config config) {
		super(context, config);

		this._dialogName = config.getDialogName();
		_actions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
	}

    /**
     * dispatch the control to the dialog.
     */
    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, 
                         Object model, Map<String, Object> someArguments) {
		/* init the component */
		LayoutComponent dialog = this.getDialog(aComponent, someArguments);
		if (dialog == null) {
		    throw new NullPointerException("Failed to getDialog()");
		}

		transferModel(dialog, model);

		this.beforeOpening(aContext, aComponent, someArguments, dialog);
		
		DialogInfo dialogInfo = dialog.getDialogInfo();
		HTMLFragment dialogTitle;
		if (dialogInfo.getTitle() != null) {
			dialogTitle = TypedConfigUtil.createInstance(dialogInfo.getTitle()).createTitle(dialog);
		} else {
			dialogTitle = new ResourceText(createDialogTitle(aComponent, dialog, dialogInfo));
		}
		internalOpenDialog(dialog, dialogTitle, aComponent);

		WithPostCreateActions.processCreateActions(_actions, aComponent, model);

		return HandlerResult.DEFAULT_RESULT;
    }

	/**
	 * Transfers the model on which this open handler operates to a configured
	 * {@link Config#getTargetComponent()} (normally within the given dialog component).
	 */
	protected void transferModel(LayoutComponent dialog, Object model) {
		ComponentName targetComponentName = ((Config) getConfig()).getTargetComponent();
		if (targetComponentName != null) {
			LayoutComponent targetComponent = dialog.getComponentByName(targetComponentName);
			if (targetComponent == null) {
				throw new IllegalArgumentException(
					"Target component with name '" + targetComponentName + "' not found.");
			}
			targetComponent.setModel(model);
		}
	}
	
	/**
	 * Opens the given dialog using {@link TitleProvider} configured in
	 * {@link DialogInfo#getTitle()} to create a title.
	 * 
	 * @see TitleProvider
	 * @see DialogInfo
	 * @see #openDialog(LayoutComponent, HTMLFragment)
	 * 
	 * @param dialog
	 *        {@link LayoutComponent} opened as dialog.
	 */
	public static void openDialog(LayoutComponent dialog) {
		TitleProvider instance =
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(dialog.getDialogInfo().getTitle());

		internalOpenDialog(dialog, instance.createTitle(dialog), dialog.getDialogParent());
	}

    /**
	 * This method opens the given dialog.
	 * 
	 * @param dialog
	 *        the dialog to open
	 * @param dialogTitle
	 *        the title of the dialog
	 */
	public static void openDialog(LayoutComponent dialog, HTMLFragment dialogTitle) {
		internalOpenDialog(dialog, dialogTitle, dialog.getDialogParent());
	}

	private static void internalOpenDialog(LayoutComponent dialog, HTMLFragment dialogTitle,
			LayoutComponent dialogParent) {
		DialogSupport dialogSupport = dialogParent.getDialogSupport();
		dialog.setVisible(true);

        /* compute js to show dialog */
		DialogInfo theInfo = dialog.getDialogInfo();

		dialogParent.setDialog(dialog);
		dialogSupport.registerOpenedDialog(dialog, theInfo, dialogTitle);
		// Notify component, too (will be suppressed by default Event logic)
		dialogParent.receiveDialogEvent(dialog, dialogParent, /* isOpen */true);
        // Notify GUI of Dialog opening
		dialogParent.doFireModelEvent(dialog, dialogParent, ModelEventListener.DIALOG_OPENED);
	}

	
	/**
	 * Resolves the dialog from the given arguments
	 * 
	 * @param component
	 *        the component on which the dialog should be opened
	 * @param someArguments
	 *        the arguments delivered by
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)} to resolve dialog from
	 * @return the dialog which is finally opened. It must be a dialog of the given component, i.e.
	 *         {@link LayoutComponent#getDialogParent()} of the returned dialog must be the given
	 *         component.
	 */
	protected LayoutComponent getDialog(LayoutComponent component, Map<String, Object> someArguments) {
		return component.getComponentByName(_dialogName);
    }

    /**
	 * @deprecated use {@link #createDialogTitle(LayoutComponent, LayoutComponent, DialogInfo)}
	 */
    @Deprecated
	public final String createDialogTitle(DisplayContext aContext, LayoutComponent aDialogOpener,
			LayoutComponent aDialogContent, DialogInfo aDialogInfo) {
		Resources resources = aContext.getResources();
		return createDialogTitle(resources, aDialogOpener, aDialogContent, aDialogInfo);
    }

	/**
	 * @deprecated use {@link #createDialogTitle(LayoutComponent, LayoutComponent, DialogInfo)}
	 */
    @Deprecated
	public final String createDialogTitle(Resources resources, LayoutComponent aDialogOpener,
			LayoutComponent aDialogContent, DialogInfo aDialogInfo) {
		return resources.getString(createDialogTitle(aDialogOpener, aDialogContent, aDialogInfo));
	}
    
    
	/**
	 * This method creates an internationalized title for a dialog.
	 * 
	 * @param aDialogOpener
	 *            the component which opens the dialog
	 * @param aDialogContent
	 *            the component which shall be shown in the dialog
	 * @param aDialogInfo
	 *            the info for the dialog
	 * @return the I18N title for the dialog
	 */
	public ResKey createDialogTitle(LayoutComponent aDialogOpener, LayoutComponent aDialogContent,
			DialogInfo aDialogInfo) {
		ResKey openerTitle = getResourceKey(aDialogOpener);
		ResKey derivedFromOpener = openerTitle.suffix(DIALOG_TITLE_SUFFIX);
		ResKey dialogContentTitle = aDialogContent.getTitleKey();
		
		return derivedFromOpener.fallback(dialogContentTitle).fallback(openerTitle);
	}

	/** 
     * This is a hook to do something before the dialog is opened.
     */
    protected void beforeOpening(DisplayContext aContext, LayoutComponent aComponent, Map aSomeArguments, LayoutComponent aDialog) {
		// Hook for subclasses.
	}
	
    /**
     * the dialog name this handler can open.
     */
    public ComponentName getOpenToDialogName() {
		return this._dialogName;
    }

	public static String getOpenCommandName(ComponentName dialogName) {
		String localName = dialogName.localName()
			.replaceAll("\\.", "_").replaceAll("\\/", "_");
		// TODO KHA check this
		// dialogName in older implementation was a res prefix ('.')
		// neowadays it is a path to a a layout that may contain '/'
		return COMMAND + "_" + localName;
	}

    @Override
	public CommandScriptWriter getCommandScriptWriter(LayoutComponent component) {
		return AJAXCommandScriptWriter.INSTANCE;
    }

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return new OpenExecutabilityRule(this);
	}

	public static PolymorphicConfiguration<? extends CommandHandler> createDialogOpenHandler(Log log,
			LayoutComponent.Config dialogParent, LayoutComponent.Config dialog) {
		DialogInfo dialogInfo = dialog.getDialogInfo();
		if (dialogInfo == null) {
			log.error(
				"Missing dialog info for dialog '" + dialog.getName() + "' declared in '" + dialog.location() + "'.");
			return null;
		}
		ComponentName dialogName = dialog.getName();
		PolymorphicConfiguration<? extends CommandHandler> openHandlerConfig =
			OpenModalDialogCommandHandler.createOpenHandler(dialogInfo, dialogName);
		if (openHandlerConfig == null) {
			String openHandlerName = dialogInfo.getOpenHandlerName();
			boolean hasOpenHandlerName = openHandlerName != null;
			String commandId = hasOpenHandlerName ? openHandlerName : getOpenCommandName(dialogName);

			Config config = AbstractCommandHandler.createConfig(dialogInfo.getOpenHandlerClass(), commandId);
			updateDialogName(config, dialog.getName());
			transferInfo(dialogInfo, config);

			ResKey labelKey = dialogInfo.getDefaultI18N();
			if (labelKey == null) {
				if (dialogInfo.getCreateOpenerButtons()) {
					labelKey = createDefaultOpenerLabelKey(dialogParent, dialog, dialogName, openHandlerName,
						hasOpenHandlerName);
				}
			}
			if (labelKey != null) {
				updateResourceKey(config, labelKey);
			}

			if (dialogInfo.getTargetComponent() == null) {
				addAllowDialogCheck(config, dialogName);
			}
			openHandlerConfig = config;
		}
		ComponentName checkerName = dialogInfo.getSecurityComponentName();
		if (checkerName == null) {
			return openHandlerConfig;
		}

		CheckerProxyHandler.Config checkerConfig = TypedConfiguration.newConfigItem(CheckerProxyHandler.Config.class);
		checkerConfig.setName(checkerName);
		checkerConfig.setCommand(openHandlerConfig);
		return checkerConfig;
	}

	/**
	 * Transfers common information from the window config to the command config.
	 */
	public static void transferInfo(AbstractWindowInfo theInfo, CommandHandler.Config config) {
		transferClique(theInfo, config);
		transferGroup(theInfo, config);
		transferImages(theInfo, config);
		transferExecutability(theInfo, config);
		transferTarget(theInfo, config);
		transferTargetComponent(theInfo, config);
	}

	private static void transferTarget(AbstractWindowInfo theInfo, CommandHandler.Config config) {
		ModelSpec target = theInfo.getTarget();
		if (target != null) {
			update(config, Config.TARGET, TypedConfiguration.copy(target));
		}
	}

	private static void transferTargetComponent(AbstractWindowInfo theInfo, CommandHandler.Config config) {
		ComponentName targetComponent = theInfo.getTargetComponent();
		if (targetComponent != null) {
			update(config, Config.TARGET_COMPONENT, targetComponent);
		}
	}

	/**
	 * Transfers executability information from the window config to the command config.
	 */
	public static void transferExecutability(AbstractWindowInfo theInfo, CommandHandler.Config command) {
		updateExecutability(command, theInfo.getExecutability());
	}

	/**
	 * Transfers command group information from the window config to the command config.
	 */
	public static void transferGroup(AbstractWindowInfo theInfo, CommandHandler.Config command) {
		updateGroup(command, theInfo.getOpenerCommandGroup());
	}

	/**
	 * Transfers command clique information from the window config to the command config.
	 */
	public static void transferClique(AbstractWindowInfo window, CommandHandler.Config command) {
		String clique = window.getCommandClique();
		if (clique == null) {
			clique = CommandHandlerFactory.getInstance().getDefaultClique();
		}
		command.setClique(clique);
	}

	/**
	 * Transfers icon information from the window config to the command config.
	 */
	public static void transferImages(AbstractWindowInfo window, CommandHandler.Config command) {
		ThemeImage image = window.getImage();
		if (image != null) {
			updateImage(command, image);
		}
		ThemeImage disabledImage = window.getDisabledImage();
		if (disabledImage != null) {
			updateDisabledImage(command, disabledImage);
		}
	}

	private static ResKey createDefaultOpenerLabelKey(LayoutComponent.Config dialogParent,
			LayoutComponent.Config dialog, ComponentName dialogName, String openHandlerName, boolean hasOpenHandlerName) {
		return ResKey.fallback(
			// Opener-local key, if an explicit openHandlerName is given.
			LayoutComponent.Config.getEffectiveResPrefix(dialogParent).key(openHandlerName),

			ResKey.fallback(
				// Dialog-local key derived form the dialog's resources.
				LayoutComponent.Config.getEffectiveResPrefix(dialog).key(OPEN_AS_DIALOG_SUFFIX),

				// The default open command name instead of the explicitly configured one in the context
				// of the opening component.
				hasOpenHandlerName ? 
					// TODO #6121: Delete TL 5.8.0 deprecation: Replace with null.
					ResKey.deprecated(generatedNameKey(dialogParent, dialogName)) : 

					// A generated name key, if no other key can be used.
					generatedNameKey(dialogParent, dialogName)));
	}

	private static ResKey generatedNameKey(LayoutComponent.Config dialogParent, ComponentName dialogName) {
		return LayoutComponent.Config.getEffectiveResPrefix(dialogParent).key(getOpenCommandName(dialogName));
	}

	private static void addAllowDialogCheck(Config config, ComponentName dialogName) {
		Class<?> implementationClass = config.getImplementationClass();
		while (implementationClass != null) {
			NoDialogContentCheck annotation = implementationClass.getAnnotation(NoDialogContentCheck.class);
			if (annotation == null) {
				implementationClass = implementationClass.getSuperclass();
				continue;
			}
			if (annotation.value()) {
				// no check;
				return;
			} else {
				// with check.
				break;
			}
		}
		OpenAllowedExecutability.Config newConfigItem =
			TypedConfiguration.newConfigItem(OpenAllowedExecutability.Config.class);
		newConfigItem.setDialogName(dialogName);
		config.getExecutability().add(newConfigItem);
	}

	public static <C extends Config> C updateDialogName(C config, ComponentName value) {
		return update(config, Config.DIALOG_NAME_PROPERTY, value);
	}

	public static PolymorphicConfiguration<? extends CommandHandler> createOpenHandler(DialogInfo dialogInfo,
			ComponentName dialogName) {
		PolymorphicConfiguration<? extends CommandHandler> openHandlerConfig = dialogInfo.getOpenHandler();
		if (openHandlerConfig instanceof OpenModalDialogCommandHandler.Config) {
			OpenModalDialogCommandHandler.Config openModalHandlerConfig =
				(OpenModalDialogCommandHandler.Config) openHandlerConfig;
			if (openModalHandlerConfig.getDialogName() == null) {
				// Apply dynamic default.
				openModalHandlerConfig = TypedConfiguration.copy(openModalHandlerConfig);
				openModalHandlerConfig.setDialogName(dialogName);
				openHandlerConfig = openModalHandlerConfig;
			}
		}
		return openHandlerConfig;
	}

}
