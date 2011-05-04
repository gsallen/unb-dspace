package ca.unb.lib.riverrun.app.xmlui.aspect.sherparomeo;

public class Response {
    
    // Possible responses to S/R queries
    // @todo: move this?
    public enum Outcome {
        notFound, // No results for ISSN, journal title, or publisher name search
        publisherFound, // Publisher name search successful
        uniqueZetoc, // Publisher identified but not indexed by S/R
        singleJournal, // 1 result in ISSN or journal title search
        manyJournals, // 2 or more results in journal title search
        excessJournals,  // 50+ journals found; results 1-50 returned
        failed; // He's dead, Jim.
    }

    private Outcome outcome = Outcome.failed;
    private String disclaimer = "";

    public Response() {
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

}
