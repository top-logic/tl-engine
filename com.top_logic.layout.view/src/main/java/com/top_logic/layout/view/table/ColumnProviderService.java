/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.HTMLFormatter;
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
 * Resolution chain (mirrors {@code FieldControlService}):
 * </p>
 * <ol>
 * <li>A provider configured in this service for the attribute's {@link TLType} (app-extensible).</li>
 * <li>A built-in default derived from the type's structure: enumeration &rarr; options filter,
 * {@code INT}/{@code FLOAT} &rarr; numeric range, {@code DATE} &rarr; date range,
 * {@code BOOLEAN}/{@code TRISTATE} &rarr; boolean, {@code STRING} &rarr; text; anything else (custom
 * primitives, references, multi-valued or unresolved parts) &rarr; a display-label text filter.</li>
 * </ol>
 *
 * <p>
 * The built-in default is the only place a type is inspected, and it is fully overridable per type
 * through {@link Config#getProviders()} - so a new datatype plugs in a column affordance without
 * editing this layer.
 * </p>
 */
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
	 * display text, used when a {@code <column>} configures its own filter. The accessor, renderer
	 * and sort are the display-label defaults; only the filter differs from
	 * {@link #createColumn(String, ResKey, TLStructuredTypePart)}.
	 */
	public Column<Object, ?> createColumn(String attribute, ResKey label, ColumnFilter<String> customFilter) {
		return DefaultColumn.<Object, String> builder(attribute, row -> label(attributeValue(row, attribute)))
			.label(label)
			.renderer(value -> CellContent.text(value))
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(customFilter)
			.build();
	}

	/**
	 * Builds a column whose cell <em>value is the whole row object</em>, so a custom filter (e.g. a
	 * {@code ScriptedFilter}) receives the full row as context. The cell still displays the
	 * attribute's localized label.
	 */
	public Column<Object, ?> createScriptedColumn(String attribute, ResKey label, ColumnFilter<Object> filter) {
		return DefaultColumn.<Object, Object> builder(attribute, row -> row)
			.label(label)
			.renderer(value -> CellContent.text(label(attributeValue(value, attribute))))
			.sort(() -> Comparator.comparing(value -> label(attributeValue(value, attribute))))
			.filter(filter)
			.build();
	}

	/**
	 * The built-in column whose filter and comparator are derived from the attribute's type.
	 */
	private static Column<Object, ?> defaultColumn(String attribute, ResKey label, TLStructuredTypePart part) {
		if (part != null && !part.isMultiple()) {
			TLType type = part.getType();
			if (type instanceof TLEnumeration enumeration) {
				return optionsColumn(attribute, label, enumeration);
			}
			if (type instanceof TLPrimitive primitive) {
				switch (primitive.getKind()) {
					case BOOLEAN:
					case TRISTATE:
						// Label the filter's true/false options exactly as the cells render them.
						return typedColumn(attribute, label, Boolean.class,
							Comparator.<Boolean> naturalOrder(),
							new BooleanColumnFilter(ResKey.text(label(Boolean.TRUE)), ResKey.text(label(Boolean.FALSE))));
					case INT:
					case FLOAT:
						return typedColumn(attribute, label, Number.class,
							Comparator.comparingDouble(Number::doubleValue),
							new ComparableColumnFilter<>(Comparator.comparingDouble(Number::doubleValue),
								Double::valueOf));
					case DATE:
						return typedColumn(attribute, label, Date.class,
							Comparator.<Date> naturalOrder(),
							new ComparableColumnFilter<>(Comparator.<Date> naturalOrder(),
								ColumnProviderService::parseDate));
					case STRING:
						return typedColumn(attribute, label, String.class,
							Comparator.<String> naturalOrder(), TextColumnFilter.forStrings());
					default:
						break;
				}
			}
		}
		return labelColumn(attribute, label);
	}

	/**
	 * A column reading a typed attribute value, with a value comparator and a matching column
	 * filter. The cell renders the value's localized display label. A value that is not an instance
	 * of the expected type (a data / model-kind mismatch) yields {@code null} rather than a
	 * {@link ClassCastException}, so one stray cell cannot break the whole table render.
	 */
	private static <V> Column<Object, V> typedColumn(String attribute, ResKey label, Class<V> valueType,
			Comparator<V> comparator, ColumnFilter<V> filter) {
		return DefaultColumn.<Object, V> builder(attribute, row -> typedValue(row, attribute, valueType))
			.label(label)
			.renderer(value -> CellContent.text(label(value)))
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
	 * classifiers, sorted and rendered by their display labels.
	 */
	private static Column<Object, Object> optionsColumn(String attribute, ResKey label, TLEnumeration enumeration) {
		List<Option> options = new ArrayList<>();
		for (TLClassifier classifier : enumeration.getClassifiers()) {
			options.add(new Option(classifier, TLModelNamingConvention.resourceKey(classifier)));
		}
		return DefaultColumn.<Object, Object> builder(attribute, row -> attributeValue(row, attribute))
			.label(label)
			.renderer(value -> CellContent.text(label(value)))
			.sort(() -> Comparator.comparing(ColumnProviderService::label))
			.filter(new OptionsColumnFilter<>(options))
			.build();
	}

	/**
	 * The fallback column: sorts and text-filters by the cell's display label.
	 */
	private static Column<Object, Object> labelColumn(String attribute, ResKey label) {
		return DefaultColumn.<Object, Object> builder(attribute, row -> attributeValue(row, attribute))
			.label(label)
			.renderer(value -> CellContent.text(label(value)))
			.sort(() -> Comparator.comparing(ColumnProviderService::label))
			.filter(new TextColumnFilter<>(ColumnProviderService::label))
			.build();
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

	private static Date parseDate(String text) {
		try {
			return HTMLFormatter.getInstance().getDateFormat().parse(text.trim());
		} catch (ParseException ex) {
			return null;
		}
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
