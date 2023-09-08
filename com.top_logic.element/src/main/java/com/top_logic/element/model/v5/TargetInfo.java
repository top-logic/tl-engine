/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.v5;

import com.top_logic.model.TLType;

/**
 * Description of the value of a reference attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class TargetInfo {
	
	public final TLType targetType;

	public final boolean multiple;

	public final boolean ordered;

	public final boolean bag;

	public TargetInfo(TLType targetType, boolean multiple, boolean ordered, boolean bag) {
		super();
		this.targetType = targetType;
		this.multiple = multiple;
		this.ordered = ordered;
		this.bag = bag;
	}
	
}