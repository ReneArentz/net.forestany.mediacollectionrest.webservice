package net.forestany.mediacollectionrest.webservice;

public class MediaCollectionRest extends net.forestany.forestj.lib.net.https.rest.ForestREST {
    private net.forestany.forestj.lib.io.JSON o_json = null;
    private net.forestany.forestj.lib.Cryptography o_cryptography = null;
    private String s_authUser = null;
    private String s_authPassphrase = null;

    public MediaCollectionRest(String p_s_jsonSchemaFile, net.forestany.forestj.lib.Cryptography p_o_cryptography, String p_s_authUser, String p_s_authPassphrase) throws NullPointerException, IllegalArgumentException, java.io.IOException {
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
                JSONMediaCollection o_jsonMediaCollection = new JSONMediaCollection();
                o_jsonMediaCollection.Timestamp = java.time.LocalDateTime.now().withNano(0);

                LanguageRecord o_languageRecordInstance = new LanguageRecord();
                MediaCollectionRecord o_mediaCollectionRecordInstance = new MediaCollectionRecord();
                this.setOtherBaseSource(o_languageRecordInstance);
                this.setOtherBaseSource(o_mediaCollectionRecordInstance);

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "iterate all records for deletion check");

                /* delete records with deleted timestamp older than 30 days */
                for (MediaCollectionRecord o_mediaCollectionRecord : o_mediaCollectionRecordInstance.getRecords(true)) {
                    if ( (o_mediaCollectionRecord.ColumnDeleted != null) && (o_mediaCollectionRecord.ColumnDeleted.isBefore( java.time.LocalDateTime.now().withNano(0).minusDays(30) )) ) {
                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "delete '" + o_mediaCollectionRecord.ColumnOriginalTitle + "' - deleted: '" + o_mediaCollectionRecord.ColumnDeleted + "'");
                        
                        this.setOtherBaseSource(o_mediaCollectionRecord);
                        o_mediaCollectionRecord.deleteRecord();
                    }
                }

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "iterate 'language' records");

                for (LanguageRecord o_languageRecord : o_languageRecordInstance.getRecords(true)) {
                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "add 'language' record: '" + o_languageRecord.ColumnLanguage + "'");
                    
                    o_jsonMediaCollection.Languages.add(o_languageRecord);
                }     
                
                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "iterate 'mediacollection' records");

                for (MediaCollectionRecord o_mediaCollectionRecord : o_mediaCollectionRecordInstance.getRecords(true)) {
                    String s_posterBytesLength = "0";

                    if (!net.forestany.forestj.lib.Helper.isStringEmpty(o_mediaCollectionRecord.ColumnPoster)) {
                        s_posterBytesLength = o_mediaCollectionRecord.ColumnPoster.length() + "";
                    }
                    
                    o_mediaCollectionRecord.ColumnPoster = s_posterBytesLength;

                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "add 'mediacollection' record: '" + o_mediaCollectionRecord.ColumnUUID + "' - '" + o_mediaCollectionRecord.ColumnOriginalTitle + "'");
                        
                    o_jsonMediaCollection.Records.add(o_mediaCollectionRecord);
                }

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "Encoding data to json string.");

                s_foo = this.o_json.jsonEncode(o_jsonMediaCollection);
                
                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "Encoded data to json string.");
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

                MediaCollectionRecord o_mediaCollectionRecordInstance = new MediaCollectionRecord();
                this.setOtherBaseSource(o_mediaCollectionRecordInstance);

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "find 'mediacollection' record with uuid '" + this.getSeed().getRequestHeader().getParameters().get("uuid") + "'.");

                if (o_mediaCollectionRecordInstance.getOneRecord(java.util.Arrays.asList("UUID"), java.util.Arrays.asList( this.getSeed().getRequestHeader().getParameters().get("uuid") ))) {
                    if (o_mediaCollectionRecordInstance.ColumnPoster != null) {
                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "found '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "', adding poster hex string bytes ('" + (o_mediaCollectionRecordInstance.ColumnPoster.length() / 2) + "') to response");
                        
                        s_foo = o_mediaCollectionRecordInstance.ColumnPoster;
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
                JSONMediaCollection o_jsonMediaCollection = (JSONMediaCollection)o_json.jsonDecode(java.util.Arrays.asList(s_jsonPostData));

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "Received json data decoded. '" + o_jsonMediaCollection.Records.size() + "' records.");

                MediaCollectionRecord o_mediaCollectionRecordInstance = new MediaCollectionRecord();
                this.setOtherBaseSource(o_mediaCollectionRecordInstance);
                
                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "iterate all 'mediacollection' records for deletion check");

                /* delete records with deleted timestamp older than 30 days */
                for (MediaCollectionRecord o_mediaCollectionRecord : o_mediaCollectionRecordInstance.getRecords(true)) {
                    if ( (o_mediaCollectionRecord.ColumnDeleted != null) && (o_mediaCollectionRecord.ColumnDeleted.isBefore( java.time.LocalDateTime.now().withNano(0).minusDays(30) )) ) {
                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "delete '" + o_mediaCollectionRecord.ColumnOriginalTitle + "' - deleted: '" + o_mediaCollectionRecord.ColumnDeleted + "'");
                        
                        this.setOtherBaseSource(o_mediaCollectionRecord);
                        o_mediaCollectionRecord.deleteRecord();
                    }
                }

                /* response list for answer */
                java.util.List<String> a_responseList = new java.util.ArrayList<String>();

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "iterate received 'mediacollection' records");

                /* iterate all sended records */
                if (o_jsonMediaCollection.Records.size() > 0) {
                    for (MediaCollectionRecord jsonMediaCollectionRecord : o_jsonMediaCollection.Records) {
                        if (o_mediaCollectionRecordInstance.getOneRecord(java.util.Arrays.asList("UUID"), java.util.Arrays.asList(jsonMediaCollectionRecord.ColumnUUID))) {
                            if ( (jsonMediaCollectionRecord.ColumnDeleted == null) && (o_mediaCollectionRecordInstance.ColumnDeleted == null) && (jsonMediaCollectionRecord.ColumnLastModified.isAfter(o_mediaCollectionRecordInstance.ColumnLastModified)) ) {
                                /* sended record found, sended deleted is null on both sides, and sended record last modified timestamp is newer -> update all */
                                o_mediaCollectionRecordInstance.ColumnUUID = jsonMediaCollectionRecord.ColumnUUID;
                                o_mediaCollectionRecordInstance.ColumnTitle = jsonMediaCollectionRecord.ColumnTitle;
                                o_mediaCollectionRecordInstance.ColumnType = jsonMediaCollectionRecord.ColumnType;
                                o_mediaCollectionRecordInstance.ColumnPublicationYear = jsonMediaCollectionRecord.ColumnPublicationYear;
                                o_mediaCollectionRecordInstance.ColumnOriginalTitle = jsonMediaCollectionRecord.ColumnOriginalTitle;
                                o_mediaCollectionRecordInstance.ColumnSubType = jsonMediaCollectionRecord.ColumnSubType;
                                o_mediaCollectionRecordInstance.ColumnFiledUnder = jsonMediaCollectionRecord.ColumnFiledUnder;
                                o_mediaCollectionRecordInstance.ColumnLastSeen = jsonMediaCollectionRecord.ColumnLastSeen;
                                o_mediaCollectionRecordInstance.ColumnLengthInMinutes = jsonMediaCollectionRecord.ColumnLengthInMinutes;
                                o_mediaCollectionRecordInstance.ColumnLanguages = jsonMediaCollectionRecord.ColumnLanguages;
                                o_mediaCollectionRecordInstance.ColumnSubtitles = jsonMediaCollectionRecord.ColumnSubtitles;
                                o_mediaCollectionRecordInstance.ColumnDirectors = jsonMediaCollectionRecord.ColumnDirectors;
                                o_mediaCollectionRecordInstance.ColumnScreenwriters = jsonMediaCollectionRecord.ColumnScreenwriters;
                                o_mediaCollectionRecordInstance.ColumnCast = jsonMediaCollectionRecord.ColumnCast;
                                o_mediaCollectionRecordInstance.ColumnSpecialFeatures = jsonMediaCollectionRecord.ColumnSpecialFeatures;
                                o_mediaCollectionRecordInstance.ColumnOther = jsonMediaCollectionRecord.ColumnOther;
                                o_mediaCollectionRecordInstance.ColumnLastModified = jsonMediaCollectionRecord.ColumnLastModified;

                                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "update record '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "' - deleted is null on both sides, and sended record last modified timestamp is newer '" + o_mediaCollectionRecordInstance.ColumnLastModified + "'");

                                try {
                                    if (o_mediaCollectionRecordInstance.updateRecord(true) >= 0) {
                                        a_responseList.add(o_mediaCollectionRecordInstance.ColumnUUID);
                                    }
                                } catch (IllegalStateException o_exc) {
                                    /* catch primary/unique violation and ignore it */
                                    net.forestany.forestj.lib.Global.ilogWarning(this.getSeed().getSalt() + " " + "update record '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "' error: " + o_exc.getMessage());
                                }
                            } else if ( (jsonMediaCollectionRecord.ColumnDeleted != null) && (!jsonMediaCollectionRecord.ColumnDeleted.equals(o_mediaCollectionRecordInstance.ColumnDeleted)) ) {
                                /* sended record found, sended deleted is not null and local is not equal to it -> only update deleted */
                                o_mediaCollectionRecordInstance.ColumnDeleted = jsonMediaCollectionRecord.ColumnDeleted;

                                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "update record '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "' - sended deleted('" + jsonMediaCollectionRecord.ColumnDeleted + "') is not null and local('" + ((o_mediaCollectionRecordInstance.ColumnDeleted != null) ? o_mediaCollectionRecordInstance.ColumnDeleted : "null") + "') is not equal to it");

                                try {
                                    o_mediaCollectionRecordInstance.updateRecord(true);
                                } catch (IllegalStateException o_exc) {
                                    /* catch primary/unique violation and ignore it */
                                    net.forestany.forestj.lib.Global.ilogWarning(this.getSeed().getSalt() + " " + "update record '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "' error: " + o_exc.getMessage());
                                }
                            }
                        } else {
                            if (jsonMediaCollectionRecord.ColumnDeleted == null) {
                                /* sended record not found and deleted is null */
                                try {
                                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "check if record exists with original title and publication year");

                                    /* prepare media collection record instance */
                                    o_mediaCollectionRecordInstance = new MediaCollectionRecord();
                                    this.setOtherBaseSource(o_mediaCollectionRecordInstance);

                                    /* set filters */
                                    o_mediaCollectionRecordInstance.Filters.add(new net.forestany.forestj.lib.sql.Filter("OriginalTitle", jsonMediaCollectionRecord.ColumnOriginalTitle, "=", "AND"));
                                    o_mediaCollectionRecordInstance.Filters.add(new net.forestany.forestj.lib.sql.Filter("PublicationYear", jsonMediaCollectionRecord.ColumnPublicationYear, "=", "AND"));
                                    o_mediaCollectionRecordInstance.Filters.add(new net.forestany.forestj.lib.sql.Filter("Deleted", "NULL", "IS", "AND"));

                                    String s_uuidAppendix = "";

                                    /* check if record exists with original title and publication year */
                                    if (o_mediaCollectionRecordInstance.getRecords().size() > 0) {
                                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "found identical record with '" + jsonMediaCollectionRecord.ColumnOriginalTitle + "' AND '" + jsonMediaCollectionRecord.ColumnPublicationYear + "' AND Deleted IS NULL");
                                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "set deleted timestamp to sended record with '" + jsonMediaCollectionRecord.ColumnOriginalTitle + "' AND '" + jsonMediaCollectionRecord.ColumnPublicationYear + "', because first record on server wins");

                                        /* set deleted column, because the record is already there with same 'original title', 'publication year' and 'deleted'(null) */
                                        /* first record on server wins */
                                        jsonMediaCollectionRecord.ColumnDeleted = java.time.LocalDateTime.now().withNano(0);

                                        net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "deleted timestamp set to '" + jsonMediaCollectionRecord.ColumnDeleted + "'");
                                        
                                        /* return deleted timestamp with uuid so sender can set record to deleted on their side */
                                        java.time.ZonedDateTime o_zonedDatetime = jsonMediaCollectionRecord.ColumnDeleted.atZone(java.time.ZoneId.systemDefault());
		                                String s_deleted = java.time.format.DateTimeFormatter.ISO_INSTANT.format(o_zonedDatetime.withZoneSameInstant(java.time.ZoneId.of("UTC")));
		                                s_uuidAppendix = s_deleted.substring(0, s_deleted.length() - 1);
                                    }

                                    /* insert record and save uuid to send poster data later */
                                    this.setOtherBaseSource(jsonMediaCollectionRecord);

                                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "insert record '" + jsonMediaCollectionRecord.ColumnOriginalTitle + "'");

                                    if (jsonMediaCollectionRecord.insertRecord() > 0) {
                                        /* return uuid for poster sending poster data later */
                                        a_responseList.add(jsonMediaCollectionRecord.ColumnUUID + s_uuidAppendix);
                                    }
                                } catch (IllegalStateException o_exc) {
                                    /* catch primary/unique violation and ignore it */
                                    net.forestany.forestj.lib.Global.ilogWarning(this.getSeed().getSalt() + " " + "insert record '" + jsonMediaCollectionRecord.ColumnOriginalTitle + "' error: " + o_exc.getMessage());
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

                MediaCollectionRecord o_mediaCollectionRecordInstance = new MediaCollectionRecord();
                this.setOtherBaseSource(o_mediaCollectionRecordInstance);

                net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "find 'mediacollection' record with uuid '" + this.getSeed().getPostData().get("uuid") + "'.");

                if (o_mediaCollectionRecordInstance.getOneRecord(java.util.Arrays.asList("UUID"), java.util.Arrays.asList( this.getSeed().getPostData().get("uuid") ))) {
                    net.forestany.forestj.lib.Global.ilog(this.getSeed().getSalt() + " " + "found record '" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + "' for updating poster data (" + (this.getSeed().getPostData().get("posterdata").length() / 2) + " bytes)");
                    
                    o_mediaCollectionRecordInstance.ColumnPoster = this.getSeed().getPostData().get("posterdata");
                    
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
}