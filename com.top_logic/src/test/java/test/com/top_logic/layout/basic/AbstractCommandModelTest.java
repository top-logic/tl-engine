/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.form.PropertyEvent;

import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.ButtonImageListener;
import com.top_logic.layout.basic.ButtonUIModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ExecutableListener;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.form.DisabledPropertyListener;
import com.top_logic.layout.form.FormMember;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;

/**
 * The class {@link AbstractCommandModelTest} tests the methods in
 * {@link CommandModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractCommandModelTest<T extends CommandModel> extends BasicTestCase {
	
	protected TestListener listener;

	protected static class TestListener implements VisibilityListener, ExecutableListener, ButtonImageListener,
			DisabledPropertyListener {
		
		private final Set<PropertyEvent> capturedEvents = new HashSet<>();
	
		@Override
		public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
			return addEvent(sender, FormMember.VISIBLE_PROPERTY, oldVisibility, newVisibility);
		}

		private Bubble addEvent(Object sender, EventType<?, ?, ?> type, Object oldValue, Object newValue) {
			getCapturedEvents().add(new PropertyEvent(sender, type, oldValue, newValue));
			return Bubble.BUBBLE;
		}

		@Override
		public Bubble handleDisabledChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
			return addEvent(sender, FormMember.DISABLED_PROPERTY, oldValue, newValue);
		}

		@Override
		public Bubble handleExecutableChange(ButtonUIModel sender, Boolean oldExecutability, Boolean newExecutability) {
			return addEvent(sender, ButtonUIModel.EXECUTABLE_PROPERTY, !newExecutability, newExecutability);
		}

		@Override
		public Bubble handleNotExecutableReasonChange(ButtonUIModel sender, ResKey oldReason, ResKey newReason) {
			return addEvent(sender, ButtonUIModel.NOT_EXECUTABLE_REASON_PROPERTY, oldReason, newReason);
		}

		@Override
		public Bubble handleImageChanged(ButtonUIModel sender, ThemeImage oldValue, ThemeImage newValue) {
			return addEvent(sender, ButtonUIModel.IMAGE_PROPERTY, oldValue, newValue);
		}

		@Override
		public Bubble handleDisabledImageChanged(ButtonUIModel sender, ThemeImage oldValue, ThemeImage newValue) {
			return addEvent(sender, ButtonUIModel.NOT_EXECUTABLE_IMAGE_PROPERTY, oldValue, newValue);
		}

		public void clear() {
			getCapturedEvents().clear();
		}
		
		public Set<PropertyEvent> getCapturedEvents() {
			return capturedEvents;
		}
		
		public void assertCaptured(EventType<?, ?, ?> type, Object oldValue, Object newValue) {
			for (PropertyEvent event : getCapturedEvents()) {
				if (event.getType() == type && Utils.equals(oldValue, event.getOldValue()) && Utils.equals(newValue, event.getNewValue())) {
					return;
				}
			}
			fail("no event with type '" + type + "' old value '" + oldValue + "' and new value '" + newValue + "'");
		}

	}
	
	protected void addListener(CommandModel model, TestListener listener) {
		model.addListener(FormMember.VISIBLE_PROPERTY, listener);
		model.addListener(FormMember.DISABLED_PROPERTY, listener);
		model.addListener(ButtonUIModel.EXECUTABLE_PROPERTY, listener);
		model.addListener(ButtonUIModel.NOT_EXECUTABLE_REASON_PROPERTY, listener);
		model.addListener(ButtonUIModel.IMAGE_PROPERTY, listener);
		model.addListener(ButtonUIModel.NOT_EXECUTABLE_IMAGE_PROPERTY, listener);
	}

	protected void removeListener(CommandModel model, TestListener listener) {
		model.removeListener(ButtonUIModel.NOT_EXECUTABLE_IMAGE_PROPERTY, listener);
		model.removeListener(ButtonUIModel.IMAGE_PROPERTY, listener);
		model.removeListener(ButtonUIModel.NOT_EXECUTABLE_REASON_PROPERTY, listener);
		model.removeListener(ButtonUIModel.EXECUTABLE_PROPERTY, listener);
		model.removeListener(FormMember.DISABLED_PROPERTY, listener);
		model.removeListener(FormMember.VISIBLE_PROPERTY, listener);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		listener = new TestListener();
	}
	
	@Override
	protected void tearDown() throws Exception {
		listener = null;
		super.tearDown();
	}

	/**
	 * Returns the concrete {@link CommandModel} which will be used in the
	 * tests.
	 */
	protected abstract T getCommandModel();
	
	public void testPropertyListener() {
		ResKey notExecReason = Resources.encodeLiteralText("some reason");
		final T model = getCommandModel();
		addListener(model, listener);

		listener.clear();
		model.setVisible(false);
		assertFalse(listener.getCapturedEvents().isEmpty());

		listener.clear();
		model.setVisible(true);
		assertFalse(listener.getCapturedEvents().isEmpty());
		
		listener.clear();
		model.setNotExecutable(notExecReason);
		assertFalse(listener.getCapturedEvents().isEmpty());

		listener.clear();
		model.setExecutable();
		assertFalse(listener.getCapturedEvents().isEmpty());
		
		removeListener(model, listener);

		listener.clear();
		model.setVisible(false);
		assertTrue(listener.getCapturedEvents().isEmpty());
		
		listener.clear();
		model.setVisible(true);
		assertTrue(listener.getCapturedEvents().isEmpty());
		
		listener.clear();
		model.setNotExecutable(notExecReason);
		assertTrue(listener.getCapturedEvents().isEmpty());
		
		listener.clear();
		model.setExecutable();
		assertTrue(listener.getCapturedEvents().isEmpty());
	}
	
	public void testEvents() {
		ResKey notExecReason = Resources.encodeLiteralText("some reason");
		final T model = getCommandModel();
		addListener(model, listener);
		
		model.setNotExecutable(notExecReason);
		listener.assertCaptured(CommandModel.EXECUTABLE_PROPERTY, true, false);
		
		listener.clear();
		model.setExecutable();
		listener.assertCaptured(CommandModel.EXECUTABLE_PROPERTY, false, true);
		
		listener.clear();
		assertTrue(model.isVisible());
		model.setVisible(false);
		listener.assertCaptured(FormMember.VISIBLE_PROPERTY, true, false);
		listener.assertCaptured(CommandModel.EXECUTABLE_PROPERTY, true, false);
		
		listener.clear();
		model.setVisible(true);
		listener.assertCaptured(FormMember.VISIBLE_PROPERTY, false, true);
		listener.assertCaptured(CommandModel.EXECUTABLE_PROPERTY, false, true);
		
		removeListener(model, listener);
	}
	
	public void testProperties() {
		final T model = getCommandModel();
		Property<Property> prop = TypedAnnotatable.property(Property.class, "prop");
		Property<Property> value = TypedAnnotatable.property(Property.class, "value");

		assertNull(model.get(prop));
		
		model.set(prop, value);
		
		assertSame(value, model.get(prop));
		// double getProperty must remain stable
		assertSame(value, model.get(prop));
		
		final Object storedProperty = model.set(prop, prop);
		assertSame(value, storedProperty);
		assertSame(prop, model.get(prop));
		
		final Object firstProperty = model.reset(prop);
		assertSame(prop, firstProperty);
		final Object secondRemove = model.reset(prop);
		assertNull(secondRemove);
	}

	public void testCheckScope() {
		AtomicBoolean checkScopeChecked = new AtomicBoolean();
		AbstractCommandModel testModel = new AbstractCommandModel() {

			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				return HandlerResult.DEFAULT_RESULT;
			}
		};

		testModel.setCheckClosure(new CheckScope() {

			@Override
			public Collection<? extends ChangeHandler> getAffectedFormHandlers() {
				checkScopeChecked.set(true);
				return Collections.emptyList();
			}
		});
		testModel.executeCommand(DefaultDisplayContext.getDisplayContext());
		assertTrue(checkScopeChecked.get());
	}

	/**
	 * Test suite creation for {@link CommandModel} tests.
	 */
	public static Test suite(Class<? extends Test> clazz) {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(clazz,
				CommandHandlerFactory.Module.INSTANCE,
				RequestLockFactory.Module.INSTANCE,
				SecurityComponentCache.Module.INSTANCE));
	}

}

