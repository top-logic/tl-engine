/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.Positive;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.type.PrimitiveTypeUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.view.form.DatePickerControlProvider;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterPushdown;
import com.top_logic.table.FilterState;
import com.top_logic.table.Option;
import com.top_logic.table.filter.BooleanColumnFilter;
import com.top_logic.table.filter.BoundCodec;
import com.top_logic.table.filter.ComparableColumnFilter;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;

/**
 * Resolves the {@link ColumnProvider} for a model attribute, turning a datatype into a green-field
 * table {@link Column} (accessor + renderer + comparator + filter).
 *
 * <p>
 * Cells display attribute values through {@link FieldControlService}: a cell shows a value exactly
 * as a view-mode form field does (color swatch, icon, checkbox, selection labels), so forms and
 * tables share a single type-to-display mechanism.
 * </p>
 *
 * <p>
 * Comparator and filter are resolved per attribute type (mirrors {@link FieldControlService}):
 * </p>
 * <ol>
 * <li>A provider configured in this service for the attribute's {@link TLType} (app-extensible).</li>
 * <li>A built-in default derived from the type's structure: enumeration → options filter,
 * {@code INT}/{@code FLOAT} → numeric range, {@code DATE} → date range,
 * {@code BOOLEAN}/{@code TRISTATE} → boolean, {@code STRING} → text; anything else (custom
 * primitives, references, multi-valued or unresolved parts) → a display-label text filter.</li>
 * </ol>
 *
 * <p>
 * The built-in default is the only place a type is inspected, and it is fully overridable per type
 * through {@link Config#getProviders()} - so a new datatype plugs in a column affordance without
 * editing this layer.
 * </p>
 *
 * <p>
 * The same classification decides how wide a column starts out, one configured width per kind of
 * attribute: a truth value is narrow, a date with a time of day wide. A column configuring a width
 * of its own is shown in that width instead.
 * </p>
 */
@Label("Table columns")
public class ColumnProviderService extends ConfiguredManagedClass<ColumnProviderService.Config> {

	/**
	 * Configuration of the {@link ColumnProviderService}.
	 *
	 * <p>
	 * Besides the providers building the columns, the width a column is displayed in is configured
	 * here, one width per kind of attribute a column shows: a truth value needs far less room than
	 * a text, a date less than a date with a time of day. A column showing an attribute of that
	 * kind starts out this wide, unless it configures a width of its own.
	 * </p>
	 */
	public interface Config extends ConfiguredManagedClass.Config<ColumnProviderService> {

		/** Property name of {@link #getBooleanWidth()}. */
		String BOOLEAN_WIDTH = "boolean-width";

		/** Property name of {@link #getNumberWidth()}. */
		String NUMBER_WIDTH = "number-width";

		/** Property name of {@link #getDateWidth()}. */
		String DATE_WIDTH = "date-width";

		/** Property name of {@link #getTimeWidth()}. */
		String TIME_WIDTH = "time-width";

		/** Property name of {@link #getDateTimeWidth()}. */
		String DATE_TIME_WIDTH = "date-time-width";

		/** Property name of {@link #getEnumerationWidth()}. */
		String ENUMERATION_WIDTH = "enumeration-width";

		/** Property name of {@link #getStringWidth()}. */
		String STRING_WIDTH = "string-width";

		/** Property name of {@link #getLabelWidth()}. */
		String LABEL_WIDTH = "label-width";

		/**
		 * Provider mappings keyed by model type reference.
		 */
		@Key(ProviderMapping.TYPE)
		Map<TLModelPartRef, ProviderMapping> getProviders();

		/**
		 * The width in pixels of a column showing a truth value, used when the column configures
		 * none.
		 *
		 * <p>
		 * A truth value is displayed as a check mark, so the header label decides how much room the
		 * column needs.
		 * </p>
		 */
		@Name(BOOLEAN_WIDTH)
		@IntDefault(80)
		@Constraint(Positive.class)
		int getBooleanWidth();

		/**
		 * The width in pixels of a column showing a number, used when the column configures none.
		 */
		@Name(NUMBER_WIDTH)
		@IntDefault(100)
		@Constraint(Positive.class)
		int getNumberWidth();

		/**
		 * The width in pixels of a column showing a date, used when the column configures none.
		 */
		@Name(DATE_WIDTH)
		@IntDefault(110)
		@Constraint(Positive.class)
		int getDateWidth();

