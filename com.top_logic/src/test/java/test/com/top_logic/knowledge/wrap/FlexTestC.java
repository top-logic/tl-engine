/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import test.com.top_logic.knowledge.service.KBTestMeta;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;

/**
 * Use {@link KBTestMeta#TEST_C} as Flexwrapper
 */
public class FlexTestC extends AbstractBoundWrapper {

    /**
     * Create a new instance of this wrapper.
     *
     * @param    ko         The knowledge object to be wrapped.
     */
    public FlexTestC(KnowledgeObject ko) {
        super(ko);
    }


}