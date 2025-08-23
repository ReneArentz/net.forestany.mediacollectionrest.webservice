module net.forestany.mediacollectionrest.webservice {
	requires transitive net.forestany.forestj.lib.net;
    requires transitive net.forestany.forestj.lib.sql.sqlite;
	
	opens net.forestany.mediacollectionrest.webservice to net.forestany.forestj, net.forestany.forestj.lib.sql;
}