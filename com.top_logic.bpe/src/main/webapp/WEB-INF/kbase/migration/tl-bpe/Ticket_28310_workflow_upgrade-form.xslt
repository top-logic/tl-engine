<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="/config">
		<config xmlns:config="http://www.top-logic.com/ns/config/6.0" config:interface="com.top_logic.bpe.bpml.display.ProcessFormDefinition">
			<specialized-form>
				<xsl:element name="form">
					<xsl:apply-templates select="@*"/>
					<xsl:apply-templates/>
				</xsl:element>
			</specialized-form>
		</config>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>