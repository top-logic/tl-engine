/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.merge;

/**
 * Validators validate MergeTreeNodes by creating MergeMessages. 
 * 
 * Validators are statefull and will be recreated for new validations.
 * 
 * @author    <a href="mailto:jco@top-logic.com">J&ouml;rg Connotte</a>
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public interface Validator {
    
    /** 
     * Validate the data found in MergeTreeNode.
     * 
     * @return if validation succeeded , same as {@link MergeTreeNode#hasErrors()} afterwards.
     */
    public boolean validate (MergeTreeNode aNode);
        
}
