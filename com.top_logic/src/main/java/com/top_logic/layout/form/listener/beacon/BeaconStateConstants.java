/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.listener.beacon;

/**
 * This interface contains only the beacon states.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public interface BeaconStateConstants {

	/** No beacon state is selected. */
	public static final Integer STATE_NOT_SET = null;
	/** The green beacon state. */
	public static final Integer STATE_GREEN = Integer.valueOf(1);
	/** The yellow beacon state. */
	public static final Integer STATE_YELLOW = Integer.valueOf(2);
	/** The red beacon state. */
	public static final Integer STATE_RED = Integer.valueOf(3);
	
}
