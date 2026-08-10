/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.table.model.AdditionalHeaderControl;
import com.top_logic.layout.table.model.AdditionalHeaderControlModel;
import com.top_logic.layout.table.model.SimpleAdditionalHeaderControl;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeTableModel;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * An {@link AdditionalHeaderControl} that writes a configurable TL-Script expression.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class AdditionalHeaderControlByExpression extends SimpleAdditionalHeaderControl {

	private final QueryExecutor _label;

	private final QueryExecutor _tooltip;

	private final ResKey _resKey;

	private final String _cssClass;

	private final boolean _useRowObjects;

	private final LayoutComponent _component;

	/**
	 * Creates an {@link AdditionalHeaderControlByExpression}.
	 * <p>
	 * See
	 * {@link SimpleAdditionalHeaderControl#SimpleAdditionalHeaderControl(AdditionalHeaderControlModel, Map)}
	 * for the "inherited" parameters.
	 * </p>
	 * <p>
	 * See {@link SimpleAdditionalHeaderControl#composeStaticWithDynamicPart(ResKey, Object)} for
	 * the interaction of the script and key.
	 * </p>
	 * 
	 * @param labelScript
	 *        If null, only the {@link ResKey} for the label is written.
	 * @param tooltipScript
	 *        If null, only the {@link ResKey} for the tooltip content is written.
	 * @param resKey
	 *        The {@link ResKey} for the label. The tooltip {@link ResKey} is derived from it with
	 *        the ".tooltip" suffix. If null, only the script is written.
	 * @param cssClass
	 *        See: {@link #getCssClass()}
	 * @param useRowObjects
	 *        See: {@link #getUseRowObjects()}
	 */
	public AdditionalHeaderControlByExpression(AdditionalHeaderControlModel model, QueryExecutor labelScript,
			QueryExecutor tooltipScript, ResKey resKey, String cssClass, boolean useRowObjects,
			LayoutComponent component,
			Map<String, ControlCommand> map) {
		super(model, map);
		_label = labelScript;
		_tooltip = tooltipScript;
		_resKey = resKey;
		_cssClass = cssClass;
		_useRowObjects = useRowObjects;
		_component = component;
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		HTMLUtil.appendCSSClass(out, getCssClass());
	}

	@Override
	protected Object getDynamicLabelPart() {
		return executeScript(_label);
	}

	@Override
	protected ResKey getStaticLabelPart() {
		return _resKey;
	}

	@Override
	protected Object getDynamicTooltipPart() {
		return executeScript(_tooltip);
	}

	@Override
	protected ResKey getStaticTooltipPart() {
		return _resKey == null ? null : _resKey.tooltip();
	}

	private Object executeScript(QueryExecutor script) {
		if (script == null) {
			return "";
		}
		if (_component == null) {
			return script.execute(getValues());
		}
		return script.execute(getValues(), _component.getModel());
	}

	private List<?> getValues() {
		if (getUseRowObjects()) {
			return getRowObjects();
		}
		return getModel().getValues();
	}

	/**
	 * The business objects of the {@link AdditionalHeaderControlModel#getDisplayedRows() displayed
	 * rows}.
	 * <p>
	 * The rows of a table are not necessarily the objects that are displayed in them: A tree table
	 * wraps them into nodes and a {@link GridComponent} wraps them into {@link FormGroup}s. Such
	 * technical objects cannot be processed in TL-Script and are therefore unwrapped here.
	 * </p>
	 *
	 * @return Never null. The {@link AdditionalHeaderControlModel#getDisplayedRows() rows}
	 *         themselves, if there is nothing to unwrap.
	 */
	private List<?> getRowObjects() {
		List<?> rows = getModel().getDisplayedRows();
		Function<Object, Object> unwrapping = rowUnwrapping();
		if (unwrapping == null) {
			return rows;
		}
		List<Object> businessObjects = new ArrayList<>(rows.size());
		for (Object row : rows) {
			businessObjects.add(unwrapping.apply(row));
		}
		return businessObjects;
	}

	/**
	 * The mapping from the technical row objects to the business objects displayed in them.
	 * <p>
	 * Whether the rows are technical objects is a property of the table, not of the single row: In
	 * a plain table, a {@link TLTreeNode} is a business object like any other and must not be
	 * unwrapped.
	 * </p>
	 *
	 * @return Null, if the rows are the business objects themselves.
	 */
	private Function<Object, Object> rowUnwrapping() {
		if (_component instanceof GridComponent grid) {
			return grid::getBusinessObjectFromInternalRow;
		}
		if (getModel().getTableViewModel().getApplicationModel() instanceof TreeTableModel) {
			return TLTreeModelUtil::getInnerBusinessObject;
		}
		return null;
	}

	/** @see AdditionalHeaderControlProviderByExpression.Config#getUseRowObjects() */
	protected boolean getUseRowObjects() {
		return _useRowObjects;
	}

	/** @see AdditionalHeaderControlProviderByExpression.Config#getCssClass() */
	protected String getCssClass() {
		return _cssClass;
	}

}
