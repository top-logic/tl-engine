<%@page import="com.top_logic.layout.form.PlainKeyResources"
%><%@page import="com.top_logic.reporting.flex.chart.config.util.ChartThemeConfigurator"
%><%@page import="com.top_logic.layout.form.model.StringField"
%><%@page import="com.top_logic.layout.LabelProvider"
%><%@page import="org.jfree.chart.StandardChartTheme"
%><%@page import="java.util.List"
%><%@page import="java.util.ArrayList"
%><%@page import="com.top_logic.layout.form.model.FormFactory"
%><%@page import="org.jfree.chart.ChartTheme"
%><%@page import="org.jfree.chart.ChartFactory"
%><%@page import="com.top_logic.layout.form.model.SelectField"
%><%@page import="com.top_logic.layout.basic.Command"
%><%@page import="com.top_logic.layout.basic.CommandModel"
%><%@page import="com.top_logic.layout.form.model.CommandField"
%><%@page import="com.top_logic.tool.boundsec.HandlerResult"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@page import="com.top_logic.layout.form.template.ButtonControlProvider"
%><%@page import="com.top_logic.layout.form.template.DefaultFormFieldControlProvider"
%><%@page import="com.top_logic.layout.Control"
%><%@page import="com.top_logic.layout.form.FormMember"
%><%@page import="java.io.IOException"
%><%@page import="com.top_logic.basic.xml.TagWriter"
%><%@page import="com.top_logic.layout.DisplayContext"
%><%@page import="com.top_logic.layout.form.model.FormContext"
%><%@page import="java.net.URI"
%><%@page extends="com.top_logic.util.TopLogicJspBase"
contentType="text/html; charset=UTF-8"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head>
		<title>
			Senderstatus administratieren
		</title>
		<meta
			content="text/html; charset=iso-8859-1"
			http-equiv="Content-Type"
		/>
		<basic:cssLink/>
	</layout:head>
	
	<%!public FormContext getFormContext() {
		
		final LabelProvider provider = new LabelProvider() {
			
			@Override
			public String getLabel(Object object) {
				return ((StandardChartTheme)object).getName();
			}
		};
		
		FormContext result = new FormContext("context", PlainKeyResources.INSTANCE);
		StandardChartTheme theme = (StandardChartTheme)ChartFactory.getChartTheme();
		String label = provider.getLabel(theme);
		List<ChartTheme> options = new ArrayList<>();
		List<ChartTheme> init = new ArrayList<>();
		if (ChartThemeConfigurator.Module.INSTANCE.isActive()) {
			ChartThemeConfigurator configurator = ChartThemeConfigurator.getInstance();
			for (String name : configurator.getThemeNames()) {
				ChartTheme option = configurator.getTheme(name);
				if (name.equals(theme.getName())) {
					init.add(option);
				}
				options.add(option);
			}
		}
		else {
			options.add(StandardChartTheme.createDarknessTheme());
			options.add(StandardChartTheme.createJFreeTheme());
			options.add(StandardChartTheme.createLegacyTheme());
			if (label.equals("JFree")) {
				init.add(options.get(1));
			}
			else if (label.equals("Legacy")) {
				init.add(options.get(2));
			}
			else {
				init.add(options.get(0));
			}
		}
		
		
		SelectField selectField = FormFactory.newSelectField("select", options, false, init, true, false, null);
		selectField.setOptionLabelProvider(provider);
		
		StringField currentField = FormFactory.newStringField("current", label, true);
		
		CommandField applyField = getCommandField(selectField, currentField, provider);
		
		result.addMember(currentField);
		result.addMember(selectField);
		result.addMember(applyField);
		
		return result;
	}
	
	private CommandField getCommandField(final SelectField selectField, final StringField currentField, final LabelProvider provider) {
		CommandField result = FormFactory.newCommandField("apply", new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					ChartTheme theme = (ChartTheme)selectField.getSingleSelection();
					
					if (ChartThemeConfigurator.Module.INSTANCE.isActive()) {
						ChartThemeConfigurator.getInstance().setDefaultTheme((StandardChartTheme)theme);
					}
					else {
						ChartFactory.setChartTheme(theme);
					}
					currentField.setValue(provider.getLabel(theme));
					return HandlerResult.DEFAULT_RESULT;
				}
		});
		result.setLabel("Übernehmen");
		result.setControlProvider(ButtonControlProvider.INSTANCE);
		return result;
	}
	
	private void writeField(FormContext context, String name, DisplayContext displayContext, TagWriter writer) throws IOException {
		FormMember member = context.getMember(name);
		Control control = DefaultFormFieldControlProvider.INSTANCE.createControl(member, null);
		control.write(displayContext, writer);
		writer.flushBuffer();
	}%>
	<layout:body>
		<basic:access>
			<div style="margin: 2em;">
				<%
				DisplayContext dc = DefaultDisplayContext.getDisplayContext(pageContext);
				TagWriter writer = MainLayout.getTagWriter(pageContext);
				FormContext fc = getFormContext();
				%>
				<fieldset class="expanded">
					<legend>
						Standard Chart-Theme anpassen
					</legend>
					<table border="1">
						<tr>
							<td align="right">
								Aktuell
							</td>
							<td>
								<%
								writeField(fc, "current", dc, writer);
								%>
							</td>
						</tr>
						<tr>
							<td align="right">
								Auswahl
							</td>
							<td>
								<%
								writeField(fc, "select", dc, writer);
								%>
							</td>
						</tr>
						<tr>
							<td align="right">
							</td>
							<td align="center">
								<%
								writeField(fc, "apply", dc, writer);
								%>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
		</basic:access>
	</layout:body>
</layout:html>