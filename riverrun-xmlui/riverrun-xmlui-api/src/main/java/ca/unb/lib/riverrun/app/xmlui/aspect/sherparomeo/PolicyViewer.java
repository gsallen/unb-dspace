/*
 * PolicyViewer.java
 */
package ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo;

import ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo.jaxb.Condition;
import ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo.jaxb.Copyrightlink;
import ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo.jaxb.Journal;
import ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo.jaxb.Postrestriction;
import ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo.jaxb.Postrestrictions;
import ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo.jaxb.Prerestriction;
import ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo.jaxb.Prerestrictions;
import ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo.jaxb.Publisher;
import ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo.jaxb.Romeoapi;
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
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.xml.sax.SAXException;

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

    private static final Message T_FAILED =
                                 message("xmlui.SherpaRomeo.PolicyViewer.failed");

    private static final Message T_NO_INFORMATION =
                                 message("xmlui.SherpaRomeo.PolicyViewer.no_information");

    private static final Message T_CONTACT_US =
                                 message("xmlui.SherpaRomeo.PolicyViewer.contact_us");

    /** Cached validity object */
    private SourceValidity validity = null;

    /** DSpace metadata element that stores ISSN */
    private static String issnElement = null;

    /** S/R responses */
    /** @todo move this into Query? */
    public enum Outcome {
        failed, notFound, singleJournal, manyJournals, excessJournals,
        publisherFound, uniqueZetoc;
    }

    /** S/R archiving permission */
    /** @todo: move this into Query? */
    public enum Permission {
        can, cannot, restricted, unclear, unknown
    }


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
        Query query = new Query(getItemISSN(item));
        Romeoapi response = query.getResponse();

        if (response != null) {

            try {
                // Throws IllegalArgumentException if String retured by
                // getOutcome() doesn't map to a value in enum list
                Outcome outcome = Outcome.valueOf(response.getHeader().getOutcome());

                switch (outcome) {

                    case failed:
                        displayFailed(division);
                        break;

                    case notFound:
                        displayNotFound(division);
                        break;

                    case singleJournal:
                        displayJournal(division, response);
                        break;

                    case manyJournals:
                    case excessJournals:
                    case publisherFound:
                    case uniqueZetoc:
                        // Responses to unimplemented S/R query types
                        // it's an error if they're received
                        log.error("Unexpected SHERPA/RoMEO outcome: " + outcome);
                        displayFailed(division);
                        break;

                    default:
                        // Unhandled outcomes are an error.
                        log.error("Unhandled SHERPA/RoMEO outcome: " + outcome);
                        displayFailed(division);
                        break;
                }
            }
            catch (IllegalArgumentException ex) {
                // Log the error, report failure.
                log.error("Unknown SHERPA/RoMEO outcome", ex);
                displayFailed(division);
            }
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

    private void displayFailed(Division division) throws WingException {
        division.addPara(T_FAILED);
        division.addPara(T_CONTACT_US);
    }

    private void displayNotFound(Division division) throws WingException {
        division.addPara(T_NO_INFORMATION);
        division.addPara(T_CONTACT_US);
    }


    private void displayJournal(Division division, Romeoapi response) throws WingException {
        Journal journal = response.getJournals().getJournal().get(0);
        
        Division journalDivision = division.addDivision("single-journal");

        journalDivision.setHead(journal.getJtitle());
        journalDivision.addPara("ISSN: " + journal.getIssn());

        List<Publisher> publisherList = response.getPublishers().getPublisher();

        if (publisherList.isEmpty()) {
            journalDivision.addPara("No publishers found.");
        }
        else {
            journalDivision.addPara("Information found for " + publisherList.size() + " publisher(s).");

            Iterator i = publisherList.iterator();
            while (i.hasNext()) {

                Publisher publ = (Publisher) i.next();
                Division publDivision = journalDivision.addDivision("publisher");
                publDivision.setHead(publ.getName());

                // Publisher website, if available
                if (! publ.getHomeurl().trim().isEmpty())
                    publDivision.addPara().addXref(publ.getHomeurl(), publ.getHomeurl());

                // Preprint policy
                Division preprintPolicy = publDivision.addDivision("preprint-policy");
                preprintPolicy.setHead("Author's Pre-print");
                preprintPolicy.addPara(getPermissionDescription(publ.getPreprints().getPrearchiving()));

                // One or more lists of preprint conditions
                List<Prerestrictions> prerestrictionsList = publ.getPreprints().getPrerestrictions();
                if (! prerestrictionsList.isEmpty()) {
                    org.dspace.app.xmlui.wing.element.List preprintConditions = preprintPolicy.addList("preprint-conditions");

                    Iterator iprel = prerestrictionsList.iterator();
                    while (iprel.hasNext()) {
                        Prerestrictions prerestrictions = (Prerestrictions) iprel.next();

                        // Get a list of individual preresticons
                        List<Prerestriction> prerestriction = prerestrictions.getPrerestriction();
                        Iterator ipre = prerestriction.iterator();
                        while (ipre.hasNext()) {
                            Prerestriction pre = (Prerestriction) i.next();
                            preprintConditions.addItem(pre.getvalue());
                        }
                    }
                }

               // Postprint policy
                Division postprintPolicy = publDivision.addDivision("postprint-policy");
                postprintPolicy.setHead("Author's Post-print");
                postprintPolicy.addPara(getPermissionDescription(publ.getPostprints().getPostarchiving()));

                // One or more lists of postprint conditions
                List<Postrestrictions> postrestrictionsList = publ.getPostprints().getPostrestrictions();
                if (! postrestrictionsList.isEmpty()) {
                    org.dspace.app.xmlui.wing.element.List postprintConditions = postprintPolicy.addList("postprint-conditions");

                    Iterator ipostl = postrestrictionsList.iterator();
                    while (ipostl.hasNext()) {
                        Postrestrictions postrestrictions = (Postrestrictions) ipostl.next();

                        // Get a list of individual post-restrictions
                        List<Postrestriction> postrestriction = postrestrictions.getPostrestriction();
                        Iterator ipost = postrestriction.iterator();
                        while (ipost.hasNext()) {
                            Postrestriction post = (Postrestriction) i.next();
                            postprintConditions.addItem(post.getvalue());
                        }
                    }
                }

                // General Conditions, if any
                List<Condition> conditionList = publ.getConditions().getCondition();
                if (! conditionList.isEmpty()) {
                    org.dspace.app.xmlui.wing.element.List conditions  = publDivision.addList("conditions");
                    conditions.setHead("General Conditions");

                    Iterator ic = conditionList.iterator();
                    while (ic.hasNext()) {
                        Condition cond = (Condition) ic.next();
                        conditions.addItem(cond.getvalue());
                    }
                }
                // Paid open access, if any
                Division paidaccessDiv = publDivision.addDivision("paid-access");
                paidaccessDiv.setHead("Paid Open Access");
                Para paidaccessPara = paidaccessDiv.addPara();

                String paidaccessName = publ.getPaidaccess().getPaidaccessname().trim();
                String paidaccessURL = publ.getPaidaccess().getPaidaccessurl().trim();
                String paidaccessNotes = publ.getPaidaccess().getPaidaccessnotes().trim();

                if (paidaccessName.isEmpty() && paidaccessURL.isEmpty()
                        && paidaccessNotes.isEmpty()) {
                    paidaccessPara.addContent("No information is available about paid open-accces options.");
                }
                else {
                    // format a URL
                    // @fixme ugly
                    if (! paidaccessName.isEmpty() && ! paidaccessURL.isEmpty()) {
                        paidaccessPara.addXref(paidaccessURL, paidaccessName);
                    }
                    else if (paidaccessName.isEmpty()) {
                        // we only have the target
                        paidaccessPara.addXref(paidaccessURL, paidaccessURL);
                    }
                    else {
                        // we only have the name
                        paidaccessPara.addContent(paidaccessName);
                    }

                    // Add notes, if available
                    if (! paidaccessNotes.isEmpty()) {
                        paidaccessPara.addContent(" (" + paidaccessNotes + ")");
                    }

                }
                // Copyright
                Division copyrightDivision = publDivision.addDivision("copyright");
                copyrightDivision.setHead("Copyright");

                List<Copyrightlink> copyrightlinks = publ.getCopyrightlinks().getCopyrightlink();

                if (copyrightlinks.isEmpty()) {
                    copyrightDivision.addPara("Copyright information is not available.");
                }
                else {
                    Iterator ic = copyrightlinks.iterator();
                    Para linksPara = copyrightDivision.addPara();

                    // @fixme link generation w/ possibly empty text, target repeated
                    while (ic.hasNext()) {
                        Copyrightlink copyrightlink = (Copyrightlink) ic.next();
                        String crText = copyrightlink.getCopyrightlinktext().trim();
                        String crURL = copyrightlink.getCopyrightlinkurl().trim();
                        
                        if (crText.isEmpty() && crURL.isEmpty()) {
                            continue;
                        }
                        if (crURL.isEmpty()) {
                            linksPara.addContent(crURL);
                            continue;
                        }
                        if (crText.isEmpty()) {
                            linksPara.addXref(crURL, crURL);
                            continue;
                        }
                        
                        // both present then.
                        linksPara.addXref(crURL, crText);

                        if (ic.hasNext())
                            linksPara.addContent(", ");
                    }
                }
                
                // Last updated
                if (! publ.getDateupdated().trim().isEmpty()) {
                    publDivision.addPara("last-updated", null).addContent("Last updated: " +  publ.getDateupdated().trim());
                }
            }
        }
    }

    // @fixme descriptions should be part of the enum
    private String getPermissionDescription(String permissionText) {

        String permissionDesc;

        try {
            Permission permission = Permission.valueOf(permissionText);
        
            switch (permission) {

                case can:
                    permissionDesc = "Self-archiving is permitted.";
                    break;

                case cannot:
                    permissionDesc = "Self-archiving is not permitted.";
                    break;

                case restricted:
                    permissionDesc = "Self-archiving is permitted, subject to the following restrictions.";
                    break;

                case unclear:
                    permissionDesc = "Self-archiving permission is unclear.";
                    break;

                case unknown:
                    permissionDesc = "Self-archiving permission is unknown.";
                    break;

                default:
                    log.error("Unknown SHERPA/RoMEO self-archiving permission");
                    permissionDesc = "An error has occurred.  Please contact us.";
            }
        }
        catch (IllegalArgumentException ex) {
            log.error("unknown", ex);
            permissionDesc = "An error has occurred. Please contact us.";
        }

        return permissionDesc;

    }

}
