/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;

import com.top_logic.base.services.simpleajax.AJAXConstants;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.FragmentInsertion;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.SetBuilder;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataProxy;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.gui.layout.upload.DefaultDataItem;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.ApplicationWindowScope;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.Control;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DownloadModification;
import com.top_logic.layout.ErrorPage;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.KeyEvent;
import com.top_logic.layout.KeyEventDispatcher;
import com.top_logic.layout.NoModification;
import com.top_logic.layout.URLBuilder;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.UpdateListener;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.basic.ControlValidator;
import com.top_logic.layout.basic.FragmentRenderer;
import com.top_logic.layout.basic.KeyCodeHandler;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.basic.timer.TimerControl;
import com.top_logic.layout.history.HistoryControl;
import com.top_logic.layout.history.HistoryEntry;
import com.top_logic.layout.layoutRenderer.BrowserWindowRenderer;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.servlet.CacheControl;
import com.top_logic.layout.window.WindowManager;
import com.top_logic.mig.html.layout.CommandDispatcher;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.filter.CompressionFilter;

/**
 * The class {@link BrowserWindowControl} represents the {@link WindowControl} which is responsible
 * for a whole browser page. It also provides a list of {@link DialogWindowControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BrowserWindowControl extends WindowControl<BrowserWindowControl>
		implements ApplicationWindowScope, ContentHandler, KeyEventDispatcher {

	private static final ControlCommand[] INNER_COMMANDS = new ControlCommand[] { KeyCodeHandler.INSTANCE, GlobalPopupDialogHandler.INSTANCE,
																							   SinglePopupDialogHandler.INSTANCE};

	private static final Property<Boolean> KEEP_DOWNLOADS =
		TypedAnnotatable.property(Boolean.class, "keep downloads", Boolean.FALSE);

	/**
	 * Set of content types which are zipped
	 */
	private static final Set<String> COMPRESSED_FORMATS;
	static {
		COMPRESSED_FORMATS = new SetBuilder<String>()
			// gzip
			.add("application/gzip")
			.add("application/x-gzip")
			.add("application/x-gunzip")
			.add("application/gzipped")
			.add("application/gzip-compressed")
			// zip
			.add("application/zip")
			.add("application/x-zip")
			.add("application/x-zip-compressed")
			.add("application/x-compress")
			.add("application/x-compressed")
			.add("multipart/x-zip")
			// bzip2
			.add("application/bzip2")
			.add("application/x-bz2")
			.add("application/x-bzip")
			.toSet();
	}


	public static final String DIALOG_ANCHOR = "dlgDialogs";
	public static final String POPUP_DIALOG_ANCHOR = "pdlgPopupDialogs";
	public static final String POPUP_DIALOG_PANE = "pdlgPopupDialogPane";
	
	public static final String POPUP_DIALOG_OPENED_CLASS = "pdlgPopupDialogPane_visible";
	public static final String NO_POPUP_DIALOG_OPENED_CLASS = "pdlgPopupDialogPane_invisible";

	public static final String DIALOG_OPENED_CLASS = "dialogOpened";
	public static final String NO_DIALOG_OPENED_CLASS = "noDialogOpened";
	
	/**
	 * Configuration of the {@link BrowserWindowControl}.
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends ConfigurationItem {

		/**
		 * Name of {@link #getDownloadSurvivingTime()} property.
		 */
		String DOWNLOAD_SURVIVING_TIME_PROPERTY = "download-surviving-time";

		/**
		 * Name of {@link #getDownloadSurvivingTime()} property.
		 */
		String DOWNLOAD_MODIFICATION_PROPERTY = "download-modification";

		/**
		 * Time in milliseconds which is used to determine when a download can be forgotten: At each
		 * time a download is accessed the date of access is stored. If an AJAX-Request receives the
		 * server and the last access happened {@link #getDownloadSurvivingTime()} milliseconds ago,
		 * then the corresponding download will be thrown away.
		 */
		@Name(DOWNLOAD_SURVIVING_TIME_PROPERTY)
		@LongDefault(3 * 60 * 1000)
		long getDownloadSurvivingTime();

		/**
		 * Plugin to modify the {@link BinaryData} delivered by this {@link BrowserWindowControl}.
		 */
		@Name(DOWNLOAD_MODIFICATION_PROPERTY)
		@InstanceFormat
		@InstanceDefault(NoModification.class)
		DownloadModification getDownloadModification();

	}

	/**
	 * The dialogs which are currently visible on the GUI. The dialogs are kept in "open-order": The
	 * first opened dialog is the first one in the list. The most recently opened dialog is the last
	 * in the list. As the most recently opened dialog is also the active one, this means that the
	 * active dialog is the last one in the list.
	 */
	private final List<DialogWindowControl> dialogs = new ArrayList<>();
	
	/**
	 * The popup dialogs which are currently visible on the GUI. The dialogs are kept in
	 * "open-order": The first opened dialog is the first one in the list. The most recently opened
	 * dialog is the last in the list. As the most recently opened dialog is also the active one,
	 * this means that the active dialog is the last one in the list.
	 */
	private final List<PopupDialogControl> popupDialogs = new ArrayList<>();
	
	private final HashMap<Control, ControlScope> layerScopes = new HashMap<>();
	
	/**
	 * Unmodifiable version of {@link #dialogs}.
	 */
	private final List<DialogWindowControl> dialogsView = Collections.unmodifiableList(this.dialogs);
	
	private final List<DialogWindowControl> dialogsToClose = new ArrayList<>();
	private final List<DialogWindowControl> dialogsToOpen = new ArrayList<>();
	
	private final List<PopupDialogControl> popupDialogsToClose = new ArrayList<>();
	private final List<PopupDialogControl> popupDialogsToOpen = new ArrayList<>();

	private final WindowScope opener;
	private final WindowManager winManager;
	
	/**
	 * map which stores holder for {@link BinaryDataSource}s which were
	 * {@link #deliverContent(BinaryDataSource, boolean) delivered} for download.
	 */
	private HashMap<String, ItemHolder> contents;
	
	/**
	 * lazy list of {@link ClientAction}s which trigger the download of an item
	 * formerly added via {@link #deliverContent(BinaryDataSource, boolean)}.
	 */
	private List<ClientAction> downloadActions;
	private HistoryControl browserHistory;
	
	private final long _downloadSurvivingTime;

	private final DownloadModification _downloadModification;

	/** @see #getName() */
	private final ComponentName _name;

	private final TimerControl _timerControl;

	/**
	 * @param opener
	 *        the opener of this window. my be <code>null</code> if this window is the main window
	 * @param windowManager
	 *        the manager which handles windows opened within this window
	 * @param windowModel
	 *        the model of this window
	 */
	public BrowserWindowControl(WindowScope opener, WindowManager windowManager, WindowModel windowModel, ComponentName name) {
		super(windowModel, createCommandMap(INNER_COMMANDS));
		this.opener = opener;
		if (opener == null) {
			_timerControl = new TimerControl();
		} else {
			_timerControl = null;
		}
		this.winManager = windowManager;
		Config config = ApplicationConfig.getInstance().getConfig(Config.class);
		_downloadSurvivingTime = config.getDownloadSurvivingTime();
		_downloadModification = config.getDownloadModification();
		_name = name;
	}

	@Override
	public ComponentName getName() {
		return _name;
	}

	@Override
	public List<DialogWindowControl> getDialogs() {
		return dialogsView;
	}

	@Override
	public List<PopupDialogControl> getPopupDialogs() {
		return Collections.unmodifiableList(popupDialogs);
	}

	@Override
	public DialogWindowControl getActiveDialog() {
		return CollectionUtil.getLast(dialogsView);
	}

	@Override
	public PopupDialogControl getActivePopupDialog() {
		return CollectionUtil.getLast(popupDialogs);
	}

	@Override
	public void openDialog(DialogWindowControl aDialog) {
		if (aDialog == null) {
			throw new IllegalArgumentException("Dialog to open must not be 'null'.");
		}
		
		if (dialogs.contains(aDialog)) {
			Logger.warn("Dialog '" + aDialog + "' is  already open.", this);
			return;
		}

		unregisterAndCloseAllPopupDialogs();

		dialogsToOpen.add(aDialog);
		
		addDialog(aDialog);
		aDialog.initParent(this);
	}

	private void addDialog(DialogWindowControl dialog) {
		disableTopmostDialog(true);
		dialogs.add(dialog);
		// dialogs are in general a point of no return in history
		push();
	}
	
	/** 
	 * @see com.top_logic.layout.WindowScope#openPopupDialog(com.top_logic.layout.structure.PopupDialogControl)
	 */
	@Override
	public void openPopupDialog(PopupDialogControl aPopupDialog) {
		
		if (aPopupDialog == null) {
			throw new IllegalArgumentException("PopupDialog to open must not be 'null'.");
		}
		
		if (popupDialogs.contains(aPopupDialog)) {
			Logger.warn("PopupDialog '" + aPopupDialog + "' is  already open.", this);
			return;
		}

		aPopupDialog.initParent(this);
		
		popupDialogsToOpen.add(aPopupDialog);
		popupDialogs.add(aPopupDialog);		
	}

	/*package protected*/ void unregisterDialog(DialogWindowControl aDialog) {
		if (aDialog == null) {
			throw new IllegalArgumentException("Dialog to close must not be 'null'.");
		}
		
		if (dialogsToOpen.remove(aDialog)) {
			// Cancel opening dialog.
			removeDialog(aDialog);
			return;
		}
		
		if (! dialogs.contains(aDialog)) {
			Logger.warn("Dialog '" + aDialog + "' is not open.", this);
			return;
		}
		
		dialogsToClose.add(aDialog);
		removeDialog(aDialog);
	}

	private void removeDialog(DialogWindowControl dialog) {
		dialogs.remove(dialog);
		dropLayerScope(dialog);
		disableTopmostDialog(false);
		dialog.detach();
		// dialogs are in general points of no return in history
		pop();
	}

	private void disableTopmostDialog(boolean disable) {
		LayoutControl controlToDisable;
		if (dialogs.isEmpty()) {
			controlToDisable = getChildControl();
		} else {
			controlToDisable = getActiveDialog();
		}
		getLayerScope(controlToDisable).disableScope(disable);
	}

	/**
	 * This view is never disabled. Disabling of the content will be updated
	 * hand made.
	 * 
	 * @see com.top_logic.layout.basic.AbstractControlBase#isViewDisabled()
	 */
	@Override
	public boolean isViewDisabled() {
		return false;
	}
	
	/**
	 * Unregisters a popup dialog and mark it to be removed from the client-side view.
	 * 
	 * @param aDialog
	 *        The popup dialog to close.
	 */
	/*package protected*/ void unregisterAndClosePopupDialog(PopupDialogControl aDialog) {
		if (aDialog == null) {
			throw new IllegalArgumentException("Dialog to close must not be 'null'.");
		}
		
		if (popupDialogsToOpen.remove(aDialog)) {
			// Canceled opening dialog.
			popupDialogs.remove(aDialog);
			return;
		}
		
		int popupIndex = popupDialogs.indexOf(aDialog);
		if (popupIndex < 0) {
			// Currently closing all popups (they are already removed from the client-side view,
			// therefore, no close action must be marshalled to the client).
			return;
		}
		
		// Close all popups that are opened "over" the given popup.
		for (int i = popupDialogs.size() - 1; i > popupIndex; i--) {
			popupDialogs.get(i).getModel().setClosed();
		}
		popupDialogs.remove(popupIndex);
		popupDialogsToClose.add(aDialog);
		aDialog.detach();
	}
	
	@Override
	protected void beforeRendering(DisplayContext context) {
		super.beforeRendering(context);

		unregisterAllPopupDialogs();
	}

	/**
	 * Unregisters all open popup dialogs (they are already removed from the client-side view).
	 */
	/*package protected*/ final void unregisterAllPopupDialogs() {
		for (int i = popupDialogs.size() - 1; i >= 0; i--) {
			// Note: The popup is removed from the currently open popups fist: This prevents
			// marshalling a close request back to the client, because the popup is already
			// removed from the client-side view.
			PopupDialogControl popup = popupDialogs.remove(i);
			close(popup);			
		}
	}
	
	/**
	 * This method unregisters a specified open popup dialog
	 */
	/*package protected*/ final void unregisterSinglePopupDialog(String popupID) {
		int size = popupDialogs.size();
		for(int i = 0; i < size; i++) {
			PopupDialogControl popup = popupDialogs.get(i);
			if(popup.getID().equals(popupID)) {
				// Note: The popup is removed from the currently open popups fist: This prevents
				// marshalling a close request back to the client, because the popup is already
				// removed from the client-side view.
				popupDialogs.remove(i);
				close(popup);
				return;
			}
		}
		
		{
			Logger.error("Could not unregister popup dialog with id '" + popupID +
						 "', due to the popup dialog is not registered!", BrowserWindowControl.class);
		}
	}
	
	private void close(PopupDialogControl popup) {
		popup.getPopupDialogModel().setClosed();
		popup.detach();
	}

	/**
	 * This method unregisters all open popup dialogs, and marks them as removable from the gui
	 *
	 */
	private final void unregisterAndCloseAllPopupDialogs() {
		for (int i = popupDialogs.size() - 1; i >= 0; i--) {
			close(popupDialogs.get(i));
		}
		assert popupDialogs.isEmpty() : "All popups must have been closed.";
	}

	/**
	 * This method brings the visible popup dialogs in sync with the popup dialogs which were opened (and
	 * closed, resp.) on server side.
	 * 
	 * @param actions
	 *            may be <code>null</code>. If <code>actions != null</code>
	 *            {@link ClientAction} to bring the client in sync will be added to.
	 */
	protected void updatePopupDialogs(UpdateQueue actions) {
		// Closed popup dialogs
		int size = popupDialogsToClose.size();
		for (int i = 0; i < size; i++) {
			actions.add(new ElementReplacement(popupDialogsToClose.get(i).getID(), Fragments.empty()));
		}

		// Opened popup dialogs
		size = popupDialogsToOpen.size();
		for (int i = 0; i < size; i++) {
			final PopupDialogControl popup = popupDialogsToOpen.get(i);
			actions.add(new FragmentInsertion(POPUP_DIALOG_ANCHOR, AJAXConstants.AJAX_POSITION_END_VALUE, popup));
			actions.add(new JSSnipplet(new AbstractDisplayValue() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					popup.writeRenderingCommand(context, out);
				}
			}));
		}

		// Toggling popup dialog pane
		boolean noDialogsVisibleBefore =
				popupDialogsToClose.isEmpty() && (popupDialogs.size() == popupDialogsToOpen.size());
		boolean noDialogsVisibleAfter = popupDialogs.isEmpty();

		boolean dialogsVisibleAfter = !noDialogsVisibleAfter;
		if (noDialogsVisibleBefore && dialogsVisibleAfter) {
			actions.add(new PropertyUpdate(POPUP_DIALOG_PANE, CLASS_PROPERTY,
				new ConstantDisplayValue(POPUP_DIALOG_OPENED_CLASS)));
		}

		boolean dialogsVisibleBefore = !noDialogsVisibleBefore;
		if (dialogsVisibleBefore && noDialogsVisibleAfter) {
			actions.add(new PropertyUpdate(POPUP_DIALOG_PANE, CLASS_PROPERTY,
				new ConstantDisplayValue(NO_POPUP_DIALOG_OPENED_CLASS)));
		}
		
		// Clear update markers
		clearPopups();
	}

	private void clearPopups() {
		popupDialogsToOpen.clear();
		popupDialogsToClose.clear();
	}

	@Override
	protected void handleRepaintRequested(UpdateQueue actions) {
		super.handleRepaintRequested(actions);

		dropUpdates();
		clearPopups();
	}

	/**
	 * This method brings the visible dialogs in sync with the dialogs which
	 * were opened (and closed, resp.) on server side.
	 * 
	 * @param context
	 *        the context in which revalidating occurs
	 * @param actions
	 *        may be <code>null</code>. If <code>actions != null</code>
	 *        {@link ClientAction} to bring the client in sync will be added to.
	 */
	protected void updateDialogs(DisplayContext context, UpdateQueue actions) {
		
		for (int index = 0, size = dialogsToClose.size(); index < size; index++) {
			DialogWindowControl dialog = dialogsToClose.get(index);
			addClosingActions(dialog, actions);
		}
		
		for (int index = 0, size = dialogsToOpen.size(); index < size; index++) {
			DialogWindowControl dialog = dialogsToOpen.get(index);
			addOpeningActions(dialog, actions);
		}

		boolean noDialogsVisibleBefore = dialogsToClose.isEmpty() && (dialogs.size() == dialogsToOpen.size());
		boolean noDialogsVisibleAfter = dialogs.isEmpty();
		
		boolean dialogsVisibleAfter = !noDialogsVisibleAfter;
		if (noDialogsVisibleBefore && dialogsVisibleAfter) {
			actions.add(new PropertyUpdate(DIALOG_ANCHOR, CLASS_PROPERTY, new ConstantDisplayValue(DIALOG_OPENED_CLASS)));
		}
		
		boolean dialogsVisibleBefore = !noDialogsVisibleBefore;
		if (dialogsVisibleBefore && noDialogsVisibleAfter) {
			actions.add(new PropertyUpdate(DIALOG_ANCHOR, CLASS_PROPERTY, new ConstantDisplayValue(NO_DIALOG_OPENED_CLASS)));
		}
		
		dropUpdates();
		
		for (DialogWindowControl dialog: dialogsView) {
			if (dialog.isInvalid()) {
				context.validateScoped(getLayerScope(dialog), ControlValidator.INSTANCE, actions, dialog);
			}
		}
	}

	private void dropUpdates() {
		dialogsToClose.clear();
		dialogsToOpen.clear();
	}

	/**
	 * This method adds actions depending on <code>context</code> to <code>actions</code> for
	 * initial bringing the Dialog to the GUI.
	 */
	private void addOpeningActions(final DialogWindowControl dialog, UpdateQueue actions) {
		actions.add(new FragmentInsertion("dlgDialogs", AJAXConstants.AJAX_POSITION_END_VALUE, new HTMLFragment() {
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				context.renderScoped(getLayerScope(dialog), FragmentRenderer.INSTANCE, out, dialog);
			}
		}));
		actions.add(new JSSnipplet(new AbstractDisplayValue() {
			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				dialog.writeRenderingCommand(context, out);
			}
		}));
	}
	 
	/**
	 * This method adds actions for removing the dialog from the GUI to <code>actions</code>.
	 */
	private void addClosingActions(DialogWindowControl dialog, UpdateQueue actions) {
		actions.add(new ElementReplacement(dialog.getID(), Fragments.empty()));
	}

	/**
	 * This method returns the command for lay out this {@link BrowserWindowControl}.
	 */
	public final void writeRenderingCommand(DisplayContext context, TagWriter out) throws IOException {
		out.append("services.layout.initialLayout('");
		out.append(this.getID());
		out.append("');");
		for (int index = 0, size = dialogs.size(); index < size ; index++) {
			out.append('\n');
			dialogs.get(index).writeRenderingCommand(context, out);
		}
	}

	private boolean isIE9(DisplayContext context) {
		return context.getUserAgent().is_ie() && context.getUserAgent().is_ie9up()
			&& !context.getUserAgent().is_ie10up();
	}

	@Override
	protected boolean hasUpdates() {
		
		// as the child is not rendered in the localScope of AbstractControlBase
		// a the global procedure does not check its validation.
		if (getChildControl().isInvalid()) {
			return true;
		}
		
		// Lookup for updates in dialogs
		for(int i = 0; i < dialogs.size(); i++) {
			if (dialogs.get(i).isInvalid()) {
				return true;
			}
		}
		
		// Lookup for updates in popup dialogs
		for(int i = 0; i < popupDialogs.size(); i++) {
			if (popupDialogs.get(i).isInvalid()) {
				return true;
			}
		}
		
		if (!dialogsToClose.isEmpty() || !dialogsToOpen.isEmpty() || !popupDialogsToClose.isEmpty() || !popupDialogsToOpen.isEmpty()) {
			return true;
		}
		
		if (hasDownloads()) {
			return true;
		} else {
			checkDownloadLifetime();
		}
		return super.hasUpdates();
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		super.internalRevalidate(context, actions);

		LayoutControl child = getChildControl();
		if (child.isInvalid()) {
			context.validateScoped(getLayerScope(child), ControlValidator.INSTANCE, actions, (AbstractControlBase) child);
		}

		updateDialogs(context, actions);
		updatePopupDialogs(actions);
		if (hasDownloads()) {
			for (int index = 0, size = downloadActions.size(); index < size; index++) {
				actions.add(downloadActions.get(index));
			}
			downloadActions.clear();
		}
	}
	
	/**
	 * Determines whether currently downloads are present.
	 * 
	 * @return <code>true</code> iff this control needs to revalidate 
	 */
	private boolean hasDownloads() {
		return downloadActions != null && !downloadActions.isEmpty();
	}

	/**
	 * Checks which downloads can be removed and removes them.
	 */
	private void checkDownloadLifetime() {
		synchronized (this) {
			if (contents == null) {
				return;
			}
			
			final int remainingDownloads = contents.size();
			if (remainingDownloads == 0) {
				return;
			}
			final long now = System.currentTimeMillis();
			Iterator<Entry<String, ItemHolder>> it = contents.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, ItemHolder> entry = it.next();
				final ItemHolder holder = entry.getValue();
				final long lastAccess = holder.lastAccess;
				if (lastAccess == ItemHolder.UNTOUCHED) {
					holder.lastAccess = now;
				} else {
					if (now - lastAccess > _downloadSurvivingTime) {
						it.remove();
					}
				}
			}
		}
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();
		/*
		 * drop updates is not performed in detachInvalidated() since after
		 * requestRepaint() dialogs can still be opened
		 */
		dropUpdates();
	}
	
	@Override
	protected void detachInvalidated() {
		/* Detach all dialogs. That does not happens automatically since they don't live in the
		 * scope spanned by this control. */
		for (Control control : layerScopes.keySet()) {
			control.detach();
		}

		super.detachInvalidated();
	}
	
	@Override
	protected void internalAttach() {
		super.internalAttach();
//		disableContentControl(!dialogsView.isEmpty());
		getScope().getFrameScope().registerContentHandler("downloads", this);
	}
	
	@Override
	protected void internalDetach() {
		getScope().getFrameScope().deregisterContentHandler(this);

		super.internalDetach();
	}	
	
	/**
	 * Writes the content in an separate {@link ControlScope} to be
	 * {@link #isViewDisabled() view disabled} independent of the
	 * browserWindowControl.
	 * 
	 * @see com.top_logic.layout.structure.WrappingControl#writeChildControl(com.top_logic.layout.DisplayContext,
	 *      com.top_logic.basic.xml.TagWriter)
	 */
	@Override
	public void writeChildControl(DisplayContext context, TagWriter out) throws IOException {
		LayoutControl child = getChildControl();
		context.renderScoped(getLayerScope(child), FragmentRenderer.INSTANCE, out, child);
	}
	
	@Override
	public LayoutControl setChildControl(LayoutControl aLayoutControl) {
		LayoutControl old = super.setChildControl(aLayoutControl);
		if (old != null) {
			dropLayerScope(old);
		}
		return old;
	}

	public void writeDialogs(DisplayContext context, TagWriter out) throws IOException {
		int size = dialogsView.size();

		// Write anchor for dialogs.
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, DIALOG_ANCHOR);
		String theClass;
		if (size > 0) {
			theClass = DIALOG_OPENED_CLASS;
		} else {
			theClass = NO_DIALOG_OPENED_CLASS;
		}
		out.writeAttribute(CLASS_ATTR, theClass);
		out.endBeginTag();
		{
			for (int index = 0; index < size; index++) {
				final DialogWindowControl currentDialog = dialogsView.get(index);
				context.renderScoped(getLayerScope(currentDialog), FragmentRenderer.INSTANCE, out, currentDialog);
			}
		}
		out.endTag(DIV);
	}

	/**
	 * Returns a {@link ControlScope} to render a {@link Control}.
	 * 
	 * @param control
	 *        the control for which the {@link ControlScope} is requested.
	 */
	private ControlScope getLayerScope(Control control) {
		ControlScope result = layerScopes.get(control);
		if (result != null) {
			return result;
		}

		ControlScope newScope = new ContentScope();
		layerScopes.put(control, newScope);
		return newScope;
	}

	private void dropLayerScope(Control control) {
		layerScopes.remove(control);
	}

	@Override
	public WindowScope getOpener() {
		return opener;
	}
	
	@Override
	public FrameScope getTopLevelFrameScope() {
		return getScope().getFrameScope();
	}

	@Override
	public <T extends Appendable> T appendReference(T out, WindowScope openedWindow) throws IOException {
		if (winManager != null) {
			return winManager.addWindowReference(out, openedWindow);
		}
		return out;
	}
	
	@Override
	public <T extends Appendable> T appendCloseAllWindowsCommand(T out, FrameScope source) throws IOException {
		if (winManager != null) {
			winManager.appendCloseAllWindows(out, source);
		}
		return out;
	}
	
	@Override
	public void deliverContent(BinaryDataSource data, final boolean showInline) {
		deliver(showInline, preprocess(showInline, data));
	}

	private BinaryDataSource preprocess(final boolean showInline, BinaryDataSource source) {
		if (source instanceof BinaryData) {
			BinaryData data = _downloadModification.modifyDownload((BinaryData) source, showInline);

			// Touch data to be able to report errors during command processing. During download, no
			// error reporting is possible.
			data = pingStream(data);

			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordDownload(data, showInline);
			}
			return data;
		} else {
			return source;
		}
	}

	private void deliver(final boolean showInline, BinaryDataSource item) {
		final ItemHolder holder = new ItemHolder(item);
		
		final String dataKey = getFrameScope().createNewID();
		/*
		 * Actually no synchronized is necessary as either in command context
		 * there is no concurrent thread and when currently a download occur no
		 * command can be executed.
		 */
		synchronized (this) {
			if (contents == null) {
				contents = new HashMap<>();
			}
			contents.put(dataKey, holder);
		}
		if (downloadActions == null) {
			downloadActions = new ArrayList<>();
		}
		
		String contentType = holder.item.getContentType();
		if (showInline) {
			// Opens a new Browser window in which the content will be displayed
			downloadActions.add(new JSSnipplet(new AbstractDisplayValue() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					// openBrowserWindow declared in util.js
					out.append("openBrowserWindow('TopLogic','");
					out.append(
						BrowserWindowControl.this.getDownloadURL(context, dataKey, contentType, true));
					out.append("');");
				}
			}));
		} else {
			/* Inserts a DIV into which the javascript installs the IFRAME which offers the content
			 * for download. */
			downloadActions.add(new FragmentInsertion(getID(), AJAXConstants.AJAX_POSITION_END_VALUE, new HTMLFragment() {
				
				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					out.beginBeginTag(DIV);
					out.writeAttribute(ID_ATTR, dataKey);
					out.endBeginTag();
					out.endTag(DIV);
				}
			}));
			downloadActions.add(new JSSnipplet(new AbstractDisplayValue() {
				
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					out.append("BAL.requestDownload('");
					out.append(dataKey);
					out.append("', '");
					out.append(
						BrowserWindowControl.this.getDownloadURL(context, dataKey, contentType, false));
					out.append("')");
				}
			}));
		}
	}
	
	private static BinaryData pingStream(final BinaryData dataItem) throws TopLogicException {
		try {
			class EagerBinaryData extends BinaryDataProxy {

				private final BinaryData _data;

				private InputStream _initialStream;

				public EagerBinaryData(BinaryData data) throws IOException {
					super(data);
					_data = data;
					_initialStream = data.getStream();
				}

				@Override
				public InputStream getStream() throws IOException {
					if (_initialStream != null) {
						InputStream result = _initialStream;
						_initialStream = null;
						return result;
					} else {
						return super.getStream();
					}
				}

				@Override
				protected BinaryData createBinaryData() throws IOException {
					return _data;
				}

				@Override
				public String getName() {
					return _data.getName();
				}

			}
			BinaryDataProxy dataProxy = new EagerBinaryData(dataItem);
			return new DefaultDataItem(dataItem.getName(), dataProxy, dataItem.getContentType());
		} catch (IOException ex) {
			throw new TopLogicException(BrowserWindowControl.class, "dataNotAvailable", ex);
		}
	}

	/**
	 * Creates an URL which maps to the given {@link BinaryDataSource} stored under the given key.
	 * 
	 * @param dataKey
	 *        the key under which the {@link BinaryDataSource} can be found.
	 * @param contentType
	 *        The announced content type of the transmitted data.
	 * @param showInline
	 *        whether the file should be shown inline
	 * 
	 * @see #deliverContent(BinaryDataSource, boolean)
	 */
	String getDownloadURL(DisplayContext context, String dataKey, String contentType, boolean showInline) {
		final URLBuilder urlBuilder = getScope().getFrameScope().getURL(context, this);
		urlBuilder.addResource(dataKey);
		urlBuilder.addResource(String.valueOf(showInline));

		/* The context is annotated as keep downloads. */
		if (context.get(KEEP_DOWNLOADS)) {
			urlBuilder.addResource(String.valueOf(true));
		}
		
		/*
		 * Prevent compressing ZIP downloads twice. Compressing a ZIP download
		 * results in a gzip compressed ZIP file, if downloading with IE 8,
		 * since IE8 seems not to revert the additional gzip compression for ZIP
		 * files. This problem only occures, if the "alwaysZip" option is set
		 * for the servlet. see #2691
		 */
		if (isCompressed(contentType)) {
			urlBuilder.appendParameter(CompressionFilter.GZIP, "false");
		}
		return urlBuilder.getURL();
	}
	
	/**
	 * Determines whether the stream is already zipped.
	 * 
	 * @param contentType
	 *        The announced content type of the transmitted data.
	 */
	private boolean isCompressed(String contentType) {
		return COMPRESSED_FORMATS.contains(contentType);
	}
	
	/**
	 * Copies the data of the file formerly added via
	 * {@link #deliverContent(BinaryDataSource, boolean)} into the response of the given
	 * {@link DisplayContext}.
	 * 
	 * @param context
	 *        {@link DisplayContext} which wraps the {@link DisplayContext#asRequest() request} and
	 *        {@link DisplayContext#asResponse() response}.
	 * @throws IOException
	 *         If delivering the contents of the {@link BinaryDataSource} fails.
	 */
	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException {
		final HttpServletResponse response = context.asResponse();

		final String requestedData = url.removeResource();
		final boolean showInline = Boolean.parseBoolean(url.removeResource());
		boolean keepDownload = showInline;
		if (!url.isEmpty()) {
			keepDownload |= Boolean.parseBoolean(url.removeResource());
		}

		final BinaryDataSource dataItem = getDataItem(requestedData, !keepDownload);
		if (dataItem == null) {
			ErrorPage.showPage(context, "downloadNotAvailable");
			return;
		}
		
		// mantra to ensure that the browser does not open the file in the frame
		if (showInline) {
			response.setHeader("Content-Disposition", "inline; filename=\"" + dataItem.getName() + "\"");
		} else {
			setContentDispositionHeader(response, dataItem.getName());
		}
		CacheControl.setNoCache(response);
		
		response.setContentType(dataItem.getContentType());
		
		long size = dataItem.getSize();
		if (size > 0) {
			response.setContentLengthLong(size);
		}
		
		dataItem.deliverTo(response.getOutputStream());
	}

	private void setContentDispositionHeader(final HttpServletResponse response, String name)
			throws UnsupportedEncodingException {
		// The "filename*" property allows to set the character set used.
		StringBuilder buffer = new StringBuilder("attachment; filename*=UTF-8''");

		// Encode the name as hexadecimal values of utf-8 code points to consistently support
		// non-ASCII characters in the file name, see https://tools.ietf.org/html/rfc5987.
		char[] encodedName = Hex.encodeHex(name.getBytes("UTF-8"));
		for (int i = 0; i < encodedName.length; i += 2) {
			buffer.append("%");
			buffer.append(encodedName[i]);
			buffer.append(encodedName[i + 1]);
		}
		response.setHeader("Content-Disposition", buffer.toString());
	}
	
	/**
	 * Returns the item stored in {@link #contents} under the given key and, if
	 * required, removes it.
	 * 
	 * @param dataKey
	 *        the index of the slot whose item is demanded
	 * @param remove
	 *        whether the item should be removed
	 */
	private BinaryDataSource getDataItem(String dataKey, boolean remove) {
		// must synchronized as many downloads can happen concurrently
		final ItemHolder itemHolder;
		synchronized (this) {
			if (remove) {
				itemHolder = contents.remove(dataKey);
			} else {
				itemHolder = contents.get(dataKey);
			}
		}
		if (itemHolder == null) {
			return null;
		}
		if (!remove) {
			itemHolder.lastAccess = System.currentTimeMillis();
		}
		return itemHolder.item;
	}

	/**
	 * Scope which is used to render the relevant content of an
	 * {@link BrowserWindowControl}, i.e. a {@link DialogWindowControl} or the
	 * {@link BrowserWindowControl#getChildControl() content of the browser}.
	 * 
	 * It can only have one listener.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private class ContentScope implements ControlScope {

		private UpdateListener listener;
		private boolean disabled;

		ContentScope() {
			// Default constructor.
			// Can not pass needed FrameScope in constructor as the FrameScope must not be present
			// at call time.
		}

		@Override
		public void addUpdateListener(UpdateListener aListener) {
			assert this.listener == null || this.listener == aListener : "Adding a listener whereas already a different was added.";
			this.listener = aListener;
		}

		@Override
		public void disableScope(boolean disable) {
			if (disabled == disable) {
				return;
			}
			this.disabled = disable;
			if (this.listener != null) {
				this.listener.notifyDisabled(disable);
			}
		}

		@Override
		public FrameScope getFrameScope() {
			return BrowserWindowControl.this.getScope().getFrameScope();
		}

		@Override
		public boolean isScopeDisabled() {
			return disabled;
		}

		@Override
		public boolean removeUpdateListener(UpdateListener aListener) {
			assert this.listener == null || this.listener == aListener : "Remove a listener which was not added";
			this.listener = null;
			return true;
		}
	}
	
	/**
	 * Holder of a {@link BinaryDataSource} and a time at which the last access to that
	 * {@link BinaryDataSource} occurred.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static final class ItemHolder {
		
		static final long UNTOUCHED = -1;
		
		final BinaryDataSource item;

		long lastAccess = UNTOUCHED;
		
		public ItemHolder(BinaryDataSource data) {
			this.item = data;
		}
	}

	public void writeHistoryFrame(DisplayContext context, TagWriter out) throws IOException {
		if (browserHistory == null) {
			if (context.getUserAgent().is_firefox()) {
				/*
				 * Seems to be an absurdity of FF when using browser back
				 * button. It does not only switch back the history frame but
				 * also the frame currently visible to a different content. A
				 * history size of 0 ensures that after each browser back the
				 * logout warning arises.
				 */
				browserHistory = new HistoryControl(0); 
			} else {
				browserHistory = new HistoryControl();
			}

			for (int i = 0, cnt = dialogsView.size(); i < cnt; i++) {
				push();
			}
		}
		browserHistory.write(context, out);
	}

	@Override
	public void addHistory(HistoryEntry entry) {
		if (browserHistory != null) {
			browserHistory.addHistory(entry);
		}
	}
	
	@Override
	public void push() {
		if (browserHistory != null) {
			browserHistory.push();
		}
	}
	
	@Override
	public void pop() {
		if (browserHistory != null) {
			browserHistory.pop();
		}
	}

	@Override
	public ScheduledExecutorService getUIExecutor() {
		if (_timerControl == null) {
			throw new UnsupportedOperationException("Only application windows have an executor service.");
		}
		return _timerControl.getExecutor();
	}

	/**
	 * The {@link TimerControl} to render, or <code>null</code>, if this window has no timer.
	 */
	public TimerControl getTimerControl() {
		return _timerControl;
	}

	@Override
	public HandlerResult dispatchKeyEvent(DisplayContext commandContext, KeyEvent event) {
		switch (event.getKeyCode()) {
			case ESCAPE: {
				if (!popupDialogs.isEmpty()) {
					getActivePopupDialog().getPopupDialogModel().setClosed();
					return HandlerResult.DEFAULT_RESULT;
				} else {
					if (dialogs.isEmpty()) {
						return dispatchEvent(commandContext, LayoutComponent::getCancelCommand, getChildControl());
					} else {
						final DialogModel dialogModel = getActiveDialog().getDialogModel();
						if (dialogModel.hasCloseButton()) {
							return dialogModel.getCloseAction().executeCommand(commandContext);
						} else {
							return HandlerResult.DEFAULT_RESULT;
						}
					}
				}
			}

			case RETURN: {
				if (!popupDialogs.isEmpty()) {
					unregisterAndCloseAllPopupDialogs();
				}
				if (dispatchEnter(event)) {
					if (dialogs.isEmpty()) {
						return dispatchEvent(commandContext, LayoutComponent::getDefaultCommand, getChildControl());
					} else {
						return dispatchEvent(commandContext, LayoutComponent::getDefaultCommand, getActiveDialog());
					}
				} else {
					return HandlerResult.DEFAULT_RESULT;
				}
			}

			default: {
				return HandlerResult.DEFAULT_RESULT;
			}
		}
	}

	private boolean dispatchEnter(KeyEvent event) {
		ApplicationConfig applicationConfig = ApplicationConfig.getInstance();
		KeyEventConfiguration keyEventConfig = applicationConfig.getConfig(KeyEventConfiguration.class);
		if (keyEventConfig.getNoDefaultActionOnEnter()) {
			boolean hasModifiers = event.hasCtrlModifier() || event.hasAltModifier() || event.hasShiftModifier();
			return hasModifiers;
		}
		return true;
	}

	private HandlerResult dispatchEvent(DisplayContext commandContext,
			Function<LayoutComponent, CommandHandler> commandProvider, LayoutControl control) {
		if (control instanceof DeckPaneControl) {
			// Only the active child is relevant.
			LayoutControl activeChild = ((DeckPaneControl) control).getActiveChild();
			if (activeChild != null) {
				HandlerResult result = dispatchEvent(commandContext, commandProvider, activeChild);
				if (!result.isSuccess()) {
					return result;
				}
			}
		} else {
			for (LayoutControl child : control.getChildren()) {
				HandlerResult result = dispatchEvent(commandContext, commandProvider, child);
				if (!result.isSuccess()) {
					return result;
				}
			}
		}

		if (control instanceof ContentControl) {
			LayoutComponent component = ((ContentControl) control).getBusinessComponent();
			CommandHandler handler = commandProvider.apply(component);
			if (handler != null) {
				Map<String, Object> noArgs = Collections.emptyMap();
				if (!CommandDispatcher.resolveExecutableState(handler, component, noArgs).isExecutable()) {
					// Prevent error, if command temporarily not executable.
					return HandlerResult.DEFAULT_RESULT;
				}
				HandlerResult result =
					CommandDispatcher.getInstance().dispatchCommand(handler, commandContext, component);
				if (!result.isSuccess()) {
					return result;
				}
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Access to {@link BrowserWindowControl} internals for testing only.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class Internal {
		/**
		 * Drops all pending downloads.
		 */
		@SuppressWarnings("synthetic-access")
		public static void clearDownloads(BrowserWindowControl scope) {
			HashMap<String, ItemHolder> pendingDownloads = scope.contents;
			pendingDownloads.clear();
		}

		/**
		 * Fetches the (unique) download with the given name.
		 */
		@SuppressWarnings("synthetic-access")
		public static BinaryDataSource getDownload(BrowserWindowControl scope, String name) {
			BinaryDataSource result = null;

			HashMap<String, ItemHolder> pendingDownloads = scope.contents;
			for (Entry<String, ItemHolder> download : pendingDownloads.entrySet()) {
				if (name.equals(download.getValue().item.getName())) {
					if (result != null) {
						throw new IllegalStateException("Ambigouos download name: " + name);
					}
					result = scope.getDataItem(download.getKey(), false);
				}
			}

			return result;
		}

		/**
		 * Ensure that the downloads are not dropped after they are fetched. It must be ensured that
		 * the downloads are cleared later.
		 * 
		 * @see #clearDownloads(BrowserWindowControl)
		 */
		@SuppressWarnings("synthetic-access")
		public static final void keepDownloads(DisplayContext context) {
			context.set(BrowserWindowControl.KEEP_DOWNLOADS, Boolean.TRUE);
		}

		/**
		 * Fetches the single download.
		 */
		@SuppressWarnings("synthetic-access")
		public static BinaryDataSource getUniqueDownload(BrowserWindowControl scope) {
			HashMap<String, ItemHolder> pendingDownloads = scope.contents;
			switch (pendingDownloads.size()) {
				case 0:
					return null;
				case 1:
					return scope.getDataItem(pendingDownloads.keySet().iterator().next(), false);
				default:
					throw new IllegalStateException("More than one download. Could not determine unique download.");
			}
		}
	}

	@Override
	protected ControlRenderer<BrowserWindowControl> createDefaultRenderer() {
		return BrowserWindowRenderer.INSTANCE;
	}

	@Override
	public Scrolling getScrolling() {
		return null;
	}

	@Override
	public BrowserWindowControl self() {
		return this;
	}
}
