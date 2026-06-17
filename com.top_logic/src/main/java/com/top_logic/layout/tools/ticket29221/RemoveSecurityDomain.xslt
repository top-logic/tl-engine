<?xml version="1.0" encoding="utf-8" ?>

<!--
	Removes the no longer supported "securityDomain" configuration (ticket #29221).

	The property was defined by
	com.top_logic.tool.boundsec.compound.CompoundSecurityLayout.Config (used on
	"securityLayout" components) and by
	com.top_logic.tool.boundsec.InAppSecurityLayoutConfig (mixed into "in app"
	component templates). It is dropped in all of the forms in which it can occur
	in a layout file:

	* as an attribute on any component, e.g. <securityLayout ... securityDomain="..."/>,
	* as a template parameter declaration, e.g. <param name="securityDomain" .../>,
	* as a property reference within a template display order, e.g. <property name="securityDomain"/>.
-->
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>

	<!-- Drop the "securityDomain" attribute wherever it is configured. -->
	<xsl:template match="@securityDomain">
	</xsl:template>

	<!-- Drop the "securityDomain" template parameter declaration. -->
	<xsl:template match="param[@name='securityDomain' and parent::params]">
	</xsl:template>

	<!-- Drop references to the "securityDomain" property from template display orders. -->
	<xsl:template match="property[@name='securityDomain' and ancestor::display-order]">
	</xsl:template>

	<!-- Identity transformation: copy everything else unchanged. -->
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
