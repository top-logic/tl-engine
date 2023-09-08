/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import java.util.Date;

/**
 * A {@link Date} value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @deprecated Use {@link DateNaming}.
 */
@Deprecated
public interface DateValue extends ValueRef {

	Date getDate();
	void setDate(Date value);
	
}
