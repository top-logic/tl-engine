/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;


import java.io.IOException;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.component.ControlComponent;
import com.top_logic.layout.template.NoSuchPropertyException;

/**
 * A control is the controller (and the view-backend) of an active element displayed on a
 * {@link ControlComponent} page.
 * 
 * <p>
 * A {@link Control} can accept {@link ControlComponent commands} that are sent from the client-side
 * in response to user interactions with the control's client-side view (see
 * {@link CommandListenerRegistry#addCommandListener(CommandListener)}). Furthermore, a
 * {@link Control} can update its client-side view by issuing {@link ClientAction}s, if its state
 * (its model) changes (see {@link ControlScope#addUpdateListener(UpdateListener)}).
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Control extends View, TemplateVariables {

	/**
	 * The client-side ID of this {@link Control}.
	 * 
	 * <p>
	 * The identifier must be used as ID attribute of the top-level tag that represents this
	 * {@link Control}. The identifier of a {@link Control} must be unique within its
	 * {@link ControlComponent}.
	 * </p>
	 */
	@TemplateVariable(ID)
	public String getID();

	/**
	 * Writes this {@link Control}'s initial display to the given stream.
	 * 
	 * <p>
	 * After a {@link Control} is displayed, it is registered as
	 * {@link CommandListener} and {@link UpdateListener} to the
	 * {@link DisplayContext#getExecutionScope()}. Additionally, it is
	 * {@link #isAttached() attached} to its model and listens for
	 * updates, which it translates into {@link ClientAction}s for updating its
	 * client-side view. Model changes result in incremental updates that are
	 * transported to the client in response to
	 * {@link UpdateListener#revalidate(DisplayContext, UpdateQueue)} calls.
	 * </p>
	 * 
	 * <p>
	 * To allow generic redraws of a {@link Control}, the write method must
	 * produce a single HTML element with the {@link #getID()} of this
	 * {@link Control}.
	 * </p>
	 * 
	 * <p>
	 * A {@link Control} is displayed, at the time this {@link Control}'s page
	 * is created, or at the time, a {@link CompositeControl} decides about
	 * displaying (some of) its children.
	 * </p>
	 * 
	 * <p>
	 * This {@link View} method was re-declared in {@link Control} to adapt the
	 * documentation.
	 * </p>
	 * 
	 * @see View#write(DisplayContext, TagWriter)
	 */
	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException;
	
	/**
	 * Detaches this {@link Control} from its model.
	 * 
	 * <p>
	 * Detaching a control stops it from listening for changes to its model. 
	 * </p>
	 * 
	 * @return Whether the detachment operation was actually performed.
	 * 
	 * @see #isAttached()
	 */
	public boolean detach();
	
	/**
	 * Whether this {@link Control} is currently attached.
	 * 
	 * <p>
	 * A {@link Control} must not perform any actions upon changes in its model
	 * before {@link #isAttached()} returns true.
	 * </p>
	 */
	public boolean isAttached();

	/**
	 * Whether the {@link ControlScope scope} of this control is disabled or not.
	 * 
	 * @see ControlScope#isScopeDisabled()
	 */
	boolean isViewDisabled();
	
	/**
	 * The model element this {@link Control} displays.
	 * 
	 * @return The displayed model, or <code>null</code>, if this control has no model.
	 */
	Object getModel();

	@Override
	default Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
		return TemplateVariables.super.getPropertyValue(propertyName);
	}

	@Override
	default Control self() {
		return this;
	}

}
