<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>

	<xsl:template match="@downloadNameKey[parent::button[@class='com.top_logic.layout.table.export.ExcelExportHandler']]">
	</xsl:template>

	<xsl:template match="@downloadNameKey[parent::exporter]">
	</xsl:template>

	<xsl:template match="@exportNameKey[parent::button[@class='com.top_logic.layout.table.export.StreamingExcelExportHandler']]">
	</xsl:template>

	<xsl:template match="@dynamic-download-name[parent::button]">
	</xsl:template>

	<xsl:template match="dynamic-download-name[parent::button]">
	</xsl:template>

	<xsl:template match="@name-of-export-file[parent::configurationProvider[@class='com.top_logic.bpe.app.layout.tiles.ContextTableConfiguration']]">
	</xsl:template>

	<xsl:template match="button[@class='com.top_logic.layout.table.export.ExcelExportHandler' and @downloadNameKey and not(@dynamic-download-name) and not(dynamic-download-name)]">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
			
			<downloadNameProvider class="com.top_logic.layout.table.export.ConstantDownloadName"
				downloadName="{@downloadNameKey}"
			/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="exporter[@downloadNameKey]">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
			
			<downloadNameProvider class="com.top_logic.layout.table.export.ConstantDownloadName"
				downloadName="{@downloadNameKey}"
			/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="button[@class='com.top_logic.layout.table.export.StreamingExcelExportHandler' and @exportNameKey and not(@dynamic-download-name) and not(dynamic-download-name)]">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
			
			<downloadNameProvider class="com.top_logic.layout.table.export.ConstantDownloadName"
				downloadName="{@exportNameKey}"
			/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="button[@downloadNameKey and @dynamic-download-name]">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
			
			<downloadNameProvider class="{@dynamic-download-name}"
				downloadNameTemplate="{@downloadNameKey}"
			/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="button[@exportNameKey and @dynamic-download-name]">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
			
			<downloadNameProvider class="{@dynamic-download-name}"
				downloadNameTemplate="{@exportNameKey}"
			/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="dynamic-download-name[parent::button]">
		<xsl:element name="downloadName">
			<xsl:apply-templates select="@*"/>
			
			<xsl:if test="parent::button/@exportNameKey">
				<xsl:attribute name="downloadNameTemplate">
					<xsl:value-of select="parent::button/@exportNameKey"/>
				</xsl:attribute>
			</xsl:if>
			
			<xsl:if test="parent::button/@downloadNameKey">
				<xsl:attribute name="downloadNameTemplate">
					<xsl:value-of select="parent::button/@downloadNameKey"/>
				</xsl:attribute>
			</xsl:if>
			
			<xsl:apply-templates select="node()"/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="configurationProvider[@class='com.top_logic.bpe.app.layout.tiles.ContextTableConfiguration' and @name-of-export-file]">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
			
			<downloadNameProvider class="com.top_logic.layout.table.export.ConstantDownloadName"
				downloadName="{@name-of-export-file}"
			/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>