/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.RangeReplacement;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.ConfigurableControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.tree.TreeUpdateAccumulator.NodeUpdate;
import com.top_logic.layout.tree.TreeUpdateAccumulator.SubtreeUpdate;
import com.top_logic.layout.tree.TreeUpdateAccumulator.UpdateVisitor;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * {@link ControlRenderer} for a {@link BreadcrumbControl}.
 * 
 * <p>
 * A {@link BreadcrumbContentRenderer} is used as inner renderer for the nodes of the
 * {@link BreadcrumbData}. Updates in response to changes of inner nodes are created with this
 * content renderer.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultBreadcrumbRenderer
		extends ConfigurableControlRenderer<BreadcrumbControl, DefaultBreadcrumbRenderer.Config>
		implements BreadcrumbRenderer, UpdateVisitor<Void, UpdateQueue, BreadcrumbControl> {

	/**
	 * Configuration interface to set {@link DefaultBreadcrumbRenderer} via configuration
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static interface Config extends ConfigurableControlRenderer.Config<DefaultBreadcrumbRenderer> {

		/** @see #getContentRenderer() */
		String CONTENT_RENDERER = "contentRenderer";

		/** The configuration for the displaying {@link BreadcrumbContentRenderer}. */
		/* The order of the annotations is strange, but prevents a NullPointerException in the
		 * Eclipse compiler itself. It happens in Eclipse version before 4.11 (2019.03). */
		@ImplementationClassDefault(BreadcrumbContentRenderer.class)
		@ItemDefault
		@Name(CONTENT_RENDERER)
		BreadcrumbContentRenderer.Config getContentRenderer();

	}

	private static final Void none = null;

	private static final String BREADCRUMB_CONTROL_CSS = "breadcrumb";

	private BreadcrumbContentRenderer contentRenderer;

	public DefaultBreadcrumbRenderer(InstantiationContext context, Config config) {
		super(context, config);
		contentRenderer = context.getInstance(config.getContentRenderer());
	}

	/**
	 * the renderer which actually writes the content.
	 */
	protected final BreadcrumbContentRenderer getContentRenderer() {
		return contentRenderer;
	}

	/**
	 * Setter for {@link #getContentRenderer()}.
	 * <p>
	 * Don't use this setter, as this change is not reflected in the configuration, which can cause
	 * problems. Use the normal way instead: Create a configuration with the correct content
	 * renderer.
	 * </p>
	 */
	@FrameworkInternal
	public final void setBreadcrumbContentRenderer(BreadcrumbContentRenderer contentRenderer) {
		this.contentRenderer = contentRenderer;
	}

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, BreadcrumbControl control)
			throws IOException {

		// Use same tag as for the control itself - allows rendering as div and span..
		String controlTag = getControlTag(control);

		out.beginBeginTag(controlTag);
		out.writeAttribute(CLASS_ATTR, BREADCRUMB_CONTROL_CSS);
		out.endBeginTag();
		{
			BreadcrumbControl breadcrumb = control;
			final TLTreeModel tree = breadcrumb.getTree();
			final Object lastNode = breadcrumb.getLastNode();
			final List<?> currentPath = tree.createPathToRoot(lastNode);
			writePath(context, out, breadcrumb, currentPath);
		}
		out.endTag(controlTag);
	}

	/**
	 * Writes the given path from the end to the beginning
	 */
	protected void writePath(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb, List<?> path) throws IOException {
		for (int index = path.size() - 1; index >= 0; index--) {
			writeNode(context, out, breadcrumb, path.get(index));
		}
	}

	/**
	 * Writes the given node, i.e. it writes the tag for the node and the id.
	 * the other things are written by the {@link #getContentRenderer() content
	 * renderer}.
	 */
	protected void writeNode(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb, final Object currentNode) throws IOException {
		final String nodeTag = getContentRenderer().getNodeTag(breadcrumb, currentNode);
		out.beginBeginTag(nodeTag);
		out.writeAttribute(ID_ATTR, breadcrumb.getNodeId(currentNode));
		getContentRenderer().writeNodeTagAttributes(context, out, breadcrumb, currentNode);
		out.endBeginTag();
		{
			getContentRenderer().writeNode(context, out, breadcrumb, currentNode);
		}
		out.endTag(nodeTag);
	}

	@Override
	public Void visitNodeUpdate(final NodeUpdate update, UpdateQueue queue, final BreadcrumbControl breadcrumb) {
		queue.add(new ElementReplacement(update.getNodeId(), new HTMLFragment() {

			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				writeNode(context, out, breadcrumb, update.getNode());
			}
		}));
		return none;
	}

	@Override
	public Void visitSubtreeUpdate(SubtreeUpdate update, UpdateQueue queue, final BreadcrumbControl breadcrumb) {
		final TLTreeModel tree = breadcrumb.getTree();
		final Object currentNode = update.getNode();

		queue.add(new RangeReplacement(update.getNodeId(), update.getRightmostVisibleDescendantId(), new HTMLFragment() {

			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				Object node = breadcrumb.getLastNode();
				List<Object> pathToWrite = new ArrayList<>();
				while (node != null) {
					pathToWrite.add(node);
					if (node == currentNode) {
						break;
					}
					node = tree.getParent(node);
				}
				writePath(context, out, breadcrumb, pathToWrite);
			}
		}));
		return none;
	}

	@Override
	public UpdateVisitor<?, UpdateQueue, BreadcrumbControl> getUpdater() {
		return this;
	}

	/**
	 * Creates a default instance {@link DefaultBreadcrumbRenderer}.
	 */
	public static DefaultBreadcrumbRenderer defaultRenderer() {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(TypedConfiguration.newConfigItem(Config.class));
	}

}