		/**
		 * The width in pixels of a column showing a time of day, used when the column configures
		 * none.
		 */
		@Name(TIME_WIDTH)
		@IntDefault(90)
		@Constraint(Positive.class)
		int getTimeWidth();

		/**
		 * The width in pixels of a column showing a date with a time of day, used when the column
		 * configures none.
		 */
		@Name(DATE_TIME_WIDTH)
		@IntDefault(160)
		@Constraint(Positive.class)
		int getDateTimeWidth();

		/**
		 * The width in pixels of a column showing a classifier of an enumeration, used when the
		 * column configures none.
		 */
		@Name(ENUMERATION_WIDTH)
		@IntDefault(120)
		@Constraint(Positive.class)
		int getEnumerationWidth();

		/**
		 * The width in pixels of a column showing a text, used when the column configures none.
		 */
		@Name(STRING_WIDTH)
		@IntDefault(150)
		@Constraint(Positive.class)
		int getStringWidth();

		/**
		 * The width in pixels of a column showing its values by their display label, used when the
		 * column configures none.
		 *
		 * <p>
		 * The width of every column not covered by one of the other widths: a reference, a
		 * multi-valued attribute, an attribute of an application-defined datatype, and one whose
		 * type could not be resolved.
		 * </p>
		 */
		@Name(LABEL_WIDTH)
		@IntDefault(150)
		@Constraint(Positive.class)
		int getLabelWidth();

	}

	/**
	 * A single type-to-{@link ColumnProvider} mapping.
	 */
	public interface ProviderMapping extends ConfigurationItem {

		/** Property name of {@link #getType()}. */
		String TYPE = "type";

		/**
		 * The model type this mapping applies to.
		 */
		@Name(TYPE)
		TLModelPartRef getType();

		/**
		 * The provider used for attributes of this type.
		 */
		@Mandatory
		PolymorphicConfiguration<? extends ColumnProvider> getImpl();

	}

	private final InstantiationContext _context;

	private Map<String, ColumnProvider> _providerByQualifiedType;

	/**
	 * Creates a {@link ColumnProviderService} from configuration.
	 */
	@CalledByReflection
	public ColumnProviderService(InstantiationContext context, Config config) {
		super(context, config);
		_context = context;
	}

	@Override
	protected void startUp() {
		super.startUp();
		_providerByQualifiedType = new HashMap<>();
		for (ProviderMapping mapping : getConfig().getProviders().values()) {
			TLModelPartRef typeRef = mapping.getType();
			if (typeRef != null) {
				TLType type = typeRef.resolveType();
				if (type != null) {
					_providerByQualifiedType.put(TLModelUtil.qualifiedName(type), _context.getInstance(mapping.getImpl()));
				}
			}
		}
	}

	/**
	 * Builds the column for the given attribute, using a configured provider for its type if any,
	 * otherwise the built-in type-derived default.
	 *
	 * @param attribute
	 *        The attribute (column) name.
	 * @param label
	 *        The resolved column header label.
	 * @param part
	 *        The model attribute, or {@code null} if the row type is unresolved.
	 */
	public Column<Object, ?> createColumn(String attribute, ResKey label, TLStructuredTypePart part) {
		if (part != null) {
			ColumnProvider mapped = _providerByQualifiedType.get(TLModelUtil.qualifiedName(part.getType()));
			if (mapped != null) {
				return mapped.createColumn(attribute, label, part);
			}
		}
		return defaultColumn(attribute, label, part);
	}

	/**
	 * Builds a column whose filter is an application-defined override matching against the cell's
	 * display text, used when a {@code <column>} configures its own filter. The cell display and
	 * the label-based sort are the same as in
	 * {@link #createColumn(String, ResKey, TLStructuredTypePart)}; only the filter differs.
	 */
	public Column<Object, ?> createColumn(String attribute, ResKey label, TLStructuredTypePart part,
			ColumnFilter<String> customFilter) {
		return valueColumn(attribute, label, part, defaultWidth(part), row -> attributeValue(row, attribute))
			.sort(() -> Comparator.comparing(ColumnProviderService::label))
			.filter(byLabel(customFilter))
			.build();
	}

