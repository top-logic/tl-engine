<%@page extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><%@page import="java.util.ArrayList"
%><%@page import="java.util.Iterator"
%><%@page import="java.util.Map"
%><%@page import="com.top_logic.basic.DebugHelper"
%><%@page import="com.top_logic.base.administration.PerformanceTests"
%><%@page import="com.top_logic.base.administration.TLPerformanceTests"%>

<%
// Use here your subclass of PerformanceTests if you want to have your custom tests:
PerformanceTests tests = new TLPerformanceTests();
long startTime = System.currentTimeMillis();
%><layout:html>
	<layout:head>
		<title>
			Performance Test
		</title>
		<meta
			content="text/html; charset=iso-8859-1"
			http-equiv="Content-Type"
		/>
		<basic:script>
			services.ajax.ignoreTLAttributes = true;
		</basic:script>
		<basic:script>
			function selectAllTests(check) {
				for (i = 0; i < document.forms[0].elements.length; i++) {
					if (document.forms[0].elements[i].type == "checkbox") {
						document.forms[0].elements[i].checked = check;
					}
				}
			}
		</basic:script>
	</layout:head>

	<layout:body>
		<basic:access>
			<h1>
				Performance Test
			</h1>
			<%
			if (request.getParameter("SUBMIT") != null) {
				%>
				<table>
					<tr>
						<td>
							<code class="normal">
								<%
								tests.setWriter(out);
								tests.logMessage("Received test request at " + DebugHelper.formatTime(startTime) + ".");
								ArrayList theTestList = new ArrayList();
								Iterator it = tests.getDescriptionMap().keySet().iterator();
								while (it.hasNext()) {
									String theName = (String)it.next();
									if (request.getParameter(theName) == null) continue;
									theTestList.add(theName);
								}
								tests.runTests(theTestList);
								tests.logMessage("Request duration: " + DebugHelper.getTime(System.currentTimeMillis() - startTime));
								tests.setWriter(null);
								%>
							</code>
						</td>
					</tr>
				</table>
				<br/>
				<form method="POST">
					<table>
						<tr>
							<td>
								<%
								it = theTestList.iterator();
								while (it.hasNext()) {
									String theName = (String)it.next();
									%>
									<input name="<%=theName%>"
										checked="checked"
										type="hidden"
										value="<%=theName%>"
									/>
									<%
								}
								%>
								<p>
									<input name="OK"
										type="submit"
										value="   OK   "
									/>
								</p>
							</td>
						</tr>
					</table>
				</form>
				<br/>
				<%
			}
			else {
				%>
				<p>
					This page allows to do some performance tests. You should be care of how many users
					are working on the system when running the tests, which may influence the test results.
				</p>
				<br/>
				<p>
					<b>Please select the tests to run:</b>
				</p>
				<form method="POST">
					<table>
						<tr>
							<td>
								<p>
									<span style="cursor:default;">
										<b>
											Select:
										</b>
										&#xA0;&#xA0;&#xA0;
										<a
											onclick="selectAllTests(true);"
											onmouseout="window.status='';"
											onmouseover="window.status='Select all tests.';"
											style="cursor:pointer;"
										>
											all
										</a>
										&#xA0;&#xA0;&#xA0;
										<a
											onclick="selectAllTests(false);"
											onmouseout="window.status='';"
											onmouseover="window.status='Select no test.';"
											style="cursor:pointer;"
										>
											none
										</a>
									</span>
								</p>
								<%
								boolean checked, initial = (request.getParameter("OK") == null);
								Iterator it = tests.getDescriptionMap().entrySet().iterator();
								%>
								<div class="cChoice cChoice-vertical" style="gap: var(--spacing-03);">
								<%
								while (it.hasNext()) {
									Map.Entry theEntry = (Map.Entry)it.next();
									String theName = (String)theEntry.getKey();
									String theDescription = (String)theEntry.getValue();
									checked = (initial || request.getParameter(theName) != null);
									%>
										<div style="display: flex; flex-direction: column; gap: var(--spacing-02);">
											<div class="cChoice-option" style="gap: var(--spacing-02)">
												<input name="<%=theName%>"
													checked="<%=checked ? "checked" : "" %>"
													type="checkbox"
													value="<%=theName%>"
												/>
												<b><%=theName%></b>
											</div>
											<span>
												<%=theDescription%>
											</span>
										</div>
									<%
								}
								%>
								</div>
								<p>
									<button class="tlButton cButton cmdButton" name="SUBMIT" type="submit">
										<h4 class="tlButtonLabel">Run Tests</h4>
									</button>
								</p>
							</td>
						</tr>
					</table>
				</form>
				<br/>
				<%
			}
			%>
		</basic:access>
	</layout:body>
</layout:html>