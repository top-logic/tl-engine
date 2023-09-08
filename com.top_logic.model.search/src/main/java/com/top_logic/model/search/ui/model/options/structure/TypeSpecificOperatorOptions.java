/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options.structure;

import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLTypeVisitor;
import com.top_logic.model.search.annotate.TLSearchOptions;
import com.top_logic.model.search.ui.model.operator.BooleanCompare;
import com.top_logic.model.search.ui.model.operator.DateCompare;
import com.top_logic.model.search.ui.model.operator.DateRangeCompare;
import com.top_logic.model.search.ui.model.operator.FilterCompare;
import com.top_logic.model.search.ui.model.operator.FilterCompareSingleton;
import com.top_logic.model.search.ui.model.operator.FloatCompare;
import com.top_logic.model.search.ui.model.operator.FloatRangeCompare;
import com.top_logic.model.search.ui.model.operator.IntegerCompare;
import com.top_logic.model.search.ui.model.operator.IntegerRangeCompare;
import com.top_logic.model.search.ui.model.operator.Operator;
import com.top_logic.model.search.ui.model.operator.PrimitiveCompareKind;
import com.top_logic.model.search.ui.model.operator.RangeCompareKind;
import com.top_logic.model.search.ui.model.operator.SetCompare;
import com.top_logic.model.search.ui.model.operator.SingletonCompare;
import com.top_logic.model.search.ui.model.operator.SingletonCompareOperation;
import com.top_logic.model.search.ui.model.operator.StringCompare;
import com.top_logic.model.search.ui.model.operator.StringRangeCompare;
import com.top_logic.model.search.ui.model.operator.TypeCheck;
import com.top_logic.model.search.ui.model.options.SearchTypeUtil;

