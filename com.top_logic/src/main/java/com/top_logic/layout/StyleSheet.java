/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.layout.Flavor.Atom;

/**
 * {@link Style} implementation based on a list of rules.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StyleSheet<T> implements Style<T> {

	private final Map<Atom, List<RuleEntry<T>>> data = new HashMap<>();

	/**
	 * Creates a {@link Style} form a list of {@link Rule}s.
	 * 
	 * @param rules
	 *        The rules that make up the new {@link Style}.
	 */
	public StyleSheet(List<Rule<T>> rules) {
		for (int n = 0, cnt = rules.size(); n < cnt; n++) {
			Rule<T> rule = rules.get(n);
			
			for (Atom atom : rule.flavor.definingAtoms) {
				enter(n, atom, rule);
			}
		}
		
		for (List<RuleEntry<T>> potentialRules : data.values()) {
			Collections.sort(potentialRules);
		}
	}

	/**
	 * Creates a new {@link Rule} from the given {@link Flavor} and value.
	 * 
	 * @param <T>
	 *        The type of the value.
	 * @param flavor
	 *        The {@link Flavor} to associate with the given value.
	 * @param value
	 *        The value to associate with the given {@link Flavor}.
	 * @return the new {@link Rule}.
	 */
	public static <T> Rule<T> rule(Flavor flavor, T value) {
		return new Rule<>(flavor, value);
	}

	private void enter(int order, Atom atom, Rule<T> rule) {
		List<RuleEntry<T>> rulesWithFlavor = data.get(atom);
		if (rulesWithFlavor == null) {
			rulesWithFlavor = new ArrayList<>();
			data.put(atom, rulesWithFlavor);
		}
		
		RuleEntry<T> newEntry = new RuleEntry<>(order, rule.flavor, rule.value);
		rulesWithFlavor.add(newEntry);
	}

	/**
	 * Find the first value in this {@link Style}s rule list, whose {@link Flavor} has maximum
	 * selectivity and is implied by the given {@link Flavor}.
	 * 
	 * <p>
	 * Note: This method has a complexity of O(atoms(flavor) * )
	 * </p>
	 * 
	 * @param flavor
	 *        The {@link Flavor} to search a value for.
	 * @return The best matching value of the given {@link Flavor}.
	 * 
	 * @see Style#getValue(Flavor)
	 */
	@Override
	public T getValue(Flavor flavor) {
		RuleEntry<T> bestRule = null;
		for (Atom atom : flavor.atoms) {
			bestRule = findBestRule(atom, flavor, bestRule);
		}
		
		if (bestRule != null) {
			return bestRule.value;
		} else {
			return null;
		}
	}

	private RuleEntry<T> findBestRule(Atom atom, Flavor flavor, RuleEntry<T> bestRule) {
		List<RuleEntry<T>> potentialRules = data.get(atom);
		if (potentialRules != null) {
			for (int n = potentialRules.size() - 1; n >= 0; n--) {
				RuleEntry<T> potentialRule = potentialRules.get(n);
				
				if (bestRule == null || potentialRule.compareTo(bestRule) > 0) {
					if (flavor.implies(potentialRule.flavor)) {
						bestRule = potentialRule;
						
						// No better match can be found since rules are ordered by selectivity.
						break;
					}
				} else {
					// No better match can be found since rules are ordered by selectivity.
					break;
				}
			}
		}
		return bestRule;
	}
	
	/**
	 * Association of arbitrary value to some {@link Flavor}.
	 * 
	 * @param <T> The value type.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class Rule<T> {
		final Flavor flavor;
		final T value;
		
		/**
		 * Creates a {@link StyleSheet.Rule}.
		 *
		 * @param flavor See {@link #getFlavor()}
		 * @param value See {@link #getValue()}
		 */
		/*package protected*/ Rule(Flavor flavor, T value) {
			super();
			this.flavor = flavor;
			this.value = value;
		}
		
		/**
		 * The {@link Flavor} a value is associated with.
		 * 
		 * @see #getValue()
		 */
		public Flavor getFlavor() {
			return flavor;
		}

		/**
		 * The value that is associated to some {@link Flavor}.
		 * 
		 * @see #getFlavor()
		 */
		public T getValue() {
			return value;
		}
		
		@Override
		public final String toString() {
			StringBuilder result = new StringBuilder();
			try {
				appendTo(result);
			} catch (IOException ex) {
				throw new UnreachableAssertion("StringBuilder append may not fail.");
			}
			return result.toString();
		}

		/**
		 * Appends the {@link #toString()} representation to the given
		 * {@link Appendable}.
		 */
		public void appendTo(Appendable result) throws IOException {
			flavor.appendTo(result);
			result.append(' ');
			result.append('{');
			if (value != null) {
				result.append(value.toString());
			}
			result.append('}');
		}
	}

	/**
	 * {@link StyleSheet.Rule} with associated selectivity.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	/*package protected*/
	static class RuleEntry<T> extends Rule<T> implements Comparable<RuleEntry<?>> {

		final int selectivity;
		final int order;

		public RuleEntry(int order, Flavor flavor, T value) {
			super(flavor, value);
			
			this.order = order;
			this.selectivity = flavor.atoms.length;
		}
		
		public int getOrder() {
			return order;
		}

		@Override
		public int compareTo(RuleEntry<?> other) {
			if (this.selectivity < other.selectivity) {
				return -1;
			} else if (this.selectivity > other.selectivity) {
				return 1;
			} else {
				if (this.order < other.order) {
					return -1;
				} else if (this.order > other.order) {
					return 1;
				} else {
					return 0;
				}
			}
		}
	}
	
	@Override
	public String toString() {
		HashSet<RuleEntry<T>> rules = new HashSet<>();
		for (List<RuleEntry<T>> entry : data.values()) {
			rules.addAll(entry);
		}
		ArrayList<RuleEntry<T>> orderedRules = new ArrayList<>(rules);
		Collections.sort(orderedRules);
		
		return orderedRules.toString();
	}

}
