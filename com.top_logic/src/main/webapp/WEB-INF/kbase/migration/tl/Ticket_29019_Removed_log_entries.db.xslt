<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="/config/meta-objects/metaobject[@object_name='ArchiveUserDayEntry']/annotations">
		<!--
			Remove annotation, because it contains the removed class 'com.top_logic.event.logEntry.LogEntry',
			so the configuration can not be loaded without this processor.
		-->
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='LogEntry']/annotations">
		<!--
			Remove annotation, because it contains the removed class 'com.top_logic.event.logEntry.ArchiveUserDayEntry',
			so the configuration can not be loaded without this processor.
		-->
	</xsl:template>

	<!-- Standard copy template. -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>