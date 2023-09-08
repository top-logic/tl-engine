/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;

/**
 * {@link ConfiguredFilterSerialization} of text filters of format version 1.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TextFilterSerializationV1 implements ConfiguredFilterSerialization<TextFilterConfiguration> {

	/** Static instance of {@link TextFilterSerializationV1} */
	public static final ConfiguredFilterSerialization<TextFilterConfiguration> INSTANCE =
		new TextFilterSerializationV1();

	@Override
	public List<Object> serialize(TextFilterConfiguration configuration) {
		List<Object> configState = new ArrayList<>(4);
		configState.add(configuration.getTextPattern());
		configState.add(configuration.isWholeField());
		configState.add(configuration.isCaseSensitive());
		configState.add(configuration.isRegExp());

		return configState;
	}

	@Override
	public void deserialize(TextFilterConfiguration configuration, List<Object> serializedState) {
		if (serializedState.size() == 4) {

			// Backup current configuration state for failsave
			String currentFilterPattern = configuration.getTextPattern();
			boolean currentWholeField = configuration.isWholeField();
			boolean currentCaseSensitive = configuration.isCaseSensitive();
			boolean currentRegExp = configuration.isRegExp();

			try {
				configuration.setTextPattern((String) serializedState.get(0));
				configuration.setWholeField((Boolean) serializedState.get(1));
				configuration.setCaseSensitive((Boolean) serializedState.get(2));
				configuration.setRegExp((Boolean) serializedState.get(3));
			} catch (ClassCastException e) {
				if (Logger.isDebugEnabled(TextFilterConfiguration.class)) {
					Logger
						.debug("Recovery of the filter configuration failed, because of invalid recovery parameters. " +
							"[" + configuration.getTextPattern().getClass()
							+ ", Boolean, Boolean, Boolean] was expected, but "
							+ serializedState.toString() + " was found.",
							e, TextFilterConfiguration.class);
				}

				// Restore original configuration state
				configuration.setTextPattern(currentFilterPattern);
				configuration.setWholeField(currentWholeField);
				configuration.setCaseSensitive(currentCaseSensitive);
				configuration.setRegExp(currentRegExp);
			}
		}

		else {
			if (Logger.isDebugEnabled(TextFilterConfiguration.class)) {
				Logger.debug(
					"Recovery of the filter configuration failed, because of invalid amount of recovery parameters. " +
						"[" + configuration.getTextPattern().getClass() + ", Boolean, Boolean] was expected, but "
						+ serializedState.toString()
						+ " was found.",
					TextFilterConfiguration.class);
			}
		}
	}
}