/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

/**
 * {@link TLProperty} that is part of a {@link TLClass}.
 * 
 * @see TLAssociationProperty
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TLClassProperty extends TLProperty, TLClassPart {

	@Override
	default <R, A> R visitClassPart(TLClassPartVisitor<R, A> v, A arg) {
		return v.visitClassProperty(this, arg);
	}

}

