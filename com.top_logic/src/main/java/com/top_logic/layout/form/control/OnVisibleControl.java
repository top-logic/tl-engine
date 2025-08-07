/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.Collections;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.model.VisibilityModel;
import com.top_logic.mig.html.layout.VisibilityListener;

/**
 * A {@link OnVisibleControl} is a control whose visibility is exactly that of a given
 * {@link VisibilityModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OnVisibleControl extends AbstractCompositeControl<OnVisibleControl> {

	/**
	 * Listener attached to the given {@link #_model}
	 * 
	 * @see VisibilityListenerImpl
	 */
	private final VisibilityListener visibilityListener = new VisibilityListenerImpl();

	/**
	 * The {@link VisibilityModel} which has the same visibility as this control.
	 */
	private final VisibilityModel _model;

	/**
	 * Creates a new {@link OnVisibleControl} which reacts on the visibility of the given model,
	 * i.e. {@link VisibilityModel#isVisible()} and {@link OnVisibleControl#isVisible()} have the
	 * same value if this control is attached.
	 * 
	 * @param model
	 *        The {@link VisibilityModel} with the same visibility. Must not be <code>null</code>
	 */
	public OnVisibleControl(VisibilityModel model) {
		super(Collections.<String, ControlCommand> emptyMap());
		_model = model;
		if (_model == null) {
			throw new IllegalArgumentException("Given " + VisibilityModel.class.getName() + " must not be null");
		}
	}

	@Override
	public VisibilityModel getModel() {
		return _model;
	}

	@Override
	public boolean isVisible() {
		return _model.isVisible();
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		_model.addListener(VisibilityModel.VISIBLE_PROPERTY, visibilityListener);
	}

	@Override
	protected void internalDetach() {
		_model.removeListener(VisibilityModel.VISIBLE_PROPERTY, visibilityListener);
		super.internalDetach();
	}

	@Override
	public OnVisibleControl self() {
		return this;
	}

	/**
	 * Listener which forces the enclosing control to redraw if the visibility of the model, this
	 * listener is attached to, changes.
	 */
	private final class VisibilityListenerImpl implements VisibilityListener {

		VisibilityListenerImpl() {
			// default constructor
		}

		@Override
		public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
			if (sender != OnVisibleControl.this.getModel()) {
				/* Don't care about change of visibility of model different from the observed one.
				 * In case the observed model is a FormGroup, also visibility changes of inner
				 * FormFields are propagated. */
				return Bubble.BUBBLE;
			}
			OnVisibleControl.this.requestRepaint();
			return Bubble.BUBBLE;
		}

	}
}

