/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import com.top_logic.mail.base.MailServer;

/**
 * {@link LoginInfo} for {@link MailServer}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class LoginInfo {
	
	final String _server;

	final String _user;

	final int _port;

	LoginInfo(String server, String user, int port) {
		_server = server;
		_user = user;
		_port = port;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _port;
		result = prime * result + _server.hashCode();
		result = prime * result + _user.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LoginInfo other = (LoginInfo) obj;
		if (!_server.equals(other._server))
			return false;
		if (!_user.equals(other._user))
			return false;
		if (_port != other._port)
			return false;
		return true;
	}

}
