package io.joshworks.simpletow.discovery.common;


import java.util.Date;

/**
 * Created by Josue on 12/07/2016.
 */
public class Instance {

    public enum State {
        UP, DOWN, OUT_OF_SERVICE
    }

    private String id;
    private String address;
    private long lastUpdate;
    private Date since;
    private Date downSince;
    private String name;
    private State state = State.DOWN;
    private boolean discoverable;
    private boolean client;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Date getDownSince() {
        return downSince;
    }

    public void setDownSince(Date downSince) {
        this.downSince = downSince;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDiscoverable() {
        return discoverable;
    }

    public void setDiscoverable(boolean discoverable) {
        this.discoverable = discoverable;
    }

    public boolean isClient() {
        return client;
    }

    public void setClient(boolean client) {
        this.client = client;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void updateInstanceState(Instance.State newState) {
        state = newState;
        if (Instance.State.DOWN.equals(newState)) {
            downSince = new Date();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Instance)) return false;

        Instance that = (Instance) o;

        return address != null ? address.equals(that.address) : that.address == null;
    }

    @Override
    public int hashCode() {
        return address != null ? address.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Instance{" +
                "id='" + id + '\'' +
                ", address='" + address + '\'' +
                ", lastUpdate=" + lastUpdate +
                ", since=" + since +
                ", downSince=" + downSince +
                ", name='" + name + '\'' +
                ", state=" + state +
                ", discoverable=" + discoverable +
                ", client=" + client +
                '}';
    }
}
