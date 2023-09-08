/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.gui.layout.ButtonComponent;
import com.top_logic.layout.admin.component.DescendantComponentFinder;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.inspector.OpenSeparateInspectorWindowCommandHandler;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupType;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.util.Resources;

/**
 * Display some information about a component and its environment.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ViewInfoComponent extends FormComponent {

	/**
	 * Application-global configuration for {@link ViewInfoComponent}.
	 */
	public interface GlobalConfig extends ConfigurationItem {
		/**
		 * Session-local name of the {@link ViewInfoComponent} to use for inspect buttons.
		 */
		ComponentName getInspectComponent();
	}

	private static final String INSPECTOR_WINDOW = "openInspector";

	private LayoutComponent _buttonOrForm;

	private LayoutComponent _businessComponent;

	/**
	 * Creates a {@link ViewInfoComponent}.
	 */
    public ViewInfoComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

    @Override
	public FormContext createFormContext() {
        FormContext     theContext = new FormContext("viewInfo", this.getResPrefix());
        LayoutComponent theComp    = this.getCurrentView();
		if (_buttonOrForm != null) {
			if (_businessComponent != null) {
				this.addObjectField(theContext, "real", _businessComponent, false);

				this.addComponentDescription(theContext, _businessComponent, "real_");
				this.addCommands(theContext, _businessComponent, "real_");

				if (_buttonOrForm instanceof ButtonComponent) {
					this.addObjectField(theContext, "button_component", _buttonOrForm, false);
					theContext.addMember(this.createStringField("button_prefix", _buttonOrForm.getResPrefix()));
				}
			}
        }
        else {
            this.addMasterDescription(theContext, theComp, "real_");
        }

        this.addComponentDescription(theContext, theComp, null);

		return theContext;
    }

	private LayoutComponent getBusinessComponent(LayoutComponent component) {
		if (!(component instanceof ButtonComponent)) {
			// component is businss component
			return component;
		}

		return component.getMaster();
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(this);
		}
		return super.validateModel(context);
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject instanceof LayoutComponent;
    }

    /** 
     * Return the layout component currently selected.
     */
    public LayoutComponent getCurrentView() {
        return ((LayoutComponent) this.getModel());
    }

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		removeFormContext();
	}

	@Override
	protected void handleNewModel(Object newModel) {
		super.handleNewModel(newModel);
		_buttonOrForm = this.findButtonOrDirectComponent((LayoutComponent) newModel);
		if (_buttonOrForm != null) {
			_businessComponent = getBusinessComponent(_buttonOrForm);
			/* Fetch model here to initialise components that were not yet visible. Otherwise this
			 * occurs during rendering (i.e. creation of FormContext) which leads to errors in log. */
			if (_businessComponent != null && _businessComponent != this) {
				_businessComponent.getModel();
			}
		}
	}

    /** 
     * Append several fields describing the given component to the given context.
     * 
     * This method will append different information of the component and its master component.
     * 
     * @param    aContext      The context to be filled.
     * @param    aComponent    The component to be described, must not be <code>null</code>.
     * @param    aPrefix       An optional prefix for the field names.
     */
    protected void addComponentDescription(FormContext aContext, LayoutComponent aComponent, String aPrefix) {
        if (aPrefix == null) {
            aPrefix = "";
        }

        this.addObjectField(aContext, aPrefix + "model", aComponent.getModel(), true);
        this.addObjectField(aContext, aPrefix + "component", aComponent, false);

        aContext.addMember(this.createStringField(aPrefix + "prefix", aComponent.getResPrefix()));
        aContext.addMember(this.createStringField(aPrefix + "class", aComponent.getClass().getName()));
        aContext.addMember(this.createStringField(aPrefix + "location", aComponent.getLocation()));

        this.addMasterDescription(aContext, aComponent, aPrefix);
    }

    /** 
     * Add some information about the master component (if available).
     * 
     * @param aContext      The form context to append the information to.
     * @param aComponent    The component to get its master from.
     * @param aPrefix       The I18N prefix.
     */
    protected void addMasterDescription(FormContext aContext, LayoutComponent aComponent, String aPrefix) {
        LayoutComponent theMaster = CollectionUtil.getFirst(aComponent.getMasters());

        if (aPrefix == null) {
            aPrefix = "";
        }

        if (theMaster != null) {
            this.addObjectField(aContext, aPrefix + "master_component", theMaster, false);
            this.addObjectField(aContext, aPrefix + "master_model", theMaster.getModel(), true);

            aContext.addMember(this.createStringField(aPrefix + "master_prefix", theMaster.getResPrefix()));
            aContext.addMember(this.createStringField(aPrefix + "master_location", theMaster.getLocation()));
            aContext.addMember(this.createStringField(aPrefix + "master_model_desc", this.getModelDescription(theMaster)));
        }
    }

    /** 
     * Create an object field and 
     * 
     * @param aContext      The form context to append the information to.
     * @param aName         THe name of the requested object field.
     * @param anObject      The object to create the field for.
     * @param asInstance    <code>true</code> when instance java class information should be displayed.
     */
    protected void addObjectField(FormContext aContext, String aName, Object anObject, boolean asInstance) {
		aContext.addMember(this.createStringField(aName, this.getObjectDisplayName(anObject, asInstance)));
    	aContext.addMember(this.createButtonCommand(aName + "__class", anObject));
    }

	/** 
	 * Return an object name to be used for displaying.
	 * 
     * @param    anObject      The object to create the display name for.
     * @param    asInstance    <code>true</code> when instance java class information should be displayed.
	 * @return   The requested name, never <code>null</code>.
	 */
	private Object getObjectDisplayName(Object anObject, boolean asInstance) {
		return (anObject != null) && asInstance ? anObject.getClass().getName() + '@' + Integer.toHexString(anObject.hashCode()) : anObject;
	}

	/**
	 * Return the command field to inspect the given object.
	 * 
	 * @param    aName       The field name.
	 * @param    anObject    The object to create the button for.
	 * @return   The requested command field, never <code>null</code>.
	 */
	protected FormMember createButtonCommand(String aName, Object anObject) {
		CommandField theField = createInspectButton(this, aName, anObject);

		theField.setLabel(" (CLICK)");
		theField.setVisible(anObject != null);

		return theField;
	}

	/**
	 * Creates a button that opens the given model in the object inspector.
	 * 
	 * @param layout
	 *        The layout root.
	 * @param fieldName
	 *        The name of the button to create.
	 * @param model
	 *        The model to inspect.
	 * @return The button that opens the inspector.
	 */
	public static CommandField createInspectButton(MainLayout layout, String fieldName, Object model) {
		GlobalConfig config = ApplicationConfig.getInstance().getConfig(GlobalConfig.class);
		if (config == null) {
			return null;
		}
		LayoutComponent viewInfo = layout.getComponentByName(config.getInspectComponent());
		if (viewInfo == null) {
			return null;
		}
		return createInspectButton(viewInfo, fieldName, model);
	}

	private static CommandField createInspectButton(LayoutComponent self, String aName, Object model) {
		CommandHandler handler = self.getCommandById(INSPECTOR_WINDOW);
		Map<String, Object> arguments =
			Collections.singletonMap(OpenSeparateInspectorWindowCommandHandler.PARAM_OBJECT, model);
		return FormFactory.newCommandField(aName, handler, self, arguments);
	}

    /** 
     * Append all commands of the given component to the given context.
     * 
     * This method will append different information of the component and its master component.
     * 
     * @param    aContext      The context to be filled.
     * @param    aComponent    The component to get the commands from, must not be <code>null</code>.
     * @param    aPrefix       An optional prefix for the field names.
     */
    protected void addCommands(FormContext aContext, LayoutComponent aComponent, String aPrefix) {
        Collection<? extends CommandHandler> theCommands = aComponent.getCommands();

        if (theCommands != null) {
            FormGroup         theContainer;
            BoundCommandGroup theGroup;
			CommandGroupType theCType;
            String            theType;
            int               thePos    = 0;
            FormGroup         theResult = new FormGroup(aPrefix + "commands", this.getResPrefix());

            for (CommandHandler theHandler : theCommands) {
                theContainer = new FormGroup(aPrefix + "commands_" + thePos , this.getResPrefix());
                theGroup     = theHandler.getCommandGroup();
                theCType     = theGroup.getCommandType();

                switch (theCType) {
					case READ:
						theType = "R";
						break;
					case WRITE:
						theType = "W";
						break;
					case DELETE:
						theType = "D";
						break;
					default:
						theType = theCType.getExternalName();
                }

                theContainer.addMember(this.createStringField("cmdGroup", theType));
                theContainer.addMember(this.createStringField("id", theHandler.getID()));
                theContainer.addMember(this.createStringField("class", theHandler.getClass().getName()));

                theResult.addMember(theContainer);

                thePos++;
            }

            aContext.addMember(theResult);
        }
    }

    /** 
     * Return a short description of the model of the given component.
     * 
     * The short description is the value, which will be returned by {@link Object#toString()}.
     * 
     * @param    aComponent    The component to get the model for, must not be <code>null</code>.
     * @return   The description of the model or <code>null</code>, if there is no model.
     */
    protected Object getModelDescription(LayoutComponent aComponent) {
        Object theModel  = aComponent.getModel();

        if (theModel != null) {
            theModel = theModel.getClass().getName() + '@' + Integer.toHexString(theModel.hashCode());
        }

        return (theModel);
    }

	/**
	 * Look up the first (button or grid) component which is a child of the given component.
	 * 
	 * @param aComponent
	 *        The component to start the search at.
	 * @return The found component or <code>null</code>.
	 */
	protected LayoutComponent findButtonOrDirectComponent(LayoutComponent aComponent) {
		LayoutComponent foundButton = DescendantComponentFinder.findButtonOrDirectComponent(aComponent, false);
		if (foundButton != null) {
			return foundButton;
		}
		LayoutComponent foundForm = DescendantComponentFinder.findButtonOrDirectComponent(aComponent, true);
		return foundForm;
	}

    /** 
     * Create a string based field for the given value.
     * 
     * @param    aName     The name of the field, must not be <code>null</code>.
     * @param    aValue    The value to be displayed in the field, may be <code>null</code>.
     * @return   The requested field (immutable), never <code>null</code>.
     */
    protected StringField createStringField(String aName, Object aValue) {
        StringField theField = FormFactory.newStringField(aName, true);

        if (aValue instanceof LayoutComponent) {
            LayoutComponent theLayout = (LayoutComponent) aValue;

            theField.setAsString(theLayout.getName() + " (Class: " + theLayout.getClass().getName() + ")");
        }
        else if (aValue == null) {
            theField.setAsString("[null]");
        }
        else {
            theField.setAsString(aValue.toString());
        }

        return (theField);
    }

	/**
	 * {@link TreeModelBuilder} displaying the {@link CompoundSecurityLayout} in the application
	 * with the {@link MainLayout} as root.
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ViewInfoTreeModelBuilder extends CompoundSecurityLayoutBuilder {
    	
		/**
		 * Singleton {@link ViewInfoTreeModelBuilder} instance.
		 */
		public static final ViewInfoTreeModelBuilder INSTANCE = new ViewInfoTreeModelBuilder();

		private ViewInfoTreeModelBuilder() {
			// Singleton constructor.
		}

		@Override
		public Object retrieveModelFromNode(LayoutComponent contextComponent, LayoutComponent node) {
			return node.getMainLayout();
		}

		@Override
		public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
			return aModel instanceof MainLayout;
		}

    }

	/**
	 * {@link ModelProvider} returning the {@link MainLayout} of the {@link LayoutComponent}.
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class MainLayoutProvider implements ModelProvider {

		@Override
		public Object getBusinessModel(LayoutComponent businessComponent) {
			return businessComponent.getMainLayout();
		}

	}

    /**
     * Provide rendering information for layout components.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class ViewInfoTreeResourceProvider extends DefaultResourceProvider {

		/** Singleton {@link ViewInfoTreeResourceProvider} instance. */
		@SuppressWarnings("hiding")
		public static final ViewInfoTreeResourceProvider INSTANCE = new ViewInfoTreeResourceProvider();

		/**
		 * Creates a new {@link ViewInfoTreeResourceProvider}.
		 * 
		 */
		protected ViewInfoTreeResourceProvider() {
			// singleton instance
		}

		@Override
		public ThemeImage getImage(Object aNode, Flavor aFlavor) {
			return Icons.V;
        }

        @Override
		public String getLabel(Object aNode) {
            LayoutComponent theComp   = (LayoutComponent) aNode;
            Resources       theRes    = Resources.getInstance();
			String theName = theRes.getString(theComp.getTitleKey(), null);

            if (theName == null) {
				theName = theComp.getName().qualifiedName();
            }

            return (theName);
        }
    }
}
