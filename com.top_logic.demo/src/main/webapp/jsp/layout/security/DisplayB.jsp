<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%> <%@taglib uri="layout"   prefix="layout"
%><layout:html>
	<layout:head/>
	
	<layout:body>
		<p>
			Dieser Tabber wird nur eingeblendet, wenn ein Element vom Typ B selektiert ist.
			Der Tabber "Details Typ A" ist jetzt ausgeblendet.
		</p>
		<p>
			Die Komponente akzeptiert nur ein Modell vom Typ B. Ist kein B-Knoten gewählt, wird das Modell auf null gesetzt und der Tabber blendet sich aus (BoundComponent#allowNullModel()).
			Dieses Verhalten gilt auch für den Super-User und ist unabhängig von der Berechtigung.
		</p>
	</layout:body>
</layout:html>