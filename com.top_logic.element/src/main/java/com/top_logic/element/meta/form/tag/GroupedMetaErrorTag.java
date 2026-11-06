/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

/**
 * {@link MetaErrorTag} for an attribute of the object of the enclosing {@link MetaGroupTag}.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class GroupedMetaErrorTag extends MetaErrorTag {

    private String name;

    /**
     * The name of the attribute whose error is displayed.
     */
    public String getName() {
        return (this.name);
    }

    /**
     * Setter for {@link #getName()}.
     */
    public void setName(String aName) {
        this.name = aName;

        GroupedMetaInputTag.initAttributeUpdate(this, aName);
    }
}

