/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;

/**
 * {@link AbstractControlBase}, that displays and handles a view for a {@link ConfiguredFilter}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class FilterViewControl<T extends FilterModel> extends AbstractControlBase {

	/** Marker of {@link FormContext}, that a programmatic update will be applied */
	public static final Property<Boolean> PROGRAMMATIC_UPDATE = TypedAnnotatable.property(Boolean.class, "programmaticUpdate");

	private T filterModel;

	private FormGroup filterGroup;

	/**
	 * Create a new {@link FilterViewControl}
	 */
	public FilterViewControl(T filterModel, FormGroup formGroup) {
		this.filterModel = filterModel;
		this.filterGroup = formGroup;
	}

	@Override
	public T getModel() {
		return filterModel;
	}

	/**
	 * Stores filter settings of GUI into {@link FilterConfiguration}
	 * 
	 * @return true, if settings appliance had changed {@link FilterConfiguration}, false otherwise.
	 */
	@SuppressWarnings("unchecked")
	public final boolean applyFilterSettings() {
		filterModel.removeValueListener((FilterViewControl<FilterModel>) this);
		boolean changed = internalApplyFilterSettings();
		filterModel.addValueListener((FilterViewControl<FilterModel>) this);
		return changed;
	}

	/**
	 * Called after value updates has been disabled.
	 * 
	 * @return true, if filter settings have been changed, false otherwise.
	 */
	protected abstract boolean internalApplyFilterSettings();

	/**
	 * Reset filter settings of GUI to empty state.
	 */
	public abstract void resetFilterSettings();

	/**
	 * Called from filter configuration, when this {@link FilterViewControl} is it's registered
	 * value listener.
	 */
	public final void valueChanged() {
		getFilterGroup().getFormContext().set(PROGRAMMATIC_UPDATE, true);
		internalValueChanged();
		getFilterGroup().getFormContext().reset(PROGRAMMATIC_UPDATE);
	}

	/**
	 * Called after {@link FormContext} of {@link #getFilterGroup()} is marked as programmatic
	 * update
	 */
	protected abstract void internalValueChanged();


	@Override
	protected boolean hasUpdates() {
		return false;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// Nothing to revalidate
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void internalAttach() {
		super.internalAttach();
		filterModel.addValueListener((FilterViewControl<FilterModel>) this);
		valueChanged();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void internalDetach() {
		super.internalDetach();
		filterModel.removeValueListener((FilterViewControl<FilterModel>) this);
	}

	/**
	 * {@link FilterModel} of this {@link FilterViewControl}.
	 */
	protected final T getFilterModel() {
		return filterModel;
	}

	/**
	 * {@link FormGroup} of this {@link FilterViewControl}
	 */
	protected final FormGroup getFilterGroup() {
		return filterGroup;
	}
}
