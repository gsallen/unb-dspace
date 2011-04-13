/*
 * PolicyViewer.java
 */
package ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.sql.SQLException;
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
import org.dspace.content.crosswalk.CrosswalkException;
import org.dspace.content.crosswalk.DisseminationCrosswalk;
import org.dspace.core.PluginManager;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

/**
 * @todo: write me
 * Display policy...
 */
public class PolicyViewer extends AbstractDSpaceTransformer implements CacheableProcessingComponent
{
    private static final Logger log = Logger.getLogger(PolicyViewer.class);
    
    /** Language strings */
    private static final Message T_dspace_home =
        message("xmlui.general.dspace_home");
    
    private static final Message T_trail =
        message("xmlui.ArtifactBrowser.ItemViewer.trail");
    
    private static final Message T_show_simple =
        message("xmlui.ArtifactBrowser.ItemViewer.show_simple");
    
    private static final Message T_show_full =
        message("xmlui.ArtifactBrowser.ItemViewer.show_full");
    
    private static final Message T_head_parent_collections =
        message("xmlui.ArtifactBrowser.ItemViewer.head_parent_collections");
    
	/** Cached validity object */
	private SourceValidity validity = null;
	
	/** XHTML crosswalk instance */
	private DisseminationCrosswalk xHTMLHeadCrosswalk = null;
	
    /**
     * Generate the unique caching key.
     * This key must be unique inside the space of this component.
     */
    public Serializable getKey() {
        try {
            DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
            
            if (dso == null)
                return "0"; // no item, something is wrong.
            
            return HashUtil.hash(dso.getHandle() + "show-policies");
        } 
        catch (SQLException sqle)
        {
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
    public SourceValidity getValidity() 
    {
        DSpaceObject dso = null;

        if (this.validity == null)
    	{
	        try {
	            dso = HandleUtil.obtainHandle(objectModel);
	            
	            DSpaceValidity dsValidity = new DSpaceValidity();
	            dsValidity.add(dso);
	            this.validity =  dsValidity.complete();
	        }
	        catch (Exception e)
	        {
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
            AuthorizeException
    {
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (!(dso instanceof Item))
            return;
        Item item = (Item) dso;

        // Set the page title
        String title = getItemTitle(item);

        if (title != null)
            pageMeta.addMetadata("title").addContent(title);
        else
            pageMeta.addMetadata("title").addContent(item.getHandle());

        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
        HandleUtil.buildHandleTrail(item,pageMeta,contextPath);
        pageMeta.addTrail().addContent(T_trail);
        
        // Metadata for <head> element
        if (xHTMLHeadCrosswalk == null)
        {
            xHTMLHeadCrosswalk = (DisseminationCrosswalk) PluginManager.getNamedPlugin(
              DisseminationCrosswalk.class, "XHTML_HEAD_ITEM");
        }

        // Produce <meta> elements for header from crosswalk
        try
        {
            List l = xHTMLHeadCrosswalk.disseminateList(item);
            StringWriter sw = new StringWriter();

            XMLOutputter xmlo = new XMLOutputter();
            xmlo.output(new Text("\n"), sw);
            for (int i = 0; i < l.size(); i++)
            {
                Element e = (Element) l.get(i);
                // FIXME: we unset the Namespace so it's not printed.
                // This is fairly yucky, but means the same crosswalk should
                // work for Manakin as well as the JSP-based UI.
                e.setNamespace(null);
                xmlo.output(e, sw);
                xmlo.output(new Text("\n"), sw);
            }
            pageMeta.addMetadata("xhtml_head_item").addContent(sw.toString());
        }
        catch (CrosswalkException ce)
        {
            // TODO: Is this the right exception class?
            throw new WingException(ce);
        }
    }

    /**
     * Display a single item
     */
    @Override
    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {

        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (!(dso instanceof Item))
            return;
        Item item = (Item) dso;
        
        // Build the policy viewer division.
        Division division = body.addDivision("policy-view","primary");
        division.setHead(item.getHandle());

        Para testPara = division.addPara();
        testPara.addContent("This is a test paragraph.");

    }

    /**
     * Obtain the item's title.
     */
    public static String getItemTitle(Item item)
    {
        DCValue[] titles = item.getDC("title", Item.ANY, Item.ANY);

        String title;
        if (titles != null && titles.length > 0)
            title = titles[0].value;
        else
            title = null;
        return title;
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
