/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.value;

import com.top_logic.service.openapi.client.registry.impl.call.Call;

/**
 * Algorithm computing a value based of the arguments of a TL-Script function call.
 * 
 * @see com.top_logic.service.openapi.client.registry.impl.call.uri.QueryArgument.Config#getValue()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ValueProducer {

	/**
	 * Compute a value.
	 * 
	 * @param call
	 *        The arguments of the current function invocation.
	 */
	Object getValue(Call call);

}
