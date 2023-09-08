/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;
import java.util.Collection;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.CommandListenerRegistry;
import com.top_logic.layout.CompositeContentHandler;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.SimpleCommandListenerRegistry;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.listen.ModelScope;

/**
 * The class {@link LayoutComponentScope} is a {@link FrameScope} which
 * represents a business component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LayoutComponentScope extends ComponentContentHandler implements FrameScope {

	/**
	 * Data attribute referencing the <code>iframe</code>'s loading div.
	 */
	private static final String DATA_LOADING_ID = "data-loadingid";

	private final CommandListenerRegistry registry;
	
	/**
	 * Creates a {@link LayoutComponentScope}.
	 *
	 * @param businessComponent The base model of all views rendered in this scope.
	 */
	public LayoutComponentScope(LayoutComponent businessComponent) {
		// Parent handler is initialized lately, see #setUrlContext().
		super(null, businessComponent);
		
		this.registry = new SimpleCommandListenerRegistry();
	}

	/**
	 * Adds the client action to the
	 * {@link FrameScope#addClientAction(ClientAction) client actions} of the
	 * the enclosing scope.
	 */
	@Override
	public void addClientAction(ClientAction update) {
		getEnclosingScope().addClientAction(update);
	}

	/**
	 * Returns the window scope of the enclosing frame scope
	 * 
	 * @see #getEnclosingScope()
	 */
	@Override
	public WindowScope getWindowScope() {
		return getEnclosingScope().getWindowScope();
	}

	@Override
	public ModelScope getModelScope() {
		return getComponent().getModelScope();
	}

	/**
	 * Returns the enclosing {@link FrameScope} of this
	 * {@link LayoutComponentScope}. Must just be called if a non
	 * <code>null</code> enclosing scope was set.
	 * 
	 * @see LayoutComponentScope#setUrlContext(CompositeContentHandler)
	 * 
	 * @throws IllegalStateException
	 *         iff there is currently no enclosing scope set
	 */
	@Override
	public FrameScope getEnclosingScope() {
		CompositeContentHandler theUrlContext = getUrlContext();
		if (theUrlContext instanceof FrameScope) {
			return (FrameScope) theUrlContext;
		}
		
		throw new IllegalStateException(this + " has currently no parent " + FrameScope.class.getSimpleName() + ".");
	}

	@Override
	public <T extends Appendable> T appendClientReference(T out) throws IOException {
		out.append(".document.getElementById('").append(getPathName()).append("').contentWindow");
		return out;
	}
	
	protected CommandListenerRegistry getRegistryImplementation() {
		return registry;
	}
	
	@Override
	public void addCommandListener(CommandListener listener) {
		getRegistryImplementation().addCommandListener(listener);
	}
	
	@Override
	public boolean removeCommandListener(CommandListener listener) {
		return getRegistryImplementation().removeCommandListener(listener);
	}
	
	@Override
	public void clear() {
		getRegistryImplementation().clear();
	}
	
	@Override
	public CommandListener getCommandListener(String requestedId) {
		return getRegistryImplementation().getCommandListener(requestedId);
	}

	@Override
	public Collection<CommandListener> getCommandListener() {
		return getRegistryImplementation().getCommandListener();
	}

	/**
	 * Writes an {@link HTMLConstants#IFRAME} to load the content of this
	 * {@link LayoutComponentScope} lazy.
	 * 
	 * @param context
	 *        the context in which rendering occur
	 * @param out
	 *        the stream to write content to
	 * @param frameName
	 *        the name of the iframe to write
	 * @param loadingId
	 *        The ID referencing the loading div.
	 * @param width
	 *        the width of the iframe
	 * @param height
	 *        the height of the iframe
	 * @param scrollable
	 *        the value of the {@link HTMLConstants#SCROLLING scrolling attribute}..
	 * 
	 * @throws IOException
	 *         iff the {@link TagWriter} throws some.
	 * 
	 * @return the id attribute of the written tag
	 */
	public String writeIFrame(DisplayContext context, TagWriter out, String frameName, String loadingId, String width,
			String height, Scrolling scrollable)
			throws IOException {
		final String iframeID = getPathName();

		out.beginBeginTag(HTMLConstants.IFRAME);
		out.writeAttribute(HTMLConstants.NAME_ATTR, frameName);
		out.writeAttribute(HTMLConstants.ID_ATTR, iframeID);
		out.writeAttribute(DATA_LOADING_ID, loadingId);
		out.writeAttribute(HTMLConstants.SRC_ATTR, HTMLConstants.EMPTY_SOURCE_VALUE);
		out.writeAttribute(HTMLConstants.TL_FRAME_SOURCE_ATTR,
			getURL(context).appendParameter("sn", businessComponent.getSubmitNumber()).getURL());
		writeStyle(out, width, height, scrollable.toOverflowAttribute());
		out.writeAttribute(HTMLConstants.WIDTH_ATTR, width);
		out.writeAttribute(HTMLConstants.HEIGHT_ATTR, height);
		out.writeAttribute(HTMLConstants.FRAMEBORDER_ATTR, 0);
		out.writeAttribute(HTMLConstants.SCROLLING, scrollable.toScrollingValue());
		out.endBeginTag();
		out.endTag(HTMLConstants.IFRAME);
		return iframeID;
	}

	private void writeStyle(TagWriter out, String width, String height, String overflow) throws IOException {
		out.beginAttribute(HTMLConstants.STYLE_ATTR);
		out.append("position: absolute; top:0; left:0; overflow:");
		out.append(overflow);
		out.append("; height:");
		out.append(height);
		out.append("; width:");
		out.append(width);
		out.append(";");
		out.endAttribute();
	}

}
