/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.util.Date;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.export.ExportQueueManager.ExportExecutor;

/**
 * {@link Export} describes both the process and the result of an
 * {@link ExportHandler} exporting a model.
 * 
 * Since an {@link Export} is stateful, getters will return different values in
 * different {@link State}s.
 * 
 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public interface Export {

	/**
	 * Separator between {@link #getDisplayNameKey()} and
	 * {@link #getFileExtension()}
	 */
	public static final String EXTENSION_SEPARATOR = ".";

	/**
	 * The {@link State} defines the state of the {@link Export}s process.
	 * 
	 * <ul>
	 * <li>{@link #INITIALIZED}: The export has never been done.</li>
	 * <li>{@link #QUEUED}: The {@link Export} is queued and waits for execution.</li>
	 * <li>{@link #RUNNING}: The {@link Export} is currently performed.</li>
	 * <li>{@link #FINISHED}: The last {@link Export} run was successful.</li>
	 * <li>{@link #FAILED}: The last {@link Export} run ended with a failure.</li>
	 * </ul>
	 * 
	 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
	 *          2016) $
	 */
	public enum State {
		INITIALIZED, QUEUED, RUNNING, FINISHED, FAILED
	}

	/**
	 * Return the {@link Document} of this {@link Export}. The {@link Document}
	 * returned is always the result of the last {@link Export} ended with
	 * {@link State#FINISHED}
	 * 
	 * The Document may be null, if there were no successful export run yet.
	 */
	Document getDocument();

	/**
	 * Return the model this exports refers to.
	 */
	Object getModel();

	/**
	 * Return the {@link Person} that triggered this exports. This attribute
	 * will be <code>null</code> if {@link ExportHandler#isPersonalized()} is
	 * false.
	 */
	Person getPerson();

	/**
	 * Return the ID of the {@link ExportHandler} of this {@link Export}
	 */
	String getExportHandlerID();

	/**
	 * Time when this {@link Export} entered {@link State#QUEUED}.
	 */
	Date getTimeQueued();

	/**
	 * Time when this {@link Export} entered {@link State#FINISHED}
	 */
	Date getTimeFinished();

	/**
	 * Time when this {@link Export} entered {@link State#FAILED}
	 */
	Date getTimeFailed();

	/**
	 * Time when this {@link Export} entered {@link State#RUNNING}
	 */
	Date getTimeRunning();

	/**
	 * Return the {@link State} of this {@link Export}s process.
	 */
	State getState();

	/**
	 * Return a resource key that describes the cause of the failure of this
	 * export. In {@link State#FAILED} this key must not be <code>null</code>.
	 * In all other {@link State}s this key will be <code>null</code>.
	 */
	ResKey getFailureKey();

	/**
	 * Return a resource key used to display and download the
	 * {@link Export#getDocument()}. In {@link State#INITIALIZED} this method
	 * will return <code>null</code>. In all other {@link State}s the key must
	 * not be <code>null</code>.
	 * <p>
	 * <b>Note:</b> This key must not be used for persistency issues of
	 * {@link #getDocument()}
	 * </p>
	 */
	ResKey getDisplayNameKey();

	/**
	 * Return the file extension used to display and download
	 * {@link #getDocument()}.
	 * 
	 * <p>
	 * <b>Note:</b> In difference to {@link #getDisplayNameKey()} this extension
	 * will be added to a {@link Document}s names.
	 * </p>
	 */
	String getFileExtension();

	/**
	 * Set this {@link Export} into {@link State#QUEUED}.
	 * 
	 * @throws ExportFailure
	 *         If access to the {@link ExportRegistry} fails.
	 */
	boolean setStateQueued() throws ExportFailure;

	/**
	 * Set this {@link Export} into {@link State#RUNNING}.
	 * 
	 * @throws ExportFailure
	 *         If access to the {@link ExportRegistry} fails.
	 */
	boolean setStateRunning() throws ExportFailure;

	/**
	 * Set this {@link Export} into {@link State#FINISHED}
	 */
	boolean setStateFinished(Document theDoc, ResKey aDisplayNameKey, String aFileExtension);

	/**
	 * Set this {@link Export} into {@link State#FAILED}
	 */
	boolean setStateFailed(ResKey theFailureKey);

	/**
	 * Return the technology this export is using. The technology is one
	 * category used by the {@link ExportQueueManager} to enqueue this export
	 * into an {@link ExportExecutor}.
	 * 
	 * @see ExportHandler#getExportTechnology()
	 */
	String getExportTechnology();

	/**
	 * Return the expected duration of this export. The duration is one category
	 * used by the {@link ExportQueueManager} to enqueue this export into an
	 * {@link ExportExecutor}.
	 * 
	 * @see ExportHandler#getExportDuration()
	 */
	String getExportDuration();
}