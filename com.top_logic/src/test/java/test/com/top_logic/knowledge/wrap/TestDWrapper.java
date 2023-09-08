/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;


import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * This class is an example used for testing. 
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestDWrapper extends TestBWrapper {

    /**
     * Construct an instance wrapped around the specified
     * {@link com.top_logic.knowledge.objects.KnowledgeObject}.
     *
     * This CTor is only for the WrapperFactory! <b>DO NEVER USE THIS
     * CONSTRUCTOR!</b> Use always the getInstance() method of the wrappers.
     *  
     * @param    ko    The KnowledgeObject, must never be <code>null</code>.
     * 
     * @throws   NullPointerException  If the KO is <code>null</code>.
     */
    public TestDWrapper(KnowledgeObject ko) {
        super(ko);
    }
}
