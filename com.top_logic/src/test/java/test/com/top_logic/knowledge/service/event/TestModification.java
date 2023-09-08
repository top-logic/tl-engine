/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.event;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.event.Modification;

/**
 * Tests for {@link Modification}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestModification extends TestCase {

	private static class AddModification<T> implements Modification {

		final List<T> _list;

		final T _obj;

		AddModification(List<T> l, T obj) {
			_list = l;
			_obj = obj;

		}

		@Override
		public void execute() throws DataObjectException {
			_list.add(_obj);
		}

	}

	public void testComposeAndThen() {
		List<String> tmp = new ArrayList<>();
		Modification before = new AddModification<>(tmp, "first");
		Modification after = new AddModification<>(tmp, "second");

		before.andThen(after).execute();
		assertEquals(list("first", "second"), tmp);
		tmp.clear();

		after.andThen(before).execute();
		assertEquals(list("second", "first"), tmp);
		tmp.clear();

		after.compose(before).execute();
		assertEquals(list("first", "second"), tmp);
		tmp.clear();

		before.compose(after).execute();
		assertEquals(list("second", "first"), tmp);
		tmp.clear();

		before.compose(Modification.NONE).execute();
		assertEquals(list("first"), tmp);
		tmp.clear();
		before.andThen(Modification.NONE).execute();
		assertEquals(list("first"), tmp);
		tmp.clear();

		Modification.NONE.compose(before).execute();
		assertEquals(list("first"), tmp);
		tmp.clear();
		Modification.NONE.andThen(before).execute();
		assertEquals(list("first"), tmp);
		tmp.clear();

	}


}

