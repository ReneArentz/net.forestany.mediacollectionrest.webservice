package net.forestany.mediacollectionrest.webservice;

public class MediaCollectionRest extends net.forestany.forestj.lib.net.https.rest.ForestREST {
    private net.forestany.forestj.lib.io.JSON o_jsonSmall = null;
    private net.forestany.forestj.lib.io.JSON o_jsonRecord = null;
    private net.forestany.forestj.lib.io.JSON o_json = null;
    private net.forestany.forestj.lib.Cryptography o_cryptography = null;
    private String s_authUser = null;
    private String s_authPassphrase = null;

    public MediaCollectionRest(String p_s_jsonSmallSchemaFile, String p_s_jsonRecordSchemaFile, String p_s_jsonSchemaFile, net.forestany.forestj.lib.Cryptography p_o_cryptography, String p_s_authUser, String p_s_authPassphrase) throws NullPointerException, IllegalArgumentException, java.io.IOException {
        if (net.forestany.forestj.lib.Helper.isStringEmpty(p_s_jsonSmallSchemaFile)) {
            throw new NullPointerException("Parameter for json small schema file is null");
        }

        if (net.forestany.forestj.lib.Helper.isStringEmpty(p_s_jsonRecordSchemaFile)) {
            throw new NullPointerException("Parameter for json record schema file is null");
        }

        if (net.forestany.forestj.lib.Helper.isStringEmpty(p_s_jsonSchemaFile)) {
            throw new NullPointerException("Parameter for json schema file is null");
        }

        if (p_o_cryptography == null) {
            throw new NullPointerException("Cryptography instance is null");
        }
        
        if (net.forestany.forestj.lib.Helper.isStringEmpty(p_s_authUser)) {
            throw new NullPointerException("Parameter for authentication user is null");
        }

        if (net.forestany.forestj.lib.Helper.isStringEmpty(p_s_authPassphrase)) {
            throw new NullPointerException("Parameter for authentication passphrase file is null");
        }

        this.o_jsonSmall = new net.forestany.forestj.lib.io.JSON(p_s_jsonSmallSchemaFile);
        this.o_jsonRecord = new net.forestany.forestj.lib.io.JSON(p_s_jsonRecordSchemaFile);
        this.o_json = new net.forestany.forestj.lib.io.JSON(p_s_jsonSchemaFile);
        this.o_cryptography = p_o_cryptography;
        this.s_authUser = p_s_authUser;
        this.s_authPassphrase = p_s_authPassphrase;
    }

    private <T> void setOtherBaseSource(net.forestany.forestj.lib.sql.Record<T> p_o_recordInstance) {
        /* get config to access base */
        MediaCollectionConfig o_config = (MediaCollectionConfig)this.getSeed().getConfig();

        /* use base from config class as other base source */
        p_o_recordInstance.setOtherBaseSource(
            new net.forestany.forestj.lib.sql.Record.IDelegate() {
                @Override public java.util.List<java.util.LinkedHashMap<String, Object>> OtherBaseSourceImplementation(net.forestany.forestj.lib.sqlcore.IQuery<?> p_o_sqlQuery) throws IllegalAccessException, RuntimeException, InterruptedException {
                    return o_config.getBase().fetchQuery((net.forestany.forestj.lib.sql.Query<?>)p_o_sqlQuery);
                }
            }
        );
    }

