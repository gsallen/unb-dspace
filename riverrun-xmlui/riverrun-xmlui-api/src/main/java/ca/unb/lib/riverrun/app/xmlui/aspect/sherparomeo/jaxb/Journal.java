//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.05.05 at 12:04:17 PM ADT 
//


package ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "jtitle",
    "issn",
    "zetocpub",
    "romeopub"
})
@XmlRootElement(name = "journal")
public class Journal {

    @XmlElement(required = true)
    protected String jtitle;
    protected String issn;
    protected String zetocpub;
    protected String romeopub;

    /**
     * Gets the value of the jtitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJtitle() {
        return jtitle;
    }

    /**
     * Sets the value of the jtitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJtitle(String value) {
        this.jtitle = value;
    }

    /**
     * Gets the value of the issn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIssn() {
        return issn;
    }

    /**
     * Sets the value of the issn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIssn(String value) {
        this.issn = value;
    }

    /**
     * Gets the value of the zetocpub property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getZetocpub() {
        return zetocpub;
    }

    /**
     * Sets the value of the zetocpub property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setZetocpub(String value) {
        this.zetocpub = value;
    }

    /**
     * Gets the value of the romeopub property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRomeopub() {
        return romeopub;
    }

    /**
     * Sets the value of the romeopub property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRomeopub(String value) {
        this.romeopub = value;
    }

}