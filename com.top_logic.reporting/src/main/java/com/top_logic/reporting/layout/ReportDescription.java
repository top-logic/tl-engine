/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout;

import com.top_logic.basic.col.Filter;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.wrap.currency.Currency;
import com.top_logic.model.TLStructuredTypePart;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ReportDescription {

    private Filter filter;

    private StructuredElement element;

    private TLStructuredTypePart metaAttribute;

    private String structureName;

    private final Currency currency;

    /** 
     * Create a new instance of this class.
     */
    public ReportDescription(StructuredElement aStart, TLStructuredTypePart aMA, Filter aFilter, String aStructure, Currency aCurrency) {
        this.element       = aStart;
        this.metaAttribute = aMA;
        this.filter        = aFilter;
        this.structureName = aStructure;
        this.currency      = (aCurrency == null) ? Currency.getSystemCurrency() : aCurrency;
    }

    public StructuredElement getElement() {
        return (this.element);
    }

    public TLStructuredTypePart getMetaAttribute() {
        return (this.metaAttribute);
    }

    public Filter getFilter() {
        return (this.filter);
    }

    public String getStructureName() {
        return (this.structureName);
    }

    /** 
     * Return the currency the report has to store values for.
     * 
     * @return    The currency used in the report.
     */
    public Currency getCurrency() {
        return this.currency;
    }
}