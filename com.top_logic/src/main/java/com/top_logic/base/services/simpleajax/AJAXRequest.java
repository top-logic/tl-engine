/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.mig.html.layout.ComponentName;


/**
 * Object representation of a SOAP request message sent by an AJAX enabled
 * component.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AJAXRequest extends CommandRequest {

	/** list of commands to execute */
	List<CommandRequest> commands = Collections.<CommandRequest> singletonList(this);

	/**
	 * @see #getTxSequence()
	 */
	Integer txSequence;

	List<Integer> _acks = new ArrayList<>(5);

	/** @see #getSource() */
	ComponentName source;

	/** @see #getWindow() */
	String window;

	/**
	 * The sequence number transmitted together with this request.
	 */
	public Integer getTxSequence() {
		assert hasTxSequence();
		return txSequence;
	}

	/**
	 * Whether a sequence number is associated with the transmission of
	 *     this {@link AJAXRequest}.
	 */
	public boolean hasTxSequence() {
		return txSequence != null;
	}

	/**
	 * Sequence numbers of request that have been acknowledged by the client.
	 */
	public List<Integer> getAcks() {
		return _acks;
	}

	/**
	 * ID of the component that send the request.
	 */
	ComponentName getSource() {
		return source;
	}

	/**
	 * The client-side window name from which the request was transmitted.
	 */
	public String getWindow() {
		return window;
	}

	/**
	 * All commands that are requested with this {@link AJAXRequest}
	 */
	public List<CommandRequest> getCommands() {
		return commands;
	}

	void addCommand(CommandRequest requestedCommand) {
		if (requestedCommand == this) {
			return;
		}
		if (commands.size() == 1) {
			commands = new ArrayList<>();
			commands.add(this);
		}
		commands.add(requestedCommand);
	}
}