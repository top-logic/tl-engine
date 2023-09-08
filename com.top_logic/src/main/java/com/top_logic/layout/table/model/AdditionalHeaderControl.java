/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.Objects.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.basic.ControlCommand;

/**
 * A {@link Control} displaying one cell of an {@link ColumnBaseConfig#getAdditionalHeaders()
 * additional header}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AdditionalHeaderControl extends AbstractVisibleControl {

	/** The CSS class for all additional headers. */
	public static final String TYPE_CSS_CLASS = "tl_table_additional_header";

	private final AdditionalHeaderControlModel _model;

	/**
	 * The listener needs to be cached to be able to remove it.
	 * <p>
	 * Creating a method reference multiple times (once when it is registered and once when it is
	 * deregistered) can result in listeners that are not equal, as method references to the same
	 * method don' have to be equal. That can make it impossible to remove the listener, when the
	 * listener is a method reference that cannot be found as a new method reference was created for
	 * the deregistration call.
	 * </p>
	 */
	private final TableModelListener _listener = this::onTableModelEvent;

	private final Set<Integer> _relevantEventTypes = set(
		TableModelEvent.INSERT,
		TableModelEvent.UPDATE,
		TableModelEvent.DELETE,
		TableModelEvent.INVALIDATE);

	/**
	 * Creates an {@link AdditionalHeaderControl} with the given {@link ControlCommand}s.
	 * 
	 * @param model
	 *        Is not allowed to be null.
	 * @param commandsByName
	 *        Null is equivalent to the empty {@link Map}, and therefore to
	 *        {@link #AdditionalHeaderControl the shorter constructor}.
	 */
	public AdditionalHeaderControl(AdditionalHeaderControlModel model, Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
		_model = requireNonNull(model);
	}

	@Override
	public AdditionalHeaderControlModel getModel() {
		return _model;
	}

	/**
	 * Don't override this method. Override {@link #internalWriteContent(DisplayContext, TagWriter)}
	 * instead.
	 * <p>
	 * This method writes the surrounding tag with the necessary attributes.
	 * </p>
	 * 
	 * @see com.top_logic.layout.basic.AbstractControlBase#internalWrite(com.top_logic.layout.DisplayContext,
	 *      com.top_logic.basic.xml.TagWriter)
	 */
	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(getControlTag());
		writeControlAttributes(context, out);
		out.endBeginTag();
		internalWriteContent(context, out);
		out.endTag(getControlTag());
	}

	/**
	 * Hook for subclasses that defines the tag written in
	 * {@link #internalWrite(DisplayContext, TagWriter)}.
	 */
	protected String getControlTag() {
		return DIV;
	}

	/**
	 * Writes the content of this control's HTML tag.
	 * <p>
	 * The content is enclosed by an XML tag ({@link #getControlTag()}) that has the necessary
	 * attributes (control attributes and style attributes).
	 * </p>
	 * 
	 * @param context
	 *        Never null.
	 * @param out
	 *        Never null.
	 */
	protected abstract void internalWriteContent(DisplayContext context, TagWriter out);

	/**
	 * Don't override this method. Override {@link #writeControlClassesContent(Appendable)} to set
	 * your own CSS classes, but don't forget to call super.
	 * <p>
	 * This method returns the CSS class that all additional headers have. It's purpose is to
	 * customize the style of the additional table headers in the entire application.
	 * </p>
	 */
	@Override
	protected String getTypeCssClass() {
		return TYPE_CSS_CLASS;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		getModel().getTableViewModel().addTableModelListener(_listener);
	}

	@Override
	protected void internalDetach() {
		getModel().getTableViewModel().removeTableModelListener(_listener);
		super.internalDetach();
	}

	private void onTableModelEvent(TableModelEvent event) {
		if (_relevantEventTypes.contains(event.getType())) {
			requestRepaint();
		}
	}

}