	/**
	 * Adapts a filter over the cell's display text to a column holding raw attribute values: the
	 * predicate and facet keys see the value's display label, everything else delegates unchanged.
	 *
	 * <p>
	 * A declared criterion value is passed on untouched: which value shapes a filter accepts is
	 * part of its own contract, and a facet key or an option value is not a display label.
	 * </p>
	 */
	private static ColumnFilter<Object> byLabel(ColumnFilter<String> filter) {
		return new ColumnFilter<>() {
			@Override
			public FilterInput input() {
				return filter.input();
			}

			@Override
			public Predicate<Object> predicate(FilterState state) {
				Predicate<String> inner = filter.predicate(state);
				return value -> inner.test(label(value));
			}

			@Override
			public Optional<FilterPushdown> pushdown(FilterState state) {
				return filter.pushdown(state);
			}

			@Override
			public boolean countsMatches() {
				return filter.countsMatches();
			}

			@Override
			public boolean supportsInversion() {
				return filter.supportsInversion();
			}

			@Override
			public Object toJson(FilterState state) {
				return filter.toJson(state);
			}

			@Override
			public FilterState fromJson(Object json) {
				return filter.fromJson(json);
			}

			@Override
			public FilterState stateFor(Object value) {
				return filter.stateFor(value);
			}

			@Override
			public Collection<Object> facetKeys(Object value) {
				return filter.facetKeys(label(value));
			}
		};
	}

	/**
	 * The built-in column whose filter and comparator are derived from the attribute's type, shown
	 * in the width configured for that kind of attribute.
	 */
	private Column<Object, ?> defaultColumn(String attribute, ResKey label, TLStructuredTypePart part) {
		int width = defaultWidth(part);
		switch (columnKind(part)) {
			case BOOLEAN:
				// A two-valued boolean has no empty cells, so the filter offers just the two value
				// options.
				return booleanColumn(attribute, label, part, width, false);
			case TRISTATE:
				return booleanColumn(attribute, label, part, width, true);
			case NUMBER:
				// A bound is typed the way the column writes its values, so a German user enters a
				// decimal fraction with a comma.
				Format numberFormat = FieldControlService.numberFormat(part);
				return typedColumn(attribute, label, part, width, Number.class,
					Comparator.comparingDouble(Number::doubleValue),
					new ComparableColumnFilter<>(Comparator.comparingDouble(Number::doubleValue),
						BoundCodec.numbers(numberFormat)));
			case DATE:
				// Which part of a point in time the attribute holds decides the format a filter
				// bound is entered in.
				ReactDatePickerControl.Kind temporalKind = DatePickerControlProvider.kind(part);
				return typedColumn(attribute, label, part, width, Date.class,
					Comparator.<Date> naturalOrder(),
					new ComparableColumnFilter<>(Comparator.<Date> naturalOrder(),
						BoundCodec.dates(temporalKind.inputFormats(), temporalKind.parsePatterns())));
			case STRING:
				return typedColumn(attribute, label, part, width, String.class,
					Comparator.<String> naturalOrder(), TextColumnFilter.forStrings());
			case ENUMERATION:
				return optionsColumn(attribute, label, part, width, (TLEnumeration) part.getType());
			default:
				return labelColumn(attribute, label, part, width);
		}
	}

	/**
	 * The kind of column an attribute is shown in, the one classification of its type: it decides
	 * both the column's filter and comparator and the width it is displayed in.
	 */
	private enum ColumnKind {

		/** A truth value that is either true or false. */
		BOOLEAN,

		/** A truth value that also has a no-value state. */
		TRISTATE,

		/** A whole or a fractional number. */
		NUMBER,

		/** A point in time: a date, a time of day, or a date with a time of day. */
		DATE,

		/** A text. */
		STRING,

		/** A classifier of an enumeration. */
		ENUMERATION,

		/** Everything shown by the display label of its values. */
		LABEL,

	}

