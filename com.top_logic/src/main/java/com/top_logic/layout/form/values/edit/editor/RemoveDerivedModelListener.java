/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.basic.util.Utils.*;

import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;

/**
 * A {@link ConfigurationListener} that removes a {@link FormMember} from its parent when the given
 * model is removed from the property on which this listener is registered.
 * <p>
 * This is used for {@link FormMember}s that are derived from {@link ConfigurationItem}s. When the
 * {@link ConfigurationItem} is removed from its container, the {@link FormMember} is removed from
 * its parent.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class RemoveDerivedModelListener implements ConfigurationListener {

	private final Object _source;

	private final FormMember _derived;

	/**
	 * Creates a {@link RemoveDerivedModelListener}.
	 * 
	 * @param source
	 *        If this model is removed, the given derived model is removed from its parent.
	 * @param derived
	 *        The {@link FormMember} that was derived from the given model.
	 */
	RemoveDerivedModelListener(Object source, FormMember derived) {
		_source = requireNonNull(source);
		_derived = requireNonNull(derived);
	}

	@Override
	public void onChange(ConfigurationChange change) {
		if (change.getKind() != ConfigurationChange.Kind.REMOVE) {
			return;
		}
		if (!Utils.equals(change.getOldValue(), _source)) {
			return;
		}
		FormContainer oldParent = _derived.getParent();
		if (oldParent == null) {
			// Somebody else cared about the UI update.
			return;
		}
		oldParent.removeMember(_derived);
	}

}
