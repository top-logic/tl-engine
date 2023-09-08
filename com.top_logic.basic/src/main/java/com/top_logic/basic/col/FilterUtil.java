/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;


/**
 * The FilterUtil useful static methods for {@link com.top_logic.basic.col.Filter}s.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class FilterUtil {

	/**
	 * Applies the given filter to the elements of the given source list that
	 * are in the range from the given start index (inclusive) to the given stop
	 * index (exclusive). All {@link Filter#accept(Object) accepted} elements
	 * are inserted into the given destination list beginning at the given
	 * insertion index.
	 * 
	 * @param filter
	 *        The filter to apply.
	 * @param source
	 *        The source list from which the filtered items are taken.
	 * @param startIndex
	 *        The index of the first filtered item in the source list.
	 * @param stopIndex
	 *        The index, where processing of items form the source list stops.
	 * @param destination
	 *        The destination list into which accepted items are inserted.
	 * @param insertIndex
	 *        The index, where the first item is inserted into the destination
	 *        list.
	 * @return The destination list as convenience for method chaining.
	 * 
	 * @since TL_5_6_1
	 */
    public static <T> List<T> filterSublistInto(Filter<? super T> filter, List<? extends T> source, int startIndex, int stopIndex, List<T> destination, int insertIndex) {
    	if (source instanceof RandomAccess) {
    		for (int index = startIndex; index < stopIndex; index++) {
    			T item = source.get(index);
    			if (filter.accept(item)) {
    				destination.add(insertIndex, item);
    				insertIndex++;
    			}
    		}
    	} else {
    		ListIterator<T> insertIt = destination.listIterator(insertIndex);
    		ListIterator<? extends T> sourceIt = source.listIterator(startIndex);
    		
    		for (int cnt = stopIndex - startIndex; cnt > 0; cnt--) {
    			insertIt.add(sourceIt.next());
    		}
    	}
		return destination;
    }

	/**
	 * Applies the given filter to the elements of the given source list that
	 * are in the range from the given start index (inclusive) to the given stop
	 * index (exclusive). All {@link Filter#accept(Object) accepted} elements
	 * are inserted into a new result list in the same order as they appeared in
	 * the source list.
	 * 
	 * @param filter
	 *        The filter to apply.
	 * @param source
	 *        The source list from which the filtered items are taken.
	 * @param startIndex
	 *        The index of the first filtered item in the source list.
	 * @param stopIndex
	 *        The index, where processing of items form the source list stops.
	 *        
	 * @since TL_5_6_1
	 */
    public static <T> List<T> filterSublist(Filter<? super T> filter, List<? extends T> source, int startIndex, int stopIndex) {
		return filterSublistInto(filter, source, startIndex, stopIndex, new ArrayList<>(), 0);
    }

	/**
	 * Filters the given values into a new list.
	 * 
	 * @param <T>
	 *        The type of the result list.
	 * @param filter
	 *        The filter to apply.
	 * @param values
	 *        The values to filter.
	 * @return A new list containing only those given values that match the
	 *         given filter.
	 * 
	 * @since TL_5_6_1
	 */
    public static <T> List<T> filterList(Filter<? super T> filter, Iterable<? extends T> values) {
		return filterInto(new ArrayList<>(), filter, values);
    }

	/**
	 * Filters the given values into a new list.
	 * 
	 * @param <T>
	 *        The type of the result list.
	 * @param filter
	 *        The instance-of filter to apply.
	 * @param values
	 *        The values to filter.
	 * @return A new list containing only those given values that match the given filter.
	 */
	public static <T> List<T> filterList(final Class<T> filter, final Iterable<?> values) {
		return filterInto(new ArrayList<>(), filter, values);
	}

	/**
	 * Filters the given values into a new list.
	 * 
	 * @param <T>
	 *        The type of the result list.
	 * @param filter
	 *        The instance-of filter to apply.
	 * @param values
	 *        The values to filter.
	 * @return A new list containing only those given values that match the given filter.
	 */
	public static <T> List<T> filterList(final Class<T> filter, final Collection<?> values) {
		return filterInto(new ArrayList<>(values.size()), filter, values);
	}

	/**
	 * Filters the given values into a new set.
	 * 
	 * @param <T>
	 *        The type of the result set.
	 * @param filter
	 *        The filter to apply.
	 * @param values
	 *        The values to filter.
	 * @return A new set containing only those given values that match the given
	 *         filter.
	 * 
	 * @since TL_5_6_1
	 */
    public static <T> Set<T> filterSet(Filter<? super T> filter, Iterable<? extends T> values) {
		return filterInto(new HashSet<>(), filter, values);
    }

	/**
	 * Filters the given values into a new set.
	 * 
	 * @param <T>
	 *        The type of the result set.
	 * @param filter
	 *        The instance-of filter to apply.
	 * @param values
	 *        The values to filter.
	 * @return A new set containing only those given values that match the given filter.
	 */
	public static <T> Set<T> filterSet(Class<T> filter, Collection<?> values) {
		return filterInto(CollectionUtil.<T> newSet(values.size()), filter, values);
	}

	/**
	 * Filters the given values into a new set.
	 * 
	 * @param <T>
	 *        The type of the result set.
	 * @param filter
	 *        The instance-of filter to apply.
	 * @param values
	 *        The values to filter.
	 * @return A new set containing only those given values that match the given filter.
	 */
    public static <T> Set<T> filterSet(Class<T> filter, Iterable<?> values) {
		return filterInto(new HashSet<>(), filter, values);
    }
    
	/**
	 * Filters the given values into a given collection by applying the given filter.
	 * 
	 * @param <S>
	 *        The type of the source values.
	 * @param <D>
	 *        The content type of the destination collection.
	 * @param <C>
	 *        The concrete type of the destination collection..
	 * 
	 * @param destination
	 *        The collection to add matching source elements to.
	 * @param filter
	 *        The filter to apply to the given set.
	 * @param source
	 *        The values to filter.
	 * 
	 * @return The given destination collection.
	 * 
	 * @since TL_5_6_1
	 * 
	 * @see FilterUtil#filterInto(Collection, Class, Iterable)
	 */
	public static <D, S extends D, C extends Collection<D>> C filterInto(final C destination, Filter<? super S> filter, Iterable<? extends S> source) {
        if (source instanceof RandomAccess) {
            List<S> theList = (List<S>) source;
            for (int i=0, n=theList.size(); i < n; i++) {
                S theCurrent = theList.get(i);
                if (filter.accept(theCurrent)) {
                    destination.add(theCurrent);
                }
            }
        }
        else if (source != null) {
            for (Iterator<? extends S> i = source.iterator(); i.hasNext(); ) {
                S theCurrent = i.next();
                if (filter.accept(theCurrent)) {
                    destination.add(theCurrent);
                }
            }
        }
        return destination;
	}

	/**
	 * Filters a collection in place.
	 * 
	 * @param <T>
	 *        The element type of the filtered collection.
	 * 
	 * @param values
	 *        The collection that is filtered in place.
	 * @param filter
	 *        The filter to apply to the given collection.
	 * 
	 * @return The modified collection as convenience.
	 * 
	 * @since TL_5_6_1
	 */
	public static <T, C extends Collection<? extends T>> C filterInline(Filter<? super T> filter, C values) {
		if (values instanceof RandomAccess) {
	        List<T> theList = (List<T>) values;
	        int to = 0;
	        int cnt = theList.size();
	        for (int from = 0; from < cnt; from++) {
	            T element = theList.get(from);
	            if (filter.accept(element)) {
	            	theList.set(to++, element);
	            }
	        }
	        while (cnt > to) {
	        	theList.remove(--cnt); 
	        }
	    }
	    else if (values != null) {
	        for (Iterator<? extends T> it = values.iterator(); it.hasNext(); ) {
	            T theCurrent = it.next();
	            if (!filter.accept(theCurrent)) {
	                it.remove();
	            }
	        }
	    }
	    return values;
	}

	/**
	 * Filters the given values by applying all given filters.
	 * 
	 * @param <T>
	 *        The type of the given values.
	 * @param filters
	 *        The filters to apply.
	 * @param values
	 *        The values to filter.
	 * @return All values that match all given filters, may be the same as the
	 *         given collection.
	 * 
	 * @since TL_5_6_1
	 */
	public static <T> Collection<T> filterAnd(Iterable<? extends Filter<? super T>> filters, Collection<T> values) {
		if (values == null) { 
			return null; 
		}
		
        if (filters == null) {
        	return values; 
        }

        Iterator<? extends Filter<? super T>> it = filters.iterator();
        if (! it.hasNext()) {
        	return values;
        }
        
        Filter<? super T> firstFilter = it.next();
        
		ArrayList<T> result = filterInto(new ArrayList<>(), firstFilter, values);
        while (it.hasNext()) {
            Filter<? super T> additionalFilter = it.next();
            filterInline(additionalFilter, result);
        }
        
        return result;
	}
    
	/**
	 * Find the first element returned from the given {@link Collection} that
	 * matches the given {@link Filter}.
	 * 
	 * @param filter
	 *        The filter to apply.
	 * @param source
	 *        the source of elements to test.
	 * @return the first element from the given source that matches the given
	 *         filter.
	 */
    public static <T> T findFirst(Filter<? super T> filter, Iterable<T> source) {
    	for (T item : source) {
			if (filter.accept(item)) {
				return item;
			}
		}
		return null;
    }

	/**
	 * Find the first element returned from the given {@link Iterator} that
	 * matches the given {@link Filter}.
	 * 
	 * @param filter
	 *        The filter to apply.
	 * @param iterator
	 *        the source of elements to test.
	 * @return the first element from the given source that matches the given
	 *         filter.
	 */
	public static <T> T findFirst(Filter<? super T> filter, Iterator<T> iterator) {
		while (iterator.hasNext()) {
			T item = iterator.next();
			if (filter.accept(item)) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Tests whether all elements of the given collection are instance of the
	 * given class.
	 * 
	 * @param dynamicType
	 *        The type to test the given values against.
	 * @param values
	 *        The values to test.
	 * 
	 * @since TL_5_6_1
	 */
    public static boolean containsOnly(Class<?> dynamicType, Iterable<?> values) {
    	for (Object object : values) {
            if (! dynamicType.isInstance(object)) {
                return false;
            }
		}
        
        return true;
    }
    
    /**
	 * Groups the given input {@link List} according to the given grouping.
	 * 
	 * <p>
	 * <b>Note:</b> The given input {@link List} must be sorted in ascending
	 * order according to the grouping.
	 * </p>
	 * 
	 * @param grouping
	 *        A {@link Comparator} that identifies objects in the given input
	 *        {@link List} that are considered equal. Equal objects are put into
	 *        the same group.
	 * @param input
	 *        The input list to group.
	 * @return an {@link Iterator} that returns sub {@link List}s of the given
	 *         input list, where all elements are equal according to the given
	 *         grouping comparator.
	 */
    public static <T> Iterator<List<T>> groupBySorted(final Comparator<? super T> grouping, final List<T> input) {
    	return new Iterator<>() {
    		int startIndex = 0;
    		int stopIndex = 0;
    		{
    			findNext();
    		}
    		
			private void findNext() {
				int size = input.size();
				if (startIndex >= size) {
					return;
				}
				T lastElement = input.get(startIndex);
				stopIndex = startIndex + 1;
				while (stopIndex < size) {
					T currentElement = input.get(stopIndex);
					int comparision = grouping.compare(lastElement, currentElement);
					if (comparision < 0) {
						// The current element is larger than the first element
						// in the group. The end of this group is found.
						break;
					} else if (comparision > 0) {
						// Input does not satisfy sorting precondition.
						throw new IllegalArgumentException("Input list is not sorted according to given grouping: " + lastElement + " > " + currentElement);
					}
					
					// Test next element.
					lastElement = currentElement;
					stopIndex++;
				}
			}

			@Override
			public boolean hasNext() {
				return startIndex < input.size();
			}

			@Override
			public List<T> next() {
				List<T> result = input.subList(startIndex, stopIndex);
				startIndex = stopIndex;
				findNext();
				return result;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
    		
    	};
    }
    
    /**
	 * Groups the given input sequence according to the given grouping.
	 * 
	 * <p>
	 * <b>Note:</b> The given input sequence must be sorted in ascending order
	 * according to the grouping.
	 * </p>
	 * 
	 * @param grouping
	 *        A {@link Comparator} that identifies objects in the given input
	 *        sequence that are considered equal. Equal objects are put into the
	 *        same group.
	 * @param input
	 *        The input iterator form which elements are grouped.
	 * @return an {@link Iterator} that returns {@link List}s of objects, where
	 *         all elements are equal according to the given grouping
	 *         comparator.
	 */
    public static <T> Iterator<List<T>> groupBySorted(final Comparator<? super T> grouping, final Iterator<? extends T> input) {
    	return new Iterator<>() {
    		
    		/**
    		 * The first element of the next result.
    		 */
    		T nextElement;
    		
    		/**
    		 * Whether #nextElement contains a valid reference.
    		 */
    		boolean hasNextElement = false;
    		
    		/**
    		 * The next result of #next()
    		 */
    		ArrayList<T> nextResult;
    		{
    			if (input.hasNext()) {
    				 nextElement = input.next();
    				 hasNextElement = true;
    				 nextResult = new ArrayList<>();
    				 findNext();
    			}
    		}
    		
    		private void findNext() {
    			while (input.hasNext()) {
    				T currentElement = input.next();
    				nextResult.add(nextElement);
    				
    				int comparision = grouping.compare(nextElement, currentElement);
    				
    				nextElement = currentElement;
    				if (comparision < 0) {
						// The current element is larger than an element
						// in the group. The end of this group is found.
    					return;
    				} else if (comparision > 0) {
    					// Input does not satisfy sorting precondition.
						throw new IllegalArgumentException("Input list is not sorted according to given grouping: " + nextElement + " > " + currentElement);
    				}
    			}
    			
    			// The next element belongs to this group.
    			nextResult.add(nextElement);
    			hasNextElement = false;
    		}
    		
    		@Override
			public boolean hasNext() {
    			return nextResult != null;
    		}
    		
    		@Override
			public List<T> next() {
    			ArrayList<T> theResult = nextResult;
    			
    			if (hasNextElement) {
    				nextResult = new ArrayList<>();
    				findNext();
    			} else {
    				// This is the very last element of this iterator. 
    				nextResult = null;
    			}
    			
				return theResult;
    		}
    		
    		@Override
			public void remove() {
    			throw new UnsupportedOperationException();
    		}
    		
    	};
    }
    
    /**
	 * Same as {@link #groupBySorted(Comparator, List)}, but establishes the required
	 * precondition explicitly.
	 * 
	 * @see #groupBySorted(Comparator, List) for the most efficient version, if the
	 *      input holds the required precondition.
	 */
    public static <T> Iterator<List<T>> groupByUnsorted(Comparator<? super T> grouping, Collection<? extends T> input) {
    	ArrayList<T> sortedInput = new ArrayList<>(input);
    	Collections.sort(sortedInput, grouping);
    	return groupBySorted(grouping, sortedInput);
    }
    
    /**
	 * Compute the number of items in the given collection that match the given
	 * filter.
	 * 
	 * @param <T>
	 *        The type of elements to count.
	 * @param filter
	 *        The filter that must match counted elements.
	 * @param values
	 *        The values to count.
	 * @return The number of elements in the given values that match the given
	 *         filter.
	 * 
	 * @since TL_5_6_1
	 */
	public static <T> int count(Filter<? super T> filter, Iterable<? extends T> values) {
		int result = 0;
		if (values != null) {
			for (T value : values) {
				if (filter.accept(value)) {
					result++;
				}
			}
		}
		return result;
	}
	
	/**
	 * Determines whether each element of the iterator is accepted by the given {@link Filter}
	 * 
	 * @param iterator
	 *            an iterator whose elements shall be tested. must not be <code>null</code>.
	 * @param filter
	 *            a filter which is the oracle to ask for accepting. must not be <code>null</code>.
	 */
	public static <T> boolean matchAll(Iterator<? extends T> iterator, Filter<? super T> filter) {
		while (iterator.hasNext()) {
			if (!filter.accept(iterator.next())) {
				return false;
			}
		}
		return true;
	}	

	/**
	 * Returns <code>true</code> iff each element in the given collection is accepted by the given
	 * filter.
	 * <p>
	 * This method actually calls {@link #matchAll(Iterator, Filter)} with the given filter and
	 * the iterator returned by the given collection.
	 * </p>
	 * 
	 * @param collection
	 *            must not be <code>null</code>.
	 * @param filter
	 *            must not be <code>null</code>
	 */
	public static <T> boolean matchAll(Iterable<? extends T> collection, Filter<? super T> filter) {
		return matchAll(collection.iterator(), filter);
	}
	
	/**
	 * Determines whether at least one element of the iterator is accepted by the given
	 * {@link Filter}.
	 * 
	 * @param iterator
	 *            an iterator whose elements shall be tested. must not be <code>null</code>.
	 * @param filter
	 *            a filter which is the oracle to ask for accepting. must not be <code>null</code>.
	 */
	public static <T> boolean hasMatch(Iterator<? extends T> iterator, Filter<? super T> filter) {
		while (iterator.hasNext()) {
			if (filter.accept(iterator.next())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> iff at least one element in the given collection is accepted by the
	 * given filter.
	 * <p>
	 * This method actually calls {@link #hasMatch(Iterator, Filter)} with the given filter
	 * and the iterator returned by the given collection.
	 * </p>
	 * 
	 * @param collection
	 *            must not be <code>null</code>.
	 * @param filter
	 *            must not be <code>null</code>
	 */
	public static <T> boolean hasMatch(Iterable<? extends T> collection, Filter<? super T> filter) {
		return hasMatch(collection.iterator(), filter);
	}

    /**
	 * Returns an {@link Iterator} which returns only that elements from the
	 * given {@link Iterator} which matches the given filter.
	 * 
	 * @param <T>
	 *        the type of the elements in the {@link Iterator}
	 * @param filter
	 *        the filter to apply
	 * @param iterator
	 *        the iterator whose elements must be filtered
	 *        
	 * @since TL_5_7_0
	 */
	public static <T> Iterator<T> filterIterator(Filter<? super T> filter, Iterator<? extends T> iterator) {
		return new FilterIterator<>(iterator, filter);
	}

	/**
	 * Creates an {@link Iterable} that returns only elements from the source {@link Iterable} that
	 * are of the given type.
	 * 
	 * @param type
	 *        The type filter.
	 * @param source
	 *        The source {@link Iterable}
	 * 
	 * @since 5.7.3
	 */
	public static <T> Iterable<T> filterIterable(Class<T> type, Iterable<?> source) {
		return new TypeFilteredIterable<>(type, source);
	}

	/**
	 * Creates an {@link Iterator} that returns only elements from the source {@link Iterator} that
	 * are of the given type.
	 * 
	 * @param type
	 *        The type filter.
	 * @param source
	 *        The source {@link Iterator}
	 * 
	 * @since 5.7.3
	 */
	public static <T> Iterator<T> filterIterator(Class<T> type, Iterator<?> source) {
		return new TypeMatchingIterator<>(type, source);
	}
	
	/**
	 * Filters all elements in the given source that are not <code>null</code> and
	 * assignment-compatible to the given class. These elements are added to the given destination.
	 * 
	 * @param destination
	 *        The {@link Collection} to add elements from the source.
	 * @param type
	 *        {@link Class} defining the type of the objects to add to the destination collection.
	 * @param source
	 *        The objects to filter.
	 * 
	 * @return The given destination {@link Collection}.
	 * 
	 * @since 5.7.3
	 * 
	 * @see FilterUtil#filterInto(Collection, Filter, Iterable)
	 */
	public static <T, C extends Collection<? super T>> C filterInto(C destination, Class<T> type, Iterable<?> source) {
        if (source instanceof RandomAccess) {
            List<?> theList = (List<?>) source;
            for (int i=0, n=theList.size(); i < n; i++) {
                Object current = theList.get(i);
				if (type.isInstance(current)) {
                	/* Is checked in condition */
                    @SuppressWarnings("unchecked")
					T typeSafe = (T) current;
					destination.add(typeSafe);
                }
            }
        }
        else if (source != null) {
            for (Iterator<? > i = source.iterator(); i.hasNext(); ) {
                Object current = i.next();
				if (type.isInstance(current)) {
                	/* Is checked in condition */
                	@SuppressWarnings("unchecked")
					T typeSafe = (T) current;
                	destination.add(typeSafe);
                }
            }
        }
        return destination;
	}
}

