/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.knowledge.search.HistoryQuery;
import com.top_logic.knowledge.service.db2.expr.sym.DataSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.ItemSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.Symbol;

/**
 * {@link JoinBuilder} that creates joins for a {@link HistoryQuery}.
 * 
 * @see RevisionJoinBuilder
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class HistoryJoinBuilder extends JoinBuilder {

	private List<String> _historicTables = new ArrayList<>();

	HistoryJoinBuilder() {
		super();
	}

	@Override
	protected SQLExpression prependLifeperiodJoinCondition(ItemSymbol context, DataSymbol symbol, SQLExpression joinCondition) {
		if (HistoricSymbol.isHistoricSymbol(context)) {
			/* If the context is a historic symbol, then the life period of that symbol is
			 * irrelevant. The revision of the data (e.g. the revision of the historic reference
			 * which are represented by the context) must match the life period of the symbol. */
			return prependContextInRange(context, symbol, joinCondition);
		}
		return prependCommonLifePeriodWithAncestors(context, symbol, joinCondition);
	}

	@Override
	protected SQLExpression prependInSetLifeperiodJoinConditio(Symbol contextSymbol, Symbol setSymbol,
			SQLExpression joinCondition) {
		boolean contextHistoric = HistoricSymbol.isHistoricSymbol(contextSymbol);
		boolean setHistoric = HistoricSymbol.isHistoricSymbol(setSymbol);
		if (contextHistoric != setHistoric) {
			throw new UnsupportedOperationException(
				"Can not create HistoryQuery for inSet's in which the set is historic and the context is not or vice versa.");
		}


		Symbol nonHistoricContextSymbol;
		Symbol nonHistoricSetSymbol;
		if (contextHistoric) {
			assert setHistoric;
			nonHistoricContextSymbol = HistoricSymbol.getNonHistoricParent(contextSymbol);
			nonHistoricSetSymbol = HistoricSymbol.getNonHistoricParent(setSymbol);
		} else {
			nonHistoricContextSymbol = contextSymbol;
			nonHistoricSetSymbol = setSymbol;
		}

		joinCondition = prependLifePeriodIntersection(nonHistoricContextSymbol, nonHistoricSetSymbol, joinCondition);

		/* Check that the revision of the set symbol is the same as the revision of the context
		 * symbol, e.g. if the context is a historic reference then it is necessary the the elements
		 * in the inSet set have the same revision. */
		if ((contextSymbol instanceof ItemSymbol) && (setSymbol instanceof ItemSymbol)) {
			joinCondition = prependSameRevision((ItemSymbol) contextSymbol, (ItemSymbol) setSymbol, joinCondition);
		}
		return joinCondition;
	}

	/**
	 * Prepends an expression that checks that the intersection of the life period of each ancestor
	 * of <code>symbol1</code> with each ancestor of <code>symbol2</code> is non empty.
	 */
	private SQLExpression prependLifePeriodIntersection(Symbol symbol1, Symbol symbol2, SQLExpression condition) {
		Symbol ancestorOrSelfSym = symbol1;
		while (ancestorOrSelfSym != null) {
			if (ancestorOrSelfSym instanceof DataSymbol) {
				condition = prependCommonLifePeriodWithAncestors(symbol2, (DataSymbol) ancestorOrSelfSym, condition);
			}
			ancestorOrSelfSym = ancestorOrSelfSym.getParent();
		}
		return condition;
	}

	/**
	 * Prepends an expression that ensures that the given symbol and the context and all its
	 * {@link Symbol#getParent() parents} have a common life period, i.e. that there is a revision
	 * in which each data were valid.
	 * 
	 * @see HistoryJoinBuilder#prependCommonLifePeriod(DataSymbol, DataSymbol, SQLExpression)
	 */
	private SQLExpression prependCommonLifePeriodWithAncestors(Symbol context, DataSymbol symbol, SQLExpression condition) {
		/* Produce non empty intersection with all ancestors because there is no own representation
		 * for the join produced by the ancestors. If this were the case then a non empty
		 * intersection with the join would enough. */
		if (symbol.hasOwnLifetime()) {
			Symbol ancestorOrSelfSym = context;
			while (ancestorOrSelfSym != null) {
				if (ancestorOrSelfSym instanceof DataSymbol) {
					condition = prependCommonLifePeriod((DataSymbol) ancestorOrSelfSym, symbol, condition);
				}
				ancestorOrSelfSym = ancestorOrSelfSym.getParent();
			}
		} else {
			// Data have no own lifetime so it can not take part on a life period join
		}
		return condition;
	}

	/**
	 * Prepends an expression that ensures that the given symbol and the context have a common life
	 * period, i.e. that there is a revision in which both data were valid.
	 */
	private SQLExpression prependCommonLifePeriod(DataSymbol context, DataSymbol symbol, SQLExpression condition) {
		if (context.hasOwnLifetime()) {
			/* empty if revMax of one is less than revMin of other */
			SQLExpression historyJoinCondition =
				and(
					le(
						symbol.createRevMinExpr(),
						context.createRevMaxExpr()),
					le(
						context.createRevMinExpr(),
						symbol.createRevMaxExpr()));

			condition = and(historyJoinCondition, condition);
		} else {
			// Data have no own lifetime so it can not take part on a life period join
		}
		return condition;
	}

	@Override
	protected void checkTableHistoricAccessed(Symbol context, SQLTable table) {
		if (HistoricSymbol.isHistoricSymbol(context)) {
			_historicTables.add(table.getTableAlias());
		}
	}

	@Override
	protected Collection<String> getHistoricAccessedTables() {
		return _historicTables;
	}

}

