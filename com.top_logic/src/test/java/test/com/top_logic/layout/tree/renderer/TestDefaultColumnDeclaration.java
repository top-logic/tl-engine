/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.renderer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.knowledge.gui.layout.list.BeaconRenderer;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.tree.renderer.ColumnDeclaration;
import com.top_logic.layout.tree.renderer.DefaultColumnDeclaration;

/**
 * Test the DefaultColumnDeclaration.
 * 
 * @author    <a href="mailto:klaus.halfman@top-logic.com">Klaus Halfmann</a>
 */
public class TestDefaultColumnDeclaration extends TestCase {

    /** 
     * Creates a TestDefaultColumnDeclaration for given test name.
     */
    public TestDefaultColumnDeclaration(String name) {
        super(name);
    }

    /**
	 * Test definition of {@link ColumnDeclaration#DEFAULT_COLUMN}
	 */
    public void testDefaultColumn() {
        DefaultColumnDeclaration def = new DefaultColumnDeclaration(ColumnDeclaration.DEFAULT_COLUMN);
        assertEquals(ColumnDeclaration.DEFAULT_COLUMN, def.getColumnType());
        assertEquals(ColumnDeclaration.DEFAULT_HEADER, def.getHeaderType());

        assertNull(def.getControlProvider());
        assertNull(def.getHeaderKey());
        assertNull(def.getHeaderRenderer());
        assertNull(def.getRenderer());
    }

    /**
     * Test {@link DefaultColumnDeclaration#DefaultColumnDeclaration(ControlProvider)}.
     */
    public void testControlProvider() {
        ControlProvider cp = DefaultFormFieldControlProvider.INSTANCE;
        
        DefaultColumnDeclaration dcd = new DefaultColumnDeclaration(cp);

        assertEquals(ColumnDeclaration.CONTROL_COLUMN, dcd.getColumnType());
        assertEquals(ColumnDeclaration.DEFAULT_HEADER, dcd.getHeaderType());
        
        assertSame(cp, dcd.getControlProvider());

        assertNull(dcd.getHeaderKey());
        assertNull(dcd.getHeaderRenderer());
        assertNull(dcd.getRenderer());
    }

    /**
     * Test method for {@link DefaultColumnDeclaration#DefaultColumnDeclaration(Renderer)}.
     */
    public void testRenderer() {
		Renderer<FastListElement> rend = BeaconRenderer.INSTANCE;
		Renderer<Object> genericRenderer = rend.generic();
        
		DefaultColumnDeclaration dcd = new DefaultColumnDeclaration(genericRenderer);

        assertEquals(ColumnDeclaration.RENDERED_COLUMN, dcd.getColumnType());
        assertEquals(ColumnDeclaration.DEFAULT_HEADER , dcd.getHeaderType());
        
		assertSame(genericRenderer, dcd.getRenderer());

        assertNull(dcd.getHeaderKey());
        assertNull(dcd.getHeaderRenderer());
        assertNull(dcd.getControlProvider());
    }
    
    /**
     * the suite of Tests to execute 
     */
    static public Test suite () {       
        return new TestSuite(TestDefaultColumnDeclaration.class);
    }

}

