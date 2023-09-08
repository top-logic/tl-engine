/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.history;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.AJAXConstants;
import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.FragmentInsertion;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.col.ArrayStack;
import com.top_logic.basic.col.Stack;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.FaviconTag;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.SimpleDisplayValue;
import com.top_logic.layout.URLBuilder;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.XMLContentHandler;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.history.WarnDialogTemplate.WarnMode;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * The {@link HistoryControl} writes the invisible IFrame to ensure that the
 * browser shows a history. Moreover it manages the "browser back" and
 * "browser forward" actions.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HistoryControl extends AbstractVisibleControl implements BrowserHistory {

	private static final ControlCommand[] COMMANDS = new ControlCommand[] { ReplayHistoryCommand.INSTANCE, HistoryChangedCommand.INSTANCE,
			UpdateHistoryCommand.INSTANCE };

	/**
	 * Configuration of the {@link BrowserHistory}.
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * Name of {@link #getHistorySize()} property.
		 */
		String HISTORY_SIZE_PROPERTY = "history-size";

		/**
		 * Name of {@link #getLogoutWarnMode()} property.
		 */
		String LOGOUT_WARN_MODE_PROPERTY = "logout-warn-mode";

		/**
		 * Name of {@link #getNotUndoableWarnMode()} property.
		 */
		String NOT_UNDOABLE_WARN_MODE_PROPERTY = "not-undoable-warn-mode";

		/**
		 * Number of steps to store in the history and offer the user to go back using browser back
		 * button.
		 */
		@Name(HISTORY_SIZE_PROPERTY)
		@IntDefault(10)
		int getHistorySize();

		/**
		 * Which warn mode should be used when browser back can not undo the last action.
		 */
		@Mandatory
		@Name(NOT_UNDOABLE_WARN_MODE_PROPERTY)
		WarnMode getNotUndoableWarnMode();

		/**
		 * Which warn mode should be used when browser back leaves the application.
		 */
		@Mandatory
		@Name(LOGOUT_WARN_MODE_PROPERTY)
		WarnMode getLogoutWarnMode();

	}

	/**
	 * state of the history control during initial write. When the content of
	 * the history frame has that state a special handling must be performed: If
	 * the control has also that state then the control is rendered initial and
	 * the next entry must replayed. If the control has an other state then the
	 * user has pushed 'back'. If this occurs the user has to be informed that
	 * he leaves the application.
	 */
	static final String SAFETY_FRAME = "safetyFrame";

	/**
	 * State of the control when a new IFrame is installed.
	 */
	static final String STACK_OPENED_FRAME = "stackOpened";

	/**
	 * state of the history control which says that currently some history
	 * entries are brought from the server to the client.
	 */
	static final String REPLAYING = "replaying";

	/**
	 * The value of this attribute describes the state of this control. It will
	 * be updated at each server action. After loading the content of the
	 * history frame it will be compared to the state of the loaded page.
	 * 
	 * If they are different (i.e. the user has pushed the browser back button)
	 * then a {@link HistoryChangedCommand change command} is executed.
	 * 
	 * If they are same (the server has changed not only the src of the frame
	 * but also that attribute value, e.g. the user has switched to an other
	 * tab) nothing happens.
	 */
	static final String TL_HISTORY_STATE = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "historystate";

	/**
	 * The value of this attributes states that the server side history must be
	 * brought to the server from that point in the history until the current
	 * state.
	 */
	static final String TL_REPLAY_FROM_ATTR = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "replayfrom";
	// end: constants are used in history.js

	/**
	 * Handler which writes the content of the history iframe.
	 */
	final ContentHandler historyContentHandler = new XMLContentHandler() {

		@Override
		protected void setContentType(DisplayContext context) {
			context.asResponse().setContentType(CONTENT_TYPE_TEXT_HTML_UTF_8);
		}

		@Override
		protected void handleXMLResponse(DisplayContext context, TagWriter out, URLParser url) throws IOException {
			MainLayout.writeHTMLStructureStart(context, out);
			{
				out.beginTag(HEAD);
				out.beginTag(TITLE);
				historyQueue.current().appendTitle(context, out);
				out.endTag(TITLE);
				HTMLUtil.writeJavascriptRef(out, context.getContextPath(), "/script/tl/history.js");
				out.beginScript();
				out.append("function doOnLoad() {");
				out.append("services.ajax.currentState = ");
				out.writeJsString(url.getResource());
				out.append(";");
				out.append("services.form.History.checkChanged('" + HistoryControl.this.getID() + "');");
				out.append("};");
				out.endScript();
				FaviconTag.write(context, out);
				out.endTag(HEAD);
				out.beginBeginTag(BODY);
				out.writeAttribute(ONLOAD_ATTR, "doOnLoad();");
				out.endBeginTag();
				out.endTag(BODY);
			}
			MainLayout.writeHtmlEndTag(out);
		}

	};

	/**
	 * The queue which stores the entries of the history.
	 */
	HistoryQueue historyQueue;

	/**
	 * Whether a warn dialog ("This action can not be undone" or "You leave the application") is
	 * currently displayed. This variable is used to ensure that only one dialog is opened. If such
	 * a variable is not uses and log out dialog is opened another click on browser back would open
	 * an equal dialog again.
	 * 
	 * @see WarnLogoutDialog
	 * @see NotUndoableOperationDialog
	 */
	boolean warnDialogOpened;

	/**
	 * Stack containing the stack levels of the {@link #historyQueue} for which an IFrame was
	 * installed on the client.
	 * 
	 * <p>
	 * This stack may contain gaps, because new IFrames are installed only for those levels in which
	 * a revert-able operation occur.
	 * </p>
	 */
	Stack<Integer> _installedFrames;

	/**
	 * Creates a new history control with configured size
	 */
	public HistoryControl() {
		this(getConfig().getHistorySize());
	}

	/**
	 * Creates a new history control
	 * 
	 * @param historySize
	 *        the size of the history, i.e. the number of entries to store
	 *        before the anterior entries will be forgotten.
	 */
	public HistoryControl(int historySize) {
		super(createCommandMap(COMMANDS));

		init(historySize);
	}

	private void init(int historySize) {
		IdentifiedEntry safetyElement = new IdentifiedEntry("Login", LoginEntry.INSTANCE);
		this.historyQueue = new HistoryQueue(historySize, safetyElement);
		_installedFrames =  new ArrayStack<>();
	}

	@Override
	public Object getModel() {
		return null;
	}

	// browser history aspects

	@Override
	public void addHistory(HistoryEntry entry) {
		final IdentifiedEntry newEntry = new IdentifiedEntry(createNewId(), entry);
		final IdentifiedEntry lastEntry = historyQueue.current();

		historyQueue.add(newEntry);

		// Safety: There are reports that the _installedFrames stack can be empty and crashes
		// with an exception, if peek() is called.
		int currentStackSize = getStackSize();
		if (_installedFrames.isEmpty() || _installedFrames.peek().intValue() != currentStackSize) {
			installNewIFrame(currentStackSize, getIframeID(currentStackSize), lastEntry.getID(),
				String.valueOf(historyQueue.getIndex()));
		} else {
			assert historyQueue.current() == newEntry : "current entry is not the stored one";
			updateToHistory(newEntry);
		}
	}

	@Override
	public void push() {
		historyQueue.push();
	}

	@Override
	public void pop() {
		int currentStackSize = getStackSize();
		if (currentStackSize == 0) {
			// trying to remove first frame
			return;
		}

		// Safety: There are reports that the _installedFrames stack can be empty and crashes with
		// an exception, if peek() is called.
		if (!_installedFrames.isEmpty()) {
			if (_installedFrames.peek().intValue() == currentStackSize) {
				/* For the current stack an frame was installed. */
				_installedFrames.pop();
				removeElement(getIframeID(currentStackSize));
			}
		}

		historyQueue.pop();
	}

	// end browser history aspects

	/**
	 * Updates the client side state of this control without forcing the
	 * {@link HistoryChangedCommand}
	 * 
	 * @param historyEntry
	 *        the entry to which the state must be updated
	 */
	void updateToHistory(final IdentifiedEntry historyEntry) {
		final String historyId = historyEntry.getID();
		addUpdate(new PropertyUpdate(getID(), TL_HISTORY_STATE, ConstantDisplayValue.valueOf(historyId)));
		addUpdate(new PropertyUpdate(getIframeID(historyEntry), SRC_ATTR, new SimpleDisplayValue() {

			@Override
			public String get(DisplayContext context) {
				return getURL(context, historyId);
			}

		}));
	}

	/**
	 * Installs the history to the browser from the element in the history queue
	 * with the given index until {@link HistoryQueue#current()}
	 * 
	 * @param from
	 *        index of the first element in the queue which shall be replayed
	 * 
	 * @param callingFrame
	 *        the Id of the IFrame on the client which triggered the replay
	 */
	void replayHistory(int from, String callingFrame) {
		addUpdate(new PropertyUpdate(getID(), TL_HISTORY_STATE, ConstantDisplayValue.valueOf(REPLAYING)));
		historyQueue.setReplayIndex(from);
		replayEntry(callingFrame);
	}

	/**
	 * Replays the {@link HistoryQueue#elementToReplay() next entry} to replay.
	 * 
	 * @param callingFrame
	 *        the Id of the IFrame on the client which triggered the replay
	 */
	void replayEntry(String callingFrame) {
		final IdentifiedEntry elementToReplay = historyQueue.elementToReplay();

		assert notNull(elementToReplay) : "trying to replay wheras there is nothing to replay.";
		if (elementToReplay == null) {
			doFailsafe();
			return;
		}
		final IdentifiedEntry alreadyReplayedElement = elementToReplay.previous;

		
		// the potential new replay index. Is used to determine whether
		// replaying must go on
		int currentReplayIndex = historyQueue.getReplayIndex();

		//after replaying the next element, we replayed until the current sate of the queue so stop replaying
		boolean stopReplay = currentReplayIndex >= historyQueue.getIndex();
		
		if (stopReplay) {
			/*
			 * must update TL_HISTORY_STATE before new src, as otherwise it
			 * could be possible that the next entry is also replayed (replaying
			 * depends on the value of TL_HISTORY_STATE)
			 */
			addUpdate(new PropertyUpdate(getID(), TL_HISTORY_STATE, ConstantDisplayValue.valueOf(elementToReplay
				.getID())));
		}

		if (alreadyReplayedElement != null) {
			if (alreadyReplayedElement.endsStack()) {
				int stackDepth = elementToReplay.getStackDepth();
				final String newIframe = getIframeID(stackDepth);
				if (callingFrame.equals(newIframe)) {
					// request from the new IFrame so we already installed
					// the new history IFrame and just have to update it to
					// get history within.
					updateIFrameSrc(elementToReplay);
					if (!stopReplay) {
						historyQueue.increaseReplayIndex();
					}
				} else {
					// must not increase replayIndex as we have to replay the current element again
					installNewIFrame(stackDepth, newIframe, alreadyReplayedElement.getID(),
						String.valueOf(currentReplayIndex));
				}
			} else {
				// Don't care about a stack end of the element to replay,
				// because we care about when replaying or adding the next
				// element. 
				updateIFrameSrc(elementToReplay);
				if (!stopReplay) {
					historyQueue.increaseReplayIndex();
				}
			}
		} else {
			// Must replay first element. As this implementation has as first
			// element a simple element here is nothing special to do.
			updateIFrameSrc(elementToReplay);
			if (!stopReplay) {
				historyQueue.increaseReplayIndex();
			}
		}
	}

	// control aspects

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			/*
			 * stack depth is not necessarily 0, e.g. opening a dialog begins a
			 * stack and to many actions within the dialog will kick out the
			 * first last element which was added before the dialog opening.
			 */
			int stackDepth = historyQueue.getFirst().getStackDepth();
			
			writeIFrame(context, out, stackDepth, getIframeID(stackDepth), SAFETY_FRAME);
		}
		out.endTag(DIV);
	}

	@Override
	protected void writeControlAttributes(DisplayContext context, TagWriter out) throws IOException {
		super.writeControlAttributes(context, out);
		out.writeAttribute(TL_HISTORY_STATE, SAFETY_FRAME);
		out.writeAttribute(TL_REPLAY_FROM_ATTR, 1);
		out.writeAttribute(STYLE_ATTR, "display:none");
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		getScope().getFrameScope().registerContentHandler(null, historyContentHandler);
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();
		_installedFrames.clear();
	}

	@Override
	protected void internalDetach() {
		getScope().getFrameScope().deregisterContentHandler(historyContentHandler);
		super.internalDetach();
	}

	// end control aspects

	private int removeElement(String elementId) {
		return addUpdate(new ElementReplacement(elementId, Fragments.empty()));
	}

	private void installNewIFrame(final int stackDepth, final String iFrameId, final String historyId, String replayFrom) {
		addUpdate(new PropertyUpdate(getID(), TL_REPLAY_FROM_ATTR, ConstantDisplayValue.valueOf(replayFrom)));
		addUpdate(new PropertyUpdate(getID(), TL_HISTORY_STATE, ConstantDisplayValue.valueOf(STACK_OPENED_FRAME)));
		addUpdate(new FragmentInsertion(getID(), AJAXConstants.AJAX_POSITION_START_VALUE, new HTMLFragment() {

			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				writeIFrame(context, out, stackDepth, iFrameId, historyId);
			}
		}));
	}

	void writeIFrame(DisplayContext context, TagWriter out, int stackDepth, String iFrameId, final String historyId)
			throws IOException {
		out.beginBeginTag(IFRAME);
		out.writeAttribute(ID_ATTR, iFrameId);
		out.writeAttribute(NAME_ATTR, iFrameId);
		out.writeAttribute(SCROLLING, SCROLLING_NO_VALUE);
		out.writeAttribute(SRC_ATTR, getURL(context, historyId));
		out.endBeginTag();
		out.endTag(IFRAME);

		_installedFrames.push(stackDepth);
	}

	String getURL(DisplayContext context, String historyId) {
		final FrameScope frameScope = getScope().getFrameScope();
		final URLBuilder urlBuilder = frameScope.getURL(context, historyContentHandler);
		urlBuilder.addResource(historyId);
		return urlBuilder.getURL();
	}

	private String getIframeID(IdentifiedEntry element) {
		return getIframeID(element.getStackDepth());
	}
	
	private String getIframeID(int stack) {
		return getID() + "_iframe_" + stack;
	}

	final boolean notNull(Object o) {
		return o != null;
	}

	private void doFailsafe() {
		// Un expected situation
		init(0);
		requestRepaint();
	}

	/**
	 * Creates a new id which is unique for the document this control is
	 * rendered in.
	 */
	private String createNewId() {
		return "history_" + getScope().getFrameScope().createNewID();
	}

	private void updateIFrameSrc(final IdentifiedEntry element) {
		addUpdate(new PropertyUpdate(getIframeID(element), SRC_ATTR, new SimpleDisplayValue() {

			@Override
			public String get(DisplayContext context) {
				return getURL(context, element.getID());
			}
		}));
	}

	int getStackSize() {
		return historyQueue.getStackNumbers();
	}
	
	static Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

}
