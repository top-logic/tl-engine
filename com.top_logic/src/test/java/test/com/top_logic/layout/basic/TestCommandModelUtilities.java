/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelUtilities;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The class {@link TestCommandModelUtilities} tests methods in
 * {@link CommandModelUtilities}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestCommandModelUtilities extends BasicTestCase {
	
	private static final ThemeImage ICON = ThemeImage.forTest("/imageKey");

	public static final class TestingConfirmCommand extends AbstractCommandHandler {

		public TestingConfirmCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			return null;
		}

		public static AbstractCommandHandler newInstance(String commandId, boolean needsConfirm) {
			return newInstance(updateConfirm(
					AbstractCommandHandler.<Config> createConfig(TestingConfirmCommand.class, commandId),
				needsConfirm));
		}

	}

	private CommandModel model;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		model = new AbstractCommandModel() {
			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	protected void tearDown() throws Exception {
		model = null;
		super.tearDown();
	}

	public void testCopyValues() {
		model.setCssClasses("cssClass");
		model.setNotExecutable(ResKey.forTest("reasonKey"));
		ThemeImage image = ICON;
		model.setImage(image);
		ThemeImage notExecImage = ICON;
		model.setNotExecutableImage(notExecImage );
		model.setTooltip("MyToolTip");
		model.setTooltipCaption("MyToolTipCaption");
		model.setVisible(true);
		model.setLabel("label");
		
		CommandModel targetModel = new AbstractCommandModel() {
			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				throw new UnsupportedOperationException();
			}
		};
		CommandModelUtilities.copyValues(model, targetModel );
		
		assertEquals(model.getCssClasses(), targetModel.getCssClasses());
		assertEquals(model.isExecutable(), targetModel.isExecutable());
		assertEquals(model.getNotExecutableReasonKey(), targetModel.getNotExecutableReasonKey());
		assertEquals(model.getImage(), targetModel.getImage());
		assertEquals(model.getNotExecutableImage(), targetModel.getNotExecutableImage());
		assertEquals(model.getTooltip(), targetModel.getTooltip());
		assertEquals(model.getTooltipCaption(), targetModel.getTooltipCaption());
		assertEquals(model.getLabel(), targetModel.getLabel());
	}

	public static Test suite() {
		return AbstractCommandModelTest.suite(TestCommandModelUtilities.class);
	}

}
