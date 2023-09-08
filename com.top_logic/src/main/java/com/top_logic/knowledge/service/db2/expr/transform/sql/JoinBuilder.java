/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.table;

import java.util.Collection;

import com.top_logic.basic.db.sql.SQLBoolean;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLJoin;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.basic.db.sql.SQLTableReference;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.db2.expr.sym.AbstractSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.DataSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.FlexAttributeSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.ItemSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.LiteralItemSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.NullSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.PrimitiveSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.ReferenceSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.RowAttributeSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.Symbol;
import com.top_logic.knowledge.service.db2.expr.sym.SymbolVisitor;
import com.top_logic.knowledge.service.db2.expr.sym.TableSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.TupleSymbol;

/**
 * BHU: This class
 * 
 * <p>
 * Argument of the visit: The table created by the {@link Symbol#getParent() parent} of the visited
 * {@link Symbol}.
 * </p>
 * <p>
 * Result of the visit: The table in which the data of the visited {@link Symbol} are contained.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class JoinBuilder implements SymbolVisitor<SQLTableReference, SQLTableReference> {

	private static final String TABLE_ALIAS_PREFIX = "t";
	private int nextTableId = 0;
	
	private String newTableAlias() {
		return TABLE_ALIAS_PREFIX + (nextTableId++);
	}
	
	public SQLTableReference createJoin(SetExpression expr) {
		Symbol rootSym = AbstractSymbol.getRootSymbol(expr.getSymbol());
		SQLTableReference join = rootSym.visit(this, null);
		return join;
	}

	@Override
	public SQLTableReference visitTableSymbol(TableSymbol sym, SQLTableReference leftTable) {
		String rightTableAlias = newTableAlias();
		sym.setTableAlias(rightTableAlias);
		
		SQLTableReference resultTable;
		SQLTable rightTable = table((DBTableMetaObject) sym.getType(), rightTableAlias);
		if (leftTable == null) {
			resultTable = rightTable;
		} else {
			ReferenceSymbol parent = (ReferenceSymbol) sym.getParent();
			SQLExpression joinCondition = SQLBoolean.TRUE;

			// Note: Reverse order of importance, since join conditions are prepended to the
			// existing join condition.
			joinCondition = prependTypeJoinCondition(parent, sym, joinCondition);
			joinCondition = prependItemJoinCondition(parent, sym, joinCondition);

			resultTable = leftJoin(leftTable, rightTable, joinCondition);
			checkTableHistoricAccessed(parent, rightTable);
			sym.setPotentiallyNull(true);
		}
		
		resultTable = descend(sym.getAttributeSymbols(), resultTable);
		resultTable = descend(sym.getFlexAttributeSymbols(), resultTable);
		
		return resultTable;
	}

	@Override
	public SQLTableReference visitRowAttributeSymbol(RowAttributeSymbol sym, SQLTableReference leftTable) {
		return leftTable;
	}
	
	@Override
	public SQLTableReference visitFlexAttributeSymbol(FlexAttributeSymbol sym, SQLTableReference leftTable) {
		String rightTableAlias = newTableAlias();
		sym.setDataTableAlias(rightTableAlias);
		ItemSymbol parent = sym.getParent();

		SQLExpression joinCondition = eq(sym.createAttrExpr(), literalString(sym.getAttributeName()));

		// Note: Reverse order of importance, since join conditions are prepended to the existing
		// join condition.
		joinCondition = prependTypeJoinCondition(parent, sym, joinCondition);
		joinCondition = prependItemJoinCondition(parent, sym, joinCondition);

		SQLTable rightTable = table(sym.getFlexDataType(), rightTableAlias);
		checkTableHistoricAccessed(parent, rightTable);
		SQLJoin join = leftJoin(leftTable, rightTable, joinCondition);
		sym.setPotentiallyNull(true);
		return join;
	}

	/**
	 * Prepends a check for a match of the identifier of the represented data for a join condition.
	 */
	protected SQLExpression prependIdentifierJoinCondition(DataSymbol context, DataSymbol symbol,
			SQLExpression joinCondition) {
		return and(eq(context.createIdentifierExpr(), symbol.createIdentifierExpr()), joinCondition);
	}

	/**
	 * Prepends a check for a match of the type of the represented data for a join condition.
	 */
	protected SQLExpression prependTypeJoinCondition(DataSymbol context, DataSymbol symbol, SQLExpression joinCondition) {
		return and(eq(context.createTypeExpr(), symbol.createTypeExpr()), joinCondition);
	}

	/**
	 * Prepends a check for a match of the branch of the represented data for a join condition.
	 */
	protected SQLExpression prependBranchJoinCondition(DataSymbol context, DataSymbol symbol, SQLExpression joinCondition) {
		return and(eq(context.createBranchExpr(), symbol.createBranchExpr()), joinCondition);
	}

	private SQLExpression prependItemJoinCondition(ItemSymbol context, DataSymbol symbol, SQLExpression joinCondition) {
		joinCondition = prependLifeperiodJoinCondition(context, symbol, joinCondition);
		joinCondition = prependIdentifierJoinCondition(context, symbol, joinCondition);
		joinCondition = prependBranchJoinCondition(context, symbol, joinCondition);
		return joinCondition;
	}

	/**
	 * Prepends a check for a match of the life periods of the represented data for a join
	 * condition.
	 */
	protected abstract SQLExpression prependLifeperiodJoinCondition(ItemSymbol context, DataSymbol symbol,
			SQLExpression joinCondition);

	/**
	 * Prepends a join condition for an {@link InSet}
	 * 
	 * @param contextSymbol
	 *        {@link Symbol} of {@link InSet#getContext()}
	 * @param setSymbol
	 *        {@link Symbol} of {@link InSet#getSetExpr()}
	 */
	protected abstract SQLExpression prependInSetLifeperiodJoinConditio(Symbol contextSymbol, Symbol setSymbol,
			SQLExpression joinCondition);

	@Override
	public SQLTableReference visitReferenceSymbol(ReferenceSymbol sym, SQLTableReference leftTable) {
		leftTable = descend(sym.getTableSymbols(), leftTable);
		leftTable = descend(sym.getFlexAttributeSymbols(), leftTable);
		return leftTable;
	}

	@Override
	public SQLTableReference visitTupleSymbol(TupleSymbol sym, SQLTableReference leftTable) {
		if (leftTable == null) {
			throw new UnsupportedOperationException("Non-initial tuple symbol");
		}
		
		SQLTableReference result = null;
		for (Symbol entrySym : sym.getSymbols()) {
			// Create initial table references.
			SQLTableReference initialTable = entrySym.visit(this, null);
			if (result == null) {
				result = initialTable;
			} else {
				result = innerJoin(result, initialTable);
				if (entrySym instanceof DataSymbol) {
					// in an inner join only non-null matches are contained in the result, so no changes must be done.
					// ((DataSymbol) entrySym).setPotentiallyNull(false);
				}
			}
		}
		return result;
	}
	
	@Override
	public SQLTableReference visitLiteralItemSymbol(LiteralItemSymbol sym, SQLTableReference leftTable) {
		leftTable = descend(sym.getFlexAttributeSymbols(), leftTable);
		return leftTable;
	}
	
	@Override
	public SQLTableReference visitPrimitiveSymbol(PrimitiveSymbol sym, SQLTableReference leftTable) {
		throw new AssertionError("Primitive symbols may not contribute to a join.");
	}
	
	@Override
	public SQLTableReference visitNullSymbol(NullSymbol sym, SQLTableReference leftTable) {
		throw new AssertionError("Null symbols may not contribute to a join.");
	}
	
	private SQLTableReference descend(Collection<? extends Symbol> symbols, SQLTableReference leftTable) {
		for (Symbol sym : symbols) {
			leftTable = sym.visit(this, leftTable);
		}
		return leftTable;
	}

	/**
	 * Checks whether the data of the given table are accessed historic.
	 * 
	 * @param context
	 *        {@link Symbol} of the context of the given table.
	 */
	protected abstract void checkTableHistoricAccessed(Symbol context, SQLTable table);

	/**
	 * Returns the table aliases of the table whose data are accessed historically
	 */
	protected abstract Collection<String> getHistoricAccessedTables();

	/**
	 * Prepends an {@link SQLExpression} to the given condition which ensures that the
	 * {@link ItemSymbol#createRevisionExpr() revision} of the context is contained in the life
	 * period of the given {@link DataSymbol symbol}.
	 */
	protected SQLExpression prependContextInRange(ItemSymbol context, DataSymbol symbol, SQLExpression condition) {
		/* Ensure that the revision of the context is contained in the life period of the symbol. */
		SQLExpression historyCondition = and(
			le(
				context.createRevisionExpr(),
				symbol.createRevMaxExpr()),
			ge(
				context.createRevisionExpr(),
				symbol.createRevMinExpr()));
	
		return and(historyCondition, condition);
	}

	/**
	 * Prepends an {@link SQLExpression} to the given condition which ensures that the
	 * {@link ItemSymbol#createRevisionExpr() revision} of the context is equal to the
	 * {@link ItemSymbol#createRevisionExpr() revision} of the given {@link DataSymbol symbol}.
	 */
	protected SQLExpression prependSameRevision(ItemSymbol context, ItemSymbol symbol, SQLExpression condition) {
		/* Ensure that the revision of the context is the same as that of symbol. */
		return and(eq(context.createRevisionExpr(), symbol.createRevisionExpr()), condition);
	}

	/**
	 * Creates a new {@link JoinBuilder}.
	 * 
	 * @param revisionQuery
	 *        Whether the query is a revision query.
	 */
	public static JoinBuilder createJoinBuilder(boolean revisionQuery) {
		if (revisionQuery) {
			return new RevisionJoinBuilder();
		} else {
			return new HistoryJoinBuilder();
		}
	}

}