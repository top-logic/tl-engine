/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.common.tree;

import java.util.LinkedList;

/**
 * Over the TreeInfoNode it is easy to construct a tree with {@link TreeInfo}s. The important method
 * is the {@link #getReverseArray()}. This method returns an array of {@link TreeInfo} which can be
 * used for constructing a {@link com.top_logic.reporting.common.tree.TreeAxis}.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TreeInfoNode {

    /** The TreeInfo contains the needed information for the renderer. */
    private TreeInfo     info;
    /** The parent of this node. */
    private TreeInfoNode parent;
    /** The children of this node. */
    private LinkedList   children;
    
    /** 
     * Creates a {@link TreeInfoNode} with the
     * given parameters.
     * 
     * @param aInfo A {@link TreeInfo}.
     */
    public TreeInfoNode(TreeInfo aInfo) {
        this.info     = aInfo;
        this.parent   = null;
        this.children = new LinkedList();
    }

    /** 
     * This method adds the given node as child node.
     * 
     * @param aNode  A {@link TreeInfoNode}.
     */
    public void addChild(TreeInfoNode aNode) {
        this.children.addLast(aNode);
        aNode.setParent(this);
    }
    
    /** 
     * This method returns the depth of this node.
     * 
     * @return Returns the depth of this node.
     */
    public int getDepth() {
        TreeInfoNode current = this;
        int  depth   = -1;
        while(current != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }
    
    /** 
     * This method returns a TreeInfo array which can be used for the {@link TreeAxis}.
     * 
     * @return Returns a TreeInfo array which can be used for the {@link TreeAxis}.
     */
    public TreeInfo[] getReverseArray() {
        LinkedList collection = getTree();
        TreeInfoNode[]     infos   = (TreeInfoNode[]) collection.toArray(new TreeInfoNode[collection.size()]);
        TreeInfo[] reverse = new TreeInfo[infos.length];
        for (int i = 0, j = infos.length - 1; i < infos.length; i++, j--) {
            reverse[i] = infos[j].info;
            reverse[i].setDepth(infos[j].getDepth());
            if (infos[j].getParent() != null) {
                reverse[i].setParent(infos.length - 1 - collection.indexOf(infos[j].getParent()));
            } else {
                reverse[i].setParent(infos.length - 1);
            }
        }
        return reverse;
    }
    
    /** 
     * This method returns the tree with this node as root node in a linked list.
     * 
     * @return Returns the tree with this node as root node in a linked list.
     */
    private LinkedList getTree() {
        LinkedList list = new LinkedList();
        list.addFirst(this);
        return getSubNodes(list);
    }
    
    /** 
     * This method stores all sub nodes of this node in the given linked list.
     * 
     * @param  aList A {@link LinkedList}.
     * @return Returns a list of all sub nodes of this node.
     */ 
    private LinkedList getSubNodes(LinkedList aList) {
        for (int i = 0; i < children.size(); i++) {
            TreeInfoNode child = (TreeInfoNode)children.get(i);
            aList.addLast(child);
            child.getSubNodes(aList);
        }
        return aList;
    }
    
    /**
     * This method returns the info.
     * 
     * @return Returns the info.
     */
    public TreeInfo getInfo() {
        return this.info;
    }
    
    /**
     * This method sets the info.
     *
     * @param aInfo The info to set.
     */
    public void setInfo(TreeInfo aInfo) {
        this.info = aInfo;
    }
    
    /**
     * This method returns the parent.
     * 
     * @return Returns the parent.
     */
    public TreeInfoNode getParent() {
        return this.parent;
    }

    /**
     * This method sets the parent.
     *
     * @param aParent The parent to set.
     */
    public void setParent(TreeInfoNode aParent) {
        this.parent = aParent;
    }
    
}

