/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.chart.chartjs.control;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.DefaultValueAnalyzer;
import com.top_logic.basic.json.JSON.ValueAnalyzer;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link Control} displaying a <a href="https://www.chartjs.org/">chart.js</a> chart.
 * 
 * @see "https://www.chartjs.org/"
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ChartJsControl extends AbstractVisibleControl {

	private static final ValueAnalyzer JSON_VALUES = new DefaultValueAnalyzer() {

		@Override
		public int getType(Object value) {
			int result = super.getType(value);
			if (result == UNSUPPORTED_TYPE) {
				return STRING_TYPE;
			}
			return result;
		}

		@Override
		public String getString(Object object) {
			if (object instanceof CharSequence) {
				return object.toString();
			}
			return MetaResourceProvider.INSTANCE.getLabel(object);
		}

	};

	/**
	 * The last version of the model that was installed in the UI. When changing the model, only
	 * differences are transported to the UI. This is required for chart.js to perform animations
	 * from the original state to the new state.
	 */
	private Object _lastModel;

	private Object _model;

	private boolean _updateRequested;

	private String _cssClass;

	private String _style;

	/**
	 * Creates a {@link ChartJsControl}.
	 * 
	 * @param model
	 *        See {@link #getModel()}.
	 */
	public ChartJsControl(Object model) {
		_model = model;
	}

	/**
	 * The chart.js configuration model. This object is converted to JSON and transported to the
	 * client.
	 * 
	 * <p>
	 * Note: The configuration model must not be internally modified after it has been displayed.
	 * Instead, a new version of the model must be created and displayed by calling
	 * {@link #setModel(Object)}. Otherwise no incremental updates can be performed.
	 * </p>
	 */
	@Override
	public Object getModel() {
		return _model;
	}

	/**
	 * Updates the chart.js configuration model.
	 * 
	 * @see #getModel()
	 */
	public void setModel(Object model) {
		_model = model;
		updateChart();
	}

	/**
	 * The style of the control element.
	 */
	public String getStyle() {
		return _style;
	}

	/**
	 * @see #getStyle()
	 */
	public void setStyle(String style) {
		_style = style;
		requestRepaint();
	}

	/**
	 * The CSS class of the control element.
	 */
	public String getCssClass() {
		return _cssClass;
	}

	/**
	 * @see #getCssClass()
	 */
	public void setCssClass(String cssClass) {
		_cssClass = cssClass;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(HTMLConstants.ID_ATTR, getID());
		out.writeAttribute(HTMLConstants.CLASS_ATTR, _cssClass);
		out.writeAttribute(HTMLConstants.STYLE_ATTR, _style);
		out.endBeginTag();
		{
			_lastModel = _model;
			if (isValid(_model)) {
				out.beginBeginTag("canvas");
				String canvasId = canvasId();
				out.writeAttribute(HTMLConstants.ID_ATTR, canvasId);
				out.endBeginTag();
				out.endTag("canvas");

				out.beginScript();
				out.append("var chartElement = document.getElementById(");
				out.writeJsString(canvasId);
				out.append(");");
				out.append("var chartHandler = new Chart(chartElement, ");
				writeJSON(out, _model);
				out.append(");");
				out.append("chartElement.chartHandler = chartHandler;");
				out.endScript();
			} else {
				out.beginBeginTag(PRE);
				out.endBeginTag();
				writeJSON(out, _model);
				out.endTag(PRE);
			}

		}
		out.endTag(DIV);
	}

	private void writeJSON(TagWriter out, Object data) throws IOException {
		JSON.write(out, JSON_VALUES, data);
	}

	private String canvasId() {
		return getID() + "-canvas";
	}

	/**
	 * Requests updating the chart data.
	 */
	private void updateChart() {
		_updateRequested = true;
	}

	@Override
	protected boolean hasUpdates() {
		return _updateRequested || super.hasUpdates();
	}

	@Override
	protected void internalRevalidate(DisplayContext revalidateContext, UpdateQueue actions) {
		if (_updateRequested) {
			actions.add(new JSSnipplet((renderContext, out) -> {
				Object oldData = _lastModel;
				Object newData = _model;
				if (isValid(newData)) {
					out.append("var chartElement = document.getElementById('");
					out.append(canvasId());
					out.append("');");
					out.append("var chartHandler = chartElement.chartHandler;");
					out.append("if (chartHandler != null) {");
					writeUpdate(out, oldData, newData, "chartHandler");
					out.append(";");
					out.append("chartHandler.update();");
					out.append("}");

					_lastModel = newData;
				}
			}));
			_updateRequested = false;
		}
		super.internalRevalidate(revalidateContext, actions);
	}

	private void writeUpdate(Appendable out, Object oldData, Object newData, String path) throws IOException {
		if (oldData instanceof Map<?, ?>) {
			if (newData instanceof Map<?, ?>) {
				Map<?, ?> oldMap = (Map<?, ?>) oldData;
				Map<?, ?> newMap = (Map<?, ?>) newData;

				for (Object key : newMap.keySet()) {
					if (oldMap.containsKey(key)) {
						writeUpdate(out, oldMap.get(key), newMap.get(key), path + "." + key);
					} else {
						writeSetProperty(out, path + "." + key, newMap.get(key));
					}
				}

				for (Object key : oldMap.keySet()) {
					if (!newMap.containsKey(key)) {
						out.append("delete ");
						out.append(path + "." + key);
						out.append(";");
					}
				}

				return;
			}
		} else if (oldData instanceof List<?>) {
			if (newData instanceof List<?>) {
				List<?> oldList = (List<?>) oldData;
				List<?> newList = (List<?>) newData;
				if (oldList.size() == newList.size()) {
					for (int n = 0, cnt = oldList.size(); n < cnt; n++) {
						writeUpdate(out, oldList.get(n), newList.get(n), path + "[" + n + "]");
					}
					return;
				}
			}
		}
		writeSetProperty(out, path, newData);
	}

	private void writeSetProperty(Appendable out, String path, Object newData) throws IOException {
		out.append(path);
		out.append(" = ");
		out.append(JSON.toString(JSON_VALUES, newData));
		out.append(";");
	}

	/**
	 * Whether the given object is valid ChartJS configuration data.
	 */
	private boolean isValid(Object config) {
		if (config instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) config;

			Object type = map.get("type");
			if (!(type instanceof CharSequence)) {
				return false;
			}

			Object data = map.get("data");
			if (!(data instanceof Map)) {
				return false;
			}

			return true;
		}
		return false;
	}

}
