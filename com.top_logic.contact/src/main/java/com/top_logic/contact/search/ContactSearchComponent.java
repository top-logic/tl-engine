/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.search;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.mandatoraware.COSCompanyContact;
import com.top_logic.contact.mandatoraware.COSContactConstants;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.layout.meta.search.AttributedSearchComponent;
import com.top_logic.element.layout.meta.search.SearchFilterSupport;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.element.meta.query.kbbased.KBBasedWrapperValuedAttributeFilter;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.error.TopLogicException;

/**
 * @author    <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class ContactSearchComponent extends AttributedSearchComponent {

    /** 
     * Creates a {@link ContactSearchComponent}.
     */
    public ContactSearchComponent(InstantiationContext context, Config aSomeAttrs) throws ConfigurationException {
        super(context, aSomeAttrs);
    }

    /**
     * Get the current Mandator
     *
     * @return the current Mandator
     */
    public Mandator getMandator() {
        if (this.showMandatorInput()) {
            return Mandator.getRootMandator();
            //return COSPersonContact.getActualMandator();
        }

        LayoutComponent theMaster = this.getMaster();
        return (Mandator) ((theMaster instanceof Selectable) ? ((Selectable) theMaster).getSelected() : theMaster.getModel());
    }

    public TLStructuredTypePart getMandatorAttribute() {
        try {
			return MetaElementUtil.getMetaAttribute(this.getSearchMetaElement(), COSCompanyContact.ATTRIBUTE_MANDATOR);
        } catch (NoSuchAttributeException nsax) {
            Logger.warn("unable to get mandator attribute", nsax, this.getClass());
        }
        return null;
    }

    
    @Override
    protected void addMoreConstraints(AttributeFormContext aContext, StoredQuery aQuery, List aSomeValues) {
        super.addMoreConstraints(aContext, aQuery, aSomeValues);
        
		ContactSearchFieldSupport.updateMandatorField(aContext, getSearchMetaElement(), COSContactConstants.ATTRIBUTE_MANDATOR, this.getMandator(), aQuery);
    }
    
    @Override
    public List getFilters(FormContext context, Map someArguments) {
        List result = super.getFilters(context, someArguments);

        /** Add a StructuredElementAttributeFilter filter for the mandator */
        try {
            TLStructuredTypePart theMA = this.getMandatorAttribute();
            
            Iterator theFilters = result.iterator();
            while (theFilters.hasNext()) {
                Object theFilter = theFilters.next();
                if (theFilter instanceof KBBasedWrapperValuedAttributeFilter) {
                    KBBasedWrapperValuedAttributeFilter theKBFilter = (KBBasedWrapperValuedAttributeFilter) theFilter;
                    if (theMA.equals(theKBFilter.getFilteredAttribute())) {
                        theFilters.remove();
                    }
                }
            }
            boolean useRelAndNeg = this.getRelevantAndNegate();
            boolean isRelevant = true;
            boolean doNegate = false;
            SelectField theField = (SelectField)((AttributeFormContext)context).getField(COSCompanyContact.ATTRIBUTE_MANDATOR, getSearchMetaElement());
            Mandator theMandator = (Mandator) (theField).getSingleSelection();
            if(useRelAndNeg) {
                String theRelevantAndNegateMemberName = SearchFilterSupport.getRelevantAndNegateMemberName(theMA, null);
                if(context.hasMember(theRelevantAndNegateMemberName)){
                    Boolean theFlag = (Boolean) (context.getField(theRelevantAndNegateMemberName)).getValue();
                    isRelevant = (theFlag != null);
                    doNegate   = (theFlag != null) && !theFlag.booleanValue();
                }
            }
            if (theMandator != null) {
                result.add(this.getStructureFilter(theMA, theMandator, isRelevant, doNegate));
            }
        }
        catch (NoSuchAttributeException ex) {
            throw new TopLogicException(this.getClass(), "Couldn't get Mandator attribute from Project ME", ex);
        }

        return result;
    }
    
    /**
     * Return, if this component allows the mandator to be displayed in the search UI.
     *
     * @return    <code>true</code>, if mandator can be displayed / selected in UI.
     */
    public boolean showMandatorInput() {
		return !hasMaster();
    }
    
    @Override
	public SearchFilterSupport getSearchFilterSupport() {
        return new ContactSearchFilterSupport();
    }
}
