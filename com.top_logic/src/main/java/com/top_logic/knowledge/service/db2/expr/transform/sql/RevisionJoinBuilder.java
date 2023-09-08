/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.knowledge.service.db2.expr.transform.sql.DBSQLFactory.*;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.service.db2.expr.sym.AbstractSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.DataSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.ItemSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.Symbol;

/**
 * {@link JoinBuilder} that creates joins for a {@link RevisionQuery}.
 * 
 * @see HistoryJoinBuilder
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class RevisionJoinBuilder extends JoinBuilder {

	RevisionJoinBuilder() {
		super();
	}

	@Override
	protected SQLExpression prependLifeperiodJoinCondition(ItemSymbol context, DataSymbol symbol, SQLExpression joinCondition) {
		return prependContextInRange(context, symbol, joinCondition);
	}

	@Override
	protected SQLExpression prependInSetLifeperiodJoinConditio(Symbol contextSymbol, Symbol setSymbol,
			SQLExpression joinCondition) {
		/* check that objects which are base objects of the set expression have the correct life
		 * period, e.g. if set is the result of an historic navigation, the referee objects must
		 * match the revision of the context. */
		DataSymbol rootSetSymbol = (DataSymbol) AbstractSymbol.getRootSymbol(setSymbol);
		if (false) {
			if (contextSymbol instanceof ItemSymbol) {
				ItemSymbol context = (ItemSymbol) contextSymbol;
				/* This condition is correct, when the set is evaluated in the revision of the
				 * context, e.g. if the context is a reference into history, say A.x and the inSet
				 * is the set of all references of a type B, say B.y, then there are two possible
				 * interpretations of the revision of B. 1. The set in the inset is completely
				 * independent of the other world, so the revision must match the requested revision
				 * of the query. (This interpretation is used.) 2. The user has navigated into
				 * history using A.x. Now the word is a historic one in revison r. In such case the
				 * B that are potentially correct must be current at revision r, i.e. the revision
				 * of A.x must be in the life period of B. */
				joinCondition = prependContextInRange(context, rootSetSymbol, joinCondition);
			} else {
				joinCondition = prependLifeperiodJoinCondition(rootSetSymbol, joinCondition);
			}
		} else {
			joinCondition = prependLifeperiodJoinCondition(rootSetSymbol, joinCondition);
		}

		/* Check that the revision of the set symbol is the same as the revision of the context
		 * symbol, e.g. if the context is a historic reference then it is necessary the the elements
		 * in the inSet set have the same revision. */
		if (contextSymbol instanceof ItemSymbol && setSymbol instanceof ItemSymbol) {
			joinCondition = prependSameRevision((ItemSymbol) contextSymbol, (ItemSymbol) setSymbol, joinCondition);
		}
		return joinCondition;
	}

	private SQLExpression prependLifeperiodJoinCondition(DataSymbol dataSymbol, SQLExpression joinCondition) {
		/* expression that checks that the given revision is between revMin (inclusive) and revMax
		 * (inclusive) */
		SQLExpression historyCondition = requestedRevisionContained(dataSymbol);

		return and(historyCondition, joinCondition);
	}

	/**
	 * Creates an {@link SQLExpression} that checks that the requested revision is contained in the
	 * lifetime period of the given data.
	 */
	private SQLExpression requestedRevisionContained(DataSymbol data) {
		return and(
			ge(
				data.createRevMaxExpr(),
				currentRevision()),
			le(
				data.createRevMinExpr(),
				currentRevision()));
	}

	@Override
	protected void checkTableHistoricAccessed(Symbol context, SQLTable table) {
		// don't care about historic tables.
	}

	@Override
	protected Collection<String> getHistoricAccessedTables() {
		return Collections.emptyList();
	}

}

