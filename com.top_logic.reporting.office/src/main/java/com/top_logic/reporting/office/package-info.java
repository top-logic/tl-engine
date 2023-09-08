/**
 * Interfaces and basic classes office reporting.
 * 
 * <p>
 * The main access to generate a report is the
 * {@link com.top_logic.reporting.office.ReportGenerator}. This singleton is used to process a
 * report in three major steps:
 * </p>
 * <ol>
 * <li>request a report
 * {@link com.top_logic.reporting.office.ReportGenerator#requestReport(String, java.util.Map, com.top_logic.base.user.UserInterface)}</li>
 * <li>check if the report is finished
 * {@link com.top_logic.reporting.office.ReportGenerator#reportReady(ReportToken)}</li>
 * <li>get the report result
 * {@link com.top_logic.reporting.office.ReportGenerator#getReportResult(ReportToken)} if the report
 * processing is finished</li>
 * </ol>
 * <p>
 * To identify a report in the chain of processing a
 * {@link com.top_logic.reporting.office.ReportToken} is used.<br />
 * The main interface to handle the real generation of a report is
 * {@link com.top_logic.reporting.office.ReportHandler}. Implementations of this interface take care
 * of the different specialties certain report require. For example the generation of a table with
 * stylesheets in powerpoint is difficult to predict so the specific implementation of the report
 * takes care of these tasks. For that purpose the generation of a report is handled also in a three
 * step process in the {@link com.top_logic.reporting.office.ReportHandler}:
 * </p>
 * <ol>
 * <li>preparation</li>
 * <li>processing</li>
 * <li>finishing</li>
 * </ol>
 */
package com.top_logic.reporting.office;
