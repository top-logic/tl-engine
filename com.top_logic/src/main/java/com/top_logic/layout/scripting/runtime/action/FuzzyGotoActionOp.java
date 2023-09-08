/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ui.LayoutComponentResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;

/**
 * {@link ApplicationActionOp} that makes a "goto" to a fuzzy described target component.
 * 
 * @see FuzzyGoto
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FuzzyGotoActionOp extends AbstractApplicationActionOp<FuzzyGotoActionOp.FuzzyGoto> {

	/** Config interface for the {@link FuzzyGotoActionOp}. */
	public interface FuzzyGoto extends ApplicationAction {

		@ClassDefault(FuzzyGotoActionOp.class)
		@Override
		Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

		/**
		 * The component to go to or a parent of it.
		 * 
		 * @see #getTargetObject() for details.
		 */
		ModelName getComponent();

		/** @see #getComponent() */
		void setComponent(ModelName value);

		/**
		 * The model for the target component. Is used to identify the target component of this
		 * {@link FuzzyGoto}.
		 * 
		 * <p>
		 * If this is <code>null</code> or resoves to <code>null</code> {@link #getComponent()} is
		 * the target component.
		 * </p>
		 * 
		 * <p>
		 * The target component is either {@link #getComponent()} if it
		 * {@link LayoutComponent#supportsModel(Object) accepts} this target object, or the child
		 * which does. The children are searched recursively: For all {@link LayoutContainer}s,
		 * every child is searched. Except for {@link TabComponent} for which only the selected
		 * child is searched.
		 * </p>
		 * 
		 * <p>
		 * The target component must {@link LayoutComponent#supportsModel(Object) support} this
		 * target object.
		 * </p>
		 * 
		 * <p>
		 * If there is more than one component which would accept this target object, it is
		 * unspecified which one is found. If no one accepts this target object, an
		 * {@link ApplicationAssertion} is thrown.
		 * </p>
		 * 
		 * @see LayoutComponent#supportsModel(Object) Used to select the target component.
		 */
		ModelName getTargetObject();

		/** @see #getTargetObject() */
		void setTargetObject(ModelName value);

	}

	private static final LayoutComponent NO_COMPONENT_FOUND = null;

	/**
	 * Creates a {@link FuzzyGotoActionOp} from a {@link FuzzyGoto}.
	 * <p>
	 * Is called by the {@link TypedConfiguration}.
	 * </p>
	 */
	@CalledByReflection
	public FuzzyGotoActionOp(InstantiationContext context, FuzzyGoto config) {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) {
		boolean before = LayoutComponentResolver.allowResolvingHiddenComponents(context, true);
		LayoutComponent targetComponent;
		try {
			LayoutComponent layout = (LayoutComponent) ModelResolver.locateModel(context, config.getComponent());
			targetComponent = findTargetComponent(context, layout);
		} finally {
			LayoutComponentResolver.allowResolvingHiddenComponents(context, before);
		}
		boolean success = targetComponent.makeVisible();
		ApplicationAssertions.assertTrue(config, "Component '" + targetComponent.getName() + "' cannot be displayed.",
			success);
		return argument;
	}

	private LayoutComponent findTargetComponent(ActionContext context, LayoutComponent layout) {
		ModelName targetObjectName = config.getTargetObject();
		if (targetObjectName == null) {
			return layout;
		}
		Object targetObject = context.resolve(targetObjectName);
		LayoutComponent targetComponent = findTargetComponent(layout, targetObject);
		if (targetComponent == NO_COMPONENT_FOUND) {
			throw ApplicationAssertions.fail(config, "No component accepts the model "
				+ StringServices.getObjectDescription(targetObject) + ".");
		}
		return targetComponent;
	}

	private LayoutComponent findTargetComponent(LayoutComponent component, Object targetObject) {
		if (component instanceof LayoutContainer) {
			return findTargetComponent((LayoutContainer) component, targetObject);
		}

		if (component.supportsModel(targetObject)) {
			component.setModel(targetObject);
			return component;
		}
		
		return NO_COMPONENT_FOUND;
	}

	private LayoutComponent findTargetComponent(LayoutContainer container, Object targetObject) {
		for (LayoutComponent child : container.getChildList()) {
			LayoutComponent result = findTargetComponent(child, targetObject);
			if (result != NO_COMPONENT_FOUND) {
				return result;
			}
		}

		return NO_COMPONENT_FOUND;
	}

}

