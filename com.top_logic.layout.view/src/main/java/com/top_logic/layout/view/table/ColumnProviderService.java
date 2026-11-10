/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.text.DateFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.UICategory;
import com.top_logic.basic.type.PrimitiveTypeUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.list.FastListElementComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.CollectionLabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.view.form.AttributeOptions;
import com.top_logic.layout.view.form.DatePickerControlProvider;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.DynamicVisibility;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterPushdown;
import com.top_logic.table.FilterState;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.Resources;
import com.top_logic.table.Option;
import com.top_logic.table.Sort;
import com.top_logic.table.filter.BooleanColumnFilter;
import com.top_logic.table.filter.BoundCodec;
import com.top_logic.table.filter.ComparableColumnFilter;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.util.TLCollator;

/**
 * Resolves the {@link ColumnProvider} for a kind of value, turning a datatype into a green-field
 * table {@link Column} (accessor + renderer + comparator + filter).
 *
 * <p>
 * A column is described by a {@link ColumnType} - the model type of its values, whether a cell
 * holds several of them, and where their display annotations come from - and a function reading the
 * cell value from a row. A model attribute is one such description, not the only one, so a column
 * over a computed value gets the same affordances as one over an attribute.
 * </p>
 *
 * <p>
 * Cells display their values through {@link FieldControlService}: a cell shows a value exactly as a
 * view-mode form field does (color swatch, icon, checkbox, selection labels), so forms and tables
 * share a single type-to-display mechanism.
 * </p>
 *
 * <p>
 * Comparator and filter are resolved per value type (mirrors {@link FieldControlService}):
 * </p>
 * <ol>
 * <li>A provider configured in this service for the {@link TLType} of the values
 * (app-extensible).</li>
 * <li>A built-in default derived from the type's structure: enumeration → options filter,
 * {@code INT}/{@code FLOAT} → numeric range, {@code DATE} → date range,
 * {@code BOOLEAN}/{@code TRISTATE} → boolean, {@code STRING} → text; anything else (custom
 * primitives, references, multi-valued or unresolved descriptors) → a display-label text
 * filter.</li>
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
 * value: a truth value is narrow, a date with a time of day wide. A column configuring a width of
 * its own is shown in that width instead.
 * </p>
 */
@Label("Table columns")
@ServiceCategory.Of(UICategory.class)
public class ColumnProviderService extends ConfiguredManagedClass<ColumnProviderService.Config> {

	/** What separates the values a cell holding several of them shows. */
	static final String VALUE_SEPARATOR = ", ";

	/** What separates a number from the unit written after it. */
	static final String UNIT_SEPARATOR = " ";

	/**
	 * Configuration of the {@link ColumnProviderService}.
	 *
	 * <p>
	 * Besides the providers building the columns, the width a column is displayed in is configured
	 * here, one width per kind of value a column shows: a truth value needs far less room than a
	 * text, a date less than a date with a time of day. A column showing a value of that kind
	 * starts out this wide, unless it configures a width of its own.
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
		 * multi-valued attribute, a value of an application-defined datatype, and one whose type
		 * could not be resolved.
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
		 * The provider used for values of this type.
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
	 * Builds the column showing the described values, using a configured provider for their type if
	 * any, otherwise the built-in type-derived default.
	 *
	 * @param name
	 *        The column name, which for a column over a model attribute is the attribute name.
	 * @param type
	 *        What the column's values are, see {@link ColumnType}.
	 * @param label
	 *        The resolved column header label.
	 * @param value
	 *        Reads the cell value from a row.
	 */
	public Column<Object, ?> createColumn(String name, ResKey label, ColumnType type, Function<Object, Object> value) {
		TLType valueType = type.type();
		if (valueType != null) {
			ColumnProvider mapped = _providerByQualifiedType.get(TLModelUtil.qualifiedName(valueType));
			if (mapped != null) {
				// A provider of its own knows nothing of unreadable cells: it is given the readable
				// values only, and the unreadable cells are shown by the placeholder around it.
				Function<Object, Object> readable = row -> {
					Object cellValue = value.apply(row);
					return isUnreadable(cellValue) ? null : cellValue;
				};
				return new UnreadableAwareColumn<>(mapped.createColumn(name, label, type, readable), value);
			}
		}
		return defaultColumn(name, label, type, value);
	}

