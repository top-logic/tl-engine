/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.tree.NodeContext;

/**
 * Set of default images for tree rendering.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultTreeImageProvider implements TreeImageProvider {

    public static final TreeImageProvider INSTANCE = new DefaultTreeImageProvider();
    
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
    	
    	PREFIX_IMAGES[FIRST] = Icons.L;
		PREFIX_IMAGES[MIDDLE] = Icons.L;
		PREFIX_IMAGES[LAST] = Icons.S;
		PREFIX_IMAGES[SINGLE] = Icons.S;
		
    	NODE_IMAGES   = new ThemeImage[LEAF + 1][SINGLE + 1];

    	NODE_IMAGES[LEAF][FIRST] = Icons.TREE_LF;
		NODE_IMAGES[LEAF][MIDDLE] = Icons.TREE_LNL; 
		NODE_IMAGES[LEAF][LAST] = Icons.TREE_LL;
		NODE_IMAGES[LEAF][SINGLE] = Icons.TREE_LS; 
		
		NODE_IMAGES[CLOSED][FIRST] = Icons.TREE_PF;
		NODE_IMAGES[CLOSED][MIDDLE] = Icons.TREE_PNL; 
		NODE_IMAGES[CLOSED][LAST] = Icons.TREE_PL;
		NODE_IMAGES[CLOSED][SINGLE] = Icons.TREE_PS; 
		
		NODE_IMAGES[OPEN][FIRST] = Icons.TREE_MF;
		NODE_IMAGES[OPEN][MIDDLE] = Icons.TREE_MNL; 
		NODE_IMAGES[OPEN][LAST] = Icons.TREE_ML;
		NODE_IMAGES[OPEN][SINGLE] = Icons.TREE_MS;
		
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
    

    protected DefaultTreeImageProvider() {
    	// Singleton constructor.
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
