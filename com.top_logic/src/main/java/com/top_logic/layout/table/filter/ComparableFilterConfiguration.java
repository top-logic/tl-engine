/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Arrays;
import java.util.List;

import com.top_logic.layout.table.TableViewModel;

/**
 * A configuration used by filters for comparables.
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public class ComparableFilterConfiguration extends SelectionFilterConfiguration {

	private Comparable<?> primaryFilterPattern;
	private Comparable<?> secondaryFilterPattern;
	private Operators operator;
	private ComparisonOperatorsProvider operatorsProvider;
	private FilterComparator _comparator;


	/** Create a new empty {@link ComparableFilterConfiguration} */
	public ComparableFilterConfiguration(TableViewModel tableModel, String columnName, ComparisonOperatorsProvider operatorsProvider,
			FilterComparator comparator, boolean hasCollectionValues, boolean showOptionEntries) {
		super(tableModel, columnName, IdentityTransformer.INSTANCE, hasCollectionValues, showOptionEntries);
		assert comparator != null : "Filter comparator must not be null";
		this.operatorsProvider = operatorsProvider;
		_comparator = comparator;
		setUseRawOptions(true);
		clearConfiguration();
	}
	
	/** 
	 *{@inheritDoc}
	 */
	@Override
	public List<Object> getSerializedState() {
		return VersionBasedComparableFilterSerialization.INSTANCE.serialize(this);
	}

	/** 
	 *{@inheritDoc}
	 */
	@Override
	public void setSerializedState(List<Object> state) {
		VersionBasedComparableFilterSerialization.INSTANCE.deserialize(this, state);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public FilterConfigurationType getConfigurationType() {
		return new FilterConfigurationType(this.getClass(), NumberTransformer.getInstance().getClass());
	}
	
	/**
	 * the primary filter pattern.
	 */
	public Comparable<?> getPrimaryFilterPattern() {
		return primaryFilterPattern;
	}

	/**
	 * the secondary filter pattern.
	 */
	public Comparable<?> getSecondaryFilterPattern() {
		return secondaryFilterPattern;
	}

	/**
	 * @param   filterPattern - the filter pattern to set.
	 */
	public void setPrimaryFilterPattern(Comparable<?> filterPattern) {
		this.primaryFilterPattern = filterPattern;
		notifyValueChanged();
	}

	/**
	 * @param filterPattern
	 *        - the filter pattern to set.
	 */
	public void setSecondaryFilterPattern(Comparable<?> filterPattern) {
		this.secondaryFilterPattern = filterPattern;
		notifyValueChanged();
	}

	/**
	 * the comparison operator.
	 */
	public Operators getOperator() {
		return operator;
	}

	/**
	 * @param   operator - the comparison operator to set.
	 */
	public void setOperator(Operators operator) {
		this.operator = operator;
		notifyValueChanged();
	}
	
	/**
	 * This class is a transformer for numbers.
	 * 
	 * @author <a href=mailto:sts@top-logic.com>sts</a>
	 */
	public static class NumberTransformer implements JSONTransformer{
		
		private static NumberTransformer INSTANCE = new NumberTransformer();
		
		/**
		 * Private constructor for singleton
		 */
		private NumberTransformer(){}
		
		/**
		 * getter for singleton instance
		 */
		public static NumberTransformer getInstance() {
			return INSTANCE;
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public List<Object> transformFromJSON(List<Object> jsonObjects) {
			int operatorSerializationIndex = getOperatorIndex(jsonObjects);
			
			// Transform operator, due to number was directly deserialized by JSON
			jsonObjects.set(operatorSerializationIndex,
				Enum.valueOf(Operators.class, (String) jsonObjects.get(operatorSerializationIndex)));
			
			return jsonObjects;
		}

		/** 
		 * {@inheritDoc}
		 */
		@Override
		public List<Object> transformToJSON(Object... rawObjects) {
			List<Object> transformedObjects = Arrays.asList(rawObjects);
			
			int operatorSerializationIndex = getOperatorIndex(transformedObjects);

			// Transform operator, due to number can be directly serialized by JSON
			transformedObjects.set(operatorSerializationIndex,
				((Operators) transformedObjects.get(operatorSerializationIndex)).name());
			
			return transformedObjects;
		}

		private int getOperatorIndex(List<Object> transformedObjects) {
			if (transformedObjects.size() == 2) {
				return 1;
			} else {
				return 2;
			}
		}
	}
	
	/**
	 * all comparison operators, available for this filter.
	 */
	public List<Operators> getAvailableOperators() {
		return operatorsProvider.getComparisonOperators();
	}

	/**
	 * the default operator, if none is set explicitly.
	 */
	public Operators getDefaultOperator() {
		return operatorsProvider.getDefaultOperator();
	}

	FilterComparator getFilterComparator() {
		return _comparator;
	}

	@Override
	protected void clearConfiguration() {
		super.clearConfiguration();
		setPrimaryFilterPattern(null);
		setSecondaryFilterPattern(null);
		setOperator(getDefaultOperator());
	}

	/**
	 * The possible operators for comparison
	 */
	public enum Operators{
		
		LESS_THAN,
		LESS_OR_EQUALS,
		EQUALS,
		NOT_EQUAL,
		GREATER_OR_EQUALS,
		GREATER_THAN,
		BETWEEN;
	}
}
