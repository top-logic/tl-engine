/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.entry;

import java.util.Arrays;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.util.sched.entry.SchedulerEntryStorage;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.composite.CompositeTaskImpl;
import com.top_logic.util.sched.task.impl.TaskImpl;

/**
 * {@link TestCase} for {@link SchedulerEntryStorage}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestSchedulerEntryStorage extends TestCase {

	private SchedulerEntryStorage _storage;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_storage = new SchedulerEntryStorage();
	}

	@Override
	protected void tearDown() throws Exception {
		_storage = null;
		super.tearDown();
	}

	private SchedulerEntryStorage getStorage() {
		return _storage;
	}

	public void testUniqueTaskNames() {
		add(task("a",
			task("a1", task("a1a"), task("a1b")),
			task("a2", task("a2a"), task("a2b"))));
		add(task("b",
			task("b1", task("b1a"), task("b1b")),
			task("b2", task("b2a"), task("b2b"))));
		add(task("c",
			task("c1", task("c1a"), task("c1b")),
			task("c2", task("c2a"), task("c2b"))));
	}

	public void testDuplicateTaskName() {
		String duplicateName = "fubar";
		add(task(duplicateName));
		assertAddFails(task(duplicateName));
	}

	public void testDuplicateTaskNameCaseInsensitive() {
		String duplicateName = "fubar";
		add(task(duplicateName));
		assertAddFails(task(duplicateName.toUpperCase()));
	}

	public void testDuplicateTaskNameNewTopLevelVsExistingChild() {
		String duplicateName = "fubar";
		add(task("outer", task(duplicateName)));
		assertAddFails(task(duplicateName));
	}

	public void testDuplicateTaskNameNewChildVsExistingTopLevel() {
		String duplicateName = "fubar";
		add(task(duplicateName));
		assertAddFails(task("outer", task(duplicateName)));
	}

	public void testDuplicateTaskNameNewChildVsExistingChild() {
		String duplicateName = "fubar";
		add(task("outerA", task(duplicateName)));
		assertAddFails(task("outerB", task(duplicateName)));
	}

	public void testDuplicateTaskNameNewDescendantVsExistingDescendant() {
		String duplicateName = "fubar";
		add(task("outerA", task("innerA", task(duplicateName))));
		assertAddFails(task("outerB", task("innerB", task(duplicateName))));
	}

	public void testDuplicateTaskNameNewDescendantVsExistingDescendantCaseInsensitive() {
		String duplicateName = "fubar";
		add(task("outerA", task("innerA", task(duplicateName))));
		assertAddFails(task("outerB", task("innerB", task(duplicateName.toUpperCase()))));
	}

	public void testDuplicateTaskNameNewChildVsNewChild() {
		String duplicateName = "fubar";
		assertAddFails(task("outer", task(duplicateName), task(duplicateName)));
	}

	public void testDuplicateTaskNameNewDescendantVsNewDescendant() {
		String duplicateName = "fubar";
		assertAddFails(task("outer",
			task("innerA", task(duplicateName)),
			task("innerB", task(duplicateName))));
	}

	public void testDuplicateTaskNameNewDescendantVsNewDescendantCaseInsensitive() {
		String duplicateName = "fubar";
		assertAddFails(task("outer",
			task("innerA", task(duplicateName)),
			task("innerB", task(duplicateName.toUpperCase()))));
	}

	private void add(Task.Config<?> config) {
		getStorage().add(instantiate(config));
	}

	private void assertAddFails(Task.Config<?> config) {
		assertAddFails(instantiate(config));
	}

	private void assertAddFails(Task instantiate) {
		try {
			getStorage().add(instantiate);
		} catch (Exception ex) {
			Pattern pattern = Pattern.compile("Task names .* unique");
			BasicTestCase.assertErrorMessage("Task names have to be unique.", pattern, ex);
			return;
		}
		fail("Conflicting task names were not detected.");
	}

	private <T> T instantiate(PolymorphicConfiguration<T> config) {
		return getInstantiationContext().getInstance(config);
	}

	private InstantiationContext getInstantiationContext() {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
	}

	private CompositeTaskImpl.Config<?> task(String name, TaskImpl.Config<?>... taskConfigs) {
		CompositeTaskImpl.Config<?> config = TypedConfiguration.newConfigItem(CompositeTaskImpl.Config.class);
		config.setName(name);
		config.getTasks().addAll(Arrays.asList(taskConfigs));
		return config;
	}

}
