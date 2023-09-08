/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import java.util.List;

import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * A {@link List} value.
 * 
 * @see MapValue
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @deprecated Use {@link ListNaming}.
 */
@Deprecated
public interface ListValue extends ValueRef {
	
	@EntryTag("entry")
	List<ModelName> getList();

	void setList(List<? extends ModelName> value);
	
}
