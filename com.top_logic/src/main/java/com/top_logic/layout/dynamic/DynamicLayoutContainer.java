/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.dynamic;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;

/**
 * {@link LayoutContainer} selecting a layout specific to the current business model using a
 * {@link LayoutResolver}.
 * 
 * @see Config#getLayoutResolver()
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class DynamicLayoutContainer extends LayoutContainer implements DynamicFrame, SelectionModelOwner {

    public interface Config extends LayoutContainer.Config {
		@Name(XML_CONF_KEY_LAYOUT_RESOLVER)
		@InstanceDefault(DummyLayoutResolver.class)
		@InstanceFormat
		LayoutResolver getLayoutResolver();

		@Name(XML_CONF_KEY_LAYOUT_MODEL_MAPPER)
		String getModelMapper();

		@Name(XML_CONF_KEY_DEFAULT_LAYOUT)
		@StringDefault(DEFAULT_DEFAULT_LAYOUT)
		String getDefaultLayout();

		@Name(XML_CONF_KEY_NULL_LAYOUT)
		@StringDefault(DEFAULT_NULL_LAYOUT)
		String getNullLayout();

		@ListDefault({})
		@Override
		List<? extends LayoutComponent.Config> getChildConfigurations();
	}

	public static final String XML_CONF_KEY_LAYOUT_RESOLVER = "layoutResolver";
    public static final String XML_CONF_KEY_LAYOUT_MODEL_MAPPER = "modelMapper";
    public static final String XML_CONF_KEY_DEFAULT_LAYOUT      = "defaultLayout";
    public static final String XML_CONF_KEY_NULL_LAYOUT         = "nullLayout";

    /** Layout to show when nothing is selected */
    private static final String DEFAULT_NULL_LAYOUT 
        = "ewe/default/defaultWorklistNothingSelected.xml";

    /** Layout to show as default (TODO TSA ?) */
    private static final String DEFAULT_DEFAULT_LAYOUT 
        = "ewe/default/defaultWorklistTask.xml";

    /** the layout model */
    private DynamicLayoutModel layoutModel;

    private String xmlLayoutModelMapper;
    
    
    /**
     * A mapping to map the current model to a model fitting the layout resolver
     */
    private Mapping mapping;
    
	/**
	 * Creates a {@link DynamicLayoutContainer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param atts
	 *        The configuration.
	 */
	@CalledByReflection
	public DynamicLayoutContainer(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        
		LayoutResolver layoutResolver = atts.getLayoutResolver();
        String  theMappingClass   = StringServices.nonEmpty(atts.getModelMapper());
        Mapping theMapper         = theMappingClass == null 
                                  ? Mappings.identity() 
                                  : (Mapping) getInstanceOfClass(theMappingClass, "Invalid mapping class configured");
        String  theDefaultLayout  = atts.getDefaultLayout();
        String  theNullLayout     = atts.getNullLayout();
        
        this.mapping              = theMapper;
		this.layoutModel = new DynamicLayoutModel(layoutResolver, theDefaultLayout, theNullLayout, this);
        this.xmlLayoutModelMapper = theMappingClass; 
    }

    private Object getInstanceOfClass(String theResolverClassName, String anErrorMessage)
			throws ConfigurationException {
        try {
            return Class.forName(theResolverClassName).newInstance();
        }
        catch (Exception e) {
			throw new ConfigurationException(anErrorMessage + ": " + theResolverClassName, e);
        }
    }

    /**
     * We write the framese on our own, so
     * 
     * @return always <code>true</code>
     */
    @Override
	public boolean isCompleteRenderer() {
        return true;
    }

    @Override
	public List<LayoutComponent> getChildList() {
        return this.layoutModel.getComponents();
    }

	@Override
	public int getChildCount() {
		return this.layoutModel.getSize();
	}

	@Override
	public void setChildren(List<LayoutComponent> newChildren) {
		// Can not be set directly.
	}

    @Override
	public Iterable<? extends LayoutComponent> getVisibleChildren() {
		return Collections.singleton(this.layoutModel.getCurrentComponent());
    }
    
	/**
	 * Informs this component about the change of the children of the base model.
	 * 
	 * @param oldChildren
	 *        The components before the children change.
	 */
	void notifyNewChildren(List<LayoutComponent> oldChildren) {
		fireChildrenChanged(oldChildren);
	}

	/**
	 * This method returns the layoutModel.
	 */
    public DynamicLayoutModel getLayoutModel() {
        return (this.layoutModel);
    }

    /**
     * @see com.top_logic.mig.html.layout.LayoutContainer#isOuterFrameset()
     */
    @Override
	public boolean isOuterFrameset() {
        return true;
    }

    /**
     * We do not support makeVisible from a child component, 
     * the visible child is determined by the model of htis component.
     * 
     * @see com.top_logic.mig.html.layout.LayoutContainer#makeVisible(com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public boolean makeVisible(LayoutComponent aChild) {
        return false;
    }

	@Override
	protected void setChildVisibility(boolean visible) {
		if (!visible) {
			for (LayoutComponent child : getChildList()) {
				if (child.isVisible()) {
					child.setVisible(false);
				}
			}
		}
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(mapping.map(null));
		}
		return super.validateModel(context);
    }

	@Override
	protected void handleNewModel(Object newModel) {
		super.handleNewModel(newModel);
		this.layoutModel.adaptModel(newModel, this);
	}
    
    /**
     * @see com.top_logic.mig.html.layout.LayoutComponent#becomingVisible()
     */
    @Override
	protected void becomingVisible() {
        super.becomingVisible();
        LayoutComponent theCurrentComponent = this.layoutModel.getCurrentComponent();
        if (theCurrentComponent != null) {
            theCurrentComponent.setVisible(true);
        }
    }
    
    @Override
	protected void componentsResolved(InstantiationContext context) {
		super.componentsResolved(context);
		/* Initialise model after children are resolved, because initialising adds default and null
		 * layout to container _and_ resolves them. Therefore children would be initialised twice. */
		this.layoutModel.init(this);
    }

	@Override
	public SelectionModel getSelectionModel() {
		return layoutModel;
	}

}

