/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task;

import static com.top_logic.util.SystemPropertyUtil.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;


/**
 * The name of the local cluster node that should be used in the scheduler gui.
 * <p>
 * Is also used by the {@link Task}s to identify the node they are running on. <br/>
 * <b>Has to be unique between all cluster nodes.</b> Should stay the same if the node is restarted.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class SystemPropertyTaskClusterNodeName {

	/**
	 * Hard code the name to ensure it keeps it's value if the class is renamed. If the programmer
	 * forgets to adapt this property, the check in the static initializer block will fail. That
	 * will remind the programmer to adapt the users of this class. (The scripts starting
	 * <i>TopLogic</i>.)
	 */
	private static final String NAME =
		"com.top_logic.util.sched.task.SystemPropertyTaskClusterNodeName";

	private static final String DEFAULT = getInnermostHostNamePart();

	/** The value of this system property. */
	public static final String VALUE;

	static {
		checkStartsWithCanonicalClassName(NAME, SystemPropertyTaskClusterNodeName.class);
		VALUE = System.getProperty(NAME, DEFAULT);
	}

	private static String getInnermostHostNamePart() {
		try {
			String nodeIdentifier = InetAddress.getLocalHost().getHostName();
			int dotIndex = nodeIdentifier.indexOf('.');
			if (dotIndex > 0) {
				return nodeIdentifier.substring(0, dotIndex);
			}
			return nodeIdentifier;
		} catch (UnknownHostException exception) {
			boolean isDefaultNeeded = StringServices.isEmpty(System.getProperty(NAME));
			if (isDefaultNeeded) {
				String message = "Failed to retrieve local host name. Cluster node names are not unique.";
				Logger.error(message, exception, SystemPropertyTaskClusterNodeName.class);
			} else {
				String message = "Failed to retrieve local host name.";
				Logger.info(message, exception, SystemPropertyTaskClusterNodeName.class);
			}
			return "[No Cluster Node Name Available]";
		}
	}

}
