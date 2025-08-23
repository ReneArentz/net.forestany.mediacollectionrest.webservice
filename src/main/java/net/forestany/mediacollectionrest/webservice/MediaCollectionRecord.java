package net.forestany.mediacollectionrest.webservice;

public class MediaCollectionRecord extends net.forestany.forestj.lib.sql.Record<MediaCollectionRecord> {

    /* Fields */

    public int ColumnId = 0;
    public String ColumnUUID = null;
    public String ColumnTitle = null;
    public String ColumnType = null;
    public short ColumnPublicationYear = 0;
    public String ColumnOriginalTitle = null;
    public String ColumnSubType = null;
    public String ColumnFiledUnder = null;
    public java.time.LocalDateTime ColumnLastSeen = null;
    public short ColumnLengthInMinutes = 0;
    public String ColumnLanguages = null;
    public String ColumnSubtitles = null;
    public String ColumnDirectors = null;
    public String ColumnScreenwriters = null;
    public String ColumnCast = null;
    public String ColumnSpecialFeatures = null;
    public String ColumnOther = null;
    public java.time.LocalDateTime ColumnLastModified = null;
    public java.time.LocalDateTime ColumnDeleted = null;
    public String ColumnPoster = null;

    /* Properties */

    /* Methods */

    public MediaCollectionRecord() throws NullPointerException, IllegalArgumentException, NoSuchFieldException {
        super();
    }

    protected void init() {
        this.RecordImageClass = MediaCollectionRecord.class;
        this.Table = "mediacollection";
        this.Primary.add("Id");
        this.Unique.add("UUID");
        this.Unique.add("OriginalTitle;PublicationYear;Deleted");
        this.OrderBy.put("FiledUnder", true);
        this.OrderBy.put("Title", true);
        this.Interval = 10;
    }
}