/**
 * The options provider for {@link Operator}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class TypeSpecificOperatorOptions implements TLTypeVisitor<List<Operator<?>>, Void> {

	private final boolean _multiple;

	private final String _searchName;

	/**
	 * Creates {@link TypeSpecificOperatorOptions}.
	 * 
	 * @param multiple
	 *        Whether the value is a collection of values.
	 */
	TypeSpecificOperatorOptions(boolean multiple, String searchName) {
		_multiple = multiple;
		_searchName = searchName;
	}

	@Override
	public List<Operator<?>> visitPrimitive(TLPrimitive model, Void arg) {
		if (_multiple) {
			/* Comparing two collections of primitive values against each other is not yet
			 * implemented. */
			return emptyList();
		}

		TLSearchOptions options = model.getAnnotation(TLSearchOptions.class);
		if (options != null) {
			return options.getOperators().get();
		}

		switch (model.getKind()) {
			case STRING:
				return StringOptions.INSTANCE.get();
			case TRISTATE:
			case BOOLEAN:
				return BooleanOptions.INSTANCE.get();
			case DATE:
				return DateOptions.INSTANCE.get();
			case FLOAT:
				return FloatOptions.INSTANCE.get();
			case INT:
				return IntOptions.INSTANCE.get();
			case BINARY:
			case CUSTOM: {
				return emptyList();
			}
			default: {
				throw new UnreachableAssertion("Unknown " + TLPrimitive.class.getSimpleName() + ": "
					+ model.getKind().getExternalName());
			}
		}
	}

	@Override
	public List<Operator<?>> visitEnumeration(TLEnumeration model, Void arg) {
		ArrayList<Operator<?>> comparisons = new ArrayList<>();
		if (_multiple) {
			comparisons.add(setCompare(null));
		} else {
			comparisons.add(singletonCompare(SingletonCompareOperation.EQUALS));
			comparisons.add(singletonCompare(SingletonCompareOperation.CONTAINED_IN));
		}
		return comparisons;
	}

	@Override
	public List<Operator<?>> visitClass(TLClass model, Void arg) {
		ArrayList<Operator<?>> comparisons = new ArrayList<>();
		if (_multiple) {
			comparisons.add(setCompare(null));
			comparisons.add(filterCompare());
		} else {
			comparisons.add(singletonCompare(SingletonCompareOperation.EQUALS));
			comparisons.add(singletonCompare(SingletonCompareOperation.CONTAINED_IN));
			comparisons.add(filterCompareSingleton());
			if (SearchTypeUtil.hasSubtypes(model, _searchName)) {
				comparisons.add(typeCheck());
			}
		}

		return comparisons;
	}

	private FilterCompare filterCompare() {
		return TypedConfiguration.newConfigItem(FilterCompare.class);
	}

	private FilterCompareSingleton filterCompareSingleton() {
		return TypedConfiguration.newConfigItem(FilterCompareSingleton.class);
	}

	private static SingletonCompare singletonCompare(SingletonCompareOperation operation) {
		SingletonCompare result = TypedConfiguration.newConfigItem(SingletonCompare.class);
		result.setOperation(operation);
		return result;
	}

	private TypeCheck typeCheck() {
		return TypedConfiguration.newConfigItem(TypeCheck.class);
	}

	private SetCompare setCompare(SetCompare.Operation operation) {
		SetCompare result = TypedConfiguration.newConfigItem(SetCompare.class);
		result.setOperation(operation);
		return result;
	}

	@Override
	public List<Operator<?>> visitAssociation(TLAssociation model, Void arg) {
		// TODO bhu Automatically created
		return emptyList();
	}

	public static class StringOptions implements Provider<List<Operator<?>>> {

		/**
		 * Singleton {@link TypeSpecificOperatorOptions.StringOptions} instance.
		 */
		public static final StringOptions INSTANCE = new StringOptions();

		private StringOptions() {
			// Singleton constructor.
		}

		@Override
		public List<Operator<?>> get() {
			return CollectionUtil.concat(
				options(StringCompare.Kind.values()),
				options(RangeCompareKind.values()));
		}

		private List<Operator<?>> options(StringCompare.Kind[] values) {
			return Arrays.asList(values).stream().map(kind -> stringCompare(kind)).collect(Collectors.toList());
		}

		private Operator<?> stringCompare(StringCompare.Kind kind) {
			StringCompare result = TypedConfiguration.newConfigItem(StringCompare.class);
			if (kind != null) {
				result.setKind(kind);
			}
			return result;
		}

		private List<Operator<?>> options(RangeCompareKind[] values) {
			return Arrays.asList(values).stream().map(kind -> stringRangeCompare(kind)).collect(Collectors.toList());
		}

		private Operator<?> stringRangeCompare(RangeCompareKind kind) {
			StringRangeCompare result = TypedConfiguration.newConfigItem(StringRangeCompare.class);
			if (kind != null) {
				result.setKind(kind);
			}
			return result;
		}

	}

	public static class BooleanOptions implements Provider<List<Operator<?>>> {

		/**
		 * Singleton {@link BooleanOptions} instance.
		 */
		public static final BooleanOptions INSTANCE = new BooleanOptions();

		private BooleanOptions() {
			// Singleton constructor.
		}

		@Override
		public List<Operator<?>> get() {
			return options(BooleanCompare.Kind.values());
		}

		private List<Operator<?>> options(BooleanCompare.Kind[] values) {
			return Arrays.asList(values).stream().map(kind -> booleanCompare(kind)).collect(Collectors.toList());
		}

		private Operator<?> booleanCompare(BooleanCompare.Kind kind) {
			BooleanCompare result = TypedConfiguration.newConfigItem(BooleanCompare.class);
			if (kind != null) {
				result.setKind(kind);
			}
			return result;
		}
	}

	public static class FloatOptions implements Provider<List<Operator<?>>> {

		/**
		 * Singleton {@link FloatOptions} instance.
		 */
		public static final FloatOptions INSTANCE = new FloatOptions();

		private FloatOptions() {
			// Singleton constructor.
		}

		@Override
		public List<Operator<?>> get() {
			return CollectionUtil.concat(
				options(FloatCompare.Kind.values()),
				options(RangeCompareKind.values()));
		}

		private List<Operator<?>> options(FloatCompare.Kind[] values) {
			return Arrays.asList(values).stream().map(kind -> floatCompare(kind)).collect(Collectors.toList());
		}

		private Operator<?> floatCompare(FloatCompare.Kind kind) {
			FloatCompare result = TypedConfiguration.newConfigItem(FloatCompare.class);
			if (kind != null) {
				result.setKind(kind);
			}
			return result;
		}

		private List<Operator<?>> options(RangeCompareKind[] values) {
			return Arrays.asList(values).stream().map(kind -> floatRangeCompare(kind)).collect(Collectors.toList());
		}

		private Operator<?> floatRangeCompare(RangeCompareKind kind) {
			FloatRangeCompare result = TypedConfiguration.newConfigItem(FloatRangeCompare.class);
			if (kind != null) {
				result.setKind(kind);
			}
			return result;
		}
	}

	public static class IntOptions implements Provider<List<Operator<?>>> {

		/**
		 * Singleton {@link IntOptions} instance.
		 */
		public static final IntOptions INSTANCE = new IntOptions();

		private IntOptions() {
			// Singleton constructor.
		}

		@Override
		public List<Operator<?>> get() {
			return CollectionUtil.concat(
				options(IntegerCompare.Kind.values()),
				options(RangeCompareKind.values()));
		}

		private List<Operator<?>> options(IntegerCompare.Kind[] values) {
			return Arrays.asList(values).stream().map(kind -> integerCompare(kind)).collect(Collectors.toList());
		}

		private Operator<?> integerCompare(IntegerCompare.Kind kind) {
			IntegerCompare result = TypedConfiguration.newConfigItem(IntegerCompare.class);
			if (kind != null) {
				result.setKind(kind);
			}
			return result;
		}

		private List<Operator<?>> options(RangeCompareKind[] values) {
			return Arrays.asList(values).stream().map(kind -> integerRangeCompare(kind)).collect(Collectors.toList());
		}

		private Operator<?> integerRangeCompare(RangeCompareKind kind) {
			IntegerRangeCompare result = TypedConfiguration.newConfigItem(IntegerRangeCompare.class);
			if (kind != null) {
				result.setKind(kind);
			}
			return result;
		}
	}

	public static class DateOptions implements Provider<List<Operator<?>>> {

		/**
		 * Singleton {@link DateOptions} instance.
		 */
		public static final DateOptions INSTANCE = new DateOptions();

		private DateOptions() {
			// Singleton constructor.
		}

		@Override
		public List<Operator<?>> get() {
			return CollectionUtil.concat(
				options(PrimitiveCompareKind.values()),
				options(RangeCompareKind.values()));
		}

		private List<Operator<?>> options(PrimitiveCompareKind[] values) {
			return Arrays.asList(values).stream().map(kind -> dateCompare(kind)).collect(Collectors.toList());
		}

		private Operator<?> dateCompare(PrimitiveCompareKind kind) {
			DateCompare result = TypedConfiguration.newConfigItem(DateCompare.class);
			if (kind != null) {
				result.setKind(kind);
			}
			return result;
		}

		private List<Operator<?>> options(RangeCompareKind[] values) {
			return Arrays.asList(values).stream().map(kind -> dateRangeCompare(kind)).collect(Collectors.toList());
		}

		private Operator<?> dateRangeCompare(RangeCompareKind kind) {
			DateRangeCompare result = TypedConfiguration.newConfigItem(DateRangeCompare.class);
			if (kind != null) {
				result.setKind(kind);
			}
			return result;
		}
	}
}
