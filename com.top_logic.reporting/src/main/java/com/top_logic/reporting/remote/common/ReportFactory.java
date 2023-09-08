/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.remote.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.office.AbstractOffice.ImageReplacerData;
import com.top_logic.base.office.excel.ExcelImage;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.reporting.queue.QueueReporter;
import com.top_logic.reporting.queue.ReportQueueConstants;
import com.top_logic.reporting.remote.ReportDescriptor;
import com.top_logic.reporting.remote.Reporter;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
@Deprecated
public final class ReportFactory extends ManagedClass {
	
	public static final String FILE_TYPE = "FileType";
    public static final String VALUE = "VALUE";
    public static final String TYPE = "TYPE";
    
	private final Reporter clientReporter;

	public interface Config extends ServiceConfiguration<ReportFactory> {

		String CLIENT_REPORTER_CLASS = "client-reporter-class";

		@Name(CLIENT_REPORTER_CLASS)
		Class<? extends Reporter> getReporterClass();
	}

	public ReportFactory(InstantiationContext context, Config config) {
		super(context, config);

		Reporter reporter;

		try {
			Class<? extends Reporter> theClass = config.getReporterClass();
			reporter = theClass.newInstance();
			if (reporter.isValid()) {
				if (reporter.init()) {
					Logger.info("Reporter initialized", this);
				} else {
					Logger.info("Couldn't get connection to Reporter. Check Configuration! Using fallback-reporter",
						this);
					reporter = new QueueReporter();
					reporter.init();
				}
			} else {
				Logger.info("Reporter-Configuration invalid. Using fallback-reporter", this);
				reporter = new QueueReporter();
				reporter.init();
			}
		} catch (Exception e) {
			Logger.error("Can not instantiate ClientReporterClass: " + config.getReporterClass(), e, this);
			Logger.info("Using fallback-reporter", this);
			reporter = new QueueReporter();
			reporter.init();
		}

		clientReporter = reporter;
	}

	public static ReportDescriptor getNewReportDescriptor(byte[] aTemplate, String aTemplateName, Map aValueMap, String aType, String aMode) throws IOException {
	    
	    // special handling for other content than Strings. E.g. replace image-references in map with byte[] of this image
	    handleSpecialContentForWrite(aValueMap);
	    
	    ReportDescriptorImpl rd = new ReportDescriptorImpl();
	    rd.init(aTemplate, aTemplateName, aValueMap, aType, aMode);
		return rd;
	}

