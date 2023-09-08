/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.util.Set;
import java.util.stream.Stream;

import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;

/**
 * Adapter for {@link ModelEventListener} that dispatches to separate methods by event type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ModelEventAdapter implements ModelEventListener, ModelListener {

	/**
	 * Dispatches to the type specific handler methods.
	 * 
	 * @see #receiveAnyModelEvent(Object, Object, int)
	 * @see #receiveDialogOpenedEvent(Object, Object)
	 * @see #receiveDialogClosedEvent(Object, Object)
	 * @see #receiveWindowOpenedEvent(Object, Object)
	 * @see #receiveWindowClosedEvent(Object, Object)
	 * @see #receiveGlobalRefreshEvent(Object, Object)
	 * @see #receiveModelChangedEvent(Object, Object)
	 * @see #receiveModelCreatedEvent(Object, Object)
	 * @see #receiveModelDeletedEvent(Set, Object)
	 * @see #receiveModelSecurityChangedEvent(Object)
	 * 
	 * @see ModelEventListener#handleModelEvent(Object, java.lang.Object, int)
	 */
	@Override
	public final void handleModelEvent(Object model, Object changedBy, int eventType) {
		receiveAnyModelEvent(model, changedBy, eventType);

		switch (eventType) {
			case ModelEventListener.MODEL_CREATED:
				receiveModelCreatedEvent(model, changedBy);
				break;
			case ModelEventListener.MODEL_MODIFIED:
				receiveModelChangedEvent(model, changedBy);
				break;
			case ModelEventListener.MODEL_DELETED:
				@SuppressWarnings("unchecked")
				Set<TLObject> deletedObjects = (Set<TLObject>) model;
				receiveModelDeletedEvent(deletedObjects, changedBy);
				break;
			case ModelEventListener.SECURITY_CHANGED:
				receiveModelSecurityChangedEvent(changedBy);
				break;
			case ModelEventListener.DIALOG_OPENED:
				receiveDialogOpenedEvent(model, changedBy);
				break;
			case ModelEventListener.DIALOG_CLOSED:
				receiveDialogClosedEvent(model, changedBy);
				break;
			case ModelEventListener.WINDOW_OPENED:
				receiveWindowOpenedEvent(model, changedBy);
				break;
			case ModelEventListener.WINDOW_CLOSED:
				receiveWindowClosedEvent(model, changedBy);
				break;
			case ModelEventListener.GLOBAL_REFRESH:
				receiveGlobalRefreshEvent(model, changedBy);
				break;
		}
	}

	/**
	 * Signal, that something happened with a model.
	 * 
	 * You shouldn't use this! Use better the concrete events. This method will be deleted in
	 * future.
	 * 
	 * @param model
	 *        the model ha changed in any manner.
	 * @param changedBy
	 *        The modifier called this method.
	 * @param aType
	 *        the type of the event. See {@link ModelEventListener}
	 * @return <code>true</code> to indicate that this part (or subparts) have become invalid.
	 */
	protected boolean receiveAnyModelEvent(Object model, Object changedBy, int aType) {
		return false;
	}

	/**
	 * Signal, that a model has been created new.
	 * 
	 * This method will normally be called, when a component created a new object in the
	 * application.
	 * 
	 * @param model
	 *        The new model created, may be <code>null</code>.
	 * @param changedBy
	 *        The modifier called this method.
	 * @return <code>true</code> to indicate that this part (or subparts) have become invalid.
	 * 
	 * @deprecated Use {@link ModelListener}
	 */
	@Deprecated
	protected boolean receiveModelCreatedEvent(Object model, Object changedBy) {
		return (false);
	}

	/**
	 * Signal, that the content of a model has changed.
	 * 
	 * This method will normally be called, when a component has changed the content of the given
	 * model.
	 * 
	 * @param model
	 *        The changed model.
	 * @param changedBy
	 *        The modifier called this method.
	 * @return <code>true</code> to indicate that this part (or subparts) have become invalid.
	 * 
	 * @deprecated Use {@link ModelListener}
	 */
	@Deprecated
	protected boolean receiveModelChangedEvent(Object model, Object changedBy) {
		return false;
	}

	/**
	 * Signal, that a models has been deleted.
	 * 
	 * This method will normally be called, when a component has deleted an object.
	 * 
	 * @param models
	 *        The rest information of the deleted models.
	 * @param changedBy
	 *        The modifier called this method.
	 * @return <code>true</code> to indicate that this part (or subparts) have become invalid.
	 * 
	 * @deprecated Use {@link ModelListener}
	 */
	@Deprecated
	protected boolean receiveModelDeletedEvent(Set<TLObject> models, Object changedBy) {
		return (false);
	}

	/**
	 * Signal, that security has changed.
	 * 
	 * @param changedBy
	 *        The modifier called this method.
	 * @return <code>true</code> to indicate that this part (or subparts) have become invalid.
	 */
	protected boolean receiveModelSecurityChangedEvent(Object changedBy) {
		return (false);
	}

	/**
	 * Signal that a global refresh (F5) was received.
	 * 
	 * @param model
	 *        See {@link #handleModelEvent(Object, Object, int)}.
	 * @param changedBy
	 *        See {@link #handleModelEvent(Object, Object, int)}.
	 */
	protected boolean receiveGlobalRefreshEvent(Object model, Object changedBy) {
		return false;
	}

	/**
	 * Signal that a window has been opened.
	 * 
	 * @param model
	 *        See {@link #handleModelEvent(Object, Object, int)}.
	 * @param changedBy
	 *        See {@link #handleModelEvent(Object, Object, int)}.
	 */
	protected boolean receiveWindowOpenedEvent(Object model, Object changedBy) {
		return false;
	}

	/**
	 * Signal that a window has been closed.
	 * 
	 * @param model
	 *        See {@link #handleModelEvent(Object, Object, int)}.
	 * @param changedBy
	 *        See {@link #handleModelEvent(Object, Object, int)}.
	 */
	protected boolean receiveWindowClosedEvent(Object model, Object changedBy) {
		return false;
	}

	/**
	 * Signal that a dialog has been opened.
	 * 
	 * @param model
	 *        See {@link #handleModelEvent(Object, Object, int)}.
	 * @param changedBy
	 *        See {@link #handleModelEvent(Object, Object, int)}.
	 */
	protected boolean receiveDialogOpenedEvent(Object model, Object changedBy) {
		return false;
	}

	/**
	 * Signal that a dialog has been opened.
	 * 
	 * @param model
	 *        See {@link #handleModelEvent(Object, Object, int)}.
	 * @param changedBy
	 *        See {@link #handleModelEvent(Object, Object, int)}.
	 */
	protected boolean receiveDialogClosedEvent(Object model, Object changedBy) {
		return false;
	}

	@Override
	public void notifyChange(ModelChangeEvent event) {
		handleTLObjectDeletions(event.getDeleted());
		handleTLObjectUpdates(event.getUpdated());
		handleTLObjectCreations(event.getCreated());
	}

	/**
	 * Hook for subclasses that need to be informed about deletions of {@link TLObject}s.
	 * <p>
	 * The default implementation dispatches the events to the legacy
	 * {@link #receiveModelDeletedEvent(Set, Object)} hook. Subclasses should override this method,
	 * <em>not</em> call "super" and not use the legacy hook in any way.
	 * </p>
	 */
	protected void handleTLObjectDeletions(Stream<? extends TLObject> deleted) {
		Set<TLObject> deletions = deleted.collect(toSet());
		if (!deletions.isEmpty()) {
			receiveModelDeletedEvent(unmodifiableSet(deletions), null);
		}
	}

	/**
	 * Hook for subclasses that need to be informed about updates of {@link TLObject}s.
	 * <p>
	 * The default implementation dispatches the events to the legacy
	 * {@link #receiveModelChangedEvent(Object, Object)} hook. Subclasses should override this
	 * method, <em>not</em> call "super" and not use the legacy hook in any way.
	 * </p>
	 */
	protected void handleTLObjectUpdates(Stream<? extends TLObject> updated) {
		updated.forEach(tlObject -> receiveModelChangedEvent(tlObject, null));
	}

	/**
	 * Hook for subclasses that need to be informed about creations of {@link TLObject}s.
	 * <p>
	 * The default implementation dispatches the events to the legacy
	 * {@link #receiveModelCreatedEvent(Object, Object)} hook. Subclasses should override this
	 * method, <em>not</em> call "super" and not use the legacy hook in any way.
	 * </p>
	 */
	protected void handleTLObjectCreations(Stream<? extends TLObject> created) {
		created.forEach(tlObject -> receiveModelCreatedEvent(tlObject, null));
	}

}
