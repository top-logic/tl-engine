/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.column;
import static com.top_logic.basic.db.sql.SQLFactory.parameter;
import static com.top_logic.dob.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.table;
import static com.top_logic.knowledge.service.db2.expr.transform.sql.DBSQLFactory.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.db.sql.SQLBoolean;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLLimit;
import com.top_logic.basic.db.sql.SQLLiteral;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.db.sql.SQLStatement;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.basic.db.sql.SQLTableReference;
import com.top_logic.basic.sql.CollationHint;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.search.AbstractQuery;
import com.top_logic.knowledge.search.BranchParam;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionVisitor;
import com.top_logic.knowledge.search.HistoryQuery;
import com.top_logic.knowledge.search.Order;
import com.top_logic.knowledge.search.OrderSpec;
import com.top_logic.knowledge.search.OrderTuple;
import com.top_logic.knowledge.search.OrderVisitor;
import com.top_logic.knowledge.search.RangeParam;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.SetExpressionVisitor;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.db2.CollationHintComputation;
import com.top_logic.knowledge.service.db2.expr.exec.ForeverAlive;
import com.top_logic.knowledge.service.db2.expr.exec.HistorySearch;
import com.top_logic.knowledge.service.db2.expr.exec.LifePeriodColumnBuilder;
import com.top_logic.knowledge.service.db2.expr.exec.LifePeriodColumnBuilder.TableInfo;
import com.top_logic.knowledge.service.db2.expr.exec.LifePeriodComputation;
import com.top_logic.knowledge.service.db2.expr.exec.LifePeriodComputationBuilder;
import com.top_logic.knowledge.service.db2.expr.exec.LifePeriodIntersection;
import com.top_logic.knowledge.service.db2.expr.exec.RowLifePeriod;
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
import com.top_logic.knowledge.service.db2.expr.transform.UnionDecomposition;

