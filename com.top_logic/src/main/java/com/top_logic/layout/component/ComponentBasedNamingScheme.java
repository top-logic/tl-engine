/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.layout.scripting.recorder.ref.ui.LayoutComponentResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.QualifiedComponentNameConstraint;

/**
 * {@link ModelNamingScheme} for UI models that are contained in {@link LayoutComponent}s.
 */
public abstract class ComponentBasedNamingScheme<M extends NamedModel, N extends ComponentBasedNamingScheme.ComponentBasedName>
		extends AbstractModelNamingScheme<M, N> implements LayoutComponentResolver {

	/**
	 * Identifier for a {@link LayoutComponent}
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	@Abstract
	public interface ComponentBasedName extends ModelName {

		/**
		 * The {@link LayoutComponent#getName() name} of the referenced {@link LayoutComponent}.
		 * 
		 * @see LayoutComponent#getName()
		 */
		@Mandatory
		@Constraint(QualifiedComponentNameConstraint.class)
		com.top_logic.mig.html.layout.ComponentName getComponentName();

		/**
		 * Sets the {@link #getComponentName()} property.
		 */
		void setComponentName(com.top_logic.mig.html.layout.ComponentName value);

		/**
		 * E.g. the name of the class that is supposed to implements the component.
		 * 
		 * <p>
		 * This property is optional and only for debugging purpose. The value of this property must
		 * not be used for the action implementation.
		 * </p>
		 */
		String getComponentImplementationComment();

		/** @see #getComponentImplementationComment() */
		void setComponentImplementationComment(String value);

		/**
		 * Whether the component is required to be visible when it is resolved.
		 */
		@BooleanDefault(true)
		boolean getRequireVisible();

		/**
		 * @see #getRequireVisible()
		 */
		void setRequireVisible(boolean value);
	}

	public interface ComponentName extends ComponentBasedName {
		// Pure marker interface.
	}

	/**
	 * Creates a new {@link ComponentBasedNamingScheme}.
	 * 
	 * @see AbstractModelNamingScheme#AbstractModelNamingScheme(Class, Class)
	 */
	protected ComponentBasedNamingScheme(Class<M> modelClass, Class<? extends N> nameClass) {
		super(modelClass, nameClass);
	}

	@Override
	protected void initName(N name, M model) {
		LayoutComponent component = getContextComponent(model);

		name.setComponentName(component.getName());
		name.setRequireVisible(component.isVisible());

		if (ScriptingRecorder.recordImplementationDetails()) {
			name.setComponentImplementationComment(component.getClass().getName());
		}
	}

	/**
	 * Returns the corresponding {@link LayoutComponent} of the model. (The model itself can be the
	 * LayoutComponent.)
	 */
	protected abstract LayoutComponent getContextComponent(M model);

	/**
	 * Locates the {@link LayoutComponent} identified by the given {@link ComponentName} in the
	 * given {@link TLSessionContext}.
	 */
	protected LayoutComponent locateComponent(ActionContext context, ComponentBasedName name) {
		LayoutComponent componentRoot = context.getDisplayContext().getLayoutContext().getMainLayout();
		com.top_logic.mig.html.layout.ComponentName componentName = name.getComponentName();
		LayoutComponent component = locateComponent(name, componentRoot, componentName);
		if (name.getRequireVisible()) {
			checkVisible(context, name, component);
		}
		return component;
	}

	/**
	 * Find the component with the given name relative to the given component root.
	 * 
	 * @param config
	 *        The currently processed configuration.
	 * @param componentRoot
	 *        The component root.
	 * @param componentName
	 *        The name of the searched component.
	 * @return The component with the given name.
	 */
	public static LayoutComponent locateComponent(final ConfigurationItem config, LayoutComponent componentRoot,
			final com.top_logic.mig.html.layout.ComponentName componentName) {
		/**
		 * Search the complete component tree for a given name to detect components with duplicate
		 * names.
		 * 
		 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
		 */
		class ComponentSearch extends DefaultDescendingLayoutVisitor {
			private LayoutComponent result;

			@Override
			public boolean visitLayoutComponent(LayoutComponent aComponent) {
				if (componentName.equals(aComponent.getName())) {
					if (result != null) {
						ApplicationAssertions.fail(config,
							"Multiple components with same name '" + componentName + "'.");
					}
					result = aComponent;
				}
				return super.visitLayoutComponent(aComponent);
			}

			public LayoutComponent getResult() {
				return result;
			}
		}
		if (componentName.equals(componentRoot.getName())) {
			return componentRoot;
		}
		ComponentSearch search = new ComponentSearch();
		componentRoot.visitChildrenRecursively(search);
		LayoutComponent component = search.getResult();
		ApplicationAssertions.assertNotNull(config,
			"Component '" + componentName + "' cannot be resolved.", component);
		return component;
	}

}
