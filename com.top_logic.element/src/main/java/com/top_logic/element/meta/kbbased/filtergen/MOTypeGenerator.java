/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.AbstractWrapper;

/**
 * Generate a List of all Wrappers for a given KO-Type.
 * 
 * @author    <a href="mailto:kbu@top-logic.com></a>
 */
public class MOTypeGenerator extends ListGeneratorAdaptor {
    
    /** Teh KOType to use for generation */
    private String moType;

    /**
     * CTor with MO type
     * 
     * @param anMOType the MO type
     */
    public MOTypeGenerator(String anMOType) {
        this.moType = anMOType;
    }

    /**
     * Generate a List of all Wrappers for moType.
     */
    @Override
	public List<?> generateList(EditContext editContext) {
        try {
            KnowledgeBase  theKB   = AbstractWrapper.getDefaultKnowledgeBase();
            Collection     theKOs  = theKB.getAllKnowledgeObjects(this.moType);
            return AbstractWrapper.getWrappersFromCollection(theKOs);
        }
        catch (Exception e) {
			Logger.error("Problem getting all wrapper instances", e, MOTypeGenerator.class);
        }

        return Collections.EMPTY_LIST;
    }
}