/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.template.ControlProvider;


/**
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

    public String getCssClass() {
        return (this.cssClass);
    }

    public void setCssClass(String aCssClass) {
        this.cssClass = aCssClass;
    }

    public void setHeaderCssClass(String aHeaderCssClass) {
        this.headerCssClass = aHeaderCssClass;
    }

    public String getHeaderCssClass() {
        return headerCssClass;
    }
    
    

}
