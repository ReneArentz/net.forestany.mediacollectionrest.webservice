package net.forestany.mediacollectionrest.webservice;

public class MediaCollectionRecordSmall extends net.forestany.forestj.lib.sql.Record<MediaCollectionRecordSmall> {

    /* Fields */

    public int ColumnId = 0;
    public String ColumnUUID = null;
    public short ColumnPublicationYear = 0;
    public String ColumnOriginalTitle = null;
    public java.time.LocalDateTime ColumnLastModified = null;
    public java.time.LocalDateTime ColumnDeleted = null;
    public String ColumnPoster = null;

    /* Properties */

    /* Methods */

    public MediaCollectionRecordSmall() throws NullPointerException, IllegalArgumentException, NoSuchFieldException {
        super();
    }

    protected void init() {
        this.RecordImageClass = MediaCollectionRecordSmall.class;
        this.Table = "mediacollection";
        this.Primary.add("Id");
        this.Unique.add("UUID");
        this.Unique.add("OriginalTitle;PublicationYear;Deleted");
        this.OrderBy.put("Id", true);
        this.Interval = 10;
    }
}
