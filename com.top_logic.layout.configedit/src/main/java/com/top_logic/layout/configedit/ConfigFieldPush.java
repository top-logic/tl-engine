/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;

/**
 * Runs an action once something an editor displays has actually been changed.
 *
 * <p>
 * What the action does is the caller's business: handing the edited copy to the field the editor
 * is rendered in, or checking what is edited again and showing what the check found.
 * </p>
 *
 * <p>
 * Deliberately silent while the editor is being built: building it registers every field it
 * creates, and a registration is reported like a change - see
 * {@link #watch(ConfigFieldModel)} - which would otherwise leave an untouched form looking edited
 * and would run the action over an editor that is only half there. {@link #armed()} ends that
 * silence, {@link #disarmed()} starts it again for an editor that is built more than once.
 * </p>
 */
public final class ConfigFieldPush {

	private final Runnable _push;

	private boolean _armed;

	/**
	 * Creates a {@link ConfigFieldPush}.
	 *
	 * @param push
	 *        What to run once something has changed. Silent until {@link #armed()}.
	 */
	public ConfigFieldPush(Runnable push) {
		_push = push;
	}

	/** Starts reacting - called once the editor is built. */
	public void armed() {
		_armed = true;
	}

	/**
	 * Stops reacting - called before an editor that has already been built is built again.
	 *
	 * <p>
	 * The fields of the editor being replaced are registered afresh, which is reported like a
	 * change; acting on that would run the action over an editor half of whose fields do not exist
	 * yet.
	 * </p>
	 */
	public void disarmed() {
		_armed = false;
	}

	/**
	 * Watches one field the editor built, and takes its registration as a change too.
	 *
	 * <p>
	 * The registration matters on its own: a field appearing after the editor was built means
	 * the editor was rebuilt, which is what a structural change does.
	 * </p>
	 */
	public void watch(ConfigFieldModel field) {
		field.addListener(listener());
		fire();
	}

	/** The listener that reports a value change, for something other than a field to be watched by. */
	public FieldModelListener listener() {
		return new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				fire();
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Not a change of what is edited.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// A verdict about a value, not a new one.
			}
		};
	}

	private void fire() {
		if (_armed) {
			_push.run();
		}
	}
}
