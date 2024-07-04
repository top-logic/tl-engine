/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure.embedd;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.structure.AbstractLayoutControl;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link HTMLFragment} displaying an <code>iframe</code> element for embedding a view of another
 * application.
 */
public class EmbeddedView extends AbstractLayoutControl<EmbeddedView> implements ControlRenderer<EmbeddedView> {

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(EmbeddedView.OnClose.INSTANCE);

	private LayoutComponent _component;

	private URLProvider _urlProvider;

	private UICallback _onClose;

	/**
	 * Creates a {@link EmbeddedView}.
	 */
	public EmbeddedView(LayoutComponent component, URLProvider urlProvider, UICallback onClose) {
		super(COMMANDS);
		_component = component;
		_urlProvider = urlProvider;
		_onClose = onClose;
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);

		out.append("tlEmbedd");
	}
	
	@Override
	public void write(DisplayContext context, TagWriter out, EmbeddedView value) throws IOException {
		out.beginBeginTag(IFRAME);
		writeControlAttributes(context, out);
		out.writeAttribute(TITLE, context.getResources().getString(_component.getTitleKey()));
		out.writeAttribute(SRC_ATTR, _urlProvider.getUrl(context, _component, _component.getModel()));
		out.endBeginTag();
		out.endTag(IFRAME);
	}

	private HandlerResult onClose(DisplayContext commandContext, Map<String, Object> arguments) {
		return _onClose.executeCommand(commandContext, arguments);
	}

	@Override
	public List<? extends LayoutControl> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public Object getModel() {
		return _component;
	}

	@Override
	protected ControlRenderer<? super EmbeddedView> createDefaultRenderer() {
		return this;
	}

	@Override
	public EmbeddedView self() {
		return this;
	}

	/**
	 * Command invoked when an embedded view requests to close.
	 */
	public static class OnClose extends ControlCommand {

		/**
		 * Command ID.
		 */
		public static final String ON_CLOSE = "onClose";

		/**
		 * Singleton {@link EmbeddedView.OnClose} instance.
		 */
		public static final EmbeddedView.OnClose INSTANCE = new EmbeddedView.OnClose();

		/**
		 * Creates a {@link OnClose}.
		 */
		private OnClose() {
			super(ON_CLOSE);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.EMBEDD_ON_CLOSE;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			return ((EmbeddedView) control).onClose(commandContext, arguments);
		}

	}
}
