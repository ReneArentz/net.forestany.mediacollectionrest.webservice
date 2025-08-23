package net.forestany.mediacollectionrest.webservice;

public class MediaCollectionConfig extends net.forestany.forestj.lib.net.https.Config {

    /* Fields */

    private net.forestany.forestj.lib.sql.BaseJDBC o_base;

    /* Properties */

    public net.forestany.forestj.lib.sql.BaseJDBC getBase() {
        return this.o_base;
    }

    public void setBase(net.forestany.forestj.lib.sql.BaseJDBC p_o_value) throws NullPointerException {
        if (p_o_value == null) {
            throw new NullPointerException("Base object instance is null");
        }

        this.o_base = p_o_value;
    }

    /* Method */

    public MediaCollectionConfig(String p_s_domain) throws IllegalArgumentException {
        this(p_s_domain, net.forestany.forestj.lib.net.https.Mode.NORMAL, net.forestany.forestj.lib.net.sock.recv.ReceiveType.SERVER);
    }

    public MediaCollectionConfig(String p_s_domain, net.forestany.forestj.lib.net.https.Mode p_e_mode) throws IllegalArgumentException {
        this(p_s_domain, p_e_mode, net.forestany.forestj.lib.net.sock.recv.ReceiveType.SERVER);
    }

    public MediaCollectionConfig(String p_s_domain, net.forestany.forestj.lib.net.https.Mode p_e_mode, net.forestany.forestj.lib.net.sock.recv.ReceiveType p_e_receiveType) throws IllegalArgumentException {
        super(p_s_domain, p_e_mode, p_e_receiveType);

        this.o_base = null;
    }
}