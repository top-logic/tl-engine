/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.create;

import java.util.Map;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.core.util.ElementEventUtil;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;


/**
 * Create a new {@link TLObject} in the context of e.g. a {@link CreateFormBuilder}.
 * 
 * @see #linkNewObject(TLObject, TLObject, Object)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class GenericCreateHandler extends AbstractCreateCommandHandler {

	/**
	 * Configuration options for {@link GenericCreateHandler}.
	 */
	public interface Config extends AbstractCreateCommandHandler.Config {

		// Defaults for the in-app dialog configuration UI.

		@Override
		@FormattedDefault("tl.command.genericCreate")
		ResKey getResourceKey();

	}

	/**
	 * Creates a {@link GenericCreateHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
    public GenericCreateHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
			Map<String, Object> arguments) {

		AttributeFormContext formContext = (AttributeFormContext) formContainer.getFormContext();
		formContext.store();

		TLFormObject creation = formContext.getAttributeUpdateContainer().getOverlay(null, null);
		TLObject newObject = creation.getEditedObject();
		TLObject container = creation.tContainer();

		linkNewObject(container, newObject, createContext);

		sendEvent(newObject);
		return newObject;
    }

	/**
	 * Links the newly created object to it's create context.
	 * 
	 * @param container
	 *        The container for the new object.
	 * @param newObject
	 *        The newly allocated object.
	 * @param model
	 *        The object this handler is executed on. Normally, this is the create component's
	 *        model, which defines the context in which the creation happened.
	 */
	protected abstract void linkNewObject(TLObject container, TLObject newObject, Object model);

    /**
	 * Sends a {@link MonitorEvent#CREATED} {@link MonitorEvent} to the bus announcing the newly
	 * created object.
	 * 
	 * @param newObject
	 *        the newly created object.
	 */
	public void sendEvent(TLObject newObject) {
		ElementEventUtil.sendEvent(newObject, MonitorEvent.CREATED);
    }
    
}
