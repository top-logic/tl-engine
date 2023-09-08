/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.template;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.form.template.BeaconFormFieldControlProvider;

/**
 * Test for {@link BeaconFormFieldControlProvider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestBeaconFormFieldControlProvider extends TestCase {

	public void testDefaultType() throws ConfigurationException {
		String defaultType = BeaconFormFieldControlProvider.VAL_TYPE_BEACON;

		BeaconFormFieldControlProvider fieldProv1 = new BeaconFormFieldControlProvider();
		assertEquals(defaultType, fieldProv1.getType());
		BeaconFormFieldControlProvider fieldProv2 = new BeaconFormFieldControlProvider(defaultType);
		assertEquals(defaultType, fieldProv2.getType());
		PolymorphicConfiguration<?> beaconProviderConfig = TypedConfiguration.createConfigItemForImplementationClass(BeaconFormFieldControlProvider.class);
		BeaconFormFieldControlProvider fieldProv3 =
			(BeaconFormFieldControlProvider) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
				.getInstance(beaconProviderConfig);
		assertEquals(defaultType, fieldProv3.getType());
	}

}

