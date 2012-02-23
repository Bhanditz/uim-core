/* ValidateFieldsIngestionPlugin.java - created on Mar 15, 2011, Copyright (c) 2011 The European Library, all rights reserved */
package org.theeuropeanlibrary.uim.check.validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.theeuropeanlibrary.model.tel.ObjectModelRegistry;
import org.theeuropeanlibrary.model.tel.qualifier.Maturity;

import eu.europeana.uim.api.AbstractIngestionPlugin;
import eu.europeana.uim.api.ActiveExecution;
import eu.europeana.uim.api.CorruptedMetadataRecordException;
import eu.europeana.uim.api.ExecutionContext;
import eu.europeana.uim.api.IngestionPluginFailedException;
import eu.europeana.uim.common.TKey;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.MetaDataRecord;
import eu.europeana.uim.store.Request;
import eu.europeana.uim.store.UimDataSet;
import eu.europeana.uim.sugarcrm.SugarControlledVocabulary;
import eu.europeana.uim.sugarcrm.SugarService;

/**
 * Validate fields, does nothing to alter the metadata record.
 * 
 * @author Andreas Juffinger (andreas.juffinger@kb.nl)
 * @since Mar 15, 2011
 */
public class FieldCheckIngestionPlugin extends AbstractIngestionPlugin {
    /**
     * Set the Logging variable to use logging within this class
     */
    private static final Logger                                log           = Logger.getLogger(FieldCheckIngestionPlugin.class.getName());

    /**
     * where to write the statistics output
     */
    public static final String                                 KEY_FIELDSPEC = "fieldcheck.file";

    /** minimum sum of points */
    public static final String                                 KEY_THRESHOLD = "fieldcheck.threshold";

    /** minimum sum of points */
    public static final String                                 KEY_POPULATE  = "fieldcheck.populate";

    /**
     * typed key to retrieve the container holding all execution dependent variables
     */
    private static final TKey<FieldCheckIngestionPlugin, Data> DATA          = TKey.register(
                                                                                     FieldCheckIngestionPlugin.class,
                                                                                     "data",
                                                                                     Data.class);

    /**
     * external parameters that must be provided
     */
    private static final List<String>                          PARAMETERS    = new ArrayList<String>() {
                                                                                 {
                                                                                     add(KEY_FIELDSPEC);
                                                                                     add(KEY_THRESHOLD);
                                                                                     add(KEY_POPULATE);
                                                                                 }
                                                                             };

    private final static SimpleDateFormat                      df            = new SimpleDateFormat(
                                                                                     "yyyy-MM-dd HH:mm:ss");

    private static SugarService                                sugarService;

    /**
     * Creates a new instance of this class.
     */
    public FieldCheckIngestionPlugin() {
        super("Field Validation Plugin", "Validates the size and content of the specified fields.");
    }

    @Override
    public TKey<?, ?>[] getInputFields() {
        return new TKey[0];
    }

    @Override
    public TKey<?, ?>[] getOptionalFields() {
        return new TKey[0];
    }

