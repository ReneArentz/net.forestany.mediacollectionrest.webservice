package net.forestany.mediacollectionrest.webservice;

public class Program 
{
    public static void main( String[] args )
    {
        System.out.println("Webservice started . . ." + "\n");
        
        try {
			String s_currentDirectory = net.forestany.forestj.lib.io.File.getCurrentDirectory();
			
			net.forestany.forestj.lib.Global.get().resetLog();
			
			net.forestany.forestj.lib.LoggingConfig o_loggingConfigAll = new net.forestany.forestj.lib.LoggingConfig();
            o_loggingConfigAll.setLevel(java.util.logging.Level.INFO);
			o_loggingConfigAll.setUseConsole(true);
			
			o_loggingConfigAll.setConsoleLevel(java.util.logging.Level.WARNING);
			o_loggingConfigAll.setUseFile(true);
            String s_logPath = "/var/log/mediacollectionrest/";
            if (!net.forestany.forestj.lib.io.File.folderExists(s_logPath)) { net.forestany.forestj.lib.io.File.createDirectory(s_logPath, true); }
            o_loggingConfigAll.setFileLevel(java.util.logging.Level.INFO);
			o_loggingConfigAll.setFilePath(s_logPath);
			o_loggingConfigAll.setFileLimit(10000000); // ~ 10.0 MB
			o_loggingConfigAll.setFileCount(25);
			
			o_loggingConfigAll.loadConfig();

			net.forestany.forestj.lib.Global.get().by_logControl = net.forestany.forestj.lib.Global.OFF;
			net.forestany.forestj.lib.Global.get().by_internalLogControl = net.forestany.forestj.lib.Global.SEVERE + net.forestany.forestj.lib.Global.WARNING;
            net.forestany.forestj.lib.Global.get().by_internalLogControl = net.forestany.forestj.lib.Global.SEVERE + net.forestany.forestj.lib.Global.WARNING + net.forestany.forestj.lib.Global.INFO;
            
            net.forestany.forestj.lib.Global.ilog("++++++++++++++++++++++++++++++++");
            net.forestany.forestj.lib.Global.ilog("+ Media Collection REST 1.0.1  +");
            net.forestany.forestj.lib.Global.ilog("++++++++++++++++++++++++++++++++");
            
            net.forestany.forestj.lib.Global.ilog("");
            net.forestany.forestj.lib.Global.ilog("current directory: " + s_currentDirectory);
            net.forestany.forestj.lib.Global.ilog("");

            runService(s_currentDirectory);
		} catch (Exception o_exc) {
            System.err.println("ERROR: " + o_exc.getMessage());
            net.forestany.forestj.lib.Global.logException(o_exc);
		}
		
		System.out.println("\n" + " . . . Webservice finished");
    }

