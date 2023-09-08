/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.gui;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.general.Dataset;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.layout.basic.ObservableBase;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.reporting.flex.chart.config.aggregation.AggregationFunction;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.model.DefaultModelPreparation;
import com.top_logic.reporting.flex.chart.config.partition.PartitionFunction;
import com.top_logic.util.Utils;

/**
 * Context used for {@link InteractiveChartBuilder} to react on model changes and update the view.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class ChartContextObserver extends ObservableBase implements TypedAnnotatable {

	/**
	 * A {@link DimensionListener} is informed if the number of partitions or aggregations changes.
	 */
	public interface DimensionListener {

		/**
		 * Called to inform about a change of the number of partitions or aggregations in the
		 * {@link DefaultModelPreparation}.
		 * 
		 * <p>
		 * Both informations are always given, even if only one item has changed.
		 * </p>
		 * 
		 * @param partitions
		 *        the new number of partitions
		 * @param aggregations
		 *        the new number of aggregations
		 */
		public void onDimensionChange(List<PartitionFunction.Config> partitions, List<AggregationFunction.Config> aggregations);

	}

	/**
	 * A {@link ModelListener} is informed if the component-model changes.
	 */
	public interface ModelListener {

		/**
		 * Called to inform about a change of the component model.
		 * 
		 * @param oldModel
		 *        the model before the change
		 * @param newModel
		 *        the new model
		 */
		public void onModelChange(Object oldModel, Object newModel);

	}

	/**
	 * Listener that is informed when the {@link ChartTree} and {@link Dataset} have been created.
	 */
	public interface ChartListener {

		/**
		 * Called when a {@link ChartTree} and {@link Dataset} for this {@link ChartContext} have
		 * been calculated.
		 */
		public void onChartDataCreated(Dataset dataSet, ChartTree chartTree);

	}

	private static final Property<ChartContextObserver> OBSERVER =
		TypedAnnotatable.property(ChartContextObserver.class, "observer");

	private final FormContext _context;

	private Object _model;

	private List<PartitionFunction.Config> _partitions;

	private List<AggregationFunction.Config> _aggregations;

	/**
	 * Creates a new {@link ChartContextObserver}, initializes it with the given model and annotate
	 * it to the given form-context.
	 * 
	 * @param model
	 *        the initial model, may be null
	 * @param context
	 *        the context to annotate this observer to, must not be null
	 */
	public ChartContextObserver(Object model, FormContext context) {
		_model = model;
		_partitions = new ArrayList<>();
		_aggregations = new ArrayList<>();
		_context = context;
		context.set(OBSERVER, this);
	}

	/**
	 * Initializes the partitions and aggregations without calling any listeners.
	 */
	public void initDimensions(List<PartitionFunction.Config> partitions, List<AggregationFunction.Config> aggregations) {
		_partitions = partitions;
		_aggregations = aggregations;
	}

	/**
	 * the currently set partitions
	 */
	public List<PartitionFunction.Config> getPartitions() {
		return _partitions;
	}

	/**
	 * the currently set aggregations
	 */
	public List<AggregationFunction.Config> getAggregations() {
		return _aggregations;
	}

	/**
	 * the currently set model
	 */
	public Object getModel() {
		return _model;
	}

	/**
	 * Updates the number of partitions and informs the registerd {@link DimensionListener}
	 * 
	 * @param partitions
	 *        the new list of partitions
	 */
	public void setPartitions(List<PartitionFunction.Config> partitions) {
		if (Utils.equals(partitions, _partitions)) {
			return;
		}
		_partitions = partitions;
		updateListener();
	}

	/**
	 * Updates the number of aggregations and informs the registerd {@link DimensionListener}
	 * 
	 * @param aggregations
	 *        the new list of aggregations
	 */
	public void setAggregations(List<AggregationFunction.Config> aggregations) {
		if (Utils.equals(aggregations, _aggregations)) {
			return;
		}
		_aggregations = aggregations;
		updateListener();
	}

	private void updateListener() {
		List<DimensionListener> currentListeners = getListeners(DimensionListener.class);
		for (int cnt = currentListeners.size(), n = 0; n < cnt; n++) {
			currentListeners.get(n).onDimensionChange(_partitions, _aggregations);
		}
	}

	/**
	 * Updates the model and informs the registerd {@link ModelListener}
	 * 
	 * @param oldModel
	 *        the model before the change
	 * @param newValue
	 *        the model after the change
	 */
	public void updateModel(Object oldModel, Object newValue) {
		_model = newValue;
		List<ModelListener> currentListeners = getListeners(ModelListener.class);
		for (int cnt = currentListeners.size(), n = 0; n < cnt; n++) {
			currentListeners.get(n).onModelChange(oldModel, newValue);
		}
	}

	/**
	 * Informs the registerd {@link ChartListener} about the resulting {@link Dataset} and
	 * {@link ChartTree}
	 * 
	 * @param dataSet
	 *        the jfreechart-model that has been in this chart-context
	 * @param tree
	 *        the universal chart-model that has been in this chart-context
	 */
	public void updateResult(Dataset dataSet, ChartTree tree) {
		List<ChartListener> currentListeners = getListeners(ChartListener.class);
		for (int cnt = currentListeners.size(), n = 0; n < cnt; n++) {
			currentListeners.get(n).onChartDataCreated(dataSet, tree);
		}
	}

	/**
	 * see {@link ObservableBase#addListener(Class, Object)} for listenerInterface
	 * {@link DimensionListener}
	 */
	public boolean addDimensionListener(DimensionListener listener) {
		return addListener(DimensionListener.class, listener);
	}

	/**
	 * see {@link ObservableBase#addListener(Class, Object)} for listenerInterface
	 * {@link ModelListener}
	 */
	public boolean addModelListener(ModelListener listener) {
		return addListener(ModelListener.class, listener);
	}

	/**
	 * see {@link ObservableBase#addListener(Class, Object)} for listenerInterface
	 * {@link ChartListener}
	 */
	public boolean addChartListener(ChartListener listener) {
		return addListener(ChartListener.class, listener);
	}

	/**
	 * @param component
	 *        the component where the ChartContextObserver is annotated to the {@link FormContext}
	 * @return the observer for the given component, may be null
	 */
	public static ChartContextObserver getObserver(FormComponent component) {
		if (component.hasFormContext()) {
			FormContext context = component.getFormContext();
			return getObserver(context);
		}
		return null;
	}

	/**
	 * @param context
	 *        the context where the ChartContextObserver is annotated to
	 * @return the observer for the given context, may be null
	 */
	public static ChartContextObserver getObserver(FormContext context) {
		return context.get(OBSERVER);
	}

	@Override
	public <T> T set(Property<T> property, T value) {
		return _context.set(property, value);
	}

	@Override
	public <T> T get(Property<T> property) {
		return _context.get(property);
	}

	@Override
	public <T> T reset(Property<T> property) {
		return _context.reset(property);
	}

	@Override
	public boolean isSet(Property<?> property) {
		return _context.isSet(property);
	}
}