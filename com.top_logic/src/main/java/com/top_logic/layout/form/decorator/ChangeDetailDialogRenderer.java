/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.io.IOException;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.control.AbstractButtonControl;
import com.top_logic.layout.form.control.AbstractButtonRenderer;
import com.top_logic.layout.form.decorator.DetailDecorator.Context;

/**
 * {@link Renderer} of button, to open detail dialog of comparison change.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ChangeDetailDialogRenderer
		extends AbstractButtonRenderer<AbstractButtonRenderer.Config<ChangeDetailDialogRenderer>> {
	
	private final Object _value;
	private DetailDecorator _decorator;
	private Context _context;

	/**
	 * Create a new {@link ChangeDetailDialogRenderer}.
	 */
	public ChangeDetailDialogRenderer(DetailDecorator decorator, Context context, Object value) {
		super(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, newConfig());
		_decorator = decorator;
		_context = context;
		_value = value;
	}

	@SuppressWarnings("unchecked")
	private static AbstractButtonRenderer.Config<ChangeDetailDialogRenderer> newConfig() {
		@SuppressWarnings("rawtypes")
		AbstractButtonRenderer.Config config = TypedConfiguration.newConfigItem(AbstractButtonRenderer.Config.class);
		config.setImplementationClass(ChangeDetailDialogRenderer.class);
		return config;
	}

	@Override
	protected void writeButton(DisplayContext context, TagWriter out, AbstractButtonControl<?> button)
			throws IOException {
		CompareInfo compareInfo = (CompareInfo) _value;
		out.beginBeginTag(SPAN);
		if (!button.isDisabled()) {
			writeOnClickAttribute(context, out, button);
		}
		out.endBeginTag();
		_decorator.start(context, out, compareInfo, _context);
		_decorator.end(context, out, compareInfo, _context);
		out.endTag(SPAN);
	}

	@Override
	protected String getTypeCssClass(AbstractButtonControl<?> button) {
		return null;
	}
}