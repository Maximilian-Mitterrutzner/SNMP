package com.mitmax.backend;

import javafx.collections.ObservableList;
import org.soulwing.snmp.Varbind;

import java.util.HashMap;
import java.util.List;

/**
 * This class represents a single IP-address to be requested SNMP-data from.
 * It contains a {@link HashMap} of {@link SNMPRecord}s, one for each community.
 * Here the hostname is stored, if there is one.
 * The {@link Comparable} interface is implemented in an attempt to make collections of {@link SNMPTarget}s sortable.
 */
public class SNMPTarget  implements Comparable<SNMPTarget> {
    private final String ip;
    private final long ipBinary;
    private final HashMap<String, SNMPRecord> records;
    private String hostName;
    private boolean isAdded;

    /**
     * Constructs a new instance of this class, specified by the IP-address both as a {@code String} and as binary.
     * @param ip a {@code String} containing the IP-address of this {@link SNMPTarget}
     * @param ipBinary a {@code long} containing the IP-address of this {@link SNMPTarget} in binary.
     */
    SNMPTarget(String ip, long ipBinary) {
        this.ip = ip;
        this.ipBinary = ipBinary;
        records = new HashMap<>(Settings.communities.size());
        hostName = ip;
        isAdded = false;

        for(String community : Settings.communities) {
            records.put(community, new SNMPRecord(ip, community, this));
        }
    }

    /**
     * Tries to retrieve a {@link List} of {@link org.snmp4j.smi.OID}s in the specified community.
     * @param community a {@code String} containing the community to perform the request in.
     * @param oids a {@link List} of {@link org.snmp4j.smi.OID}s in their textual representation.
     * @param isSubnet a {@code boolean} specifying whether this request is performed as a part of a
     *                 larger subnet- or range-scan.
     */
    void retrieve(String community, List<String> oids, boolean isSubnet) {
        records.get(community).retrieve(oids, isSubnet);
    }

    /**
     * Closes the opened context of all {@link SNMPRecord}s.
     */
    void close() {
        for(SNMPRecord record : records.values()) {
            record.close();
        }
    }

    /**
     * Getter for the {@link ObservableList} of {@link Varbind}s of the {@link SNMPRecord} specified by the community.
     * @param community a {@code String} containing the community from which to get the {@link Varbind}s.
     * @return the {@link ObservableList} of {@link Varbind}s of the {@link SNMPRecord} specified by the community.
     */
    public ObservableList<Varbind> getVarbinds(String community) {
        return records.get(community).getVarbinds();
    }

    /**
     * Synchronized method to be called from a {@link SNMPRecord}-child on a finished (but not necessarily successful)
     * SNMP-request. If the {@link SNMPTarget} was already added to the {@link SNMPManager}s list, nothing happens.
     * Otherwise it will check all {@link SNMPRecord}s for still pending requests. <br>
     * Now one of the following cases can occur:
     * <ul>
     *   <li>
     *     The request was a success. <br>
     *     In this case, the {@link SNMPManager} is informed of the success in order to add
     *     this {@link SNMPTarget} to the list.
     *   </li>
     *   <li>
     *     The request was a failure. <br>
     *     If there are still other pending requests the {@link SNMPManager} is not informed
     *     and other calls of this function are awaited. <br>
     *     If there are no more pending requests the {@link SNMPManager} is informed of the failure,
     *     and it will remove this instance from the pending requests.
     *   </li>
     * </ul>
     * @param successful a {@code boolean} indicating whether the request returned successfully.
     */
    synchronized void onRetrievalDone(boolean successful) {
        if(isAdded) {
            return;
        }

        if(successful) {
            isAdded = true;
        }
        else {
            for(SNMPRecord record : records.values()) {
                if(record.getPendingRequests().get() != 0) {
                    return;
                }
            }
        }
        SNMPManager.onRetrievalDone(this, successful);
    }

    /**
     * Getter for the target-IP-address represented by this class.
     * @return a {@code String} containing the IP-address
     */
    public String getIp() {
        return ip;
    }

    /**
     * Getter for the hostname. It includes the hostname and the ip-address.
     * @return a {@code String} containing the hostname.
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Setter for the hostname.
     * @param hostName a {@code String} containing the new hostname.
     */
    void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Implemented method of the interface {@link Comparable} for comparing two {@link SNMPTarget}s.
     * This method compares the IP-addresses (in their binary representation) of the two {@link SNMPTarget}s.
     * @param o the other {@link SNMPTarget} to compare against.
     * @return An {@code int} with 0 if both IPs are equal;
     * An {@code int} with a number less than 0 if the IP of this {@link SNMPTarget} is smaller than the other's;
     * And an {@code int} with a number greater than 0 if the IP of this {@link SNMPTarget} is greater than the other's.
     */
    @Override
    public int compareTo(SNMPTarget o) {
        return Long.compare(this.ipBinary, o.ipBinary);
    }
}
