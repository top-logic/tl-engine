/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.util.error.TopLogicException;

/**
 * An input control that may hold input the user has entered but not yet committed into the value
 * of its field.
 *
 * <p>
 * For example, an entry of a configuration list that has been filled in but not confirmed: it is
 * not part of the field's value until the user confirms it. Whoever takes the field's value over
 * from such a control - a form saving its fields, the dialog of a {@link ReactCompactFieldControl}
 * applying its edit - lets it commit that input first, so that what the user entered is taken
 * over rather than silently dropped.
 * </p>
 */
public interface PendingInput {

	/**
	 * Commits all input not yet part of the field's value into it, as if the user had committed
	 * each piece of it.
	 *
	 * <p>
	 * Input that cannot be committed stays where it is, with its problem shown at the input.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         If some input could not be committed.
	 */
	void commitPendingInput();

}
