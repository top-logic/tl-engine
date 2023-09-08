/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.gantt;

import com.top_logic.util.Utils;

/**
 * Demo milestone representation.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class DemoMilestone {

	/** The name of the milestone. */
	private String name;

	/** The holder of this {@link DemoMilestone}. */
	private Object holder;


	/**
	 * Creates a new {@link DemoMilestone}.
	 */
	public DemoMilestone(String name, Object holder) {
		this.name = name;
		this.holder = holder;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof DemoMilestone) {
			return Utils.equals(this.name, ((DemoMilestone) obj).name)
				&& Utils.equals(this.holder, ((DemoMilestone) obj).holder);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 13 * (name == null ? 0 : name.hashCode()) + 42 * (holder == null ? 0 : holder.hashCode());
	}


	/**
	 * Gets the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the holder.
	 */
	public Object getHolder() {
		return holder;
	}

	/**
	 * Sets the holder.
	 */
	public void setHolder(Object holder) {
		this.holder = holder;
	}

}
