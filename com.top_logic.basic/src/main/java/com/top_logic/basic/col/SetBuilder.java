/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;

/**
 * Utility for creating sets with multiple entries in a single expression.
 *
 * Mostly copied from {@link MapBuilder}
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SetBuilder<T> {

    /**
     * Whether a reference to this builder's {@link #buffer} has already
     * {@link #toSet() handed out}.
     */
    private boolean shared = false;

    /**
     * The buffer, where set entries are accumulated.
     */
    private Set<T> buffer;


    /**
     * Creates a new SetBuilder with a new set.
     */
    public SetBuilder() {
        buffer = new HashSet<>();
    }

    /**
     * Creates a new SetBuilder with a new set of the given size.
     *
     * @param size the size of this set builder to avoid resizing operations.
     */
    public SetBuilder(int size) {
        buffer = CollectionUtil.newSet(size);
    }

    /**
     * Creates a new SetBuilder to append the given set.
     *
     * @param aSet the set to append
     */
    public SetBuilder(Set<T> aSet) {
        buffer = aSet;
    }

    /**
     * Adds a new entry to this set builder.
     *
     * @return This {@link SetBuilder} object for call chaining.
     */
    public SetBuilder<T> add(T value) {
        if (shared) {
            // Copy on write.
            buffer = new HashSet<>(buffer);
            shared = false;
        }
        buffer.add(value);
        return this;
    }

    /**
     * Adds new entries to this set builder.
     *
     * @return This {@link SetBuilder} object for call chaining.
     */
    public SetBuilder<T> addAll(Collection<? extends T> values) {
        if (shared) {
            // Copy on write.
            buffer = new HashSet<>(buffer);
            shared = false;
        }
        buffer.addAll(values);
        return this;
    }

    /**
     * Creates a non-shared set instance that contains all values added to this set builder.
     *
     * <p>
     * Later calls to {@link #add(Object)} will not modify the returned set.
     * </p>
     */
    public Set<T> toSet() {
        Set<T> result;
        if (shared) {
            // Prevent returning two reference to the same set object.
            result = new HashSet<>(buffer);
        } else {
            result = buffer;
        }
        shared = true;
        return result;
    }
    
    /**
     * Creates a non-shared unmodifiable set instance that contains all values added to this set builder.
     *
     */
    public Set<T> toUnmodifiableSet() {
    	return Collections.unmodifiableSet(new HashSet<>(buffer));
    }

    /**
     * Gets a shared set that contains all entries added to this list builder.
     *
     * <p>
     * Later calls to {@link #add(Object)} will modify the returned set also.
     * </p>
     * 
     * @deprecated will be removed in TL 5.8, use toSet() instead
     */
    @Deprecated
    public Set<T> toSharedSet() {
        return buffer;
    }

}
