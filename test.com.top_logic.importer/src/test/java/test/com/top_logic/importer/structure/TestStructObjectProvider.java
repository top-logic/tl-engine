/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.importer.structure;

import java.util.Collection;

import test.com.top_logic.importer.data.struct.TestStructFactory;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.wrap.SubtreeLoader;
import com.top_logic.importer.base.ObjectProvider;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestStructObjectProvider implements ObjectProvider {

    @Override
    public Collection<? extends Wrapper> getObjects(Wrapper aModel) {
        SubtreeLoader theLoader = new SubtreeLoader();

        try {
			StructuredElement theRoot = TestStructFactory.getInstance().getRootSingleton();

            theLoader.loadTree(theRoot);

            return CollectionUtil.dynamicCastView(Wrapper.class, theLoader.getLoadedNodes());
        }
        finally {
            theLoader.close();
        }
    }

    @Override
    public boolean supportsModel(Wrapper aModel) {
        return true;
    }
}
