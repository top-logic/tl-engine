/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.format.FormatDefinition;
import com.top_logic.basic.format.NormalizedParsingDecimalFormat;
import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.ui.BooleanDisplay;
import com.top_logic.model.annotate.ui.BooleanPresentation;
import com.top_logic.model.annotate.ui.Format;
import com.top_logic.model.annotate.ui.InputSize;
import com.top_logic.model.annotate.ui.TLIDColumn;
import com.top_logic.model.annotate.ui.TLSortColumns;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.config.AttributeConfigBase;
import com.top_logic.model.config.annotation.MainProperties;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLContext;

/**
 * Factory for model display annotations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DisplayAnnotations {

	/**
	 * Name of the computed attribute of the storage object that delivers the {@link TLDefaultValue
	 * annotated} {@link DefaultProvider}.
	 * 
	 * <p>
	 * See definition of {@link ApplicationObjectUtil#META_ATTRIBUTE_OBJECT_TYPE} in ElementMeta.xml
	 * </p>
	 */
	private static final String DEFAULT_PROVIDER_ATTRIBUTE = "defaultProvider";

	/**
	 * Global configuration options for {@link BooleanDisplay}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface Config extends ConfigurationItem {
		/**
		 * The global default for boolean attribute display.
		 */
		BooleanPresentation getDefaultDisplay();
	}

	/**
	 * Order of {@link TLStructuredTypePart}s.
	 */
	public static final Comparator<TLStructuredTypePart> PART_ORDER = new Comparator<>() {
		@Override
		public int compare(TLStructuredTypePart a1, TLStructuredTypePart a2) {
			double order1 = DisplayAnnotations.getSortOrder(a1);
			double order2 = DisplayAnnotations.getSortOrder(a2);

			// Use sortOrder first
			int sortCompare = Double.compare(order1, order2);
			if (sortCompare != 0) {
				return sortCompare;
			}

			return a1.getName().compareTo(a2.getName());
		}
	};

	/**
	 * Order of {@link TLClassifier}s based on their intrinsic order.
	 * 
	 * @see TLClassifier#getIndex()
	 */
	public static final Comparator<TLClassifier> CLASSIFIER_ORDER = new Comparator<>() {
		@Override
		public int compare(TLClassifier c1, TLClassifier c2) {
			if (c1 == c2) {
				return 0;
			}

			TLEnumeration enum1 = c1.getOwner();
			TLEnumeration enum2 = c2.getOwner();

			if (enum1.equals(enum2)) {
				int indexComparison = CollectionUtil.compareInt(c1.getIndex(), c2.getIndex());
				if (indexComparison == 0) {
					return c1.getName().compareTo(c2.getName());
				}
				return indexComparison;
			} else {
				return (enum1.getName().compareTo(enum2.getName()));
			}
		}
	};

	/**
	 * Decides about the display of the given boolean property.
	 */
	public static BooleanPresentation getBooleanDisplay(AnnotationLookup typePart) {
		BooleanDisplay annotation = TLAnnotations.getAnnotation(typePart, BooleanDisplay.class);
		if (annotation == null) {
			Config config = ApplicationConfig.getInstance().getConfig(Config.class);
			return config.getDefaultDisplay();
		}
		return annotation.getPresentation();
	}

	/**
	 * Factory method for {@link BooleanDisplay} annotations.
	 * 
	 * @param display
	 *        See {@link BooleanDisplay#getPresentation()}.
	 * @return The new annotation instance.
	 */
	public static BooleanDisplay createTLBooleanDisplay(BooleanPresentation display) {
		BooleanDisplay result = TypedConfiguration.newConfigItem(BooleanDisplay.class);
		result.update(result.descriptor().getProperty(BooleanDisplay.PRESENTATION_PROPERTY), display);
		return result;
	}

	/**
	 * Factory method for {@link TLI18NKey} annotations.
	 * 
	 * @param key
	 *        See {@link TLI18NKey#getValue()}.
	 * @return The new annotation instance.
	 */
	public static TLI18NKey createTLI18NKey(String key) {
		TLI18NKey result = TypedConfiguration.newConfigItem(TLI18NKey.class);
		result.update(result.descriptor().getProperty(TLI18NKey.VALUE), key);
		return result;
	}

	/**
	 * Whether the given {@link TLModelPart} is hidden during creation.
	 */
	public static boolean isHiddenInCreate(TLModelPart part) {
		return getCreateVisibility(part) == Visibility.HIDDEN;
	}

	/**
	 * The visibility of the part during create process.
	 * 
	 * @param part
	 *        The part to get create visibility from.
	 * @return The annotated {@link TLCreateVisibility#getValue() create visibility} or the
	 *         {@link TLVisibility#getValue() editor} visibility, if create visibility not set.
	 */
	public static Visibility getCreateVisibility(TLModelPart part) {
		TLCreateVisibility annotation = getCreateVisibilityAnnotation(part);
		if (annotation == null) {
			return getVisibility(part);
		}
		return annotation.getValue();
	}

	/**
	 * {@link TLCreateVisibility} annotated at the given part.
	 * 
	 * @param part
	 *        {@link TLModelPart} to get annotation for.
	 * @return May be <code>null</code> when nothing is set.
	 */
	public static TLCreateVisibility getCreateVisibilityAnnotation(TLModelPart part) {
		return TLAnnotations.getAnnotation(part, TLCreateVisibility.class);
	}

	/**
	 * The visibility of the part.
	 * 
	 * @param part
	 *        The part to get visibility from.
	 */
	public static Visibility getVisibility(AnnotationLookup part) {
		TLVisibility annotation = getVisibilityAnnotation(part);
		if (annotation == null) {
			return TLVisibility.DEFAULT_VISIBILITY;
		}
		return annotation.getValue();
	}

	/**
	 * {@link TLVisibility} annotated at the given part.
	 * 
	 * @param part
	 *        {@link TLModelPart} to get annotation for.
	 * @return May be <code>null</code> when nothing is set.
	 */
	public static TLVisibility getVisibilityAnnotation(AnnotationLookup part) {
		return TLAnnotations.getAnnotation(part, TLVisibility.class);
	}

	/**
	 * Whether the given {@link TLModelPart} is hidden.
	 */
	public static boolean isHidden(AnnotationLookup part) {
		return getVisibility(part) == Visibility.HIDDEN;
	}

	/**
	 * Whether the given {@link TLModelPart} is hidden during creation.
	 */
	public static boolean isEditableInCreate(TLModelPart part) {
		return !TLModelUtil.isDerived(part) && getCreateVisibility(part) == Visibility.EDITABLE;
	}

	/**
	 * Whether the given {@link TLModelPart} is not derived and editable.
	 */
	public static boolean isEditable(TLModelPart part) {
		return !TLModelUtil.isDerived(part) && getVisibility(part) == Visibility.EDITABLE;
	}
	
	/** @see #isHidden(AnnotationLookup) */
	public static void setHidden(TLModelPart part, boolean value) {
		if (isHidden(part) == value) {
			return;
		}
		if (value) {
			part.setAnnotation(hidden());
		} else {
			part.removeAnnotation(TLVisibility.class);
		}
	}
	
	/**
	 * Creates a {@link TLVisibility} annotation marking a model element as {@link Visibility#HIDDEN}.
	 */
	public static TLVisibility hidden() {
		return newVisibility(Visibility.HIDDEN);
	}
	
	/**
	 * Creates a {@link TLVisibility} annotation marking a model element as {@link Visibility#READ_ONLY}.
	 */
	public static TLVisibility readOnly() {
		return newVisibility(Visibility.READ_ONLY);
	}

	/**
	 * Creates a {@link TLVisibility} annotation with the given {@link Visibility}.
	 * 
	 * @param value
	 *        Value of {@link TLVisibility#getValue()}.
	 * 
	 * @see #newCreateVisibility(Visibility)
	 */
	public static TLVisibility newVisibility(Visibility value) {
		TLVisibility visibilityAnnotation = TypedConfiguration.newConfigItem(TLVisibility.class);
		visibilityAnnotation.update(visibilityAnnotation.descriptor().getProperty(TLVisibility.VALUE), value);
		return visibilityAnnotation;
	}
	
	/**
	 * Creates a {@link TLCreateVisibility} annotation with the given {@link Visibility}.
	 * 
	 * @param value
	 *        Value of {@link TLCreateVisibility#getValue()}.
	 * 
	 * @see #newVisibility(Visibility)
	 */
	public static TLCreateVisibility newCreateVisibility(Visibility value) {
		TLCreateVisibility visibilityAnnotation = TypedConfiguration.newConfigItem(TLCreateVisibility.class);
		ConfigurationDescriptor descriptor = visibilityAnnotation.descriptor();
		visibilityAnnotation.update(descriptor.getProperty(TLCreateVisibility.VALUE), value);
		return visibilityAnnotation;
	}

	/**
	 * Returns the {@link Format} annotation for the given {@link TLTypePart}, or null none given.
	 */
	public static Format getFormat(AnnotationLookup modelPart) {
		return TLAnnotations.getAnnotation(modelPart, Format.class);
	}

	/**
	 * Returns the format pattern of the given model part
	 * 
	 * @param modelPart
	 *        The {@link TLModelPart} to get configured {@link NumberFormat} from.
	 * 
	 * @return <code>null</code> if given model part has not a configured format or the format does
	 *         not base on a pattern.
	 * 
	 * @throws ConfigurationException
	 *         iff configured format is a global format but not a {@link NumberFormat} or format is
	 *         not a global format and is invalid as pattern for a {@link DecimalFormat}.
	 * 
	 * @see #getConfiguredNumberFormat(TLTypePart)
	 */
	public static String getFormatPattern(AnnotationLookup modelPart)
			throws ConfigurationException {
		Format formatAnnotation = getFormat(modelPart);
		if (formatAnnotation == null) {
			return null;
		}
		String formatReference = formatAnnotation.getFormatReference();
		if (!StringServices.isEmpty(formatReference)) {
			checkNoLiteralFormat(formatAnnotation);

			FormatDefinition<?> format = FormatterService.getInstance().getFormatDefinition(formatReference);
			if (format != null) {
				return format.getPattern();
			} else {
				throw handleGlobalFormatNotFound(formatReference);
			}
		}

		String literalFormat = formatAnnotation.getFormat();
		if (!StringServices.isEmpty(literalFormat)) {
			return literalFormat;
		}
		return null;

	}

	/**
	 * Returns a {@link DateFormat} representing the configured {@link Format} of the given model
	 * part.
	 * 
	 * <p>
	 * The configured format can either be the id of a global configured {@link DateFormat} or a
	 * {@link String} that can be used as pattern for a {@link SimpleDateFormat}.
	 * </p>
	 * 
	 * @param modelPart
	 *        The {@link TLModelPart} to get configured {@link NumberFormat} from.
	 * 
	 * @return <code>null</code> iff given model part has not configured format.
	 * 
	 * @throws ConfigurationException
	 *         iff configured format is a global format but not a {@link DateFormat} or format is
	 *         not a global format and is invalid as pattern for a {@link SimpleDateFormat}.
	 */
	public static java.text.Format getConfiguredDateFormat(AnnotationLookup modelPart)
			throws ConfigurationException {
		return getConfiguredDateFormat(getFormat(modelPart));
	}

	/**
	 * A {@link DateFormat} representing the configured {@link Format} of the given model part.
	 * 
	 * <p>
	 * The configured format can either be the id of a global configured {@link DateFormat} or a
	 * {@link String} that can be used as pattern for a {@link SimpleDateFormat}.
	 * </p>
	 * 
	 * @param formatAnnotation
	 *        The format configuration, or <code>null</code>.
	 * 
	 * @return <code>null</code> if format was configured.
	 * 
	 * @throws ConfigurationException
	 *         iff configured format is a global format but not a {@link DateFormat} or format is
	 *         not a global format and is invalid as pattern for a {@link SimpleDateFormat}.
	 */
	public static java.text.Format getConfiguredDateFormat(Format formatAnnotation) throws ConfigurationException {
		if (formatAnnotation == null) {
			return null;
		}

		String formatReference = formatAnnotation.getFormatReference();
		if (!StringServices.isEmpty(formatReference)) {
			return referencedFormat(formatAnnotation, formatReference);
		}

		String literalFormat = formatAnnotation.getFormat();
		if (!StringServices.isEmpty(literalFormat)) {
			return literalDateFormat(literalFormat);
		}
		return null;
	}

	private static java.text.Format literalDateFormat(String literalFormat)
			throws ConfigurationException {
		try {
			return CalendarUtil.newSimpleDateFormat(literalFormat);
		} catch (IllegalArgumentException ex) {
			// thrown by SimpleDateFormat if pattern is invalid
			throw handleIllegalFormatPattern(literalFormat);
		}
	}

	/**
	 * Returns a {@link NumberFormat} representing the configured {@link Format} of the given model
	 * part.
	 * 
	 * <p>
	 * The configured format can either be the id of a global configured {@link NumberFormat} or a
	 * {@link String} that can be used as pattern for a {@link DecimalFormat}.
	 * </p>
	 * 
	 * @param typePart
	 *        The {@link TLTypePart} to get configured {@link NumberFormat} from.
	 * 
	 * @return <code>null</code> iff given model part has not configured format.
	 * 
	 * @throws ConfigurationException
	 *         iff configured format is a global format but not a {@link NumberFormat} or format is
	 *         not a global format and is invalid as pattern for a {@link DecimalFormat}.
	 */
	public static java.text.Format getConfiguredNumberFormat(TLTypePart typePart) throws ConfigurationException {
		return getConfiguredNumberFormat(getFormat(typePart));
	}

	/**
	 * The {@link NumberFormat} representing the annotated {@link Format}.
	 * 
	 * <p>
	 * The configured format can either be the id of a global configured {@link NumberFormat} or a
	 * {@link String} that can be used as pattern for a {@link DecimalFormat}.
	 * </p>
	 * 
	 * @return <code>null</code> iff there is no format configuration.
	 * 
	 * @throws ConfigurationException
	 *         iff configured format is a global format but not a {@link NumberFormat} or format is
	 *         not a global format and is invalid as pattern for a {@link DecimalFormat}.
	 */
	public static java.text.Format getConfiguredNumberFormat(Format formatAnnotation) throws ConfigurationException {
		if (formatAnnotation == null) {
			return null;
		}

		String formatReference = formatAnnotation.getFormatReference();
		if (!StringServices.isEmpty(formatReference)) {
			return referencedFormat(formatAnnotation, formatReference);
		}

		String literalFormat = formatAnnotation.getFormat();
		if (!StringServices.isEmpty(literalFormat)) {
			return literalNumberFormat(literalFormat);
		}
		return null;
	}

	private static java.text.Format literalNumberFormat(String literalFormat)
			throws ConfigurationException {
		try {
			return NormalizedParsingDecimalFormat.getNormalizingInstance(literalFormat, TLContext.getLocale());
		} catch (IllegalArgumentException ex) {
			// thrown by DecimalFormat if pattern is invalid
			throw handleIllegalFormatPattern(literalFormat);
		}
	}

	private static java.text.Format referencedFormat(Format formatAnnotation, String formatReference) throws ConfigurationException {
		checkNoLiteralFormat(formatAnnotation);
		java.text.Format format = HTMLFormatter.getInstance().getFormat(formatReference);
		if (format != null) {
			return format;
		} else {
			throw handleGlobalFormatNotFound(formatReference);
		}
	}

	private static void checkNoLiteralFormat(Format formatAnnotation)
			throws ConfigurationException {
		if (!StringServices.isEmpty(formatAnnotation.getFormat())) {
			throw new ConfigurationException("Either a reference or a literal format must be given, not both.");
		}
	}

	private static ConfigurationException handleGlobalFormatNotFound(String formatReference)
			throws ConfigurationException {
		StringBuilder error = new StringBuilder();
		error.append("No global format found for given reference '");
		error.append(formatReference);
		error.append("' configured.");
		throw new ConfigurationException(error.toString());
	}

	private static ConfigurationException handleIllegalFormatPattern(String formatPattern)
			throws ConfigurationException {
		StringBuilder error = new StringBuilder();
		error.append("Invalid format pattern: ");
		error.append(formatPattern);
		throw new ConfigurationException(error.toString());
	}

	/**
	 * Returns the float format of the given model part.
	 * 
	 * <p>
	 * If there is none the {@link Formatter#getDoubleFormat() default float format} is returned.
	 * </p>
	 * 
	 * @param modelPart
	 *        The {@link TLModelPart} to get {@link NumberFormat} from.
	 */
	public static java.text.Format getFloatFormat(AnnotationLookup modelPart) throws ConfigurationException {
		return getFloatFormat(getFormat(modelPart));
	}

	/**
	 * The float format for the given format configuration.
	 * 
	 * <p>
	 * If there is none the {@link Formatter#getDoubleFormat() default float format} is returned.
	 * </p>
	 */
	public static java.text.Format getFloatFormat(Format formatAnnotation) throws ConfigurationException {
		java.text.Format configuredFormat = getConfiguredNumberFormat(formatAnnotation);
		if (configuredFormat != null) {
			return configuredFormat;
		}
		return HTMLFormatter.getInstance().getDoubleFormat();
	}

	/**
	 * Returns the float format pattern of the given model part.
	 * 
	 * @param modelPart
	 *        The {@link TLModelPart} to get {@link NumberFormat} pattern from.
	 * 
	 * @return The configured format if there is one, or the pattern of the default
	 *         {@link Formatter#DOUBLE_STYLE float format}. This may be <code>null</code>, e.g. when
	 *         the {@link java.text.Format} does not use a pattern.
	 * 
	 * @throws ConfigurationException
	 *         iff configured format is a global format but not a {@link NumberFormat} or format is
	 *         not a global format and is invalid as pattern for a {@link DecimalFormat}.
	 * 
	 * @see #getFloatFormat(AnnotationLookup)
	 */
	public static String getFloatFormatPattern(AnnotationLookup modelPart)
			throws ConfigurationException {
		String configuredPattern = getFormatPattern(modelPart);
		if (configuredPattern != null) {
			return configuredPattern;
		}
		return FormatterService.getInstance().getFormatDefinition(Formatter.DOUBLE_STYLE).getPattern();
	}

	/**
	 * Returns the date format of the given model part.
	 * 
	 * <p>
	 * If there is none the {@link Formatter#getDateFormat() default date format} is returned.
	 * </p>
	 * 
	 * @param typePart
	 *        The {@link TLModelPart} to get {@link DateFormat} from.
	 */
	public static java.text.Format getDateFormat(AnnotationLookup typePart) throws ConfigurationException {
		java.text.Format configuredFormat = getConfiguredDateFormat(typePart);
		if (configuredFormat != null) {
			return configuredFormat;
		}
		return HTMLFormatter.getInstance().getDateFormat();
	}

	/**
	 * Returns the date format pattern of the given model part.
	 * 
	 * @param modelPart
	 *        The {@link TLModelPart} to get {@link DateFormat} pattern from.
	 * 
	 * @return The configured format if there is one, or the pattern of the default
	 *         {@link Formatter#DATE_STYLE date format}. This may be <code>null</code>, e.g. when
	 *         the {@link java.text.Format} does not use a pattern.
	 * 
	 * @throws ConfigurationException
	 *         iff configured format is a global format but not a {@link DateFormat} or format is
	 *         not a global format and is invalid as pattern for a {@link SimpleDateFormat}.
	 * 
	 * @see #getDateFormat(AnnotationLookup)
	 */
	public static String getDateFormatPattern(AnnotationLookup modelPart) throws ConfigurationException {
		String configuredPattern = getFormatPattern(modelPart);
		if (configuredPattern != null) {
			return configuredPattern;
		}
		return FormatterService.getInstance().getFormatDefinition(Formatter.DATE_STYLE).getPattern();
	}

	/**
	 * Returns the long format of the given model part.
	 * 
	 * <p>
	 * If there is none the {@link Formatter#getLongFormat() default long format} is returned.
	 * </p>
	 * 
	 * @param modelPart
	 *        The {@link TLModelPart} to get {@link NumberFormat} from.
	 */
	public static java.text.Format getLongFormat(AnnotationLookup modelPart) throws ConfigurationException {
		return getLongFormat(getFormat(modelPart));
	}

	/**
	 * The long format specified by the given annotation.
	 * 
	 * <p>
	 * If there is no annotation given, the {@link Formatter#getLongFormat() default long format} is
	 * returned.
	 * </p>
	 */
	public static java.text.Format getLongFormat(Format formatAnnotation) throws ConfigurationException {
		java.text.Format configuredFormat = getConfiguredNumberFormat(formatAnnotation);
		if (configuredFormat != null) {
			return configuredFormat;
		}
		return HTMLFormatter.getInstance().getLongFormat();
	}

	/**
	 * Returns the long format pattern of the given model part.
	 * 
	 * @param modelPart
	 *        The {@link TLModelPart} to get {@link NumberFormat} pattern from.
	 * 
	 * @return The configured format if there is one, or the pattern of the default
	 *         {@link Formatter#LONG_STYLE long format}. This may be <code>null</code>, e.g. when
	 *         the {@link java.text.Format} does not use a pattern.
	 * 
	 * @throws ConfigurationException
	 *         iff configured format is a global format but not a {@link NumberFormat} or format is
	 *         not a global format and is invalid as pattern for a {@link DecimalFormat}.
	 * 
	 * @see #getLongFormat(AnnotationLookup)
	 */
	public static String getLongFormatPattern(AnnotationLookup modelPart) throws ConfigurationException {
		String configuredPattern = getFormatPattern(modelPart);
		if (configuredPattern != null) {
			return configuredPattern;
		}
		return FormatterService.getInstance().getFormatDefinition(Formatter.LONG_STYLE).getPattern();
	}

	/**
	 * Searches the relevant {@link TLIDColumn} annotation for the {@link TLType}.
	 * 
	 * @return <code>null</code> when there is none.
	 */
	public static TLIDColumn getIDColumn(TLType type) {
		return getAnnotation(type, TLIDColumn.class);
	}

	/**
	 * Searches the relevant {@link TLSortColumns} annotation for the {@link TLType}.
	 * 
	 * @return <code>null</code> when there is none.
	 */
	public static TLSortColumns getSortColumns(TLType type) {
		return getAnnotation(type, TLSortColumns.class);
	}

	/**
	 * Returns the "main properties" of a {@link TLStructuredType}.
	 * 
	 * @return An unmodifiable {@link List}.
	 * 
	 * @see MainProperties#getProperties()
	 */
	public static List<String> getMainProperties(TLStructuredType type) {
		List<String> mainProperties = getMainPropertiesNoInheritance(type);
		if (!mainProperties.isEmpty()) {
			return mainProperties;
		}

		// Search only the "main" supertype.
		TLClass primarySuperType = TLModelUtil.getPrimaryGeneralization(type);
		if (primarySuperType == null) {
			return Collections.emptyList();
		}
		return getMainProperties(primarySuperType);
	}

	private static List<String> getMainPropertiesNoInheritance(TLStructuredType type) {
		MainProperties mainProperties = TLAnnotations.getAnnotation(type, MainProperties.class);
		if (mainProperties == null || mainProperties.getProperties() == null) {
			return Collections.emptyList();
		}
		/* Always return an unmodifiable list, as returning one just sometimes can lead to bugs. */
		return Collections.unmodifiableList(mainProperties.getProperties());
	}

	/** @see TLSortOrder */
	public static double getSortOrder(TLStructuredTypePart typePart) {
		TLSortOrder annotation = TLAnnotations.getAnnotation(typePart, TLSortOrder.class);
		if (annotation == null) {
			return TLSortOrder.DEFAULT_SORT_ORDER;
		}
		return annotation.getValue();
	}

	/**
	 * Adds a {@link TLSortOrder} annotation to the given config.
	 */
	public static void setSortOrder(AttributeConfigBase config, double sortOrder) {
		setAnnotation(config, createTLSortOrder(sortOrder));
	}

	/** @see #getSortOrder(TLStructuredTypePart) */
	public static void setSortOrder(TLStructuredTypePart typePart, double sortOrder) {
		TLAnnotation annotation = createTLSortOrder(sortOrder);
		typePart.setAnnotation(annotation);
	}

	/**
	 * Factory method for {@link TLSortOrder} annotations.
	 * 
	 * @param sortOrder
	 *        See {@link TLSortOrder#getValue()}.
	 * @return The new annotation instance.
	 */
	public static TLSortOrder createTLSortOrder(double sortOrder) {
		TLSortOrder result = TypedConfiguration.newConfigItem(TLSortOrder.class);
		result.update(result.descriptor().getProperty(TLSortOrder.VALUE), sortOrder);
		return result;
	}

	/**
	 * Whether the given part has the {@link TLDeleteProtected} annotation.
	 */
	public static boolean isDeleteProtected(TLModelPart part) {
		return TLAnnotations.getAnnotation(part, TLDeleteProtected.class) != null;
	}

	/**
	 * Sets a {@link TLDeleteProtected} annotation to the given configuration.
	 */
	public static void setDeleteProtected(AttributeConfigBase config) {
		setAnnotation(config, TLDeleteProtected.class);
	}

	/**
	 * Sets a {@link TLDeleteProtected} annotation to the given part.
	 */
	public static void setDeleteProtected(TLStructuredTypePart part) {
		part.setAnnotation(TypedConfiguration.newConfigItem(TLDeleteProtected.class));
	}

	/**
	 * Searches the {@link TLAnnotation} for the given {@link TLType}.
	 * <p>
	 * When the type itself is not annotated with it, the super types are searched recursively,
	 * depth first. When none of them is annotated, too, <code>null</code> is returned.
	 * </p>
	 */
	public static <A extends TLAnnotation> A getAnnotation(TLType type, Class<A> annotationClass) {
		A annotation = type.getAnnotation(annotationClass);
		if (annotation != null) {
			return annotation;
		}
		if (type.getModelKind() != ModelKind.CLASS) {
			return null;
		}
		TLClass tlClass = (TLClass) type;
		for (TLType generalization : tlClass.getGeneralizations()) {
			A inheritedAnnotation = getAnnotation(generalization, annotationClass);
			if (inheritedAnnotation != null) {
				return inheritedAnnotation;
			}
		}
		return null;
	}

	private static void setAnnotation(AttributeConfigBase config, Class<TLDeleteProtected> type) {
		setAnnotation(config, TypedConfiguration.newConfigItem(type));
	}

	private static void setAnnotation(AttributeConfigBase config, TLAttributeAnnotation annotation) {
		config.getAnnotations().add(annotation);
	}

	/**
	 * Returns the {@link DefaultProvider} for the given part.
	 * 
	 * @param part
	 *        The part to get get default provider for.
	 * 
	 * @return A {@link DefaultProvider} for the part, or <code>null</code> when no
	 *         {@link DefaultProvider} is configured.
	 */
	public static DefaultProvider getDefaultProvider(TLStructuredTypePart part) {
		DefaultProvider localResult =
				(DefaultProvider) part.tHandle().getAttributeValue(DEFAULT_PROVIDER_ATTRIBUTE);
		if (localResult != null) {
			return localResult;
		}

		// Note: Defaults are not inherited by overriding parts. See the policy annotation at
		// TLDefaultValue.

		AttributeSettings attributeSettings = AttributeSettings.getInstance();
		TLDefaultValue annotation = attributeSettings.getConfiguredPartAnnotation(TLDefaultValue.class, part);
		if (annotation != null) {
			return TypedConfigUtil.createInstance(annotation.getProvider());
		}

		TLType type = part.getType();
		return (DefaultProvider) type.tHandle().getAttributeValue(DEFAULT_PROVIDER_ATTRIBUTE);
	}

	/**
	 * The annotated {@link InputSize}, or the given default value.
	 */
	public static int inputSize(AnnotationLookup editContext, int defaultValue) {
		InputSize annotation = editContext.getAnnotation(InputSize.class);
		if (annotation != null) {
			return annotation.getValue();
		}
		return defaultValue;
	}

}
