<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="/config//metaobject[@object_name='MetaAttribute']//mo_attribute[@att_name='navigate']">
		<xsl:copy-of select="."/>

		<mo_attribute
			att_name="historyType"
			att_type="String"
			mandatory="false"
		>
			<storage class="com.top_logic.dob.attr.storage.EnumAttributeStorage"
				enum="com.top_logic.dob.meta.MOReference$HistoryType"
			/>
		</mo_attribute>
	</xsl:template>

	<xsl:template match="/config//association[@object_name='hasWrapperAttValue']">
		<xsl:copy-of select="."/>

		<metaobject
			object_name="hasHistoricValue"
			super_class="Item"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.KnowledgeItemFactoryAnnotation">
					<implementation-factory class="com.top_logic.knowledge.service.db2.KnowledgeAssociationFactory"/>
				</annotation>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.FullLoadAnnotation"
					full-load="true"
				/>
			</annotations>
			<attributes>
				<source target-type="Object"/>
				<destination
					history-type="historic"
					target-type="Object"
				/>
				<reference
					att_name="metaAttribute"
					branch-global="false"
					by-value="true"
					deletion-policy="delete-referer"
					history-type="current"
					is-container="false"
					mandatory="true"
					monomorphic="true"
					target-type="MetaAttribute"
				/>
				<mo_attribute
					att_name="sortOrder"
					att_type="Integer"
					mandatory="false"
				/>
			</attributes>
			<index>
				<mo_index name="source"
					unique="false"
				>
					<index-parts>
						<index_part name="source"
							part="name"
						/>
						<index_part name="metaAttribute"/>
					</index-parts>
				</mo_index>

				<mo_index name="dest"
					unique="false"
				>
					<index-parts>
						<index_part name="dest"
							part="name"
						/>
						<index_part name="metaAttribute"/>
					</index-parts>
				</mo_index>
			</index>
		</metaobject>

		<metaobject
			object_name="hasMixedValue"
			super_class="Item"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.KnowledgeItemFactoryAnnotation">
					<implementation-factory class="com.top_logic.knowledge.service.db2.KnowledgeAssociationFactory"/>
				</annotation>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.FullLoadAnnotation"
					full-load="true"
				/>
			</annotations>
			<attributes>
				<source target-type="Object"/>
				<destination
					history-type="mixed"
					target-type="Object"
				/>
				<reference
					att_name="metaAttribute"
					branch-global="false"
					by-value="true"
					deletion-policy="delete-referer"
					history-type="current"
					is-container="false"
					mandatory="true"
					monomorphic="true"
					target-type="MetaAttribute"
				/>
				<mo_attribute
					att_name="sortOrder"
					att_type="Integer"
					mandatory="false"
				/>
			</attributes>
			<index>
				<mo_index name="source"
					unique="false"
				>
					<index-parts>
						<index_part name="source"
							part="name"
						/>
						<index_part name="metaAttribute"/>
					</index-parts>
				</mo_index>

				<mo_index name="dest"
					unique="false"
				>
					<index-parts>
						<index_part name="dest"
							part="name"
						/>
						<index_part name="metaAttribute"/>
					</index-parts>
				</mo_index>
			</index>
		</metaobject>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>