package utils;

public enum TermHoodMeasure {

    LIDF_value("LIDF_value"),
    F_TFIDF_C_A("F-TFIDF-C_A"),
    TFIDF_A("TFIDF_A"),
    F_OCapi_A("F-OCapi_A"),
    C_value("C_value"),
    TFIDF_S("TFIDF_S");

    private String name = "";

    TermHoodMeasure(String name){
        this.name = name;
    }

    public String toString(){
        return name;
    }

}
