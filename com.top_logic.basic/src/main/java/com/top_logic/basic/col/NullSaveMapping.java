/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Makes another mapping <code>null</code> save.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class NullSaveMapping<S, D> implements Mapping<S, D> {

    private Mapping<? super S, ? extends D> mapping;

    /**
     * Creates a {@link NullSaveMapping}.
     *
     * @param mapping The mapping to wrap.
     */
    public NullSaveMapping(Mapping<? super S, ? extends D> mapping) {
        this.mapping = mapping;
    }

    @Override
	public D map(S input) {
        return input == null ? null : mapping.map(input);
    }

}
