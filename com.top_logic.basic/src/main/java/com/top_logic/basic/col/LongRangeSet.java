/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Set of <code>long</code> values optimized for sets containing ranges of values.
 * 
 * <p>
 * Instances of this class are only used as {@link List} of non-overlapping
 * {@link LongRange}s in ascending order representing a set of <code>long</code>
 * values.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class LongRangeSet extends AbstractSingletonList<LongRange> implements LongRange {

	/**
	 * The empty range.
	 */
	public static final List<LongRange> EMPTY_SET = Collections.emptyList();

	/**
	 * The full {@link Long} range.
	 */
	public static final List<LongRange> FULL_SET = range(Long.MIN_VALUE, Long.MAX_VALUE);
	
	
	private static final Comparator<? super LongRange> REV_MIN_COMPARATOR = new Comparator<LongRange>() {
		@Override
		public int compare(LongRange o1, LongRange o2) {
			long o1Start = o1.getStartValue();
			long o2Start = o2.getStartValue();
			
			if (o1Start < o2Start) {
				return -1;
			} else if (o1Start > o2Start) {
				return 1;
			} else {
				return 0;
			}
		}
	};
	
	/**
	 * @see #getStartValue()
	 */
	private final long startValue;
	
	/**
	 * @see #getEndValue()
	 */
	private final long endValue;

	/**
	 * Creates a {@link LongRangeSet}.
	 * 
	 * @param startValue
	 *        See {@link #getStartValue()}.
	 * @param endValue
	 *        See {@link #getEndValue()}.
	 * 
	 * @see #range(long, long) for public construction.
	 */
	private LongRangeSet(long startValue, long endValue) {
		assert endValue >= startValue : "Ranges must not be empty.";
		this.startValue = startValue;
		this.endValue = endValue;
	}
	
	@Override
	public long getStartValue() {
		return startValue;
	}

	@Override
	public long getEndValue() {
		return endValue;
	}
	
	/**
	 * Computes the intersection of the given periods.
	 * 
	 * @param period1
	 *        List of non-overlapping ranges in ascending order.
	 * @param period2
	 *        List of non-overlapping ranges in ascending order.
	 * @return List of non-overlapping ranges in ascending order.
	 */
	public static List<LongRange> intersect(List<LongRange> period1, List<LongRange> period2) {
		int period1Size = period1.size();
		int period2Size = period2.size();
		
		if (period1Size == 0) {
			return EMPTY_SET;
		}
		if (period2Size == 0) {
			return EMPTY_SET;
		}
		
		if (period1Size == 1 && period2Size == 1) {
			// Simple intersection.
			LongRange range1 = period1.get(0);
			LongRange range2 = period2.get(0);
			
			if (range1.getStartValue() < range2.getStartValue()) {
				LongRange tmp = range1;
				range1 = range2;
				range2 = tmp;
			}
			
			// range1.getStartValue() >= range2.getStartValue()
			if (range1.getEndValue() <= range2.getEndValue()) {
				return toList(range1);
			}
			
			if (range1.getStartValue() > range2.getEndValue()) {
				return EMPTY_SET;
			}
			
			return range(range1.getStartValue(), range2.getEndValue());
		}
		
		int range2Index = 0;
		LongRange range2 = period2.get(range2Index++);
		
		// Compute the index in ranges1 of the first possible range overlap.
		int range1Index;
		int searchResult = Collections.binarySearch(period1, range2, LongRangeSet.REV_MIN_COMPARATOR);
		if (searchResult >= 0) {
			// otherRanges[firstIndex].getStartValue() == this.getStartValue()
			range1Index = searchResult;
		} else {
			int insertionPoint = -(searchResult) - 1;
			if (insertionPoint > 0) {
				range1Index = insertionPoint - 1;
			} else {
				range1Index = 0;
				// otherRanges[firstIndex].getStartValue() > this.getStartValue()
			}
		}
		
		List<LongRange> resultRanges = new ArrayList<>();
		
		LongRange range1 = period1.get(range1Index++);
		while (true) {
			if (range2.getEndValue() < range1.getStartValue()) {
				// range2 is completely processed.
				if (range2Index < period2Size) {
					range2 = period2.get(range2Index++);
				} else {
					break;
				}
			}
			
			else if (range1.getEndValue() < range2.getStartValue()) {
				// range1 does not match range2.
				if (range1Index < period1Size) {
					range1 = period1.get(range1Index++);
				} else {
					break;
				}
			}
			
			else {
				long startValueIntersection = Math.max(range1.getStartValue(), range2.getStartValue());
				long endValueIntersection;
				if (range1.getEndValue() <= range2.getEndValue()) {
					endValueIntersection = range1.getEndValue();
					resultRanges.add(new LongRangeSet(startValueIntersection, endValueIntersection));
					
					// range1 is completely processed.
					if (range1Index < period1Size) {
						range1 = period1.get(range1Index++);
					} else {
						break;
					}
				} else {
					endValueIntersection = range2.getEndValue();
					resultRanges.add(new LongRangeSet(startValueIntersection, endValueIntersection));
					
					// range2 is completely processed.
					if (range2Index < period2Size) {
						range2 = period2.get(range2Index++);
					} else {
						break;
					}
				}
			}
		}
		
		return optimizeResult(resultRanges);
	}

	private static List<LongRange> toList(LongRange range) {
		if (range instanceof LongRangeSet) {
			return (LongRangeSet) range;
		} else {
			return new LongRangeSet(range.getStartValue(), range.getEndValue());
		}
	}

	/**
	 * Computes the union of the given periods.
	 * 
	 * @param period1
	 *        List of non-overlapping ranges in ascending order.
	 * @param period2
	 *        List of non-overlapping ranges in ascending order.
	 * @return List of non-overlapping ranges in ascending order.
	 */
	public static List<LongRange> union(List<LongRange> period1, List<LongRange> period2) {
		int period1Size = period1.size();
		int period2Size = period2.size();

		if (period1Size == 0) {
			return period2;
		}
		if (period2Size == 0) {
			return period1;
		}

		LongRange range1 = period1.get(0);
		LongRange range2 = period2.get(0);

		if (period1Size == 1 && period2Size == 1) {
			// Simple union.
			return simpleUnion(range1, range2);
		}

		ArrayList<LongRange> result = new ArrayList<>();

		int period1Index = 0;
		int period2Index = 0;

		while (true) {
			LongRange potentialNext = getFirst(range1, range2);
			final long nextStart;
			long nextEnd;
			if (potentialNext != null) {
				nextStart = potentialNext.getStartValue();
				nextEnd = potentialNext.getEndValue();
			} else {
				// both periods are exploited
				break;
			}

			// adapt end of the next range in the union
			while (true) {
				if (nextEnd == Long.MAX_VALUE) {
					/* All longs from start are contained in result. */
					add(result, potentialNext, nextStart, nextEnd);
					return optimizeResult(result);
				}
				if (period1Index < period1Size) {
					range1 = period1.get(period1Index);
					assert nextStart <= range1.getStartValue();
					if (range1.getStartValue() <= nextEnd + 1) {
						long r1End = range1.getEndValue();
						if (r1End > nextEnd) {
							nextEnd = r1End;
							potentialNext = null;
						}
						period1Index++;
						continue;
					}
				} else {
					// no more content in period1
					range1 = null;
				}

				if (period2Index < period2Size) {
					range2 = period2.get(period2Index);
					assert nextStart <= range2.getStartValue();
					if (range2.getStartValue() <= nextEnd + 1) {
						long r2End = range2.getEndValue();
						if (r2End > nextEnd) {
							nextEnd = r2End;
							potentialNext = null;
						}
						period2Index++;
						continue;
					}
				} else {
					// no more content in period2
					range2 = null;
				}
				break;
			}

			add(result, potentialNext, nextStart, nextEnd);
		}
		return optimizeResult(result);

	}

	private static void add(ArrayList<LongRange> result, LongRange original, long nextStart, long nextEnd) {
		LongRange newRange;
		if (original != null) {
			// do not create new LongRange if original exists
			assert nextStart == original.getStartValue();
			assert nextEnd == original.getEndValue();
			newRange = original;
		} else {
			newRange = new LongRangeSet(nextStart, nextEnd);
		}
		result.add(newRange);
	}

	private static LongRange getFirst(LongRange range1, LongRange range2) {
		if (range1 == null) {
			return range2;
		} else if (range2 == null) {
			return range1;
		} else if (range1.getStartValue() <= range2.getStartValue()) {
			return range1;
		} else {
			return range2;
		}
	}

	private static List<LongRange> simpleUnion(LongRange range1, LongRange range2) {
		if (range2.getStartValue() < range1.getStartValue()) {
			LongRange tmp = range2;
			range2 = range1;
			range1 = tmp;
		}

		// range2.getStartValue() >= range1.getStartValue()
		if (range2.getEndValue() <= range1.getEndValue()) {
			return toList(range1);
		}

		if (range2.getStartValue() > range1.getEndValue() + 1) {
			return Arrays.asList(new LongRange[] { range1, range2 });
		}

		return range(range1.getStartValue(), range2.getEndValue());
	}

	private static List<LongRange> optimizeResult(List<LongRange> resultRanges) {
		if (resultRanges.size() > 1) {
			return resultRanges;
		} else if (resultRanges.size() == 1) {
			return toList(resultRanges.get(0));
		} else {
			return EMPTY_SET;
		}
	}

	/**
	 * Construct a range from the given minimum to the given maximum value
	 * (inclusive).
	 * 
	 * @param startValue
	 *        The start value.
	 * @param endValue
	 *        The end value. Must be greater or equal to the start value.
	 * 
	 * @return A range from the start to the end value (inclusive).
	 */
	public static List<LongRange> range(long startValue, long endValue) {
		if (endValue < startValue) {
			return EMPTY_SET;
		}
		return new LongRangeSet(startValue, endValue);
	}

	/**
	 * Construct a range from the given minimum to the largest possible value
	 * (inclusive).
	 * 
	 * @param startValue
	 *        The start value.
	 * 
	 * @return A range from the start to {@link Long#MAX_VALUE}.
	 */
	public static List<LongRange> endSection(long startValue) {
		return new LongRangeSet(startValue, Long.MAX_VALUE);
	}
	
	/**
	 * Construct a range from the smallest possible value to the given value (inclusive).
	 * 
	 * @since 5.8.0
	 * 
	 * @param endValue
	 *        The end value.
	 * 
	 * @return A range from {@link Long#MIN_VALUE} to the end.
	 */
	public static List<LongRange> startSection(long endValue) {
		return new LongRangeSet(Long.MIN_VALUE, endValue);
	}

	@Override
	protected LongRange internalGet() {
		return this;
	}

	@Override
	public int hashCode() {
		// Note: Must violate the list interface regarding hashcode due to
		// mathematical absurdity of the implementation.
		return (int)((startValue ^ (startValue >>> 32)) * (endValue ^ (endValue >>> 32)));
	}

	@Override
	public boolean equals(Object other) {
    	if (other == this) {
    		return true;
    	}
		// Note: Must violate the list interface regarding equals due to
		// mathematical absurdity of the implementation.
		if (other instanceof LongRange) {
			LongRange otherRange = (LongRange) other;
			return otherRange.getStartValue() == startValue && otherRange.getEndValue() == endValue;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "[" + startValue + ", " + endValue + "]";
	}

	/**
	 * Checks, whether the given value in within the given period.
	 */
	public static boolean contains(List<LongRange> period, long value) {
		return ! intersect(period, range(value, value)).isEmpty();
	}
	
	/**
	 * Inverses the given list of {@link LongRange}s.
	 * 
	 * <p>
	 * A long is contained in some of the returned {@link LongRange} iff it is not contained in any
	 * {@link LongRange} in the argument list.
	 * </p>
	 * 
	 * @since 5.8.0
	 * 
	 * @param period
	 *        List of non-overlapping ranges in ascending order.
	 * 
	 * @return Ascending list of {@link LongRange} which contains exactly the values which are not
	 *         contained in the given period.
	 */
	public static List<LongRange> invert(List<LongRange> period) {
		switch (period.size()) {
			case 0: {
				return FULL_SET;
			}
			default: {
				List<LongRange> result = new ArrayList<>();

				LongRange range = period.get(0);
				if (range.getStartValue() > Long.MIN_VALUE) {
					result.add(new LongRangeSet(Long.MIN_VALUE, range.getStartValue() - 1));
				}
				long lastEnd = range.getEndValue();
				for (int i = 1; i < period.size(); i++) {
					range = period.get(i);
					long start = range.getStartValue();
					if (!(start > lastEnd)) {
						throw new IllegalArgumentException(period + " is not ascending.");
					}
					if (start > lastEnd + 1) {
						result.add(new LongRangeSet(lastEnd + 1, start - 1));
					}
					lastEnd = range.getEndValue();
				}
				if (lastEnd < Long.MAX_VALUE) {
					result.add(new LongRangeSet(lastEnd + 1, Long.MAX_VALUE));
				}
				return optimizeResult(result);
			}

		}

	}

	/**
	 * The substraction of the right ranges from the left ones.
	 * 
	 * <p>
	 * The {@link #intersect(List, List) intersection} of the resulting ranges
	 * and the right ranges is {@link #EMPTY_SET}. The
	 * {@link #union(List, List)} of the resulting ranges with the right ranges
	 * is the {@link #union(List, List)} of the left and right ranges.
	 * </p>
	 * 
	 * @param left
	 *        The ranges to substract from.
	 * @param right
	 *        The ranges to substract.
	 * @return The substraction of the right from the left.
	 */
	public static List<LongRange> substract(List<LongRange> left, List<LongRange> right) {
		return intersect(left, invert(right));
	}

}
