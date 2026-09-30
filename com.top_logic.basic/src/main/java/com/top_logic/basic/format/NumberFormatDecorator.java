/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;

/**
 * Base class for a {@link NumberFormat} that formats and parses through another one.
 *
 * <p>
 * Answers for the settings of the {@link #impl() format it works through} - the digits it writes,
 * whether it groups them, whether it reads a fraction - so that asking a decorator how it writes a
 * number gives the same answer as asking the format that actually writes it. A subclass says which
 * format that is and adds what it does on top: a value rounded to the format's own precision, a
 * result of a fixed number type, or a format resolved per thread.
 * </p>
 *
 * @see DoubleFormat
 * @see LongFormat
 * @see NormalizingFormat
 */
public abstract class NumberFormatDecorator extends NumberFormat {

	/**
	 * The format this one formats and parses through.
	 */
	protected abstract NumberFormat impl();

	@Override
	public int getMaximumIntegerDigits() {
		return impl().getMaximumIntegerDigits();
	}

	@Override
	public int getMinimumIntegerDigits() {
		return impl().getMinimumIntegerDigits();
	}

	@Override
	public int getMaximumFractionDigits() {
		return impl().getMaximumFractionDigits();
	}

	@Override
	public int getMinimumFractionDigits() {
		return impl().getMinimumFractionDigits();
	}

	@Override
	public boolean isGroupingUsed() {
		return impl().isGroupingUsed();
	}

	@Override
	public boolean isParseIntegerOnly() {
		return impl().isParseIntegerOnly();
	}

	@Override
	public Currency getCurrency() {
		return impl().getCurrency();
	}

	@Override
	public RoundingMode getRoundingMode() {
		return impl().getRoundingMode();
	}

}
