/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

/**
 * {@link TLProperty} that is part of a {@link TLAssociation}.
 * 
 * @see TLClassProperty
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TLAssociationProperty extends TLProperty, TLAssociationPart {

	@Override
	default <R, A> R visitAssociationPart(TLAssociationPartVisitor<R, A> v, A arg) {
		return v.visitAssociationProperty(this, arg);
	}

}

