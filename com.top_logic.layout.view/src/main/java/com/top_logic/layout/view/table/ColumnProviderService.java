/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
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
 */
@Label("Table columns")
public class ColumnProviderService extends ConfiguredManagedClass<ColumnProviderService.Config> {

	/**
	 * Configuration of the {@link ColumnProviderService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<ColumnProviderService> {

		/**
		 * Provider mappings keyed by model type reference.
		 */
		@Key(ProviderMapping.TYPE)
		Map<TLModelPartRef, ProviderMapping> getProviders();

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
		return DefaultColumn.<Object, Object> builder(attribute, row -> attributeValue(row, attribute))
			.label(label)
			.renderer(value -> displayContent(part, value))
			.sort(() -> Comparator.comparing(ColumnProviderService::label))
			.filter(byLabel(customFilter))
			.build();
	}

	/**
	 * Adapts a filter over the cell's display text to a column holding raw attribute values: the
	 * predicate and facet keys see the value's display label, everything else delegates unchanged.
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
			public Collection<Object> facetKeys(Object value) {
				return filter.facetKeys(label(value));
			}
		};
	}

	/**
	 * The built-in column whose filter and comparator are derived from the attribute's type.
	 */
	private static Column<Object, ?> defaultColumn(String attribute, ResKey label, TLStructuredTypePart part) {
		if (part != null && !part.isMultiple()) {
			TLType type = part.getType();
			if (type instanceof TLEnumeration enumeration) {
				return optionsColumn(attribute, label, part, enumeration);
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
						// A two-valued boolean has no empty cells, so the filter offers just the
						// two value options.
						if (Boolean.class.isAssignableFrom(applicationType)) {
							return booleanColumn(attribute, label, part, false);
						}
						break;
					case TRISTATE:
						if (Boolean.class.isAssignableFrom(applicationType)) {
							return booleanColumn(attribute, label, part, true);
						}
						break;
					case INT:
					case FLOAT:
						if (Number.class.isAssignableFrom(applicationType)) {
							return typedColumn(attribute, label, part, Number.class,
								Comparator.comparingDouble(Number::doubleValue),
								new ComparableColumnFilter<>(Comparator.comparingDouble(Number::doubleValue),
									Double::valueOf));
						}
						break;
					case DATE:
						if (Date.class.isAssignableFrom(applicationType)) {
							// A date, a time of day and a date with a time of day share this kind;
							// which one it is decides the format a filter bound is entered in.
							ReactDatePickerControl.Kind temporalKind = DatePickerControlProvider.kind(part);
							return typedColumn(attribute, label, part, Date.class,
								Comparator.<Date> naturalOrder(),
								new ComparableColumnFilter<>(Comparator.<Date> naturalOrder(),
									text -> parseTemporal(temporalKind, text)));
						}
						break;
					case STRING:
						if (String.class.isAssignableFrom(applicationType)) {
							return typedColumn(attribute, label, part, String.class,
								Comparator.<String> naturalOrder(), TextColumnFilter.forStrings());
						}
						break;
					default:
						break;
				}
			}
		}
		return labelColumn(attribute, label, part);
	}

	/**
	 * A column over a boolean attribute, filtered by the value options labelled exactly as the
	 * column renders them.
	 *
	 * @param nullable
	 *        Whether the attribute has a no-value state (a tri-state boolean).
	 */
	private static Column<Object, Boolean> booleanColumn(String attribute, ResKey label, TLStructuredTypePart part,
			boolean nullable) {
		return typedColumn(attribute, label, part, Boolean.class, Comparator.<Boolean> naturalOrder(),
			new BooleanColumnFilter(ResKey.text(label(Boolean.TRUE)), ResKey.text(label(Boolean.FALSE)), nullable));
	}

	/**
	 * A column reading a typed attribute value, with a value comparator and a matching column
	 * filter. A value that is not an instance of the expected type (a data / model-kind mismatch)
	 * yields {@code null} rather than a {@link ClassCastException}, so one stray cell cannot break
	 * the whole table render.
	 */
	private static <V> Column<Object, V> typedColumn(String attribute, ResKey label, TLStructuredTypePart part,
			Class<V> valueType, Comparator<V> comparator, ColumnFilter<V> filter) {
		return DefaultColumn.<Object, V> builder(attribute, row -> typedValue(row, attribute, valueType))
			.label(label)
			.renderer(value -> displayContent(part, value))
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
			TLEnumeration enumeration) {
		List<Option> options = new ArrayList<>();
		for (TLClassifier classifier : enumeration.getClassifiers()) {
			options.add(new Option(classifier, TLModelNamingConvention.resourceKey(classifier)));
		}
		return DefaultColumn.<Object, Object> builder(attribute, row -> attributeValue(row, attribute))
			.label(label)
			.renderer(value -> displayContent(part, value))
			.sort(() -> Comparator.comparing(ColumnProviderService::label))
			.filter(new OptionsColumnFilter<>(options))
			.build();
	}

	/**
	 * The fallback column: sorts and text-filters by the cell's display label.
	 */
	private static Column<Object, Object> labelColumn(String attribute, ResKey label, TLStructuredTypePart part) {
		return DefaultColumn.<Object, Object> builder(attribute, row -> attributeValue(row, attribute))
			.label(label)
			.renderer(value -> displayContent(part, value))
			.sort(() -> Comparator.comparing(ColumnProviderService::label))
			.filter(new TextColumnFilter<>(ColumnProviderService::label))
			.build();
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
	 * Parses a filter bound of a temporal column in the formats belonging to its kind: a time of day
	 * is entered as a time, not as a date.
	 */
	private static Date parseTemporal(ReactDatePickerControl.Kind kind, String text) {
		String trimmed = text.trim();
		for (DateFormat format : kind.inputFormats()) {
			try {
				return format.parse(trimmed);
			} catch (ParseException ex) {
				// Try the next accepted format; an unparsable bound leaves the filter unbounded.
			}
		}
		return null;
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
