//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.05.05 at 12:04:17 PM ADT 
//


package ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo.jaxb;

import java.util.ArrayList;
import java.util.List;
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
    "postarchiving",
    "postrestrictions"
})
@XmlRootElement(name = "postprints")
public class Postprints {

    @XmlElement(required = true)
    protected String postarchiving;
    protected List<Postrestrictions> postrestrictions;

    /**
     * Gets the value of the postarchiving property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostarchiving() {
        return postarchiving;
    }

    /**
     * Sets the value of the postarchiving property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPostarchiving(String value) {
        this.postarchiving = value;
    }

    /**
     * Gets the value of the postrestrictions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the postrestrictions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPostrestrictions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Postrestrictions }
     * 
     * 
     */
    public List<Postrestrictions> getPostrestrictions() {
        if (postrestrictions == null) {
            postrestrictions = new ArrayList<Postrestrictions>();
        }
        return this.postrestrictions;
    }

}
