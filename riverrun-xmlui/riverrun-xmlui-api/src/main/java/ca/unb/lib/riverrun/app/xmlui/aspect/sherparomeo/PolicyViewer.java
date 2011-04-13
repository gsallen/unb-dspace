/*
 * PolicyViewer.java
 */
package ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

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
import org.xml.sax.SAXException;

/**
 * @todo: write me
 * Display policy...
 */
public class PolicyViewer extends AbstractDSpaceTransformer implements CacheableProcessingComponent
{
    private static final Logger log = Logger.getLogger(PolicyViewer.class);
    
    /** Language strings */
    private static final Message T_DSPACE_HOME =
        message("xmlui.general.dspace_home");
    
    private static final Message T_ITEM_TRAIL =
        message("xmlui.ArtifactBrowser.ItemViewer.trail");

    private static final Message T_POLICY_TRAIL =
        message("xmlui.SherpaRomeo.PolicyViewer.trail");
    
	/** Cached validity object */
	private SourceValidity validity = null;
	
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


        String title = getItemTitle(item);

        if (title != null)
            pageMeta.addMetadata("title").addContent(title);
        else
            pageMeta.addMetadata("title").addContent(item.getHandle());

        pageMeta.addTrailLink(contextPath + "/", T_DSPACE_HOME);
        HandleUtil.buildHandleTrail(item,pageMeta,contextPath);
        
        // Add trail items for referring item & current view
        pageMeta.addTrailLink(contextPath + "/handle/" + item.getHandle(), T_ITEM_TRAIL);
        pageMeta.addTrail().addContent(T_POLICY_TRAIL);
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

        division.addPara().addContent("This is another message");

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
