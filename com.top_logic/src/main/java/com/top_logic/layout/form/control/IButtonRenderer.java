/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ButtonUIModel;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Specialized {@link Renderer} interface for {@link AbstractButtonControl}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface IButtonRenderer extends ControlRenderer<AbstractButtonControl<?>> {

	/**
	 * This method translates a change of {@link ButtonUIModel#getCssClasses()} into changes of the
	 * current view.
	 * 
	 * @param oldValue
	 *        The old classes.
	 * @param newValue
	 *        The new classes.
	 */
	default void handleClassPropertyChange(AbstractButtonControl<?> button, String oldValue, String newValue) {
		button.requestRepaint();
	}

	/**
	 * Translates a change of the {@link AbstractButtonControl#isDisabled()} property into a change
	 * of the currently displayed view of the control.
	 * 
	 * <p>
	 * Note: This method can no longer be overridden, since it would be extremely difficult to
	 * incrementally update the disabled state. The onclick script is only generated for active
	 * buttons. Activating a disabled button would need to incrementally add a handler script.
	 * </p>
	 *
	 * @param button
	 *        The {@link AbstractButtonControl} being rendered.
	 * @param oldValue
	 *        The old disabled state.
	 * @param newValue
	 *        The new disabled state.
	 */
	default void handleDisabledPropertyChange(AbstractButtonControl<?> button, boolean oldValue, boolean newValue) {
		button.requestRepaint();
	}

	/**
	 * Translates a change of {@link AbstractButtonControl#getDisabledReason()} into changes of the
	 * current view.
	 * 
	 * @param button
	 *        The {@link AbstractButtonControl} being rendered.
	 * @param oldReason
	 *        the I18N key for the reason why the given button is disabled before the change.
	 * @param newReason
	 *        the I18N key for the reason why the given button is disabled after the change.
	 */
	default void handleDisabledReasonChanged(AbstractButtonControl<?> button, ResKey oldReason, ResKey newReason) {
		button.requestRepaint();
	}

	/**
	 * Translates a change of the {@link AbstractButtonControl#getImage()}, or
	 * {@link AbstractButtonControl#getDisabledImage()} to a change of the currently displayed view.
	 * 
	 * @param imageProperty
	 *        Either {@link AbstractButtonControl#IMAGE_PROPERTY}, or
	 *        {@link AbstractButtonControl#DISABLED_IMAGE_PROPERTY}, depending on the property that
	 *        has changed.
	 * @param oldValue
	 *        The old image.
	 * @param newValue
	 *        The new image.
	 */
	default void handleImagePropertyChange(AbstractButtonControl<?> button, int imageProperty,
			ThemeImage oldValue, ThemeImage newValue) {
		button.requestRepaint();
	}

	/**
	 * Translates a change of the {@link AbstractButtonControl#getLabel()} property into a change of
	 * the currently displayed view of the control.
	 * 
	 * @param newLabel
	 *        The new label to be set.
	 */
	default void handleLabelPropertyChange(AbstractButtonControl<?> button, ResKey newLabel) {
		button.requestRepaint();
	}

}