    public static void runService(String p_s_currentDirectory) throws Exception {
        Config o_config = new Config(p_s_currentDirectory + net.forestany.forestj.lib.io.File.DIR + "config.txt");
        o_config.resourcesPath = o_config.resourcesPath.replace("./", p_s_currentDirectory + net.forestany.forestj.lib.io.File.DIR);
        o_config.sqliteFilePath = o_config.sqliteFilePath.replace("./", p_s_currentDirectory + net.forestany.forestj.lib.io.File.DIR);
		String s_rootDirectory = o_config.resourcesPath + "restserver" + net.forestany.forestj.lib.io.File.DIR;
        
        String s_host = o_config.serverIp;
        int i_port = o_config.serverPort;

        checkSqlite(o_config.sqliteFilePath);
        //fillSqlite(o_config.sqliteFilePath, o_config.resourcesPath + "JSONMediaCollection.json");

        /* SERVER */

        net.forestany.forestj.lib.Cryptography o_cryptography = new net.forestany.forestj.lib.Cryptography(o_config.commonSecretPassphrase, net.forestany.forestj.lib.Cryptography.KEY256BIT);
        
        byte[] a_encrypted = o_cryptography.encrypt(o_config.authPassphrase.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        net.forestany.forestj.lib.Global.ilog(new String(java.util.Base64.getEncoder().encode(a_encrypted), java.nio.charset.StandardCharsets.UTF_8));

        MediaCollectionConfig o_serverConfig = new MediaCollectionConfig("https://" + s_host, net.forestany.forestj.lib.net.https.Mode.REST, net.forestany.forestj.lib.net.sock.recv.ReceiveType.SERVER);
        o_serverConfig.setHost(s_host);
        o_serverConfig.setPort(i_port);
        o_serverConfig.setRootDirectory(s_rootDirectory);
        o_serverConfig.setNotUsingCookies(true);
        //o_serverConfig.setAllowSourceList( java.util.Arrays.asList( s_host.substring(0, s_host.lastIndexOf(".")) + ".1/24" ) );
        //o_serverConfig.setSessionDirectory(s_sessionDirectory);
        //o_serverConfig.setSessionMaxAge(new net.forestany.forestj.lib.DateInterval("PT30M"));
        //o_serverConfig.setSessionRefresh(true);
        o_serverConfig.setForestREST(new MediaCollectionRest(o_config.resourcesPath + "JSONMediaCollectionSmall.json", o_config.resourcesPath + "JSONMediaCollectionRecord.json", o_config.resourcesPath + "JSONMediaCollection.json", o_cryptography, o_config.authUser, o_config.authPassphrase));

        o_serverConfig.setBase(
            new net.forestany.forestj.lib.sql.sqlite.BaseSQLite(o_config.sqliteFilePath)
        );

        net.forestany.forestj.lib.net.sock.task.recv.https.TinyHttpsServer<javax.net.ssl.SSLServerSocket> o_serverTask = new net.forestany.forestj.lib.net.sock.task.recv.https.TinyHttpsServer<javax.net.ssl.SSLServerSocket>( o_serverConfig );
        net.forestany.forestj.lib.net.sock.recv.ReceiveTCP<javax.net.ssl.SSLServerSocket> o_socketReceive = new net.forestany.forestj.lib.net.sock.recv.ReceiveTCP<javax.net.ssl.SSLServerSocket>(
            javax.net.ssl.SSLServerSocket.class,				/* class type */
            net.forestany.forestj.lib.net.sock.recv.ReceiveType.SERVER,		/* socket type */
            s_host,															/* receiving address */
            i_port,															/* receiving port */
            o_serverTask,													/* server task */
            30000,									/* timeout milliseconds */
            -1,																/* max. number of executions */
            o_config.receiveBufferSize,								        /* receive buffer size */
            net.forestany.forestj.lib.Cryptography.createSSLContextWithOneCertificate(o_config.resourcesPath + "certificates" + net.forestany.forestj.lib.io.File.DIR + o_config.keystoreFile, o_config.keystorePass, o_config.certificateAlias)	/* ssl context */
        );
        o_socketReceive.setExecutorServicePoolAmount(10);
        Thread o_threadServer = new Thread(o_socketReceive);

        /* START SERVER + BASE POOL */

        o_threadServer.start();

        net.forestany.forestj.lib.Global.ilog("Server started with '" + s_host + ":" + i_port + "' and background base SQLite");
        net.forestany.forestj.lib.Global.ilog("You can access the site with a browser. Keep in mind it will be an unknown certificate.");
        net.forestany.forestj.lib.Global.ilog("Try 'https://"+ s_host + ":" + i_port + "'");

        // only for manual tests
        //net.forestany.forestj.lib.Console.consoleInputString("Please enter any key to stop tiny https server . . . ", true);

        /* use count down latch to block main until shutdown */
        java.util.concurrent.CountDownLatch o_countDownLatch = new java.util.concurrent.CountDownLatch(1);

        /* add shutdown hook for systemd (SIGTERM, SIGINT) */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                /* ------------------------------------------ */
                /* Stop SERVER + BASE POOL                    */
                /* ------------------------------------------ */

                if (o_socketReceive != null) {
                    o_socketReceive.stop();
                }

                if (o_serverConfig.getBase() != null) {
                    o_serverConfig.getBase().closeConnection();
                }
            } catch (Exception e) {
                net.forestany.forestj.lib.Global.logException(e);
                Thread.currentThread().interrupt();
            }

            net.forestany.forestj.lib.Global.ilog("Server stopped");

            /* wait 6 seconds to clean all up */
            try {
                Thread.sleep(6_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt flag
            }

            /* initiate service shutdown */
            o_countDownLatch.countDown();
        }));

        /* await shutdown */
        o_countDownLatch.await();
    }

	public static void checkSqlite(String p_s_sqliteFilePath) throws Exception {
        net.forestany.forestj.lib.Global o_glob = net.forestany.forestj.lib.Global.get();
        o_glob.BaseGateway = net.forestany.forestj.lib.sqlcore.BaseGateway.SQLITE;
        o_glob.Base = new net.forestany.forestj.lib.sql.sqlite.BaseSQLite(p_s_sqliteFilePath);

        try {
            MediaCollectionRecord o_record = new MediaCollectionRecord();
            @SuppressWarnings("unused")
            java.util.List<MediaCollectionRecord> a_records = o_record.getRecords();
        } catch (Exception o_exc) {
            net.forestany.forestj.lib.Global.ilog("Could not query 'mediacollection'; creating table . . .");

            /* #### CREATE ############################################################################################# */
            net.forestany.forestj.lib.sql.Query<net.forestany.forestj.lib.sql.Create> o_queryCreate = new net.forestany.forestj.lib.sql.Query<net.forestany.forestj.lib.sql.Create>(o_glob.BaseGateway, net.forestany.forestj.lib.sqlcore.SqlType.CREATE, "mediacollection");
            /* #### Columns ############################################################################################ */
            java.util.List<java.util.Properties> a_columnsDefinition = new java.util.ArrayList<java.util.Properties>();

            java.util.Properties o_properties = new java.util.Properties();
            o_properties.put("name", "Id");
            o_properties.put("columnType", "integer [int]");
            o_properties.put("constraints", "NOT NULL;PRIMARY KEY;AUTO_INCREMENT");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "UUID");
            o_properties.put("columnType", "text [36]");
            o_properties.put("constraints", "NOT NULL;UNIQUE");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "Title");
            o_properties.put("columnType", "text [255]");
            o_properties.put("constraints", "NOT NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "Type");
            o_properties.put("columnType", "text [36]");
            o_properties.put("constraints", "NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "PublicationYear");
            o_properties.put("columnType", "integer [small]");
            o_properties.put("constraints", "NOT NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "OriginalTitle");
            o_properties.put("columnType", "text [255]");
            o_properties.put("constraints", "NOT NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "SubType");
            o_properties.put("columnType", "text [36]");
            o_properties.put("constraints", "NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "FiledUnder");
            o_properties.put("columnType", "text [36]");
            o_properties.put("constraints", "NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "LastSeen");
            o_properties.put("columnType", "datetime");
            o_properties.put("constraints", "NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "LengthInMinutes");
            o_properties.put("columnType", "integer [small]");
            o_properties.put("constraints", "NOT NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "Languages");
            o_properties.put("columnType", "text [255]");
            o_properties.put("constraints", "NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "Subtitles");
            o_properties.put("columnType", "text [255]");
            o_properties.put("constraints", "NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "Directors");
            o_properties.put("columnType", "text [255]");
            o_properties.put("constraints", "NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "Screenwriters");
            o_properties.put("columnType", "text [255]");
            o_properties.put("constraints", "NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "Cast");
            o_properties.put("columnType", "text [255]");
            o_properties.put("constraints", "NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "SpecialFeatures");
            o_properties.put("columnType", "text");
            o_properties.put("constraints", "NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "Other");
            o_properties.put("columnType", "text");
            o_properties.put("constraints", "NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "LastModified");
            o_properties.put("columnType", "datetime");
            o_properties.put("constraints", "NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "Deleted");
            o_properties.put("columnType", "datetime");
            o_properties.put("constraints", "NULL");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "Poster");
            o_properties.put("columnType", "text");
            o_properties.put("constraints", "NULL");
            a_columnsDefinition.add(o_properties);

            /* #### Query ############################################################################ */

            for (java.util.Properties o_columnDefinition : a_columnsDefinition) {
                net.forestany.forestj.lib.sql.ColumnStructure o_column = new net.forestany.forestj.lib.sql.ColumnStructure(o_queryCreate);
                o_column.columnTypeAllocation(o_columnDefinition.getProperty("columnType"));
                o_column.s_name = o_columnDefinition.getProperty("name");
                o_column.setAlterOperation("ADD");

                if (o_columnDefinition.containsKey("constraints")) {
                    String[] a_constraints = o_columnDefinition.getProperty("constraints").split(";");

                    for (int i = 0; i < a_constraints.length; i++) {
                        o_column.addConstraint(o_queryCreate.constraintTypeAllocation(a_constraints[i]));

                        if ( (a_constraints[i].compareTo("DEFAULT") == 0) && (o_columnDefinition.containsKey("constraintDefaultValue")) ) {
                            o_column.setConstraintDefaultValue((Object)o_columnDefinition.getProperty("constraintDefaultValue"));
                        }
                    }
                }

                o_queryCreate.getQuery().a_columns.add(o_column);
            }

            java.util.List<java.util.LinkedHashMap<String, Object>> a_result = o_glob.Base.fetchQuery(o_queryCreate);

            /* check table has been created */

            if (a_result.size() != 1) {
                throw new Exception("Result row amount of create query is not '1', it is '" + a_result.size() + "'" );
            }

            /* #### ALTER ########################################################################################### */
            //net.forestany.forestj.lib.sql.Query<net.forestany.forestj.lib.sql.Alter> o_queryAlter = new net.forestany.forestj.lib.sql.Query<net.forestany.forestj.lib.sql.Alter>(o_glob.BaseGateway, net.forestany.forestj.lib.sqlcore.SqlType.ALTER, "mediacollection");
            /* #### Constraints ##################################################################################### */
            //net.forestany.forestj.lib.sql.Constraint o_constraint = new net.forestany.forestj.lib.sql.Constraint(o_queryAlter, "UNIQUE", "mediacollection_unique", "", "ADD");
            //    o_constraint.a_columns.add("OriginalTitle");
            //    o_constraint.a_columns.add("PublicationYear");
            //    o_constraint.a_columns.add("Deleted");
                
            //o_queryAlter.getQuery().a_constraints.add(o_constraint);
            
            //a_result = o_glob.Base.fetchQuery(o_queryAlter);

            /* check unique constraint has been created */

            //if (a_result.size() != 1) {
            //    throw new Exception("Result row amount of alter query is not '1', it is '" + a_result.size() + "'" );
            //}

            net.forestany.forestj.lib.Global.ilog("Table 'mediacollection' created.");
        }

        /* ########################################################################################################## */

        try {
            LanguageRecord o_language = new LanguageRecord();
            @SuppressWarnings("unused")
            java.util.List<LanguageRecord> a_languages = o_language.getRecords();
        } catch (Exception o_exc) {
            net.forestany.forestj.lib.Global.ilog("Could not query 'languages'; creating table . . .");

            /* #### CREATE ############################################################################################# */
            net.forestany.forestj.lib.sql.Query<net.forestany.forestj.lib.sql.Create> o_queryCreate = new net.forestany.forestj.lib.sql.Query<net.forestany.forestj.lib.sql.Create>(o_glob.BaseGateway, net.forestany.forestj.lib.sqlcore.SqlType.CREATE, "languages");
            /* #### Columns ############################################################################################ */
            java.util.List<java.util.Properties> a_columnsDefinition = new java.util.ArrayList<java.util.Properties>();

            java.util.Properties o_properties = new java.util.Properties();
            o_properties.put("name", "Id");
            o_properties.put("columnType", "integer [int]");
            o_properties.put("constraints", "NOT NULL;PRIMARY KEY;AUTO_INCREMENT");
            a_columnsDefinition.add(o_properties);

            o_properties = new java.util.Properties();
            o_properties.put("name", "Language");
            o_properties.put("columnType", "text [255]");
            o_properties.put("constraints", "NOT NULL;UNIQUE");
            a_columnsDefinition.add(o_properties);

            /* #### Query ############################################################################ */

            for (java.util.Properties o_columnDefinition : a_columnsDefinition) {
                net.forestany.forestj.lib.sql.ColumnStructure o_column = new net.forestany.forestj.lib.sql.ColumnStructure(o_queryCreate);
                o_column.columnTypeAllocation(o_columnDefinition.getProperty("columnType"));
                o_column.s_name = o_columnDefinition.getProperty("name");
                o_column.setAlterOperation("ADD");

                if (o_columnDefinition.containsKey("constraints")) {
                    String[] a_constraints = o_columnDefinition.getProperty("constraints").split(";");

                    for (int i = 0; i < a_constraints.length; i++) {
                        o_column.addConstraint(o_queryCreate.constraintTypeAllocation(a_constraints[i]));

                        if ( (a_constraints[i].compareTo("DEFAULT") == 0) && (o_columnDefinition.containsKey("constraintDefaultValue")) ) {
                            o_column.setConstraintDefaultValue((Object)o_columnDefinition.getProperty("constraintDefaultValue"));
                        }
                    }
                }

                o_queryCreate.getQuery().a_columns.add(o_column);
            }

            java.util.List<java.util.LinkedHashMap<String, Object>> a_result = o_glob.Base.fetchQuery(o_queryCreate);

            /* check table has been created */

            if (a_result.size() != 1) {
                throw new Exception("Result row amount of create query is not '1', it is '" + a_result.size() + "'" );
            }

            net.forestany.forestj.lib.Global.ilog("Table 'languages' created.");

            LanguageRecord o_languageRecord = new LanguageRecord();

            o_languageRecord.ColumnLanguage = "German";
            if (o_languageRecord.insertRecord() <= 0) { System.err.println("Result of insert record is lower equal '0'"); }

            o_languageRecord.ColumnLanguage = "English";
            if (o_languageRecord.insertRecord() <= 0) { System.err.println("Result of insert record is lower equal '0'"); }

            o_languageRecord.ColumnLanguage = "Japanese";
            if (o_languageRecord.insertRecord() <= 0) { System.err.println("Result of insert record is lower equal '0'"); }

            o_languageRecord.ColumnLanguage = "French";
            if (o_languageRecord.insertRecord() <= 0) { System.err.println("Result of insert record is lower equal '0'"); }

            o_languageRecord.ColumnLanguage = "Spanish";
            if (o_languageRecord.insertRecord() <= 0) { System.err.println("Result of insert record is lower equal '0'"); }

            o_languageRecord.ColumnLanguage = "Korean";
            if (o_languageRecord.insertRecord() <= 0) { System.err.println("Result of insert record is lower equal '0'"); }

            o_languageRecord.ColumnLanguage = "Italian";
            if (o_languageRecord.insertRecord() <= 0) { System.err.println("Result of insert record is lower equal '0'"); }

            o_languageRecord.ColumnLanguage = "Hindi";
            if (o_languageRecord.insertRecord() <= 0) { System.err.println("Result of insert record is lower equal '0'"); }

            o_languageRecord.ColumnLanguage = "Russian";
            if (o_languageRecord.insertRecord() <= 0) { System.err.println("Result of insert record is lower equal '0'"); }
            
            o_languageRecord.ColumnLanguage = "Swedish";
            if (o_languageRecord.insertRecord() <= 0) { System.err.println("Result of insert record is lower equal '0'"); }

            o_languageRecord.ColumnLanguage = "Thai";
            if (o_languageRecord.insertRecord() <= 0) { System.err.println("Result of insert record is lower equal '0'"); }

            net.forestany.forestj.lib.Global.ilog("Table 'languages' filled with standard languages.");
        }

        o_glob.Base.closeConnection();
    }

	public static void fillSqlite(String p_s_sqliteFilePath, String p_s_jsonSchemaFile) throws Exception {
        net.forestany.forestj.lib.Global.ilog("Open sqlite db (" + p_s_sqliteFilePath + ").");

        net.forestany.forestj.lib.Global o_glob = net.forestany.forestj.lib.Global.get();
        o_glob.BaseGateway = net.forestany.forestj.lib.sqlcore.BaseGateway.SQLITE;
        o_glob.Base = new net.forestany.forestj.lib.sql.sqlite.BaseSQLite(p_s_sqliteFilePath);

        String s_jsonFile = p_s_sqliteFilePath.substring(0, p_s_sqliteFilePath.lastIndexOf("/")) + net.forestany.forestj.lib.io.File.DIR + "bkp.json";
        String s_posterFile = p_s_sqliteFilePath.substring(0, p_s_sqliteFilePath.lastIndexOf("/")) + net.forestany.forestj.lib.io.File.DIR + "bkp_poster.txt";

        net.forestany.forestj.lib.Global.ilog("Decoding json file for filling sqlite db.");

        net.forestany.forestj.lib.io.JSON o_json = new net.forestany.forestj.lib.io.JSON(p_s_jsonSchemaFile);
        JSONMediaCollection o_jsonMediaCollection = (JSONMediaCollection)o_json.jsonDecode(s_jsonFile);

        net.forestany.forestj.lib.Global.ilog("JSON file decoded. '" + o_jsonMediaCollection.Records.size() + "' records.");

        MediaCollectionRecord o_mediaCollectionRecordInstance = new MediaCollectionRecord();
		LanguageRecord o_languageRecordInstance = new LanguageRecord();

        net.forestany.forestj.lib.Global.ilog("Deleting all data in 'mediacollection' table.");

        if (o_mediaCollectionRecordInstance.truncateTable() < 0) {
            throw new Exception("Could not truncate media collection");
        }

        net.forestany.forestj.lib.Global.ilog("Deleting all data in 'languages' table.");

		if (o_languageRecordInstance.truncateTable() < 0) {
            throw new Exception("Could not truncate media collection languages");
        }

		if (o_jsonMediaCollection.Languages.size() > 0) {
            net.forestany.forestj.lib.Global.ilog("Iterate all 'language' records (" + o_jsonMediaCollection.Languages.size() + ").");

            for (LanguageRecord jsonLanguageRecord : o_jsonMediaCollection.Languages) {
                net.forestany.forestj.lib.Global.ilog("Insert 'language' record (" + jsonLanguageRecord.ColumnLanguage + ").");

				if (jsonLanguageRecord.insertRecord() < 0) {
                    throw new Exception("Could not create language with '" + jsonLanguageRecord.ColumnLanguage + "'.");
                }
            }
        }

        if (o_jsonMediaCollection.Records.size() > 0) {
            net.forestany.forestj.lib.Global.ilog("Iterate all 'mediacollection' records (" + o_jsonMediaCollection.Records.size() + ").");

            for (MediaCollectionRecord jsonMediaCollectionRecord : o_jsonMediaCollection.Records) {
                net.forestany.forestj.lib.Global.ilog("Insert 'mediacollection' record (" + jsonMediaCollectionRecord.ColumnOriginalTitle + ").");

				if (jsonMediaCollectionRecord.insertRecord() < 0) {
                    throw new Exception("Could not create media collection item with '" + jsonMediaCollectionRecord.ColumnOriginalTitle + "'.");
                }
            }
        }

        net.forestany.forestj.lib.Global.ilog("Open file for poster data.");

        net.forestany.forestj.lib.io.File o_posterFile = new net.forestany.forestj.lib.io.File(s_posterFile);
        
        if (o_posterFile.getFileLines() > 0) {
            net.forestany.forestj.lib.Global.ilog("Iterate all 'poster' file lines (" + o_posterFile.getFileLines() + ").");

            for (String s_line : o_posterFile.getFileContentAsList()) {
                if (s_line.length() > 100) {
                    String s_uuid = s_line.substring(0, 36);
                    o_mediaCollectionRecordInstance = new MediaCollectionRecord();

                    net.forestany.forestj.lib.Global.ilog("Get 'mediacollection' record with uuid (" + s_uuid + ").");

                    if (o_mediaCollectionRecordInstance.getOneRecord(java.util.Arrays.asList("UUID"), java.util.Arrays.asList(s_uuid))) {
                        net.forestany.forestj.lib.Global.ilog("Update poster data in 'mediacollection' record (" + o_mediaCollectionRecordInstance.ColumnOriginalTitle + ").");

                        o_mediaCollectionRecordInstance.ColumnPoster = s_line.substring(36);
                        o_mediaCollectionRecordInstance.updateRecord();
                    }
                }
            }
        }

        o_glob.Base.closeConnection();

        net.forestany.forestj.lib.Global.ilog("sqlite db closed.");
    }
}
