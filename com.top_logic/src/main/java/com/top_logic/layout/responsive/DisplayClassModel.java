/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.responsive;

import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.NoBubblingEventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.util.TLContextManager;

/**
 * Per-subsession (per browser tab) holder of the current {@link DisplayClass}.
 *
 * <p>
 * The value is updated when the client reports a viewport breakpoint change (via the app shell's
 * {@code reportDisplayClass} command) and observed by adaptive UI controls so that they switch
 * presentation when the available space changes (e.g. a desktop window resized across the
 * breakpoint, a tablet rotated). Controls subscribe to {@link #DISPLAY_CLASS} on attach and
 * unsubscribe on detach.
 * </p>
 *
 * <p>
 * The model is stored per {@link SubSessionContext} rather than per login session because two
 * browser tabs of the same user may have different viewport sizes.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DisplayClassModel extends PropertyObservableBase {

	/**
	 * {@link Property} under which the per-subsession {@link DisplayClassModel} is stored on the
	 * {@link SubSessionContext}.
	 */
	private static final Property<DisplayClassModel> MODEL_PROPERTY =
		TypedAnnotatable.property(DisplayClassModel.class, "displayClassModel");

	/**
	 * Listener notified when the {@link DisplayClass} held by a {@link DisplayClassModel} changes.
	 */
	public interface DisplayClassListener extends PropertyListener {

		/**
		 * Handles a change of the observed {@link DisplayClass}.
		 *
		 * @param sender
		 *        The model whose value changed.
		 * @param oldValue
		 *        The previous {@link DisplayClass}.
		 * @param newValue
		 *        The new {@link DisplayClass}.
		 */
		void handleDisplayClassChanged(DisplayClassModel sender, DisplayClass oldValue, DisplayClass newValue);
	}

	/**
	 * {@link EventType} fired when the {@link #getDisplayClass() display class} changes.
	 */
	public static final EventType<DisplayClassListener, DisplayClassModel, DisplayClass> DISPLAY_CLASS =
		new NoBubblingEventType<>("displayClass") {
			@Override
			protected void internalDispatch(DisplayClassListener listener, DisplayClassModel sender,
					DisplayClass oldValue, DisplayClass newValue) {
				listener.handleDisplayClassChanged(sender, oldValue, newValue);
			}
		};

	private DisplayClass _displayClass = DisplayClass.DEFAULT;

	/**
	 * The {@link DisplayClassModel} of the current subsession, creating it on first access.
	 *
	 * @return The current subsession's model, never {@code null}.
	 */
	public static DisplayClassModel forCurrentSubSession() {
		return forSubSession(TLContextManager.getSubSession());
	}

	/**
	 * The {@link DisplayClassModel} of the given subsession, creating it on first access.
	 *
	 * @param subSession
	 *        The subsession to read from; may be {@code null} (e.g. during background processing),
	 *        in which case a transient, default-valued model is returned.
	 * @return The subsession's model, never {@code null}.
	 */
	public static DisplayClassModel forSubSession(SubSessionContext subSession) {
		if (subSession == null) {
			return new DisplayClassModel();
		}
		synchronized (subSession) {
			DisplayClassModel model = subSession.get(MODEL_PROPERTY);
			if (model == null) {
				model = new DisplayClassModel();
				subSession.set(MODEL_PROPERTY, model);
			}
			return model;
		}
	}

	/**
	 * The current {@link DisplayClass}, never {@code null}.
	 */
	public DisplayClass getDisplayClass() {
		return _displayClass;
	}

	/**
	 * Updates the {@link #getDisplayClass() display class}, notifying {@link DisplayClassListener}s
	 * if the value changed.
	 *
	 * @param newValue
	 *        The new value; {@code null} is treated as {@link DisplayClass#DEFAULT}.
	 * @return Whether the value actually changed.
	 */
	public boolean setDisplayClass(DisplayClass newValue) {
		DisplayClass effective = newValue == null ? DisplayClass.DEFAULT : newValue;
		DisplayClass oldValue = _displayClass;
		if (oldValue == effective) {
			return false;
		}
		_displayClass = effective;
		notifyListeners(DISPLAY_CLASS, this, oldValue, effective);
		return true;
	}

}
