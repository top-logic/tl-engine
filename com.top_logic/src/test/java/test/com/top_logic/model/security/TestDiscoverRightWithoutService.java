/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.security.DiscoverRight;
import com.top_logic.model.security.SecurityConfigurationService;

/**
 * Test of {@link DiscoverRight#filter(com.top_logic.knowledge.wrap.person.Person, Object, java.util.function.Consumer)}
 * for values without business objects, while the {@link SecurityConfigurationService} is not active.
 *
 * <p>
 * A value without objects needs no access check: it is returned as it is, without asking for the
 * access rights, so that it can also be filtered where the security service is not started (e.g. the
 * state of a job handed to a script in a test without model security).
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestDiscoverRightWithoutService extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		assertFalse("The test needs an inactive security service.",
			SecurityConfigurationService.Module.INSTANCE.isActive());
	}

	public void testNull() {
		assertNull(DiscoverRight.filter(null, null, null));
	}

	public void testPlainValues() {
		String text = "text";
		assertSame(text, DiscoverRight.filter(null, text, null));
		Integer number = Integer.valueOf(42);
		assertSame(number, DiscoverRight.filter(null, number, null));
		ResKey key = ResKey.text("label");
		assertSame(key, DiscoverRight.filter(null, key, null));
	}

	public void testCollectionsWithoutObjects() {
		List<Object> list = Arrays.asList("a", Integer.valueOf(1), null, ResKey.text("b"));
		assertSame(list, DiscoverRight.filter(null, list, null));

		Set<Object> set = new LinkedHashSet<>(Arrays.asList("a", "b"));
		assertSame(set, DiscoverRight.filter(null, set, null));

		List<Object> empty = Collections.emptyList();
		assertSame(empty, DiscoverRight.filter(null, empty, null));
	}

	public void testNestedMapWithoutObjects() {
		Map<Object, Object> map = new LinkedHashMap<>();
		map.put("names", Arrays.asList("x", "y"));
		map.put(Integer.valueOf(1), null);
		map.put("inner", Collections.singletonMap("k", "v"));
		assertSame(map, DiscoverRight.filter(null, map, null));
	}

}
