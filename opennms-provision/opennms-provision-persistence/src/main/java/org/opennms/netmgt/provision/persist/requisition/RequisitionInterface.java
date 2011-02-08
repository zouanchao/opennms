//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.3-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.01.29 at 01:15:48 PM EST 
//


package org.opennms.netmgt.provision.persist.requisition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * <p>RequisitionInterface class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="", propOrder = { "m_monitoredServices", "m_categories" })
@XmlRootElement(name = "interface")
public class RequisitionInterface implements Comparable<RequisitionInterface> {

    //TODO Change these to be sets so that we don't have to verify duplicates in the lists
    
    @XmlElement(name="monitored-service")
    protected List<RequisitionMonitoredService> m_monitoredServices = new ArrayList<RequisitionMonitoredService>();

    @XmlElement(name="category")
    protected List<RequisitionCategory> m_categories = new ArrayList<RequisitionCategory>();

    @XmlAttribute(name="descr")
    protected String m_description;
    
    @XmlAttribute(name="ip-addr", required=true)
    protected String m_ipAddress;
    
    @XmlAttribute(name="managed")
    protected Boolean m_isManaged;
    
    @XmlAttribute(name="snmp-primary")
    protected String m_snmpPrimary;
    
    @XmlAttribute(name="status")
    protected Integer m_status;

    /**
     * <p>getMonitoredServiceCount</p>
     *
     * @return a int.
     */
    @XmlTransient
    public int getMonitoredServiceCount() {
        return (m_monitoredServices == null) ? 0 : m_monitoredServices.size();
    }

    /* backwards-compat with ModelImport */
    /**
     * <p>getMonitoredService</p>
     *
     * @return an array of {@link org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService} objects.
     */
    @XmlTransient
    public RequisitionMonitoredService[] getMonitoredService() {
        return getMonitoredServices().toArray(new RequisitionMonitoredService[] {});
    }

    /**
     * <p>getMonitoredServices</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<RequisitionMonitoredService> getMonitoredServices() {
        if (m_monitoredServices == null) {
            m_monitoredServices = new ArrayList<RequisitionMonitoredService>();
        }
        return m_monitoredServices;
    }

    /**
     * <p>setMonitoredServices</p>
     *
     * @param services a {@link java.util.List} object.
     */
    public void setMonitoredServices(List<RequisitionMonitoredService> services) {
        m_monitoredServices = services;
    }

    /**
     * <p>getMonitoredService</p>
     *
     * @param service a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService} object.
     */
    public RequisitionMonitoredService getMonitoredService(String service) {
        if (m_monitoredServices != null) {
            for (RequisitionMonitoredService svc : m_monitoredServices) {
                if (svc.getServiceName().equals(service)) {
                    return svc;
                }
            }
            
        }
        return null;
    }

    /**
     * <p>removeMonitoredService</p>
     *
     * @param service a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService} object.
     */
    public void removeMonitoredService(RequisitionMonitoredService service) {
        if (m_monitoredServices != null) {
            Iterator<RequisitionMonitoredService> i = m_monitoredServices.iterator();
            while (i.hasNext()) {
                RequisitionMonitoredService svc = i.next();
                if (svc.getServiceName().equals(service.getServiceName())) {
                    i.remove();
                    break;
                }
            }
        }
    }

    /**
     * <p>deleteMonitoredService</p>
     *
     * @param service a {@link java.lang.String} object.
     */
    public void deleteMonitoredService(String service) {
        if (m_monitoredServices != null) {
            Iterator<RequisitionMonitoredService> i = m_monitoredServices.iterator();
            while (i.hasNext()) {
                RequisitionMonitoredService svc = i.next();
                if (svc.getServiceName().equals(service)) {
                    i.remove();
                    break;
                }
            }
        }
    }

    /**
     * <p>insertMonitoredService</p>
     *
     * @param service a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService} object.
     */
    public void insertMonitoredService(RequisitionMonitoredService service) {
        Iterator<RequisitionMonitoredService> iterator = m_monitoredServices.iterator();
        while (iterator.hasNext()) {
            RequisitionMonitoredService existingService = iterator.next();
            if (existingService.getServiceName().equals(service.getServiceName())) {
                iterator.remove();
            }
        }
        m_monitoredServices.add(0, service);
    }

