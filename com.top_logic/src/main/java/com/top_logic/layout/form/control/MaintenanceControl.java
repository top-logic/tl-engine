/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DefaultControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.component.configuration.MaintenanceViewConfiguration.MaintenanceStateModel;
import com.top_logic.layout.form.model.VisibilityModel;
import com.top_logic.util.Resources;

/**
 * The class {@link MaintenanceControl} is a {@link View} which shows the remaining time until the
 * system changes to the maintenance mode.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MaintenanceControl extends AbstractControlBase implements
		MaintenanceStateModel.MaintenanceStateChangedListener {

	/** ID of the timer field for the maintenance window timer. */
	protected static final String TIME_LEFT_ID = "timeLeftField";

	private boolean _alert;

	private final MaintenanceStateModel _model;

	public MaintenanceControl(MaintenanceStateModel model) {
		this(model, Collections.<String, ControlCommand> emptyMap());
	}

	public MaintenanceControl(MaintenanceStateModel model, Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
		_model = model;
	}

	@Override
	public MaintenanceStateModel getModel() {
		return _model;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		_model.addListener(STATE_PROPERTY, this);
	}

	@Override
	protected void internalDetach() {
		_model.removeListener(STATE_PROPERTY, this);
		super.internalDetach();
	}

	/**
	 * This model has updates if the local copy of the state of the model is different to the
	 * current state of the model.
	 * 
	 * @see AbstractControlBase#hasUpdates()
	 */
	@Override
	protected boolean hasUpdates() {
		return false;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		MaintenanceControlRenderer.INSTANCE.write(context, out, this);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		MaintenanceControlRenderer.INSTANCE.appendControlCSSClasses(out, this);
	}

	/**
	 * Pops the alert state that causes an extra notification displayed, if entering the maintenance
	 * mode starts.
	 */
	public boolean popAlert() {
		boolean result = _alert;
		_alert = false;
		return result;
	}

	/**
	 * Always returns true.
	 * 
	 * @see View#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return isVisible(_model.getState());
	}

	private boolean isVisible(int state) {
		return state != MaintenanceWindowManager.DEFAULT_MODE;
	}

	/**
	 * adds actions to replace the whole content of this control.
	 * 
	 * @see AbstractControlBase#internalRevalidate(DisplayContext, UpdateQueue)
	 */
	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// Ignore.
	}

	@Override
	public void handleStateChange(MaintenanceStateModel model, int oldState, int newState) {
		if (newState == MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE) {
			if (!model.isChangingContext()) {
				_alert = true;
			}
		}
		boolean oldVisible = isVisible(oldState);
		boolean newVisible = isVisible(newState);
		if (oldVisible != newVisible) {
			notifyListeners(VisibilityModel.VISIBLE_PROPERTY, this, oldVisible, newVisible);
		}
		requestRepaint();
	}

	/**
	 * The class {@link MaintenanceControl.MaintenanceControlRenderer} is the default renderer for rendering some
	 * {@link MaintenanceControl}.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static class MaintenanceControlRenderer extends DefaultControlRenderer<MaintenanceControl> {

		public static MaintenanceControlRenderer INSTANCE = new MaintenanceControlRenderer();

		@Override
		protected String getControlTag(MaintenanceControl control) {
			return DIV;
		}

		@Override
		protected void writeControlContents(DisplayContext context, TagWriter out, MaintenanceControl control)
				throws IOException {
			MaintenanceControl theControl = control;
			Resources          res        = context.getResources();
			int currentMaintentanceState = theControl._model.getState();
			if (currentMaintentanceState != MaintenanceWindowManager.IN_MAINTENANCE_MODE
					&& currentMaintentanceState != MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE) {
				/* there is no real content to show */
				return;
			}

			/*
			 * Must use this Div, otherwise an Javascript error occurs due to the Script-block. see
			 * #943
			 */
			out.beginTag(DIV);
			{
				if (currentMaintentanceState == MaintenanceWindowManager.IN_MAINTENANCE_MODE) {
					out.writeText(res.getString(I18NConstants.IN_MAINTENANCE_MODE));
				} else if (currentMaintentanceState == MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE) {
					out.writeText(res.getString(I18NConstants.ENTER_MAINTENANCE_MODE_0));
					out.writeText(NBSP);

					out.beginBeginTag(SPAN);
					out.writeAttribute(ID_ATTR, TIME_LEFT_ID);
					out.endBeginTag();
					{
						out.writeText(res.getString(I18NConstants.ENTER_MAINTENANCE_MODE_1));
					}
					out.endTag(SPAN);

					out.writeText(NBSP);
					out.writeText(res.getString(I18NConstants.ENTER_MAINTENANCE_MODE_2));
				}
			}

			if (currentMaintentanceState == MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE) {
				out.beginScript();
				out.append(getTimerTriggerFunction(theControl));
				if (theControl.popAlert()) {
					out.append("alert(");
					out.writeJsString(theControl._model.getMessage());
					out.append(");");
				}
				out.endScript();
			}
			out.endTag(DIV);
		}

		/**
		 * This method returns a String representing the trigger of the client side function which
		 * counts the time left to enter the maintenance mode.
		 */
		private String getTimerTriggerFunction(MaintenanceControl control) {
		    StringBuilder timerTrigger = new StringBuilder(64); // actually 57
		    timerTrigger.append("initTimer('")
		                .append(TIME_LEFT_ID)
		                .append("', ")
		                .append(System.currentTimeMillis())
		                .append(", ")
		                .append(control._model.getFinishedTime())
		                .append(");");
			return timerTrigger.toString();
		}

	}

}
