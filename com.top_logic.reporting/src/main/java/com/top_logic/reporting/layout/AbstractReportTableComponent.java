/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;
import com.top_logic.tool.export.ExcelExportHandler;
import com.top_logic.tool.export.ExportAware;

/**
 * A WrapperTable that allows export to Excel.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractReportTableComponent extends TableComponent implements ExportAware {

	public interface Config extends TableComponent.Config {
		@Name("editComponent")
		String getEditComponent();

		@Name("exporterClass")
		@InstanceFormat
		ExportAware getExporterClass();

		@Override
		@ItemDefault
		@ImplementationClassDefault(ReportTableModelBuilder.class)
		public PolymorphicConfiguration<? extends ListModelBuilder> getModelBuilder();

	}

	
    public static final String EXPORT_RES_PREFIX = "_exportResPrefix";
    
    public static final String EXPORT_MODEL = "_exportModel";

    public static final String EXPORT_DOWNLOAD_NAME = "_exportDownload";

    public static final String EXPORT_TEMPLATE_NAME = "_exportTemplate";
    
    public static final String EXPORT_COMPONENT = "_exportComponent";

    /** Component to jump to, if there is a special goto link defined. */ 
    private String editComponent;
    private ExportAware exporterClass;

    /** 
     * Create a new instance of this class.
     */
    public AbstractReportTableComponent(InstantiationContext context, Config someAttr) throws ConfigurationException {
        super(context, someAttr);

        this.editComponent = StringServices.nonEmpty(someAttr.getEditComponent());
        this.exporterClass = (someAttr.getExporterClass() == null) ? this : someAttr.getExporterClass();

        this.setSelectable(false);
    }

    /** 
     * name of the (Excel File) to download.
     */
    protected abstract String getDownloadName();

    /** 
      * path relative to {@link ExcelExportHandler#getRelativeTemplatePath()} 
     *          of the Excel template file to use.
     */
    protected abstract String getExcelTemplateName();

    /**
     * Export all using an almost empty template.
     */
    @Override
	public OfficeExportValueHolder getExportValues(DefaultProgressInfo progressInfo, Map arguments) {
        if (this.exporterClass == this) {
            return new OfficeExportValueHolder(this.getExcelTemplateName(), this.getDownloadName(), this.getExportValues(), true);
        }
        else {
            HashMap theMap = new HashMap(arguments);

            theMap.put(EXPORT_RES_PREFIX, this.getResPrefix());
            theMap.put(EXPORT_MODEL, getViewModel().getDisplayedItemOrder((List) this.getModel()));
            theMap.put(EXPORT_DOWNLOAD_NAME, this.getDownloadName());
            theMap.put(EXPORT_TEMPLATE_NAME, this.getExcelTemplateName());
            theMap.put(EXPORT_COMPONENT, this);
            return this.exporterClass.getExportValues(progressInfo, theMap);
        }
    }

    public ReportDescription getReportFilter() {
        LayoutComponent theMaster = this.getMaster();

        if (theMaster instanceof AbstractReportFilterComponent) {
            return ((AbstractReportFilterComponent) theMaster).getReportFilter();
        }
        else {
            return (null);
        }
    }

    /** 
     * Return the values for the export (the table control in this implementation). 
     * 
     * @return    The requested values.
     */
    protected Object getExportValues() {
        return (this.getTableControl());
    }

    protected String getEditComponent() {
        return (this.editComponent);
    }

	public static class ReportTableModelBuilder implements ListModelBuilder {

		/**
		 * Singleton {@link ReportTableModelBuilder} instance.
		 */
		public static final ReportTableModelBuilder INSTANCE = new ReportTableModelBuilder();

		private ReportTableModelBuilder() {
			// Singleton constructor.
		}

        /**
         * @see com.top_logic.mig.html.ModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
         */
        @Override
		public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
            LayoutComponent theMaster = aComponent.getMaster();

            if (theMaster instanceof AbstractReportFilterComponent) {
                AbstractReportFilterComponent theFilter = (AbstractReportFilterComponent) theMaster;

                this.processReportFilter(aComponent, theFilter);

                return (theFilter.getResultList());
            }
            else {
                return (Collections.EMPTY_LIST);
            }
        }

        /**
         * @see com.top_logic.mig.html.ModelBuilder#supportsModel(java.lang.Object, LayoutComponent)
         */
        @Override
		public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
            return (aModel instanceof Collection);
        }
        
		@Override
		public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
			return true;
		}

		@Override
		public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
			return contextComponent.getModel();
		}

        /** 
         * Hook for sub classes to handle special stuff.
         * 
         * @param    aComponent    The component calling this method.
         * @param    aMaster       The master component of the calling one.
         */
        protected void processReportFilter(LayoutComponent aComponent, AbstractReportFilterComponent aMaster) {
        }
    }
}
