/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Properties;

import com.top_logic.basic.StringServices;
import com.top_logic.element.genericimport.interfaces.GenericCreateHandler;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;
import com.top_logic.element.meta.kbbased.AttributedWrapper;
import com.top_logic.knowledge.objects.CreateException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WrapperFactory;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class WrapperCreateHandler extends WrapperUpdateHandler implements
        GenericCreateHandler {

    private String wrapperType;

    private KnowledgeBase kBase;

    /**
     * Creates a {@link WrapperCreateHandler}.
     */
    public WrapperCreateHandler(Properties aSomeProps) {
        this(aSomeProps, aSomeProps.getProperty("koType"));
    }

    protected WrapperCreateHandler(Properties someProperties, String aKOType) {
        super(someProperties);
        wrapperType = aKOType;
        if (StringServices.isEmpty(wrapperType)) {
            throw new IllegalArgumentException("Type must not be null");
        }
        
        kBase = PersistencyLayer.getKnowledgeBase();
    }
    
    @Override
	public Object createBusinessObject(GenericValueMap aDO, String aFKeyAttr) throws CreateException {
		{
			KnowledgeObject theObject = kBase.createKnowledgeObject(this.wrapperType);
			// IGNORE FindBugs(RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT): For better portability.
            AttributedWrapper theWrap   = (AttributedWrapper) WrapperFactory.getWrapper(theObject);

            this.updateBusinessObject(theWrap, aDO, aFKeyAttr);

            return theWrap;
        }
    }

}