	/**
	 * Builds a column whose filter is an application-defined override matching against the cell's
	 * display text, used when a {@code <column>} configures its own filter. The cell display and
	 * the text-based sort are the same as in
	 * {@link #createColumn(String, ResKey, ColumnType, Function)}; only the filter differs.
	 */
	public Column<Object, ?> createColumn(String name, ResKey label, ColumnType type, Function<Object, Object> value,
			ColumnFilter<String> customFilter) {
		Function<Object, String> text = displayText(type);
		return valueColumn(name, label, type, defaultWidth(type), value)
			.sort(cellSort(() -> Comparator.comparing(text)))
			.filter(byText(customFilter, text))
			.build();
	}

	/**
	 * Adapts a filter over the cell's display text to a column holding raw values: the predicate and
	 * facet keys see the text the cell shows, everything else delegates unchanged.
	 *
	 * <p>
	 * A declared criterion value is passed on untouched: which value shapes a filter accepts is
	 * part of its own contract, and a facet key or an option value is not a display text.
	 * </p>
	 *
	 * @param text
	 *        The text a value is shown as, see {@link #displayText(ColumnType)}.
	 */
	private static ColumnFilter<Object> byText(ColumnFilter<String> filter, Function<Object, String> text) {
		return new ColumnFilter<>() {
			@Override
			public FilterInput input() {
				return filter.input();
			}

			@Override
			public Predicate<Object> predicate(FilterState state) {
				Predicate<String> inner = filter.predicate(state);
				return value -> inner.test(text.apply(value));
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
				return filter.facetKeys(text.apply(value));
			}
		};
	}

	/**
	 * The built-in column whose filter and comparator are derived from the type of its values,
	 * shown in the width configured for that kind of value.
	 */
	private Column<Object, ?> defaultColumn(String name, ResKey label, ColumnType type,
			Function<Object, Object> value) {
		int width = defaultWidth(type);
		if (AttributeOptions.labelsFor(type.part()) != null) {
			// Options displayed by labels of their own are sorted and searched by these labels,
			// not by the values the labels stand for.
			return labelColumn(name, label, type, width, value);
		}
		switch (columnKind(type)) {
			case BOOLEAN:
				// A two-valued boolean has no empty cells, so the filter offers just the two value
				// options.
				return booleanColumn(name, label, type, width, value, false);
			case TRISTATE:
				return booleanColumn(name, label, type, width, value, true);
			case NUMBER:
				// A bound is typed the way the column writes its values, so a German user enters a
				// decimal fraction with a comma.
				Format numberFormat = numberFormat(type);
				return typedColumn(name, label, type, width, value, Number.class,
					Comparator.comparingDouble(Number::doubleValue),
					comparableFilter(Comparator.comparingDouble(Number::doubleValue),
						BoundCodec.numbers(numberFormat)));
			case DATE:
				// A bound is typed the way the column writes its values: in the annotated format
				// of the attribute, or the default one for the part of a point in time it holds.
				return typedColumn(name, label, type, width, value, Date.class,
					Comparator.<Date> naturalOrder(),
					comparableFilter(Comparator.<Date> naturalOrder(),
						BoundCodec.dates(dateInputFormats(type), temporalKind(type).parsePatterns())));
			case STRING:
				return typedColumn(name, label, type, width, value, String.class,
					Comparator.<String> naturalOrder(), stringFilter());
			case ENUMERATION:
				return optionsColumn(name, label, type, width, value, (TLEnumeration) type.type());
			default:
				return labelColumn(name, label, type, width, value);
		}
	}

	/**
	 * The format one of the column's values is written in, or {@code null} if it holds no numbers.
	 */
	private static Format numberFormat(ColumnType type) {
		return FieldControlService.numberFormat(type.annotations(), type.type());
	}

