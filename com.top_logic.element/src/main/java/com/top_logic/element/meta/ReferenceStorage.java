/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.Set;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;

/**
 * {@link StorageImplementation} for {@link TLReference}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ReferenceStorage extends StorageImplementation {

	@Override
	Set<? extends TLObject> getReferers(TLObject self, TLReference reference);

}
