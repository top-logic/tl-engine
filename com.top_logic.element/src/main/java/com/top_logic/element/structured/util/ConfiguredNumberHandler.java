/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.knowledge.service.db2.SequenceManager;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.values.edit.annotation.DynamicMandatory;

/**
 * {@link NumberHandler} based on a {@link SequenceManager} providing the sequence of raw numbers.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ConfiguredNumberHandler extends AbstractConfiguredInstance<ConfiguredNumberHandler.Config>
		implements NumberHandler {
	
	/**
	 * {@link ConfiguredNumberHandler} options that are directly configurable at the UI.
	 * 
	 * @see NumberHandlerDefaultProvider.Config
	 */
	public static interface UIConfig extends ConfigurationItem {

		/** @see #getPattern() */
		String PATTERN = "pattern";

		/** @see #getNumberPattern() */
		String NUMBER_PATTERN = "number-pattern";

		/** @see #getDatePattern() */
		String DATE_PATTERN = "date-pattern";

		/** @see #getDynamicSequenceName() */
		String DYNAMIC_SEQUENCE_NAME = "dynamic-sequence-name";

		/**
		 * Template for the generated ID.
		 * 
		 * <p>
		 * The template must contain the placeholder {@value SequenceIdGenerator#NUMBER_PLACEHOLDER}
		 * that is replaced with a consecutive number. The format of this number is given in
		 * {@link #getNumberPattern()}. If {@link #getDynamicSequenceName()} is given, the pattern
		 * may also contain the placeholder {@value SequenceIdGenerator#OBJECT_PLACEHOLDER} to add
		 * an indication of the number range to the ID. If {@link #getDatePattern()} is given, the
		 * pattern may contain the placeholder {@value SequenceIdGenerator#DATE_PLACEHOLDER} to add
		 * an indication of the time slot the number range belongs to.
		 * </p>
		 */
		@Name(PATTERN)
		@Constraint(ContainsNumberPattern.class)
		@StringDefault(SequenceIdGenerator.NUMBER_PLACEHOLDER)
		@Label("ID pattern")
		String getPattern();

		/**
		 * @see #getPattern()
		 */
		void setPattern(String pattern);

		/**
		 * Format of the consecutive number added to the generated ID.
		 * 
		 * <p>
		 * The formatted value replaces the {@value SequenceIdGenerator#NUMBER_PLACEHOLDER}
		 * placeholder in {@link #getPattern()}.
		 * </p>
		 * 
		 * <p>
		 * The value must be a valid {@link DecimalFormat} format pattern.
		 * </p>
		 * 
		 * <p>
		 * The value <code>000</code> formats the number with three digits including leading zeros,
		 * while <code>#</code> formats the number with as much digits as required without leading
		 * zeros.
		 * </p>
		 * 
		 * @see DecimalFormat
		 */
		@Name(NUMBER_PATTERN)
		@Constraint(IsNumberFormatPattern.class)
		@StringDefault("#")
		@Label("Number format")
		String getNumberPattern();

		/**
		 * @see #getNumberPattern()
		 */
		void setNumberPattern(String pattern);

		/**
		 * Format of the current date to use as part of the ID.
		 * 
		 * <p>
		 * For example, the following pattern letters can be used:
		 * </p>
		 * 
		 * <blockquote>
		 * <table>
		 * <tr>
		 * <th>Pattern</th>
		 * <th>Description</th>
		 * <th>Example</th>
		 * </tr>
		 * <tr>
		 * <td><code>yyyy</code></td>
		 * <td>Year</td>
		 * <td><code>1996</code></td>
		 * </tr>
		 * <tr>
		 * <td><code>MM</code></td>
		 * <td>Month in year</td>
		 * <td><code>07</code></td>
		 * </tr>
		 * <tr>
		 * <td><code>ww</code></td>
		 * <td>Week in year</td>
		 * <td><code>27</code></td>
		 * </tr>
		 * <tr>
		 * <td><code>dd</code></td>
		 * <td>Day in month</td>
		 * <td><code>10</code></td>
		 * </tr>
		 * <tr>
		 * <td><code>HH</code></td>
		 * <td>Hour in day (0-23)</td>
		 * <td><code>0</code></td>
		 * </tr>
		 * <tr>
		 * <td><code>mm</code></td>
		 * <td>Minute in hour</td>
		 * <td><code>30</code></td>
		 * </tr>
		 * <tr>
		 * <td><code>ss</code></td>
		 * <td>Second in minute</td>
		 * <td><code>55</code></td>
		 * </tr>
		 * <tr>
		 * <td><code>SSS</code></td>
		 * <td>Millisecond</td>
		 * <td><code>978</code></td>
		 * </tr>
		 * </table>
		 * </blockquote>
		 * 
		 * <p>
		 * If a value is given, the generated numbers start a new sequence for each unique value
		 * produced by this format. E.g. when using the value <code>yyyy</code> a new sequence of
		 * numbers is started for each year.
		 * </p>
		 * 
		 * <p>
		 * The formatted value replaces the {@value SequenceIdGenerator#DATE_PLACEHOLDER}
		 * placeholder in {@link #getPattern()}.
		 * </p>
		 * 
		 * @implNote A complete documentation of the possible format symbols can be found in
		 *           {@link SimpleDateFormat}.
		 */
		@Nullable
		@Name(DATE_PATTERN)
		@Constraint(IsDateFormatPattern.class)
		@DynamicMandatory(fun = IfHasDatePlaceholder.class, args = @Ref(PATTERN))
		@Label("Date format")
		String getDatePattern();
		
		/**
		 * @see #getDatePattern()
		 */
		void setDatePattern(String pattern);

		/**
		 * {@link LabelProvider Function} computing a dynamic sequence name based on the context, in
		 * which a new object is created.
		 * 
		 * <p>
		 * If given, a new sequence of numbers is started for each unique value computed from the
		 * creation context of the object.
		 * </p>
		 * 
		 * <p>
		 * The computed value replaces the {@value SequenceIdGenerator#OBJECT_PLACEHOLDER}
		 * placeholder in {@link #getPattern()}.
		 * </p>
		 */
		@Name(DYNAMIC_SEQUENCE_NAME)
		@DynamicMandatory(fun = IfHasObjectPlaceholder.class, args = @Ref(PATTERN))
		PolymorphicConfiguration<? extends DynamicSequenceName> getDynamicSequenceName();

		/**
		 * @see #getDynamicSequenceName()
		 */
		void setDynamicSequenceName(PolymorphicConfiguration<? extends DynamicSequenceName> value);

		/**
		 * {@link ValueConstraint} checking that the value contains the number pattern.
		 */
		class ContainsNumberPattern extends ValueConstraint<String> {

			/**
			 * Creates a {@link ContainsNumberPattern}.
			 */
			public ContainsNumberPattern() {
				super(String.class);
			}

			@Override
			protected void checkValue(PropertyModel<String> propertyModel) {
				String value = propertyModel.getValue();

				if (value.indexOf(SequenceIdGenerator.NUMBER_PLACEHOLDER) < 0) {
					propertyModel.setProblemDescription(
						I18NConstants.ERROR_NUMBER_HANDLER_NO_NUMBER_PATTERN
							.fill(SequenceIdGenerator.NUMBER_PLACEHOLDER));
				}
			}

		}

		/**
		 * Decides whether the given value contains the
		 * {@value SequenceIdGenerator#DATE_PLACEHOLDER}.
		 */
		class IfHasDatePlaceholder extends Function1<Boolean, String> {
			@Override
			public Boolean apply(String value) {
				return Boolean.valueOf(value.indexOf(SequenceIdGenerator.DATE_PLACEHOLDER) >= 0);
			}
		}

		/**
		 * Decides whether the given value contains the
		 * {@value SequenceIdGenerator#OBJECT_PLACEHOLDER}.
		 */
		class IfHasObjectPlaceholder extends Function1<Boolean, String> {
			@Override
			public Boolean apply(String value) {
				return Boolean.valueOf(value.indexOf(SequenceIdGenerator.OBJECT_PLACEHOLDER) >= 0);
			}
		}

		/**
		 * {@link ValueConstraint} checking that the value is a valid number format pattern.
		 */
		class IsNumberFormatPattern extends ValueConstraint<String> {

			/**
			 * Creates a {@link IsNumberFormatPattern}.
			 */
			public IsNumberFormatPattern() {
				super(String.class);
			}

			@Override
			protected void checkValue(PropertyModel<String> propertyModel) {
				String value = propertyModel.getValue();
				if (StringServices.isEmpty(value)) {
					return;
				}
				try {
					createDecimalFormat(value);
				} catch (IllegalArgumentException ex) {
					propertyModel.setProblemDescription(
						I18NConstants.ERROR_NUMBER_HANDLER_INVALID_NUMBER_PATTERN);
				}
			}

			private DecimalFormat createDecimalFormat(String value) throws IllegalArgumentException {
				return new DecimalFormat(value);
			}
		}

		/**
		 * {@link ValueConstraint} checking that the value is a valid number format pattern.
		 */
		class IsDateFormatPattern extends ValueConstraint<String> {

			/**
			 * Creates a {@link IsNumberFormatPattern}.
			 */
			public IsDateFormatPattern() {
				super(String.class);
			}

			@Override
			protected void checkValue(PropertyModel<String> propertyModel) {
				String value = propertyModel.getValue();
				if (StringServices.isEmpty(value)) {
					return;
				}
				try {
					CalendarUtil.newSimpleDateFormat(value);
				} catch (IllegalArgumentException ex) {
					propertyModel.setProblemDescription(
						I18NConstants.ERROR_NUMBER_HANDLER_INVALID_DATE_PATTERN);
				}
			}
		}

	}

	/**
	 * Configuration of a {@link ConfiguredNumberHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends NumberHandler.NumberHandlerConfig<ConfiguredNumberHandler>, UIConfig {

		/** @see #getRetryCount() */
		String RETRY_COUNT = "retry-count";

		/**
		 * How often the {@link NumberHandler} tries to create a new number before a failure occurs.
		 */
		@IntDefault(3)
		@Name(RETRY_COUNT)
		@Hidden
		int getRetryCount();

		/**
		 * @see #getRetryCount()
		 */
		void setRetryCount(int retryCount);

	}

	private SequenceIdGenerator _generator;

	/**
	 * Creates a new {@link ConfiguredNumberHandler} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ConfiguredNumberHandler}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public ConfiguredNumberHandler(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		String sequenceName = config.getName();
		String idPattern = config.getPattern();
		String numberPattern = getConfig().getNumberPattern();
		String datePattern = config.getDatePattern();
		DynamicSequenceName dynamicSequenceName = context.getInstance(config.getDynamicSequenceName());
		int retryCount = getConfig().getRetryCount();
		_generator =
			new SequenceIdGenerator(sequenceName, idPattern, numberPattern, datePattern, dynamicSequenceName,
				retryCount);
	}

	@Override
	public Object generateId(Object context) throws GenerateNumberException {
		return _generator.generateId(context);
	}

}
