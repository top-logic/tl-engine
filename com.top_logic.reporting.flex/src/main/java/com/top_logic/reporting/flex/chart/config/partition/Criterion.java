/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.jfree.chart.JFreeChart;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriod;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.element.version.TagWrapper;
import com.top_logic.element.version.intf.Tag;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.aggregation.AggregationFunction;
import com.top_logic.reporting.flex.chart.config.aggregation.MethodAggregationFunction;
import com.top_logic.reporting.flex.chart.config.dataset.DateAware;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.CoordinatePartition.Coordinate;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;

/**
 * A {@link Criterion} provides context-information about the partitioning / aggregation used that
 * lead to a {@link ChartTree}.
 * 
 * @see ChartTree#getCriterion(int)
 * @see PartitionFunction#getCriterion()
 * @see AggregationFunction#getCriterion()
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public interface Criterion {

	/**
	 * name for debug-reasons
	 */
	public String getName();

	/**
	 * a label containing context-information about this {@link Criterion} in a user-friendly
	 *         way.
	 */
	public String getLabel();

	/**
	 * additional information about this {@link Criterion}
	 */
	public Set<Object> getDetails();

	/**
	 * Interface for {@link Criterion}s that are based on a {@link TLStructuredTypePart}
	 */
	public interface AttributeBasedCriterion extends Criterion {

		/**
		 * the {@link TLStructuredTypePart} used by this {@link Criterion}
		 */
		public TLStructuredTypePart getAttribute();

	}

	/**
	 * Interface for mergeable {@link Criterion}s. This is necessary if different
	 * {@link AggregationFunction} s (on the same level of the {@link ChartTree}) with different
	 * {@link Criterion}s should be registered as {@link Criterion}.
	 */
	public interface MergeableCriterion extends Criterion {

		/**
		 * Adapts the context-information in the target with the context-information of the given
		 * {@link Criterion} or creates a new merged one.
		 * 
		 * @param criterion
		 *        the {@link Criterion} containing context-information to merge
		 * @return a resulting {@link Criterion} containing context-information of the target and the
		 *         given one
		 */
		public Criterion merge(Criterion criterion);

	}

	/**
	 * Base-implementation of {@link Criterion}
	 */
	static abstract class AbstractCriterion implements Criterion {

		private final String _name;

		private final Set<Object> _details;

		public AbstractCriterion(String name) {
			_name = name;
			_details = new HashSet<>();
		}

		@Override
		public String getName() {
			return _name;
		}

		@Override
		public Set<Object> getDetails() {
			return _details;
		}

		protected void addDetails(Object object) {
			_details.add(object);
		}

		@Override
		public int hashCode() {
			return AbstractCriterion.class.hashCode() + (123 * _name.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof AbstractCriterion)) {
				return false;
			}
			AbstractCriterion other = (AbstractCriterion) obj;
			return Utils.equals(_name, other._name) && CollectionUtil.containsSame(_details, other.getDetails());
		}

		@Override
		public String getLabel() {
			String details = MetaResourceProvider.INSTANCE.getLabel(_details);
			return Resources.getInstance().getMessage(ResPrefix.legacyString(getClass().getSimpleName()).key(".label"),
				details);
		}

	}

	/**
	 * {@link Criterion} used for XY-charts
	 */
	public static class CoordinateCriterion extends AbstractCriterion {

		/**
		 * @param coord
		 *        the name of the axis this funtion provides values for
		 */
		public CoordinateCriterion(Coordinate coord) {
			super("COORDINATE");
			addDetails(coord);
		}
	}

	public static class ClassificationCriterion extends AbstractCriterion implements AttributeBasedCriterion {

		/**
		 * @param metaAttribute
		 *        the attribute for context-information
		 */
		public ClassificationCriterion(TLStructuredTypePart metaAttribute) {
			super("CLASSIFICATION");
			addDetails(metaAttribute);
		}

		@Override
		public TLStructuredTypePart getAttribute() {
			return (TLStructuredTypePart) CollectionUtil.getFirst(getDetails());
		}
	}

	/**
	 * {@link Criterion} used for time-based-values. For {@link JFreeChart} a timeseries-chart must
	 * use {@link RegularTimePeriod}s. A {@link DateCriterion} must provide such a
	 * regular-time-period for the keys creted by the PartitionFunction using the {@link Criterion}.
	 */
	public static class DateCriterion extends AbstractCriterion implements AttributeBasedCriterion {

		/**
		 * Singleton <code>INSTANCE</code> for {@link PartitionFunction}s using {@link Date}
		 * directly as key.
		 */
		public static DateCriterion DATE_INSTANCE = new DateCriterion();

		/**
		 * Creates a new {@link DateCriterion}
		 */
		public DateCriterion() {
			super("DATE");
		}

		/**
		 * @param obj
		 *        a key used in a {@link Partition}
		 * @return a date corrsponding to the given key
		 */
		public Date getDate(Comparable<?> obj) {
			return internalGetDate(unwrap(obj));
		}

		private Comparable<?> unwrap(Comparable<?> obj) {
			if (obj instanceof UniqueName) {
				return unwrap(((UniqueName) obj).getKey());
			}
			return obj;
		}

		/**
		 * Hook for subclasses if the key is not directly a {@link Date}.
		 */
		protected Date internalGetDate(Comparable<?> obj) {
			return (Date) obj;
		}

		/**
		 * @param obj
		 *        the key from the {@link Partition}
		 * @param period
		 *        the target time-period-class
		 * @return an instance of {@link RegularTimePeriod} for the date described by the given obj.
		 */
		public RegularTimePeriod getPeriod(Comparable<?> obj, Class<? extends TimePeriod> period) {
			Date date = getDate(obj);
			TLSubSessionContext subSession = DefaultDisplayContext.getDisplayContext().getSubSessionContext();
			TimeZone timeZone = subSession.getCurrentTimeZone();
			Locale locale = subSession.getCurrentLocale();
			return RegularTimePeriod.createInstance(period, date, timeZone, locale);
		}

		@Override
		public TLStructuredTypePart getAttribute() {
			Object detail = CollectionUtil.getFirst(getDetails());
			return (detail instanceof TLStructuredTypePart) ? (TLStructuredTypePart) detail : null;
		}

	}

	/**
	 * {@link DateCriterion} for {@link PartitionFunction}s using a {@link RegularTimePeriod} as key.
	 */
	public static class TimePeriodCriterion extends DateCriterion {

		/**
		 * Singleton <code>INSTANCE</code> for {@link PartitionFunction}s using
		 * {@link RegularTimePeriod} directly as key. If the {@link RegularTimePeriod} does not
		 * match the configured target class, the period is translated using the start-date of the
		 * input-period.
		 */
		public static TimePeriodCriterion TIME_PERIOD_TRANSLATE_INSTANCE = new TimePeriodCriterion(true);

		/**
		 * Singleton <code>INSTANCE</code> for {@link PartitionFunction}s using
		 * {@link RegularTimePeriod} directly as key. The input-period will be returned even if it
		 * does not match the configured target class.
		 */
		public static TimePeriodCriterion TIME_PERIOD_INSTANCE = new TimePeriodCriterion(false);

		private final boolean _translate;

		/**
		 * Creates a new {@link TimePeriodCriterion}
		 * 
		 * @param translate
		 *        indicates if the incoming period should be used directly or translated to the
		 *        configured target-class
		 */
		public TimePeriodCriterion(boolean translate) {
			_translate = translate;
		}

		@Override
		protected Date internalGetDate(Comparable<?> obj) {
			RegularTimePeriod period = (RegularTimePeriod) obj;
			return period.getStart();
		}

		@Override
		public RegularTimePeriod getPeriod(Comparable<?> obj, Class<? extends TimePeriod> period) {
			if (!_translate) {
				return (RegularTimePeriod) obj;
			}
			if (period.isInstance(obj)) {
				return (RegularTimePeriod) obj;
			}
			return super.getPeriod(obj, period);
		}

		/**
		 * Factory-method for a {@link TimePeriodCriterion} with a given {@link TLStructuredTypePart} as
		 * context-information.
		 */
		public static TimePeriodCriterion newInstance(TLStructuredTypePart ma, boolean translate) {
			TimePeriodCriterion res = new TimePeriodCriterion(translate);
			res.addDetails(ma);
			return res;
		}
	}

	/**
	 * {@link DateCriterion} for {@link PartitionFunction}s using a {@link Revision} as key.
	 */
	public static class RevisionCriterion extends DateCriterion {

		/**
		 * Singleton <code>INSTANCE</code> for {@link PartitionFunction}s using {@link Revision}
		 * directly as key.
		 */
		public static RevisionCriterion REVISION_INSTANCE = new RevisionCriterion();

		@Override
		protected Date internalGetDate(Comparable<?> obj) {
			Revision rev = (Revision) obj;
			return rev.isCurrent() ? new Date() : new Date(rev.getDate());
		}
	}

	/**
	 * {@link DateCriterion} for {@link PartitionFunction}s using a {@link TagWrapper} as key.
	 */
	public static class TagCriterion extends DateCriterion {

		/**
		 * Singleton <code>INSTANCE</code> for {@link PartitionFunction}s using {@link TagWrapper}
		 * directly as key.
		 */
		public static TagCriterion TAG_INSTANCE = new TagCriterion();

		@Override
		protected Date internalGetDate(Comparable<?> obj) {
			return ((Tag) obj).getDate();
		}
	}

	/**
	 * {@link DateCriterion} for {@link PartitionFunction}s using a {@link DateAware} as key.
	 */
	public static class DateAwareCriterion extends DateCriterion {

		/**
		 * Singleton <code>INSTANCE</code> for {@link PartitionFunction}s using {@link DateAware}
		 * directly as key.
		 */
		public static DateAwareCriterion DATE_AWARE_INSTANCE = new DateAwareCriterion();

		@Override
		protected Date internalGetDate(Comparable<?> obj) {
			return ((DateAware) obj).getDate();
		}
	}

	/**
	 * {@link Criterion} with no relevant context-information at all.
	 */
	public static class NoneCriterion extends AbstractCriterion {

		/**
		 * Singleton <code>INSTANCE</code>
		 */
		public static NoneCriterion INSTANCE = new NoneCriterion();

		private NoneCriterion() {
			super("NONE");
		}

		@Override
		protected void addDetails(Object object) {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * {@link Criterion} used if the partitioning / aggregation is based on any values and no more
	 * detailed information can be given.
	 */
	public static class ValueCriterion extends AbstractCriterion {

		/**
		 * Singleton <code>INSTANCE</code>
		 */
		public static final ValueCriterion INSTANCE = new ValueCriterion();

		private ValueCriterion() {
			super("VALUE");
		}

		@Override
		protected void addDetails(Object object) {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * Base-class for {@link Criterion}s of aggregation-functions.
	 */
	public static class FunctionCriterion extends AbstractCriterion implements MergeableCriterion {

		private String _label;

		/**
		 * Creates a new {@link FunctionCriterion}
		 */
		public FunctionCriterion(String label, TLStructuredTypePart ma) {
			super("FUNCTION");
			_label = label;
			if (ma != null) {
				addDetails(ma);
			}
		}

		@Override
		public String getLabel() {
			return _label;
		}

		@Override
		public Criterion merge(Criterion criterion) {
			if (criterion == this) {
				return this;
			}
			for (Object detail : criterion.getDetails()) {
				if (detail instanceof TLStructuredTypePart) {
					addDetails(detail);
				}
			}

			// If multiple functions are displayed, it makes no sense to display the first one as
			// "group legend".
			_label = "";

			return this;
		}

	}

	/**
	 * {@link FunctionCriterion} that allows access to the method-function that calculated the
	 * aggregation-values.
	 */
	public static class MethodCriterion extends FunctionCriterion {

		private final MethodAggregationFunction _function;

		/**
		 * Create a new {@link MethodCriterion} for a {@link MethodAggregationFunction}
		 */
		public MethodCriterion(MethodAggregationFunction function) {
			super(function.getLabel(), null);
			_function = function;
		}

		/**
		 * the {@link MethodAggregationFunction} the provided this {@link Criterion}
		 */
		public MethodAggregationFunction getFunction() {
			return _function;
		}

	}

}