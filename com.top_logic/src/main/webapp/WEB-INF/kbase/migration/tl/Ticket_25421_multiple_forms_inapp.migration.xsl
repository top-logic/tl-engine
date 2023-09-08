<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="//arguments/formDefinition">
		<xsl:element name="forms">
			<xsl:element name="form">
				<xsl:attribute name="type">
					<xsl:value-of select="../@type"/>
				</xsl:attribute>
				<xsl:copy>
					<xsl:apply-templates select="node()|@*"/>
				</xsl:copy>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<xsl:template match="@type[parent::arguments[formDefinition]]">
		<!-- Drop. -->
	</xsl:template>

	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>