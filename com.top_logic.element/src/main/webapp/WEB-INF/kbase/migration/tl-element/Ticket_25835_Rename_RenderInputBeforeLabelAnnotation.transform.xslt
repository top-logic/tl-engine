<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="//input-before-label[@value='true']|//input-before-label[not(@value)]">
		<label-position value="after-value"/>
	</xsl:template>

	<xsl:template match="//node()[@class='com.top_logic.model.annotate.RenderInputBeforeLabelAnnotation'][@value='true']|//node()[@class='com.top_logic.model.annotate.RenderInputBeforeLabelAnnotation'][not(@value)]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:attribute name="class">
				<xsl:value-of select="'com.top_logic.model.annotate.LabelPositionAnnotation'"/>
			</xsl:attribute>
			<xsl:attribute name="value">
				<xsl:value-of select="'after-value'"/>
			</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//input-before-label[@value='false']">
		<label-position value="default"/>
	</xsl:template>

	<xsl:template match="//node()[@class='com.top_logic.model.annotate.RenderInputBeforeLabelAnnotation'][@value='false']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:attribute name="class">
				<xsl:value-of select="'com.top_logic.model.annotate.LabelPositionAnnotation'"/>
			</xsl:attribute>
			<xsl:attribute name="value">
				<xsl:value-of select="'default'"/>
			</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>