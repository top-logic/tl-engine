/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelNamingConvention;

/**
 * Helpers to build transaction commit messages that carry the model type name in addition to the
 * object label.
 *
 * <p>
 * When the object has a resolvable {@link TLType}, the produced message follows the typed form
 * ("Created {type}: {label}"). Otherwise a fallback message key without the type placeholder is
 * used.
 * </p>
 */
public final class CommitMessages {

	private CommitMessages() {
		// Utility class.
	}

	/**
	 * Builds a commit message that mentions the model type name if available, otherwise falls
	 * back to a label-only message.
	 *
	 * @param typed
	 *        Two-argument key with format <code>"... {type}: {label}"</code>.
	 * @param fallback
	 *        Single-argument key used when the object has no usable {@link TLType}.
	 * @param object
	 *        The affected object; label is taken from {@link MetaLabelProvider}.
	 */
	public static ResKey forObject(ResKey2 typed, ResKey1 fallback, Object object) {
		String label = MetaLabelProvider.INSTANCE.getLabel(object);
		if (object instanceof TLObject tlObject) {
			TLType type = tlObject.tType();
			if (type != null) {
				return typed.fill(TLModelNamingConvention.getTypeLabelKey(type), label);
			}
		}
		return fallback.fill(label);
	}

}
