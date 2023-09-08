/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.ReferenceResolver;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.func.Identity;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Base class for a part of the component tree that itself depends on the context component or some
 * named component.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ComponentBased {

	/**
	 * Configuration options for {@link ComponentBased}.
	 */
	public interface Config<I extends ComponentBased> extends PolymorphicConfiguration<I> {

		/** @see #getComponentName() */
		String COMPONENT_NAME = "componentName";

		/** @see #getModelMapping() */
		String MODEL_MAPPING = "modelMapping";

		/**
		 * The name of the component from which the model should be used for filtering.
		 * 
		 * @see ComponentBased#getComponent()
		 */
		@Name(COMPONENT_NAME)
		ComponentName getComponentName();

		/** @see #getComponentName() */
		void setComponentName(ComponentName value);

		/**
		 * An optional {@link Mapping} to apply to the component's model.
		 */
		@Name(MODEL_MAPPING)
		@InstanceFormat
		@InstanceDefault(Identity.class)
		Mapping<Object, Object> getModelMapping();

	}

	LayoutComponent _component;

	private final Mapping<Object, Object> _modelMapping;

	/**
	 * Creates a {@link ComponentBased} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ComponentBased(InstantiationContext context, Config<?> config) {
		ComponentName id = config.getComponentName();
		context.resolveReference(id == null ? InstantiationContext.OUTER : id, LayoutComponent.class,
			new ReferenceResolver<LayoutComponent>() {
				@Override
				public void setReference(LayoutComponent value) {
					_component = value;
				}
			});
		_modelMapping = config.getModelMapping();
	}

	/**
	 * The context component.
	 * 
	 * @see Config#getComponentName()
	 */
	public LayoutComponent getComponent() {
		return _component;
	}

	/**
	 * The {@link Mapping} to apply to the {@link #getComponent() component's} model.
	 * 
	 * @see #getModel()
	 */
	public Mapping<Object, Object> getModelMapping() {
		return _modelMapping;
	}

	/**
	 * The context {@link #getComponent() component's} model.
	 */
	public Object getModel() {
		return _modelMapping.map(getComponent().getModel());
	}

}
