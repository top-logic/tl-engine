/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.merge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.tree.TreeNode;

import com.top_logic.basic.util.ResKey;

/**
 * Used by Importers or Converters to display a hierarchical part of the Process to perform.
 * 
 * In case there is only one object involved in the merge (e.g. some types
 * of coversion) source an dest can be the same.
 * 
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class MergeTreeNode extends AbstractMergeNode {

    /** The parent of this node. */
    protected MergeTreeNode  parent;

    /** The optional source, this node represents. */
    protected Object       source;

    /** The optional destination, this node represents. */
    protected Object       dest;

    /** The list of children (MergeTreeNodes) of this  node */
    protected List<MergeTreeNode> children;

    /** The list of {@link MergeMessage} WARN/INFO/DEBUG messages, appended to this node. */
    protected List<MergeMessage> messages;

    /** 
     * The list of {@link MergeMessage} FATAL/ERROR messages, appended to this node. 
     * 
     * Error messages will not be performed. {@link #perform()} will fail
     * when errors are found.
     */
    protected List<MergeMessage> errorList;

    /** Messages not shown but to be performed last (send Events, calculate sums) */
    protected List<MergeMessage> postList;

    /**
     * The flag indicating if the order in which the tree is processed should be
     * top-down or bottom up. <code>false</code> for top-down, <code>true</code>
     * for bottom-up. Default is top-down.
     */
    private boolean reverse;

    /**
     * Create a new MergeTreeNode. The new instance will process its own messages before processing children.
     * 
     * @param aParent   The parent of this tree node, must not be null.
     * @param aSource   The optional source of the merge to be changed.
     * @param aDest     The optional destination of the merge containing the new data.
     */
    public MergeTreeNode(MergeTreeNode aParent, Object aSource, Object aDest) {
        this(aParent, aSource, aDest, false);
    }

    /**
     * Creates an instance of this class with the specified parent node, source
     * and target objects. Additionally, clients are free to choose if the new instance
     * will process the child nodes before processing its own messages.
     * 
     * @param aParent
     *            the parent node
     * @param aSource
     *            the source object
     * @param aDest
     *            the target object to perform on
     * @param reverse
     *            <code>true</code> to process child nodes first,
     *            <code>false</code> to process this node first
     * @throws NullPointerException
     *             if the parent node is <code>null</code>
     */
    public MergeTreeNode(final MergeTreeNode aParent, final Object aSource, final Object aDest, final boolean reverse) throws NullPointerException {
		super(aParent, aSource);
        if (aParent == null) {
            throw new NullPointerException("Use RootMergeNode instead of null parent");
        }
        
        this.parent = aParent;
        this.source = aSource;
        this.dest   = aDest;
        this.reverse = reverse;
        aParent.addChild(this);
    }
    
    /**
     * Allow subclasses different kinds of creation. 
     */
    protected MergeTreeNode() {
		super(null, null);
        // subclasses can do whar they like.
    }

    /** Generate some Nice String for debugging */
    @Override
	public String toString() {
        return (this.getClass().getName() + " [" + "errors: " + this.hasErrors() + ", messages: " + (messages != null)
                + ", children: " + this.getChildCount() + ", source: " + this.source + ", dest: " + this.dest
                + ", parent: " + this.parent + ']');
    }

    /**
     * awlways true here as default.
     */
    public boolean getAllowsChildren() {
        return true;
    }

    /**
     * Perform the infoList theChilderrn, then the postList.
     * 
     * @throws IllegalStateException when there are errors.
     */
    public void perform() throws Exception {
        if (hasErrors()) {
            throw new IllegalStateException("You cannot perform with errors");
        }
        
        // bottom-up processing
        if(isReverse()) {
            performChildren();
            perform(messages);
        }
        
        // top-down processing
        else {
            perform(messages);
            performChildren();
        }
        
        // in any case, the post processing is done after all child nodes
        // have been successfully processed
        perform(postList);
    }

    /**
     * Check, if there are errors registered for this node. A node will have
     * errors, if the merging of the source object fails for a reason.
     * 
     * @return <code>true</code>, if there are errors.
     */
    public boolean hasErrors() {
        return errorList != null;
    }

    /**
     * Clear all Message for this and all is childs node. 
     */
    public void clearMessages(){
        List<MergeTreeNode> theChildren = this.children;

        if (theChildren != null) {
        	for (MergeTreeNode theNode : theChildren) {
                theNode.clearMessages();
            }
        }

        this.errorList = null;
        this.messages  = null;
        this.postList  = null;
    }
    
    /**
     * Set aPrologMessage as first message to be shown.
     */
    public void addFirst(MergeMessage aPrologMessage) {
        int                theLevel = aPrologMessage.getLevel();
        List<MergeMessage> theList  = this.getOrCreateListFor(theLevel);

        theList.add(0, aPrologMessage);

        if (this.toBePropagated(theLevel)) {
            parent.addFirst(aPrologMessage);
        }
    }

    /**
     * Append the given message to this node.
     * 
     * @param aMessage The message to be appended.
     */
    public void addMessage(MergeMessage aMessage) {
        int                theLevel = aMessage.getLevel();
        List<MergeMessage> theList  = this.getOrCreateListFor(theLevel);

        theList.add(aMessage);

        if (this.toBePropagated(theLevel)) {
            parent.addMessage(aMessage);
        }
    }
    
    /**
     * Add a new MergeMessage to this Node.
     * 
     * @return the created message.
     */
	public MergeMessage addMessage(int aLevel, ResKey aText) {
        MergeMessage result = new MergeMessage (aLevel, aText);
        this.addMessage(result);
        return result;
    }

    
    /**
     * Register aMergeMessage to be performed last. 
     * 
     * The type of message is actually ignored.
     * Messages will not be propagated.
     * 
     * @see #postList
     */
    public void addPostMessage(MergeMessage aMergeMessage) {
        if (postList == null) {
            postList = new ArrayList<>();
        }
        postList.add(aMergeMessage);

    }

    /**
     * Append the given node as child to this instance.
     * 
     * No sanity checks will be performed.
     * 
     * @param    aNode The node to be appended.
     */
    public void addChild(MergeTreeNode aNode) {
        if (this.children == null) { 
        	this.children = new ArrayList<>();
        }

        this.children.add(aNode);
    }

    /**
     * Return the list of children of this tree node.
     * 
     * If this node has no children, the returned list is empty.
     * 
     * @return    The list of children (which are {@link TreeNode TreeNodes}, 
     *            <code>null</code> when there are no children.
     */
	@Override
	public List<? extends AbstractMergeNode> getChildren() {
		List<AbstractMergeNode> theNodes = new ArrayList<>();

    	if (this.children != null) {
    		theNodes.addAll(this.children);
    	}

		if (this.errorList != null) { 
			theNodes.addAll(this.errorList);
		}
		if (this.messages != null) { 
			theNodes.addAll(this.messages);
		}

		return theNodes;
    }

	@Override
	public boolean isInitialized() {
		// children are not created lazy.
		return true;
	}

    /**
     * parent avoiding cast as via {@link #getParent()}
     */
    public MergeTreeNode getMergeParent() {
        return this.parent;
    }

    /**
     * Return the list of errors occured in merging for this tree node.
     * 
     * If this node has no errors, the returned list is empty.
     * 
     * @return    an unmodifiable List, <code>null</code> when there are no errors.
     */
    public List<MergeMessage> getErrorList() {
        if (this.errorList != null) {
            return Collections.unmodifiableList(this.errorList);
        }
        return null;
    }

    /**
     * Return the list of information occured in merging for this tree node.
     * 
     * If this node has no information, the returned list is empty.
     * 
     * @return    The list of information (which are {@link MergeMessage MergeMessages}, 
     *             <code>null</code> when there are no errors.
     */
    public List<MergeMessage> getMessageList() {
        if (this.messages != null) {
            return Collections.unmodifiableList(this.messages);
        }

        return null;
    }

    /**
     * Returns the source.
     */
    public Object getSource() {
        return source;
    }

    /**
     * Returns the destination.
     */
    public Object getDest() {
        return dest;
    }

    /**
     * Allow setting of the destination (may be created from source.
     * 
     * Destination can be set only when it previously was null.
     * 
     * @throws IllegalStateException when {@link #dest} is not null.
     */
    public void setDest(Object aDest) {
        if (dest != null) {
            throw new IllegalStateException("dest was already set to '" + dest + "' now trying '" + aDest + "'");
        }
        dest = aDest;
    }
    
    /** 
     * Recursivly set the Approval to state for all {@link MergeMessage}s.
     */
    public void resetAllApproved(boolean aState) {
    	if (this.messages != null) {
    		for (MergeMessage theMessage : this.messages) {
	            if (theMessage.isApproveable()) {
	                theMessage.setApproved(aState);
	            }
    		}
        } 

    	List<MergeTreeNode> theChildren = this.children;

        if (theChildren != null) {
        	for (MergeTreeNode theChild : theChildren) {
                theChild.resetAllApproved(aState);
            }
        }

    }

    /**
     * Call perform for all children.
     * 
     * @throws Exception If performing fails.
     */
    protected void performChildren() throws Exception {
    	List<MergeTreeNode> theChildren = children;

        if (theChildren != null) {
        	for (MergeTreeNode theChild : theChildren) {
                theChild.perform();
            }
        }
    }

    /** 
     * Internal perfom method that will traverse the lists. 
     */
    protected void perform(List<MergeMessage> aList) throws Exception {
        if (aList != null) {
        	for (MergeMessage theMessage : aList) {
                if (theMessage.isApproved() || !theMessage.isApproveable()) {
                    theMessage.perform(this);
                }
            }
        }
    }

    /**
     * Return and eventually create the List for a MergeMessage
     */
    protected List<MergeMessage> getOrCreateListFor(int aLevel) {
    	List<MergeMessage> theList;

        if ((aLevel == MergeMessage.ERROR) || (aLevel == MergeMessage.FATAL)) {
            if (this.errorList == null) {
            	this.errorList = new ArrayList<>();
            }

            theList = this.errorList;
        }
        else {
            if (this.messages == null) {
            	this.messages = new ArrayList<>();
            }

            theList = this.messages;
        }

        return theList;
    }
    
    /**
     * Check, if the message with the given level of information has to be propagated
     * to its parent.
     * 
     * This implementation will propagate {@link MergeMessage#FATAL} and 
     * {@link MergeMessage#ERROR} messages.
     *  
     * @param    aLevel    The level of the message.
     * @return   <code>true</code>, if it should be propagated to its parent.
     */
    protected boolean toBePropagated(int aLevel) {
        return (aLevel == MergeMessage.FATAL) 
            || (aLevel == MergeMessage.ERROR);
    }

    /**
     * Tells the receiver if child nodes have to be processed before processing
     * its own messages.
     * 
     * <p>
     * <b>NOTE:</b> the messages themselves are still processed in the order
     * they've been added or according to the receiver's messages comparator!
     * </p>
     * 
     * @param isReverse
     *            <code>true</code> for bottom-up, <code>false</code> for
     *            top-down
     */
    public void setReverse(final boolean isReverse) {
        this.reverse = isReverse;
    }

    /**
     * Checks if the receiver will process its child nodes first.
     * 
     * @return <code>true</code> if the receiver will process the child nodes
     *         first, <code>false</code> if its own messages are processed
     *         first.
     */
    public boolean isReverse() {
        return reverse;
    }

    /**
     * Tells the entire sub-tree, starting with the receiver, if node processing
     * should be done top-down or bottom-up.
     * 
     * @param reverse
     *            <code>true</code> if the receiver and all of its children
     *            (recursively) are to be processed bottom-up,
     *            <code>false</code> to process them top-down
     */
    public void setSubtreeReverse(final boolean reverse) {
        // update the receiver first
        setReverse(reverse);
        
        if (children != null) {
            
            // use the old-style iteration here to ensure 1.4 compatibility
            for (int i = 0, size = children.size(); i < size; i++) {
                children.get(i).setSubtreeReverse(reverse);
            }
        }
    }
}