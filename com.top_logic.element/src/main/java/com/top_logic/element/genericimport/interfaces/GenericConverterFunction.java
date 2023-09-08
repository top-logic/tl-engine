/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.interfaces;

/**
 * @author    <a href="mailto:TEH@top-logic.com">TEH</a>
 */
public interface GenericConverterFunction {

    public Object map(Object aValue, GenericCache aCache);
}

