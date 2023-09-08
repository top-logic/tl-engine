<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<xsl:template match="/config/module[@name='tl.model']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
		
		<module name="tl.mail">
			<interface name="FolderContent"/>
			<class name="MailFolder">
				<generalizations>
					<generalization type="FolderContent"/>
				</generalizations>
				<annotations>
					<implementation-binding
						class-name="com.top_logic.mail.base.imap.IMAPMailFolder"
						interface-name="com.top_logic.mail.base.MailFolder"
					/>
					<instance-presentation icon="theme:mime.tl.mail.MailFolder"/>
					<table name="MailFolder"/>
				</annotations>
				<attributes>
					<property name="name"
						mandatory="true"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="description"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<reference name="content"
						multiple="true"
						type="tl.mail:FolderContent"
					>
						<annotations>
							<storage-algorithm>
								<set-storage
									monomorphic-table="true"
									table="hasMailFolderContent"
								/>
							</storage-algorithm>
							<delete-protected/>
						</annotations>
					</reference>
				</attributes>
			</class>
			<class name="Mail">
				<generalizations>
					<generalization type="FolderContent"/>
				</generalizations>
				<annotations>
					<implementation-binding
						class-name="com.top_logic.mail.base.imap.IMAPMail"
						interface-name="com.top_logic.mail.base.Mail"
					/>
					<instance-presentation icon="theme:mime.tl.mail.Mail"/>
					<main-properties properties="_self, mailID, attachements"/>
					<table name="Mail"/>
				</annotations>
				<attributes>
					<property name="name"
						mandatory="true"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="mailID"
						mandatory="true"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="sentDate"
						type="tl.core:Date"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="attachements"
						mandatory="true"
						type="tl.core:Boolean"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="from"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<reference name="attachedDocuments"
						multiple="true"
						type="tl.folder:Document"
					>
						<annotations>
							<storage-algorithm>
								<set-storage
									monomorphic-table="true"
									table="hasAttachedDocuments"
								/>
							</storage-algorithm>
							<delete-protected/>
						</annotations>
					</reference>
				</attributes>
			</class>
		</module>
	</xsl:template>
	
	<xsl:template match="@type">
		<xsl:choose>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:MailFolderTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.mail:MailFolder'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:MailTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.mail:Mail'"/></xsl:attribute></xsl:when>

			<xsl:otherwise>
				<xsl:copy>
					<xsl:apply-templates select="@*"/>
					<xsl:apply-templates/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>