package net.forestany.mediacollectionrest.webservice;

public class Config {
    public String serverIp;
    public int serverPort;
    public int receiveBufferSize;
    public String resourcesPath;
    public String keystoreFile;
    public String keystorePass;
    public String certificateAlias;
    public String commonSecretPassphrase;
    public String sqliteFilePath;
    public String authUser;
    public String authPassphrase;
    
    public Config(String p_s_pathToConfigTxt) throws Exception {
        if (!net.forestany.forestj.lib.io.File.exists(p_s_pathToConfigTxt)) {
            throw new Exception("file[" + p_s_pathToConfigTxt + "] does not exists");
        }

        net.forestany.forestj.lib.io.File o_configFile = new net.forestany.forestj.lib.io.File(p_s_pathToConfigTxt, false);

        if (o_configFile.getFileLines() != 11) {
            throw new Exception("invalid config file[" + p_s_pathToConfigTxt + "]; must have '11 lines', but has '" + o_configFile.getFileLines() + " lines'");
        }

        for (int i = 1; i <= o_configFile.getFileLines(); i++) {
            String s_line = o_configFile.readLine(i);

            if (i == 1) {
                if (!s_line.startsWith("serverIp")) {
                    throw new Exception("Line #" + i + " does not start with 'serverIp'");
                }

                String[] a_split = s_line.split("=");

                if (a_split.length != 2) {
                    throw new Exception("Invalid key value pair for 'serverIp': '" + s_line + "'");
                }

                if (net.forestany.forestj.lib.Helper.isStringEmpty(a_split[1].trim())) {
                    throw new Exception("Invalid empty value, for 'serverIp'");
                }

                this.serverIp = a_split[1].trim();
            } else if (i == 2) {
                if (!s_line.startsWith("serverPort")) {
                    throw new Exception("Line #" + i + " does not start with 'serverPort'");
                }

                String[] a_split = s_line.split("=");

                if (a_split.length != 2) {
                    throw new Exception("Invalid key value pair for 'serverPort': '" + s_line + "'");
                }

                if (!net.forestany.forestj.lib.Helper.isInteger(a_split[1].trim())) {
                    throw new Exception("Invalid value for 'serverPort': '" + a_split[1].trim() + "' is not an integer");
                }

                this.serverPort = Integer.parseInt(a_split[1].trim());
            } else if (i == 3) {
                if (!s_line.startsWith("receiveBufferSize")) {
                    throw new Exception("Line #" + i + " does not start with 'receiveBufferSize'");
                }

                String[] a_split = s_line.split("=");

                if (a_split.length != 2) {
                    throw new Exception("Invalid key value pair for 'receiveBufferSize': '" + s_line + "'");
                }

                if (!net.forestany.forestj.lib.Helper.isInteger(a_split[1].trim())) {
                    throw new Exception("Invalid value for 'receiveBufferSize': '" + a_split[1].trim() + "' is not an integer");
                }

                this.receiveBufferSize = Integer.parseInt(a_split[1].trim());
            
            } else if (i == 4) {
                if (!s_line.startsWith("resourcesPath")) {
                    throw new Exception("Line #" + i + " does not start with 'resourcesPath'");
                }

                String[] a_split = s_line.split("=");

                if (a_split.length != 2) {
                    throw new Exception("Invalid key value pair for 'resourcesPath': '" + s_line + "'");
                }

                if (net.forestany.forestj.lib.Helper.isStringEmpty(a_split[1].trim())) {
                    throw new Exception("Invalid empty value, for 'resourcesPath'");
                }

                this.resourcesPath = a_split[1].trim();
            } else if (i == 5) {
                if (!s_line.startsWith("keystoreFile")) {
                    throw new Exception("Line #" + i + " does not start with 'keystoreFile'");
                }

                String[] a_split = s_line.split("=");

                if (a_split.length != 2) {
                    throw new Exception("Invalid key value pair for 'keystoreFile': '" + s_line + "'");
                }

                if (net.forestany.forestj.lib.Helper.isStringEmpty(a_split[1].trim())) {
                    throw new Exception("Invalid empty value, for 'keystoreFile'");
                }

                this.keystoreFile = a_split[1].trim();
            } else if (i == 6) {
                if (!s_line.startsWith("keystorePass")) {
                    throw new Exception("Line #" + i + " does not start with 'keystorePass'");
                }

                String[] a_split = s_line.split("=");

                if (a_split.length != 2) {
                    throw new Exception("Invalid key value pair for 'keystorePass': '" + s_line + "'");
                }

                if (net.forestany.forestj.lib.Helper.isStringEmpty(a_split[1].trim())) {
                    throw new Exception("Invalid empty value, for 'keystorePass'");
                }

                this.keystorePass = a_split[1].trim();
            } else if (i == 7) {
                if (!s_line.startsWith("certificateAlias")) {
                    throw new Exception("Line #" + i + " does not start with 'certificateAlias'");
                }

                String[] a_split = s_line.split("=");

                if (a_split.length != 2) {
                    throw new Exception("Invalid key value pair for 'certificateAlias': '" + s_line + "'");
                }

                if (net.forestany.forestj.lib.Helper.isStringEmpty(a_split[1].trim())) {
                    throw new Exception("Invalid empty value, for 'certificateAlias'");
                }

                this.certificateAlias = a_split[1].trim();
            } else if (i == 8) {
                if (!s_line.startsWith("commonSecretPassphrase")) {
                    throw new Exception("Line #" + i + " does not start with 'commonSecretPassphrase'");
                }

                String[] a_split = s_line.split("=");

                if (a_split.length != 2) {
                    throw new Exception("Invalid key value pair for 'commonSecretPassphrase': '" + s_line + "'");
                }

                if (net.forestany.forestj.lib.Helper.isStringEmpty(a_split[1].trim())) {
                    throw new Exception("Invalid empty value, for 'commonSecretPassphrase'");
                }

                this.commonSecretPassphrase = a_split[1].trim();
            } else if (i == 9) {
                if (!s_line.startsWith("sqliteFilePath")) {
                    throw new Exception("Line #" + i + " does not start with 'sqliteFilePath'");
                }

                String[] a_split = s_line.split("=");

                if (a_split.length != 2) {
                    throw new Exception("Invalid key value pair for 'sqliteFilePath': '" + s_line + "'");
                }

                if (net.forestany.forestj.lib.Helper.isStringEmpty(a_split[1].trim())) {
                    throw new Exception("Invalid empty value, for 'sqliteFilePath'");
                }

                this.sqliteFilePath = a_split[1].trim();
            } else if (i == 10) {
                if (!s_line.startsWith("authUser")) {
                    throw new Exception("Line #" + i + " does not start with 'authUser'");
                }

                String[] a_split = s_line.split("=");

                if (a_split.length != 2) {
                    throw new Exception("Invalid key value pair for 'authUser': '" + s_line + "'");
                }

                if (net.forestany.forestj.lib.Helper.isStringEmpty(a_split[1].trim())) {
                    throw new Exception("Invalid empty value, for 'authUser'");
                }

                this.authUser = a_split[1].trim();
            } else if (i == 11) {
                if (!s_line.startsWith("authPassphrase")) {
                    throw new Exception("Line #" + i + " does not start with 'authPassphrase'");
                }

                String[] a_split = s_line.split("=");

                if (a_split.length != 2) {
                    throw new Exception("Invalid key value pair for 'authPassphrase': '" + s_line + "'");
                }

                if (net.forestany.forestj.lib.Helper.isStringEmpty(a_split[1].trim())) {
                    throw new Exception("Invalid empty value, for 'authPassphrase'");
                }

                this.authPassphrase = a_split[1].trim();
            }
        }

        net.forestany.forestj.lib.Global.ilog("++++++++++++++++++++++++++++++++");
        net.forestany.forestj.lib.Global.ilog("+ Media Collection REST Config +");
        net.forestany.forestj.lib.Global.ilog("++++++++++++++++++++++++++++++++");

        net.forestany.forestj.lib.Global.ilog("");

        net.forestany.forestj.lib.Global.ilog("server ip" + "\t\t\t" + this.serverIp);
        net.forestany.forestj.lib.Global.ilog("server port" + "\t\t\t" + this.serverPort);
        net.forestany.forestj.lib.Global.ilog("receive buffer size" + "\t\t" + this.receiveBufferSize);
        net.forestany.forestj.lib.Global.ilog("resources path" + "\t\t\t" + this.resourcesPath);
        net.forestany.forestj.lib.Global.ilog("keystore file" + "\t\t\t" + this.keystoreFile);
        net.forestany.forestj.lib.Global.ilog("keystore password" + "\t\t" + net.forestany.forestj.lib.Helper.disguiseSubstring("_" + this.keystorePass + "_", "_", "_", '*').substring(1, this.keystorePass.length() - 1));
        net.forestany.forestj.lib.Global.ilog("certificate alias" + "\t\t" + this.certificateAlias);
        net.forestany.forestj.lib.Global.ilog("common secret passphrase" + "\t\t" + net.forestany.forestj.lib.Helper.disguiseSubstring("_" + this.commonSecretPassphrase + "_", "_", "_", '*').substring(1, this.commonSecretPassphrase.length() - 1));
        net.forestany.forestj.lib.Global.ilog("sqllite file path" + "\t\t" + this.sqliteFilePath);
        net.forestany.forestj.lib.Global.ilog("auth user" + "\t\t\t" + this.authUser);
        net.forestany.forestj.lib.Global.ilog("auth passphrase" + "\t\t\t" + net.forestany.forestj.lib.Helper.disguiseSubstring("_" + this.authPassphrase + "_", "_", "_", '*').substring(1, this.authPassphrase.length() - 1));

        net.forestany.forestj.lib.Global.ilog("");
    }
}
