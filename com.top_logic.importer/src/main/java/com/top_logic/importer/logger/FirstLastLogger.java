/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.logger;

/**
 * Logs the first 20 messages and the last 20 messages.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class FirstLastLogger extends AbstractLogger{

	private static int MAX = 20;
	
	private String[] first;
	private String[] last;

	private int firstCount;

	private int lastPos;

	private boolean skipped;

    public FirstLastLogger() {
		super();
		first = new String[MAX];
		last  = new String[MAX];
		firstCount=0;
		lastPos = 0;
		skipped = false;
	}

	public String getString() {
		
		StringBuffer buf = new StringBuffer();
		
		if(skipped){
			buf.append("Es wird nur Anfang und Ende des Ergebnisses angezeigt.\n\n");
		}
		
		for(int i=0;i<firstCount ;i++){
			buf.append(first[i]);
			buf.append("\n");
		}
		if(skipped){
			buf.append("\n\n...\n\n");
		}
		for(int i=0;i<MAX;i++){
			String txt = last[ (i+lastPos)%MAX ];
			if(txt != null){
				buf.append(txt);
				buf.append("\n");
			}
		}
		return buf.toString();
	}

	@Override
	protected void log(String text) {
		if(firstCount< MAX){
			first[firstCount] = text;
			firstCount++;
		}else{
			last[lastPos] = text;
			lastPos++;
			if(lastPos % MAX == 0){
				lastPos = 0;
				skipped = true;
			}
		}
	}
}