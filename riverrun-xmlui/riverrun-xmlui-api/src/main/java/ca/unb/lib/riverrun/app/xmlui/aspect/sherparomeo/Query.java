package ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo;

import ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo.Response.Outcome;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Query {
    private static final Logger log = Logger.getLogger(Query.class);

    // SHERPA/RoMEO API URL, defined in DSpace config file
    private static final String sherpaRomeoAPI;

    // Query parameters
    // @todo: add more
    private String issn;

    // Response from S/R
    private Response response;

    // Fetch S/R API URL from DSpace config
    static {
       sherpaRomeoAPI = ConfigurationManager.getProperty("sherpa.romeo.url");
    }


    // @todo: fix this.  This should be a static class that creates queries (?)
    public Query(String issn) {
        this.issn = issn;
    }

    public Response getResponse() {
        if (this.response == null)
            initResponse();

        return response;
    }

    private void initResponse() {
        // Initialize a blank response
        this.response = new Response();

        // Query S/R for publisher info
        NameValuePair[] args = new NameValuePair[]{new NameValuePair("issn", this.issn)};
        String srUrl = sherpaRomeoAPI + "?" + EncodingUtil.formUrlEncode(args, "UTF8");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(srUrl);

            // Fetch query outcome
            XPathExpression expr = xpath.compile("//outcome");

            // This may throw an IllegalArgumentException
            Outcome outcome = Outcome.valueOf(expr.evaluate(dom));
            response.setOutcome(outcome);

            // S/R disclaimer
            expr = xpath.compile("//disclaimer");
            response.setDisclaimer(expr.evaluate(dom));

        }
        catch (ParserConfigurationException ex) {
            log.error("Parser configuration failure", ex);
        }
        catch (SAXException ex) {
            log.error("Can't parse SHERPA/RoMEO response", ex);
        }
        catch (IOException ex) {
            log.error("Can't read SHERPA/RoMEO response", ex);
        }
        catch (XPathExpressionException ex) {
            log.error("Error compiling XPath expression", ex);
        }
        catch (IllegalArgumentException ex) {
            log.error("Unknown outcome in SHERPA/RoMEO response", ex);
        }
        finally {
            // An empty response is fine.
        }
    }
/*
    private Journal getJournal(Element journalElement) {

        String jtitle = getTextValue(journalElement, "jtitle");

        // Use ISSN from query response, rather than request:
        // the query may correct or compensate for poorly-formed ISSNs.
        String responseISSN = getTextValue(journalElement, "issn");

        return new Journal(jtitle, responseISSN);
    }

    private Publisher getPublisher(Element pubElement) {
        String name = getTextValue(pubElement, "name");
        String website = getTextValue(pubElement, "homeurl");
        String policy = getTextValue(pubElement, "prearchiving");

        return new Publisher(name, website, policy);
    }
*/



}
