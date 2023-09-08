/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo.model;

/**
 * Demo class that represents an address.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoAddress implements Comparable<DemoAddress> {
    String street;
    String streetNumber;
    String zipCode;
    String city;

    /**
	 * Default constructor to create an uninitialized address object.
	 * 
	 * <p>
	 * Such object may be filled with values using
	 * {@link DemoAddressContext#saveIn(DemoAddress)}.
	 * </p>
	 */
	public DemoAddress() {
		super();
	}

	public DemoAddress(String street, String streetNumber, String zipCode, String city) {
		this.street = street;
		this.streetNumber = streetNumber;
		this.zipCode = zipCode;
		this.city = city;
	}

	public String getCity() {
		return city;
	}

	public String getStreet() {
		return street;
	}

	public String getStreetNumber() {
		return streetNumber;
	}

	public String getZipCode() {
		return zipCode;
	}

	@Override
	public int compareTo(DemoAddress o) {
		int result = city.compareTo(o.city);
		if (result == 0) {
			result = zipCode.compareTo(o.zipCode);
			if (result == 0) {
				result = street.compareTo(o.street);
				if (result == 0) {
					result = streetNumber.compareTo(o.streetNumber);
				}
			}
		}
		return result;
	}
}
