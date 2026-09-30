/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.form.boxes.control.BoxControl;
import com.top_logic.layout.form.boxes.layout.BoxLayout;
import com.top_logic.layout.form.boxes.model.Box;
import com.top_logic.layout.form.boxes.model.DefaultCollectionBox;
import com.top_logic.layout.form.boxes.model.FragmentBox;
import com.top_logic.layout.form.tag.ControlBodyTag;
import com.top_logic.layout.form.tag.ControlTagUtil;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Base class for {@link BoxContainerTag}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractBoxContainerTag extends AbstractBoxTag implements BoxContainerTag, ControlBodyTag {

	/** Default CSS class for content boxes, i.e. for content created by {@link #mkContentBox()}. */
	protected static final String FRM_CONTENT_CSS_CLASS = "content";

	private BoxContainerTag _root;

	private BoxControl _control;

	private DefaultCollectionBox _boxes;

	private FragmentBox _content;

	private Boolean _independentOptional;

	/**
	 * Whether to render independently, even if part of a {@link BoxTag} hierarchy.
	 */
	public void setIndependent(boolean independent) {
		_independentOptional = Boolean.valueOf(independent);
	}

	@Override
	public final BoxContainerTag getRoot() {
		if (_root == null) {
			_root = findRoot();
		}
		return _root;
	}

	/**
	 * Find the top-level {@link BoxContainerTag} that is responsible for rendering the whole
	 * layout.
	 */
	protected BoxContainerTag findRoot() {
		if (isIndependent()) {
			// Render independently even if part of a box tag hierarchy.
			return this;
		}
		BoxContainerTag parentContainer = getBoxContainer();
		if (parentContainer != null) {
			return parentContainer.getRoot();
		} else {
			return this;
		}
	}

	private Boolean isIndependent() {
		if (_independentOptional == null) {
			return getIndependentDefault();
		}
		return _independentOptional;
	}

	/**
	 * The value for {@link #isIndependent()}, if the tag attribute was not specified explicitly.
	 */
	protected boolean getIndependentDefault() {
		return false;
	}

	@Override
	public boolean isRoot() {
		return getRoot() == this;
	}

	@Override
	public BoxControl getRenderingControl() {
		if (isRoot()) {
			if (_control == null) {
				_control = new BoxControl();
			}
			return _control;
		}
		
		return getRoot().getRenderingControl();
	}

	/**
	 * The container {@link Box} to use, if this tag has box content.
	 * 
	 * @see AbstractBoxContainerTag#mkContentBox()
	 */
	protected abstract DefaultCollectionBox createCollectionBox();

	@Override
	public int doEndTag() throws JspException {
		initContent();

		if (isRoot()) {
			try {
				TagWriter out = MainLayout.getTagWriter(pageContext);
				Box renderingBox = getRenderingBox();
				BoxControl renderingControl = getRenderingControl();
				renderingControl.setModel(renderingBox);
				ControlTagUtil.writeControl(out, this, this.pageContext, renderingControl);
				out.flushBuffer();

			} catch (IOException ex) {
				throw new JspException(ex);
			}
		}

		return super.doEndTag();
	}

	@Override
	public String addControl(HTMLFragment childControl) {
		return JSPLayoutedControls.addControl(mkContentBox(), childControl);
	}

	/**
	 * Initializes the content box, either {@link #mkContainerBox()}, or {@link #mkContentBox()}.
	 */
	protected void initContent() {
		BodyContent jspContent = getBodyContent();
		if (_boxes != null) {
			if (jspContent != null && !jspContent.getString().trim().isEmpty()) {
				throw new IllegalStateException(
					"A "
						+ getTagName()
						+ " tag must either contain direct JSP content (with corresponding attributes for styling) or other box structure tags, not both. JSP content was: "
						+ jspContent.getString());
			}
			if (_content != null) {
				throw new IllegalStateException(
					"A " + getTagName()
						+ " tag must either contain direct JSP content (with corresponding attributes for styling) or other box structure tags, not both.");
			}
		} else {
			FragmentBox contentBox = mkContentBox();
			if (jspContent != null) {
				JSPLayoutedControls.setContentPattern(contentBox, jspContent.getString());
			}
		}
	}

	/**
	 * The {@link Box} that is rendered, if this is a root tag, see {@link #isRoot()}.
	 */
	protected Box getRenderingBox() {
		if (_boxes != null) {
			assert _content == null : "Either JSP content or box content must be set, not both.";

			return _boxes;
		} else {
			return _content;
		}
	}

	@Override
	protected void tearDown() {
		_root = null;
		_control = null;
		_boxes = null;
		_content = null;
		_independentOptional = null;

		super.tearDown();
	}

	@Override
	public BoxLayout getLayout() {
		return mkContainerBox().getLayout();
	}

	@Override
	public void addBox(Box content) {
		mkContainerBox().addContent(content);
	}

	private DefaultCollectionBox mkContainerBox() {
		if (_boxes == null) {
			DefaultCollectionBox newCollection = createCollectionBox();
			addToParent(newCollection);
			_boxes = newCollection;
		}
		return _boxes;
	}

	/**
	 * Adds the given container or content {@link Box} of this {@link Tag} to the parent container
	 * defined by the parent {@link Tag} in the {@link BoxTag} hierarchy.
	 * 
	 * <p>
	 * Does nothing, if this is the root {@link Tag}, see {@link #isRoot()}.
	 * </p>
	 * 
	 * @param newBox
	 *        The Box to add to the parent container.
	 */
	protected void addToParent(Box newBox) {
		if (!isRoot()) {
			getBoxContainer().addBox(newBox);
		}
	}

	/**
	 * Look up the enclosing {@link BoxContainerTag}, if any.
	 * 
	 * @return The enclosing {@link BoxContainerTag}, or <code>null</code>, if there is none.
	 */
	protected final BoxContainerTag getBoxContainer() {
		return AbstractBoxContainerTag.getBoxContainer(this);
	}

	/**
	 * Look up the enclosing {@link BoxContainerTag} of the given {@link Tag}, if any.
	 * 
	 * @return The enclosing {@link BoxContainerTag}, or <code>null</code>, if there is none.
	 */
	public static BoxContainerTag getBoxContainer(Tag self) {
		Tag ancestor = self.getParent();
		while (ancestor != null) {
			if (ancestor instanceof BoxContainerTag) {
				return (BoxContainerTag) ancestor;
			}
			
			if (ancestor instanceof BoxTag) {
				// Other box tag that is not a container, stop search since the current tag must be
				// rendered in textual context mode.
				return null;
			}

			// The ancestor tag may be a structural tag from another tag library. This is ok as long
			// as this tag does not produce any textual contents.
			ancestor = ancestor.getParent();
		}

		// No container found.
		return null;
	}

	/**
	 * The container box to use, if this tag has no box content, but plain JSP content.
	 */
	protected FragmentBox mkContentBox() {
		if (_content == null) {
			FragmentBox newContent = JSPLayoutedControls.createJSPContentBox();
			newContent.setCssClass(FRM_CONTENT_CSS_CLASS);
			addToParent(newContent);
			_content = newContent;
		}
		return _content;
	}

}
