/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.comparators.ComparatorChain;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.dob.DataObject;
import com.top_logic.knowledge.service.db2.expr.visit.SimpleExpressionEvaluator;

/**
 * Factory class to create an {@link Comparator} from a given {@link Order}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OrderComparator {

	private static class OrderSpecComarator implements Comparator<DataObject> {

		private OrderSpec _order;

		/**
		 * Creates a {@link OrderComparator.OrderSpecComarator}.
		 */
		public OrderSpecComarator(OrderSpec order) {
			this._order = order;
		}

		@Override
		public int compare(DataObject o1, DataObject o2) {
			if (o1 == o2) {
				return 0;
			}
			Expression expr = _order.getOrderExpr();
			Object v1 = SimpleExpressionEvaluator.evaluate(expr, o1);
			Object v2 = SimpleExpressionEvaluator.evaluate(expr, o2);
			if (_order.isDescending()) {
				return ComparableComparator.INSTANCE_DESCENDING.compare(v1, v2);
			} else {
				return ComparableComparator.INSTANCE.compare(v1, v2);
			}
		}
	}

	private static class ComparatorOrderVisitor implements OrderVisitor<Comparator<DataObject>, Void> {

		/** Singleton {@link OrderComparator.ComparatorOrderVisitor} instance. */
		static final OrderComparator.ComparatorOrderVisitor INSTANCE = new OrderComparator.ComparatorOrderVisitor();

		@Override
		public Comparator<DataObject> visitOrderSpec(OrderSpec expr, Void arg) {
			return new OrderSpecComarator(expr);
		}

		@Override
		public Comparator<DataObject> visitOrderTuple(OrderTuple expr, Void arg) {
			List<Comparator<DataObject>> innerComparators =
				expr.getOrderSpecs().stream().map(spec -> spec.visitOrder(this, arg)).toList();

			return new ComparatorChain<>(innerComparators);
		}

	}

	/**
	 * Creates a {@link Comparator} that compares the elements based on the given {@link Order}.
	 */
	public static Comparator<DataObject> createComparator(Order order) {
		return order.visitOrder(ComparatorOrderVisitor.INSTANCE, null);
	}

}
