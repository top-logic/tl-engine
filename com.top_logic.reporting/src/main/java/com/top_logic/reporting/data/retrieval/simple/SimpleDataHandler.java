/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.retrieval.simple;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.reporting.data.base.description.Description;
import com.top_logic.reporting.data.retrieval.DataHandler;
import com.top_logic.reporting.data.retrieval.ValueHolder;

/** 
 * Basic implementation of the data handler.
 * 
 * This class implements the interface to provide an easy access to all
 * methods defined there. If the interface will be extended, this class
 * will provide the correct default behavior for the changes.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class SimpleDataHandler implements DataHandler {

    private String name;    

    private Description[] desc;

    private Map values;

    private int depth;

    private DataHandler[] children;

    // Constuctors

    /**
     * Constructor for SimpleDataHandler.
     * 
     * This constructor is calling the extended constructor with parameters
     * for the one level handler.
     * 
     * @param    aName           The name of this handler, must be not 
     *                           <code>null</code>.
     * @param    someDesc        The descriptions supported by this handler,
     *                           must contain at least one description.
     * @throws   IllegalArgumentException     If one of the given parameters
     *                                        is wrong.
     * @see #SimpleDataHandler(String,Description[],int,DataHandler[])
     */
    public SimpleDataHandler(String aName, Description[] someDesc) 
                                            throws IllegalArgumentException {
        this(aName, someDesc, 0, null);
    }

    /**
     * Constructor for SimpleDataHandler.
     * 
     * The parameters can have different values, which depend on the kind
     * of data handler.
     * <ul>
     *     <li>aDepth: Zero for one level handler, larger than zero for
     *                 hierarchical data structures.</li>
     *     <li>someChildern: <code>null</code> or empty for one level handler,
     *                 at least one element for hierarchical data structures.
     *                 If the array is empty, it'll be set to <code>null</code>,
     *                 to be consistent to the rest of the system.</li>
     * </ul>
     * 
     * @param    aName           The name of this handler, must be not 
     *                           <code>null</code>.
     * @param    someDesc        The descriptions supported by this handler,
     *                           must contain at least one description.
     * @param    aDepth          The maximum evaluation depth of this handler.
     * @param    someChildren    The direct children of this handler.
     * @throws   IllegalArgumentException     If one of the given parameters
     *                                        is wrong according to the 
     *                                        description above.
     */
    public SimpleDataHandler(String aName, 
                             Description[] someDesc, 
                             int aDepth,
                             DataHandler[] someChildren) 
                                            throws IllegalArgumentException {
        super();

        if (StringServices.isEmpty(aName)) {
            throw new IllegalArgumentException("Name is not defined!");
        }

        if ((someDesc == null) || (someDesc.length == 0)) {
            throw new IllegalArgumentException("Descriptions are not defined!");
        }

        if (aDepth < 0) {
            aDepth = 0;
        }

        if ((aDepth == 0) && (someChildren != null)) {
            if (someChildren.length > 0) {
                throw new IllegalArgumentException("Depth is 0, but there are children!");
            }
            else {
                someChildren = null;
            }
        }

        if ((aDepth > 0) && ((someChildren == null) || (someChildren.length == 0))) {
            throw new IllegalArgumentException("Depth is 1, but there are no children!");
        }

        this.name     = aName;
        this.desc     = someDesc;
        this.depth    = aDepth;
        this.children = someChildren;
    }

    /**
     * @see com.top_logic.reporting.data.retrieval.DataHandler#getDescriptions()
     */
    @Override
	public Description[] getDescriptions() {
        return (this.desc);
    }

    /**
     * @see com.top_logic.reporting.data.retrieval.DataHandler#getValueHolder(Description)
     */
    @Override
	public ValueHolder getValueHolder(Description aDescription) {
        return ((ValueHolder) this.getValueMap().get(aDescription));
    }

    /**
     * @see com.top_logic.reporting.data.retrieval.DataHandler#getMaximumDepth()
     */
    @Override
	public int getMaximumDepth() {
        return (this.depth);
    }

    /**
     * @see com.top_logic.reporting.data.retrieval.DataHandler#getChildren()
     */
    @Override
	public DataHandler[] getChildren() {
        return (this.children);
    }

    /**
     * @see com.top_logic.reporting.data.retrieval.DataHandler#getDisplayName()
     */
    @Override
	public String getDisplayName() {
        return (this.name);
    }

    /**
     * Add the given value for the given description to this handler.
     * 
     * @param    aDesc     The description responsible for the given value.
     * @param    aValue    The value for the handler.
     * @return   Previous value associated with specified key, or 
     *           <code>null</code> if there was no value for key. 
     *           A <code>null</code> return can also indicate that 
     *           the map previously associated <code>null</code> 
     *           with the specified key, if the implementation 
     *           supports <code>null</code> values.
     */
    public ValueHolder addValue(Description aDesc, ValueHolder aValue) {
        return ((ValueHolder) this.getValueMap().put(aDesc, aValue));
    }

    /**
     * Remove the value defined by the given description from this handler.
     * 
     * @param    aDesc     The description responsible for the given value.
     * @return   Previous value associated with specified key, or 
     *           <code>null</code> if there was no value for the given key.
     */
    public ValueHolder removeValue(Description aDesc) {
        return ((ValueHolder) this.getValueMap().remove(aDesc));
    }

    /**
     * Return the map of values held by this handler.
     * 
     * @return    The map holding the values for the descriptions.
     */
    protected Map getValueMap() {
        if (this.values == null) {
            this.values = new HashMap();
        }
        
        return (this.values);
    }
}
