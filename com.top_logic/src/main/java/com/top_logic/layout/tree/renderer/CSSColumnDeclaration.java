/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.template.ControlProvider;


/**
 * {@link DefaultColumnDeclaration} with additional CSS classes for the column cells and the
 * column header.
 *
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
public class CSSColumnDeclaration extends DefaultColumnDeclaration {

    private String cssClass;
    private String headerCssClass;
    
    public CSSColumnDeclaration(ControlProvider someCp) {
        super(someCp);
    }

    public CSSColumnDeclaration(int someType) {
        super(someType);
    }

	public CSSColumnDeclaration(Renderer<Object> aRenderer) {
        super(aRenderer);
    }

    /** The CSS class of the cells of this column. */
    public String getCssClass() {
        return (this.cssClass);
    }

    /** Setter for {@link #getCssClass()}. */
    public void setCssClass(String aCssClass) {
        this.cssClass = aCssClass;
    }

    /** Setter for {@link #getHeaderCssClass()}. */
    public void setHeaderCssClass(String aHeaderCssClass) {
        this.headerCssClass = aHeaderCssClass;
    }

    /** The CSS class of the header cell of this column. */
    public String getHeaderCssClass() {
        return headerCssClass;
    }
    
    

}
