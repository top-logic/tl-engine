/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import com.top_logic.basic.util.Utils;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.util.WrapperUtil;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * Navigate a {@link TLAssociation}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AssociationNavigation extends SearchExpression {

	private SearchExpression _source;

	private final TLAssociationEnd _sourceEnd;

	private final TLAssociationEnd _destinationEnd;

	/**
	 * Creates an {@link AssociationNavigation}.
	 * 
	 * @param source
	 *        See {@link #getSource()}.
	 * @param sourceEnd
	 *        See {@link #getSourceEnd()}.
	 * @param destinationEnd
	 *        See {@link #getDestinationEnd()}.
	 */
	AssociationNavigation(SearchExpression source, TLAssociationEnd sourceEnd, TLAssociationEnd destinationEnd) {
		_source = source;
		_sourceEnd = sourceEnd;
		_destinationEnd = destinationEnd;
		checkEndsMatch();
	}

	/** Check that both ends are from the same association. */
	private void checkEndsMatch() {
		TLAssociation sourceAssociation = getSourceEnd().getOwner();
		TLAssociation destinationAssociation = getDestinationEnd().getOwner();
		if (!Utils.equals(sourceAssociation, destinationAssociation)) {
			throw new IllegalArgumentException("The source association end and the destination association end "
				+ "have to be from the same association. Source end: " + Utils.debug(getSourceEnd())
				+ " Destination end: " + Utils.debug(getDestinationEnd()));
		}
	}

	/**
	 * The source object from which to navigate.
	 */
	public SearchExpression getSource() {
		return _source;
	}

	/**
	 * @see #getSource()
	 */
	public void setSource(SearchExpression source) {
		_source = source;
	}

	/**
	 * The {@link TLAssociationEnd} pointing to the {@link #getSource()}.
	 */
	public TLAssociationEnd getSourceEnd() {
		return _sourceEnd;
	}

	/**
	 * The {@link TLAssociationEnd} pointing to the destination of this navigation.
	 */
	public TLAssociationEnd getDestinationEnd() {
		return _destinationEnd;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitAssociationNavigation(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		TLObject source = asTLObject(getSource().evalWith(definitions, args));
		if (source == null) {
			return null;
		}
		return evalInternal(source);
	}

	private Object evalInternal(TLObject source) {
		KnowledgeBase knowledgeBase = WrapperUtil.getKnowledgeBase(source);
		RevisionQuery<TLObject> query = createQuery(source);
		return knowledgeBase.search(query);
	}

	private RevisionQuery<TLObject> createQuery(TLObject source) {
		KnowledgeObject sourceObject = (KnowledgeObject) source.tHandle();
		String sourceEnd = getSourceEnd().getName();
		String destinationEnd = getDestinationEnd().getName();
		String associationTable = getAssociationTableName();
		return createQueryInternal(sourceObject, sourceEnd, destinationEnd, associationTable);
	}

	private String getAssociationTableName() {
		TLAssociation association = getSourceEnd().getOwner();
		return ApplicationObjectUtil.tableName(association);
	}

	private RevisionQuery<TLObject> createQueryInternal(KnowledgeObject sourceObject, String sourceEnd,
			String destinationEnd, String associationTable) {
		SetExpression setExpression = createExpression(sourceObject, sourceEnd, destinationEnd, associationTable);
		RevisionQuery<TLObject> query = queryResolved(setExpression, TLObject.class);
		query.setIdPreload();
		return query;
	}

	private SetExpression createExpression(KnowledgeObject sourceObject, String sourceEnd, String destinationEnd,
			String associationTable) {
		return map(
			filter(
				allOf(associationTable),
				eqBinary(
					reference(associationTable, sourceEnd),
					literal(sourceObject))),
			reference(associationTable, destinationEnd));
	}

}
