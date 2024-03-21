<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="//filterProvider/@format-ref">
	</xsl:template>
	
	<xsl:template match="//filterProvider[@format-ref]">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
			
			<format-ref format-id="{@format-ref}"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="//annotations/format/@format">
	</xsl:template>
	
	<xsl:template match="//property[@type='tl.core:Byte' or @type='tl.core:Short' or @type='tl.core:Integer' or @type='tl.core:Long' or @type='tl.core:Float' or @type='tl.core:Double']/annotations/format[@format]">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
			
			<decimal pattern="{@format}"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="//property[@type='tl.core:Date' or @type='tl.core:Day' or @type='tl.core:Time' or @type='tl.core:DateTime']/annotations/format[@format]">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
			
			<custom-date pattern="{@format}"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//annotations/format[@format]">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
			
			<xsl:choose>
				<xsl:when test="contains(@format, '#') or contains(@format, '0')">
					<decimal pattern="{@format}"/>
				</xsl:when>
				
				<xsl:otherwise>
					<custom-date pattern="{@format}"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="//annotations/format/@format-ref">
	</xsl:template>
	
	<xsl:template match="//annotations/format[@format-ref]">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
			
			<format-ref format-id="{@format-ref}"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//config[@service-class='com.top_logic.basic.format.configured.FormatterService']/instance/formats/number/@id"/>
	<xsl:template match="//config[@service-class='com.top_logic.basic.format.configured.FormatterService']/instance/formats/decimal/@id"/>
	<xsl:template match="//config[@service-class='com.top_logic.basic.format.configured.FormatterService']/instance/formats/date/@id"/>
	<xsl:template match="//config[@service-class='com.top_logic.basic.format.configured.FormatterService']/instance/formats/custom-date/@id"/>
	<xsl:template match="//config[@service-class='com.top_logic.basic.format.configured.FormatterService']/instance/formats/time/@id"/>
	<xsl:template match="//config[@service-class='com.top_logic.basic.format.configured.FormatterService']/instance/formats/date-time/@id"/>
	<xsl:template match="//config[@service-class='com.top_logic.basic.format.configured.FormatterService']/instance/formats/date/@id"/>
	<xsl:template match="//config[@service-class='com.top_logic.basic.format.configured.FormatterService']/instance/formats/time/@id"/>
	<xsl:template match="//config[@service-class='com.top_logic.basic.format.configured.FormatterService']/instance/formats/normalizing-format/@id"/>
	
	<xsl:template match="//config[@service-class='com.top_logic.basic.format.configured.FormatterService']/instance/formats/*[local-name()='number' or local-name()='decimal' or local-name()='date' or local-name()='custom-date' or local-name()='time' or local-name()='date-time' or local-name()='date' or local-name()='time' or local-name()='normalizing-format']">
		<format id="{@id}">
			<xsl:copy>
				<xsl:apply-templates select="@* | node()"/>
			</xsl:copy>
		</format>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
