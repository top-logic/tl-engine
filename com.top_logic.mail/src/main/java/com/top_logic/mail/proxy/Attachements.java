/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.tools.NameBuilder;

/**
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class Attachements {

	/** Held list of attachments. */
	public transient List<Attachement> attachements = new ArrayList<>();

    /**
	 * Create a representation of attachments.
	 */
    /*package protected*/ Attachements() {
    }

    @Override
	public String toString() {
		return new NameBuilder(this)
			.add("attachements", this.attachements.size())
			.build();
    }

    /** 
     * Return the number of attachments.
     * 
     * @return    The requested number of attachments.
     */
    public int getCount() {
        return this.attachements.size(); 
    }

    /** 
     * Return the requested attachment.
     * 
     * @param     aPos    Number of attachment.
     * @return    The requested attachment.
     */
    public Attachement getAttachement(int aPos) {
        return this.attachements.get(aPos);
    }

    /*package protected*/ void addAttachement(BodyPart aPart) throws MessagingException, UnsupportedEncodingException  {
        this.attachements.add(new Attachement(aPart, this.getCount()));
    }

    /**
     * One attachment contained in a mail.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public class Attachement {

        // Attributes

        private transient BodyPart part;

        private transient String name;

        private transient String mimeType;

        private int id;

		private Integer _size;

        // Constructors

        /** 
         * Create a new attachment.
         * 
         * @param    aPart    The part representing the attachment, must not be <code>null</code>.
         * @param    anID     Unique ID of the new attachment.
         * @throws   MessagingException          If reading the content fails for a reason.
         * @throws   UnsupportedEncodingException If the charset conversion failed.
         * @throws   IllegalArgumentException    If given part is <code>null</code>.
         */
        public Attachement(BodyPart aPart, int anID) throws MessagingException, UnsupportedEncodingException, IllegalArgumentException {
            if (aPart == null) {
                throw new IllegalArgumentException("Given part is null");
            }
            String filename = aPart.getFileName();

            if (filename != null) {
            	filename = MimeUtility.decodeText(filename);
            }

            this.id       = anID;
            this.part     = aPart;
            this.name     = (filename != null) ? filename : "???";
			this.mimeType = aPart.getContentType().toLowerCase();

            int thePos = this.mimeType.indexOf(';');

            if (thePos > 0) {
                this.mimeType = this.mimeType.substring(0, thePos);
            }
        }

		// Overridden methods from Object

        @Override
		public String toString() {
			return new NameBuilder(this)
				.add("id", this.id)
				.add("name", this.name)
				.add("mime", this.mimeType)
				.build();
        }

        // Public methods

		/**
		 * The name.
		 */
		public String getName() {
			return this.name;
        }

		/**
		 * The mime type.
		 */
		public String getMimeType() {
			return this.mimeType;
        }

		/**
		 * The content as stream.
		 */
        public InputStream getContent() throws IOException, MessagingException {
			return this.part.getInputStream();
        }

		/**
		 * The unique ID.
		 */
        public int getID() {
            return this.id;
        }

		public int getSize() throws MessagingException {
			if (_size == null) {
				int size = 0;

				try {
					InputStream inputStream = getContent();
					try {
						size = (int) StreamUtilities.size(inputStream);
					} finally {
						inputStream.close();

					}
				} catch (IOException ex) {
					Logger.error("Problem to initialize size of Attachment", ex, Attachement.class);
				}

				_size = Integer.valueOf(size);

			}
			return _size.intValue();
        }
    }
}
