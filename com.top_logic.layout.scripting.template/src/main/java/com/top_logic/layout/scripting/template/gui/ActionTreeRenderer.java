/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import java.io.IOException;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.scripting.runtime.execution.ScriptDriver;
import com.top_logic.layout.scripting.util.ActionResourceProvider;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TLTreeNodeResourceProvider;
import com.top_logic.layout.tree.renderer.ConfigurableTreeContentRenderer;
import com.top_logic.layout.tree.renderer.ConfigurableTreeRenderer;
import com.top_logic.layout.tree.renderer.DefaultTreeImageProvider;
import com.top_logic.layout.tree.renderer.DefaultTreeRenderer;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.util.css.CssUtil;

/**
 * The {@link TreeRenderer} for a {@link ScriptRecorderTree}.
 * 
 * <p>
 * Adds a script that is executed after rendering to resume an ongoing scripted test execution.
 * </p>
 * 
 * <p>
 * If no {@link ScriptRecorderTree#SCRIPT_DRIVER} property is set, the added script is a noop.
 * Otherwise the cached {@link ScriptDriver} is used to continue the execution.
 * </p>
 * 
 * @see ResumeScriptExecutionCommand
 * @see ScriptRecorderTreeControl
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class ActionTreeRenderer extends ConfigurableTreeRenderer {

	private static final class ActionTreeNodeResourceProvider extends TLTreeNodeResourceProvider {

		ActionTreeNodeResourceProvider(ResourceProvider userObjectResourceProvider) {
			super(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, toConfig(userObjectResourceProvider));
		}

		private static Config<?> toConfig(ResourceProvider userObjectResourceProvider) {
			@SuppressWarnings("unchecked")
			Config<ActionTreeNodeResourceProvider> config =
				TypedConfiguration.newConfigItem(ActionTreeNodeResourceProvider.Config.class);
			config.setImplementationClass(ActionTreeNodeResourceProvider.class);
			config.setTargetResourceProvider(userObjectResourceProvider);
			return config;
		}

		@Override
		public String getTooltip(Object object) {
			String error = ScriptRecorderTree.getError(node(object));
			if (error != null) {
				return TagUtil.encodeXML(error);
			}
			return super.getTooltip(object);
		}

		private TLTreeNode<?> node(Object object) {
			return (TLTreeNode<?>) object;
		}

		@Override
		public String getCssClass(Object object) {
			if (ScriptRecorderTree.hasError(node(object))) {
				return CssUtil.joinCssClasses("scrError", super.getCssClass(object));
			}
			if (ScriptRecorderTree.isDerived(node(object))) {
				return CssUtil.joinCssClasses("scrDerived", super.getCssClass(object));
			}
			return super.getCssClass(object);
		}
	}

	/**
	 * Creates a {@link ActionTreeRenderer}.
	 * 
	 * <p>
	 * Note: This instance must not be shared among sessions, since it caches localized templates,
	 * see {@link ActionResourceProvider}.
	 * </p>
	 */
	public ActionTreeRenderer() {
		super(DefaultTreeRenderer.CONTROL_TAG, DefaultTreeRenderer.NODE_TAG, createContentRenderer());
	}

	private static ConfigurableTreeContentRenderer createContentRenderer() {
		return new ConfigurableTreeContentRenderer(
			DefaultTreeImageProvider.INSTANCE,
			new ActionTreeNodeResourceProvider(ActionResourceProvider.newInstance()));
	}

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, TreeControl control) throws IOException {
		super.writeControlContents(context, out, control);

		HTMLUtil.writeScriptAfterRendering(out, createResumeScriptExecutionExpression(control));
	}

	private String createResumeScriptExecutionExpression(TreeControl control) {
		return "services.scriptrecorder.resumeScriptExecution('" + control.getID() + "');";
	}

}