    /*
     * Convenience method to handle special content. E.g. replace image references in the map with byte[]-representation 
     * of this image.
     * 
     * TODO CCA: verschiedene keys können das selbe image referenzieren und image sollte dann nicht 
     * mehrfach übertragen werden!
     */
	public static void handleSpecialContentForWrite(Map aValueMap) throws IOException {
		if (aValueMap == null) {
			return;
		}
		
        Set keys = aValueMap.keySet();
        Iterator iter = keys.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            Object value = aValueMap.get(key);
            
            
            // Some objects need special handling because they are not Serializable or reference
            // a file. In this case, the file is read into a byte[] and all values with meta-information
            // are put into a map.
            
            // no special handling needed for Date, Number, PowerpointStyledString because they implement Serializable
            
            // special handling for File
            if (value instanceof File) {
                File file = (File) value;
                Map map = new HashMap();
                map.put(TYPE, "File");
                map.put(FILE_TYPE, getFileExtension(file));
                map.put(VALUE, FileUtilities.getBytesFromFile(file));
                aValueMap.put(key, map);
            }
            
            // special handling for ImageReplacerData
            else if (value instanceof ImageReplacerData) {
                ImageReplacerData ird = (ImageReplacerData) value;
                Map map = new HashMap();
                map.put(TYPE, "ImageReplacerData");
                map.put(FILE_TYPE, getFileExtension(ird.imgFile));
                map.put(VALUE, FileUtilities.getBytesFromFile(ird.imgFile));
            
				map.put("hAlignment", Long.valueOf(ird.hAlignment));
				map.put("vAlignment", Long.valueOf(ird.vAlignment));
				map.put("hSize", Float.valueOf(ird.hSize));
				map.put("vSize", Float.valueOf(ird.vSize));
                map.put("lockRatio", ird.lockRatio ? Boolean.TRUE : Boolean.FALSE);
                aValueMap.put(key, map);
            }
            
            // special handling for ExcelImage
            else if (value instanceof ExcelImage) {
                ExcelImage ei = (ExcelImage)value;
                Map map = new HashMap();
                map.put(TYPE, "ExcelImage");
                map.put(FILE_TYPE, getFileExtension(ei.getImage()));
                map.put(VALUE, ei.getImage());
                map.put("centerX", ei.isCenterX() ? Boolean.TRUE : Boolean.FALSE);
                map.put("centerY", ei.isCenterY() ? Boolean.TRUE : Boolean.FALSE);
                aValueMap.put(key, map);
            }
            
            // TODO CCA: Object[][] (enthält String, File oder ImageReplacerData)
        }
    }
	
	// returns the file-Extension
	private static String getFileExtension(File aFile) {
	    String fileName = aFile.getName();
	    int dotPos = fileName.lastIndexOf('.');
	    return fileName.substring(dotPos);
	}

	public Reporter getReporter() {
		return clientReporter;
	}

	/**
	 * Convenience method to check the given value-map for special objects.
	 * If a value in the map is a Map, this map contains meta-information
	 * about how to handle the information in this Map.
	 * These maps are replaced with the objects they represent for further
	 * handling when generating an office-report.
	 */
	public static void handleSpecialContentForRead(Map valueMap) throws IOException {
		if (valueMap == null) {
			return;
		}
		
	    Set keys = valueMap.keySet();
	    Iterator iter = keys.iterator();
	    while (iter.hasNext()) {
	        String key = (String) iter.next();
	        Object obj = valueMap.get(key);
	        
	        // if the value is a Map, this map will have a TYPE.
	        // further processing depends on this TYPE
	        if (obj instanceof Map) {
	            Map map = (Map) obj;
	            String type = (String) map.get(ReportFactory.TYPE);
	            
	            // TYPE: File -> extract file from byte[]
	            if ("File".equals(type)) {
	                byte[] bytes = (byte[]) map.get(ReportFactory.VALUE);
	                String fileType = (String) map.get(ReportFactory.FILE_TYPE);
	                File file = writeBytes(bytes, fileType);
	                valueMap.put(key, file);
	            }
	            
	            // TYPE: ImageReplacerData -> extract file and date from map and create new ImageReplacerData 
	            else if ("ImageReplacerData".equals(type)) {
	                byte[] bytes = (byte[]) map.get(ReportFactory.VALUE);
	                String fileType = (String) map.get(ReportFactory.FILE_TYPE);
	                File file = writeBytes(bytes, fileType);
	                long hAlignment = ((Long) map.get("hAlignment")).longValue();
	                long vAlignment = ((Long) map.get("vAlignment")).longValue();
	                float hSize = ((Float)map.get("hSize")).floatValue();
	                float vSize = ((Float)map.get("vSize")).floatValue();
	                boolean lockRatio = ((Boolean)map.get("lockRatio")).booleanValue();
	                ImageReplacerData ird = new ImageReplacerData(file, hAlignment, vAlignment, hSize, vSize, lockRatio);
	                valueMap.put(key, ird);
	            }
	            
	            // TYPE: ExcelImage -> extract file and date from map and create new ExcelImage 
	            else if ("ExcelImage".equals(type)) {
	                byte[] bytes = (byte[]) map.get(ReportFactory.VALUE);
	                String fileType = (String) map.get(ReportFactory.FILE_TYPE);
	                File file = writeBytes(bytes, fileType);
	                boolean isCenterX = ((Boolean)map.get("centerX")).booleanValue();
	                boolean isCenterY = ((Boolean)map.get("centery")).booleanValue();
	                ExcelImage ei = new ExcelImage(file, isCenterX, isCenterY);
	                valueMap.put(key, ei);
	            }
	            
	            else {
	                Logger.info("Cannot handle value for key: '" + key + "'. Type unknown or null: '" + type + "'", ReportFactory.class);
	            }
	        }
	    }
	}

	/**
	 * Writes the given byte[] with the correct file-suffix into the TEMP-directory. 
	 * A full qualified filename is returned for the ReportCreator to find the
	 * image.  
	 * 
	 * @return a full qualified filename where the bytes are stored
	 */
	public static File writeBytes(byte[] bytes, String fileType) throws IOException {
	    String           fileName = ReportDescriptorImpl.getNewReportID();
	                     fileName = fileName + fileType;
	    File             img      = new File(ReportQueueConstants.TEMP_PATH + fileName);
	    FileOutputStream fos      = new FileOutputStream(img);
	    try {
	        fos.write(bytes);
	    } finally {
	        StreamUtilities.close(fos);
	    }
	    
	    return img;
	}

	/**
	 * Returns the sole instance of {@link ReportFactory}.
	 */
	public static ReportFactory getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	@Override
	protected void shutDown() {
		clientReporter.shutDown();
		super.shutDown();
	}
	
	public static final class Module extends TypedRuntimeModule<ReportFactory> {

		public static final Module INSTANCE = new Module();

		@Override
		public Class<ReportFactory> getImplementation() {
			return ReportFactory.class;
		}
	}
}
