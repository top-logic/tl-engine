/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.awt.Point;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import com.top_logic.base.services.simpleajax.ContentReplacement;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.KeyCode;
import com.top_logic.layout.KeyEvent;
import com.top_logic.layout.KeyEventDispatcher;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.basic.KeyCodeHandler;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.layoutRenderer.PopupDialogRenderer;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * This class represents a PopupDialog
 * 
 * @author     <a href="mailto:STS@top-logic.com">STS</a>
 */
public class PopupDialogControl extends AbstractControlBase implements KeyEventDispatcher, TitleChangedListener,
		DialogClosedListener {

	/**
	 * Horizontal position of the pop-up relative to the opening button.
	 */
	public enum HorizontalPopupPosition {
		/**
		 * Below or above the button keeping the left border of the popup aligned to the left side
		 * of the button.
		 * 
		 * <pre>
		 * +------+
		 * |Button|
		 * +------+
		 * +------------------
		 * |Popu-up
		 * |
		 * |
		 * </pre>
		 */
		LEFT_ALIGNED,

		/**
		 * Below or above the button keeping the right border of the popup aligned to the right side
		 * of the button.
		 * 
		 * <pre>
		 *            +------+
		 *            |Button|
		 *            +------+
		 * +-----------------+
		 *            Popu-up|
		 *                   |
		 *                   |
		 * </pre>
		 */
		RIGHT_ALIGNED,

		/**
		 * Aligning the left border of the pop-up to the right side of the button.
		 * 
		 * <pre>
		 * +------++------------------
		 * |Button||Popu-up
		 * +------+|
		 *         |
		 * </pre>
		 */
		TO_THE_RIGHT,

		/**
		 * Aligning the right border of the pop-up to the left side of the button.
		 * 
		 * <pre>
		 * ------------------++------+
		 *            Popu-up||Button|
		 *                   |+------+
		 *                   |
		 * </pre>
		 */
		TO_THE_LEFT,
	}

	/**
	 * Vertical position of the pop-up relative to the opening button.
	 */
	public enum VerticalPopupPosition {
		/**
		 * Opening the pop-up below the button or keeping the top borders of the button and the
		 * pop-up aligned.
		 */
		BELOW,

		/**
		 * Opening the pop-up above the button or keeping the bottom borders of the button and the
		 * pop-up aligned.
		 */
		ABOVE
	}

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(KeyCodeHandler.INSTANCE);

	public static final String POPUP_DIALOG_TITLE_BAR_SUFFIX = "-titleBar";

	public static final String POPUP_DIALOG_TITLE_SUFFIX = "-title";

	private PopupDialogModel model;
	private BrowserWindowControl parent;

	private HTMLFragment _content = Fragments.empty();

	private ControlRenderer<? super PopupDialogControl> popupDialogRenderer = PopupDialogRenderer.DEFAULT_INSTANCE;

	private boolean hasUpdates = false;

	private DisplayValue _placementCommand = ConstantDisplayValue.EMPTY_STRING;

	private Command _returnCommand;

	final FrameScope _targetDocument;
	
	/**
	 * Create a new PopupDialogControl
	 * 
	 * @param targetDocument
	 *        The {@link FrameScope} relative to which the popup should be placed.
	 * @param model
	 *        The model of the popup-dialog
	 * @param position
	 *        Client side position to open the control.
	 */
	public PopupDialogControl(FrameScope targetDocument, PopupDialogModel model, Point position) {
		this(targetDocument, model);
		_placementCommand = openAtPosition(position);
	}

	/**
	 * Create a new PopupDialogControl
	 * 
	 * @param targetDocument
	 *        The {@link FrameScope} relative to which the popup should be placed.
	 * @param model
	 *        The model of the popup-dialog
	 * @param placementID
	 *        ID the client-side element, near which the popup dialog should be placed.
	 */
	public PopupDialogControl(FrameScope targetDocument, PopupDialogModel model, String placementID) {
		this(targetDocument, model, placementID, HorizontalPopupPosition.LEFT_ALIGNED, VerticalPopupPosition.BELOW);
	}

	/**
	 * Creates a {@link PopupDialogControl}.
	 *
	 * @param targetDocument
	 *        The {@link FrameScope} relative to which the popup should be placed.
	 * @param model
	 *        The model of the popup-dialog
	 * @param placementID
	 *        ID the client-side element, near which the popup dialog should be placed.
	 * @param hPos
	 *        The horizontal location of the opened pop-up relative to the element identified with
	 *        <code>placementID</code>.
	 * @param vPos
	 *        The vertical location of the opened pop-up relative to the element identified with
	 *        <code>placementID</code>.
	 */
	public PopupDialogControl(FrameScope targetDocument, PopupDialogModel model, String placementID,
			HorizontalPopupPosition hPos, VerticalPopupPosition vPos) {
		this(targetDocument, model);
		_placementCommand = openAtElement(placementID, hPos, vPos);
	}

	/**
	 * Create a new PopupDialogControl
	 * 
	 * @param targetDocument
	 *        The {@link FrameScope} relative to which the popup should be placed.
	 * @param model
	 *        The model of the popup-dialog
	 */
	private PopupDialogControl(FrameScope targetDocument, PopupDialogModel model) {
		super(COMMANDS);
		_targetDocument = targetDocument;
		this.model = model;
	}
	
	@Override
	public PopupDialogModel getModel() {
		return model;
	}

	/**
	 * Getter for the model of the popup-dialog
	 * 
	 * @return model of the popup-dialog
	 */
	public PopupDialogModel getPopupDialogModel() {
		return model;
	}
	
	/**
	 * This method defines the parent for this popup dialog
	 * 
	 * @param window - the parent of this popup dialog
	 */
	/* package protected */ void initParent(BrowserWindowControl window) {
		assert window != null : "Must be initialized with a window.";
		assert parent == null : "Must be initialized only once.";
		
		parent = window;

		registerAsListener();
	}

	private void registerAsListener() {
		model.addListener(PopupDialogModel.POPUP_DIALOG_CLOSED_PROPERTY, this);
		model.addListener(WindowModel.TITLE_PROPERTY, this);
	}

	private void deregisterAsListener() {
		model.removeListener(WindowModel.TITLE_PROPERTY, this);
		model.removeListener(PopupDialogModel.POPUP_DIALOG_CLOSED_PROPERTY, this);
	}

	/**	 
	 * @see com.top_logic.layout.basic.AbstractControlBase#hasUpdates()
	 */
	@Override
	protected boolean hasUpdates() {
		return hasUpdates;
	}

	@Override
	protected void internalRevalidate(DisplayContext context,
			UpdateQueue actions) {
		
		actions.add(new ContentReplacement(getID() + POPUP_DIALOG_TITLE_SUFFIX, model.getDialogTitle()));
		hasUpdates = false;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out)
			throws IOException {
		popupDialogRenderer.write(context, out, this);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		popupDialogRenderer.appendControlCSSClasses(out, this);
	}

	@Override
	public boolean isVisible() {
		return true;
	}
	
	/**
	 * This method returns the command to lay out this {@link PopupDialogControl}.
	 * 
	 * @param context - the display context
	 */
	public final void writeRenderingCommand(DisplayContext context, Appendable out) throws IOException {
		_placementCommand.append(context, out);
	}

	private DisplayValue openAtElement(String elementId, HorizontalPopupPosition hPos, VerticalPopupPosition vPos) {
		return new AbstractDisplayValue() {

			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				String frameReference =
					LayoutUtils.getFrameReference(context.getExecutionScope().getFrameScope(), _targetDocument);

				out.append(frameReference);
				out.append(".PlaceDialog.openAtElement('");
				out.append(elementId);
				out.append("', '");
				out.append(getID());
				out.append("', '");
				out.append(hPos.name());
				out.append("', '");
				out.append(vPos.name());
				out.append("'");
				out.append(");\n");
			}
		};
	}

	private DisplayValue openAtPosition(Point position) {
		return new AbstractDisplayValue() {

			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				String frameReference =
					LayoutUtils.getFrameReference(context.getExecutionScope().getFrameScope(), _targetDocument);

				out.append(frameReference);
				out.append(".PlaceDialog.openAtPosition('");
				out.append(getID());
				out.append("', ");
				out.append(Double.toString(position.getX()));
				out.append(", ");
				out.append(Double.toString(position.getY()));
				out.append(");\n");
			}
		};
	}
	
	/**
	 * This method returns commands for client-side close actions of the popup dialog
	 */
	public void writeClientSideCloseAction(TagWriter out) throws IOException {
		out.append("services.form.BrowserWindowControl.closePopupDialog(");
		writeIdJsString(out);
		out.append(");");
	}
	
	/**
	 * This method closes the popup dialog
	 */
	private void closePopupDialog() {
		deregisterAsListener();
		parent.unregisterAndClosePopupDialog(this);
	}
	
	/**
	 * This method sets the content of this popup dialog.
	 * 
	 * @param content
	 *        New content. Must not be <code>null</code>.
	 * 
	 * @see #getPopupContent()
	 */
	public void setContent(HTMLFragment content) {
		// Detaches the old content
		requestRepaint();
		_content = Objects.requireNonNull(content);
	}
	
	/**
	 * Returns the content of the popup dialog
	 * 
	 * @return Content of this popup.
	 */
	public HTMLFragment getPopupContent() {		
		return _content;
	}
	
	@Override
	protected void detachInvalidated() {
		super.detachInvalidated();
		
		if (_content instanceof Control) {
			((Control) _content).detach();
		}
	}

	@Override
	public void handleTitleChanged(Object sender, HTMLFragment oldTitle, HTMLFragment newTitle) {
		hasUpdates = true;
	}

	@Override
	public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
		if (newValue && (parent != null))
			closePopupDialog();
	}

	@Override
	public HandlerResult dispatchKeyEvent(DisplayContext commandContext, KeyEvent event) {
		if (_returnCommand != null) {
			return _returnCommand.executeCommand(commandContext);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Sets the command to execute when an event for {@link KeyCode#RETURN} occurs
	 * 
	 * @param returnCommand
	 *        the command to execute
	 */
	public void setReturnCommand(Command returnCommand) {
		_returnCommand = returnCommand;
	}

	/**
	 * Returns the key selectors which are handled by this control.
	 */
	public String getKeySelectors() {
		/* must handle RETURN also in case no special handler is registered. This is due to current
		 * client side implementation of key handling. If this is registered, then the event is
		 * client side canceled. If this is not registered (and no parent element) then the event
		 * bubbles up to the window object. In that case the window onClick handler is triggered
		 * which causes triggering of some other button (#6316) */
		return "K013";
	}

}
