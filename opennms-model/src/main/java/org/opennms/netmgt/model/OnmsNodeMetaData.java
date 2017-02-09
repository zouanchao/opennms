package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="meta-data")
@Entity
@Table(name="node_metadata")
public class OnmsNodeMetaData implements Serializable {

    private static final long serialVersionUID = 3529745790145204662L;

    private OnmsNode node;
    private String context;
    private String key;
    private String value;

    public OnmsNodeMetaData() { }

    public OnmsNodeMetaData(OnmsNode node, String context, String key, String value) {
        this.node = Objects.requireNonNull(node);
        this.context = Objects.requireNonNull(context);
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodeid", nullable = false)
    public OnmsNode getNode(){
        return node;
    }

    public void setNode(OnmsNode node) {
        this.node = node;
    }

    @Id
    @Column(name="context", nullable = false)
    public String getContext(){
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Id
    @Column(name="key", nullable = false)
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Column(name="value", nullable = false)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
