package ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo;

import java.util.List;

/**
 *
 * @author dspace
 */
public class Publisher {

    private String name;
    private String website;
    private String preprintPolicy;

//    private List<String> preprintConditions;

    // @fixme seriously dumb
    public Publisher(String name, String website, String policy) {
        this.name = name;
        this.website = website;
        this.preprintPolicy = policy;
    }
    
    public String getName() {
        return this.name;
    }

    public String getWebsite() {
        return this.website;
    }

    public String getPreprintPolicy() {
        return this.preprintPolicy;
    }

/*
    List<String> getPreprintConditions() {
        return this.preprintConditions;
    }
 *
 */

}
