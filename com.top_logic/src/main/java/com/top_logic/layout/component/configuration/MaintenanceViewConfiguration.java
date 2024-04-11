/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.form.control.MaintenanceControl;
import com.top_logic.mig.html.layout.DynamicValidationObserver;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.ValidationListener;
import com.top_logic.util.TLContext;

/**
 * The class {@link MaintenanceViewConfiguration} can be used to configure a
 * {@link MaintenanceControl} as additional {@link View}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MaintenanceViewConfiguration implements ViewConfiguration {

	/**
	 * Singleton {@link MaintenanceViewConfiguration} instance.
	 */
	public static final MaintenanceViewConfiguration INSTANCE = new MaintenanceViewConfiguration();

	private MaintenanceViewConfiguration() {
		// Singleton constructor.
	}

	@Override
	public HTMLFragment createView(LayoutComponent component) {
		final MaintenanceStateModel model = new MaintenanceStateModel(MaintenanceWindowManager.getInstance());
		MaintenanceControl display = new MaintenanceControl(model);
		display.addListener(AbstractControlBase.ATTACHED_PROPERTY, new DynamicValidationObserver(component, model));
		return display;
	}

	/**
	 * Observable view of the {@link MaintenanceWindowManager#getMaintenanceModeState()}.
	 */
	public static class MaintenanceStateModel extends PropertyObservableBase implements ValidationListener {

		/**
		 * Listener being informed when {@link MaintenanceWindowManager#getMaintenanceModeState()}
		 * changes.
		 */
		public interface MaintenanceStateChangedListener extends PropertyListener {

			/**
			 * Event key for registering {@link MaintenanceStateChangedListener}s.
			 * 
			 * @see PropertyObservableBase#addListener(EventType, PropertyListener)
			 */
			EventType<MaintenanceStateChangedListener, MaintenanceStateModel, Integer> STATE_PROPERTY =
				new EventType<>("state") {
				@Override
				public com.top_logic.basic.listener.EventType.Bubble dispatch(
						MaintenanceStateChangedListener listener,
						MaintenanceStateModel sender, Integer oldValue, Integer newValue) {
					listener.handleStateChange(sender, oldValue.intValue(), newValue.intValue());
					return Bubble.BUBBLE;
				}
			};

			/**
			 * Informs about a state change of
			 * {@link MaintenanceWindowManager#getMaintenanceModeState()}
			 * 
			 * @param model
			 *        The sending model.
			 * @param oldState
			 *        The previous state.
			 * @param newState
			 *        the new state.
			 */
			void handleStateChange(MaintenanceStateModel model, int oldState, int newState);

		}

		/** the model whose state this controls shows */
		private final MaintenanceWindowManager _manager;

		/** the inner copy of the state of the model */
		private int _lastState;

		/**
		 * Creates a {@link MaintenanceViewConfiguration.MaintenanceStateModel}.
		 * 
		 */
		public MaintenanceStateModel(MaintenanceWindowManager manager) {
			_manager = manager;
			_lastState = manager.getMaintenanceModeState();
		}

		@Override
		public void doValidateModel(DisplayContext context, LayoutComponent component) {
			int newState = _manager.getMaintenanceModeState();
			int oldState = _lastState;

			boolean isChanged = newState != oldState;
			if (isChanged) {
				_lastState = newState;
				notifyListeners(MaintenanceStateChangedListener.STATE_PROPERTY, this, oldState, newState);
			}
		}

		/**
		 * The maintenance state.
		 * 
		 * @see MaintenanceWindowManager#getMaintenanceModeState()
		 */
		public int getState() {
			return _lastState;
		}

		/**
		 * Whether the last change was issued from the current session.
		 */
		public boolean isChangingContext() {
			return _manager.isChangingContext(TLContext.getContext());
		}

		/**
		 * @see MaintenanceWindowManager#getMessage()
		 */
		public String getMessage() {
			return _manager.getMessage();
		}

		/**
		 * @see MaintenanceWindowManager#getFinishedTime()
		 */
		public long getFinishedTime() {
			return _manager.getFinishedTime();
		}
	}

}
