/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import java.util.Date;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.TLContext;

/**
 * A {@link ReactControl} that displays a timestamp as relative time ("5 minutes ago") via the
 * {@code TLRelativeTime} React component.
 *
 * <p>
 * The client formats and periodically refreshes the relative text; the exact timestamp is shown as
 * tooltip, formatted here with the session's locale.
 * </p>
 */
public class ReactRelativeTimeControl extends ReactControl {

	private static final String REACT_MODULE = "TLRelativeTime";

	/** State key for the timestamp in epoch milliseconds, or {@code null} for no value. */
	private static final String TIMESTAMP = "timestamp";

	/** State key for the exact timestamp text shown as tooltip. */
	private static final String LABEL = "label";

	/** State key for the session locale (language tag) used for the relative text. */
	private static final String LOCALE = "locale";

	/**
	 * Creates a new {@link ReactRelativeTimeControl}.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param value
	 *        The initial timestamp, or {@code null} for no value.
	 */
	public ReactRelativeTimeControl(ReactContext context, Date value) {
		super(context, null, REACT_MODULE);
		putState(LOCALE, TLContext.getLocale().toLanguageTag());
		setValue(value);
	}

	/**
	 * Updates the displayed timestamp.
	 *
	 * @param value
	 *        The new timestamp, or {@code null} to display nothing.
	 */
	public void setValue(Date value) {
		putState(TIMESTAMP, value == null ? null : Long.valueOf(value.getTime()));
		putState(LABEL, value == null ? null : HTMLFormatter.getInstance().formatDateTime(value));
	}

}
