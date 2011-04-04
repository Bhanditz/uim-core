/* ObjectDatabaseLogEntry.java - created on Apr 4, 2011, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.uim.logging.database;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.SerializationUtils;

/**
 * Implementation of log entries for messages of generic objects.
 * 
 * @author Markus Muhr (markus.muhr@kb.nl)
 * @since Apr 4, 2011
 */
@Entity
@Table(name = "uim_objectlogentry")
@DiscriminatorValue("object")
public class TObjectDatabaseLogEntry extends TDatabaseLogEntry<Serializable> {
    @Lob
    private byte[]       msg;

    @Transient
    private Serializable message;

    @Override
    public Serializable getMessage() {
        if (message == null && msg != null) {
            message = (Serializable)SerializationUtils.deserialize(msg);
        }
        return message;
    }

    /**
     * @param message
     *            generic message
     */
    public void setMessage(Serializable message) {
        msg = SerializationUtils.serialize(message);
        this.message = message;
    }
}
