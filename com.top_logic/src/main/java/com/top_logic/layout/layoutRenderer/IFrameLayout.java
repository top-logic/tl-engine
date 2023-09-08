/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.AbstractConstantControlBase;
import com.top_logic.layout.structure.ContentControl;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponentScope;

/**
 * The class {@link IFrameLayout} renders an iframe for its {@link LayoutComponent business
 * component}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IFrameLayout extends AbstractConstantControlBase {

	public static final String FULL_SIZE = "100%";

	/** the renderer to use to render content during loading time if no else was set */
	private static final Renderer<LayoutComponent> DEFAULT_LOADING_RENDERER = new Renderer<>() {
		@Override
		public void write(DisplayContext context, TagWriter out, LayoutComponent value) throws IOException {
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "absolute maximal loading");
			out.endBeginTag();
			out.endTag(DIV);
		}

	};

	private final LayoutComponent businessComponent;
	private String height = FULL_SIZE;
	private String width = FULL_SIZE;

	private Scrolling scrollable = Scrolling.NO;

	private Renderer<? super LayoutComponent> loadingRenderer = DEFAULT_LOADING_RENDERER;

	/**
	 * Creates a {@link IFrameLayout} for the given component.
	 * 
	 * @param businessComponent
	 *            the component for which an iframe shall be rendered
	 * @param height
	 *            the value of the {@link HTMLConstants#HEIGHT_ATTR height attribute} of the
	 *            {@link HTMLConstants#IFRAME iframe} tag
	 * @param width
	 *            the value of the {@link HTMLConstants#WIDTH_ATTR width attribute} of the
	 *            {@link HTMLConstants#IFRAME iframe} tag
	 * @param scrollableValue
	 *            the value of the {@link HTMLConstants#SCROLLING scrolling attribute} of the
	 *            {@link HTMLConstants#IFRAME iframe} tag. must be
	 *            {@link HTMLConstants#SCROLLING_AUTO_VALUE auto},
	 *            {@link HTMLConstants#SCROLLING_YES_VALUE yes} or
	 *            {@link HTMLConstants#SCROLLING_NO_VALUE no}.
	 */
	public IFrameLayout(LayoutComponent businessComponent, String height, String width, Scrolling scrollableValue) {
		this.businessComponent = businessComponent;
		this.height = height;
		this.width = width;
		this.scrollable = scrollableValue;
	}

	/**
	 * Creates a {@link IFrameLayout} for the given component. The written
	 * {@link HTMLConstants#IFRAME iframe} tag will have height and width {@link #FULL_SIZE 100%}
	 * and will {@link HTMLConstants#SCROLLING_NO_VALUE not} scroll.
	 * 
	 * @param businessComponent
	 *            the component for which an iframe shall be rendered
	 */
	public IFrameLayout(LayoutComponent businessComponent) {
		super();
		this.businessComponent = businessComponent;
	}

	@Override
	public LayoutComponent getModel() {
		return businessComponent;
	}

	@Override
	public boolean isVisible() {
		return businessComponent.isVisible();
	}
	
	/**
	 * Sets a {@link Renderer} to render stuff while the iframe for the component of this
	 * {@link IFrameLayout} is loading. <code>renderer == null</code> means that nothing shall be
	 * shown during loading. The renderer gets the {@link LayoutComponent business component} of
	 * this {@link IFrameLayout} as argument.
	 * 
	 * @param renderer
	 *        A {@link Renderer} to render the content during the iframe is loading, or
	 *        <code>null</code> to produce no loading preview.
	 */
	public void setLoadingRenderer(Renderer<? super LayoutComponent> renderer) {
		this.loadingRenderer = renderer;
	}

	@Override
	public void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		final String iframeName = businessComponent.getName() + ContentControl.SUFFIX;
		final LayoutComponentScope writtenScope = businessComponent.getEnclosingFrameScope();
		String loadingId = getScope().getFrameScope().createNewID();
		final String iframeID =
			writtenScope.writeIFrame(context, out, iframeName, loadingId, width, height, scrollable);

		if (loadingRenderer != null) {

			/*
			 * must currently write the things to show during loading directly. on client-side a
			 * js-script will first hide the content and after a certain time it will show it again.
			 * This ensures that the content will not be shown directly. It is not possible to
			 * render the content with "display:none" and than turning it visible via
			 * "display:block", since IE7 won't show background images defined in css classes in
			 * this case. It is able to show the correct background color, but not the image.
			 */
			out.beginBeginTag(DIV);
			out.writeAttribute(ID_ATTR, loadingId);
			out.writeAttribute(STYLE_ATTR, "display: none;");
			out.writeAttribute(CLASS_ATTR, "absolute maximal");
			out.endBeginTag();
			{
				loadingRenderer.write(context, out, businessComponent);
			}
			out.endTag(DIV);

			out.beginScript();
			out.append("services.ajax.showLoading('" + iframeID + "');");
			out.endScript();
		}
	}

	@Override
	protected void disableChildScopes(boolean disabled) {
		super.disableChildScopes(disabled);

		ControlScope contentScope = businessComponent.getControlScope();
		if (contentScope != null) {
			contentScope.disableScope(disabled);
		}
	}
	
	@Override
	protected void internalAttach() {
		super.internalAttach();
		businessComponent.getEnclosingFrameScope().setUrlContext(getScope().getFrameScope());
	}
	
	@Override
	protected void detachInvalidated() {
		// Prevent old requests still "in the air" from hitting this component.
		businessComponent.increaseSubmitNumber();
		super.detachInvalidated();
	}

	@Override
	protected void internalDetach() {
		businessComponent.getEnclosingFrameScope().dropUrlContext();
		super.internalDetach();
	}
}
