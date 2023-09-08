/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.event.convert;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.knowledge.event.CachingEventWriter;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.event.convert.StackedEventRewriter;

/**
 * Test class for {@link StackedEventRewriter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestStackedEventRewriter extends BasicTestCase {

	private static final class TestingEventRewriter implements EventRewriter {

		long _expectedRevision;

		long _newRevision;

		public TestingEventRewriter(long expectedRevision, long newRevision) {
			_expectedRevision = expectedRevision;
			_newRevision = newRevision;
		}

		@Override
		public void rewrite(ChangeSet cs, EventWriter out) {
			assertEquals(_expectedRevision, cs.getRevision());
			cs.setRevision(_newRevision);
			out.write(cs);
		}

	}

	public void testChaining() {
		ChangeSet input = new ChangeSet(1);
		EventRewriter r1 = new TestingEventRewriter(1, 2);
		EventRewriter r2 = new TestingEventRewriter(2, 3);
		EventRewriter r3 = new TestingEventRewriter(3, 4);
		CachingEventWriter cachingEventWriter = new CachingEventWriter();
		newStackedEventRewriter(r1, r2, r3).rewrite(input, cachingEventWriter);
		List<ChangeSet> allEvents = cachingEventWriter.getAllEvents();
		assertEquals(1, allEvents.size());
		assertEquals(allEvents.get(0).getRevision(), 4);
	}

	private EventRewriter newStackedEventRewriter(EventRewriter... rewriters) {
		return StackedEventRewriter.getRewriter(rewriters);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestStackedEventRewriter}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestStackedEventRewriter.class);
	}

}

