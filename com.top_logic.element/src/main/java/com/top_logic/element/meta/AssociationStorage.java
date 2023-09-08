/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.Collection;
import java.util.Set;

import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ReferenceStorage} that stores the reference in an separate table, i.e. the for the
 * reference an own row exists in the database.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface AssociationStorage extends ReferenceStorage {

	/**
	 * The name of the association table in which this {@link AssociationStorage} stores the data
	 * for the {@link TLStructuredTypePart}.
	 */
	String getTable();

	/**
	 * Whether the {@link #getTable() table} stores the date for only one
	 * {@link TLStructuredTypePart} or for many.
	 */
	boolean monomophicTable();

	/**
	 * The {@link AbstractAssociationQuery} describing the reference from the target of the reference to the
	 * sources.
	 * 
	 * @see #getOutgoingQuery()
	 */
	AbstractAssociationQuery<KnowledgeAssociation, Set<KnowledgeAssociation>> getIncomingQuery();

	/**
	 * The {@link AbstractAssociationQuery} describing the reference from the source of the reference to the
	 * targets.
	 * 
	 * @see #getIncomingQuery()
	 */
	AbstractAssociationQuery<KnowledgeAssociation, ? extends Collection<KnowledgeAssociation>> getOutgoingQuery();

}