    @Override
    public String handleGET() throws Exception {
        try {
            String s_foo = "";

            net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "RequestPath = " + this.getSeed().getRequestHeader().getRequestPath());
            net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "File = " + this.getSeed().getRequestHeader().getFile());
            net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "Authorization = " + this.getSeed().getRequestHeader().getAuthorization());

            for (java.util.Map.Entry<String, String> o_parameter : this.getSeed().getRequestHeader().getParameters().entrySet()) {
                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "\t" + o_parameter.getKey() + " = " + o_parameter.getValue());
            }

            if (!this.chkAuth()) {
                throw new Exception("Bad GET request. Authorization failed.");
            }

            if (
                (!net.forestany.forestj.lib.Helper.isStringEmpty( this.getSeed().getRequestHeader().getFile() )) &&
                (this.getSeed().getRequestHeader().getFile().toLowerCase().contentEquals("all"))
            ) {
                JSONMediaCollectionSmall o_jsonMediaCollectionSmall = new JSONMediaCollectionSmall();
                o_jsonMediaCollectionSmall.Timestamp = java.time.LocalDateTime.now().withNano(0);

                LanguageRecord o_languageRecordInstance = new LanguageRecord();
                MediaCollectionRecordSmall o_mediaCollectionRecordSmallInstance = new MediaCollectionRecordSmall();

                o_mediaCollectionRecordSmallInstance.Columns = java.util.Arrays.asList(
                    "Id",
                    "UUID",
                    "PublicationYear",
                    "OriginalTitle",
                    "LastModified",
                    "Deleted",
                    "Poster"
                );
                
                this.setOtherBaseSource(o_languageRecordInstance);
                this.setOtherBaseSource(o_mediaCollectionRecordSmallInstance);

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "iterate all records for deletion check");

                /* delete records with deleted timestamp older than 30 days */
                for (MediaCollectionRecordSmall o_mediaCollectionRecordSmall : o_mediaCollectionRecordSmallInstance.getRecords(true)) {
                    if ( (o_mediaCollectionRecordSmall.ColumnDeleted != null) && (o_mediaCollectionRecordSmall.ColumnDeleted.isBefore( java.time.LocalDateTime.now().withNano(0).minusDays(30) )) ) {
                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "delete '" + o_mediaCollectionRecordSmall.ColumnOriginalTitle + "' - deleted: '" + o_mediaCollectionRecordSmall.ColumnDeleted + "'");
                        
                        this.setOtherBaseSource(o_mediaCollectionRecordSmall);
                        o_mediaCollectionRecordSmall.deleteRecord();
                    }
                }

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "iterate 'language' records");

                for (LanguageRecord o_languageRecord : o_languageRecordInstance.getRecords(true)) {
                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "add 'language' record: '" + o_languageRecord.ColumnLanguage + "'");
                    
                    o_jsonMediaCollectionSmall.Languages.add(o_languageRecord);
                }     

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "iterate 'mediacollection' records");

                for (MediaCollectionRecordSmall o_mediaCollectionRecordSmall : o_mediaCollectionRecordSmallInstance.getRecords(true)) {
                    String s_posterBytesLength = "0";

                    if (!net.forestany.forestj.lib.Helper.isStringEmpty(o_mediaCollectionRecordSmall.ColumnPoster)) {
                        s_posterBytesLength = o_mediaCollectionRecordSmall.ColumnPoster.length() + "";
                    }
                    
                    o_mediaCollectionRecordSmall.ColumnPoster = s_posterBytesLength;

                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "add 'mediacollection' record: '" + o_mediaCollectionRecordSmall.ColumnUUID + "' - '" + o_mediaCollectionRecordSmall.ColumnOriginalTitle + "'");
                        
                    o_jsonMediaCollectionSmall.Records.add(o_mediaCollectionRecordSmall);
                }

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "Encoding data to json string.");

                s_foo = this.o_jsonSmall.jsonEncode(o_jsonMediaCollectionSmall);
                
                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "Encoded data to json string.");
            } else if (
                (!net.forestany.forestj.lib.Helper.isStringEmpty( this.getSeed().getRequestHeader().getFile() )) &&
                (this.getSeed().getRequestHeader().getFile().toLowerCase().contentEquals("record"))
            ) {
                if (this.getSeed().getRequestHeader().getParameters().size() < 1) {
                    throw new Exception("Bad GET request. No parameters available.");
                }

                if (!this.getSeed().getRequestHeader().getParameters().containsKey("uuid")) {
                    throw new Exception("Bad GET request. No 'uuid' parameter available.");
                }

                MediaCollectionRecord o_mediaCollectionRecordInstance = new MediaCollectionRecord();
                this.setOtherBaseSource(o_mediaCollectionRecordInstance);

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "find 'mediacollection' record with uuid '" + this.getSeed().getRequestHeader().getParameters().get("uuid") + "'.");

                if (o_mediaCollectionRecordInstance.getOneRecord(java.util.Arrays.asList("UUID"), java.util.Arrays.asList( this.getSeed().getRequestHeader().getParameters().get("uuid") ))) {
                    String s_posterBytesLength = "0";

                    if (!net.forestany.forestj.lib.Helper.isStringEmpty(o_mediaCollectionRecordInstance.ColumnPoster)) {
                        s_posterBytesLength = o_mediaCollectionRecordInstance.ColumnPoster.length() + "";
                    }
                    
                    o_mediaCollectionRecordInstance.ColumnPoster = s_posterBytesLength;

                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "found '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "', encoding record to json string");

                    s_foo = this.o_jsonRecord.jsonEncode(o_mediaCollectionRecordInstance);
                
                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "encoded record to json string.");
                } else {
                    return "400;Record not found";
                }
            } else if (
                (!net.forestany.forestj.lib.Helper.isStringEmpty( this.getSeed().getRequestHeader().getFile() )) &&
                (this.getSeed().getRequestHeader().getFile().toLowerCase().contentEquals("poster"))
            ) {
                if (this.getSeed().getRequestHeader().getParameters().size() < 1) {
                    throw new Exception("Bad GET request. No parameters available.");
                }

                if (!this.getSeed().getRequestHeader().getParameters().containsKey("uuid")) {
                    throw new Exception("Bad GET request. No 'uuid' parameter available.");
                }

                boolean b_keepUncompressed = false;

                if ((this.getSeed().getRequestHeader().getParameters().containsKey("uncompressed")) && (this.getSeed().getRequestHeader().getParameters().get("uncompressed").toLowerCase().contentEquals("true"))) {
                    b_keepUncompressed = true;
                }

                MediaCollectionRecord o_mediaCollectionRecordInstance = new MediaCollectionRecord();
                this.setOtherBaseSource(o_mediaCollectionRecordInstance);

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "find 'mediacollection' record with uuid '" + this.getSeed().getRequestHeader().getParameters().get("uuid") + "'.");

                if (o_mediaCollectionRecordInstance.getOneRecord(java.util.Arrays.asList("UUID"), java.util.Arrays.asList( this.getSeed().getRequestHeader().getParameters().get("uuid") ))) {
                    if (o_mediaCollectionRecordInstance.ColumnPoster != null) {
                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "found '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "', adding poster hex string bytes ('" + (o_mediaCollectionRecordInstance.ColumnPoster.length()) + "') to response");
                        
                        if (b_keepUncompressed) {
                            s_foo = o_mediaCollectionRecordInstance.ColumnPoster;
                        } else {
                            String s_bar = compress(o_mediaCollectionRecordInstance.ColumnPoster);

                            net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "compressed to '" + s_bar.length() + "' bytes");

                            s_foo = s_bar;
                        }
                    } else {
                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "found '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "', adding poster hex string bytes ('0') to response");
                        
                        s_foo = "null";
                    }
                } else {
                    return "400;Record not found";
                }
            } else if (
                (!net.forestany.forestj.lib.Helper.isStringEmpty( this.getSeed().getRequestHeader().getFile() )) &&
                (this.getSeed().getRequestHeader().getFile().toLowerCase().contentEquals("posterhash"))
            ) {
                if (this.getSeed().getRequestHeader().getParameters().size() < 1) {
                    throw new Exception("Bad GET request. No parameters available.");
                }

                if (!this.getSeed().getRequestHeader().getParameters().containsKey("uuid")) {
                    throw new Exception("Bad GET request. No 'uuid' parameter available.");
                }

                MediaCollectionRecord o_mediaCollectionRecordInstance = new MediaCollectionRecord();
                this.setOtherBaseSource(o_mediaCollectionRecordInstance);

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "find 'mediacollection' record with uuid '" + this.getSeed().getRequestHeader().getParameters().get("uuid") + "'.");

                if (o_mediaCollectionRecordInstance.getOneRecord(java.util.Arrays.asList("UUID"), java.util.Arrays.asList( this.getSeed().getRequestHeader().getParameters().get("uuid") ))) {
                    if (o_mediaCollectionRecordInstance.ColumnPoster != null) {
                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "found '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "', adding poster hash to response");
                        
                        s_foo = net.forestany.forestj.lib.Helper.hashByteArray("SHA-256", o_mediaCollectionRecordInstance.ColumnPoster.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    } else {
                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "found '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "', adding poster hash 'null' to response");
                        
                        s_foo = "null";
                    }
                } else {
                    return "400;Record not found";
                }
            } else if (
                (!net.forestany.forestj.lib.Helper.isStringEmpty( this.getSeed().getRequestHeader().getFile() )) &&
                (this.getSeed().getRequestHeader().getFile().toLowerCase().contentEquals("posterbyteslength"))
            ) {
                if (this.getSeed().getRequestHeader().getParameters().size() < 1) {
                    throw new Exception("Bad GET request. No parameters available.");
                }

                if (!this.getSeed().getRequestHeader().getParameters().containsKey("uuid")) {
                    throw new Exception("Bad GET request. No 'uuid' parameter available.");
                }

                MediaCollectionRecord o_mediaCollectionRecordInstance = new MediaCollectionRecord();
                this.setOtherBaseSource(o_mediaCollectionRecordInstance);

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "find 'mediacollection' record with uuid '" + this.getSeed().getRequestHeader().getParameters().get("uuid") + "'.");

                if (o_mediaCollectionRecordInstance.getOneRecord(java.util.Arrays.asList("UUID"), java.util.Arrays.asList( this.getSeed().getRequestHeader().getParameters().get("uuid") ))) {
                    if (!net.forestany.forestj.lib.Helper.isStringEmpty(o_mediaCollectionRecordInstance.ColumnPoster)) {
                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "found '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "', adding poster bytes length to response");
                        
                        s_foo = o_mediaCollectionRecordInstance.ColumnPoster.length() + "";
                    } else {
                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "found '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "', adding poster bytes length '0' to response");
                        
                        s_foo = "0";
                    }
                } else {
                    return "400;Record not found";
                }
            } else {
                throw new Exception("Bad GET request.");
            }

            return s_foo;
        } catch (Exception o_exc) {
            net.forestany.forestj.lib.Global.logException(o_exc);
            return "400;" + o_exc.getMessage();
        }
    }

    @Override
    public String handlePOST() throws Exception {
        try {
            String s_foo = "";

            net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "RequestPath = " + this.getSeed().getRequestHeader().getRequestPath());
            net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "File = " + this.getSeed().getRequestHeader().getFile());
            net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "Authorization = " + this.getSeed().getRequestHeader().getAuthorization());

            if (!this.chkAuth()) {
                throw new Exception("Bad POST request. Authorization failed.");
            }

            if (
                (!net.forestany.forestj.lib.Helper.isStringEmpty( this.getSeed().getRequestHeader().getFile() )) &&
                (this.getSeed().getRequestHeader().getFile().toLowerCase().contentEquals("all"))
            ) {
                if (this.getSeed().getPostData().size() < 1) {
                    throw new Exception("Bad POST request. No post data available.");
                }

                if (this.getSeed().getPostData().size() != 1) {
                    throw new Exception("Bad POST request. No json post data available.");
                }

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "Decoding received json data.");

                String s_jsonPostData = this.getSeed().getPostData().entrySet().iterator().next().getKey();
                JSONMediaCollectionSmall o_jsonMediaCollectionSmall = (JSONMediaCollectionSmall)this.o_jsonSmall.jsonDecode(java.util.Arrays.asList(s_jsonPostData));

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "Received json data decoded. '" + o_jsonMediaCollectionSmall.Records.size() + "' records.");

                MediaCollectionRecordSmall o_mediaCollectionRecordSmallInstance = new MediaCollectionRecordSmall();

                o_mediaCollectionRecordSmallInstance.Columns = java.util.Arrays.asList(
                    "Id",
                    "UUID",
                    "PublicationYear",
                    "OriginalTitle",
                    "LastModified",
                    "Deleted",
                    "Poster"
                );

                this.setOtherBaseSource(o_mediaCollectionRecordSmallInstance);
                
                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "iterate all 'mediacollection' records for deletion check");

                /* delete records with deleted timestamp older than 30 days */
                for (MediaCollectionRecordSmall o_mediaCollectionRecordSmall : o_mediaCollectionRecordSmallInstance.getRecords(true)) {
                    if ( (o_mediaCollectionRecordSmall.ColumnDeleted != null) && (o_mediaCollectionRecordSmall.ColumnDeleted.isBefore( java.time.LocalDateTime.now().withNano(0).minusDays(30) )) ) {
                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "delete '" + o_mediaCollectionRecordSmall.ColumnOriginalTitle + "' - deleted: '" + o_mediaCollectionRecordSmall.ColumnDeleted + "'");
                        
                        this.setOtherBaseSource(o_mediaCollectionRecordSmall);
                        o_mediaCollectionRecordSmall.deleteRecord();
                    }
                }

                /* response list for answer */
                java.util.List<String> a_responseList = new java.util.ArrayList<String>();

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "iterate received 'mediacollection' records");

                /* iterate all sended records */
                if (o_jsonMediaCollectionSmall.Records.size() > 0) {
                    MediaCollectionRecord o_mediaCollectionRecordInstance = new MediaCollectionRecord();
                    this.setOtherBaseSource(o_mediaCollectionRecordInstance);

                    for (MediaCollectionRecordSmall jsonMediaCollectionRecordSmall : o_jsonMediaCollectionSmall.Records) {
                        if (o_mediaCollectionRecordInstance.getOneRecord(java.util.Arrays.asList("UUID"), java.util.Arrays.asList(jsonMediaCollectionRecordSmall.ColumnUUID))) {
                            if ( (jsonMediaCollectionRecordSmall.ColumnDeleted == null) && (o_mediaCollectionRecordInstance.ColumnDeleted == null) && (jsonMediaCollectionRecordSmall.ColumnLastModified.isAfter(o_mediaCollectionRecordInstance.ColumnLastModified)) ) {
                                /* sended record found, sended deleted is null on both sides, and sended record last modified timestamp is newer -> note uuid to send all record columns later */
                                
                                String s_posterBytesLength = "0";

                                if (!net.forestany.forestj.lib.Helper.isStringEmpty(o_mediaCollectionRecordInstance.ColumnPoster)) {
                                    s_posterBytesLength = o_mediaCollectionRecordInstance.ColumnPoster.length() + "";
                                }
                                
                                /* return UUID with 'UpdateWithPoster' if amounts of poster bytes on both sides do not match, otherwise just 'Update' because sended last modified timestamp is newer */
                                if ( ((net.forestany.forestj.lib.Helper.isStringEmpty(jsonMediaCollectionRecordSmall.ColumnPoster)) && (s_posterBytesLength.contentEquals("0"))) || (jsonMediaCollectionRecordSmall.ColumnPoster.contentEquals(s_posterBytesLength)) ) {
                                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "return UUID with 'Update' command for '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "' - sended last modified timestamp is newer");
                                    a_responseList.add(o_mediaCollectionRecordInstance.ColumnUUID + ";" + "Update");
                                } else {
                                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "return UUID with 'UpdateWithPoster' command for '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "' - sended last modified timestamp is newer and amounts of poster bytes on both sides do not match");
                                    a_responseList.add(o_mediaCollectionRecordInstance.ColumnUUID + ";" + "UpdateWithPoster");
                                }
                            } else if ( (jsonMediaCollectionRecordSmall.ColumnDeleted != null) && (!jsonMediaCollectionRecordSmall.ColumnDeleted.equals(o_mediaCollectionRecordInstance.ColumnDeleted)) ) {
                                /* sended record found, sended deleted is not null and local is not equal to it -> only update deleted */
                                o_mediaCollectionRecordInstance.ColumnDeleted = jsonMediaCollectionRecordSmall.ColumnDeleted;

                                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "update record '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "' - sended deleted('" + jsonMediaCollectionRecordSmall.ColumnDeleted + "') is not null and local('" + ((o_mediaCollectionRecordInstance.ColumnDeleted != null) ? o_mediaCollectionRecordInstance.ColumnDeleted : "null") + "') is not equal to it");

                                try {
                                    o_mediaCollectionRecordInstance.updateRecord(true);
                                } catch (IllegalStateException o_exc) {
                                    /* catch primary/unique violation and ignore it */
                                    net.forestany.forestj.lib.Global.ilogWarning(this.getSeed().getSalt() + " " + "update record '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "' error: " + o_exc.getMessage());
                                }
                            }
                        } else {
                            if (jsonMediaCollectionRecordSmall.ColumnDeleted == null) {
                                /* sended record not found and deleted is null */
                                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "check if record exists with original title and publication year");

                                /* prepare media collection record instance */
                                o_mediaCollectionRecordSmallInstance = new MediaCollectionRecordSmall();

                                o_mediaCollectionRecordSmallInstance.Columns = java.util.Arrays.asList(
                                    "Id",
                                    "UUID",
                                    "PublicationYear",
                                    "OriginalTitle",
                                    "LastModified",
                                    "Deleted",
                                    "Poster"
                                );

                                this.setOtherBaseSource(o_mediaCollectionRecordSmallInstance);

                                /* set filters */
                                o_mediaCollectionRecordSmallInstance.Filters.add(new net.forestany.forestj.lib.sql.Filter("OriginalTitle", jsonMediaCollectionRecordSmall.ColumnOriginalTitle, "=", "AND"));
                                o_mediaCollectionRecordSmallInstance.Filters.add(new net.forestany.forestj.lib.sql.Filter("PublicationYear", jsonMediaCollectionRecordSmall.ColumnPublicationYear, "=", "AND"));
                                o_mediaCollectionRecordSmallInstance.Filters.add(new net.forestany.forestj.lib.sql.Filter("Deleted", "NULL", "IS", "AND"));

                                /* check if record exists with original title and publication year */
                                if (o_mediaCollectionRecordSmallInstance.getRecords().size() > 0) {
                                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "found identical record with '" + jsonMediaCollectionRecordSmall.ColumnOriginalTitle + "' AND '" + jsonMediaCollectionRecordSmall.ColumnPublicationYear + "' AND Deleted IS NULL");
                                    
                                    /* record is already on server side with same 'original title', 'publication year' and 'deleted'(null) */
                                    /* first record on server wins */
                                    
                                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "return UUID with 'Delete' command and deleted timestamp so sender can set record to deleted on their side");

                                    /* return UUID with 'DeleteLocal' command and deleted timestamp so sender can set record to deleted on their side */
                                    java.time.ZonedDateTime o_zonedDatetime = java.time.LocalDateTime.now().withNano(0).atZone(java.time.ZoneId.systemDefault());
                                    String s_deleted = java.time.format.DateTimeFormatter.ISO_INSTANT.format(o_zonedDatetime.withZoneSameInstant(java.time.ZoneId.of("UTC")));
                                    s_deleted = s_deleted.substring(0, s_deleted.length() - 1);

                                    a_responseList.add(jsonMediaCollectionRecordSmall.ColumnUUID + ";" + "Delete" + ";" + s_deleted);
                                } else {
                                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "return UUID with 'Insert' command for a new record on server side");

                                    a_responseList.add(jsonMediaCollectionRecordSmall.ColumnUUID + ";" + "Insert");
                                }
                            }
                        }
                    }
                }

                /* return UUIDs or UUIDs with deleted timestamp where we want poster data */
                if (a_responseList.size() > 0) {
                    for (String s_response : a_responseList) {
                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "add '" + s_response + "' for poster data to response");
                        
                        s_foo += s_response + net.forestany.forestj.lib.net.https.Config.HTTP_LINEBREAK;
                    }
                } else {
                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "nothing available for update, add 'null' to response");

                    s_foo += "null" + net.forestany.forestj.lib.net.https.Config.HTTP_LINEBREAK;
                }

                /* remove last linebreak */
                if (s_foo.endsWith(net.forestany.forestj.lib.net.https.Config.HTTP_LINEBREAK)) {
                    s_foo = s_foo.substring(0, s_foo.length() - net.forestany.forestj.lib.net.https.Config.HTTP_LINEBREAK.length());
                }
            } else if (
                (!net.forestany.forestj.lib.Helper.isStringEmpty( this.getSeed().getRequestHeader().getFile() )) &&
                (this.getSeed().getRequestHeader().getFile().toLowerCase().contentEquals("insert"))
            ) {
                if (this.getSeed().getPostData().size() < 1) {
                    throw new Exception("Bad POST request. No post data available.");
                }

                if (this.getSeed().getPostData().size() != 1) {
                    throw new Exception("Bad POST request. No json post data available.");
                }

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "Decoding received json record.");

                String s_jsonPostData = this.getSeed().getPostData().entrySet().iterator().next().getKey();
                MediaCollectionRecord o_jsonMediaCollectionRecord = (MediaCollectionRecord)this.o_jsonRecord.jsonDecode(java.util.Arrays.asList(s_jsonPostData));

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "Received json record decoded. '" + o_jsonMediaCollectionRecord.ColumnUUID + "' - '" + o_jsonMediaCollectionRecord.ColumnOriginalTitle + "'");

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "check if record exists with original title and publication year");

                /* prepare media collection record instance */
                MediaCollectionRecordSmall o_mediaCollectionRecordSmallInstance = new MediaCollectionRecordSmall();

                o_mediaCollectionRecordSmallInstance.Columns = java.util.Arrays.asList(
                    "Id",
                    "UUID",
                    "PublicationYear",
                    "OriginalTitle",
                    "LastModified",
                    "Deleted",
                    "Poster"
                );

                this.setOtherBaseSource(o_mediaCollectionRecordSmallInstance);

                /* set filters */
                o_mediaCollectionRecordSmallInstance.Filters.add(new net.forestany.forestj.lib.sql.Filter("OriginalTitle", o_jsonMediaCollectionRecord.ColumnOriginalTitle, "=", "AND"));
                o_mediaCollectionRecordSmallInstance.Filters.add(new net.forestany.forestj.lib.sql.Filter("PublicationYear", o_jsonMediaCollectionRecord.ColumnPublicationYear, "=", "AND"));
                o_mediaCollectionRecordSmallInstance.Filters.add(new net.forestany.forestj.lib.sql.Filter("Deleted", "NULL", "IS", "AND"));

                /* check if record exists with original title and publication year */
                if (o_mediaCollectionRecordSmallInstance.getRecords().size() < 1) {
                    try {
                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "insert record '" + o_jsonMediaCollectionRecord.ColumnOriginalTitle + "'");
                        this.setOtherBaseSource(o_jsonMediaCollectionRecord);

                        if (o_jsonMediaCollectionRecord.insertRecord() > 0) {
                            /* return uuid for successful insert operation */
                            s_foo = o_jsonMediaCollectionRecord.ColumnUUID;
                        }
                    } catch (IllegalStateException o_exc) {
                        /* catch primary/unique violation and ignore it */
                        net.forestany.forestj.lib.Global.ilogWarning(this.getSeed().getSalt() + " " + "insert record '" + o_jsonMediaCollectionRecord.ColumnOriginalTitle + "' error: " + o_exc.getMessage());
                    }
                } else {
                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "record exists with original title '" + o_jsonMediaCollectionRecord.ColumnOriginalTitle + "' and publication year '" + o_jsonMediaCollectionRecord.ColumnPublicationYear + "'");
                    s_foo = "Exists";
                }
            } else if (
                (!net.forestany.forestj.lib.Helper.isStringEmpty( this.getSeed().getRequestHeader().getFile() )) &&
                (this.getSeed().getRequestHeader().getFile().toLowerCase().contentEquals("update"))
            ) {
                if (this.getSeed().getPostData().size() < 1) {
                    throw new Exception("Bad POST request. No post data available.");
                }

                if (this.getSeed().getPostData().size() != 1) {
                    throw new Exception("Bad POST request. No json post data available.");
                }

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "Decoding received json record.");

                String s_jsonPostData = this.getSeed().getPostData().entrySet().iterator().next().getKey();
                MediaCollectionRecord o_jsonMediaCollectionRecord = (MediaCollectionRecord)this.o_jsonRecord.jsonDecode(java.util.Arrays.asList(s_jsonPostData));

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "Received json record decoded. '" + o_jsonMediaCollectionRecord.ColumnUUID + "' - '" + o_jsonMediaCollectionRecord.ColumnOriginalTitle + "'");

                MediaCollectionRecord o_mediaCollectionRecordInstance = new MediaCollectionRecord();
                this.setOtherBaseSource(o_mediaCollectionRecordInstance);

                if (o_mediaCollectionRecordInstance.getOneRecord(java.util.Arrays.asList("UUID"), java.util.Arrays.asList(o_jsonMediaCollectionRecord.ColumnUUID))) {
                    if ( (o_jsonMediaCollectionRecord.ColumnDeleted == null) && (o_mediaCollectionRecordInstance.ColumnDeleted == null) && (o_jsonMediaCollectionRecord.ColumnLastModified.isAfter(o_mediaCollectionRecordInstance.ColumnLastModified)) ) {
                        /* sended record found, sended deleted is null on both sides, and sended record last modified timestamp is newer -> update record */
                        o_mediaCollectionRecordInstance.ColumnUUID = o_jsonMediaCollectionRecord.ColumnUUID;
                        o_mediaCollectionRecordInstance.ColumnTitle = o_jsonMediaCollectionRecord.ColumnTitle;
                        o_mediaCollectionRecordInstance.ColumnType = o_jsonMediaCollectionRecord.ColumnType;
                        o_mediaCollectionRecordInstance.ColumnPublicationYear = o_jsonMediaCollectionRecord.ColumnPublicationYear;
                        o_mediaCollectionRecordInstance.ColumnOriginalTitle = o_jsonMediaCollectionRecord.ColumnOriginalTitle;
                        o_mediaCollectionRecordInstance.ColumnSubType = o_jsonMediaCollectionRecord.ColumnSubType;
                        o_mediaCollectionRecordInstance.ColumnFiledUnder = o_jsonMediaCollectionRecord.ColumnFiledUnder;
                        o_mediaCollectionRecordInstance.ColumnLastSeen = o_jsonMediaCollectionRecord.ColumnLastSeen;
                        o_mediaCollectionRecordInstance.ColumnLengthInMinutes = o_jsonMediaCollectionRecord.ColumnLengthInMinutes;
                        o_mediaCollectionRecordInstance.ColumnLanguages = o_jsonMediaCollectionRecord.ColumnLanguages;
                        o_mediaCollectionRecordInstance.ColumnSubtitles = o_jsonMediaCollectionRecord.ColumnSubtitles;
                        o_mediaCollectionRecordInstance.ColumnDirectors = o_jsonMediaCollectionRecord.ColumnDirectors;
                        o_mediaCollectionRecordInstance.ColumnScreenwriters = o_jsonMediaCollectionRecord.ColumnScreenwriters;
                        o_mediaCollectionRecordInstance.ColumnCast = o_jsonMediaCollectionRecord.ColumnCast;
                        o_mediaCollectionRecordInstance.ColumnSpecialFeatures = o_jsonMediaCollectionRecord.ColumnSpecialFeatures;
                        o_mediaCollectionRecordInstance.ColumnOther = o_jsonMediaCollectionRecord.ColumnOther;
                        o_mediaCollectionRecordInstance.ColumnLastModified = o_jsonMediaCollectionRecord.ColumnLastModified;

                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "update record '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "' - deleted is null on both sides, and sended record last modified timestamp is newer '" + o_mediaCollectionRecordInstance.ColumnLastModified + "'");

                        try {
                            if (o_mediaCollectionRecordInstance.updateRecord(true) >= 0) {
                                /* return uuid for successful update operation */
                                s_foo = o_mediaCollectionRecordInstance.ColumnUUID;
                            }
                        } catch (IllegalStateException o_exc) {
                            /* catch primary/unique violation and ignore it */
                            net.forestany.forestj.lib.Global.ilogWarning(this.getSeed().getSalt() + " " + "update record '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "' error: " + o_exc.getMessage());
                        }
                    }
                }
            } else if (
                (!net.forestany.forestj.lib.Helper.isStringEmpty( this.getSeed().getRequestHeader().getFile() )) &&
                (this.getSeed().getRequestHeader().getFile().toLowerCase().contentEquals("poster"))
            ) {
                if (this.getSeed().getPostData().size() < 1) {
                    throw new Exception("Bad POST request. No post data available.");
                }

                if (!this.getSeed().getPostData().containsKey("uuid")) {
                    throw new Exception("Bad POST request. No post data with 'uuid' key available.");
                }

                if (!this.getSeed().getPostData().containsKey("posterdata")) {
                    throw new Exception("Bad POST request. No post data with 'posterdata' key available.");
                }

                boolean b_incomingUncompressed = false;

                if ((this.getSeed().getPostData().containsKey("uncompressed")) && (this.getSeed().getPostData().get("uncompressed").toLowerCase().contentEquals("true"))) {
                    b_incomingUncompressed = true;
                }

                MediaCollectionRecord o_mediaCollectionRecordInstance = new MediaCollectionRecord();
                this.setOtherBaseSource(o_mediaCollectionRecordInstance);

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "find 'mediacollection' record with uuid '" + this.getSeed().getPostData().get("uuid") + "'.");

                if (o_mediaCollectionRecordInstance.getOneRecord(java.util.Arrays.asList("UUID"), java.util.Arrays.asList( this.getSeed().getPostData().get("uuid") ))) {
                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "found record '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "' for updating poster data (" + (this.getSeed().getPostData().get("posterdata").length()) + " bytes)");
                    
                    if (b_incomingUncompressed) {
                        o_mediaCollectionRecordInstance.ColumnPoster = this.getSeed().getPostData().get("posterdata");
                    } else {
                        String s_bar = decompress(this.getSeed().getPostData().get("posterdata"));

                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "decompressed to '" + s_bar.length() + "' bytes");

                        o_mediaCollectionRecordInstance.ColumnPoster = s_bar;
                    }
                    
                    if (o_mediaCollectionRecordInstance.updateRecord(true) < 0) {
                        throw new Exception("Record could not be updated.");
                    } else {
                        //s_foo = net.forestany.forestj.lib.Helper.hashByteArray("SHA-256", o_mediaCollectionRecordInstance.ColumnPoster.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        s_foo = o_mediaCollectionRecordInstance.ColumnPoster.length() + "";
                    }
                } else {
                    throw new Exception("No record found with 'uuid' key.");
                }
            } else {
                throw new Exception("Bad POST request.");
            }

            return s_foo;
        } catch (Exception o_exc) {
            net.forestany.forestj.lib.Global.logException(o_exc);
            return "400;" + o_exc.getMessage();
        }
    }

    @Override
    public String handlePUT() throws Exception {
        return "400;Method Not Allowed.";
    }

    @Override
    public String handleDELETE() throws Exception {
        return "400;Method Not Allowed.";
    }

    public boolean chkAuth() throws Exception {
        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "check authorization");

        if (net.forestany.forestj.lib.Helper.isStringEmpty(this.getSeed().getRequestHeader().getAuthorization())) {
            return false;
        }

        String s_authToken = this.getSeed().getRequestHeader().getAuthorization();

        if (s_authToken.startsWith("Basic ")) {
            s_authToken = s_authToken.substring(6);
        }

        String s_authString = new String( java.util.Base64.getDecoder().decode(s_authToken) );

        if (!s_authString.contains(":")) {
            return false;
        }

        String[] a_authArray = s_authString.split(":");

        if (a_authArray.length != 2) {
            return false;
        }

        if (!a_authArray[0].contentEquals(this.s_authUser)) {
            return false;
        }

        String s_decrypted = "";

        try {
            byte[] a_decrypted = this.o_cryptography.decrypt( java.util.Base64.getDecoder().decode(a_authArray[1]) );
            s_decrypted = new String(a_decrypted, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception o_exc) {
            net.forestany.forestj.lib.Global.logException(o_exc);
            return false;
        }

        if (!s_decrypted.contentEquals(this.s_authPassphrase)) {
            return false;
        }

        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "authorization granted");

        return true;
    }

    /* compress a hex string to Base64 */
    public static String compress(String p_s_hexString) throws Exception {
        /* convert hex string to byte array */
        byte[] input = net.forestany.forestj.lib.Helper.hexStringToBytes(p_s_hexString);

        /* compress */
        java.util.zip.Deflater deflater = new java.util.zip.Deflater();
        deflater.setInput(input);
        deflater.finish();

        byte[] buffer = new byte[input.length];
        int compressedDataLength = deflater.deflate(buffer);
        deflater.end();

        byte[] compressedBytes = java.util.Arrays.copyOf(buffer, compressedDataLength);

        /* encode as Base64 string */
        return java.util.Base64.getEncoder().encodeToString(compressedBytes);
    }

    /* decompress Base64 back to hex string */
    public static String decompress(String p_s_base64Compressed) throws Exception {
        /* decode Base64 to compressed bytes */
        byte[] compressedData = java.util.Base64.getDecoder().decode(p_s_base64Compressed);

        /* decompress */
        java.util.zip.Inflater inflater = new java.util.zip.Inflater();
        inflater.setInput(compressedData);

        byte[] buffer = new byte[10_000_000]; /* big enough buffer */
        int length = inflater.inflate(buffer);
        inflater.end();

        byte[] result = java.util.Arrays.copyOf(buffer, length);

        /* convert back to hex string */
        return net.forestany.forestj.lib.Helper.bytesToHexString(result, false);
    }
}