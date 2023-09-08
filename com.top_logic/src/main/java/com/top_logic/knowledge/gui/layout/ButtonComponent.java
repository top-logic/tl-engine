/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.Control;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.Icons;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ComponentCommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.component.AJAXSupport;
import com.top_logic.layout.basic.component.ControlComponent.DispatchAction;
import com.top_logic.layout.basic.component.ControlSupport;
import com.top_logic.layout.buttonbar.ButtonBarControl;
import com.top_logic.layout.buttonbar.ButtonBarFactory;
import com.top_logic.layout.component.model.ModelChangeListener;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.structure.ControlRepresentableCP;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutInfo;
import com.top_logic.mig.html.layout.SubComponentConfig;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.Utils;

/** 
 * Build up the Button-Component (HTML-Component).
 * 
 * @author    hbo
 */
public class ButtonComponent extends BoundComponent implements ControlRepresentable {

	/**
	 * Configuration options for {@link ButtonComponent}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends BoundComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * @see SubComponentConfig#getComponents()
		 */
		String TAG_NAME = "buttonbar";

		@Override
		@ClassDefault(ButtonComponent.class)
		public Class<? extends LayoutComponent> getImplementationClass();

		@Override
		@ItemDefault(ControlRepresentableCP.Config.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		@Name(ATT_FORM)
		String getForm();

		@Name(ATT_ON_SUBMIT)
		String getOnSubmit();

		@Name(CSS_CLASS_PROPERTY)
		@StringDefault(DEFAULT_CSS_CLASS)
		String getCssClass();

		@Override
		@ItemDefault(ButtonLayoutInfo.class)
		LayoutInfo getLayoutInfo();

		/**
		 * {@link LayoutInfo} with specialized defaults for {@link ButtonComponent}.
		 */
		interface ButtonLayoutInfo extends LayoutInfo {
			@Override
			@ComplexDefault(DefaultSize.class)
			DisplayDimension getSize();

			@Override
			@BooleanDefault(false)
			boolean isScrolleable();

			class DefaultSize extends DefaultValueProvider {
				@Override
				public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
					return ThemeFactory.getTheme().getValue(Icons.BUTTON_COMP_HEIGHT);
				}
			}
		}

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			registry.registerCommand(DispatchAction.COMMAND_NAME);
		}
	}
	
	private static final String ATT_ON_SUBMIT = "onSubmit";

	private static final String ATT_FORM = "form";

	/**
	 * Name of the property for configuring {@link #cssClass}. 
	 */
	private static final String CSS_CLASS_PROPERTY = "cssClass";

	/**
	 * Default value for {@link #cssClass}.
	 */
	public static final String DEFAULT_CSS_CLASS = "cmdButtons";

    /**
     * Property of a commandModel whose value is a {@link ControlProvider}. That provider will be
     * called when rendering the corresponding {@link CommandModel}.
     */
	public static final Property<ControlProvider> BUTTON_CONTROL_PROVIDER =
		TypedAnnotatable.property(ControlProvider.class, "buttonControlProvider");
    
    /** the {@link ControlScope} for register the Controls on. see {@link #getControlScope()} */
    private ControlSupport controlSupport;

    /** 
     * The name of the form of Target-Componenent, that is invoked after click
     * on a button of this component, that has not an own form.
     */
    private String form;
    
    /** 
     * The name of the javascript-method, that is invoked after click
     * on a button of this component, that has not an own submit.
     * This attribute is not used so far.
     */
    private String onSubmit;
    
    /** 
     * Buttons that where set by some other componenent.
     * 
     * (They are only transient in so far that they will not be saved to XML ...)
     */     
	protected List<CommandModel> transientButtons;
    
    /** 
     * The CSS class of the buttons-div
     * 
     * <p>
     * This property can be configured through {@link #CSS_CLASS_PROPERTY}.
     * </p>
     */
	private String cssClass;

	/** the control for rendering the framework of this {@link ButtonComponent} */
	private ButtonBarControl buttonBarControl;

	/**
	 * Creates a {@link ButtonComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param anAttributes
	 *        The configuration.
	 */
	@CalledByReflection
    public ButtonComponent(InstantiationContext context, Config anAttributes) throws ConfigurationException{
        super(context, anAttributes);
        this.form       = StringServices.nonEmpty(anAttributes.getForm());
        this.onSubmit   = StringServices.nonEmpty(anAttributes.getOnSubmit());
        this.cssClass   = anAttributes.getCssClass();
        
        controlSupport = new ControlSupport(this);
    }
    
    /** 
     * Return the command model representing the given command handler.
     * 
     * @param    aCommand    The command handler to get the model for, must not be <code>null</code>.
     * @return   The requested command model, may be <code>null</code>.
     */
    public CommandModel getCommandModel(CommandHandler aCommand) {
        
        for (int i = 0, size = transientButtons.size(); i < size; i++) {
            Object theModel = transientButtons.get(i);

            if ((theModel instanceof ComponentCommandModel) && (aCommand == ((ComponentCommandModel) theModel).getCommandHandler())) {
                return (CommandModel) theModel;
            }
        }

        return null;
    }

    @Override
	public ControlScope getControlScope() {
    	return controlSupport;
    }
    
    @Override
	protected AJAXSupport ajaxSupport() {
    	return controlSupport;
    }
    
    @Override
	public void writeBody(ServletContext context, 
                         HttpServletRequest req, 
                         HttpServletResponse resp, 
                         TagWriter out)
                throws IOException, ServletException {

        DisplayContext theDC = DefaultDisplayContext.getDisplayContext(req);
   		getRenderingControl().detach();
        getRenderingControl().write(theDC, out);
    }

	@Override
	protected void writeBodyCssClassesContent(TagWriter out) throws IOException {
        super.writeBodyCssClassesContent(out);
		out.append(ButtonBarControl.BUTTON_BAR_CSS_CLASS);
	}
    
    /**
     * Get-method for instance-variable form.
     */
    public String getForm() {
        return form;
    }

    /**
     * Get-method for instance-variable onSubmit.
     */
    public String getOnSubmit() {
        return onSubmit;
    }

    /**
     * Set-method for instance-variable buttons.
     */
    public void setTransientButtons(List<? extends CommandModel> buttons) {
        if (transientButtons == null) {
            if (buttons != null) {
				transientButtons = new ArrayList<>(buttons);
                checkButtonsChange(Collections.EMPTY_LIST);
            }
        } else {
			ArrayList<CommandModel> oldButtons = new ArrayList<>(transientButtons);
            transientButtons.clear();
            if (buttons != null) {
                transientButtons.addAll(buttons);
            }
            checkButtonsChange(oldButtons);
        }
    }

    /**
     * Appending method for instance-variable buttons.
     */
    public void addTransientButtons(List<? extends CommandModel> buttons) {
        if (transientButtons == null) {
            if (buttons != null) {
				transientButtons = new ArrayList<>(buttons);
                checkButtonsChange(Collections.EMPTY_LIST);
            }
        } else {
			ArrayList<CommandModel> oldButtons = new ArrayList<>(transientButtons);
            if (buttons != null) {
                transientButtons.addAll(buttons);
                checkButtonsChange(oldButtons);
            }
        }
    }

	/**
     * Set-method for instance-variable form.
     */
    public void setForm(String string) {
        form = string;
    }

    /**
     * Set-method for instance-variable onSubmit.
     */
    public void setOnSubmit(String string) {
        onSubmit = string;
    }

    /**
     * Return the List of (transient) Buttons we show.
     * 
     * The transient Buttons will be used only in case the normal Buttons
     * are null (for now).
     */
	public List<CommandModel> getButtons() {
        return this.transientButtons;
    }

    /**
     * We do not support any kind of Object except null ourself.
     * 
     * @return true for null.
     */
    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject == null;
    }

    /**
     * Overridden to allow nothing, so BoundLayout can do its work.
     * 
     * This is necessary because BoundLayout assumes that it is allowed if
     * at least one of its components is allowed. This is to allow old components
     * to be integrated in the framework (ask KHA).
     * 
     * This forces components which can not decide on visibility on their own 
     * to return <code>false</code>.
     */
    @Override
	public ResKey hideReason(Object potentialModel) {
		return ResKey.NONE;
    }

    /**
     * Overridden to allow nothing, so BoundLayout can do its work.
     * 
     * See {@link ButtonComponent#hideReason(Object)} for a discussion of the return value.
     */
    @Override
	public boolean allow(BoundObject anObject) {
        return false;
    }
    
	@Override
	protected boolean attachCommandModels() {
		boolean result = super.attachCommandModels();
		List<? extends CommandModel> buttons = getButtons();
		if (buttons != null) {
			for (CommandModel button : buttons) {
				attachCommandModel(button);
				result = true;
			}
		}
		return result;
	}

	@Override
	protected boolean detachCommandModels() {
		boolean buttonsDetached = detachButtons(getButtons());
		boolean superDetached = super.detachCommandModels();
		return buttonsDetached || superDetached;
	}

	private boolean detachButtons(List<? extends CommandModel> buttons) {
		boolean detached = false;
		if (buttons != null) {
			for (CommandModel button : buttons) {
				detachCommandModel(button);
				detached = true;
			}
		}
		return detached;
	}

	private Config config() {
		return (Config) _config;
	}

    /**
     * The default height of a button component.
     */
	protected DisplayDimension getDefaultPageHeight() {
		return ThemeFactory.getTheme().getValue(Icons.BUTTON_COMP_HEIGHT);
    }
    
    /**
     * Overridden to add function for check of target and cmd being present.
     */
    @Override
	public void writeJavaScript(String contextPath, TagWriter out,
            HttpServletRequest req) throws IOException {
    	throw new UnsupportedOperationException("In DivLayout this component is rendered inline so there can not be some javascript in header (since there is no header)");
    }
    
    @Override
	public Control getRenderingControl() {
		if (this.buttonBarControl == null) {
    		initControls();
    	}
		return this.buttonBarControl;
    }
    
    public void initControls() {
		this.buttonBarControl = ButtonBarFactory.createButtonBar(this, cssClass);
	}

	@Override
	protected void becomingInvisible() {
    	if (getRenderingControl() != null) {
    		getRenderingControl().detach();
    	}
    	super.becomingInvisible();
    }
    
    public void addModelChangeListener(ModelChangeListener listener) {
		addListener(ModelChangeListener.MODEL_CHANGED, listener);
    }
    
    public void removeModelChangeListener(ModelChangeListener listener) {
		removeListener(ModelChangeListener.MODEL_CHANGED, listener);
    }
    
	private void checkButtonsChange(List<? extends CommandModel> oldButtons) {
		List<? extends CommandModel> theButtons = getButtons();
    	if (Utils.equals(oldButtons, theButtons)){
    		return;
    	}
		// getButtons have changed, detach former buttons.
		detachButtons(oldButtons);
		if (!theButtons.isEmpty()) {
			setHasCommandModelToAttach();
		}
		
		firePropertyChanged(ModelChangeListener.MODEL_CHANGED, this, oldButtons, theButtons);
    }
}
