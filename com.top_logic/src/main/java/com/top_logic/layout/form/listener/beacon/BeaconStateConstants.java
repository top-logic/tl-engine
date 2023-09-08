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

	public static final Integer STATE_NOT_SET = null;
	public static final Integer STATE_GREEN = Integer.valueOf(1);
	public static final Integer STATE_YELLOW = Integer.valueOf(2);
	public static final Integer STATE_RED = Integer.valueOf(3);
	
}
