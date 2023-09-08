/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import com.top_logic.basic.col.BooleanFlag;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.service.db2.expr.sym.AbstractSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.AbstractSymbolVisitor;
import com.top_logic.knowledge.service.db2.expr.sym.ReferenceSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.Symbol;

/**
 * Utility class to check whether a {@link Symbol} is a historic one, i.e. whether the symbol
 * represents data that are historic.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HistoricSymbol extends AbstractSymbolVisitor<Boolean, Void> {
	
	private static final HistoricSymbol INSTANCE = new HistoricSymbol();

	private HistoricSymbol() {
		// singleton instance
	}
	
	/**
	 * Checks whether the symbol represents historic data.
	 * 
	 * @return Whether the symbol or any of its ancestors is a {@link ReferenceSymbol} which
	 *         represents a historic reference attribute.
	 */
	public static boolean isHistoricSymbol(Symbol symbol) {
		do {
			Boolean isHistoric = symbol.visit(INSTANCE, null);
			if (isHistoric.booleanValue()) {
				return true;
			}
			symbol = symbol.getParent();
		} while (symbol != null);
		
		return false;
	}

	/**
	 * The first {@link Symbol} in the {@link Symbol#getParent() parent hierarchy} that is
	 *         not historic.
	 * 
	 * @see HistoricSymbol#isHistoricSymbol(Symbol)
	 */
	public static Symbol getNonHistoricParent(Symbol symbol) {
		BooleanFlag flag = new BooleanFlag(false);
		return getNonHistoricParent(symbol, flag);
	}

	private static Symbol getNonHistoricParent(Symbol symbol, BooleanFlag found) {
		if (symbol == null) {
			return null;
		}
		Symbol parentResult = getNonHistoricParent(symbol.getParent(), found);
		if (found.get()) {
			return parentResult;
		}
		if (symbol.visit(INSTANCE, null).booleanValue()) {
			found.set(true);
			return symbol.getParent();
		} else {
			return symbol;
		}
	}

	@Override
	public Boolean visitReferenceSymbol(ReferenceSymbol sym, Void arg) {
		HistoryType historyType = sym.getDefinition().getAttribute().getHistoryType();
		switch (historyType) {
			case CURRENT:
				return Boolean.FALSE;
			case HISTORIC:
				return Boolean.TRUE;
			case MIXED:
				/* In mixed references it depends on the content whether the symbol is a historic
				 * one. */
				throw new UnsupportedOperationException();
			default:
				throw HistoryType.noSuchType(historyType);
		}
	}

	@Override
	protected Boolean visitAbstractSymbol(AbstractSymbol sym, Void arg) {
		return Boolean.FALSE;
	}

}

