package eu.europeana.uim.api;

/**
 * Abstract implementation of {@link IngestionPlugin}. A name and a description should be provided
 * so that human readable details are available. As identifier the simple class name is used.
 * 
 * @author Markus Muhr (markus.muhr@kb.nl)
 * @since Mar 4, 2011
 */
public abstract class AbstractIngestionPlugin implements IngestionPlugin {
    /**
     * meaningful name of this plugin
     */
    private final String name;
    /**
     * meaningful description of this plugin
     */
    private final String description;

    /**
     * Creates a new instance of this class and initializes members.
     * 
     * @param name
     *            meaningful name of this plugin
     * @param description
     *            meaningful description of this plugin
     */
    public AbstractIngestionPlugin(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getIdentifier() {
        return getClass().getSimpleName();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getIdentifier() == null) ? 0 : getIdentifier().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AbstractIngestionPlugin other = (AbstractIngestionPlugin)obj;
        if (getIdentifier() == null) {
            if (other.getIdentifier() != null) return false;
        } else if (!getIdentifier().equals(other.getIdentifier())) return false;
        return true;
    }
}
