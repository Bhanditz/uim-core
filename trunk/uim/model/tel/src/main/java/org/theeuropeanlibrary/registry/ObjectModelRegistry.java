package org.theeuropeanlibrary.registry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.theeuropeanlibrary.model.Edition;
import org.theeuropeanlibrary.model.Facet;
import org.theeuropeanlibrary.model.Identifier;
import org.theeuropeanlibrary.model.LabeledText;
import org.theeuropeanlibrary.model.Link;
import org.theeuropeanlibrary.model.Metadata;
import org.theeuropeanlibrary.model.Numbering;
import org.theeuropeanlibrary.model.Text;
import org.theeuropeanlibrary.model.Title;
import org.theeuropeanlibrary.model.party.Family;
import org.theeuropeanlibrary.model.party.Meeting;
import org.theeuropeanlibrary.model.party.Organization;
import org.theeuropeanlibrary.model.party.Party;
import org.theeuropeanlibrary.model.party.Person;
import org.theeuropeanlibrary.model.spatial.BoundingBoxReferencedPlace;
import org.theeuropeanlibrary.model.spatial.GeoReferencedPlace;
import org.theeuropeanlibrary.model.spatial.NamedPlace;
import org.theeuropeanlibrary.model.spatial.SpatialEntity;
import org.theeuropeanlibrary.model.subject.Topic;
import org.theeuropeanlibrary.model.time.HistoricalPeriod;
import org.theeuropeanlibrary.model.time.Instant;
import org.theeuropeanlibrary.model.time.Period;
import org.theeuropeanlibrary.model.time.TemporalTextualExpression;
import org.theeuropeanlibrary.qualifier.Audience;
import org.theeuropeanlibrary.qualifier.BibliographicLevel;
import org.theeuropeanlibrary.qualifier.ContextLevel;
import org.theeuropeanlibrary.qualifier.ContributionType;
import org.theeuropeanlibrary.qualifier.Country;
import org.theeuropeanlibrary.qualifier.FacetType;
import org.theeuropeanlibrary.qualifier.FieldSource;
import org.theeuropeanlibrary.qualifier.IdentifierType;
import org.theeuropeanlibrary.qualifier.KnowledgeOrganizationSystem;
import org.theeuropeanlibrary.qualifier.Language;
import org.theeuropeanlibrary.qualifier.LanguageRelation;
import org.theeuropeanlibrary.qualifier.LinkTarget;
import org.theeuropeanlibrary.qualifier.Maturity;
import org.theeuropeanlibrary.qualifier.NumberingRelation;
import org.theeuropeanlibrary.qualifier.PartyIdentifierType;
import org.theeuropeanlibrary.qualifier.PartyRelation;
import org.theeuropeanlibrary.qualifier.PrintType;
import org.theeuropeanlibrary.qualifier.ResourceRelation;
import org.theeuropeanlibrary.qualifier.ResourceType;
import org.theeuropeanlibrary.qualifier.SpatialIdentifierType;
import org.theeuropeanlibrary.qualifier.SpatialRelation;
import org.theeuropeanlibrary.qualifier.Status;
import org.theeuropeanlibrary.qualifier.TemporalRelation;
import org.theeuropeanlibrary.qualifier.TextRelation;
import org.theeuropeanlibrary.qualifier.TitleType;

import eu.europeana.uim.common.TKey;
import eu.europeana.uim.store.bean.MetaDataRecordBean;

/**
 * Registry holding all known keys for the detailed internal object model of metadata on a record.
 * 
 * @author Markus Muhr (markus.muhr@kb.nl)
 * @author Nuno Freire <nfreire@gmail.com>
 * @since Mar 18, 2011
 */
@SuppressWarnings("all")
public final class ObjectModelRegistry {
    public static final String                                                XML_NAMESPACE       = "http://theeuropeanlibrary.org/internal_object_model";

