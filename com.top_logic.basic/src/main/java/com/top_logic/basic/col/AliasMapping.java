/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collections;
import java.util.Map;

/**
 * This Mapping looks up the input in an alias map and gets the value of the map if
 * available. Otherwise, the given input is returned.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class AliasMapping implements Mapping {

    /** The alias map to look up. */
    private Map aliasMap;

    /**
     * If this flag is set and the input was not found in map, the input will be returned
     * itself instead of <code>null</code>.
     */
    private boolean returnInput;


    /**
     * Creates a new AliasMapping with an empty lookup map, which result in the identity
     * mapping.
     */
    public AliasMapping() {
        this.aliasMap = Collections.EMPTY_MAP;
        this.returnInput = true;
    }

    /**
     * Creates a new AliasMapping with the given lookup map and returning the input itself
     * if the input was not found.
     *
     * @param aliasMap
     *        The alias map to look up
     */
    public AliasMapping(Map aliasMap) {
        this.aliasMap = aliasMap;
        this.returnInput = true;
    }

    /**
     * Creates a new AliasMapping with the given lookup map.
     *
     * @param aliasMap
     *        The alias map to look up
     * @param returnInputIfNotFound
     *        If this flag is set and the input was not found in map, the input will be
     *        returned itself instead of <code>null</code>
     */
    public AliasMapping(Map aliasMap, boolean returnInputIfNotFound) {
        this.aliasMap = aliasMap;
        this.returnInput = returnInputIfNotFound;
    }


    @Override
	public Object map(Object aInput) {
        return aliasMap.containsKey(aInput) ? aliasMap.get(aInput) : (returnInput ? aInput : null);
    }

}
