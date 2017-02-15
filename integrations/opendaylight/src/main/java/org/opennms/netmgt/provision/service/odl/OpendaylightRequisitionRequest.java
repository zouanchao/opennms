package org.opennms.netmgt.provision.service.odl;

import org.opennms.netmgt.provision.persist.RequisitionRequest;

public class OpendaylightRequisitionRequest implements RequisitionRequest {

    private String foreignSource;
    private String host;
    private int port;

    public String getForeignSource() {
        return foreignSource;
    }
    public void setForeignSource(String foreignSource) {
        this.foreignSource = foreignSource;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    
}
