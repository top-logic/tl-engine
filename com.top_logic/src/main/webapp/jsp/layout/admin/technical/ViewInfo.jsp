<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		<style type="text/css">
			form.frm: {
				padding-left: 10px;
				padding-right: 10px;
			}
		</style>
	</layout:head>
	<layout:body>
		<form:form>
			<form:groupCell titleKeySuffix="basic">
				<!-- summary="Security component info" -->
				<form:descriptionCell wholeLine="true">
					<form:description>
						<form:label name="component"/>
						<form:error name="component"/>
					</form:description>
					<form:cell>
						<form:input name="component"/>
						<form:command name="component__class"/>
					</form:cell>
				</form:descriptionCell>

				<form:cell>
					<form:inputCell name="location"/>
				</form:cell>

				<form:cell wholeLine="true">
					<form:inputCell name="prefix"/>
				</form:cell>
			</form:groupCell>

			<form:groupCell titleKeySuffix="real">
				<!-- summary="Real component info" -->
				<form:ifExists name="real_component">
					<form:descriptionCell wholeLine="true">
						<form:description>
							<form:label name="real_component"/>
							<form:error name="real_component"/>
						</form:description>
						<form:cell>
							<form:input name="real_component"/>
							<form:command name="real_component__class"/>
						</form:cell>
					</form:descriptionCell>

					<form:cell wholeLine="true">
						<form:inputCell name="real_location"/>
					</form:cell>

					<form:cell wholeLine="true">
						<form:inputCell name="real_prefix"/>
					</form:cell>

					<form:descriptionCell wholeLine="true">
						<form:description>
							<form:label name="real_model"/>
						</form:description>
						<form:cell>
							<form:input name="real_model"/>
							<form:command name="real_model__class"/>
						</form:cell>
					</form:descriptionCell>
					<form:separator/>
				</form:ifExists>

				<form:ifExists name="real_master_component">
					<form:descriptionCell wholeLine="true">
						<form:description>
							<form:label name="real_master_component"
								colon="true"
							/>
						</form:description>
						<form:cell>
							<form:input name="real_master_component"/>
							<form:command name="real_master_component__class"/>
						</form:cell>
					</form:descriptionCell>

					<form:cell wholeLine="true">
						<form:inputCell name="real_master_location"
							colon="true"
						/>
					</form:cell>

					<form:cell wholeLine="true">
						<form:inputCell name="real_master_prefix"
							colon="true"
						/>
					</form:cell>

					<form:descriptionCell wholeLine="true">
						<form:description>
							<form:label name="real_master_model"
								colon="true"
							/>
						</form:description>
						<form:cell>
							<form:input name="real_master_model"/>
							<form:command name="real_master_model__class"/>
						</form:cell>
					</form:descriptionCell>
					<form:separator/>
				</form:ifExists>

				<form:ifExists name="button_component">
					<form:descriptionCell wholeLine="true">
						<form:description>
							<form:label name="button_component"
								colon="true"
							/>
						</form:description>
						<form:cell>
							<form:input name="button_component"/>
							<form:command name="button_component__class"/>
						</form:cell>
					</form:descriptionCell>
					<form:separator/>
				</form:ifExists>

				<form:ifExists name="real_commands">
					<form:descriptionCell wholeLine="true">
						<form:description>
							<form:label name="real_commands"
								colon="true"
							/>
						</form:description>

						<form:cell>
							<form:scope name="real_commands">
								<table style="width: 100%;">
									<form:forEach member="command">
										<tr>
											<form:scope name="${command}">
												<td class="content">
													<form:input name="id"/>
												</td>
												<td class="content">
													<form:input name="cmdGroup"/>
												</td>
												<td class="content"
													width="100%"
												>
													<form:input name="class"/>
												</td>
											</form:scope>
										</tr>
									</form:forEach>
								</table>
							</form:scope>
						</form:cell>
					</form:descriptionCell>
				</form:ifExists>
			</form:groupCell>
		</form:form>
	</layout:body>
</layout:html>