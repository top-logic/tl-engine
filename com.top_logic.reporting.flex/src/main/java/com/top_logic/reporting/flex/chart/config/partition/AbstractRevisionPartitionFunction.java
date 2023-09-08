/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.RevisionCriterion;

/**
 * Simple {@link PartitionFunction} to provide a List of {@link Revision}s for further partitioning.
 * 
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
public abstract class AbstractRevisionPartitionFunction<C extends AbstractRevisionPartitionFunction.Config> implements PartitionFunction, ConfiguredInstance<C> {

	/**
	 * Config-interface for {@link AbstractRevisionPartitionFunction}.
	 */
	public interface Config extends PartitionFunction.Config {
		// Nothing in here...
	}

	private final C _config;

	/**
	 * Config-constructor for {@link AbstractRevisionPartitionFunction}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public AbstractRevisionPartitionFunction(InstantiationContext context, C config) {
		_config = config;
	}

	/**
	 * Return the revisions to be used.
	 * 
	 * @return The list of revisions to be used for the partitions.
	 */
	protected abstract List<Revision> getRevisions();

	@Override
	public C getConfig() {
		return _config;
	}

	@Override
	public Criterion getCriterion() {
		return RevisionCriterion.REVISION_INSTANCE;
	}

	@Override
	public List<Partition> createPartitions(Partition aParent) {
		List<Partition> result = new ArrayList<>();

		for (Revision rev : this.getRevisions()) {
			result.add(createPartition(aParent, rev));
		}

		return result;
	}

	private Partition createPartition(Partition aParent, Revision rev) {
		List<?> objects = aParent.getObjects();
		List<Object> result = new ArrayList<>();
		for (Object obj : objects) {
			Wrapper wrapper = WrapperHistoryUtils.getWrapper(rev, (Wrapper) obj);
			if (wrapper != null) {
				result.add(wrapper);
			}
		}
		return new Partition(aParent, rev, result);
	}

	/**
	 * {@link ConfigurationValueProvider} for {@link Revision}s. Absolute and relative values are
	 * supported. Examples:
	 * <ul>
	 * <li>Use SimpleDateFormat("dd.MM.yyyy") for fix revision at this date</li>
	 * <li>Use keyword "CURRENT" for {@link Revision#CURRENT}</li>
	 * <li>Use prefix r with int-value for a certain commitnumber</li>
	 * <li>Use prefix d with int-value to add days to current date to get a revision at this date</li>
	 * </ul>
	 */
	public static class RevisionValueProvider extends AbstractConfigurationValueProvider<Revision> {

		/**
		 * Singleton INSTANCE
		 */
		public static final RevisionValueProvider INSTANCE = new RevisionValueProvider();

		private static final Pattern PATTERN1 = Pattern.compile("r(\\d+)");

		private static final Pattern PATTERN2 = Pattern.compile("d(-?\\d+)");

		/**
		 * Default-Constructor for {@link RevisionValueProvider}. Use Singleton {@link #INSTANCE}
		 * instead.
		 */
		public RevisionValueProvider() {
			super(Revision.class);
		}

		@Override
		protected Revision getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			if ("CURRENT".equals(propertyValue)) {
				return Revision.CURRENT;
			}
			Matcher matcher = PATTERN1.matcher(propertyValue);
			if (matcher.matches()) {
				return HistoryUtils.getRevision(Long.valueOf(matcher.group(1)));
			}
			matcher = PATTERN2.matcher(propertyValue);
			if (matcher.matches()) {
				Date date = new Date();
				int diff = Integer.valueOf(matcher.group(1));
				date = DateUtil.addDays(date, diff);
				date = DateUtil.adjustToDayEnd(date);
				return HistoryUtils.getRevisionAt(date.getTime());
			}
			else {
				try {
					Date date = getDateFormat().parse(propertyValue.toString());
					date = DateUtil.adjustToDayEnd(date);
					return HistoryUtils.getRevisionAt(date.getTime());
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}

		@Override
		protected String getSpecificationNonNull(Revision configValue) {
			if (configValue.isCurrent()) {
				return "CURRENT";
			}
			return "r" + configValue.getCommitNumber();
		}

		private static SimpleDateFormat getDateFormat() {
			return CalendarUtil.newSimpleDateFormat("dd.MM.yyyy");
		}

	}

}