	/**
	 * Which kind of column the given attribute is shown in.
	 *
	 * @param part
	 *        The model attribute, or {@code null} if the row type is unresolved.
	 */
	private static ColumnKind columnKind(TLStructuredTypePart part) {
		if (part == null || part.isMultiple()) {
			return ColumnKind.LABEL;
		}
		TLType type = part.getType();
		if (type instanceof TLEnumeration) {
			return ColumnKind.ENUMERATION;
		}
		if (type instanceof TLPrimitive primitive) {
			// The kind describes the storage format; the values seen here are application
			// values, whose type is defined by the storage mapping. A datatype whose mapping
			// translates to a different application type (e.g. I18NString: stored as string,
			// application value ResKey) gets the label-based fallback column instead. A mapping
			// may declare its application type as a primitive (e.g.
			// com.top_logic.element.meta.kbbased.storage.mappings.BooleanMapping: boolean), while
			// the values passing through a column are always boxed - so compare against the
			// wrapper type.
			Class<?> applicationType =
				PrimitiveTypeUtil.asNonPrimitive(primitive.getStorageMapping().getApplicationType());
			switch (primitive.getKind()) {
				case BOOLEAN:
					if (Boolean.class.isAssignableFrom(applicationType)) {
						return ColumnKind.BOOLEAN;
					}
					break;
				case TRISTATE:
					if (Boolean.class.isAssignableFrom(applicationType)) {
						return ColumnKind.TRISTATE;
					}
					break;
				case INT:
				case FLOAT:
					if (Number.class.isAssignableFrom(applicationType)) {
						return ColumnKind.NUMBER;
					}
					break;
				case DATE:
					if (Date.class.isAssignableFrom(applicationType)) {
						return ColumnKind.DATE;
					}
					break;
				case STRING:
					if (String.class.isAssignableFrom(applicationType)) {
						return ColumnKind.STRING;
					}
					break;
				default:
					break;
			}
		}
		return ColumnKind.LABEL;
	}

	/**
	 * The width in pixels a column over the given attribute is displayed in, as configured for the
	 * kind of column that attribute is shown in.
	 *
	 * <p>
	 * The width of a point in time depends on how much of it is shown: a time of day is narrower
	 * than a date, a date with a time of day wider than both.
	 * </p>
	 *
	 * <p>
	 * This is the one place a kind of attribute maps to a width, so a {@link ColumnBinding}
	 * building a column of its own asks here for the width that column would have been given.
	 * </p>
	 *
	 * @param part
	 *        The model attribute, or {@code null} if the row type is unresolved.
	 */
	public int defaultWidth(TLStructuredTypePart part) {
		Config config = getConfig();
		switch (columnKind(part)) {
			case BOOLEAN:
			case TRISTATE:
				return config.getBooleanWidth();
			case NUMBER:
				return config.getNumberWidth();
			case DATE:
				switch (DatePickerControlProvider.kind(part)) {
					case TIME:
						return config.getTimeWidth();
					case DATE_TIME:
						return config.getDateTimeWidth();
					default:
						return config.getDateWidth();
				}
			case STRING:
				return config.getStringWidth();
			case ENUMERATION:
				return config.getEnumerationWidth();
			default:
				return config.getLabelWidth();
		}
	}

	/**
	 * A column over a boolean attribute, filtered by the value options labelled exactly as the
	 * column renders them.
	 *
	 * @param nullable
	 *        Whether the attribute has a no-value state (a tri-state boolean).
	 */
	private static Column<Object, Boolean> booleanColumn(String attribute, ResKey label, TLStructuredTypePart part,
			int width, boolean nullable) {
		return typedColumn(attribute, label, part, width, Boolean.class, Comparator.<Boolean> naturalOrder(),
			new BooleanColumnFilter(ResKey.text(label(Boolean.TRUE)), ResKey.text(label(Boolean.FALSE)), nullable));
	}

	/**
	 * A column reading a typed attribute value, with a value comparator and a matching column
	 * filter. A value that is not an instance of the expected type (a data / model-kind mismatch)
	 * yields {@code null} rather than a {@link ClassCastException}, so one stray cell cannot break
	 * the whole table render.
	 */
	private static <V> Column<Object, V> typedColumn(String attribute, ResKey label, TLStructuredTypePart part,
			int width, Class<V> valueType, Comparator<V> comparator, ColumnFilter<V> filter) {
		return valueColumn(attribute, label, part, width, row -> typedValue(row, attribute, valueType))
			.sort(() -> comparator)
			.filter(filter)
			.build();
	}

	private static <V> V typedValue(Object row, String attribute, Class<V> valueType) {
		Object value = attributeValue(row, attribute);
		return valueType.isInstance(value) ? valueType.cast(value) : null;
	}

