/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.util.Pointer;

/**
 * {@link ConstraintCheck} that refuses a preference text longer than the configured cap.
 *
 * <p>
 * The text is sent with every single request of that account, so its length is paid for on every
 * turn of every conversation - which is why it is refused where it is entered rather than truncated
 * where it is used. The cap is
 * {@link com.top_logic.tl.ai.core.agent.AgentPolicyService.Config#getMaxPreferencesLength()
 * configuration}, so this is a constraint class and not a size annotation on the attribute.
 * </p>
 *
 * @see UserPreferences
 */
public class PreferencesLengthConstraint implements ConstraintCheck {

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		Object value = object.tValue(attribute);
		if (!(value instanceof String text)) {
			return null;
		}
		return refusal(text, EnvironmentSettings.maxPreferencesLength());
	}

	/**
	 * Why the given text is refused at the given cap, or {@code null} when it is not.
	 *
	 * <p>
	 * A cap of zero or less is no cap: an installation that wants no limit says so by clearing the
	 * number rather than by typing one nobody could reach.
	 * </p>
	 */
	static ResKey refusal(String text, int limit) {
		if (text == null || limit <= 0 || text.length() <= limit) {
			return null;
		}
		return I18NConstants.PREFERENCES_TOO_LONG__LENGTH_LIMIT.fill(
			Integer.valueOf(text.length()), Integer.valueOf(limit));
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays) {
		// Only depends on the annotated attribute's own value - the cap is configuration, not data.
	}
}
