/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo.model;

/**
 * Demo class that represents an order.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoOrder {
	DemoPerson purchaser;
	DemoAddress billingAddress;
	DemoAddress deliveryAddress;
	
	public DemoOrder(DemoPerson purchaser, DemoAddress billingAddress, DemoAddress deliveryAddress) {
		this.purchaser = purchaser;
		this.billingAddress = billingAddress;
		this.deliveryAddress = deliveryAddress;
	}

	public DemoAddress getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(DemoAddress billingAddress) {
		this.billingAddress = billingAddress;
	}

	public DemoAddress getDeliveryAddress() {
		return deliveryAddress;
	}

	public void setDeliveryAddress(DemoAddress deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}

	public DemoPerson getPurchaser() {
		return purchaser;
	}

	public void setPurchaser(DemoPerson purchaser) {
		this.purchaser = purchaser;
	}
}
