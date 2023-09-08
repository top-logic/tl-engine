/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching.internet;

/**
 * Simple default implementation of a {@link com.top_logic.knowledge.searching.internet.Link}.
 *
 * @author <a href="mailto:fma@top-logic.com">Frank Mausz</a>
 */
public class DefaultLink implements Link {

    /** The title of the link */
	private String title;
    
    /** The description of the link */
	private String description;
    
    /** The URL of the link */
	private String url;
    
    /** The ranking for the link */
	private double ranking;

    /** Constructor */
	public DefaultLink(String aTitle,String aDescription,String anUrl,double aRanking)  {
		this.title        = aTitle;
		this.description  = aDescription;
		this.url          = anUrl;
		this.ranking      = aRanking;
	}

	/**
	 * Return true specified object is an instance of Link-interface and
	 * this url is equals to the url of the other Link.
	 */
	@Override
	public boolean equals(Object obj)  {
		if (obj instanceof Link)  {
			Link otherLink = (Link)obj;
			
			return getURLString() != null ?
			       getURLString().equals(otherLink.getURLString()) :
				   false;
		}
		else {
			return false;
		}
	}

	/**
	 * Returns a number in the range from 0.0 to 1.0
	 */
	@Override
	public double getRanking()  {
		return ranking;
	}

	/**
	 * Returns the title of this link.
	 */
	@Override
	public String getTitle()  {
		return title;
	}

	/**
	 * Returns an description of this link.
	 */
	@Override
	public String getDescription()  {
		return description;
	}

	/**
	 * Returns the URL of this link.
	 */
	@Override
	public String getURLString()  {
		return url;
	}

	/**
	 * Returns the hashCode.
	 */
	@Override
	public int hashCode()  {
		return getURLString().hashCode();
	}
}

