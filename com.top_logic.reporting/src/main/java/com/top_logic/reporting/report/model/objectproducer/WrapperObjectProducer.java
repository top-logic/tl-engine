/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.objectproducer;

import java.util.Collection;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.reporting.report.exception.ReportingException;

/**
 * The WrapperObjectProducer produces object sets of {@link Wrapper}s
 *
 * @author     <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@Deprecated
public class WrapperObjectProducer extends AbstractObjectProducer {


    /** xml tags / options */
    /**
     * These options can be set to restrict the set of wrappers
     * @see KnowledgeBase#getObjectsByAttribute(String, String, Object)
     */
    public static final String PROPERTY_ATTRUBUTE_NAME  = "attribute";
    public static final String PROPERTY_ATTRUBUTE_VALUE = "value";

    /** local holders for the option values */
    private String attribute;
    private Object attributeValue;

    /**
     * Default C'Tor
     */
    public WrapperObjectProducer(InstantiationContext aContext, WrapperObjectProducerConfiguration aConfig) {
        super(aContext, aConfig);
        this.attribute = aConfig.getAttribute();
        this.attributeValue = aConfig.getValue();
    }

    /**
     * Collection, never <code>null</code>
     *
     * @see com.top_logic.reporting.report.model.objectproducer.AbstractObjectProducer#_getObjectsInternal()
     */
    @Override
	protected Collection _getObjectsInternal() {
        KnowledgeBase theKB = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();

        return getObjects(theKB);
    }

    private Collection getObjects(KnowledgeBase aKnowledgeBase) {

        if (!StringServices.isEmpty(this.attribute) && this.attributeValue != null) {
            return getObjectsByAttribute(aKnowledgeBase);
        }
        else {
            return getObjectsByType(aKnowledgeBase);
        }
    }

    /**
     * This method returns Wrappers of the type {@link #getObjectType()} that
     * have an attribute with a specified value. The attribute and the value to match can be configured
     * as option in the report xml.
     *
     * @param aKnowledgeBase to obtain the dataobjects from
     * @return a collection of wrappers, never <code>null</code>
     */
    private Collection getObjectsByAttribute(KnowledgeBase aKnowledgeBase) {
        try {
            return WrapperFactory.getWrappersByAttribute(this.getObjectType(), aKnowledgeBase, this.attribute, this.attributeValue);
        } catch (UnknownTypeException utex) {
            throw new ReportingException(WrapperObjectProducer.class, "Unknown object type '"+this.getObjectType()+"'", utex);
        }
    }

    /**
     * This method returns all Wrappers of the type {@link #getObjectType()} from the
     * given {@link KnowledgeBase}
     *
     * @param aKnowledgeBase to obtain the dataobjects from
     * @return a collection of wrappers, never <code>null</code>
     */
    private Collection getObjectsByType(KnowledgeBase aKnowledgeBase) {
        return WrapperFactory.getWrappersByType(this.getObjectType(), aKnowledgeBase);
    }

    public interface WrapperObjectProducerConfiguration extends ObjectProducerConfiguration {
        
    	@Override
		@ClassDefault(WrapperObjectProducer.class)
		Class<? extends ObjectProducer> getImplementationClass();
        
        void setValue(Object aValue);
		@InstanceFormat
		@Subtypes({})
        Object getValue();
        
        void setAttribute(String aValue);
        String getAttribute();
    }
}
