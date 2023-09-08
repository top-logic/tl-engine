/*
 *  Copyright 2001-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package test.com.top_logic.basic.col;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Abstract test class for {@link Set} methods and contracts.
 * <p>
 * Since {@link Set} doesn't stipulate much new behavior that isn't already
 * found in {@link Collection}, this class basically just adds tests for
 * {@link Set#equals} and {@link Set#hashCode()} along with an updated
 * {@link #verify()} that ensures elements do not appear more than once in the
 * set.
 * <p>
 * To use, subclass and override the {@link #makeEmptySet()}
 * method.  You may have to override other protected methods if your
 * set is not modifiable, or if your set restricts what kinds of
 * elements may be added; see {@link AbstractTestCollection} for more details.
 *
 * @since Commons Collections 3.0
 * @version Revision: 1.5  Date: 2004/05/31 19:09:14 
 * 
 * @author Paul Jack
 */
public abstract class AbstractTestSet extends AbstractTestCollection {

    /**
     * JUnit constructor.
     *
     * @param name  name for test
     */
    public AbstractTestSet(String name) {
        super(name);
    }

    //-----------------------------------------------------------------------
    /**
     * Provides additional verifications for sets.
     */
    @Override
	public void verify() {
        super.verify();
        
        assertEquals("Sets should be equal", confirmed, collection);
        assertEquals("Sets should have equal hashCodes", 
                     confirmed.hashCode(), collection.hashCode());
        Collection<Object> set = makeConfirmedCollection();
        Iterator<Object> iterator = collection.iterator();
        while (iterator.hasNext()) {
            assertTrue("Set.iterator should only return unique elements", 
                       set.add(iterator.next()));
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Set equals method is defined.
     */
    @Override
	public boolean isEqualsCheckable() {
        return true;
    }

    /**
     * Returns an empty Set for use in modification testing.
     *
     * @return a confirmed empty collection
     */
    @Override
	public Collection<Object> makeConfirmedCollection() {
        return new HashSet<>();
    }

    /**
     * Returns a full Set for use in modification testing.
     *
     * @return a confirmed full collection
     */
    @Override
	public Collection<Object> makeConfirmedFullCollection() {
        Collection<Object> set = makeConfirmedCollection();
        set.addAll(Arrays.asList(getFullElements()));
        return set;
    }

    /**
     * Makes an empty set.  The returned set should have no elements.
     *
     * @return an empty set
     */
    public abstract Set<Object> makeEmptySet();

    /**
     * Makes a full set by first creating an empty set and then adding
     * all the elements returned by {@link #getFullElements()}.
     *
     * Override if your set does not support the add operation.
     *
     * @return a full set
     */
    public Set<Object> makeFullSet() {
        Set<Object> set = makeEmptySet();
        set.addAll(Arrays.asList(getFullElements()));
        return set;
    }

    /**
     * Makes an empty collection by invoking {@link #makeEmptySet()}.  
     *
     * @return an empty collection
     */
    @Override
	public final Collection<Object> makeCollection() {
        return makeEmptySet();
    }

    /**
     * Makes a full collection by invoking {@link #makeFullSet()}.
     *
     * @return a full collection
     */
    @Override
	public final Collection<Object> makeFullCollection() {
        return makeFullSet();
    }

    //-----------------------------------------------------------------------
    /**
     * Return the {@link AbstractTestCollection#collection} fixture, but cast as a Set.  
     */
    public Set<Object> getSet() {
        return(Set<Object>)collection;
    }

    /**
     * Return the {@link AbstractTestCollection#confirmed} fixture, but cast as a Set.
     */
    public Set<Object> getConfirmedSet() {
        return (Set<Object>)confirmed;
    }

    //-----------------------------------------------------------------------
    /**
     * Tests {@link Set#equals(Object)}.
     */
    public void testSetEquals() {
        resetEmpty();
        assertEquals("Empty sets should be equal", 
                     getSet(), getConfirmedSet());
        verify();

        Collection<Object> set2 = makeConfirmedCollection();
        set2.add("foo");
        assertTrue("Empty set shouldn't equal nonempty set", 
                   !getSet().equals(set2));

        resetFull();
        assertEquals("Full sets should be equal", getSet(), getConfirmedSet());
        verify();

        set2.clear();
        set2.addAll(Arrays.asList(getOtherElements()));
        assertTrue("Sets with different contents shouldn't be equal", 
                   !getSet().equals(set2));
    }

    /**
     * Tests {@link Set#hashCode()}.
     */
    public void testSetHashCode() {
        resetEmpty();
        assertEquals("Empty sets have equal hashCodes", 
                     getSet().hashCode(), getConfirmedSet().hashCode());

        resetFull();
        assertEquals("Equal sets have equal hashCodes", 
                     getSet().hashCode(), getConfirmedSet().hashCode());
    }

}