    /**
     * <p>putMonitoredService</p>
     *
     * @param service a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService} object.
     */
    public void putMonitoredService(RequisitionMonitoredService service) {
        Iterator<RequisitionMonitoredService> iterator = m_monitoredServices.iterator();
        while (iterator.hasNext()) {
            RequisitionMonitoredService existingService = iterator.next();
            if (existingService.getServiceName().equals(service.getServiceName())) {
                iterator.remove();
            }
        }
        m_monitoredServices.add(service);
    }

    /**
     * <p>getCategories</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<RequisitionCategory> getCategories() {
        if (m_categories == null) {
            m_categories = new ArrayList<RequisitionCategory>();
        }
        return m_categories;
    }
    
    /**
     * <p>setCategories</p>
     *
     * @param categories a {@link java.util.List} object.
     */
    public void setCategories(List<RequisitionCategory> categories) {
        m_categories = categories;
    }

    /**
     * <p>getCategory</p>
     *
     * @param category a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     */
    public RequisitionCategory getCategory(String category) {
        if (m_categories != null) {
            for (RequisitionCategory cat : m_categories) {
                if (cat.getName().equals(category)) {
                    return cat;
                }
            }
            
        }
        return null;
    }

    /**
     * <p>removeCategory</p>
     *
     * @param category a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     */
    public void removeCategory(RequisitionCategory category) {
        if (m_categories != null) {
            Iterator<RequisitionCategory> i = m_categories.iterator();
            while (i.hasNext()) {
                RequisitionCategory cat = i.next();
                if (cat.getName().equals(category.getName())) {
                    i.remove();
                    break;
                }
            }
        }
    }
    
    /**
     * <p>deleteCategory</p>
     *
     * @param category a {@link java.lang.String} object.
     */
    public void deleteCategory(String category) {
        if (m_categories != null) {
            Iterator<RequisitionCategory> i = m_categories.iterator();
            while (i.hasNext()) {
                RequisitionCategory cat = i.next();
                if (cat.getName().equals(category)) {
                    i.remove();
                    break;
                }
            }
        }
    }

    /**
     * <p>getDescr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescr() {
        return m_description;
    }

    /**
     * <p>setDescr</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setDescr(String value) {
        m_description = value;
    }

    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddr() {
        return m_ipAddress;
    }

    /**
     * <p>setIpAddr</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setIpAddr(String value) {
        m_ipAddress = value;
    }

    /**
     * <p>isManaged</p>
     *
     * @return a boolean.
     */
    public boolean isManaged() {
        if (m_isManaged == null) {
            return true;
        } else {
            return m_isManaged;
        }
    }

    /**
     * <p>setManaged</p>
     *
     * @param value a {@link java.lang.Boolean} object.
     */
    public void setManaged(Boolean value) {
        m_isManaged = value;
    }

    /**
     * <p>getSnmpPrimary</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpPrimary() {
        return m_snmpPrimary;
    }

    /**
     * <p>setSnmpPrimary</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setSnmpPrimary(String value) {
        m_snmpPrimary = value;
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    public int getStatus() {
        if (m_status == null) {
            return  1;
        } else {
            return m_status;
        }
    }

    /**
     * <p>setStatus</p>
     *
     * @param value a {@link java.lang.Integer} object.
     */
    public void setStatus(Integer value) {
        m_status = value;
    }

    public int compareTo(RequisitionInterface o) {
        return this.m_ipAddress.compareTo(o.getIpAddr());
    }
    
    public String toString() {
    	return new ToStringBuilder(this)
    		.append("monitored-services", m_monitoredServices)
    		.append("categories", m_categories)
    		.append("description", m_description)
    		.append("ip-address", m_ipAddress)
    		.append("is-managed", m_isManaged)
    		.append("snmp-primary", m_snmpPrimary)
    		.append("status", m_status)
    		.toString();
    }

}
