/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;

/**
 * The class {@link TupleFactory} creates for an arbitrary number of {@link Object}s an Tuple which
 * contains exactly the submitted objects.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TupleFactory {

	private static final Tuple EMPTY_TUPLE = new CompositeObjectKey(ArrayUtil.EMPTY_ARRAY);

	/**
	 * This method constructs a {@link Tuple} consisting of the {@link Object}s
	 * in <code>obj</code>. The order in the tuple is the order of
	 * <code>obj</code>. Subsequent calls of this method returns {@link Tuple}s
	 * which are equal.
	 * 
	 * <p>
	 * <b>Note:</b> The given object array may serve as storage for the tuple,
	 * so changes in the array may (but not necessarily) reflect to the values
	 * returned by {@link Tuple#get(int)}.
	 * </p>
	 * 
	 * @see #newTupleCopy(Object[])
	 * 
	 * @param obj
	 *        the base array which contains the objects to build a tuple from
	 */
	public static Tuple newTuple(Object... obj) {
		if (obj == null || obj.length == 0) {
			return EMPTY_TUPLE;
		}
		if (obj.length == 2) {
			return new Pair<>(obj[0], obj[1]);
		}
		return new CompositeObjectKey(obj);
	}

	/**
	 * This method constructs a {@link Tuple} consisting of the {@link Object}s
	 * in <code>obj</code>. The order in the tuple is the order of
	 * <code>obj</code>. Subsequent calls of this method returns {@link Tuple}s
	 * which are equal.
	 * 
	 * <p>
	 * <b>Note:</b> The method ensures that changes on the given array do not
	 * reflect to the values given by the returned tuple, so it is potentially
	 * more expensive than {@link #newTuple(Object[])}. If the application
	 * ensures that the given <code>obj</code> is untouched after constructing
	 * the tuple, then {@link #newTuple(Object[]) that method} should be used.
	 * </p>
	 * 
	 * @see #newTuple(Object[])
	 * 
	 * @param obj
	 *        the base array which contains the objects to build a tuple from
	 */
	public static Tuple newTupleCopy(Object[] obj) {
		if (obj == null || obj.length == 0) {
			return EMPTY_TUPLE;
		}
		if (obj.length == 2) {
			return new Pair<>(obj[0], obj[1]);
		}
		return new CompositeObjectKey(obj.clone());
	}
	
	/**
	 * This method is a service method which calls {@link #newTuple(Object[])} with
	 * <code>compKey.toArray()</code> as argument.
	 */
	public static Tuple newTuple(List<?> compKey) {
		return newTuple(compKey.toArray());
	}

	/**
	 * Factory method to create a new {@link Pair}.
	 * 
	 * @param <S>
	 *        Type of the first pair element.
	 * @param <T>
	 *        Type of the second pair element.
	 * @param first
	 *        First element in the pair.
	 * @param second
	 *        Second element in the pair.
	 * @return A {@link Pair} containing the elements <code>first</code> and <code>second</code>.
	 */
	public static <S, T> Pair<S, T> pair(S first, T second) {
		return new Pair<>(first, second);
	}

	/**
	 * Classes which instantiate {@link TupleFactory.Tuple} are implementations of mathematical tuples.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public interface Tuple extends Comparable<Tuple> {

		/**
		 * This method returns the size of the tuple
		 * 
		 * @return the size of the tuple. must be geq 0.
		 */
		public int size();

		/**
		 * This method returns the {@link Object} in the tuple at position <code>index</code>.
		 * 
		 * @param index
		 *            must be geq 0 and less than {@link #size() the size}.
		 * @return the object in the tuple at position <code>index</code>.
		 * @throws IndexOutOfBoundsException
		 *             if <code>index</code> is less than 0 or geq the size given by
		 *             {@link #size()}.
		 */
		public Object get(int index);

		/**
		 * If the given argument is an {@link TupleFactory.Tuple}, this method compares the given tuple with
		 * this tuple. The tuples are compared in lexicographic order.
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Tuple o);

		/**
		 * Compares the specified object with this {@link TupleFactory.Tuple} for equality. Returns <tt>true</tt>
		 * if and only if the specified object is also a {@link TupleFactory.Tuple}, both {@link TupleFactory.Tuple}s have
		 * the same size, and all corresponding pairs of elements in the two {@link TupleFactory.Tuple}s are
		 * <i>equal</i>. (Two elements <tt>e1</tt> and <tt>e2</tt> are <i>equal</i> if
		 * <tt>(e1==null ? e2==null : e1.equals(e2))</tt>.) In other words, two {@link TupleFactory.Tuple}s
		 * are defined to be equal if they contain the same elements in the same order. This
		 * definition ensures that the equals method works properly across different implementations
		 * of the <tt>Tuple</tt> interface.
		 * 
		 * @param o
		 *            the object to be compared for equality with this {@link TupleFactory.Tuple}.
		 * @return <tt>true</tt> if the specified object is equal to this {@link TupleFactory.Tuple}.
		 */
		@Override
		boolean equals(Object o);

	}

	/**
	 * The class {@link Pair} is an implementation of {@link Tuple} which contains exactly two
	 * objects.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static final class Pair<S,T> implements Tuple {

		private final S obj1;
		private final T obj2;

		/**
		 * Constructor creates a new {@link Pair}.
		 * 
		 * @param obj1
		 *        Value of {@link #getFirst()}.
		 * @param obj2
		 *        Value of {@link #getSecond()}.
		 */
		public Pair(S obj1, T obj2) {
			this.obj1 = obj1;
			this.obj2 = obj2;
		}
		
		/**
		 * Returns the first element in this {@link Pair}; 
		 */
		public final S getFirst() {
			return obj1;
		}

		/**
		 * Returns the second element in this {@link Pair}; 
		 */
		public final T getSecond() {
			return obj2;
		}
		
		@Override
		public boolean equals(Object other) {
	    	if (other == this) {
	    		return true;
	    	}
			if (!(other instanceof Tuple)) {
				return false;
			}
			Tuple t = (Tuple) other;
			if (t.size() != 2) {
				return false;
			}
			return CollectionUtil.equals(t.get(0), obj1) && CollectionUtil.equals(t.get(1), obj2);
		}

		@Override
		public int hashCode() {
			return TupleFactory.pairHashCode(this);
		}

		@Override
		public int compareTo(Tuple other) {
			return compare(this, other);
		}

		@Override
		public int size() {
			return 2;
		}

		@Override
		public Object get(int index) {
			switch (index) {
				case 0:
					return obj1;
				case 1:
					return obj2;
				default:
					throw new IndexOutOfBoundsException("A pair has just 2 entries");
			}
		}

		@Override
		public String toString() {
			return TupleFactory.toString(this);
		}
	}

	/**
	 * The class {@link CompositeObjectKey} is a general implementation of {@link Tuple} which is
	 * useful if the tuple shall contain more than two Objects.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static final class CompositeObjectKey implements Tuple {

		/** They Keys building the CompositeObject */
		private Object[] comp;

		/**
		 * Creates a new instance of this class with the given objects.
		 * 
		 * @param compKey
		 *            the composite object keys.
		 */
		/* package protected */CompositeObjectKey(Object[] compKey) {
			this.comp = compKey;
		}

		@Override
		public boolean equals(Object obj) {
	    	if (obj == this) {
	    		return true;
	    	}
			if (obj instanceof CompositeObjectKey) {
				return ArrayUtil.equals(comp, ((CompositeObjectKey) obj).comp);
			} else if (obj instanceof Tuple) {
				return TupleFactory.equalsTuple(this, (Tuple) obj);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return TupleFactory.hashCode(this);
		}

		@Override
		public int compareTo(Tuple o) {
			return TupleFactory.compare(this, o);
		}

		@Override
		public String toString() {
			return TupleFactory.toString(this);
		}

		@Override
		protected Object clone() {
			return TupleFactory.newTuple(comp);
		}

		/**
		 * Gets the size of this CompositeObjectKey.
		 * 
		 * @return the size of this CompositeObjectKey
		 */
		@Override
		public int size() {
			return comp.length;
		}

		/**
		 * Gets the object with the given index.
		 * 
		 * @param index
		 *            the index of the object to get
		 * @return the object at the given index
		 * @throws ArrayIndexOutOfBoundsException
		 *             if the index is not in range
		 */
		@Override
		public Object get(int index) {
			return comp[index];
		}

	}

	/**
	 * This method compares two {@link Tuple}s in lexicographic order. It is assumed that the
	 * objects in the tuple are instances of {@link Comparable}. The entries of the tuple will be
	 * compared via {@link ArrayUtil#compareObjects(Comparable, Comparable)}.
	 * 
	 * @return a negative integer if <code>t1</code> is lexicographic smaller than <code>t2</code>,
	 *         0 if they are equal and a positive integer if <code>t1</code> is lexicographic
	 *         greater than <code>t2</code>.
	 */
	public static int compare(Tuple t1, Tuple t2) {
		int size1 = t1.size();
		int size2 = t2.size();

		int result = 0;
		for (int i = 0, length = Math.min(size1, size2); i < length; i++) {
			result = ArrayUtil.compareObjects((Comparable) t1.get(i), (Comparable) t2.get(i));
			if (result != 0) {
				return result;
			}
		}
		return size1 - size2;
	}

    /** 
     * Shared toString() method for {@link Pair} and {@link CompositeObjectKey} 
     */
    public static final String toString(Tuple tuple) {
        int size = tuple.size();
        if (size == 0) {
            return "()";
        }
        StringBuffer result = new StringBuffer(size << 5); // * 32
        result.append('(').append(tuple.get(0));
        for (int n = 1; n < size; n++) {
            result.append(',');
            result.append(tuple.get(n));
        }
        result.append(')');

        return result.toString();
    }

    /** 
     * Gets a new array containing the values of the given tuple.
     */
    public static final Object[] toArray(Tuple tuple) {
        Object[] array = new Object[tuple.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = tuple.get(i);
        }
        return array;
    }

	/**
	 * Gets a new array containing the values of the given tuple.
	 * 
	 * @param arrayClass
	 *        runtime type of the result array
	 * @param tuple
	 *        the tuple to store as array. It is expected that all elements the tuple have the given
	 *        array type.
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T[] toArray(Class<T> arrayClass, Tuple tuple) {
		T[] array = (T[]) Array.newInstance(arrayClass, tuple.size());
		for (int i = 0; i < array.length; i++) {
			array[i] = (T) tuple.get(i);
		}
		return array;
	}

	/**
	 * Compares the given tuples for equality.
	 */
	public static boolean equalsTuple(Tuple t1, Tuple t2) {
		int size = t1.size();
		if (size != t2.size()) {
			return false;
		}
		for (int n = 0; n < size; n++) {
			if (! CollectionUtil.equals(t1.get(n), t2.get(n))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Optimization of {@link #hashCode(Tuple)} for {@link Pair}
	 */
	static int pairHashCode(Pair<?, ?> pair) {
		int hash = 31;
		if (pair.getFirst() != null) {
			hash += pair.getFirst().hashCode();
		}
		hash *= 31;
		if (pair.getSecond() != null) {
			hash += pair.getSecond().hashCode();
		}
		return hash;
	}

	/**
	 * Computes a hash code that is compatible to {@link Arrays#hashCode(Object[])} for the given
	 * tuple.
	 * 
	 * @param tuple
	 *        Must not be <code>null</code>
	 */
	public static int hashCode(Tuple tuple) {
		int size = tuple.size();
		if (size == 0) {
			return 1;
		}
		
		int result = 1;
		
		for (int i = 0; i < size; i++) {
		    Object element = tuple.get(i);
		    result = 31 * result + (element == null ? 0 : element.hashCode());
		}
		
		return result;
	}

}
