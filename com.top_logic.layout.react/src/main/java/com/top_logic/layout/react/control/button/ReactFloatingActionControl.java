/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import java.util.List;
import java.util.Set;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.ReactImages;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.state.FloatingActionState;
import com.top_logic.util.Resources;

/**
 * Actions floating over a corner of the frame they stand in, rendered by the
 * {@code TLFloatingAction} component.
 *
 * <p>
 * The frame is the body of the panel or the window around the control; the actions stay in their
 * {@link #setPosition(FloatingActionPosition) corner} while the content beneath them scrolls. One
 * action is shown as a round floating action button, several as a speed dial: a round button
 * showing the actions beside it.
 * </p>
 *
 * <p>
 * Each action is a {@link ReactButtonControl}. Its label, icon, tooltip, executability and
 * visibility are the ones of its button, which follows its {@link CommandModel}; the control adds
 * no command state of its own. While all actions are hidden, the client renders nothing.
 * </p>
 */
public class ReactFloatingActionControl extends ReactControl {

	private static final String REACT_MODULE = "TLFloatingAction";

	/**
	 * Creates a {@link ReactFloatingActionControl} in the
	 * {@link FloatingActionPosition#BOTTOM_END default corner}.
	 *
	 * @param actions
	 *        The actions, see {@link #setActions(List)}.
	 */
	public ReactFloatingActionControl(ReactContext context, List<? extends ReactButtonControl> actions) {
		super(context, null, REACT_MODULE);
		putState(FloatingActionState.ACTIONS__PROP, offered(actions));
		setTriggerImage(Icons.FLOATING_ACTION_DIAL);
		setTriggerLabel(Resources.getInstance().getString(I18NConstants.FLOATING_ACTION_DIAL));
	}

	/**
	 * Sets the actions.
	 *
	 * <p>
	 * The control owns the given buttons: replacing them disposes the ones shown before, and
	 * disposing the control disposes them.
	 * </p>
	 *
	 * <p>
	 * An action whose command asks for no display mode of its own shows its icon only, as a round
	 * button does; one asking for its label shows it (an extended floating action button).
	 * </p>
	 *
	 * @param actions
	 *        The actions in the order they are offered; in a speed dial, the first stands next to the
	 *        button opening it.
	 */
	public void setActions(List<? extends ReactButtonControl> actions) {
		List<ReactButtonControl> previous = actions();
		List<ReactButtonControl> current = offered(actions);
		putState(FloatingActionState.ACTIONS__PROP, current);
		for (ReactButtonControl old : previous) {
			if (!current.contains(old)) {
				old.cleanupTree();
			}
		}
	}

	private static List<ReactButtonControl> offered(List<? extends ReactButtonControl> actions) {
		List<ReactButtonControl> result = List.copyOf(actions);
		for (ReactButtonControl action : result) {
			action.setDefaultDisplayMode(ButtonDisplayMode.ICON_ONLY);
		}
		return result;
	}

	/**
	 * The actions.
	 *
	 * @see #setActions(List)
	 */
	@SuppressWarnings("unchecked")
	public List<ReactButtonControl> actions() {
		return (List<ReactButtonControl>) getState(FloatingActionState.ACTIONS__PROP);
	}

	/**
	 * Sets the corner the actions float over.
	 *
	 * @param position
	 *        The corner, {@code null} for {@link FloatingActionPosition#BOTTOM_END}.
	 */
	public void setPosition(FloatingActionPosition position) {
		putState(FloatingActionState.POSITION__PROP,
			position == null || position == FloatingActionPosition.BOTTOM_END ? null : position.getExternalName());
	}

	/**
	 * The corner the actions float over.
	 *
	 * @see #setPosition(FloatingActionPosition)
	 */
	public FloatingActionPosition getPosition() {
		Object externalName = getState(FloatingActionState.POSITION__PROP);
		for (FloatingActionPosition position : FloatingActionPosition.values()) {
			if (position.getExternalName().equals(externalName)) {
				return position;
			}
		}
		return FloatingActionPosition.BOTTOM_END;
	}

	/**
	 * Sets the icon of the button opening the speed dial of several actions.
	 *
	 * @param image
	 *        The icon, {@code null} for {@link Icons#FLOATING_ACTION_DIAL}.
	 */
	public void setTriggerImage(ThemeImage image) {
		ThemeImage effective = image == null ? Icons.FLOATING_ACTION_DIAL : image;
		putState(FloatingActionState.TRIGGER_IMAGE__PROP, ReactImages.encode(getReactContext(), effective));
	}

	/**
	 * Sets the label of the button opening the speed dial of several actions: its accessible name
	 * and its tooltip.
	 *
	 * @param label
	 *        The label, {@code null} for {@link I18NConstants#FLOATING_ACTION_DIAL}.
	 */
	public void setTriggerLabel(String label) {
		String effective = label == null || label.isEmpty()
			? Resources.getInstance().getString(I18NConstants.FLOATING_ACTION_DIAL)
			: label;
		putState(FloatingActionState.TRIGGER_LABEL__PROP, effective);
	}

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), FloatingActionState.POSITION__PROP,
			FloatingActionState.TRIGGER_IMAGE__PROP);
	}

}