    @Override
    public TKey<?, ?>[] getOutputFields() {
        return new TKey[0];
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public List<String> getParameters() {
        return PARAMETERS;
    }

    @Override
    public int getPreferredThreadCount() {
        return 8;
    }

    @Override
    public int getMaximumThreadCount() {
        return 10;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <I> void initialize(ExecutionContext<I> context) throws IngestionPluginFailedException {
        Data value = context.getValue(DATA);
        if (value == null) {
            value = new Data();
            context.putValue(DATA, value);
        }

        value.start = 0;
        value.fine = 0;

        Properties props = context.getProperties();
        String threshold = props.getProperty(KEY_THRESHOLD, "10");
        value.threshold = Integer.parseInt(threshold);

        String populate = props.getProperty(KEY_POPULATE, "true");
        value.populate = Boolean.parseBoolean(populate);

        String fieldSpecPath = props.getProperty(KEY_FIELDSPEC);
        if (fieldSpecPath != null && !fieldSpecPath.isEmpty()) {
            File fieldSpecFile = context.getFileResource(fieldSpecPath);
            if (fieldSpecFile == null || !fieldSpecFile.exists()) {
                fieldSpecFile = new File(fieldSpecPath);
            }
            if (!fieldSpecFile.exists()) { throw new IngestionPluginFailedException(
                    "Could not find field specification for validation: " + fieldSpecPath); }

            // now we have a proper file
            try {
                List<String> lines = IOUtils.readLines(new FileInputStream(fieldSpecFile));
                int linenum = 1;
                for (String line : lines) {
                    if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                        try {
                            String[] split = line.trim().split(";");
                            TKey<?, ?> tKey = TKey.fromString(split[0]);
                            if (tKey == null)
                                throw new NullPointerException("Not able to load tkey:" + split[0] +
                                                               "");
                            int points = Integer.parseInt(split[2]);

                            List<Enum<?>> qualifiers = new ArrayList<Enum<?>>();
                            String[] qsplit = split[1].split(",");
                            for (int i = 1; i < qsplit.length; i++) {
                                String clazz = qsplit[i].substring(0, qsplit[i].lastIndexOf("."));
                                String enumv = qsplit[i].substring(qsplit[i].lastIndexOf(".") + 1);

                                qualifiers.add(Enum.valueOf((Class<Enum>)Class.forName(clazz),
                                        enumv));
                            }

                            value.exists.put(
                                    new DataKey(tKey, points, qualifiers.toArray(new Enum<?>[0])),
                                    new SummaryStatistics());
                        } catch (Throwable e) {
                            throw new IngestionPluginFailedException(
                                    "Could not parse field specification in file <" +
                                            fieldSpecPath + ">: Line " + linenum + ": " + line);
                        }
                    }
                    linenum++;
                }
            } catch (Throwable t) {
                if (t instanceof IngestionPluginFailedException)
                    throw (IngestionPluginFailedException)t;

                throw new IngestionPluginFailedException(
                        "Could not parse field specification in file <" + fieldSpecPath + ">");
            }
        }

        Collection<?> collection = null;
        UimDataSet<?> dataset = context.getDataSet();
        if (dataset instanceof Collection) {
            collection = (Collection<?>)dataset;
        } else if (dataset instanceof Request<?>) {
            collection = ((Request<?>)dataset).getCollection();
        }

        String time = df.format(new Date());
        String mnem = collection != null ? collection.getMnemonic() : "NULL";
        String name = collection != null ? collection.getName() : "No collection";

        context.getLoggingEngine().log(context.getExecution(), Level.INFO, "fieldcheck",
                "initialize", mnem, name, time);

    }

    @Override
    public <I> boolean processRecord(MetaDataRecord<I> mdr, ExecutionContext<I> context)
            throws IngestionPluginFailedException, CorruptedMetadataRecordException {
        Data value = context.getValue(DATA);
        if (value.exists.isEmpty()) {
            // not configured... just pass by
            return true;
        }

        synchronized (value) {
            value.start++;
        }

        int sum = 0;
        for (Entry<DataKey, SummaryStatistics> entry : value.exists.entrySet()) {
            DataKey datakey = entry.getKey();
            TKey<?, ?> tKey = datakey.getTkey();

            Enum<?>[] qualifier = datakey.getQualifier();
            List<?> values = mdr.getValues(datakey.getTkey(), datakey.getQualifier());

            String tk = tKey.getFullName();
            String qu = Arrays.toString(qualifier);

            if (value.populate) {
                context.getLoggingEngine().logField(context.getExecution(), this, mdr, tk, qu,
                        values.size());
            }

            synchronized (entry.getValue()) {
                entry.getValue().addValue(values.size());
                if (!values.isEmpty()) {
                    sum += datakey.getPoints();
                }
            }
        }

        if (sum == 0) {
            mdr.addValue(ObjectModelRegistry.MATURITY, Maturity.REJECT);
        } else if (sum < value.threshold) {
            mdr.addValue(ObjectModelRegistry.MATURITY, Maturity.WEAK_REJECT);
        } else if (sum == value.threshold) {
            mdr.addValue(ObjectModelRegistry.MATURITY, Maturity.WEAK_ACCEPT);
        } else if (sum > value.threshold) {
            mdr.addValue(ObjectModelRegistry.MATURITY, Maturity.ACCEPT);
        }

        if (value.populate) {
            context.getLoggingEngine().logField(context.getExecution(), this, mdr,
                    ObjectModelRegistry.MATURITY.getFullName(), "", sum);
        }

        synchronized (value) {
            value.fine++;
        }
        return true;
    }

    @Override
    public <I> void completed(ExecutionContext<I> context) throws IngestionPluginFailedException {
        Data value = context.getValue(DATA);

        Collection<I> collection = null;
        UimDataSet<I> dataset = context.getDataSet();
        if (dataset instanceof Collection) {
            collection = (Collection<I>)dataset;
        } else if (dataset instanceof Request<?>) {
            collection = ((Request<I>)dataset).getCollection();
        }

        String time = df.format(new Date());
        String mnem = collection != null ? collection.getMnemonic() : "NULL";
        String name = collection != null ? collection.getName() : "No collection";
        context.getLoggingEngine().log(context.getExecution(), Level.INFO, "fieldcheck",
                "completed", mnem, name, "" + value.start, "" + value.fine, time);

        try {
            if (collection != null && value.populate) {
                collection.putValue(SugarControlledVocabulary.COLLECTION_FIELD_VALIDATION,
                        "" + context.getExecution().getId());

                ((ActiveExecution<I>)context).getStorageEngine().updateCollection(collection);

                if (getSugarService() != null) {
                    getSugarService().updateCollection(collection);
                }
            }
        } catch (Throwable t) {
            context.getLoggingEngine().logFailed(Level.INFO, this, t,
                    "Update collection or sugar service on " + collection + " failed");
            log.log(Level.WARNING, "Failed to update collection or call sugar service: " +
                                   collection, t);
        }

    }

    // Container holding all execution specific information for the validation plugin.
    static class Data implements Serializable {
        private int                             start     = 0;
        private int                             fine      = 0;
        private int                             threshold = 10;
        private boolean                         populate  = true;
        private Map<DataKey, SummaryStatistics> exists    = new HashMap<DataKey, SummaryStatistics>();
    }

    private static class DataKey {

        private final TKey<?, ?> tkey;
        private final Enum<?>[]  qualifier;
        private final int        points;

        public DataKey(TKey<?, ?> tkey, int points, Enum<?>... qualifier) {
            this.points = points;
            this.tkey = tkey;
            this.qualifier = qualifier;
        }

        public int getPoints() {
            return points;
        }

        public TKey<?, ?> getTkey() {
            return tkey;
        }

        public Enum<?>[] getQualifier() {
            return qualifier;
        }
    }

    /**
     * Sets the sugarService to the given value.
     * 
     * @param sugarService
     *            the sugarService to set
     */
    public void setSugarService(SugarService sugarService) {
        FieldCheckIngestionPlugin.sugarService = sugarService;
    }

    /**
     * Sets the sugarService to the given value.
     * 
     * @param sugarService
     *            the sugarService to set
     */
    public void unsetSugarService(SugarService sugarService) {
        FieldCheckIngestionPlugin.sugarService = null;
    }

    /**
     * @return the sugar service
     */
    public SugarService getSugarService() {
        return FieldCheckIngestionPlugin.sugarService;
    }

}