/**
 * Visitor that creates SQL from a {@link SetExpression}.
 * 
 * <p>
 * {@link SymbolVisitor} that creates an identity comparison of the visited {@link Symbol} and the
 * argument {@link Symbol}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLBuilder implements SymbolVisitor<SQLExpression, Symbol>, OrderVisitor<Void, Void>, JoinCollector {

	/** Synthetic parameter later filled with the stop row argument. */
	public static final String STOP_ROW_PARAM = "_stopRow";

	/** Synthetic parameter later filled with the start row argument. */
	public static final String START_ROW_PARAM = "_startRow";

	/** Synthetic parameter later filled with the requested data revision. */
	public static final String REQUESTED_REVISION_PARAM = "_requestedRevision";

	/** Synthetic parameter later filled with the history context of the request. */
	public static final String HISTORY_CONTEXT_PARAM = "_historyContext";

	static final Void none = null;

	private final ExpressionVisitor<SQLExpression, Void> _expressionEvaluator;
	
	private final SetExpressionVisitor<SQLExpression, Void> _whereBuilder;
	
	private final SymbolVisitor<SQLExpression, Void> _dereferencer;
	
	private final JoinBuilder _joinBuilder;

	private List<SQLOrder> orders;

	private SQLTableReference join;

	private SQLExpression where;

	private final boolean needsBranchCheck;


	/* package protected */SQLBuilder(boolean revisionQuery, boolean needsBranchCheck) {
		_joinBuilder = JoinBuilder.createJoinBuilder(revisionQuery);
		
		DeReferencer dereferencer = new DeReferencer();
		WhereBuilder whereBuilder = new WhereBuilder();
		_expressionEvaluator = new ExpressionEvaluator(this, _joinBuilder, this, dereferencer, whereBuilder);
		dereferencer.initExpressionEvaluator(_expressionEvaluator);
		_dereferencer = dereferencer;
		whereBuilder.initExpressionEvaluator(_expressionEvaluator);
		_whereBuilder = whereBuilder;
		
		this.needsBranchCheck = needsBranchCheck;
	}

	/**
	 * Creates an {@link SQLStatement} from the given {@link RevisionQuery}
	 * 
	 * @param sqlDialect
	 *        The SQL dialect to use.
	 * @param query
	 *        the query to create SQL for.
	 * @param fullLoad
	 *        Whether all columns of the result table should be selected. If <code>false</code>,
	 *        only the identifier columns are selected.
	 * 
	 * @return SQL representation of the given query
	 */
	public static SQLStatement createRevisionSearchSQL(DBHelper sqlDialect, TypeSystem typeSystem,
			RevisionQuery<?> query, boolean fullLoad) {
		addParameter(query, REQUESTED_REVISION_PARAM);
		addParameter(query, HISTORY_CONTEXT_PARAM);
		boolean needsBranchCheck = needsBranchCheck(query.getBranchParam());
		SQLLimit limit;
		switch (query.getRangeParam()) {
			case complete: {
				limit = SQLLimit.NO_LIMIT;
				break;
			}
			case first: {
				limit = limitFirstRow();
				break;
			}
			case head: {
				SQLLiteral startRow = literalInteger(SQLLimit.FIRST_ROW);
				String stopParam = STOP_ROW_PARAM;
				addParameter(query, stopParam);
				limit = limit(startRow, parameter(DBType.INT, stopParam));
				break;
			}
			case range: {
				String startParam = START_ROW_PARAM;
				addParameter(query, startParam);
				String stopParam = STOP_ROW_PARAM;
				addParameter(query, stopParam);
				limit = limit(parameter(DBType.INT, startParam), parameter(DBType.INT, stopParam));
				break;
			}
			default:
				throw RangeParam.unknownRangeParam(query.getRangeParam());
		}

		return createRevisionSearchSQL(sqlDialect, query, fullLoad, needsBranchCheck, typeSystem.multipleBranches(),
			limit);
	}

	private static void addParameter(AbstractQuery<?> query, String newParam) {
		Map<String, Integer> argumentIndexByName = query.getArgumentIndexByName();
		int newIndex = argumentIndexByName.size();
		Integer parameterClash = argumentIndexByName.put(newParam, newIndex);
		if (parameterClash != null) {
			throw new IllegalStateException("Parameter '" + newParam + "' is reserved for internals. Query: " + query);
		}
	}

	private static SQLStatement createRevisionSearchSQL(DBHelper sqlDialect, RevisionQuery<?> query, boolean fullLoad,
			boolean needsBranchCheck, boolean multipleBranches, SQLLimit limit) {
		List<SetExpression> exprs = UnionDecomposition.decomposeUnions(query.getSearch());
		Order order = query.getOrder();
		

		ArrayList<SQLSelect> selects = new ArrayList<>();
		for (SetExpression subExpr : exprs) {
			SQLSelect subSelect =
				SQLBuilder.createRevisionSearch(sqlDialect, fullLoad, needsBranchCheck, multipleBranches, subExpr,
					order);
			selects.add(subSelect);
		}
		
		int numberSelects = selects.size();
		if (numberSelects > 1) {
			List<SQLOrder> orderBy = new ArrayList<>(selects.get(0).getOrderBy());
			selects.get(0).getOrderBy().clear();
			for (int i = 1; i < numberSelects; i++) {
				SQLSelect select = selects.get(i);
				if (!select.getOrderBy().isEmpty() && !orderBy.equals(select.getOrderBy())) {
					throw new IllegalArgumentException("different selects for union with different order.");
				}
				select.getOrderBy().clear();
			}
			return union(selects, orderBy);
		} else {
			SQLSelect sqlSelect = selects.get(0);
			sqlSelect.setLimit(limit);
			return sqlSelect;
		}
	}
	
	private static SQLSelect createRevisionSearch(DBHelper sqlDialect, boolean fullLoad, boolean needsBranchCheck, boolean multipleBranches, SetExpression expr, Order order) {
		SQLBuilder builder = new SQLBuilder(true, needsBranchCheck);
		
		Set<? extends MetaObject> types = expr.getConcreteTypes();
		if (types.size() != 1) {
			// TODO
			throw new UnsupportedOperationException("Polymorphic queries are not supported.");
		}
	
		MetaObject monomorphicType = types.iterator().next();
		if (! (monomorphicType instanceof MOClass)) {
			// TODO
			throw new UnsupportedOperationException("Only queries of item type supported: " + monomorphicType);
		}
		
		MOClass monomorphicItemType = (MOClass) monomorphicType;
		ItemSymbol itemSymbol = (ItemSymbol) expr.getSymbol();
		TableSymbol resultSym = itemSymbol.dereference(monomorphicItemType);
		
		builder.build(expr, order);
		SQLTableReference join = builder.getJoin();
		SQLExpression where = builder.getWhere();
		List<SQLOrder> orders = builder.getOrders();
		
		// List<ColumnDefinition> columns = columnDefs(resultSym.getTableAlias(), monomorphicItemType);
		List<SQLColumnDefinition> columns;
		SQLExpression historyCondition =
			and(
				ge(
					itemSymbol.createRevMaxExpr(),
					currentRevision()),
				le(
					itemSymbol.createRevMinExpr(),
					currentRevision()));
		where = and(historyCondition, where);

		boolean distinct = !(join instanceof SQLTable);
		String tableAlias = resultSym.getTableAlias();
		MOClass type = resultSym.getType();
		if (fullLoad) {
			if (distinct && !sqlDialect.supportsDistinctLob() && containsLobs(sqlDialect, type)) {
				List<SQLColumnDefinition> idColumns;
				SQLExpression idJoinCondition;

				String dataAccess = "dt";
				String idAccess = "idt";

				try {
					idColumns = new ArrayList<>();
					SQLExpression branchEquality;
					if (multipleBranches) {
						String branchName =
							type.getAttribute(BasicTypes.BRANCH_ATTRIBUTE_NAME).getDbMapping()[0].getDBName();
						idColumns.add(columnDef(column(tableAlias, branchName), null));
						branchEquality = eqSQL(column(dataAccess, branchName), column(idAccess, branchName));
					} else {
						branchEquality = literalTrueLogical();
					}
					String idName =
						type.getAttribute(BasicTypes.IDENTIFIER_ATTRIBUTE_NAME).getDbMapping()[0].getDBName();
					String revMaxName =
						type.getAttribute(BasicTypes.REV_MAX_ATTRIBUTE_NAME).getDbMapping()[0].getDBName();
					idColumns.add(columnDef(column(tableAlias, idName), null));
					idColumns.add(columnDef(column(tableAlias, revMaxName), null));

					SQLExpression idEquality = eqSQL(column(dataAccess, idName), column(idAccess, idName));
					SQLExpression revisionEquality = eqSQL(column(dataAccess, revMaxName), column(idAccess, revMaxName));
					idJoinCondition = and(and(branchEquality, idEquality), revisionEquality);
				} catch (NoSuchAttributeException ex) {
					throw new UnreachableAssertion(ex);
				}

				for (SQLOrder orderExpr : orders) {
					orderExpr.visit(SetTableAlias.INSTANCE, dataAccess);
				}

				List<SQLOrder> noOrder = Collections.emptyList();

				return select(false,
					allColumnRefs(type, dataAccess),
					join(true,
						table(type.getDBMapping(), dataAccess),
						subQuery(
							select(distinct, idColumns, join, where, noOrder),
							idAccess
						),
						idJoinCondition),
					SQLBoolean.TRUE, orders);
			}
			columns = allColumnRefs(type, tableAlias);
		} else {
			try {
				if (needsBranchCheck || !multipleBranches) {
					// In a single branch query, there is no need to retrieve the branch information
					// as result from the database.
					columns = Collections.singletonList(
						columnDef(column(tableAlias, type.getAttribute(BasicTypes.IDENTIFIER_ATTRIBUTE_NAME).getDbMapping()[0].getDBName()), null));
				} else {
					columns = Arrays.asList(
						columnDef(column(tableAlias, type.getAttribute(BasicTypes.BRANCH_ATTRIBUTE_NAME).getDbMapping()[0].getDBName()), null),
						columnDef(column(tableAlias, type.getAttribute(BasicTypes.IDENTIFIER_ATTRIBUTE_NAME).getDbMapping()[0].getDBName()), null));
				}

				if (distinct && !orders.isEmpty()) {
					// Add ordering criterion to the selection list, since this is required for
					// DISTINCT queries (at least for Oracle).
					List<SQLColumnDefinition> distinctColumns = new ArrayList<>();
					String distinctAlias = "ds";
					for (SQLColumnDefinition column : columns) {
						distinctColumns.add(copy(column));
						column.getExpr().visit(SetTableAlias.INSTANCE, distinctAlias);
					}
					for (SQLOrder orderExpr : orders) {
						distinctColumns.add(columnDef(copy(orderExpr.getExpr()), null));
						orderExpr.getExpr().visit(SetTableAlias.INSTANCE, distinctAlias);
					}

					List<SQLOrder> noOrder = Collections.emptyList();
					return select(false,
						columns,
						subQuery(select(distinct, distinctColumns, join, where, noOrder), distinctAlias),
						SQLBoolean.TRUE,
						orders);
				}
			} catch (NoSuchAttributeException ex) {
				throw new UnreachableAssertion(ex);
			}
		}

		return select(distinct, columns, join, where, orders);
	}

	private static boolean containsLobs(DBHelper sqlDialect, MOClass clazz) {
		for (MOAttribute attribute : clazz.getAttributes()) {
			for (DBAttribute dbAttr : attribute.getDbMapping()) {
				DBType dbType = dbAttr.getSQLType();
				long size = dbAttr.getSQLSize();
				int precision = dbAttr.getSQLPrecision();
				boolean binary = dbAttr.isBinary();
				boolean notNull = dbAttr.isSQLNotNull();
				boolean isLob = sqlDialect.isLobType(dbType, size, precision, notNull, binary);
				if (isLob) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Creates {@link HistorySearch}es from the given {@link HistoryQuery}.
	 * 
	 * @param monomorphicQuery
	 *        The query to create SQL for. It is expected that the query contains only top level
	 *        unions.
	 * 
	 * @return For each part of the potential unions of the {@link HistoryQuery} a
	 *         {@link HistorySearch}.
	 */
	public static List<HistorySearch> createHistorySearches(TypeSystem typeSystem, HistoryQuery monomorphicQuery) {
		addParameter(monomorphicQuery, REQUESTED_REVISION_PARAM);
		addParameter(monomorphicQuery, HISTORY_CONTEXT_PARAM);

		List<SetExpression> exprs = UnionDecomposition.decomposeUnions(monomorphicQuery.getSearch());
		boolean needsBranchCheck = needsBranchCheck(monomorphicQuery.getBranchParam());
		ArrayList<HistorySearch> searches = new ArrayList<>();
		for (SetExpression subExpr : exprs) {
			searches.add(SQLBuilder.createHistorySearch(typeSystem, needsBranchCheck, subExpr));
		}
		return searches;
	}

	private static boolean needsBranchCheck(BranchParam branchParam) {
		switch (branchParam) {
			case single:
				return true;
			case all:
				return false;
			case set:
			case without:
				// a branch check is here also needed but not currently elaborated
				throw new UnsupportedOperationException();
			default:
				throw BranchParam.unknownRangeParam(branchParam);
		}
	}
	
	@SuppressWarnings("unused")
	private static HistorySearch createHistorySearch(TypeSystem typeSystem, boolean needsBranchCheck, SetExpression expr) {
		
		NoNotInSet.checkNoNotInSet(expr);
		NoMixedReferences.checkNoMixedReferences(expr);

		SQLBuilder builder = new SQLBuilder(false, needsBranchCheck);
		
		ItemSymbol resultSym = (ItemSymbol) expr.getSymbol();
		
		builder.build(expr, null);
		Collection<String> historicAccessedTables = builder._joinBuilder.getHistoricAccessedTables();
		
		SQLTableReference join = builder.getJoin();
		SQLExpression where = builder.getWhere();
		
		// List<ColumnDefinition> columns = columnDefs(resultSym.getTableAlias(), monomorphicItemType);
		List<SQLColumnDefinition> columns;
		
		LifePeriodColumnBuilder columnBuilder = new LifePeriodColumnBuilder(historicAccessedTables);
		join.visit(columnBuilder, none);
		
		List<SQLColumnDefinition> oracleColumns;
		LifePeriodComputation lifePeriodComputation;

		if (false) {
			/* Ticket #18308: It seems too much complex to compute the lifePeriodComputation from
			 * the "where" clause. There is currently no test or an idea for a test which needs that
			 * complexity.
			 * 
			 * Moreover if there is no "where" clause*, the result is wrong. */
			LifePeriodComputationBuilder computationBuilder = new LifePeriodComputationBuilder(historicAccessedTables);
			lifePeriodComputation = computationBuilder.build(where);
			oracleColumns = computationBuilder.getOracleColumns();
		} else {
			lifePeriodComputation = ForeverAlive.INSTANCE;
			for (TableInfo info : columnBuilder.getTableInfos().values()) {
				String tableAlias = info.getTableAlias();
				if (historicAccessedTables.contains(tableAlias)) {
					continue;
				}
				lifePeriodComputation =
					LifePeriodIntersection.lifePeriodIntersection(lifePeriodComputation, new RowLifePeriod(tableAlias));
			}
			oracleColumns = Collections.emptyList();
		}
		
		columns = new ArrayList<>();
		
		// Add identifier columns.
		final int objectIdColumnOffset = columns.size();
		long branch = addBranchColumn(resultSym, columns);
		String typeName = addTypeColumn(resultSym, columns);
		columns.add(columnDef(resultSym.createIdentifierExpr(), null));
		
		// Add revision columns for all accessed tables whose data are not accessed historically.
		Collection<TableInfo> tableInfos = columnBuilder.getTableInfos().values();
		HashMap<String, Integer> lifeRangeColumns = MapUtil.newMap(tableInfos.size());
		for (TableInfo info : tableInfos) {
			lifeRangeColumns.put(info.getTableAlias(), columns.size());
			columns.add(columnDef(notNullColumn(info.getTableAlias(), info.getRevMinColumn()), null));
			columns.add(columnDef(notNullColumn(info.getTableAlias(), info.getRevMaxColumn()), null));
		}
	
		// Add oracle columns that report the truth of all subexpressions
		// under OR nodes in the WHERE clause.
		final int oracleExpressionColumnOffset = columns.size();
		columns.addAll(oracleColumns);
				
		ObjectIdReader objectIdReader = new ObjectIdReader(typeSystem, objectIdColumnOffset, typeName, branch);
		LifePeriodReader lifePeriodReader =
			new LifePeriodReader(lifePeriodComputation, oracleExpressionColumnOffset, lifeRangeColumns);
		SQLSelect select = select(true, columns, join, where, new ArrayList<>());
		
		return new HistorySearch(select, objectIdReader, lifePeriodReader);
	}

	private static long addBranchColumn(ItemSymbol resultSym, List<SQLColumnDefinition> columns) {
		SQLExpression branchExpr = resultSym.createBranchExpr();
		long branch;
		if (branchExpr instanceof SQLLiteral) {
			branch = ((Long) ((SQLLiteral) branchExpr).getValue()).longValue();
		} else {
			branch = ObjectIdReader.NO_BRANCH;
			columns.add(columnDef(branchExpr, null));
		}
		return branch;
	}

	private static String addTypeColumn(ItemSymbol resultSym, List<SQLColumnDefinition> columns) {
		SQLExpression typeExpr = resultSym.createTypeExpr();
		String typeName;
		if (typeExpr instanceof SQLLiteral) {
			typeName = (String) ((SQLLiteral) typeExpr).getValue();
		} else {
			typeName = ObjectIdReader.NO_TYPE;
			columns.add(columnDef(typeExpr, null));
		}
		return typeName;
	}
	
	private SQLTableReference getJoin() {
		return join;
	}

	private SQLExpression getWhere() {
		return where;
	}

	private List<SQLOrder> getOrders() {
		return orders;
	}

	private void build(SetExpression expr, Order order) {
		this.join = _joinBuilder.createJoin(expr);
		this.where = expr.visitSetExpr(_whereBuilder, none);
		this.orders = new ArrayList<>();
		if (order != null) {
			order.visitOrder(this, none);
		}
	}
	
	// SymbolVisitor implementation
	
	@Override
	public SQLExpression visitPrimitiveSymbol(PrimitiveSymbol leftSym, Symbol rightSym) {
		return visitValueSymbol(leftSym, rightSym);
	}
	
	@Override
	public SQLExpression visitRowAttributeSymbol(RowAttributeSymbol leftSym, Symbol rightSym) {
		return visitValueSymbol(leftSym, rightSym);
	}
	
	@Override
	public SQLExpression visitFlexAttributeSymbol(FlexAttributeSymbol leftSym, Symbol rightSym) {
		return visitValueSymbol(leftSym, rightSym);
	}

	private SQLExpression visitValueSymbol(Symbol leftSym, Symbol rightSym) {
		SQLExpression leftValue = leftSym.visit(_dereferencer, none);
		if (rightSym == NullSymbol.INSTANCE) {
			return isNull(leftValue);
		} else {
			SQLExpression rightValue = rightSym.visit(_dereferencer, none);
			return eq(leftValue, rightValue);
		}
	}

	@Override
	public SQLExpression visitReferenceSymbol(ReferenceSymbol leftSym, Symbol rightSym) {
		return visitItemSymbol(leftSym, rightSym);
	}

	@Override
	public SQLExpression visitTableSymbol(TableSymbol leftSym, Symbol rightSym) {
		return visitItemSymbol(leftSym, rightSym);
	}
	
	@Override
	public SQLExpression visitLiteralItemSymbol(LiteralItemSymbol leftSym, Symbol rightSym) {
		return visitItemSymbol(leftSym, rightSym);
	}
	
	private SQLExpression visitItemSymbol(ItemSymbol leftSym, Symbol rightSym) {
		return createMatch(leftSym, rightSym, needsBranchCheck, false);
	}

	static SQLExpression createMatch(ItemSymbol leftSym, Symbol rightSym, boolean withBranchCheck, boolean withRevCheck) {
		if (rightSym == NullSymbol.INSTANCE) {
			/* Either a comparison with the "null" value (which leads to comparison with 0, because
			 * for null references 0 is stored), or may be a join with another table in which case
			 * the column is */
			if (leftSym.isPotentiallyNull()) {
				/* Symbol represents a table or a reference in a table on the right part of a left
				 * join. */
				if (leftSym.getParent() instanceof ItemSymbol
					&& !((ItemSymbol) leftSym.getParent()).isPotentiallyNull()) {
					/* This is the first symbol on the right side of a join; Either the join does
					 * not produce a match (all columns "null"), or it has a non 0 id. */
					return isNull(leftSym.createIdentifierExpr());
				} else {
					/* Either the join does not create a match, or in the matched row, there is null
					 * reference. */
					return or(isNull(leftSym.createIdentifierExpr()), isId0(leftSym.createIdentifierExpr()));
				}
			} else {
				return isId0(leftSym.createIdentifierExpr());
			}
		} else {
			ItemSymbol rightItemSym = (ItemSymbol) rightSym;
			return createNonNullMatch(leftSym, rightItemSym, withBranchCheck, withRevCheck);
		}
	}

	private static SQLExpression isId0(SQLExpression leftId) {
		return eq(leftId, literalLong(0));
	}

	private static SQLExpression createNonNullMatch(ItemSymbol leftSym, ItemSymbol rightSym, boolean withBranchCheck,
			boolean withRevCheck) {

		/* No need to include type expression, because the identifier is unique over all types. */
		SQLExpression leftId = leftSym.createIdentifierExpr();
		SQLExpression rightId = rightSym.createIdentifierExpr();
		SQLExpression idCheck = eqSQL(leftId, rightId);
		SQLExpression branchRevCheck = createBranchAndRevisionCheck(leftSym, rightSym, withBranchCheck, withRevCheck);
		return and(idCheck, branchRevCheck);
	}

	private static SQLExpression createBranchAndRevisionCheck(ItemSymbol leftSym, ItemSymbol rightSym,
			boolean withBranchCheck, boolean withRevCheck) {
		SQLExpression result = SQLBoolean.TRUE;
		if (withRevCheck) {
			SQLExpression leftRev = leftSym.createRevisionExpr();
			SQLExpression rightRev = rightSym.createRevisionExpr();
			SQLExpression revisionCheck = eqSQL(leftRev, rightRev);
			result = and(revisionCheck, result);
		}
		if (withBranchCheck) {
			SQLExpression leftBrc = leftSym.createBranchExpr();
			SQLExpression rightBrc = rightSym.createBranchExpr();
			SQLExpression branchCheck = eqSQL(leftBrc, rightBrc);
			result = and(branchCheck, result);
		}
		if (result != SQLBoolean.TRUE) {
			/* Branch and Revision Equality is just necessary, when there is a real match, i.e. the
			 * ids are both 0 (= null) */
			SQLExpression leftId = leftSym.createIdentifierExpr();
			/* No need to check both id's, because id equality check is prepended. */
			result = or(isId0(leftId), result);
		}
		return result;
	}

	@Override
	public SQLExpression visitTupleSymbol(TupleSymbol leftSym, Symbol rightSym) {
		List<Symbol> leftEntrySymbols = leftSym.getSymbols();
		List<Symbol> rightEntrySymbols;
		if (rightSym == NullSymbol.INSTANCE) {
			rightEntrySymbols = null;
		} else {
			rightEntrySymbols = ((TupleSymbol) rightSym).getSymbols();
			assert leftEntrySymbols.size() == rightEntrySymbols.size() : "Size mismatch should be found during type analysis.";
		}
		
		SQLExpression result = SQLBoolean.TRUE;
		for (int n = 0, cnt = leftEntrySymbols.size(); n < cnt; n++) {
			Symbol leftEntrySym = leftEntrySymbols.get(n);
			Symbol rightEntrySym;
			if (rightEntrySymbols == null) {
				rightEntrySym = NullSymbol.INSTANCE;
			} else {
				rightEntrySym = rightEntrySymbols.get(n);
			}
			SQLExpression equalityExpr = leftEntrySym.visit(this, rightEntrySym);
			result = and(result, equalityExpr);
		}
		
		return result;
	}
	
	@Override
	public SQLExpression visitNullSymbol(NullSymbol sym, Symbol rightSym) {
		if (rightSym == NullSymbol.INSTANCE) {
			return SQLBoolean.TRUE;
		} else {
			// Dispatch to other symbol.
			return rightSym.visit(this, sym);
		}
	}

	@Override
	public Void visitOrderSpec(OrderSpec expr, Void arg) {
		checkComparable(expr.getOrderExpr());
		SQLExpression order = expr.getOrderExpr().visit(_expressionEvaluator, none);
		CollationHint collationHint = expr.getOrderExpr().visit(CollationHintComputation.INSTANCE, none);
		this.orders.add(order(expr.isDescending(), collationHint, order));
		return none;
	}

	private void checkComparable(Expression expr) {
		Set<MetaObject> concreteTypes = expr.getConcreteTypes();
		if (concreteTypes.size() != 1) {
			throw new IllegalArgumentException();
		}
		MetaObject type = concreteTypes.iterator().next();
		boolean comparable;
		if (type == MOPrimitive.STRING)
			comparable = true;
		else if (type == MOPrimitive.TLID)
			comparable = true;
		else if (type == MOPrimitive.LONG)
			comparable = true;
		else if (type == MOPrimitive.DOUBLE)
			comparable = true;
		else if (type == MOPrimitive.FLOAT)
			comparable = true;
		else if (type == MOPrimitive.DATE)
			comparable = true;
		else if (type == MOPrimitive.CHARACTER)
			comparable = true;
		else if (type == MOPrimitive.INTEGER)
			comparable = true;
		else if (type == MOPrimitive.SHORT)
			comparable = true;
		else if (type == MOPrimitive.BYTE)
			comparable = true;
		else
			comparable = false;
		if (!comparable) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Void visitOrderTuple(OrderTuple expr, Void arg) {
		for (OrderSpec spec : expr.getOrderSpecs()) {
			spec.visitOrder(this, arg);
		}
		return none;
	}

	@Override
	public void addLeftJoin(SQLTableReference setJoin, SQLExpression condition) {
		this.join = leftJoin(this.join, setJoin, condition);
	}
	
}