	/**
	 * A column over an enumeration attribute: an options filter offering the enumeration's
	 * classifiers, sorted by their display labels.
	 */
	private static Column<Object, Object> optionsColumn(String attribute, ResKey label, TLStructuredTypePart part,
			int width, TLEnumeration enumeration) {
		List<Option> options = new ArrayList<>();
		for (TLClassifier classifier : enumeration.getClassifiers()) {
			options.add(new Option(classifier, TLModelNamingConvention.resourceKey(classifier)));
		}
		return valueColumn(attribute, label, part, width, row -> attributeValue(row, attribute))
			.sort(() -> Comparator.comparing(ColumnProviderService::label))
			.filter(new OptionsColumnFilter<>(options))
			.build();
	}

	/**
	 * The fallback column: sorts and text-filters by the cell's display label.
	 */
	private static Column<Object, Object> labelColumn(String attribute, ResKey label, TLStructuredTypePart part,
			int width) {
		return valueColumn(attribute, label, part, width, row -> attributeValue(row, attribute))
			.sort(() -> Comparator.comparing(ColumnProviderService::label))
			.filter(new TextColumnFilter<>(ColumnProviderService::label))
			.build();
	}

	/**
	 * A column over an attribute value, displayed and searched consistently: the cell shows the
	 * attribute's {@link #displayContent(TLStructuredTypePart, Object) form display}, and the
	 * free-text search examines the text that display shows.
	 *
	 * <p>
	 * The two belong together: a form display is a control, which carries no text of its own, so a
	 * column built from it takes part in a search only through a text derived from the value. Every
	 * column this service builds goes through here, so that showing a value and finding it never
	 * come apart.
	 * </p>
	 *
	 * @param width
	 *        The width in pixels the column is displayed in.
	 * @param value
	 *        Reads the cell value from a row.
	 * @return The builder, for the caller to add the column's sort and filter capabilities.
	 */
	private static <V> DefaultColumn.Builder<Object, V> valueColumn(String attribute, ResKey label,
			TLStructuredTypePart part, int width, Function<Object, V> value) {
		return DefaultColumn.<Object, V> builder(attribute, value)
			.label(label)
			.width(width)
			.renderer(cellValue -> displayContent(part, cellValue))
			.searchText(searchText(part));
	}

	/**
	 * The text a cell of the given attribute is searched by: the text its display shows.
	 *
	 * <p>
	 * A numeric attribute is written by its {@link FieldControlService#numberFormat(TLStructuredTypePart)
	 * number format}, the same one the cell's display control writes it with - so a search matches
	 * against the text the user reads, be that the digits and separators of a locale or the words of
	 * a duration. Every other value is searched by its display label.
	 * </p>
	 */
	private static Function<Object, String> searchText(TLStructuredTypePart part) {
		Format numberFormat = FieldControlService.numberFormat(part);
		if (numberFormat == null) {
			return ColumnProviderService::label;
		}
		return value -> value instanceof Number ? numberFormat.format(value) : label(value);
	}

	/**
	 * The cell content displaying an attribute value: the attribute's view-mode form display (see
	 * {@link FieldControlService#createDisplayControl}), or the value's display label as plain text
	 * when no attribute is available.
	 *
	 * @param part
	 *        The model attribute the value belongs to, or {@code null} if the row type is
	 *        unresolved.
	 * @param value
	 *        The attribute value to display, may be {@code null}.
	 */
	public static CellContent displayContent(TLStructuredTypePart part, Object value) {
		if (part == null) {
			return CellContent.text(label(value));
		}
		return new CellContent.Raw((CellControlFactory) context -> FieldControlService.getInstance()
			.createDisplayControl(context, part, value));
	}

	/**
	 * The raw model value of an attribute, or {@code null} for a non-model row.
	 */
	public static Object attributeValue(Object row, String attribute) {
		return row instanceof TLObject object ? object.tValueByName(attribute) : null;
	}

	/**
	 * The localized display label of a value (empty for {@code null}).
	 */
	public static String label(Object value) {
		return value == null ? "" : MetaLabelProvider.INSTANCE.getLabel(value);
	}

	/**
	 * The {@link ColumnProviderService} singleton.
	 */
	public static ColumnProviderService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton holder for the {@link ColumnProviderService}.
	 */
	public static final class Module extends TypedRuntimeModule<ColumnProviderService> {

		/** Singleton {@link ColumnProviderService.Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<ColumnProviderService> getImplementation() {
			return ColumnProviderService.class;
		}

	}

}
