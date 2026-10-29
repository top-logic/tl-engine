/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.present;

/**
 * What a chart computes from each presented object: a label and a value.
 *
 * <p>
 * The argument of the {@code present} tool's {@code chart} parameter, as the model writes it. Both
 * functions are TL-Script functions of one object, e.g. {@code c -> $c.get(`crm:Customer#name`)}. They
 * are compiled and tried on every row before anything reaches the page, so a function that does not
 * compile, or a value that is not a number, is answered as an error the model can correct.
 * </p>
 *
 * <p>
 * For a query that computed data rather than objects, only the {@link #kind() kind} counts: the
 * labels and numbers are in the data. A whole Chart.js configuration needs no spec at all.
 * </p>
 *
 * @param kind
 *        The kind of chart.
 * @param label
 *        TL-Script function from an object to the text that names its bar, point or slice; empty for
 *        data.
 * @param value
 *        TL-Script function from an object to its number; empty for data.
 */
public record ChartSpec(ChartKind kind, String label, String value) {
	// Sole record.
}
