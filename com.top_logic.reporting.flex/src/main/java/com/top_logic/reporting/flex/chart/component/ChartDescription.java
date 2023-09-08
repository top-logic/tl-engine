/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.partition.Criterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.AttributeBasedCriterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.FunctionCriterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.MethodCriterion;

/**
 * Collection of relevant data displayed in a chart.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ChartDescription {

	private final Map<String, MethodCriterion> _criteria = new HashMap<>();

	private final Map<String, TLStructuredTypePart> _attributes = new HashMap<>();

	private final List<String> _columns = new ArrayList<>();

	private final ChartTree _tree;

	/**
	 * Creates a {@link ChartDescription}.
	 * 
	 * @param tree see {@link #getChartTree()}
	 *        
	 */
	public ChartDescription(ChartTree tree) {
		_tree = tree;
		Set<String> columns = new HashSet<>();
		for (int i = 1; i <= tree.getDepth() + 1; i++) {
			Criterion criterion = tree.getCriterion(i);
			if (criterion instanceof AttributeBasedCriterion) {
				TLStructuredTypePart part = ((AttributeBasedCriterion) criterion).getAttribute();
				if (part != null) {
					String key = part.getName();
					if (!columns.contains(key)) {
						_columns.add(key);
						columns.add(key);
						_attributes.put(key, part);
					}
				}
			}
			else if (criterion instanceof MethodCriterion) {
				String key = criterion.getLabel();
				if (!columns.contains(key)) {
					_columns.add(key);
					columns.add(key);
					_criteria.put(key, (MethodCriterion) criterion);
				}
			}
			else if (criterion instanceof FunctionCriterion) {
				for (Object detail : criterion.getDetails()) {
					if (detail instanceof TLStructuredTypePart) {
						TLStructuredTypePart part = (TLStructuredTypePart) detail;
						String key = part.getName();
						if (!columns.contains(key)) {
							_columns.add(key);
							columns.add(key);
							_attributes.put(key, part);
						}
					}
				}
			}
		}
	}

	/** 
	 * The {@link ChartTree} to collect the relevant data for.
	 */
	public ChartTree getChartTree() {
		return _tree;
	}
	
	/**
	 * The columns used in {@link #getChartTree()}
	 */
	public List<String> getColumns() {
		return _columns;
	}

	/**
	 * The attributes by name used in {@link #getChartTree()}
	 */
	public Map<String, TLStructuredTypePart> getAttributes() {
		return _attributes;
	}

	/**
	 * The {@link MethodCriterion} for the given key, may be null.
	 */
	public MethodCriterion getMethodCriterion(String key) {
		return _criteria.get(key);
	}

}