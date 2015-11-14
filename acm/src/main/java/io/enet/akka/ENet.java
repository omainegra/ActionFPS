package io.enet.akka;

import com.sun.jna.*;
import com.sun.jna.ptr.ShortByReference;

import java.util.Arrays;
import java.util.List;

public interface ENet extends Library {
    public static class size_t extends IntegerType {
        public size_t() { this(0); }
        public size_t(long value) { super(Native.SIZE_T_SIZE, value); }
    }
    public static class ENetAddress extends Structure {
        public static class ByReference extends ENetAddress implements Structure.ByReference {}
        public static class ByValue extends ENetAddress implements Structure.ByValue {}
        public int host;
        public short port;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("host", "port");
        }
    }

    public class ENetChannelPointer extends Pointer {
        public ENetChannelPointer(long peer) {
            super(peer);
        }
    }

    public class ByteListPointer extends Pointer {
        public ByteListPointer(long peer) {
            super(peer);
        }
    }
    public static class ENetPacket extends Structure {
        public static class ByReference extends ENetPacket implements Structure.ByReference {}
        public size_t referenceCount;
        public int flags;
        public Pointer data;
        public size_t dataLength;
        public Pointer freeCallback;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("referenceCount", "flags", "data", "dataLength", "freeCallback");
        }
    }
    public class ENetPacketFreeCallback extends Pointer {
        public ENetPacketFreeCallback(long peer) {
            super(peer);
        }
    }

    public class ENetPeerPointer extends Pointer {
        public ENetPeerPointer(long peer) {
            super(peer);
        }
    }
    public class ENetHostPointer extends Pointer {
        public ENetHostPointer(long peer) {
            super(peer);
        }
    }
    public enum ENetEventType
    {
        ENET_EVENT_TYPE_NONE    ,
        ENET_EVENT_TYPE_CONNECT  ,
        ENET_EVENT_TYPE_DISCONNECT,
        ENET_EVENT_TYPE_RECEIVE
    }
    public class ENetEvent extends Structure {
        public static class ByReference extends ENetEvent implements Structure.ByReference {
        }
        public int type;
        // to peer
        public Pointer peer;
        public byte channelID;
        public int data;
        // to packet
        public ENetPacket.ByReference packet;
        @Override
        protected List getFieldOrder() {
            return Arrays.asList("type", "peer", "channelID", "data", "packet");
        }
    }
    short enet_initialize ();
//    void enet_deinitialize ();
//    int enet_time_get ();
//    void enet_time_set (int a);
    int enet_address_set_host (ENetAddress.ByReference address, String hostName);
//    short enet_address_get_host_ip (ENetAddress.ByReference address, Pointer hostName, int nameLength);
//    short enet_address_get_host (ENetAddress.ByReference address, String hostName, int nameLength);
    Pointer enet_packet_create (Pointer a, size_t size, int c);
//    void         enet_packet_destroy (ENetPacket.ByReference packet);
//    short          enet_packet_resize  (ENetPacket.ByReference packet, int a);
    Pointer enet_host_create (ENetAddress.ByReference address, size_t maxConnections, size_t channelNum, int downRate, int upRate);
//    void       enet_host_destroy (ENetHostPointer host);
    // host: ENetHost *
    Pointer enet_host_connect (Pointer host, ENetAddress.ByReference address, size_t a, int b);
//    short        enet_host_check_events (ENetHostPointer host, ENetEvent.ByReference event);
    short        enet_host_service (Pointer host, ENetEvent.ByReference event, int a);
    void       enet_host_flush (Pointer host);
//    void       enet_host_broadcast (ENetHostPointer host, byte a, ENetPacket.ByReference packet);
//    void       enet_host_channel_limit (ENetHostPointer host, int a);
//    void       enet_host_bandwidth_limit (ENetHostPointer host, int a, int b);
//      void       enet_host_bandwidth_throttle (ENetHostPointer host);
    short                 enet_peer_send (Pointer peer, byte a, Pointer packet);
//    ENetPacket.ByReference        enet_peer_receive (ENetPeerPointer host, byte channelID);
//    void                enet_peer_ping (ENetPeerPointer host);
//    void                enet_peer_ping_shorterval (ENetPeerPointer host, int a);
//    void                enet_peer_timeout (ENetPeerPointer host, int a, int b, int c);
//    void                enet_peer_reset (ENetPeerPointer host);
    void                enet_peer_disconnect (Pointer peer, int a);
