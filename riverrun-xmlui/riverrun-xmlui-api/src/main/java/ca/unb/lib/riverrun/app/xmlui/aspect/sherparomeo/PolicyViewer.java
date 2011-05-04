/*
 * PolicyViewer.java
 */
package ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.util.HashUtil;
import org.apache.excalibur.source.SourceValidity;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.DSpaceValidity;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;

import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * @todo: write me
 */
public class PolicyViewer extends AbstractDSpaceTransformer implements CacheableProcessingComponent {

    private static final Logger log = Logger.getLogger(PolicyViewer.class);

    /** Language strings */
    private static final Message T_DSPACE_HOME =
                                 message("xmlui.general.dspace_home");

    private static final Message T_ITEM_TRAIL =
                                 message("xmlui.ArtifactBrowser.ItemViewer.trail");

    private static final Message T_POLICY_TRAIL =
                                 message("xmlui.SherpaRomeo.PolicyViewer.trail");

    private static final Message T_TITLE =
                                 message("xmlui.SherpaRomeo.PolicyViewer.title");

    private static final Message T_NO_INFORMATION =
                                 message("xmlui.SherpaRomeo.PolicyViewer.no_information");

    private static final Message T_CONTACT_US =
                                 message("xmlui.SherpaRomeo.PolicyViewer.contact_us");

    /** Cached validity object */
    private SourceValidity validity = null;

    /** DSpace metadata element that stores ISSN */
    private static String issnElement = null;

    static {
        issnElement = ConfigurationManager.getProperty("sherpa.romeo.issn");
    }

    /**
     * Generate the unique caching key.
     * This key must be unique inside the space of this component.
     */
    public Serializable getKey() {
        try {
            DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

            if (dso == null) {
                return "0"; // no item, something is wrong.
            }
            return HashUtil.hash(dso.getHandle() + "show-policies");
        }
        catch (SQLException sqle) {
            // Ignore all errors and just return that the component is not cachable.
            return "0";
        }
    }

    /**
     * Generate the cache validity object.
     * 
     * The validity object will include the item being viewed, 
     * along with all bundles & bitstreams.
     */
    public SourceValidity getValidity() {
        DSpaceObject dso = null;

        if (this.validity == null) {
            try {
                dso = HandleUtil.obtainHandle(objectModel);

                DSpaceValidity dsValidity = new DSpaceValidity();
                dsValidity.add(dso);
                this.validity = dsValidity.complete();
            }
            catch (Exception e) {
                // Ignore all errors and just invalidate the cache.
            }

        }
        return this.validity;
    }

    /**
     * Add the item's title and trail links to the page's metadata.
     */
    @Override
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException {

        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (!(dso instanceof Item)) {
            return;
        }
        Item item = (Item) dso;

        // set page title
        pageMeta.addMetadata("title").addContent(T_TITLE);

        // Modify trail links for Policy view
        pageMeta.addTrailLink(contextPath + "/", T_DSPACE_HOME);
        HandleUtil.buildHandleTrail(item, pageMeta, contextPath);

        // Add trail items for referring item & current view
        pageMeta.addTrailLink(contextPath + "/handle/" + item.getHandle(), T_ITEM_TRAIL);
        pageMeta.addTrail().addContent(T_POLICY_TRAIL);
    }

    @Override
    public void addBody(Body body) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException {

        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (!(dso instanceof Item)) {
            return;
        }

        Division division = body.addDivision("policy-viewer", "primary");
        division.setHead(T_TITLE);

        Item item = (Item) dso;
        String issn = getItemISSN(item);

        if (issn != null) {
            Query query = new Query(issn);
            Response response = query.getResponse();
            division.addPara(response.getOutcome().toString());
            division.addPara(response.getDisclaimer());
        }
    }

    /**
     * Obtain the item's ISSN
     */
    public static String getItemISSN(Item item) {
        String issn = null;

        if (issnElement == null) {
            throw new IllegalStateException("Missing DSpace configuration keys for SHERPA/RoMEO queries: sherpa.romeo.issn");
        }

        // We expect issnElement to be [schema].[element]{.[qualifier]?}
        List<String> issnParts = Arrays.asList(issnElement.split("\\."));

        String schema = null;
        String name = null;
        String qualifier = null;

        Iterator i = issnParts.iterator();
        if (i.hasNext()) {
            schema = (String) i.next();
        }
        if (i.hasNext()) {
            name = (String) i.next();
        }
        if (i.hasNext()) {
            qualifier = (String) i.next();
        }

        DCValue[] issns = item.getMetadata(schema, name, qualifier, Item.ANY);

        if (issns != null && issns.length > 0) {
            issn = issns[0].value;
        }

        return issn;
    }

    /**
     * Recycle
     */
    @Override
    public void recycle() {
        this.validity = null;
        super.recycle();
    }

}
