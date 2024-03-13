/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.person;

import com.top_logic.base.user.UserInterface;
import com.top_logic.base.user.UserService;

/**
 * Factory for {@link UserInterface}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
final class MockUserInterface {

	private static int _nextUserId = 0;

	public static UserInterface newUserInterface(String userName, String dataAccessDeviceId) {
		UserInterface userInterface = UserService.getEmptyUser(userName);
		return userInterface;
	}

	public static UserInterface newUserInterface(String dataAccessDeviceId) {
		return newUserInterface("User_" + _nextUserId++, dataAccessDeviceId);
	}

}