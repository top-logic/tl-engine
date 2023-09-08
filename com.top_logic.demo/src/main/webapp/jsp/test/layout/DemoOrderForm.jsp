<%@page import="com.top_logic.layout.structure.OrientationAware.Orientation"
%><%@page import="com.top_logic.layout.form.template.BeaconFormFieldControlProvider"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><%

%><%@page import="com.top_logic.layout.basic.PlainTextRenderer"
%><%@page import="java.util.Collections"
%><%@page import="com.top_logic.layout.basic.GoogleSearchRenderer"
%><%@page import="com.top_logic.layout.provider.DefaultLabelProvider"
%><%@page import="com.top_logic.demo.layout.form.demo.DemoOrderForm"
%><layout:html>
	<layout:head>
		<style type="text/css">
			table.gridTable {
				border-collapse: collapse;
			}
			
			table.gridTable td {
				border-style: solid;
				border-color: black;
				border-width: 1px;
			}
			
			.errorDisplay {
				padding: 1ex;
				border-style: solid;
				border-color: red;
				border-width: 3px;
			}
			
			.problematic label {
				color: red;
			}
		</style>
	</layout:head>
	<layout:body>
		<h1>
			Demo Order Form with AJAX
		</h1>

		<form:form>
			<form:onerror id="order-errors"
				name="order"
			>
				<div class="errorDisplay">
					The the order-part of the form has the following errors:
					<form:errors id="oder-errors-display"
						icon="false"
						names="order"
					/>
				</div>
			</form:onerror>

			<form:scope name="order">
				<form:group name="purchaser"
					collapsible="true"
				>
					<table class="gridTable">
						<form:onvisible id="gender-row"
							localName="tbody"
							name="gender"
						>
							<tr>
								<td>
									<form:label name="gender"/>
								</td>
								<td colspan="5">
									<form:choice name="gender"/>
									<form:error name="gender"/>
								</td>
							</tr>
						</form:onvisible>

						<tr>
							<td style="vertical-align: top;">
								<form:label name="title"/>
							</td>
							<td colspan="5">
								<form:choice name="title"
									orientation="<%=Orientation.HORIZONTAL %>"
								/>
								<form:error name="title"/>
							</td>
						</tr>

						<tr>
							<td colspan="6">
								An alternative display would be the following: Please select your
								gender. If your feel like a
								<form:label name="gender2"
									index="0"
								/>
								,
								please select this option:
								<form:option name="gender2"
									index="0"
								/>
								.
								Otherwise, you potentially are a
								<form:label name="gender2"
									index="1"
								/>
								and you should select this one:
								<form:option name="gender2"
									index="1"
								/>
								.
							</td>
						</tr>

						<tr>
							<td colspan="6">
								The following titles are known:
								<table>
									<tr>
										<td>
											<form:label name="title2"
												index="0"
											/>
										</td>
										<td>
											<form:option name="title2"
												index="0"
											/>
										</td>
										<td>
											<form:label name="title2"
												index="1"
											/>
										</td>
										<td>
											<form:option name="title2"
												index="1"
											/>
										</td>
										<td>
											<form:label name="title2"
												index="2"
											/>
										</td>
										<td>
											<form:option name="title2"
												index="2"
											/>
										</td>
									</tr>
									<tr>
										<td>
											<form:label name="title2"
												index="3"
											/>
										</td>
										<td>
											<form:option name="title2"
												index="3"
											/>
										</td>
										<td>
											<form:label name="title2"
												index="4"
											/>
										</td>
										<td>
											<form:option name="title2"
												index="4"
											/>
										</td>
										<td>
											<form:label name="title2"
												index="5"
											/>
										</td>
										<td>
											<form:option name="title2"
												index="5"
											/>
										</td>
									</tr>
								</table>
							</td>
						</tr>

						<tr>
							<td>
								Toggle visibility of name fields:
							</td>
							<td colspan="5">
								<form:button command="toggleNameVisibility"/>
							</td>
						</tr>

						<form:block id="nameRows"
							initiallyVisible="false"
							localName="tbody"
						>
							<tr>
								<td>
									<form:label name="givenName"/>
								</td>
								<td>
									<form:input name="givenName"/>
									<form:error name="givenName"
										icon="true"
									/>
								</td>
								<td>
									<form:label name="surname"/>
								</td>
								<td>
									<form:input name="surname"/>
									<form:error name="surname"/>
									<form:button
										command="fillInDefault"
										image="theme:TREE_NEW_ELEMENT"
									/>
								</td>
								<td>
									<form:label name="dateOfBirth"/>
								</td>
								<td>
									<form:date name="dateOfBirth"/>
									<form:error name="dateOfBirth"/>
								</td>
							</tr>
							<tr>
								<td>
									<form:label name="mailAddress1"/>
								</td>
								<td>
									<form:input name="mailAddress1"
										columns="20"
									/>
								</td>
								<td>
									<form:label name="mailAddress2"/>
								</td>
								<td>
									<form:input name="mailAddress2"
										columns="20"
									/>
								</td>
								<td>
									<form:label name="mailAddress3"/>
								</td>
								<td>
									<form:input name="mailAddress3"
										columns="20"
									/>
								</td>
							</tr>
							<tr>
								<td colspan="2">
									<form:error name="mailAddress1"/>
								</td>
								<td colspan="2">
									<form:error name="mailAddress2"/>
								</td>
								<td colspan="2">
									<form:error name="mailAddress3"/>
								</td>
							</tr>
							<tr>
								<td>
									<form:label name="mailAddresses"/>
								</td>
								<td colspan="5">
									<form:input name="mailAddresses"
										columns="40"
									/>
									<form:error name="mailAddresses"/>
								</td>
							</tr>
						</form:block>
					</table>
				</form:group>

				<form:group name="deliveryAddress"
					collapsible="true"
				>
					<table>
						<tr>
							<td>
								<form:label name="street"/>
							</td>
							<td>
								<form:input name="street"/>
								<form:error name="street"/>
							</td>
						</tr>

						<tr>
							<td>
								<form:label name="streetNumber"/>
							</td>
							<td>
								<form:input name="streetNumber"/>
								<form:error name="streetNumber"/>
							</td>
						</tr>

						<tr>
							<td>
								<form:label name="zipCode"/>
							</td>
							<td>
								<form:input name="zipCode"/>
								<form:error name="zipCode"/>
							</td>
						</tr>

						<tr>
							<td>
								<form:label name="city"/>
							</td>
							<td>
								<form:popup name="city"/>
								<form:error name="city"/>
							</td>
						</tr>

						<tr>
							<td>
								<form:label name="cities"/>
							</td>
							<td>
								<form:popup name="cities"/>
								<form:error name="cities"/>
							</td>
						</tr>
					</table>
				</form:group>

				<form:table name="lastAddresses"
					columnNames="street streetNumber zipCode city"
					rowMove="true"
					selectable="true"
				>
					<form:button
						command="<%=DemoOrderForm.NewAddressAction.COMMAND_ID %>"
						image="theme:TREE_NEW_ELEMENT"
					/>
				</form:table>

				<table>
					<tr>
						<td>
							<form:label name="satisfaction"/>
						</td>
						<td>
							<form:beacon name="satisfaction"
								type="<%=BeaconFormFieldControlProvider.VAL_TYPE_BEACON%>"
							/>
						</td>
						<td>
							<form:error name="satisfaction"/>
						</td>
					</tr>

					<tr>
						<td>
							<form:label name="evaluation"/>
						</td>
						<td>
							<form:input name="evaluation"
								columns="40"
								multiLine="true"
								rows="8"
							/>
						</td>
						<td>
							<form:error name="evaluation"/>
						</td>
					</tr>

					<tr>
						<td>
							<form:label name="evaluationPreview"/>
						</td>
						<td>
							<form:input name="evaluationPreview"
								multiLine="true"
							/>
						</td>
						<td>
							<form:error name="evaluationPreview"/>
						</td>
					</tr>

					<tr>
						<td>
							<form:label name="isbn"/>
						</td>
						<td>
							<form:input name="isbn"/>
							<form:error name="isbn"/>
						</td>
					</tr>

					<tr>
						<td>
							<form:label name="DUNS"/>
						</td>
						<td>
							<form:input name="DUNS"/>
							<form:error name="DUNS"/>
						</td>
					</tr>

					<tr>
						<td>
							<form:label name="options"/>
						</td>
						<td>
							<form:popup name="options"
								clearButton="true"
							/>
							<form:error name="options"/>
						</td>
					</tr>

					<tr>
						<td>
							<form:label name="multiOptions"/>
						</td>
						<td>
							<form:popup name="multiOptions"
								clearButton="true"
							/>
							<form:error name="multiOptions"/>
						</td>
					</tr>

					<tr>
						<td>
							<form:label name="number"/>
						</td>
						<td>
							<form:popup name="number"
								clearButton="true"
							/>
							<form:error name="number"/>
						</td>
					</tr>

					<tr>
						<td>
							<form:label name="numbers"/>
						</td>
						<td>
							<form:popup name="numbers"
								clearButton="true"
							/>
							<form:error name="numbers"/>
						</td>
					</tr>
				</table>

				<p>
					<form:label name="orderDate"/>
					<form:date name="orderDate"/>
					<form:error name="orderDate"/>
				</p>

				<p>
					<form:label name="colorChooser"/>
					<form:colorchooser name="colorChooser"/>
				</p>

				<p>
					<form:error name="carrier"/>
					<form:error name="dialPrefix"/>
					<form:error name="phoneNumber"/>
					<form:select name="carrier"/>
					<form:select name="dialPrefix"/>
					-
					<form:input name="phoneNumber"/>
				</p>

				<p>
					<form:label name="directDialPrefix"/>
					<form:select name="directDialPrefix"/>
					<form:error name="directDialPrefix"/>
				</p>

				<p>
					<form:label name="hasSeparateBillingAddress"/>
					<form:checkbox name="hasSeparateBillingAddress"
						yesNo="true"
					/>
					<form:error name="hasSeparateBillingAddress"/>
				</p>

				<form:group name="billingAddress">
					<table>
						<tr>
							<td>
								<form:label name="street"/>
							</td>
							<td>
								<form:input name="street"/>
								<form:error name="street"/>
							</td>
						</tr>

						<tr>
							<td>
								<form:label name="streetNumber"/>
							</td>
							<td>
								<form:input name="streetNumber"/>
								<form:error name="streetNumber"/>
							</td>
						</tr>

						<tr>
							<td>
								<form:label name="zipCode"/>
							</td>
							<td>
								<form:input name="zipCode"/>
								<form:error name="zipCode"/>
							</td>
						</tr>

						<tr>
							<td>
								<form:label name="city"/>
							</td>
							<td>
								<form:popup name="city"/>
								<form:error name="city"/>
							</td>
						</tr>
					</table>
				</form:group>

				<p>
					<form:label name="needEnsurance"/>
					<form:tristate name="needEnsurance"/>
					<form:error name="needEnsurance"/>
				</p>
			</form:scope>

			<form:group name="reflection">
				<table>
					<thead>
						<tr>
							<th>
								field/property
							</th>

							<th>
								visible
							</th>
							<th>
								immutable
							</th>
							<th>
								disabled
							</th>
							<th>
								frozen
							</th>
							<th>
								blocked
							</th>
							<th>
								mandatory
							</th>
							<th>
								value
							</th>
						</tr>
					</thead>
					<tbody>
						<form:forEach member="group">
							<tr>
								<td>
									<form:label name="${group}"/>
								</td>

								<form:scope name="${group}">
									<form:for
										in="visible immutable disabled frozen blocked mandatory"
										var="field"
									>
										<!-- value -->
										<td style="text-align: center;">
											<form:ifExists name="${field}">
												<form:checkbox name="${field}"/>
											</form:ifExists>
										</td>
									</form:for>
								</form:scope>
							</tr>
						</form:forEach>
					</tbody>
				</table>
			</form:group>
		</form:form>
	</layout:body>
</layout:html>