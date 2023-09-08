/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import java.util.Date;

import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * Class holding {@link Accessor} for {@link Revision}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RevisionAccessor {

	/**
	 * Access to {@link Revision#getDate()}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class DateAccessor extends ReadOnlyAccessor<Revision> {

		@Override
		public Object getValue(Revision object, String property) {
			if (object.isCurrent()) {
				return null;
			}
			return new Date(object.getDate());
		}

	}

	/**
	 * Access to {@link Revision#getAuthor()}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class AuthorAccessor extends AbstractAuthorAccessor<Revision> {

		@Override
		public Object getValue(Revision object, String property) {
			if (object.isCurrent()) {
				return null;
			}
			return resolveAuthor(object.getAuthor());
		}

	}

}

