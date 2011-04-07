/*
 * ETDMSDisseminationCrosswalk.java
 *
 * @todo: add header & copyright notice
 *
 */
package ca.unb.lib.riverrun.content.crosswalk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.crosswalk.CrosswalkException;
import org.dspace.content.crosswalk.CrosswalkInternalException;
import org.dspace.content.crosswalk.CrosswalkObjectNotSupported;
import org.dspace.content.crosswalk.DisseminationCrosswalk;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.SelfNamedPlugin;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * @todo: clean up documentation
 *
 * Configurable QDC Crosswalk
 * <p>
 * This class supports multiple dissemination crosswalks from DSpace
 * internal data to the Qualified Dublin Core XML format
 *  (see <a href="http://dublincore.org/">http://dublincore.org/</a>
 * <p>
 * It registers multiple Plugin names, which it reads from
 * the DSpace configuration as follows:
 *
 * <h3>Configuration</h3>
 * Every key starting with <code>"crosswalk.qdc.properties."</code> describes a
 * QDC crosswalk.  Everything after the last period is the <em>plugin instance</em>,
 * and the value is the pathname (relative to <code><em>dspace.dir</em>/config</code>)
 * of the crosswalk configuration file.
 * <p>
 * You can have two aliases point to the same crosswalk,
 * just add two configuration entries with the same value, e.g.
 * <pre>
 *    crosswalk.qdc.properties.QDC = xwalk/qdc.properties
 *    crosswalk.qdc.properties.default = xwalk/qdc.properties
 * </pre>
 * The first line creates a plugin with the name <code>"QDC"</code>
 * which is configured from the file <em>dspace-dir</em><code>/xwalk/qdc.properties</code>.
 * <p>
 * Since there is significant overhead in reading the properties file to
 * configure the crosswalk, and a crosswalk instance may be used any number
 * of times, we recommend caching one instance of the crosswalk for each
 * alias and simply reusing those instances. The PluginManager does
 * this by default.
 * <p>
 * Each named crosswalk has two other types of configuration lines:
 * <p>
 * XML Namespaces: all XML namespace prefixes used in the XML fragments below
 * <em>must</em> be defined in the configuration as follows.  Add a line of
 * the form: <pre>
 *  crosswalk.qdc.namespace.{NAME}.{prefix} = {namespace-URI}</pre>
 * e.g. for the namespaces <code>dc</code> and <code>dcterms</code>
 * in the plugin named <code>QDC</code>, add these lines:
 * <pre>crosswalk.qdc.namespace.QDC.dc = http://purl.org/dc/elements/1.1/
 * crosswalk.qdc.namespace.QDC.dcterms = http://purl.org/dc/terms/</pre>
 *
 * <p>
 * Finally, you need to declare an XML Schema URI for the plugin, with
 * a line of the form <pre>
 *  crosswalk.qdc.schema.{NAME} = {schema-URI}</pre>
 * for example,
 * <pre>crosswalk.qdc.schemaLocation.QDC  = \
 *  http://purl.org/dc/terms/ \
 *  http://dublincore.org/schemas/xmls/qdc/2003/04/02/qualifieddc.xsd</pre>
 *
 * @author Larry Stone
 * @version $Revision: 3761 $
 */
public class ETDMSDisseminationCrosswalk extends SelfNamedPlugin
        implements DisseminationCrosswalk {

    /** log4j category */
    private static Logger log = Logger.getLogger(ETDMSDisseminationCrosswalk.class);

    // map of qdc to etdms
    private Map<String, Set<String>> qdc2etdms = new HashMap<String, Set<String>>();

    // List of elments nested under 'degree'
    private static final List<String> degreeChildren = Arrays.asList(new String[] {"name", "level", "discipline", "grantor"});

    // the XML namespaces from config file for this name.
    private Namespace namespaces[] = null;

    // ETDMS Namespace
    private static final Namespace ETDMS_NS =
            Namespace.getNamespace("etdms", "http://www.ndltd.org/standards/metadata/etdms/1.0/");

    // sentinal: done init?
    private boolean inited = false;

    // my plugin name
    private String myName = null;

    // prefix of all DSpace Configuration entries.
    private static final String CONFIG_PREFIX = "crosswalk.etdms";

    // prefix of Theses Canada identifier
    // @todo remove this.  Crosswalks shouldn't generate metadata
    private static final String TC_ID_PREFIX = "TC-UNB-";

    // XML schemaLocation fragment for this crosswalk, from config.
    private String schemaLocation = null;

    private static final Namespace XLINK_NS =
            Namespace.getNamespace("xlink", "http://www.w3.org/TR/xlink");


    /**
     * Fill in the plugin-name table from DSpace configuration entries
     * for configuration files for flavors of ETDMS crosswalk:
     */
    private static String aliases[] = null;

    static {
        List aliasList = new ArrayList();
        Enumeration pe = ConfigurationManager.propertyNames();
        String propname = CONFIG_PREFIX + ".properties.";

        while (pe.hasMoreElements()) {
            String key = (String) pe.nextElement();
            if (key.startsWith(propname)) {
                aliasList.add(key.substring(propname.length()));
            }
        }
        aliases = (String[]) aliasList.toArray(new String[aliasList.size()]);
    }

    public static String[] getPluginNames() {
        return aliases;
    }


    /**
     * Initialize Crosswalk table from a properties file
     * which itself is the value of the DSpace configuration property
     * "crosswalk.etdms.properties.X", where "X" is the alias name of this
     * instance. Each instance may be configured with a separate mapping table.
     *
     * The ETDMS crosswalk configuration properties follow the format:
     *
     *  {qdc-element} = {etdms-element[,etdms-element]*}
     *
     *  1. qualified DC field name is of the form (qualifier is optional)
     *       {MDschema}.{element}.{qualifier}
     *
     *      e.g.  dc.contributor.author
     *            dc.title
     *            dc.subject.*
     *
     *  2. etdms-element identifies one or more field names into which the value
     *     of the corresponding qdc-element is mapped.
     *
     * Example properties lines:
     *
     *  dc.title = title
     *  dc.contributor.author = creator
     *  dc.subject.* = subject
     *  dc.publisher = publisher, grantor
     * 
     */
    private void init()
            throws CrosswalkException, IOException {
        if (inited) {
            return;
        }
        inited = true;

        myName = getPluginInstanceName();
        if (myName == null) {
            throw new CrosswalkInternalException("Cannot determine plugin name, "
                    + "You must use PluginManager to instantiate ETDMSDisseminationCrosswalk so the instance knows its name.");
        }

        // grovel DSpace configuration for namespaces
        List nsList = new ArrayList();
        Enumeration pe = ConfigurationManager.propertyNames();
        String propname = CONFIG_PREFIX + ".namespace." + myName + ".";

        while (pe.hasMoreElements()) {
            String key = (String) pe.nextElement();
            if (key.startsWith(propname)) {
                nsList.add(Namespace.getNamespace(key.substring(propname.length()),
                        ConfigurationManager.getProperty(key)));
            }
        }
        nsList.add(Namespace.XML_NAMESPACE);
        namespaces = (Namespace[]) nsList.toArray(new Namespace[nsList.size()]);

        // get XML schemaLocation fragment from config
        schemaLocation = ConfigurationManager.getProperty(CONFIG_PREFIX + ".schemaLocation." + myName);

        // read properties
        String cmPropName = CONFIG_PREFIX + ".properties." + myName;
        String propsFilename = ConfigurationManager.getProperty(cmPropName);
        if (propsFilename == null) {
            throw new CrosswalkInternalException("Configuration error: "
                    + "No properties file configured for ETDMS crosswalk named \"" + myName + "\"");
        }

        String parent = ConfigurationManager.getProperty("dspace.dir")
                + File.separator + "config" + File.separator;
        File propsFile = new File(parent, propsFilename);
        Properties etdmsProps = new Properties();
        FileInputStream pfs = null;
        try {
            pfs = new FileInputStream(propsFile);
            etdmsProps.load(pfs);
        } finally {
            if (pfs != null) {
                try {
                    pfs.close();
                } catch (IOException ioe) {
                }
            }
        }

        // grovel properties to initialize qdc->etdms map
        pe = etdmsProps.propertyNames();

        while (pe.hasMoreElements()) {
            String qdc = (String) pe.nextElement();
            List<String> etdmsList = Arrays.asList(etdmsProps.getProperty(qdc, "").split(","));

            Set<String> tmp = qdc2etdms.get(qdc);
            if (tmp == null)
                tmp = new HashSet<String>();

            Iterator i = etdmsList.iterator();
            while (i.hasNext()) {
                String etdmsString = (String) i.next();
                tmp.add(etdmsString);
            }

            qdc2etdms.put(qdc, tmp);
        }
    }

    public Namespace[] getNamespaces() {
        try {
            init();
        } catch (Exception e) {
        }
        return namespaces;
    }

    public String getSchemaLocation() {
        try {
            init();
        } catch (Exception e) {
        }
        return schemaLocation;
    }

    /**
     * Returns object's metadata in MODS format, as XML structure node.
     */
    public List disseminateList(DSpaceObject dso)
            throws CrosswalkException,
            IOException, SQLException, AuthorizeException {
        return disseminateListInternal(dso, false);
    }

    private List disseminateListInternal(DSpaceObject dso, boolean addSchema)
            throws CrosswalkException,
            IOException, SQLException, AuthorizeException {
        if (dso.getType() != Constants.ITEM) {
            throw new CrosswalkObjectNotSupported("ETDMSCrosswalk can only crosswalk an Item.");
        }
        Item item = (Item) dso;
        init();

        DCValue[] dc = item.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY);

        // Most elements added directly to list
        List result = new ArrayList(dc.length);

        // ... but some added as children of degree element
        Element degreeElement = new Element("degree");
        

        for (int i = 0; i < dc.length; i++) {
            // Compose qualified DC name - schema.element[.qualifier]
            // e.g. "dc.title", "dc.subject.lcc", "lom.Classification.Keyword"

            String qdc = dc[i].schema + "."
                    + ((dc[i].qualifier == null) ? dc[i].element
                    : (dc[i].element + "." + dc[i].qualifier));

            Set<String> etdmsSet = qdc2etdms.get(qdc);

            if (etdmsSet != null) {
                Iterator iterator = etdmsSet.iterator();
                while (iterator.hasNext()) {
                    String etdmsString = (String) iterator.next();
                    Element etdmsElement = new Element(etdmsString.trim());

                    etdmsElement.addContent(dc[i].value);
                    if (addSchema) {
                        etdmsElement.setAttribute("schemaLocation", schemaLocation, XSI_NS);
                    }

                    if (degreeChildren.contains(etdmsElement.getName()))
                        degreeElement.addContent(etdmsElement);
                    else
                        result.add(etdmsElement);
                }

            }

        }

        // Add degree element, empty or not
        result.add(degreeElement);


        // Theses Canada identifier
        // @todo remove TC ID generation; crosswalks shouldn't create metadata
        String handle = item.getHandle();
        if (handle != null) {
            Element tcElement = new Element("identifier");
            tcElement.addContent(TC_ID_PREFIX + handle.substring(handle.lastIndexOf("/")+1));
            if (addSchema)
                tcElement.setAttribute("schemaLocation", schemaLocation, XSI_NS);
            result.add(tcElement);
        }

        // Return the list
        return result;
    }

    public Element disseminateElement(DSpaceObject dso)
            throws CrosswalkException,
            IOException, SQLException, AuthorizeException {

        init();
        Element root = new Element("thesis", ETDMS_NS);
        if (schemaLocation != null)
            root.setAttribute("schemaLocation", schemaLocation, XSI_NS);

        root.addContent(disseminateListInternal(dso, false));
        return root;
    }

    public boolean canDisseminate(DSpaceObject dso) {
        return true;
    }

    public boolean preferList() {
        return false;
    }
}
