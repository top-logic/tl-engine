/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.layout.table.filter.ComparableFilterConfiguration.NumberTransformer;
import com.top_logic.layout.table.filter.ComparableFilterConfiguration.Operators;

/**
 * {@link ConfiguredFilterSerialization} of comparable filters of format version 1.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ComparableFilterSerializationV1 implements ConfiguredFilterSerialization<ComparableFilterConfiguration> {

	/** Static instance of {@link ComparableFilterSerializationV1} */
	public static final ConfiguredFilterSerialization<ComparableFilterConfiguration> INSTANCE =
		new ComparableFilterSerializationV1();

	@Override
	public List<Object> serialize(ComparableFilterConfiguration configuration) {
		// Check for valid filter configuration. Secondary filter pattern must be 'null' for all
		// operators but "BETWEEN". Normally all filter configurations are valid. Due to a yet
		// untraceable bug, the filter configuration has to be repaired afterwards.
		Comparable<?> secondaryFilterPattern = configuration.getSecondaryFilterPattern();
		if (secondaryFilterPattern != null && configuration.getOperator() != Operators.BETWEEN) {
			secondaryFilterPattern = null;
			Logger.warn(
				"Invalid configuration of comparable filter shall be serialized! Secondary filter pattern must be 'null' for all filter operators, but 'BETWEEN'. Actual configuration values are: Operator: "
					+ configuration.getOperator() + ", Primary filter pattern: "
					+ configuration.getPrimaryFilterPattern() + ", Secondary filter pattern: "
					+ configuration.getSecondaryFilterPattern() + ". Configuration will be repaired.",
				ComparableFilterSerializationV1.class);
		}

		// Transform raw object types to json types
		if (secondaryFilterPattern == null) {
			return NumberTransformer.getInstance().transformToJSON(configuration.getPrimaryFilterPattern(),
				configuration.getOperator());
		} else {
			return NumberTransformer.getInstance().transformToJSON(configuration.getPrimaryFilterPattern(),
				secondaryFilterPattern, configuration.getOperator());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(ComparableFilterConfiguration configuration, List<Object> serializedState) {
		// Transform json object types to raw types
		List<Object> rawState = NumberTransformer.getInstance().transformFromJSON(serializedState);

		// Backup current configuration state for failsave
		Comparable<?> currentPrimaryFilterPattern = configuration.getPrimaryFilterPattern();
		Comparable<?> currentSecondaryFilterPattern = configuration.getSecondaryFilterPattern();
		Operators currentOperator = configuration.getOperator();

		// Restore serialized settings
		try {
			configuration.setPrimaryFilterPattern((Comparable<Object>) rawState.get(0));
			if (noSecondaryFilterPatternStored(rawState)) {
				configuration.setOperator((Operators) rawState.get(1));
				configuration.setSecondaryFilterPattern(null);
			} else {
				Operators storedOperator = (Operators) rawState.get(2);
				configuration.setOperator(storedOperator);

				// Restore secondary filter pattern for appropriate stored operator only. Normally,
				// the secondary filter pattern should be null, if the operator is not set to
				// "BETWEEN". Due to yet untraceable bug an invalid filter configuration has been
				// stored and must be repaired.
				if (storedOperator == Operators.BETWEEN) {
					configuration.setSecondaryFilterPattern((Comparable<Object>) rawState.get(1));
				} else {
					configuration.setSecondaryFilterPattern(null);
				}
			}
		} catch (ClassCastException e) {
			Logger.warn(
				"Recovery of the filter configuration failed, because of invalid recovery parameters. " +
					"[" + configuration.getPrimaryFilterPattern().getClass() + ", "
					+ configuration.getOperator().getClass() + "] was expected, but "
					+ rawState.toString() + " was found.",
				e, ComparableFilterConfiguration.class);

			// Restore original configuration state
			configuration.setPrimaryFilterPattern(currentPrimaryFilterPattern);
			configuration.setSecondaryFilterPattern(currentSecondaryFilterPattern);
			configuration.setOperator(currentOperator);
		}
	}

	private boolean noSecondaryFilterPatternStored(List<Object> rawState) {
		return rawState.size() == 2;
	}
}