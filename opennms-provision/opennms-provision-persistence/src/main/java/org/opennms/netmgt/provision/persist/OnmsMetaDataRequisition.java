package org.opennms.netmgt.provision.persist;

import java.util.Objects;

import org.opennms.netmgt.provision.persist.requisition.RequisitionMetaData;

public class OnmsMetaDataRequisition {

    private RequisitionMetaData metaData;

    public OnmsMetaDataRequisition(RequisitionMetaData metaData) {
        this.metaData = Objects.requireNonNull(metaData);
    }

    public RequisitionMetaData getMetaData() {
        return metaData;
    }

    public String getContext() {
        return metaData.getContext();
    }

    public String getKey() {
        return metaData.getKey();
    }

    public String getValue() {
        return metaData.getValue();
    }

    public void visit(RequisitionVisitor visitor) {
        visitor.visitMetaData(this);
        visitor.completeMetaData(this);
    }

}
