/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.LinkedList;

/**
 * Implementation of a "First In First Out" queue.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class FIFO {

    /** The list of objects to be managed. */
    private LinkedList innerList;

    /**
     * Create an empty queue. 
     */
    public FIFO() {
        this.innerList = new LinkedList();
    }

    /**
     * Return a debugging string representation of this instance.
     * 
     * @return    The string representation of this instance.
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " [" 
                    + "size: " + this.innerList.size() 
                    +"]");
    }

    /**
     * Append the given object to the queue.
     * 
     * @param    anObject    The object to be appended to this queue.
     */
    public synchronized void add(Object anObject) {
        this.innerList.addLast(anObject);
    }

    /**
     * Return the next object of the queue.
     * 
     * If the queue is empty, this method will return <code>null</code>.
     * 
     * @return    The next object from the queue or <code>null</code>.
     */
    public synchronized Object next() {
        return (this.isEmpty() ? null : this.innerList.removeFirst());
    }

    /**
     * Check, if the queue is empty.
     * 
     * @return    <code>true</code>, if queue is empty. 
     */
    public synchronized boolean isEmpty() {
        return this.innerList.isEmpty();
    }
}