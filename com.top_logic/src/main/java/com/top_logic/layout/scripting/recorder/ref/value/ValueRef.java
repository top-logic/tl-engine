/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ReferenceFactory;
import com.top_logic.layout.scripting.recorder.ref.ValueRefNamingDirect;
import com.top_logic.layout.scripting.recorder.ref.ValueRefVisitor;
import com.top_logic.layout.scripting.recorder.ref.ValueResolver;

/**
 * Reference to some application value.
 * 
 * @deprecated To be replaced by a {@link ModelName} that is managed by a specialized
 *             {@link ModelNamingScheme}, which offers a modular alternative to the monolithic
 *             building and resolving algorithm of {@link ValueRef}s. For backwards compatibility, a
 *             generic naming scheme {@link ValueRefNamingDirect} dispatches to legacy
 *             {@link ReferenceFactory} and {@link ValueResolver}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
@Abstract
public interface ValueRef extends ModelName {

	/**
	 * Visits this {@link ValueRef} with the given {@link ValueRefVisitor}.
	 * 
	 * @param v
	 *        the visitor
	 * @param arg
	 *        an argument to the visit.
	 * @return The value returned by the visitor.
	 */
	<R, A> R visit(ValueRefVisitor<R, A> v, A arg);
	
}
