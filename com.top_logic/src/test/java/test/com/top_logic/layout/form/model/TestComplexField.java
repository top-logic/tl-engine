/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Test;

import test.com.top_logic.basic.StringWriterNonNull;

import com.top_logic.basic.col.BooleanFlag;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DefaultUpdateQueue;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.control.DateInputControl;
import com.top_logic.layout.form.model.ComplexField;

/**
 * Test case for {@link ComplexField} using {@link DateFormat}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestComplexField extends AbstractComplexFieldTest {

	public void testInitialization() {
		assertFalse(field.hasError());
		assertTrue(field.hasValue());
		assertEquals(NOW, field.getValue());
	}

	public void testReset() {
		field.reset();

		assertFalse(field.hasError());
		assertTrue(field.hasValue());

		assertEquals(LATER, field.getValue());
	}
	
	
	public void testUpdate() throws ParseException, VetoException {
		assertEquals(NOW, field.getValue());
		
		field.update(NOW_AS_STRING);
		
		assertFalse(field.hasError());

		// For an explanation of the following test, see below: 
		assertTrue(field.hasValue());
		
		// That the field still has a value (despite the fact that it was
		// updated with new user input) is due to the special input that was
		// provided: 
		// 
		// Although
		//
		assertNotSame(DATE_TIME_FORMAT.parse(NOW_AS_STRING), NOW);
		//
		// the opposite holds:
		//
		assertEquals(NOW_AS_STRING, DATE_TIME_FORMAT.format(NOW));
		//
		// This asymmetry is due to the fact that SimpleDateFormat does not
		// output the seconds part of the date, and always produces dates with a
		// zero seconds part.
		//
		// Combined, when updating the field with NOW_AS_STRING, the field
		// detects that the value does not have changed (because it compares the
		// string representations of the value) and does not switch to the input
		// state.
		
		field.check();
		
		assertFalse(field.hasError());
		assertTrue(field.hasValue());
		
		// Since the field did never switch to its input state, it does not
		// parse the string representation of its initial value, but only checks
		// additional constraints (which are not there). As result, the
		// field's value is still its initial value.
		assertNotSame(APPROXIMATELY_NOW, field.getValue());
		assertEquals(NOW, field.getValue());
		
		field.update(LATER_AS_STRING);
		field.update(NOW_AS_STRING);

		// After changing the field's value twice, it finally has accepted the
		// new input value and has switched to its input state.
		assertFalse(field.hasError());
		
		field.check();
		
		// The field has parsed the new input value without errors.
		assertFalse(field.hasError());
		assertTrue(field.hasValue());

		// And its value finally switched to the new value APPROXIMATELY_NOW.
		assertNotSame(NOW, field.getValue());
		assertEquals(APPROXIMATELY_NOW, field.getValue());
	}

	public void testFailingUpdateWithLeadingWhitespace() throws VetoException {
		// Configure the field to be pedantic on whitespace.
		field.setWhiteSpaceIgnored(false);
		
		field.update(" " + LATER_AS_STRING);

		// The field has accepted the new user input.
		assertFalse(field.hasError());

		field.check();
		
		// One would assume that the field did not accept the input. This is not
		// true, because the SimpleDateFormat itself consumes leading
		// whitespace.
		// 
		// assertTrue(field.hasError());
		// assertFalse(field.hasValue());
		//
		assertFalse(field.hasError());
		assertTrue(field.hasValue());
		
		assertEquals(APPROXIMATELY_LATER, field.getValue());
	}
	
	public void testFailingUpdateWithTailingWhitespace() throws VetoException {
		// Configure the field to be pedantic on whitespace.
		field.setWhiteSpaceIgnored(false);
		
		field.update(NOW_AS_STRING + " ");

		// The field has accepted the new user input.
		field.check();
		
		assertTrue(field.hasError());
		assertFalse(field.hasValue());
	}

	public void testFailingUpdateWithLeadingGarbage() throws VetoException {
		// Configure the field to be pedantic on whitespace.
		field.setWhiteSpaceIgnored(false);
		
		field.update(" x " + LATER_AS_STRING);
		
		// The field has accepted the new user input.
		field.check();
		
		assertTrue(field.hasError());
		assertFalse(field.hasValue());
	}

	public void testFailingUpdateWithTailingGarbage() throws VetoException {
		// Configure the field to be pedantic on whitespace.
		field.setWhiteSpaceIgnored(false);
		
		field.update(LATER_AS_STRING + " x ");
		
		// The field has accepted the new user input.
		field.check();
		
		assertTrue(field.hasError());
		assertFalse(field.hasValue());
	}
	
	public void testSuccedingUpdateWithWhitespace() throws VetoException {
		// Configure the field to silently ignore leading and tailing whitespace.
		field.setWhiteSpaceIgnored(true);

		field.update(" " + NOW_AS_STRING + " ");
		field.check();
		
		assertFalse(field.hasError());
		assertTrue(field.hasValue());
		
		assertEquals(APPROXIMATELY_NOW, field.getValue());
	}
	
	public void testSetFormat() throws IOException {
		final DateInputControl dateControl = new DateInputControl(field);
		// write field to GUI
		StringWriter initialWrittenContent = new StringWriterNonNull();
		dateControl.write(displayContext(), new TagWriter(initialWrittenContent));
		final String currentFieldValue = field.getFormat().format(field.getValue());
		assertTrue(initialWrittenContent.getBuffer().toString().contains(currentFieldValue));
		
		// update format
		final BooleanFlag wasFormatted = new BooleanFlag(false);
		final DateFormat newFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z") {
			
			@Override
			public StringBuffer format(Date date, StringBuffer toAppendTo, java.text.FieldPosition pos) {
				wasFormatted.set(true);
				return super.format(date, toAppendTo, pos);
			}
			
		};
		newFormat.setTimeZone(TimeZones.systemTimeZone());
		final String desiredGUIValue = newFormat.format(field.getValue());
		field.setFormat(newFormat);
		assertEquals(newFormat, field.getFormat());
		
		// get Updates
		final DefaultUpdateQueue updates = new DefaultUpdateQueue();
		dateControl.revalidate(displayContext(), updates);
		
		assertTrue("No update written for the new format", !updates.getStorage().isEmpty());
		final StringWriter writtenUpdate= new StringWriterNonNull();
		updates.getStorage().get(0).writeAsXML(displayContext(), new TagWriter(writtenUpdate));
		assertTrue(writtenUpdate.getBuffer().toString().contains(desiredGUIValue));
		
		assertTrue("New format was not used", wasFormatted.get());
	}
	
	public void testIllegalInputSwitchVisibility() throws VetoException {
		field.update("not parsable");
		assertTrue("update Field with not parsable input but no error present", field.hasError());

		try {
			field.setVisible(false);
			assertFalse(field.isValid());
			field.setVisible(true);
		} catch (IllegalStateException ex) {
			fail("Ticket #3552: Changing visibility of field with illegal input failed.", ex);
		}
	}

	public static Test suite () {
		return suite(TestComplexField.class);
    }

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
