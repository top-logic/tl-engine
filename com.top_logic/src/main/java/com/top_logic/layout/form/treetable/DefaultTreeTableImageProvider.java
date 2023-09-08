/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.tree.NodeContext;
import com.top_logic.layout.tree.renderer.TreeImageProvider;

/**
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public class DefaultTreeTableImageProvider  implements TreeImageProvider{

    public static final DefaultTreeTableImageProvider INSTANCE = new DefaultTreeTableImageProvider();
    
    /**
	 * Creates a {@link DefaultTreeTableImageProvider}.
	 */
    protected DefaultTreeTableImageProvider() {
    }
    protected static final int CLOSED = 0;
    protected static final int OPEN = 1;
    protected static final int LEAF = 2;


    private static final ThemeImage[] PREFIX_IMAGES;
    private static final ThemeImage[][] NODE_IMAGES;
    static {
        int SINGLE = NodeContext.SINGLE_NODE;
        int MIDDLE = NodeContext.MIDDLE_NODE;
        int FIRST = NodeContext.FIRST_NODE;
        int LAST = NodeContext.LAST_NODE;
        
        PREFIX_IMAGES = new ThemeImage[SINGLE + 1];
        
		PREFIX_IMAGES[FIRST] = com.top_logic.layout.tree.renderer.Icons.L;
		PREFIX_IMAGES[MIDDLE] = com.top_logic.layout.tree.renderer.Icons.L;
		PREFIX_IMAGES[LAST] = com.top_logic.layout.tree.renderer.Icons.S;
		PREFIX_IMAGES[SINGLE] = com.top_logic.layout.tree.renderer.Icons.S;
        
        NODE_IMAGES   = new ThemeImage[LEAF + 1][SINGLE + 1];

		NODE_IMAGES[LEAF][FIRST] = com.top_logic.knowledge.gui.layout.tree.Icons.LF;
		NODE_IMAGES[LEAF][MIDDLE] = com.top_logic.knowledge.gui.layout.tree.Icons.LNL;
		NODE_IMAGES[LEAF][LAST] = com.top_logic.knowledge.gui.layout.tree.Icons.LL;
		NODE_IMAGES[LEAF][SINGLE] = com.top_logic.knowledge.gui.layout.tree.Icons.LS;
        
		NODE_IMAGES[CLOSED][FIRST] = com.top_logic.knowledge.gui.layout.tree.Icons.PF;
		NODE_IMAGES[CLOSED][MIDDLE] = com.top_logic.knowledge.gui.layout.tree.Icons.PNL;
		NODE_IMAGES[CLOSED][LAST] = com.top_logic.knowledge.gui.layout.tree.Icons.PL;
		NODE_IMAGES[CLOSED][SINGLE] = com.top_logic.knowledge.gui.layout.tree.Icons.PS;
        
		NODE_IMAGES[OPEN][FIRST] = com.top_logic.knowledge.gui.layout.tree.Icons.MF;
		NODE_IMAGES[OPEN][MIDDLE] = com.top_logic.knowledge.gui.layout.tree.Icons.MNL;
		NODE_IMAGES[OPEN][LAST] = com.top_logic.knowledge.gui.layout.tree.Icons.ML;
		NODE_IMAGES[OPEN][SINGLE] = com.top_logic.knowledge.gui.layout.tree.Icons.MS;
        
        // Images for first nodes and childs are by default identical 
        // to images for middle nodes and childs.
        NODE_IMAGES[LEAF][FIRST]   = NODE_IMAGES[LEAF][MIDDLE]; 
        NODE_IMAGES[CLOSED][FIRST] = NODE_IMAGES[CLOSED][MIDDLE]; 
        NODE_IMAGES[OPEN][FIRST]   = NODE_IMAGES[OPEN][MIDDLE]; 
        
        // Images for single leaf and non-leaf nodes are not supported by 
        // <i>TopLogic</i> renderers.
        NODE_IMAGES[LEAF][SINGLE]   = NODE_IMAGES[LEAF][LAST]; 
        NODE_IMAGES[CLOSED][SINGLE] = NODE_IMAGES[CLOSED][LAST]; 
        NODE_IMAGES[OPEN][SINGLE]   = NODE_IMAGES[OPEN][LAST];
    }
    

    
    @Override
	public final ThemeImage getPrefixImage(int position) {
        return PREFIX_IMAGES[position];
    }

    @Override
	public final ThemeImage getNodeImage(boolean isLeaf, boolean isExpanded, int position) {
        return (NODE_IMAGES[isLeaf ? LEAF : (isExpanded ? OPEN : CLOSED)][position]);
    }
}

