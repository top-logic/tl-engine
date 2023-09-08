/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.List;

/**
 * Dispatches to specific {@link ConfiguredFilterSerialization} of text filters, that is suitable to
 * deserialize the detected format version. In contrary the {@link ConfiguredFilterSerialization},
 * that supports the newest version format, is used for serialization.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class VersionBasedTextFilterSerialization implements ConfiguredFilterSerialization<TextFilterConfiguration> {

	/** Static instance of {@link VersionBasedTextFilterSerialization} */
	public static final ConfiguredFilterSerialization<TextFilterConfiguration> INSTANCE =
		new VersionBasedTextFilterSerialization();

	@Override
	public List<Object> serialize(TextFilterConfiguration configuration) {
		return TextFilterSerializationV2.INSTANCE.serialize(configuration);
	}

	@Override
	public void deserialize(TextFilterConfiguration configuration, List<Object> serializedState) {
		if (!serializedState.isEmpty()) {
			Object versionSection = serializedState.get(1);
			if (((List) serializedState).size() > 1) {
				// For now only one deserializer supports explicit version documention in serialized
				// state
				TextFilterSerializationV2.INSTANCE.deserialize(configuration, serializedState);
			} else {
				TextFilterSerializationV1.INSTANCE.deserialize(configuration, serializedState);
			}
		}
	}
}