package net.forestany.mediacollectionrest.webservice;

public class LanguageRecord extends net.forestany.forestj.lib.sql.Record<LanguageRecord> {

    /* Fields */

    public int ColumnId = 0;
    public String ColumnLanguage = null;

    /* Properties */

    /* Methods */

    public LanguageRecord() throws NullPointerException, IllegalArgumentException, NoSuchFieldException {
        super();
    }

    protected void init() {
        this.RecordImageClass = LanguageRecord.class;
        this.Table = "languages";
        this.Primary.add("Id");
        this.Unique.add("Language");
        this.OrderBy.put("Language", true);
        this.Interval = 50;
    }
}
