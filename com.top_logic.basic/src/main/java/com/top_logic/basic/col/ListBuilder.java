/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Utility for creating lists with multiple entries in a single expression.
 *
 * Mostly copied from {@link MapBuilder}
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListBuilder<T> {

    /**
     * Whether a reference to this builder's {@link #buffer} has already
     * {@link #toList() handed out}.
     */
    private boolean shared = false;

    /**
     * The buffer, where list entries are accumulated.
     */
    private List<T> buffer;


    /**
     * Creates a new ListBuilder with a new list.
     */
    public ListBuilder() {
        buffer = new ArrayList<>();
    }

    /**
     * Creates a new ListBuilder with a new list of the given size.
     *
     * @param size the size of this list builder to avoid resizing operations.
     */
    public ListBuilder(int size) {
        buffer = new ArrayList<>(size);
    }

    /**
     * Creates a new ListBuilder to append the given list.
     *
     * @param aList the list to append
     */
    public ListBuilder(List<T> aList) {
        buffer = aList;
    }



    /**
     * Adds a new entry to this list builder.
     *
     * @return This {@link ListBuilder} object for call chaining.
     */
    public ListBuilder<T> add(T value) {
        if (shared) {
            // Copy on write.
            buffer = new ArrayList<>(buffer);
            shared = false;
        }
        buffer.add(value);
        return this;
    }

    /**
     * Adds new entries to this list builder.
     *
     * @return This {@link ListBuilder} object for call chaining.
     */
    public ListBuilder<T> addAll(Collection<? extends T> values) {
        if (shared) {
            // Copy on write.
            buffer = new ArrayList<>(buffer);
            shared = false;
        }
        buffer.addAll(values);
        return this;
    }

    /**
     * Creates a non-shared list instance that contains all values added to this list
     * builder.
     *
     * <p>
     * Later calls to {@link #add(Object)} will not modify the returned list.
     * </p>
     */
    public List<T> toList() {
        List<T> result;
        if (shared) {
            // Prevent returning two reference to the same list object.
            result = new ArrayList<>(buffer);
        } else {
            result = buffer;
        }
        shared = true;
        return result;
    }
    
    /**
     * Creates a non-shared unmodifiable list instance that contains all values added to this list
     * builder.
     *
     */
    public List<T> toUnmodifiableList() {
    	return Collections.unmodifiableList(new ArrayList<>(buffer));
    }

    /**
     * Gets a shared list that contains all entries added to this list builder.
     *
     * <p>
     * Later calls to {@link #add(Object)} will modify the returned list also.
     * </p>
     * 
     * @deprecated will be removed in TL 5.8, use toList() instead.
     */
    @Deprecated
    public List<T> toSharedList() {
        return buffer;
    }

}
