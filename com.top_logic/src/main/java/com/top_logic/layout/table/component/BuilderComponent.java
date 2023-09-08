/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.basic.component.ControlComponent;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Component, which will get it's model from the configured builder class.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class BuilderComponent extends ControlComponent {
	
    public interface Config extends ControlComponent.Config {

		@Name(MODEL_BUILDER_ELEMENT)
		@Mandatory
		PolymorphicConfiguration<? extends ModelBuilder> getModelBuilder();

		/**
		 * @see #getModelBuilder()
		 */
		void setModelBuilder(PolymorphicConfiguration<? extends ModelBuilder> value);

	}

    /**
	 * Configuration element for setting a {@link #getBuilder()}.
	 */
	public static final String MODEL_BUILDER_ELEMENT = "modelBuilder";

    /** The builder to be used for creating the list displayed. */
	private ModelBuilder _builder;

    /** 
     * Create a new instance of this class from XML.
     */
    public BuilderComponent(InstantiationContext context, Config config) throws ConfigurationException {
        super(context, config);

		_builder = createBuilder(context, config);
    }
    
    /**
	 * The {@link ModelBuilder} decomposing the component's {@link #getModel() model} into the
	 * display aspect of this component.
	 */
	public final ModelBuilder getBuilder() {
		return _builder;
	}

    /**
	 * This will be delegated to the {@link ModelBuilder#supportsModel(Object, LayoutComponent)}.
	 *
	 * @param anObject
	 *        The object to be checked.
	 * @return If the model builder can process the given object.
	 */
    @Override
	protected boolean supportsInternalModel(Object anObject) {
		if (!super.supportsInternalModel(anObject)) {
			return false;
		}
		return _builder.supportsModel(anObject, this);
    }

	/**
     * Well, we will create our builder "later" in our own constructor.
     *  
     * So this method is a hook for subclasses to provide alternate ways to get a model builder.
     * 
     * @return    The builder to be used in this instance, never <code>null</code>.
     */
	protected ModelBuilder createBuilder(InstantiationContext context, Config attr) throws ConfigurationException {
		return context.getInstance(attr.getModelBuilder());
    }
}
