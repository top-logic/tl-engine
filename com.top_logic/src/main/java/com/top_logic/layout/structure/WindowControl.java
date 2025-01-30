/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.util.Map;
import java.util.Objects;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.layoutRenderer.WindowRenderer;

/**
 * Base class for windows.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class WindowControl<I extends WindowControl<?>> extends WrappingControl<I>
		implements LayoutDataListener, MaximalityChangeListener {

	private WindowModel _model;

	/**
	 * Creates a {@link WindowControl} based on the given {@link WindowModel} with the given parent.
	 * @param model
	 *        the {@link WindowModel} which is displayed by this control
	 * @param commandsByName
	 *        additional commands
	 */
	protected WindowControl(WindowModel model, Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
		setWindowModel(model);
	}

	/**
	 * Sets the given {@link WindowModel} as {@link #getWindowModel()}.
	 * 
	 * @param model
	 *        New {@link WindowModel}.
	 */
	public void setWindowModel(WindowModel model) {
		Objects.requireNonNull(model, "Window model must not be null.");

		// Repaint control after window model change
		requestRepaint();

		if (_model != null) {
			_model.removeListener(WindowModel.LAYOUT_DATA_PROPERTY, this);
		}
		_model = model;
		/* add listener directly during construction as otherwise events occurring in model are not
		 * forwarded when the Control is not rendered. This is important especially for
		 * DialogWindowControls as it deregisters itself when model is closed. (#6611) */
		_model.addListener(WindowModel.LAYOUT_DATA_PROPERTY, this);
	}

	/**
	 * the {@link WindowModel} this control based on
	 */
	public final WindowModel getWindowModel() {
		return _model;
	}

	@Override
	public WindowModel getModel() {
		return _model;
	}

	@Override
	public final void setConstraint(LayoutData layoutData) {
		_model.setLayoutData(layoutData, false);
	}

	@Override
	public final LayoutData getConstraint() {
		return _model.getLayoutData();
	}

	@Override
	public void setSize(DisplayDimension width, DisplayDimension height) {
		_model.setLayoutData(_model.getLayoutData().resized(width, height), true);
	}

	/**
	 * This method returns the I18N of the title of this window.
	 */
	public final HTMLFragment getTitle() {
		return _model.getWindowTitle();
	}

	/**
	 * @see WindowModel#getBorderSize()
	 */
	public final int getLeftSize() {
		return _model.getBorderSize();
	}

	/**
	 * @see WindowModel#getBorderSize()
	 */
	public final int getRightSize() {
		return _model.getBorderSize();
	}

	/**
	 * @see WindowModel#getTitleBarHeight()
	 */
	public final int getTopSize() {
		return _model.getTitleBarHeight();
	}

	/**
	 * @see WindowModel#getBorderSize()
	 */
	public final int getBottomSize() {
		return _model.getBorderSize();
	}

	/**
	 * @see WindowModel#isTitleBarShown()
	 */
	public final boolean isTitleBarShown() {
		return _model.isTitleBarShown();
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();
		_model.addListener(WindowModel.MAXIMIZED_PROPERTY, this);
	}

	@Override
	protected void detachInvalidated() {
		_model.removeListener(WindowModel.MAXIMIZED_PROPERTY, this);
		super.detachInvalidated();
	}

	@Override
	public void handleLayoutDataChanged(Object sender, LayoutData oldData, LayoutData newData, boolean layoutSizeUpdate) {
		if (layoutSizeUpdate) {
			// NOOP
			return;
		} else {
			requestRepaint();
		}
	}

	@Override
	public void maximalityChanged(Object sender, boolean maximal) {
		requestRepaint();
	}

	@Override
	protected ControlRenderer<? super I> createDefaultRenderer() {
		return WindowRenderer.INSTANCE;
	}
}
