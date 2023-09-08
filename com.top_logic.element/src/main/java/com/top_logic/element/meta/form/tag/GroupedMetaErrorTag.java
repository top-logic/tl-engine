/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class GroupedMetaErrorTag extends MetaErrorTag {

    private String name;

    public String getName() {
        return (this.name);
    }

    public void setName(String aName) {
        this.name = aName;

        GroupedMetaInputTag.initAttributeUpdate(this, aName);
    }
}

