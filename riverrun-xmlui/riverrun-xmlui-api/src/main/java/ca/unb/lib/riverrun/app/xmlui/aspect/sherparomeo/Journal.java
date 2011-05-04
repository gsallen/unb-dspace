package ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo;

public class Journal {

    private String title;
    private String issn;

    public Journal(String title, String issn) {
        this.title = title;
        this.issn = issn;
    }

    public String getTitle() {
        return this.title;
    }

    public String getISSN() {
        return this.issn;
    }
}
