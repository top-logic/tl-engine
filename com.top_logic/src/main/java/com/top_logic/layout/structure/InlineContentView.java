/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.top_logic.base.services.simpleajax.ContentReplacement;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ProcessingInfo;
import com.top_logic.layout.ProcessingKind;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.admin.component.PerformanceMonitor;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.RenderErrorUtil;
import com.top_logic.layout.component.SaveScrollPosition;
import com.top_logic.layout.layoutRenderer.LayoutControlRenderer;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.util.I18NConstants;

/**
 * {@link Control} that embeds any legacy {@link LayoutComponent} through its
 * {@link LayoutComponent#writeBody(javax.servlet.ServletContext, HttpServletRequest, javax.servlet.http.HttpServletResponse, TagWriter)}
 * method.
 * 
 * <p>
 * This view is used as {@link ContentControl#getView()} of a
 * {@link LayoutControl} representing a business component in div layout.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class InlineContentView extends AbstractControlBase {

	private final LayoutComponent businessComponent;

	/**
	 * Creates a {@link InlineContentView}.
	 * 
	 * @param businessComponent
	 *        See {@link #getComponent()}.
	 */
	public InlineContentView(LayoutComponent businessComponent) {
		super(Collections.<String, ControlCommand>emptyMap());
		
		this.businessComponent = businessComponent;
	}

	@Override
	public LayoutComponent getModel() {
		return businessComponent;
	}

	/**
	 * The {@link LayoutComponent} that is displayed.
	 */
	public final LayoutComponent getComponent() {
		return businessComponent;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected boolean hasUpdates() {
		if (businessComponent.isInvalid()) {
			return true;
		}
		
		// TODO: Incremental updates from legacy (non-control) AJAX components.
		// 
		// return (businessComponent instanceof AJAXComponent) && ((AJAXComponent) businessComponent).isRevalidateRequested();
		return false;
	}
	
	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// TODO: Incremental updates from legacy (non-control) AJAX components.
		//
		// Does not work:
		//  * The component installs itself as action context in its revalidate() method. 
		//    Since the component has no longer any representation at the UI, the actions 
		//    cannot be applied.
		//
		// if (businessComponent.isInvalid()) {
			actions.add(new ContentReplacement(getID(), new HTMLFragment() {
				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					writeComponentContents(context, out);
				}
			}));
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(HTMLConstants.DIV);
		writeControlAttributes(context, out);
		LayoutControlRenderer.writeLayoutConstraintInformation(out, DisplayDimension.HUNDERED_PERCENT);
		LayoutControlRenderer.writeLayoutInformationAttribute(Orientation.HORIZONTAL, 100, out);
		out.beginAttribute(ONSCROLL_ATTR);
		SaveScrollPosition.writePushScrollPositionScript(out, getID());
		out.endAttribute();
		out.endBeginTag();
        
        writeComponentContents(context, out);
		HTMLUtil.beginScriptAfterRendering(out);
		if(businessComponent.shallResetScrollPosition()) {
			SaveScrollPosition.writeResetScrollPositionScript(out, getID());
			businessComponent.doResetScrollPosition(false);
		} else {
			out.append(SaveScrollPosition.getPositionViewportCommand(getID()));
		}
		HTMLUtil.endScriptAfterRendering(out);
        
        out.endTag(HTMLConstants.DIV);
	}

	@Override
	protected void writeControlAttributes(DisplayContext context, TagWriter out) throws IOException {
		super.writeControlAttributes(context, out);
		out.writeAttribute(STYLE_ATTR, "position: absolute; top:0; left:0; overflow:auto; height:100%; width:100%;");
	}

	/*package protected*/ void writeComponentContents(DisplayContext context, TagWriter out) throws IOException {
		LayoutComponent mainlayout = MainLayout.getComponent(context);
		LayoutUtils.setContextComponent(context, this.businessComponent);
		context.set(LayoutConstants.ATTRIBUTE_CONTENTS_ONLY, Boolean.TRUE);
		int currentDepth = out.getDepth();
		try {
			renderComponent(context, out);
			businessComponent.markAsValid();

			if (PerformanceMonitor.isEnabled()) {
				ProcessingInfo processingInfo = context.getProcessingInfo();
				processingInfo.setCommandName(I18NConstants.RENDERING_COMMAND);
				processingInfo.setCommandID(null);
				processingInfo.setJSPName(null);
				processingInfo.setComponentName(businessComponent.getName());
				processingInfo.setProcessingKind(ProcessingKind.COMPONENT_RENDERING);
			}
		} catch (Throwable ex) {
			out.endAll(currentDepth);
			RenderErrorUtil.reportComponentRenderingError(context, out, businessComponent, currentDepth, ex);
		} finally {
			context.reset(LayoutConstants.ATTRIBUTE_CONTENTS_ONLY);
			LayoutUtils.setContextComponent(context, mainlayout);
		}
	}

	/**
	 * Actually renders the {@link #getComponent()}.
	 */
	protected abstract void renderComponent(DisplayContext context, TagWriter out) throws IOException,
			ServletException;
	
	@Override
	protected void internalAttach() {
		super.internalAttach();
		this.businessComponent.getEnclosingFrameScope().setUrlContext(getScope().getFrameScope());
	}
	
	@Override
	protected void internalDetach() {
		this.businessComponent.getEnclosingFrameScope().dropUrlContext();
		super.internalDetach();
	}

}