//    void                enet_peer_disconnect_now (ENetPeerPointer peer, int a);
//    void                enet_peer_disconnect_later (ENetPeerPointer peer, int a);
//    void                enet_peer_throttle_configure (ENetPeerPointer host, int a, int b, int c);
    void                enet_peer_throttle_configure (Pointer peer, int a, int b, int c);

    public static class ENetListNode extends Structure {
        @Override
        protected List getFieldOrder() {
            return Arrays.asList("next", "previous");
        }

        public static class ByValue extends ENetListNode implements Structure.ByValue {}
        public Pointer next;
        public Pointer previous;
    }
//    public static class ENetPeer extends Structure {
//        @Override
//        protected List getFieldOrder() {
//            return Arrays.asList(
//                    "acknowledgements", "address", "channelCount", "channels", "connectID", "data", "dispatchList", "dispatchedCommands", "earliestTimeout", "eventData", "highestRoundTripTimeVariance", "host", "incomingBandwidth", "incomingBandwidthThrottleEpoch", "incomingDataTotal", "incomingPeerID", "incomingSessionID", "incomingUnsequencedGroup", "lastReceiveTime", "lastRoundTripTime", "lastRoundTripTimeVariance", "lastSendTime", "lowestRoundTripTime", "mtu", "needsDispatch", "nextTimeout", "outgoingBandwidth", "outgoingBandwidthThrottleEpoch", "outgoingDataTotal", "outgoingPeerID", "outgoingReliableCommands", "outgoingReliableSequenceNumber", "outgoingSessionID", "outgoingUnreliableCommands", "outgoingUnsequencedGroup", "packetLoss", "packetLossEpoch", "packetLossVariance", "packetThrottle", "packetThrottleAcceleration", "packetThrottleCounter", "packetThrottleDeceleration", "packetThrottleEpoch", "packetThrottleInterval", "packetThrottleLimit", "packetsLost", "packetsSent", "pingInterval", "reliableDataInTransit", "roundTripTime", "roundTripTimeVariance", "sentReliableCommands", "sentUnreliableCommands", "state", "timeoutLimit", "timeoutMaximum", "timeoutMinimum", "unsequencedWindow", "windowSize"
//
//            );
//        }
//        public ENetPeer(Pointer p) {
//            super(p);
//        }
//        public static class ByReference extends ENetPeer implements Structure.ByReference {
//            public ByReference(Pointer p) { super(p); }
//        }
//        // ENetListNode
//        public ENetListNode.ByValue  dispatchList;
//        public Pointer host;
//        public short outgoingPeerID;
//        public short incomingPeerID;
//        public int connectID;
//        public byte  outgoingSessionID;
//        public byte   incomingSessionID;
//        public ENetAddress.ByValue  address;
//        public Pointer  data;
//        public int state;
////        // ENetChannel *
//        public Pointer channels;
//        public int        channelCount;
//        public int incomingBandwidth;  /**< Downstream bandwidth of the client in bytes/second */
//        public int outgoingBandwidth;  /**< Upstream bandwidth of the client in bytes/second */
//        public int incomingBandwidthThrottleEpoch;
//        public int outgoingBandwidthThrottleEpoch;
//        public int incomingDataTotal;
//        public int outgoingDataTotal;
//        public int lastSendTime;
//        public int lastReceiveTime;
//        public int nextTimeout;
//        public int earliestTimeout;
//        public int packetLossEpoch;
//        public int packetsSent;
//        public int packetsLost;
//        public int packetLoss;          /**< mean packet loss of reliable packets as a ratio with respect to the constant ENET_PEER_PACKET_LOSS_SCALE */
//        public int packetLossVariance;
//        public int packetThrottle;
//        public int packetThrottleLimit;
//        public int packetThrottleCounter;
//        public int packetThrottleEpoch;
//        public int packetThrottleAcceleration;
//        public int packetThrottleDeceleration;
//        public int packetThrottleInterval;
//        public int pingInterval;
//        public int timeoutLimit;
//        public int timeoutMinimum;
//        public int timeoutMaximum;
//        public int lastRoundTripTime;
//        public int lowestRoundTripTime;
//        public int lastRoundTripTimeVariance;
//        public int highestRoundTripTimeVariance;
//        public int roundTripTime;            /**< mean round trip time (RTT), in milliseconds, between sending a reliable packet and receiving its acknowledgement */
//        public int roundTripTimeVariance;
//        public int mtu;
//        public int windowSize;
//        public int reliableDataInTransit;
//        public short outgoingReliableSequenceNumber;
//        // ENetList
//        public ENetList.ByValue      acknowledgements;
//        public ENetList.ByValue      sentReliableCommands;
//        public ENetList.ByValue      sentUnreliableCommands;
//        public ENetList.ByValue      outgoingReliableCommands;
//        public ENetList.ByValue      outgoingUnreliableCommands;
//        public ENetList.ByValue      dispatchedCommands;
//        public int           needsDispatch;
//        public short incomingUnsequencedGroup;
//        public short outgoingUnsequencedGroup;
////        ignore, we don't care too much
//        public int[] unsequencedWindow = new int[1024 / 32];
//        public int eventData;
//    }

    public static class ENetPeer extends Structure
    {
        public ENetPeer(Pointer p) {
            super(p);
        }
        public static class ENetPeerByReference extends ENetPeer implements Structure.ByReference {
            public ENetPeerByReference(Pointer init) {
                super(init);
            }
        }
        public ENetListNode.ByValue  dispatchList;
        public Pointer host;
        public short   outgoingPeerID;
        public short   incomingPeerID;
        public int connectID;
        public byte    outgoingSessionID;
        public byte    incomingSessionID;
        public ENetAddress   address;
        public Pointer        data;
        public int state;
        public Pointer channels;
        public size_t channelCount;
        public int incomingBandwidth;
        public int outgoingBandwidth;
        public int incomingBandwidthThrottleEpoch;
        public int outgoingBandwidthThrottleEpoch;
        public int incomingDataTotal;
        public int outgoingDataTotal;
        public int lastSendTime;
        public int lastReceiveTime;
        public int nextTimeout;
        public int earliestTimeout;
        public int packetLossEpoch;
        public int packetsSent;
        public int packetsLost;
        public int packetLoss;
        public int packetLossVariance;
        public int packetThrottle;
        public int packetThrottleLimit;
        public int packetThrottleCounter;
        public int packetThrottleEpoch;
        public int packetThrottleAcceleration;
        public int packetThrottleDeceleration;
        public int packetThrottleInterval;
        public int pingInterval;
        public int timeoutLimit;
        public int timeoutMinimum;
        public int timeoutMaximum;
        public int lastRoundTripTime;
        public int lowestRoundTripTime;
        public int lastRoundTripTimeVariance;
        public int highestRoundTripTimeVariance;
        public int roundTripTime;
        public int roundTripTimeVariance;
        public int mtu;
        public int windowSize;
        public int reliableDataInTransit;
        public short   outgoingReliableSequenceNumber;
        public ENetList.ByValue  acknowledgements;
        public ENetList.ByValue  sentReliableCommands;
        public ENetList.ByValue  sentUnreliableCommands;
        public ENetList.ByValue  outgoingReliableCommands;
        public ENetList.ByValue  outgoingUnreliableCommands;
        public ENetList.ByValue  dispatchedCommands;
        public int           needsDispatch;
        public short   incomingUnsequencedGroup;
        public short   outgoingUnsequencedGroup;
        public int[]   unsequencedWindow = new int[1024 / 32];
        public int   eventData;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("dispatchList", "host", "outgoingPeerID", "incomingPeerID", "connectID", "outgoingSessionID",
                    "incomingSessionID", "address", "data", "state", "channels", "channelCount", "incomingBandwidth",
                    "outgoingBandwidth", "incomingBandwidthThrottleEpoch", "outgoingBandwidthThrottleEpoch",
                    "incomingDataTotal", "outgoingDataTotal", "lastSendTime", "lastReceiveTime", "nextTimeout",
                    "earliestTimeout", "packetLossEpoch", "packetsSent", "packetsLost", "packetLoss", "packetLossVariance",
                    "packetThrottle", "packetThrottleLimit", "packetThrottleCounter", "packetThrottleEpoch",
                    "packetThrottleAcceleration", "packetThrottleDeceleration", "packetThrottleInterval", "pingInterval",
                    "timeoutLimit", "timeoutMinimum", "timeoutMaximum", "lastRoundTripTime", "lowestRoundTripTime",
                    "lastRoundTripTimeVariance", "highestRoundTripTimeVariance", "roundTripTime", "roundTripTimeVariance",
                    "mtu", "windowSize", "reliableDataInTransit", "outgoingReliableSequenceNumber", "acknowledgements",
                    "sentReliableCommands", "sentUnreliableCommands", "outgoingReliableCommands",
                    "outgoingUnreliableCommands", "dispatchedCommands", "needsDispatch", "incomingUnsequencedGroup",
                    "outgoingUnsequencedGroup", "unsequencedWindow", "eventData");
        }
    }

    public static class ENetList extends Structure {
        @Override
        protected List getFieldOrder() {
            return Arrays.asList("sentinel");
        }
        public static class ByValue extends ENetList implements Structure.ByValue {}
        public ENetListNode.ByValue sentinel;

    }
}