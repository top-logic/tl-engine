<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.knowledge.wrap.AbstractWrapper"
%><%@page import="com.top_logic.element.layout.meta.search.SearchFieldSupport"
%><%@page import="com.top_logic.layout.SimpleAccessor"
%><%@page import="com.top_logic.layout.table.I18NCellRenderer"
%><%@page import="com.top_logic.layout.table.renderer.DefaultTableRenderer"
%><%@page import="com.top_logic.model.TLClass"
%><%@page import="com.top_logic.element.layout.meta.search.AttributedSearchComponent"
%><%@page import="com.top_logic.element.layout.meta.search.QueryUtils"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="meta" prefix="meta"
%><%@taglib uri="util" prefix="util"
%><%AttributedSearchComponent theComp = (AttributedSearchComponent) MainLayout.getComponent(pageContext);
TLClass               theME   = theComp.getSearchMetaElement();
DefaultTableRenderer theRenderer = DefaultTableRenderer.newInstance();
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<table>
				<tr>
					<form:ifExists name="<%=SearchFieldSupport.META_ELEMENT %>">
						<td>
							<form:label name="<%=SearchFieldSupport.META_ELEMENT %>"
								colon="true"
							/>
						</td>
						<td>
							<form:select name="<%=SearchFieldSupport.META_ELEMENT %>"
								width="120px"
							/>
						</td>
					</form:ifExists>
					<td class="label">
						<form:label name="<%=SearchFieldSupport.STORED_QUERY %>"
							colon="true"
						/>
					</td>
					<td class="content">
						<form:select name="<%=SearchFieldSupport.STORED_QUERY %>"
							width="120px"
						/>
					</td>
					<%@include file="/jsp/element/metaattributes/PublishForm_inline.inc" %>
				</tr>
			</table>
			<table>
				<tr>
					<td
						valign="top"
						width="100%"
					>
						<!-- Standard parameter and extended search -->
						<table width="100%">
							<tr>
								<td>
									<meta:group object="<%= theME %>">
										<basic:fieldset titleKeySuffix="basic">
											<table style="margin-top: 10px;">
												<tr>
													<td class="label">
														<meta:label name="<%=AbstractWrapper.NAME_ATTRIBUTE %>"
															colon="true"
														/>
													</td>
													<td class="content">
														<meta:attribute name="<%=AbstractWrapper.NAME_ATTRIBUTE %>"/>
													</td>
												</tr>
											</table>
										</basic:fieldset>
										<util:if condition="<%= theComp.isExtendedSearch() %>">
											<meta:attributes
												exclude="<%=theComp.getExcludeSet() %>"
												legend="additional"
											/>
										</util:if>
									</meta:group>
								</td>
							</tr>
						</table>
					</td>
					<td valign="top">
						<!-- column selection -->
						<basic:fieldset titleKeySuffix="attributes.columns">
							<form:custom name="<%= SearchFieldSupport.TABLE_COLUMNS %>"
								controlProvider="<%= theComp.TABLE_PROVIDER %>"
							/>
							<form:error name="<%= SearchFieldSupport.TABLE_COLUMNS %>"/>
						</basic:fieldset>
					</td>
				</tr>
			</table>
		</form:form>
	</layout:body>
</layout:html>