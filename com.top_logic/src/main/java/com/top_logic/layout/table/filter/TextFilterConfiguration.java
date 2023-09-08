/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.List;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.table.TableViewModel;

/**
 * A configuration used by {@link TextFilter}s.
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public class TextFilterConfiguration extends SelectionFilterConfiguration {
	
	private String filterPattern;
	private boolean wholeField;
	private boolean caseSensitive;
	private boolean regExp;
	private LabelProvider _labelProvider;
	
	
	/** Create a new empty {@link TextFilterConfiguration} */
	public TextFilterConfiguration(TableViewModel tableModel, String columnName, LabelProvider labelProvider,
			boolean hasCollectionValues, boolean showOptionEntries) {
		super(tableModel, columnName, IdentityTransformer.INSTANCE, hasCollectionValues, showOptionEntries);
		assert labelProvider != null : "Filter labelProvider must never be null!";
		_labelProvider = labelProvider;
		clearConfiguration();
	}
	
	/** 
	 *{@inheritDoc}
	 */
	@Override
	public List<Object> getSerializedState() {
		return VersionBasedTextFilterSerialization.INSTANCE.serialize(this);
	}

	/** 
	 *{@inheritDoc}
	 */
	@Override
	public void setSerializedState(List<Object> state) {
		VersionBasedTextFilterSerialization.INSTANCE.deserialize(this, state);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public FilterConfigurationType getConfigurationType() {
		return new FilterConfigurationType(this.getClass(), null);
	}
	
	/**
	 * text to filter for.
	 */
	public String getTextPattern() {
		return filterPattern;
	}

	/**
	 * @see #getTextPattern()
	 */
	public void setTextPattern(String filterPattern) {
		this.filterPattern = filterPattern;
		notifyValueChanged();
	}
	
	/**
	 * true, if the filter pattern must match the whole field, false otherwise.
	 */
	public boolean isWholeField() {
		return wholeField;
	}

	/**
	 * @param    wholeField - set, whether the filter pattern must match the whole field, or not
	 */
	public void setWholeField(boolean wholeField) {
		this.wholeField = wholeField;
		notifyValueChanged();
	}

	/**
	 * true, if the filter pattern must have the same case order like the filtered string, false otherwise.
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	/**
	 * @param    caseSensitive - set, whether the filter pattern must have the same case order like the filtered string, or not.
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		notifyValueChanged();
	}

	/**
	 * true, if the filter pattern is a regular expression, false otherwise.
	 */
	public boolean isRegExp() {
		return regExp;
	}

	/**
	 * @param regExp
	 *        - set, whether the filter pattern is a regular expression, or not.
	 */
	public void setRegExp(boolean regExp) {
		this.regExp = regExp;
		notifyValueChanged();
	}

	void setOptionLabelProvider(LabelProvider labelProvider) {
		_labelProvider = labelProvider;
	}

	@Override
	public LabelProvider getOptionLabelProvider() {
		return _labelProvider;
	}

	@Override
	boolean isUseRawOptions() {
		return false;
	}

	@Override
	protected void clearConfiguration() {
		super.clearConfiguration();
		setTextPattern("");
		setCaseSensitive(false);
		setWholeField(false);
		setRegExp(false);
	}
}