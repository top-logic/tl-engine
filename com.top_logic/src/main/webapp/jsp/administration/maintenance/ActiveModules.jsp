<%@page import="com.top_logic.layout.ResPrefix"
%><%@page import="java.io.IOException"
%><%@page import="com.top_logic.layout.form.control.ButtonControl"
%><%@page import="com.top_logic.basic.util.ResKey"
%><%@page import="com.top_logic.layout.provider.ImageButtonControlProvider"
%><%@page import="com.top_logic.layout.form.tag.PopupSelectTag"
%><%@page import="com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo.AbstractStaticInfoPlugin"
%><%@page import="com.top_logic.layout.form.model.CommandField"
%><%@page import="com.top_logic.layout.basic.fragments.Fragments"
%><%@page import="java.util.Collections"
%><%@page import="com.top_logic.layout.DisplayContext"
%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.mig.html.layout.LayoutComponent"
%><%@page import="com.top_logic.mig.html.HTMLConstants"
%><%@page import="com.top_logic.basic.xml.TagWriter"
%><%@page import="com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator"
%><%@page import="com.top_logic.layout.form.tag.Icons"
%><%@page import="java.util.Set"
%><%@page import="java.util.HashMap"
%><%@page import="java.util.ArrayList"
%><%@page import="java.util.List"
%><%@page import="java.util.Enumeration"
%><%@page import="com.top_logic.basic.module.BasicRuntimeModule"
%><%@page import="java.util.Collection"
%><%@page import="com.top_logic.basic.module.ModuleUtil"
%><%@page language="java" session="true"
extends="com.top_logic.util.TopLogicJspBase"%><%@page
import="java.util.Comparator"%><%@page import="java.util.TreeMap"%><%@page
import="java.util.Map"%><%@taglib uri="basic" prefix="basic"%><%@taglib
uri="layout" prefix="layout"%>
<%!
String shortName(BasicRuntimeModule<?> module) {
	String qname = module.getImplementation().getName();
	int dotIndex = qname.lastIndexOf('.');
	String shortName = qname.substring(dotIndex + 1);
	return shortName;
}

String pkg(BasicRuntimeModule<?> module) {
	String qname = module.getImplementation().getName();
	int dotIndex = qname.lastIndexOf('.');
	return qname.substring(0, dotIndex);
}

void inspect(DisplayContext context, TagWriter out, LayoutComponent component, BasicRuntimeModule<?> module) throws IOException {
	CommandField inspectButton = AbstractStaticInfoPlugin.createInspectButton(component.getMainLayout(), "inspect", module.getImplementationInstance());
	if (inspectButton != null) {
		inspectButton.setImage(Icons.OPEN_CHOOSER);
		inspectButton.setControlProvider(ImageButtonControlProvider.INSTANCE);
		inspectButton.setCssClasses("appended");
		inspectButton.setResources(ResPrefix.NONE);
		inspectButton.setLabel(
			context.getResources().getString(
		com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo.I18NConstants.SHOW_DETAIL_COMMAND));
		new ButtonControl(inspectButton).write(context, out);
	}
}
%><layout:html>
	<layout:head>
		<title>
			Active Modules
		</title>
		<meta
			content="text/html; charset=iso-8859-1"
			http-equiv="Content-Type"
		/>
		<basic:cssLink/>
	</layout:head>
	<layout:body>
		<%
		LayoutComponent component = MainLayout.getComponent(pageContext);
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(pageContext);
		
		List<BasicRuntimeModule<?>> activeModules = new ArrayList<>(ModuleUtil.INSTANCE.getActiveModules());
		
		Collections.sort(activeModules, new Comparator<BasicRuntimeModule<?>>() {
				@Override
				public int compare(BasicRuntimeModule<?> o1, BasicRuntimeModule<?> o2) {
					return shortName(o1).compareTo(shortName(o2));
				}
		});
		if (request.getParameter ("RELOAD") != null) {
			Map<String, BasicRuntimeModule<?>> moduleByName = new HashMap<>();
			for (BasicRuntimeModule<?> module: activeModules) {
				moduleByName.put(module.getClass().getName().replace('$', '_').replace('.', '_'), module);
			}
			Enumeration theEnum  = request.getParameterNames ();
			List<BasicRuntimeModule<?>> theList  = new ArrayList<>();
			
			while (theEnum.hasMoreElements ()) {
				String moduleName = theEnum.nextElement ().toString ();
				
				if (moduleName.startsWith ("chk_")) {
					moduleName = moduleName.substring (4);
					
					theList.add(moduleByName.get(moduleName));
				}
			}
			Set<BasicRuntimeModule<?>> modulesToRestart = ModuleUtil.INSTANCE.getIndependant(theList);
			for (BasicRuntimeModule<?> moduleToRestart: modulesToRestart) {
				ModuleUtil.INSTANCE.restart(moduleToRestart, null);
			}
			%>
			<h4>
				Finished
			</h4>
			<button class="tlButton cButton cmdButton" onclick="self.location.href = '<%=component.getComponentURL(displayContext).getURL()%>';">
				<h4 class="tlButtonLabel">Reload</h4>
			</button>
			<%
			
		}  else {
			%>
			<h4>
				Active Modules
			</h4>
			<form method="POST">
				<table class="tl-standard-table">
					<tr>
						<th>
						</th>
						<th>
							Module
						</th>
						<th>
							Package
						</th>
					</tr>
					<%
					for (BasicRuntimeModule<?> module : activeModules) {
						TagWriter tooltip = new TagWriter();
						boolean hasToolTip = false;
						
						tooltip.writeText("Restarting also restarts the following dependent modules:");
						tooltip.beginTag(HTMLConstants.UL);
						for (BasicRuntimeModule<?> potentialDependentModule : activeModules) {
							if (potentialDependentModule == module) {
								continue;
							}
							if (potentialDependentModule.getDependencies().contains(module.getClass())) {
								tooltip.beginTag(HTMLConstants.LI);
								tooltip.writeText(shortName(potentialDependentModule));
								tooltip.endTag(HTMLConstants.LI);
								
								hasToolTip = true;
							}
						}
						tooltip.endTag(HTMLConstants.UL);
						
						%>
						<tr>
							<td style="display: flex;align-items: center;justify-content: center;">
								<input name="chk_<%=module.getClass().getName().replace('$', '_').replace('.', '_') %>"
									type="checkbox"
								/>
							</td>
							<%
							TagWriter w = MainLayout.getTagWriter(pageContext);
							w.beginBeginTag(HTMLConstants.TD);
							w.writeAttribute(HTMLConstants.ID_ATTR, module.toString());
							if (hasToolTip) {
								OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(displayContext, w, tooltip.toString());
							}
							w.endBeginTag();
							w.writeText(shortName(module));
							w.endTag(HTMLConstants.TD);
							w.flushBuffer();
							%>
							<td>
								<%= pkg(module) %>
								<% inspect(displayContext, w, component, module); w.flushBuffer(); %>
							</td>
						</tr>
						<%
					}
					%>
				</table>
				<div class="cmdButtons">
					<p>
						<button class="tlButton cButton cmdButton" name="RELOAD" type="submit">
							<h4 class="tlButtonLabel">Reload selected modules and their dependents</h4>
						</button>
					</p>
				</div>
			</form>
			<%
		}
		%>
	</layout:body>
</layout:html>