/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure.toolrow;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.component.configuration.ViewConfiguration;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A {@link View}, which can be configured via {@link ViewConfiguration}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class DefaultComponentView implements ViewConfiguration {

	/**
	 * Instantiates a {@link HTMLFragment} from a {@link ViewConfiguration}.
	 * 
	 * @param viewConfiguration
	 *        - from which a {@link View} shall be generated
	 */
	protected static final HTMLFragment toView(LayoutComponent component, ViewConfiguration viewConfiguration) {
		HTMLFragment configuredView = viewConfiguration.createView(component);
		attachCommandModel(configuredView);
		return configuredView;
	}

	/**
	 * Instantiates {@link HTMLFragment}s from {@link ViewConfiguration}s.
	 * 
	 * @see #toView(LayoutComponent, ViewConfiguration)
	 */
	protected static final List<HTMLFragment> toViews(LayoutComponent component,
			List<ViewConfiguration> viewConfigurations) {
		List<HTMLFragment> views = new ArrayList<>();
		for (ViewConfiguration viewConfiguration : viewConfigurations) {
			HTMLFragment configuredView = toView(component, viewConfiguration);
			views.add(configuredView);
		}
		return views;
	}

	private static void attachCommandModel(HTMLFragment view) {
		if (view instanceof ButtonControl) {
			ButtonControl button = (ButtonControl) view;
			attachCommandModel(button.getModel());
		}
		if (view instanceof CompositeControl) {
			List<? extends HTMLFragment> children = ((CompositeControl) view).getChildren();
			for (HTMLFragment child : children) {
				attachCommandModel(child);
			}
		}
	}

	private static void attachCommandModel(CommandModel model) {
		/* This is a actually a hack: When the button registers itself as listener at the command
		 * model, the command model updates its state and fires an event. An event must not be fired
		 * during rendering, so here a dummy listener is registered to ensure that the model is
		 * updated itself during command execution (this can not happen in attach of the control
		 * because this also happens during rendering). **/
		model.addListener(PropertyObservable.GLOBAL_LISTENER_TYPE, GenericPropertyListener.IGNORE_EVENTS);
		/* The listener is removed immediately, otherwise it will never be deregistered because
		 * there is no life cycle. The hope is that the model is now valid during rendering and no
		 * events are fired when the control is attached. */
		model.removeListener(PropertyObservable.GLOBAL_LISTENER_TYPE, GenericPropertyListener.IGNORE_EVENTS);
	}
}
