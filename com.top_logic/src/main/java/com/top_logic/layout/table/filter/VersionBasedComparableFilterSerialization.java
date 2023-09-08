/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.List;

/**
 * Dispatches to a specific {@link ConfiguredFilterSerialization} of a comparable filter, that is
 * suitable to deserialize the detected format version. In contrary the
 * {@link ConfiguredFilterSerialization}, that supports the newest version format, is used for
 * serialization.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class VersionBasedComparableFilterSerialization
		implements ConfiguredFilterSerialization<ComparableFilterConfiguration> {

	/** Static instance of {@link VersionBasedComparableFilterSerialization} */
	public static final ConfiguredFilterSerialization<ComparableFilterConfiguration> INSTANCE =
		new VersionBasedComparableFilterSerialization();

	@Override
	public List<Object> serialize(ComparableFilterConfiguration configuration) {
		return ComparableFilterSerializationV2.INSTANCE.serialize(configuration);
	}

	@Override
	public void deserialize(ComparableFilterConfiguration configuration, List<Object> serializedState) {
		if (!serializedState.isEmpty()) {
			Object versionSection = serializedState.get(1);
			if (versionSection instanceof List) {
				// For now only one deserializer supports explicit version documention in serialized
				// state
				ComparableFilterSerializationV2.INSTANCE.deserialize(configuration, serializedState);
			} else {
				ComparableFilterSerializationV1.INSTANCE.deserialize(configuration, serializedState);
			}
		}
	}
}
