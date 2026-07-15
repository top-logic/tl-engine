/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.scripting;

import java.util.List;

import com.top_logic.layout.LabelProvider;

/**
 * The option context of a React select control: its current options and the label provider used to
 * render them.
 *
 * <p>
 * Used as the value context of {@link ReactOptionByLabelNaming} so an option can be named/resolved by
 * label relative to the control it belongs to (where the label is unique), rather than requiring a
 * globally unique identity.
 * </p>
 *
 * @param options
 *        The current option objects.
 * @param labels
 *        The label provider for the options.
 */
public record ReactOptionScope(List<?> options, LabelProvider labels) {
	// Pure value context.
}
