/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.diff.zip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.io.BOMAwareReader;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.config.diff.CompareStatistics;
import com.top_logic.config.diff.RootCompareStatistic;
import com.top_logic.config.diff.RootCompareStatistic.CompareExecutedListener;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.mig.html.layout.LayoutConstants;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ZipReader {

	public static final String SEPARATOR = "/";
	public static final String ENCODING = Configuration.getConfiguration(ZipReader.class).getValue("DefaultEncoding", LayoutConstants.UTF_8 /* StreamUtilities.ENCODING*/);
	
	private CompareExecutedListener listener = null;
	
	public void setListener(CompareExecutedListener aCLE) {
		listener = aCLE;
	}


	/**
	 * reads the entries from the given zip file and returns a map of names an zip entries
	 * 
	 * @return Map name -> ZipEntry
	 */
	public Map<String, ZipEntry> readEntries(ZipFile zipFile){
		List<String> hlp = new ArrayList<>();
		Map<String, ZipEntry> res = new HashMap<>();
		if(zipFile==null){
			return res;
		}
		
		Enumeration<? extends ZipEntry> enu = zipFile.entries();
		while(enu.hasMoreElements()){
		ZipEntry entry = enu.nextElement();
			res.put(entry.getName(),entry);
			hlp.add(entry.getName());
		}
		
		// it can happen that not for all directories there is a zip entry		
		// so we add add entries for directories
		
		
		Set<String> newEntryNames = new HashSet<>();
		Iterator<String> iter = hlp.iterator();
		while(iter.hasNext()){
			String name = iter.next();
			ZipEntry entry = res.get(name);
			List<String> allParentDirs = getAllParentDirNames(entry);
			Iterator<String> inner = allParentDirs.iterator();
			while(inner.hasNext()){
				String parentDirName = inner.next();
				if(! res.containsKey(parentDirName)){
					newEntryNames.add(parentDirName);
				}
			}
		}

		iter = newEntryNames.iterator();
		while(iter.hasNext()){
			String name = iter.next();
			ZipEntry entry = new ZipEntry(name);
			res.put(name,entry);
		}
		

		return res;
	}
	
	
	/**
	 * create and return list of all parent directories defindee by the 
	 * name of the entry
	 */
	private List<String> getAllParentDirNames(ZipEntry entry) {
		String name = entry.getName();
		List<String> res = new ArrayList<>();
		
		int last = name.lastIndexOf(SEPARATOR);
		while(last>0){
			res.add(name.substring(0,last+1));
			name = name.substring(0,last);
			last = name.lastIndexOf(SEPARATOR);
		}
		
		return res;
	}

	public List<String> getFileNames(ZipFile aFile) {
		Map<String, ZipEntry> entries = readEntries(aFile);
		List<String> theResult = new ArrayList<>();
		Iterator<Entry<String, ZipEntry>> iter = entries.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, ZipEntry> theEntry = iter.next();
			if (!theEntry.getValue().isDirectory()) {
				theResult.add(theEntry.getKey());
			}
		}
		return theResult;
	}
	
	public SimpleResult buildTree(ZipFile aFile) {

		Map<String, ZipEntry> entries = readEntries(aFile);
		
		List<SimpleResult> allSRs = new ArrayList<>();
		Iterator<ZipEntry> iter = entries.values().iterator();
		Map<String, SimpleResult> srMap = new HashMap<>();
		while (iter.hasNext()) {
			ZipEntry theEntry = iter.next();
			SimpleResult theSR = new SimpleResult(theEntry, aFile);
			srMap.put(theSR.getName(), theSR);
			allSRs.add(theSR);
		}

		Iterator<SimpleResult> srIter = allSRs.iterator();
		while (srIter.hasNext()) {
			SimpleResult theSR = srIter.next();
			SimpleResult theParent = srMap.get(theSR.getPath());
			if (theParent != null) {
				theParent.addChild(theSR);
				theSR.setParent(theParent);
			}
		}

		SimpleResult theRoot = new SimpleResult("Grundverzeichnis", aFile);
		srIter = allSRs.iterator();
		while (srIter.hasNext()) {
			SimpleResult theSR = srIter.next();
			if (StringServices.isEmpty(theSR.getPath())) {
				theRoot.addChild(theSR);
				theSR.setParent(theRoot);
			}
		}
		return theRoot;
	}

	public EntryResult compare(ZipFile zip1, ZipFile zip2){
		
		buildTree(zip1);
		
		Map<String, ZipEntry> entries1 = readEntries(zip1);
		Map<String, ZipEntry> entries2 = readEntries(zip2);
		List<EntryResult> compare = getCompareMap(entries1, entries2, zip1, zip2);
		
		
		Map<String, EntryResult> diretories = new HashMap<>();
		Iterator<EntryResult> iter = compare.iterator();
		while(iter.hasNext()){
			EntryResult res = iter.next();
			if(res.isDirectory()){
				diretories.put(res.getName(), res);
			}
		}
		ZipEntry hlp[] = new ZipEntry[2];
		EntryResult root= new EntryResult("", hlp, null, null, listener);
		
		diretories.put("", root);
		
		iter = compare.iterator();
		while(iter.hasNext()){
			EntryResult res = iter.next();
			String path     = res.getPath();
			EntryResult parent = diretories.get(path);
			parent.addChild(res);
			res.setParent(parent);
		}
	
		// the state of EntryResults which are directories is not set yet, it is set to NO_CHANGE
		// compute it
		
		root.setUpStateRecursively();
		
		/*
		 *  The root gets a special compareStatstics, if there are childres are seen, no parent is told about 
		 *  that, but a journal entry is written to document that all changes in the files are reviewed
		 */ 
		root.initRootCompareStatistic();
		 
		for (EntryResult theER : compare) {
			theER.initCompareStatistics();
		}
		
		/* set previous and next relations
		 */
		// first EntryResult, here most of the times the generate.log
		compare.get(0).setPreviousEntryResult(root);
		if (compare.size() > 1) {
			compare.get(0).setNextEntryResult(compare.get(1));
		} else {
			compare.get(0).setNextEntryResult(compare.get(0));
		}
		// EntryResults in between
		for (int i = 1; i < compare.size()-1; i++) {
			compare.get(i).setPreviousEntryResult(compare.get(i-1));
			compare.get(i).setNextEntryResult(compare.get(i+1));
		}
		// last EntryResult
		if (compare.size() > 1) {
		compare.get(compare.size()-1).setNextEntryResult(root);
		compare.get(compare.size()-1).setPreviousEntryResult(compare.get(compare.size()-2));
		} else {
			compare.get(compare.size()-1).setNextEntryResult(root);
			compare.get(compare.size()-1).setPreviousEntryResult(compare.get(compare.size()-1));
		}
		root.setNextEntryResult(compare.get(0));
		root.setPreviousEntryResult(compare.get(compare.size()-1));
		
		return root;
	}
	
	
	
	
	private List<EntryResult> getCompareMap(Map<String, ZipEntry> entries1, Map<String, ZipEntry> entries2, ZipFile zip1, ZipFile zip2) {
		List<EntryResult> result = new ArrayList<>();
		
		List<String> names1 = getSortedNames(entries1);
		List<String> names2 = getSortedNames(entries2);
		
		int l1= names1.size();
		int l2= names2.size();
		
		int p1=0;
		int p2=0;
		// go through the ordered lists and check which object is in which list
		while( p1 < l1  && p2 < l2  ){
			String s1 = names1.get(p1);
			String s2 = names2.get(p2);

			ZipEntry hlp[] = new ZipEntry[2];
			String name=null;
			int compare = s1.compareTo(s2);
			if(compare==0){
				hlp[0] = entries1.get(s1);
				hlp[1] = entries2.get(s2);
				p1++;
				p2++;		
				name = s1;
			}
			else if(compare<0){
				// s1 > s2
				// so there is an entry in entries2 which is not in entries1
				hlp[0] = entries1.get(s1);
				hlp[1] = null;
				p1++;
				name=s1;
			}
			else if(compare>0){
				// s1 < s2
				// so there is an entry in entries1 which is not in entries2
				hlp[0] = null;
				hlp[1] = entries2.get(s2);
				p2++;
				name=s2;
			}
			EntryResult entRes= new EntryResult(name, hlp, zip1, zip2, listener);
			result.add(entRes);	
		}
		
		while( p1 < l1){
			String s1 = names1.get(p1);
			ZipEntry hlp[] = new ZipEntry[2];
			hlp[0] = entries1.get(s1);
			hlp[1] = null;
			EntryResult entRes= new EntryResult(s1, hlp, zip1, zip2, listener);
			result.add(entRes);	
			p1++;
		}
		
		while( p2 < l2){
			String s2 = names2.get(p2);
			ZipEntry hlp[] = new ZipEntry[2];
			hlp[0] = null;
			hlp[1] = entries2.get(s2);
			EntryResult entRes= new EntryResult(s2, hlp, zip1, zip2, listener);
			result.add(entRes);	
			p2++;
		}		
		
		
		return result;
	}



	private List<String> getSortedNames(Map<String, ZipEntry> aMap) {
		List<String> names = new ArrayList<>(aMap.keySet());
		Collections.sort(names);
		return names;
	}

	public static class SimpleResult {
		private final String name;
		private SimpleResult parent;
		private List<SimpleResult> children;

		public SimpleResult(String aName, ZipFile zip) {
			this.name = aName;
			children = new ArrayList<>();
		}
		
		public SimpleResult(ZipEntry zipEntry, ZipFile zip) {
			this.name = zipEntry.getName();
			children = new ArrayList<>();
		}
		

		public void setParent(SimpleResult aParent) {
            this.parent = aParent;
        }

        public List<SimpleResult> getChildren(){
			return children;
		}
		
		public void addChild(SimpleResult entry) {
			children.add(entry);
		}

		public String getPath() {
			String hlp=null;
			if(isDirectory()){
				hlp=name.substring(0,name.length()-1);
			}else{
				hlp=name;
			}
			int i = hlp.lastIndexOf(SEPARATOR);
			if(i<0){
				return "";
			}
			return hlp.substring(0,i+1);
		}


		public boolean isDirectory() {
			return name.endsWith(SEPARATOR) || parent == null;
		}
		

		public String getName(){
			return name;
		}
		
		public String getPlainName() {
		    if (parent != null) {
		        return name.substring(parent.getName().length());
		    }
		    return name;
		}
		
		public SimpleResult getParent(){
			return parent;
		}
		
		@Override
		public String toString() {
			return getPath() + ", " + getPlainName() ;
		}
	}
	
	public static class EntryResult{

		public enum State implements ExternallyNamed {
			NEW("new"),
			DELETED("deleted"),
			NO_CHANGE("noChange"),
			CHANGE("change"),
			;

			private final String _externalName;

			private State(String externalName) {
				_externalName = externalName;
			}

			@Override
			public String getExternalName() {
				return _externalName;
			}

			@Override
			public String toString() {
				return getExternalName();
			}

		}
		private final String name;
		private final ZipEntry[] zipEntries;
		private final ZipFile zip0;
		private final ZipFile zip1;

		private State state;
		private List<EntryResult> children= new ArrayList<>();
        private EntryResult parent;
        private EntryResult previousEntryResult;
        private EntryResult nextEntryResult;
        
        private CompareStatistics statistics = null;
		private final CompareExecutedListener listener;
        
		public EntryResult(String name, ZipEntry[] zipEntries, ZipFile zip0, ZipFile zip1, CompareExecutedListener aCLE) {
			this.name = name;
			this.zipEntries = zipEntries;
			this.zip0 = zip0;
			this.zip1 = zip1;
			this.listener = aCLE;
			computeState();
		}
		
		public String setUpStateRecursively() {
			internalSetUpStateRecursively();
			return getStateAsString();
		}

		private void internalSetUpStateRecursively() {
			boolean changedChild = false;
			for(EntryResult res : getChildren()){
				res.internalSetUpStateRecursively();
				State childState = res.getState();
				if (State.NO_CHANGE != childState) {
					changedChild = true;
				}
			}
			
			if(isDirectory()){
				if(changedChild){
					// check if all children have the same state
					boolean first = true;
					for(EntryResult res : getChildren()){
						if(first){
							first=false;
							state = res.getState();
						}
						else{
							if (state != res.getState()) {
								state = State.CHANGE;
							}
						}
					}
				}else{
					state = State.NO_CHANGE;
				}
			}
		}

		public void initCompareStatistics() {
			statistics = new CompareStatistics(this);
			
		}
		
		public void initRootCompareStatistic() {
			RootCompareStatistic theRootCS = new RootCompareStatistic(this);
			theRootCS.setCompareExecutedListener(listener);
			statistics = theRootCS;

		}
		
		public String getSourceName(){
			return zip0.getName();
		}

		public String getDestName(){
			return zip1.getName();
		}
	    
	    public String getSource(){
	    	return getAsString(zipEntries[0],zip0);
	    }

	    public String getDest(){
	    	return getAsString(zipEntries[1], zip1);
	    	
	    }
		
		private String getAsString(ZipEntry zipEntry, ZipFile zipFile) {
			if(zipEntry == null){
				return "";
			}
			InputStream stream;
			try {
				stream = zipFile.getInputStream(zipEntry);
				return readFromStream(stream, ENCODING);
			} catch (IOException e) {
				Logger.error("Problem reading data for ZipEntry "+zipEntry.getName(), e, this);
				return "Data could not be read";
			}
		}


		public void setParent(EntryResult aParent) {
            this.parent = aParent;
        }

		public EntryResult getParent() {
			return this.parent;
		}
		
        public List<EntryResult> getChildren(){
			return children;
		}
		
		public void addChild(EntryResult entry) {
			children.add(entry);
		}

		public String getPath() {
			String hlp=null;
			if(isDirectory()){
				hlp=name.substring(0,name.length()-1);
			}else{
				hlp=name;
			}
			int i = hlp.lastIndexOf(SEPARATOR);
			if(i<0){
				return "";
			}
			return hlp.substring(0,i+1);
		}


		public boolean isDirectory() {
			return name.endsWith(SEPARATOR) || parent == null;
		}


		private void computeState() {
			if(zipEntries[0]==null){
				// some special-handling for root - the only case were [0] and [1] == null
				if (zipEntries[1]==null) {
					state = State.NO_CHANGE;
				}
				else {
					state = State.NEW;
				}
				return;
			}
			if(zipEntries[1]==null){
				state = State.DELETED;
				return;
			}
			try{				
				
				if(zipEntries[0].isDirectory()){
					state = State.NO_CHANGE;
				}else{
					InputStream stream0 = zip0.getInputStream(zipEntries[0]);
					boolean isEqual;
					try {
						InputStream stream1 = zip1.getInputStream(zipEntries[1]);
						try {
							isEqual = StreamUtilities.equalsStreamContents(stream0, stream1);
						} finally {
							stream1.close();
						}
					} finally {
						stream0.close();
					}
					state = isEqual ? State.NO_CHANGE : State.CHANGE;
				}
			}
			catch(IOException e){
				String text = "Problem comparing zipEntries files "+zipEntries[0]+" and "+zipEntries[1];
				Logger.warn(text,e,this);
				throw new RuntimeException(text);
			}
		}


		public String getName(){
			return name;
		}
		
		public String getPlainName() {
		    if (parent != null) {
		        return name.substring(parent.getName().length());
		    }
		    return name;
		}
		
//		public EntryResult getParent(){
//			return parent;
//		}
//		
//		public boolean hasChange(){
//			return ! (state == NO_CHANGE) ;
//		}
//		
//		public void printRecursively(int indent){
//			String hlp="";
//			for(int i=0;i<indent;i++){
//				hlp=hlp+" ";
//			}
//			System.out.println(hlp+name);
//			for(int i=0;i<children.size();i++){
//				EntryResult res = children.get(i);
//				res.printRecursively(indent+3);
//			}
//		}

		public State getState() {
			return state;
		}

		public String getStateAsString() {
			return getState().getExternalName();
        }


//		public ZipFile getSourceZipFile() {
//			return zip0;
//		}
//		
//		public ZipFile getDestZipFile() {
//			return zip1;
//		}
		
        public ZipEntry getSourceEntry(){
        	return zipEntries[0];
        }
        
        public ZipEntry getDestEntry(){
        	return zipEntries[1];
        }


		public CompareStatistics getStatistics() {
			return (statistics);
		}

		public EntryResult getPreviousEntryResult() {
			return (previousEntryResult);
		}

		public void setPreviousEntryResult(EntryResult previousEntryResult) {
			this.previousEntryResult = previousEntryResult;
		}

		public EntryResult getNextEntryResult() {
			return (nextEntryResult);
		}

		public void setNextEntryResult(EntryResult nextEntryResult) {
			this.nextEntryResult = nextEntryResult;
		}
        
	}

	public EntryResult compare(Document aFirstVersion, Document aSecondVersion) {
		 ZipFile zipFile1 = getZipFromDocument(aFirstVersion);
         ZipFile zipFile2 = getZipFromDocument(aSecondVersion);
         return compare(zipFile1, zipFile2);
	}
	  private ZipFile getZipFromDocument(Document aDoc) {
          if(aDoc == null){
        	  return null;
          }
          File theTempFile = getTempFileFromDocument(aDoc);
          try {
              ZipFile theZipFile = new ZipFile(theTempFile);
              return theZipFile;
          }
          catch (Exception ex) {
              throw new RuntimeException("Problems creating a ZipFile from File", ex);
          }
      }

      private File getTempFileFromDocument(Document aDoc) {
          try {
              File theFile = File.createTempFile("compareZip", "tempe", Settings.getInstance().getTempDir());
              OutputStream theOS = new FileOutputStream(theFile);
			try {
				InputStream theIS = aDoc.getStream();
				try {
					byte buf[] = new byte[1024];
					int length;
					while ((length = theIS.read(buf)) > 0) {
						theOS.write(buf, 0, length);
					}
					return theFile;
				} finally {
					theIS.close();
				}
			} finally {
				theOS.close();
			}
          }
          catch (Exception ex) {
              throw new RuntimeException("Problems creating a TempFile", ex);
          }
      }

      private static String readFromStream(InputStream is, String aDefaultEncoding) throws IOException {
          BOMAwareReader bar = new BOMAwareReader(is, aDefaultEncoding);
          BufferedReader reader = new BufferedReader(bar);
          StringBuffer buffer = new StringBuffer();
          
          for (int c = reader.read(); c > -1; c = reader.read()) {
              buffer.append((char)c);
          }
          
          reader.close();
          bar.close();
          
          return buffer.toString();
      }
}

