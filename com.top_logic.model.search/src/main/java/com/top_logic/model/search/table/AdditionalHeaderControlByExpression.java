/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.table;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.table.model.AdditionalHeaderControl;
import com.top_logic.layout.table.model.AdditionalHeaderControlModel;
import com.top_logic.layout.table.model.SimpleAdditionalHeaderControl;
import com.top_logic.mig.html.HTMLUtil;
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
			Map<String, ControlCommand> map) {
		super(model, map);
		_label = labelScript;
		_tooltip = tooltipScript;
		_resKey = resKey;
		_cssClass = cssClass;
		_useRowObjects = useRowObjects;
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
		return script.execute(getValues());
	}

	private List<?> getValues() {
		if (getUseRowObjects()) {
			return getModel().getDisplayedRows();
		}
		return getModel().getValues();
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