    public static final TKey<ObjectModelRegistry, MetaDataRecordBean>         METADATARECORD      = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "metadatarecord",
                                                                                                          MetaDataRecordBean.class);
    public static final TKey<ObjectModelRegistry, String>                     COLLECTION          = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "collection",
                                                                                                          String.class);

    public static final TKey<ObjectModelRegistry, Identifier>                 IDENTIFIER          = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "identifier",
                                                                                                          Identifier.class);

    public static final TKey<ObjectModelRegistry, Title>                      TITLE               = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "title",
                                                                                                          Title.class);

    public static final TKey<ObjectModelRegistry, Topic>                      TOPIC               = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "topic",
                                                                                                          Topic.class);

    public static final TKey<ObjectModelRegistry, Party>                      PARTY               = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "party",
                                                                                                          Party.class);
    public static final TKey<ObjectModelRegistry, Person>                     PERSON              = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "person",
                                                                                                          Person.class);
    public static final TKey<ObjectModelRegistry, Meeting>                    MEETING             = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "meeting",
                                                                                                          Meeting.class);
    public static final TKey<ObjectModelRegistry, Family>                     FAMILY              = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "family",
                                                                                                          Family.class);
    public static final TKey<ObjectModelRegistry, Organization>               ORGANIZATION        = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "organization",
                                                                                                          Organization.class);

    public static final TKey<ObjectModelRegistry, Link>                       LINK                = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "link",
                                                                                                          Link.class);

    public static final TKey<ObjectModelRegistry, Facet>                      FACET               = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "facet",
                                                                                                          Facet.class);

    public static final TKey<ObjectModelRegistry, Instant>                    INSTANT             = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "instant",
                                                                                                          Instant.class);
    public static final TKey<ObjectModelRegistry, Period>                     PERIOD              = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "period",
                                                                                                          Period.class);
    public static final TKey<ObjectModelRegistry, TemporalTextualExpression>  TIME_TEXTUAL        = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "time textual",
                                                                                                          TemporalTextualExpression.class);
    public static final TKey<ObjectModelRegistry, HistoricalPeriod>           HISTORICAL_PERIOD   = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "historical period",
                                                                                                          HistoricalPeriod.class);

    public static final TKey<ObjectModelRegistry, Numbering>                  NUMBERING           = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "numbering",
                                                                                                          Numbering.class);

    public static final TKey<ObjectModelRegistry, Edition>                    EDITION             = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "edition",
                                                                                                          Edition.class);

    public static final TKey<ObjectModelRegistry, Text>                       TEXT                = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "text",
                                                                                                          Text.class);
    public static final TKey<ObjectModelRegistry, LabeledText>                LABELED_TEXT        = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "labeled text",
                                                                                                          LabeledText.class);

    public static final TKey<ObjectModelRegistry, NamedPlace>                 PLACE               = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "place",
                                                                                                          NamedPlace.class);
    public static final TKey<ObjectModelRegistry, GeoReferencedPlace>         GEO_PLACE           = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "georeference place",
                                                                                                          GeoReferencedPlace.class);
    public static final TKey<ObjectModelRegistry, BoundingBoxReferencedPlace> GEO_BOX_PLACE       = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "bounding box reference place",
                                                                                                          BoundingBoxReferencedPlace.class);
    public static final TKey<ObjectModelRegistry, SpatialEntity>              GEOGRAPHIC_ENTITY   = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "geographic entity",
                                                                                                          SpatialEntity.class);

    public static final TKey<ObjectModelRegistry, Metadata>                   METADATA            = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "metadata",
                                                                                                          Metadata.class);

    // ----------------------------------------- Qualifiers

    public static final TKey<ObjectModelRegistry, Status>                     STATUS              = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "status",
                                                                                                          Status.class);

    public static final TKey<ObjectModelRegistry, Language>                   LANGUAGE            = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "language",
                                                                                                          Language.class);

    public static final TKey<ObjectModelRegistry, Country>                    COUNTRY             = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "country",
                                                                                                          Country.class);

    public static final TKey<ObjectModelRegistry, Maturity>                   MATURITY            = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "maturity",
                                                                                                          Maturity.class);

    public static final TKey<ObjectModelRegistry, ResourceType>               RESOURCE_TYPE       = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "resource_type",
                                                                                                          ResourceType.class);

    public static final TKey<ObjectModelRegistry, BibliographicLevel>         BIBLIOGRAPHIC_LEVEL = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "bibliographic_level",
                                                                                                          BibliographicLevel.class);

    public static final TKey<ObjectModelRegistry, PrintType>                  PRINT_TYPE          = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "print_type",
                                                                                                          PrintType.class);

    public static final TKey<ObjectModelRegistry, Audience>                   AUDIENCE            = TKey.register(
                                                                                                          ObjectModelRegistry.class,
                                                                                                          "audience",
                                                                                                          Audience.class);

    private static final Map<Class<?>, TKey<?, ?>>                            tKeyClassMap        = new HashMap<Class<?>, TKey<?, ?>>();

    private static final Map<TKey<?, ?>, ArrayList<Class<? extends Enum<?>>>> validQualifiers     = new HashMap<TKey<?, ?>, ArrayList<Class<? extends Enum<?>>>>();

    static {
        validQualifiers.put(INSTANT, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(TemporalRelation.class);
                add(FieldSource.class);
            }
        });
        validQualifiers.put(PERIOD, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(TemporalRelation.class);
                add(FieldSource.class);
            }
        });
        validQualifiers.put(TIME_TEXTUAL, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(TemporalRelation.class);
                add(Language.class);
                add(FieldSource.class);
            }
        });
        validQualifiers.put(HISTORICAL_PERIOD, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(TemporalRelation.class);
                add(Language.class);
                add(FieldSource.class);
            }
        });

        validQualifiers.put(TITLE, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(Language.class);
                add(TitleType.class);
// add(TopicIdentifierType.class);
                add(FieldSource.class);
            }
        });
        validQualifiers.put(TOPIC, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(Language.class);
                add(KnowledgeOrganizationSystem.class);
                add(FieldSource.class);
            }
        });

        validQualifiers.put(TEXT, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(Language.class);
                add(TextRelation.class);
                add(ResourceRelation.class);
                add(FieldSource.class);
            }
        });
        validQualifiers.put(LABELED_TEXT, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(Language.class);
                add(FieldSource.class);
            }
        });

        validQualifiers.put(EDITION, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(Language.class);
                add(FieldSource.class);
            }
        });

        validQualifiers.put(LANGUAGE, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(LanguageRelation.class);
                add(FieldSource.class);
            }
        });

        validQualifiers.put(IDENTIFIER, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(IdentifierType.class);
                add(FieldSource.class);
                add(ContextLevel.class);
                add(KnowledgeOrganizationSystem.class);
                add(SpatialIdentifierType.class);
                add(PartyIdentifierType.class);
            }
        });

        validQualifiers.put(LINK, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(LinkTarget.class);
                add(ResourceRelation.class);
                add(FieldSource.class);
            }
        });

        validQualifiers.put(NUMBERING, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(NumberingRelation.class);
                add(FieldSource.class);
            }
        });

        validQualifiers.put(FACET, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(FacetType.class);
                add(FieldSource.class);
            }
        });

        validQualifiers.put(PARTY, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(Language.class);
                add(PartyRelation.class);
                add(ContributionType.class);
                add(PartyIdentifierType.class);
                add(FieldSource.class);
            }
        });
        validQualifiers.put(PERSON, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(Language.class);
                add(PartyRelation.class);
                add(ContributionType.class);
                add(PartyIdentifierType.class);
                add(FieldSource.class);
            }
        });
        validQualifiers.put(MEETING, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(Language.class);
                add(PartyRelation.class);
                add(ContributionType.class);
                add(PartyIdentifierType.class);
                add(FieldSource.class);
            }
        });
        validQualifiers.put(FAMILY, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(Language.class);
                add(PartyRelation.class);
                add(ContributionType.class);
                add(PartyIdentifierType.class);
                add(FieldSource.class);
            }
        });
        validQualifiers.put(ORGANIZATION, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(Language.class);
                add(PartyRelation.class);
                add(ContributionType.class);
                add(PartyIdentifierType.class);
                add(FieldSource.class);
            }
        });

        validQualifiers.put(GEO_PLACE, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(Language.class);
                add(SpatialRelation.class);
                add(SpatialIdentifierType.class);
                add(FieldSource.class);
            }
        });
        validQualifiers.put(GEO_BOX_PLACE, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(Language.class);
                add(SpatialRelation.class);
                add(SpatialIdentifierType.class);
                add(FieldSource.class);
            }
        });
        validQualifiers.put(PLACE, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(Language.class);
                add(SpatialRelation.class);
                add(SpatialIdentifierType.class);
                add(FieldSource.class);
            }
        });
        validQualifiers.put(GEOGRAPHIC_ENTITY, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(SpatialRelation.class);
                add(SpatialIdentifierType.class);
                add(FieldSource.class);
            }
        });
        validQualifiers.put(METADATA, new ArrayList<Class<? extends Enum<?>>>() {
            {
                add(FieldSource.class);
            }
        });

