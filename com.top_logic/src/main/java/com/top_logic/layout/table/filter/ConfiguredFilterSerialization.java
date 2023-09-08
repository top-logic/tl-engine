/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.List;

/**
 * Provides serialization and deserialization of {@link ConfiguredFilter}s.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface ConfiguredFilterSerialization<T extends FilterConfiguration> {

	/** Serializes a {@link FilterConfiguration} */
	List<Object> serialize(T configuration);

	/** Restore a {@link FilterConfiguration} from serialized state */
	void deserialize(T configuration, List<Object> serializedState);
}
