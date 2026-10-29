/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import static com.top_logic.layout.form.FormConstants.*;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.ConfigurableControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.dnd.TableDropTarget;
import com.top_logic.layout.table.dnd.TableDropTarget.DropType;
import com.top_logic.mig.html.HTMLUtil;

/**
 * Common super class of all {@link TableRenderer} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTableRenderer<C extends AbstractTableRenderer.Config<?>>
		extends ConfigurableControlRenderer<TableControl, C> implements TableRenderer<C> {

	/**
	 * Configuration of the {@link AbstractTableRenderer}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends AbstractTableRenderer<?>>
			extends ConfigurableControlRenderer.Config<I>, TableRenderer.Config<I> {
		// sum interface
	}

	/**
	 * Create a {@link AbstractTableRenderer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public AbstractTableRenderer(InstantiationContext context, C config) {
		super(context, config);
	}

	// Make visible to TableRendererProxy
	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, TableControl control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);

		TableControl table = control;
		TableData data = table.getTableData();
		if (data.getDragSource().dragEnabled(data)) {
			out.beginAttribute(ONDRAGSTART_ATTR);
			out.append("return ").append(TABLE_HANDLER_CLASS).append(".handleOnDragStart(event, this);");
			out.endAttribute();
			out.beginAttribute(ONDRAGEND_ATTR);
			out.append("return ").append(TABLE_HANDLER_CLASS).append(".handleOnDragEnd(event, this);");
			out.endAttribute();
		}

		Set<DropType> dropTypes = getEnabledDropTypes(data.getDropTargets(), data);

		if (!dropTypes.isEmpty()) {
			out.writeAttribute(TableDropTarget.TL_DROPTYPE_ATTR, toAttributeValue(dropTypes));
			writeOnDropAttributes(out, data);
		}
	}

	private Set<DropType> getEnabledDropTypes(List<TableDropTarget> dropTargets, TableData data) {
		Set<DropType> result = EnumSet.noneOf(DropType.class);
		for (TableDropTarget dropTarget : dropTargets) {
			if (dropTarget.dropEnabled(data)) {
				result.add(dropTarget.getDropType());
			}
		}
		return result;
	}

	private static String toAttributeValue(Set<DropType> dropTypes) {
		return dropTypes.stream().map(DropType::name).collect(Collectors.joining(" "));
	}

	@Override
	public void appendControlCSSClasses(Appendable out, TableControl control) throws IOException {
		super.appendControlCSSClasses(out, control);
		
		HTMLUtil.appendCSSClass(out, "h-100");
	}

	/**
	 * Adds the drop handlers assuming the table can serve as drop target.
	 * 
	 * @param data
	 *        The context table.
	 */
	protected void writeOnDropAttributes(TagWriter out, TableData data) throws IOException {
		out.beginAttribute(ONDROP_ATTR);
		out.append("return ").append(TABLE_HANDLER_CLASS).append(".handleOnDrop(event, this);");
		out.endAttribute();
		out.beginAttribute(ONDRAGOVER_ATTR);
		out.append("return ").append(TABLE_HANDLER_CLASS).append(".handleOnDragOver(event, this);");
		out.endAttribute();
		out.beginAttribute(ONDRAGENTER_ATTR);
		out.append("return ").append(TABLE_HANDLER_CLASS).append(".handleOnDragEnter(event, this);");
		out.endAttribute();
		out.beginAttribute(ONDRAGLEAVE_ATTR);
		out.append("return ").append(TABLE_HANDLER_CLASS).append(".handleOnDragLeave(event, this);");
		out.endAttribute();
	}

	@Override
	public final TableRenderer<?> cloneRenderer() {
		AbstractTableRenderer.Config<?> config = getConfig();
		return TypedConfigUtil.createInstance(config);
	}

}