	/**
	 * The format one of the column's points in time is written in, or {@code null} if it holds no
	 * points in time.
	 */
	private static DateFormat dateFormat(ColumnType type) {
		return FieldControlService.dateFormat(type.annotations(), type.type());
	}

	/**
	 * The formats a bound of the column's filter may be typed in, see
	 * {@link FieldControlService#dateInputFormats(AnnotationLookup, TLType)}.
	 */
	private static List<DateFormat> dateInputFormats(ColumnType type) {
		return FieldControlService.dateInputFormats(type.annotations(), type.type());
	}

	/**
	 * Which part of a point in time the column holds.
	 */
	private static ReactDatePickerControl.Kind temporalKind(ColumnType type) {
		return DatePickerControlProvider.kind(type.annotations(), type.type());
	}

	/**
	 * The kind of column a value is shown in, the one classification of its type: it decides both
	 * the column's filter and comparator and the width it is displayed in.
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
	 * Which kind of column the described values are shown in.
	 *
	 * @param columnType
	 *        What the column's values are, see {@link ColumnType}.
	 */
	private static ColumnKind columnKind(ColumnType columnType) {
		if (columnType.multiple()) {
			return ColumnKind.LABEL;
		}
		TLType type = columnType.type();
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
	 * The width in pixels a column over the described values is displayed in, as configured for the
	 * kind of column those values are shown in.
	 *
	 * <p>
	 * The width of a point in time depends on how much of it is shown: a time of day is narrower
	 * than a date, a date with a time of day wider than both.
	 * </p>
	 *
	 * <p>
	 * This is the one place a kind of value maps to a width, so a {@link ColumnBinding} building a
	 * column of its own asks here for the width that column would have been given.
	 * </p>
	 *
	 * @param type
	 *        What the column's values are, see {@link ColumnType}.
	 */
	public int defaultWidth(ColumnType type) {
		Config config = getConfig();
		switch (columnKind(type)) {
			case BOOLEAN:
			case TRISTATE:
				return config.getBooleanWidth();
			case NUMBER:
				return config.getNumberWidth();
			case DATE:
				switch (temporalKind(type)) {
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
	 * A column over boolean values, filtered by the value options labelled exactly as the column
	 * renders them.
	 *
	 * @param nullable
	 *        Whether the values have a no-value state (a tri-state boolean).
	 */
	private static Column<Object, Object> booleanColumn(String name, ResKey label, ColumnType type, int width,
			Function<Object, Object> value, boolean nullable) {
		return typedColumn(name, label, type, width, value, Boolean.class, Comparator.<Boolean> naturalOrder(),
			booleanFilter(ResKey.text(label(Boolean.TRUE)), ResKey.text(label(Boolean.FALSE)), nullable));
	}

	/*
	 * The filters of the built-in columns. Each excludes the {@link #UNREADABLE} marker before its
	 * own criterion sees a value: an unreadable cell matches no criterion on a value and offers no
	 * facet, so a filter tells nothing about the value it stands for. They stay instances of the
	 * filter they extend, whose kind decides the editor a user enters the criterion in.
	 *
	 * Facets are counted by a filter reporting countsMatches() only. A filter over a fixed value
	 * type (Boolean, String) does not override facetKeys(): the bridge method of such an override
	 * would cast the marker to the value type.
	 */

	private static BooleanColumnFilter booleanFilter(ResKey trueLabel, ResKey falseLabel, boolean nullable) {
		return new BooleanColumnFilter(trueLabel, falseLabel, nullable) {
			@Override
			public Predicate<Boolean> predicate(FilterState state) {
				return excludingUnreadable(super.predicate(state));
			}

			@Override
			public FilterState stateFor(Object value) {
				return isUnreadable(value) ? null : super.stateFor(value);
			}
		};
	}

	private static <V> ComparableColumnFilter<V> comparableFilter(Comparator<? super V> comparator,
			BoundCodec<V> codec) {
		return new ComparableColumnFilter<>(comparator, codec) {
			@Override
			public Predicate<V> predicate(FilterState state) {
				return excludingUnreadable(super.predicate(state));
			}

			@Override
			public FilterState stateFor(Object value) {
				return isUnreadable(value) ? null : super.stateFor(value);
			}

			@Override
			public Collection<Object> facetKeys(V value) {
				return isUnreadable(value) ? List.of() : super.facetKeys(value);
			}
		};
	}

	private static TextColumnFilter<String> stringFilter() {
		return new TextColumnFilter<>(Function.identity()) {
			@Override
			public Predicate<String> predicate(FilterState state) {
				return excludingUnreadable(super.predicate(state));
			}

			@Override
			public FilterState stateFor(Object value) {
				return isUnreadable(value) ? null : super.stateFor(value);
			}
		};
	}

	private static <V> OptionsColumnFilter<V> optionsFilter(List<Option> options) {
		return new OptionsColumnFilter<>(options) {
			@Override
			public Predicate<V> predicate(FilterState state) {
				return excludingUnreadable(super.predicate(state));
			}

			@Override
			public FilterState stateFor(Object value) {
				return isUnreadable(value) ? null : super.stateFor(value);
			}

			@Override
			public Collection<Object> facetKeys(V value) {
				return isUnreadable(value) ? List.of() : super.facetKeys(value);
			}
		};
	}

	/**
	 * The given predicate over cell values, rejecting the {@link #UNREADABLE} marker before the
	 * predicate sees it.
	 */
	private static <V> Predicate<V> excludingUnreadable(Predicate<V> predicate) {
		return value -> !isUnreadable(value) && predicate.test(value);
	}

	/**
	 * A column reading a typed value, with a value comparator and a matching column filter. A value
	 * that is not an instance of the expected type (a data / model-kind mismatch) yields
	 * {@code null} rather than a {@link ClassCastException}, so one stray cell cannot break the
	 * whole table render.
	 *
	 * <p>
	 * An {@link #UNREADABLE} cell keeps its marker, so that it is shown by the placeholder rather
	 * than as an empty value; the comparator never sees it, see {@link #cellSort(Supplier)},
	 * and the filter must exclude it before its own criterion, as the filters of the built-in
	 * columns do.
	 * </p>
	 */
	private static <V> Column<Object, Object> typedColumn(String name, ResKey label, ColumnType type, int width,
			Function<Object, Object> value, Class<V> valueType, Comparator<V> comparator, ColumnFilter<V> filter) {
		@SuppressWarnings("unchecked")
		ColumnFilter<Object> markerAware = (ColumnFilter<Object>) filter;
		return valueColumn(name, label, type, width, row -> typedValue(value.apply(row), valueType))
			.sort(cellSort(() -> comparator))
			.filter(markerAware)
			.build();
	}

	private static Object typedValue(Object value, Class<?> valueType) {
		return isUnreadable(value) || valueType.isInstance(value) ? value : null;
	}

	/**
	 * Whether the given cell value shows nothing: no value at all, or an empty collection of a
	 * column over several values.
	 */
	public static boolean isNoValue(Object value) {
		return value == null || value instanceof Collection<?> collection && collection.isEmpty();
	}

	/**
	 * The sort capability of a column ordering its cell values by the given comparator, see
	 * {@link #cellSort(Supplier, Predicate)}.
	 */
	public static Sort<Object> cellSort(Supplier<? extends Comparator<?>> comparator) {
		return cellSort(comparator, value -> false);
	}

	/**
	 * The sort capability of a column ordering its cell values by the given comparator, with the
	 * cells holding no ordinary value {@link Sort#isTrailing(Object) trailing} in both directions:
	 * the {@link #UNREADABLE} marker and, after it, a cell {@link #isNoValue(Object) showing
	 * nothing}, see {@link #cellOrder(Comparator, Predicate)}.
	 *
	 * @param comparator
	 *        Creates the comparator of the column's values, which never receives a trailing value.
	 *        It is created per sort, so that a comparator depending on the user sorting the table
	 *        (e.g. comparing labels in the user's locale) follows that user.
	 * @param noValue
	 *        Whether a cell value that is not {@link #isNoValue(Object) empty} as such also stands
	 *        for no value of the column, e.g. a value of an unexpected type.
	 */
	public static Sort<Object> cellSort(Supplier<? extends Comparator<?>> comparator, Predicate<Object> noValue) {
		return new Sort<>() {
			@Override
			public Comparator<Object> comparator() {
				return cellOrder(comparator.get(), noValue);
			}

			@Override
			public boolean isTrailing(Object value) {
				return rank(value, noValue) != RANK_VALUE;
			}
		};
	}

	/**
	 * The given comparator of cell values, extended to the cells holding no ordinary value, see
	 * {@link #cellOrder(Comparator, Predicate)}.
	 */
	public static Comparator<Object> cellOrder(Comparator<?> comparator) {
		return cellOrder(comparator, value -> false);
	}

	/**
	 * The given comparator of cell values, extended to the cells holding no ordinary value: an
	 * {@link #UNREADABLE} cell sorts after every value, as a masked element sorts at a fixed
	 * position - its place must not depend on the value it stands for - and a cell
	 * {@link #isNoValue(Object) showing nothing} after that.
	 *
	 * <p>
	 * As the {@link Sort#comparator()} of a column, the result establishes the ascending order;
	 * the {@link #cellSort(Supplier, Predicate) sort} built from it marks the cells holding no
	 * ordinary value {@link Sort#isTrailing(Object) trailing}, so that they keep this place in a
	 * descending order as well.
	 * </p>
	 *
	 * @param comparator
	 *        The comparator of the column's values, which never receives the marker or a value
	 *        showing nothing.
	 * @param noValue
	 *        See {@link #cellSort(Supplier, Predicate)}.
	 */
	@SuppressWarnings("unchecked")
	private static Comparator<Object> cellOrder(Comparator<?> comparator, Predicate<Object> noValue) {
		Comparator<Object> values = (Comparator<Object>) comparator;
		return (left, right) -> {
			int leftRank = rank(left, noValue);
			int rightRank = rank(right, noValue);
			if (leftRank != RANK_VALUE || rightRank != RANK_VALUE) {
				return Integer.compare(leftRank, rightRank);
			}
			return values.compare(left, right);
		};
	}

	/** The {@link #rank(Object, Predicate) rank} of an ordinary value. */
	private static final int RANK_VALUE = 0;

	/** The {@link #rank(Object, Predicate) rank} of the {@link #UNREADABLE} marker. */
	private static final int RANK_UNREADABLE = 1;

	/** The {@link #rank(Object, Predicate) rank} of a cell showing nothing. */
	private static final int RANK_NO_VALUE = 2;

	/**
	 * The position of the given cell value in the order of the kinds of cell values, see
	 * {@link #cellOrder(Comparator, Predicate)}.
	 */
	private static int rank(Object value, Predicate<Object> noValue) {
		if (isUnreadable(value)) {
			return RANK_UNREADABLE;
		}
		if (isNoValue(value) || noValue.test(value)) {
			return RANK_NO_VALUE;
		}
		return RANK_VALUE;
	}

	/**
	 * A column over classifiers of an enumeration: an options filter offering the enumeration's
	 * classifiers in their declaration order, sorted in the enumeration's order - the declaration
	 * order of its classifiers, or their display labels for an enumeration that is not
	 * {@link TLEnumeration#isOrdered() ordered}.
	 *
	 * @implNote The comparator is created per sort, so that labels are compared in the locale of
	 *           the user sorting the table. A value that is no classifier sorts as an empty one, after
	 *           every classifier in both directions.
	 */
	private static Column<Object, Object> optionsColumn(String name, ResKey label, ColumnType type, int width,
			Function<Object, Object> value, TLEnumeration enumeration) {
		List<Option> options = new ArrayList<>();
		for (TLClassifier classifier : enumeration.getClassifiers()) {
			options.add(new Option(classifier, TLModelNamingConvention.resourceKey(classifier)));
		}
		return valueColumn(name, label, type, width, value)
			.sort(cellSort(
				() -> Comparator.comparing((Object cellValue) -> (TLClassifier) cellValue,
					new FastListElementComparator(new TLCollator())),
				cellValue -> !(cellValue instanceof TLClassifier)))
			.filter(optionsFilter(options))
			.build();
	}

	/**
	 * The fallback column: sorts and text-filters by the text the cell shows, which for a cell
	 * holding several values is the text of all of them.
	 */
	private static Column<Object, Object> labelColumn(String name, ResKey label, ColumnType type, int width,
			Function<Object, Object> value) {
		Function<Object, String> text = displayText(type);
		// An unreadable cell is searched by the placeholder it shows, which reveals nothing.
		return valueColumn(name, label, type, width, value)
			.sort(cellSort(() -> Comparator.comparing(text)))
			.filter(new TextColumnFilter<>(text))
			.build();
	}

	/**
	 * A column over a value, displayed and searched consistently: the cell shows the
	 * {@link #displayContent(ColumnType, Object, Object) form display} of the value's type for the
	 * object owning the value, and the free-text search examines the text that display shows.
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
	private static <V> DefaultColumn.Builder<Object, V> valueColumn(String name, ResKey label, ColumnType type,
			int width, Function<Object, V> value) {
		return DefaultColumn.<Object, V> builder(name, value)
			.label(label)
			.width(width)
			.renderer(cellValue -> displayContent(type, cellValue))
			.rowRenderer((row, cellValue) -> displayContent(type, type.ownerOf(row), cellValue))
			.rowSearchText(cellText(type));
	}

	/**
	 * The text a cell of the described values shows, and is therefore sorted and filtered by, for a
	 * value no object owns.
	 *
	 * @see #cellText(ColumnType)
	 */
	static Function<Object, String> displayText(ColumnType type) {
		BiFunction<Object, Object, String> cellText = cellText(type);
		return value -> cellText.apply(null, value);
	}

	/**
	 * The text the cell of a row shows, computed from the row and the cell value, and therefore
	 * searched by.
	 *
	 * <p>
	 * A cell holding several values writes each of them as {@link #elementText(ColumnType) one
	 * value} is written and joins them with {@value #VALUE_SEPARATOR}, exactly as the value list
	 * displaying them does. A {@code null} row has no object owning the values.
	 * </p>
	 */
	static BiFunction<Object, Object, String> cellText(ColumnType type) {
		BiFunction<Object, Object, String> elementText = elementText(type);
		if (!type.multiple()) {
			return (row, value) -> elementText.apply(type.ownerOf(row), value);
		}
		return (row, value) -> {
			Object owner = type.ownerOf(row);
			if (!(value instanceof Collection<?>)) {
				return elementText.apply(owner, value);
			}
			return new CollectionLabelProvider(element -> elementText.apply(owner, element), VALUE_SEPARATOR)
				.getLabel(value);
		};
	}

	/**
	 * The text one of the described values is written as, computed from the object owning the value
	 * (or {@code null} for none) and the value.
	 *
	 * <p>
	 * A number is written by its {@link FieldControlService#numberFormat(AnnotationLookup, TLType)
	 * number format}, followed by its {@link FieldControlService#unit(AnnotationLookup, TLType,
	 * Object) unit} for the object owning it, separated by the {@link #UNIT_SEPARATOR}, and a point
	 * in time by its {@link FieldControlService#dateFormat(AnnotationLookup, TLType) date format},
	 * the same ones
	 * the cell's display control writes them with - so a search matches against the text the user
	 * reads, be that the digits and separators of a locale, the words of a duration, or the time of
	 * day a date is shown with. Every other value is written by its display label.
	 * </p>
	 *
	 * <p>
	 * A value of an attribute whose options state labels of their own (see
	 * {@link AttributeOptions#labelsFor(TLStructuredTypePart)}) is written by that label, as the
	 * cell's display control shows it, whatever its type.
	 * </p>
	 */
	private static BiFunction<Object, Object, String> elementText(ColumnType type) {
		LabelProvider optionLabels = AttributeOptions.labelsFor(type.part());
		if (optionLabels != null) {
			return (owner, value) -> value == null || isPlaceholder(value) ? label(value)
				: optionLabels.getLabel(value);
		}
		Format numberFormat = numberFormat(type);
		if (numberFormat != null) {
			return (owner, value) -> value instanceof Number ? numberText(type, numberFormat, owner, value)
				: label(value);
		}
		DateFormat dateFormat = dateFormat(type);
		if (dateFormat != null) {
			return (owner, value) -> value instanceof Date ? dateFormat.format(value) : label(value);
		}
		return (owner, value) -> label(value);
	}

	/**
	 * The text a number is written as: in the given format, followed by its unit for the given
	 * object owning it, where it has one.
	 */
	private static String numberText(ColumnType type, Format format, Object owner, Object value) {
		String text = format.format(value);
		String unit = FieldControlService.unit(type.annotations(), type.type(), owner);
		return unit == null ? text : text + UNIT_SEPARATOR + unit;
	}

	/**
	 * The cell content displaying a value: its view-mode form display described for a table cell
	 * (see {@link FieldControlService#createDisplayControl(ReactContext, ColumnType, Object)}), or
	 * the value's display label as plain text when nothing is known about its type.
	 *
	 * <p>
	 * An {@link #UNREADABLE} value - also as element of a collection of values - is shown as the
	 * plain text of its placeholder, whatever the type of the column, see {@link #label(Object)}.
	 * </p>
	 *
	 * @param type
	 *        What the column's values are, see {@link ColumnType}.
	 * @param value
	 *        The value to display, may be {@code null}.
	 */
	public static CellContent displayContent(ColumnType type, Object value) {
		return displayContent(type, null, value);
	}

	/**
	 * The cell content displaying a value of the given object, as
	 * {@link #displayContent(ColumnType, Object)} does, with the unit computed for that object, see
	 * {@link FieldControlService#createDisplayControl(ReactContext, ColumnType, Object, Object)}.
	 *
	 * @param type
	 *        What the column's values are, see {@link ColumnType}.
	 * @param owner
	 *        The object owning the value, see {@link ColumnType#ownerOf(Object)}; {@code null} for
	 *        none.
	 * @param value
	 *        The value to display, may be {@code null}.
	 */
	public static CellContent displayContent(ColumnType type, Object owner, Object value) {
		if (!type.resolved() || hasUnreadable(value)) {
			return CellContent.text(label(value));
		}
		return new CellContent.Raw((CellControlFactory) context -> FieldControlService.getInstance()
			.createDisplayControl(context, type, owner, value));
	}

	/**
	 * The value of a table cell whose attribute the current user may not read on its row.
	 *
	 * <p>
	 * Stands in for the attribute's value, which is never read: the cell shows the placeholder
	 * {@link com.top_logic.layout.react.I18NConstants#MASKED_VALUE} instead of the empty value of
	 * the attribute, so that a value that may not be read is told apart from no value - an
	 * unchecked checkbox, an empty text. The cell is not edited, and sorting, filtering and
	 * exporting the column see only the placeholder, see {@link #attributeValue(Object, String)}.
	 * </p>
	 *
	 * @see #isUnreadable(Object)
	 */
	public static final Object UNREADABLE = new Object() {
		@Override
		public String toString() {
			return "<unreadable>";
		}
	};

	/**
	 * Whether the given cell value is the {@link #UNREADABLE} marker of a value the current user
	 * may not read.
	 */
	public static boolean isUnreadable(Object value) {
		return value == UNREADABLE;
	}

	/**
	 * Whether the given cell value is or contains the {@link #UNREADABLE} marker.
	 */
	public static boolean hasUnreadable(Object value) {
		return isUnreadable(value)
			|| value instanceof Collection<?> collection
				&& collection.stream().anyMatch(ColumnProviderService::isUnreadable);
	}

	/**
	 * The model value of an attribute, or {@code null} for a non-model row and for a row whose type
	 * holds no such attribute - a row of a table displaying rows of several types.
	 *
	 * <p>
	 * The value function of a column over a model attribute. A value the current user may not read
	 * on the row (see {@link ModelAccessPolicy#onAttribute(BoundCommandGroup, TLObject, TLStructuredTypePart)})
	 * is not read at all: the cell holds the {@link #UNREADABLE} marker, which every type of column
	 * displays by the placeholder {@link com.top_logic.layout.react.I18NConstants#MASKED_VALUE} - so
	 * that a value the user may not read is told apart from no value. Such a cell is not edited,
	 * and sorting, filtering and exporting the column see only the placeholder.
	 * </p>
	 *
	 * <p>
	 * A value its {@link ModeSelector dynamic visibility} hides on the row, as a form field of the
	 * attribute is hidden, is not read either: the cell is empty, and sorting, filtering and
	 * exporting the column see no value. The mode is computed for an edited object while the row is
	 * edited (the row is an editing buffer, a {@link TLFormObjectBase}), and for a displayed one
	 * otherwise, see {@link #isHiddenByMode(TLObject, TLStructuredTypePart)}.
	 * </p>
	 */
	public static Object attributeValue(Object row, String attribute) {
		if (!(row instanceof TLObject object)) {
			return null;
		}
		TLStructuredType type = object.tType();
		if (type == null) {
			return object.tValueByName(attribute);
		}
		TLStructuredTypePart part = type.getPart(attribute);
		if (part == null) {
			return null;
		}
		if (!ModelAccessPolicy.onAttribute(SimpleBoundCommandGroup.READ, object, part).isExecutable()) {
			return UNREADABLE;
		}
		if (isHiddenByMode(object, part)) {
			return null;
		}
		return object.tValue(part);
	}

	/**
	 * Whether the {@link ModeSelector dynamic visibility} of the given attribute hides its value on
	 * the given row, see {@link DynamicVisibility#isVisible(FormVisibility, TLObject, TLStructuredTypePart)}.
	 *
	 * <p>
	 * An attribute without dynamic visibility is shown as its column is declared: a column declared
	 * for an attribute is shown, whatever the static visibility of the attribute says.
	 * </p>
	 */
	private static boolean isHiddenByMode(TLObject row, TLStructuredTypePart part) {
		if (DynamicVisibility.modeSelector(part) == null) {
			return false;
		}
		FormVisibility mode = DynamicVisibility.mode(row, part, row instanceof TLFormObjectBase);
		return !DynamicVisibility.isVisible(mode, row, part);
	}

	/**
	 * The localized display label of a value (empty for {@code null}).
	 *
	 * <p>
	 * An object the current user may not discover is labelled by the placeholder of a
	 * {@link ReactDropdownSelectControl#isMasked(Object) masked} value, also as element of a
	 * collection: displaying, sorting, filtering and exporting a cell never see its label. The
	 * {@link #UNREADABLE} marker of a value the user may not read is labelled by the same
	 * placeholder.
	 * </p>
	 */
	public static String label(Object value) {
		if (value == null) {
			return "";
		}
		if (isPlaceholder(value)) {
			return Resources.getInstance().getString(com.top_logic.layout.react.I18NConstants.MASKED_VALUE);
		}
		if (value instanceof Collection<?> collection
			&& collection.stream().anyMatch(ColumnProviderService::isPlaceholder)) {
			return new CollectionLabelProvider(ColumnProviderService::label, VALUE_SEPARATOR).getLabel(value);
		}
		return MetaLabelProvider.INSTANCE.getLabel(value);
	}

	/**
	 * Whether the given value is shown by the placeholder
	 * {@link com.top_logic.layout.react.I18NConstants#MASKED_VALUE}: a value the user may not read
	 * ({@link #UNREADABLE}) or an object the user may not discover
	 * ({@link ReactDropdownSelectControl#isMasked(Object) masked}).
	 */
	private static boolean isPlaceholder(Object value) {
		return isUnreadable(value) || ReactDropdownSelectControl.isMasked(value);
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
