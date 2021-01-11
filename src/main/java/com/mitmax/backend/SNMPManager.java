package com.mitmax.backend;

import com.mitmax.frontend.Controller;
import com.mitmax.frontend.LogLevel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableMap;
import org.soulwing.snmp.Mib;
import org.soulwing.snmp.MibFactory;
import org.soulwing.snmp.SnmpFactory;
import org.soulwing.snmp.SnmpListener;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Static helper class to manage everything SNMP-related.
 */
public class SNMPManager {
    private static final ObservableMap<String, SNMPTarget> snmpTargets;
    private static final ObservableMap<String, SNMPTarget> pendingSnmpTargets;
    private static final AtomicInteger pendingSnmpTargetsCount;
    private static final ExecutorService executorService;
    private static Mib mib;
    private static SnmpListener listener;
    private static Runnable onNoMorePendingTargets;

    static {
        snmpTargets = FXCollections.observableHashMap();
        pendingSnmpTargets = FXCollections.observableHashMap();
        executorService = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        });
        pendingSnmpTargetsCount = new AtomicInteger();
        onNoMorePendingTargets = () -> {};

        Settings.mibModules.addListener((ListChangeListener<String>) c -> updateMib());
    }

    /**
     * Scans the address on the community with the OIDs.
     * Logs start of request and whether the request was successful or timed out.
     * The latter is only logged if so specified by the application's {@link LogLevel} or if {@code iSubnet} is false.
     * @param ip a {@code String} representation of the address to scan.
     * @param ipBinary a binary representation of the address to scan of type {@code long}.
     * @param community a {@code String} containing the community to perform the scan in.
     * @param oids a list of {@code Strings} containing {@link org.snmp4j.smi.OID}s to scan for.
     * @param isSubnet a {@code boolean} determining whether this operation is performed
     *                 as a part of a larger subnet- or range-scan.
     */
    public static void scanAddress(String ip, long ipBinary, String community, List<String> oids, boolean isSubnet) {
        executorService.execute(() -> {
            SNMPTarget target = snmpTargets.get(ip);

            if(target == null) {
                target = pendingSnmpTargets.get(ip);
                if(target == null) {
                    target = new SNMPTarget(ip, ipBinary);
                    pendingSnmpTargets.put(ip, target);
                    pendingSnmpTargetsCount.incrementAndGet();
                }
            }

            target.retrieve(community, oids, isSubnet);
        });
    }

    /**
     * Scans a whole subnet at once.
     * Logs start and end of scan if so specified by the application's {@link LogLevel}.
     * @param ip a {@code String} representation of the subnet to scan.
     * @param community the {@code String} containing the community to perform this scan in.
     * @param mask an {@code int} specifying the subnet-mask.
     */
    public static void scanSubnet(String ip, String community, int mask) {
        executorService.execute(() -> {
            if(Settings.logLevel != LogLevel.NONE) {
                Controller.getLogger().logImmediately("Started subnet scan!");
            }

            onNoMorePendingTargets = () -> {
                if(Settings.logLevel != LogLevel.NONE) {
                    Controller.getLogger().logBuffered("Finished subnet scan!");
                    Controller.getLogger().flush();
                }
            };

            int wildcard = 32 - mask;
            long binaryWildcard = (long) (Math.pow(2, wildcard) - 1);
            long netId = (AddressHelper.getAsBinary(ip) >> wildcard) << wildcard;
            long broadcast = netId | binaryWildcard;

            long currentAddress = netId;
            while(++currentAddress != broadcast) {
                scanAddress(AddressHelper.getAsString(currentAddress), currentAddress, community, Settings.initialRequests, true);
            }
        });
    }

    /**
     * Scans an entire ip-address-range at once.
     * Logs start and end of scan if so specified by the application's {@link LogLevel}
     * @param address the {@code String} containing the start-ip.
     * @param end the {@code String} containing the end-ip.
     * @param community the {@code String} containing the community to perform this scan in.
     */
    public static void scanRange(final long address, long end, String community) {
        executorService.execute(() -> {
            long startAddress = address;
            if(Settings.logLevel != LogLevel.NONE) {
                Controller.getLogger().logImmediately("Started range scan!");
            }

            onNoMorePendingTargets = () -> {
                if(Settings.logLevel != LogLevel.NONE) {
                    Controller.getLogger().logImmediately("Finished range scan!");
                }
            };

            while(startAddress <= end) {
                scanAddress(AddressHelper.getAsString(startAddress), startAddress, community, Settings.initialRequests, true);
                startAddress++;
            }
        });
    }

    /**
     * Closes all open {@link org.soulwing.snmp.SnmpContext}s.
     */
    public static void closeTargets() {
        for(SNMPTarget target : snmpTargets.values()) {
            target.close();
        }
    }

    /**
     * Stops the {@link SnmpListener} listening for trap/inform and shuts the {@link ExecutorService} down.
     */
    public static void stop() {
        listener.close();
        executorService.shutdown();
    }

    /**
     * Getter for the {@link ObservableMap} containing {@link SNMPTarget}s backed by a {@link java.util.HashMap}.
     * This map <b>does not</b> contain currently pending {@link SNMPTarget}s without any previous requests.
     * @return the {@link ObservableMap} containing all created {@link SNMPTarget}s.
     */
    public static ObservableMap<String, SNMPTarget> getSnmpTargets() {
        return snmpTargets;
    }

    /**
     * Getter for the {@link Mib}.
     * Calls {@code updateMib()} if the {@link Mib} is {@code null}.
     * @return the {@link Mib} containing the loaded {@link Mib}-modules.
     */
    static Mib getMib() {
        if(mib == null) {
            updateMib();
        }

        return mib;
    }

    /**
     * Updates the {@link Mib} to include all modules specified in {@link Settings}{@code .mibModules} (and only those).
     */
    static void updateMib() {
        mib = MibFactory.getInstance().newMib();
        for(String module : Settings.mibModules) {
            try {
                mib.load(module);
            } catch (IOException ex) {
                Controller.getLogger().logImmediately("MIB module '" + module + "' could not be loaded");
            }
        }
    }

    /**
     * Can be called by {@link SNMPTarget}s to tell the {@link SNMPManager} that they were successful
     * (then they will get added to the map returned by {@code getSnmpTargets()} or that their request failed
     * (then they will only be removed from the {@link ObservableMap} containing the pending requests).
     * @param target the {@link SNMPTarget} who called this function.
     * @param successful a {@code boolean} indicating whether the request was successful.
     */
    static void onRetrievalDone(SNMPTarget target, boolean successful) {
        pendingSnmpTargets.remove(target.getIp());
        pendingSnmpTargetsCount.decrementAndGet();

        if(successful) {
            Platform.runLater(() -> snmpTargets.put(target.getIp(), target));
        }
        else {
            target.close();
        }

        if(pendingSnmpTargetsCount.get() == 0) {
            onNoMorePendingTargets.run();
        }
    }

    /**
     * Registers an {@link SnmpListener} to listen for traps/informs on the default port 162.
     * Incoming messages are reported to the {@link Controller}.
     */
    public static void registerTrapListener() {
        listener = SnmpFactory.getInstance().newListener(162, SNMPManager.getMib());
        listener.addHandler(event -> {
            Controller.addNotification(event.getSubject());
            return true;
        });
    }
}