// tKeyClassMap;

        for (Field field : ObjectModelRegistry.class.getDeclaredFields()) {
            try {
                Object object = field.get(null);
                if (object instanceof TKey) {
                    TKey k = (TKey)object;
                    if (tKeyClassMap.containsKey(k.getType()))
                        throw new RuntimeException(
                                "Two TKeys for the same class in the Object Model: " +
                                        k.getType().getName());
                    tKeyClassMap.put(k.getType(), k);
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Returns valid enums for the given key or null. Note null is supposed to happen only for
     * dynamic keys and not for the ones defined in this map, as dynamic keys do not enforce only
     * specific qualifiers.
     * 
     * @param key
     *            typed key for which known qualifiers should be retrieved
     * @return valid enums for the given key or null
     */
    public static List<Class<? extends Enum<?>>> getValidEnums(TKey<?, ?> key) {
        ArrayList<Class<? extends Enum<?>>> ret = validQualifiers.get(key);
        if (ret == null) return new ArrayList<Class<? extends Enum<?>>>(0);
        return ret;
    }

    /**
     * Returns valid {@link TKey} for the given class or null.
     * 
     * @param <T>
     *            value type of tkey and class
     * @param cls
     *            for which class a {@link TKey} should be retrieved
     * @return {@link TKey} or null
     */
    public static <T> TKey<ObjectModelRegistry, T> lookup(Class<T> cls) {
        return (TKey<ObjectModelRegistry, T>)tKeyClassMap.get(cls);
    }
}
