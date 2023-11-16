/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.component.PostCreateAction.SetEditMode;
import com.top_logic.layout.form.component.edit.EditMode;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CloseModalDialogCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link FormComponent} to use as base for create dialogs.
 * 
 * <p>
 * Note: Instead of creating custom sub-classes of this component, consider using the
 * <code>com.top_logic.element/create/genericCreate.xml</code> template.
 * </p>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class AbstractCreateComponent extends FormComponent {

	/**
	 * Configuration options for {@link AbstractCreateComponent}.
	 */
	public interface Config extends FormComponent.Config {

		/**
		 * @see #getCreateHandler()
		 */
		String CREATE_HANDLER = "create-handler";

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * Operation to perform after the new object is created.
		 * 
		 * <p>
		 * Note: The current default {@link NewModelAction#SET_PARENT_MODEL} is only reasonable,
		 * when the create dialog is registered on a form. When registering the create dialog on a
		 * table, the value must be changed to {@link NewModelAction#SET_PARENT_SELECTION}
		 * otherwise, the table will behave unpredictably.
		 * </p>
		 * 
		 * @deprecated Use {@link AbstractCreateCommandHandler.Config#getPostCreateActions()}
		 *             instead.
		 */
		@Deprecated
		@Name(ATTR_NEW_MODEL_ACTION)
		@FormattedDefault(VALUE_NEW_MODEL_SET_PARENT_MODEL)
		NewModelAction getNewModelAction();

		/**
		 * Whether to switch the dialog parent component to {@link EditMode#setEditMode() edit mode}
		 * after the creation succeeds.
		 * 
		 * @see AbstractCreateComponent#handleNew(Object)
		 * @deprecated Use {@link SetEditMode} as
		 *             {@link AbstractCreateCommandHandler.Config#getPostCreateActions()}.
		 */
		@Deprecated
		@Name(ATTR_SET_PARENT_EDIT)
		@BooleanDefault(true)
		boolean getSetParentToEdit();

		@Override
		@FormattedDefault("model(dialogParent())")
		public ModelSpec getModelSpec();

		/**
		 * The {@link com.top_logic.tool.boundsec.CommandHandler.Config#getId()} of the
		 * {@link CommandHandler} responsible executing the creation of the new object from the
		 * information collected by this component.
		 */
		@Mandatory
		@Name(CREATE_HANDLER)
		String getCreateHandler();

		/**
		 * The {@link com.top_logic.tool.boundsec.CommandHandler.Config#getId()} of the
		 * {@link CommandHandler} that aborts the create operation.
		 */
		@StringDefault(CancelHandler.COMMAND_ID)
		String getCancelHandler();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			registry.registerButton(getCreateHandler());
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
		}

	}

	/**
	 * Configures the {@link #newModelAction} property.
	 * 
	 * <p>
	 * Possible values are {@link #VALUE_NEW_MODEL_NO_ACTION},
	 * {@link #VALUE_NEW_MODEL_SET_PARENT_MODEL}, or
	 * {@link #VALUE_NEW_MODEL_SET_PARENT_SELECTION}.
	 * </p>
	 */
	public static final String ATTR_NEW_MODEL_ACTION = "newModelAction";

	/**
	 * Configuration value for setting {@link NewModelAction#NO_ACTION} to {@link #newModelAction}.
	 */
	public static final String VALUE_NEW_MODEL_NO_ACTION = "no-action";

	/**
	 * Configuration value for setting {@link NewModelAction#SET_PARENT_MODEL} to
	 * {@link #newModelAction}.
	 */
	public static final String VALUE_NEW_MODEL_SET_PARENT_MODEL = "set-parent-model";

	/**
	 * Configuration value for setting {@link NewModelAction#SET_PARENT_SELECTION} to
	 * {@link #newModelAction}.
	 */
	public static final String VALUE_NEW_MODEL_SET_PARENT_SELECTION = "set-parent-selection";
	
	public static final String ATTR_SET_PARENT_EDIT="setParentToEdit";

	/**
	 * One of {@link NewModelAction#NO_ACTION}, {@link NewModelAction#SET_PARENT_MODEL}, or
	 * {@link NewModelAction#SET_PARENT_SELECTION}.
	 * 
	 * <p>
	 * Configured through the {@link #ATTR_NEW_MODEL_ACTION} property.
	 * </p>
	 */
	private final NewModelAction newModelAction;

    /**
	 * @see Config#getSetParentToEdit()
	 */
	private final boolean setParentToEdit;
	
	/**
	 * Enumeration describing possible actions after create.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static enum NewModelAction implements ExternallyNamed {

		/**
		 * Perform no action.
		 */
		NO_ACTION(VALUE_NEW_MODEL_NO_ACTION),

		/**
		 * Set the newly created object as model in the dialog parent component.
		 */
		SET_PARENT_MODEL(VALUE_NEW_MODEL_SET_PARENT_MODEL),

		/**
		 * Set the newly created object as selection in the dialog parent component.
		 */
		SET_PARENT_SELECTION(VALUE_NEW_MODEL_SET_PARENT_SELECTION);

		private final String _externalName;
		
		NewModelAction(String externalName) {
			_externalName = externalName;
		}
		
		@Override
		public String getExternalName() {
			return _externalName;
		}
	}
	
	/**
	 * Creates a {@link AbstractCreateComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
    public AbstractCreateComponent(InstantiationContext context, Config config) throws ConfigurationException {
        super(context, config);
        
		newModelAction = config.getNewModelAction();
		setParentToEdit = config.getSetParentToEdit();
    }

    /**
	 * Return the ID of the {@link com.top_logic.tool.boundsec.CommandHandler handler} responsible
	 * for the creation of a new element.
	 * 
	 * @see Config#getCreateHandler()
	 * 
	 * @return The ID of the handler responsible for creating the element, must not be
	 *         <code>null</code>.
	 */
	protected final String getCreateHandler() {
		return ((Config) getConfig()).getCreateHandler();
	}

    @Override
	public CommandHandler getDefaultCommand() {
        return this.getCommandById(this.getCreateHandler());
    }
    
    @Override
	public CommandHandler getCancelCommand() {
        return this.getCommandById(this.getCancelHandler());
    }
    
    @Override
	protected String getDefaultCloseDialogHandlerName() {
		return getCancelHandler();
    }

	protected final String getCancelHandler() {
		return ((Config) getConfig()).getCancelHandler();
    }

    /**
     * @see com.top_logic.mig.html.layout.LayoutComponent#becomingVisible()
     */
    @Override
	protected void becomingVisible() {
        super.becomingVisible();
		removeFormContext();
    }

    /**
     * Handle the UI part of creating a new object.
     * 
     * This method will invalidate the parent parts and notify about model creation.
     * Moreover the {@link #setNewModel(LayoutComponent, Object)} will be called to
     * set the model into the dialog parent component.
     * 
     * @param    aNewObject    The new created object.
     * @see      #setNewModel(LayoutComponent, Object)
     * @see      AbstractCreateCommandHandler
     */
    public void handleNew(Object aNewObject) {
        
        if (this.openedAsDialog()) {
			LayoutComponent dialogParent = this.getDialogParent();
    
			dialogParent.invalidateButtons();
			dialogParent.fireModelCreatedEvent(aNewObject, this);
    
			boolean modelSetToParent = this.setNewModel(dialogParent, aNewObject);
    
			if (this.setParentToEdit && (dialogParent instanceof EditMode)) {
				if (!((EditMode) dialogParent).isInEditMode() && modelSetToParent) {
					((EditMode) dialogParent).setEditMode();
                }
            }
        } else {
            this.fireModelCreatedEvent(aNewObject, this);
        }
    }

	/**
	 * Set the new model to the parent component.
	 * 
	 * @param aParent
	 *        The parent component of this dialog component, must not be <code>null</code>
	 * @param aNewObject
	 *        The new created object, must not be <code>null</code>.
	 */
	protected boolean setNewModel(LayoutComponent aParent, Object aNewObject) {
		switch (newModelAction) {
			case NO_ACTION: {
				return false;
			}
			case SET_PARENT_MODEL: {
				if (!aParent.supportsModel(aNewObject)) {
					return false;
				}
				aParent.setModel(aNewObject);
				return true;
			}
			case SET_PARENT_SELECTION: {
				if (!(aParent instanceof Selectable)) {
					return false;
				}
				return ((Selectable) aParent).setSelected(aNewObject);
			}
		}
		return false;
	}
    
    @Override
	protected boolean receiveModelChangedEvent(Object aModel, Object changedBy) {
    	// Must not clear form context, if parent object changes.
    	//
    	// return super.receiveModelChangedEvent(aModel, changedBy);
    	return false;
    }
    
    /**
	 * Cancels the current create operation. All values edited so far are lost and an open dialog is
	 * closed.
	 */
	@Label("Cancel")
    public static class CancelHandler extends CloseModalDialogCommandHandler {

        public static final String COMMAND_ID = "cancelCreate";

		/**
		 * Configuration for {@link CancelHandler}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends CloseModalDialogCommandHandler.Config {

			@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
			@Override
			CommandGroupReference getGroup();

		}

        public CancelHandler(InstantiationContext context, Config config) {
			super(context, config);
        }
        
        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
            HandlerResult theResult  = new HandlerResult();
            FormComponent theComp    = (FormComponent) aComponent;

            this.performCloseDialog(theComp, theResult);

            return theResult;
        }
        
        @Override
		@Deprecated
		public ResKey getDefaultI18NKey() {
			return I18NConstants.CANCEL;
        }
    }
}
