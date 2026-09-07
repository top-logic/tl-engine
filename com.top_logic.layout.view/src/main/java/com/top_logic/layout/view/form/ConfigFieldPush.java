/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.configedit.ConfigFieldIndex;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;

/**
 * Hands the edited copy to the attribute's field, once anything has actually been changed.
 *
 * <p>
 * Deliberately silent while the editor is still being built: building it registers every field
 * it creates, which would otherwise count as a change and leave an untouched form looking
 * edited.
 * </p>
 */
final class ConfigFieldPush {

	private final Runnable _push;

	private boolean _armed;

	ConfigFieldPush(Runnable push) {
		_push = push;
	}

	/** Starts reacting - called once the editor is built. */
	void armed() {
		_armed = true;
	}

	/**
	 * Watches one field the editor built, and takes its registration as a change too.
	 *
	 * <p>
	 * The registration matters on its own: a field appearing after the editor was built means
	 * the editor was rebuilt, which is what a structural change does.
	 * </p>
	 */
	void watch(ConfigFieldModel field) {
		field.addListener(listener());
		fire();
	}

	FieldModelListener listener() {
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
