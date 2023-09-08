/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.util.error.TopLogicException;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class GroupedMetaLabelTag extends MetaLabelTag {

    private String name;

    public String getName() {
        return (this.name);
    }

    public void setName(String aName) {
        this.name = aName;

        try {
            GroupedMetaInputTag.initAttributeUpdate(this, aName);
        }
        catch (Exception ex) {
            throw new TopLogicException(this.getClass(), "name.set", new Object[] {aName}, ex);
        }
    }

}

