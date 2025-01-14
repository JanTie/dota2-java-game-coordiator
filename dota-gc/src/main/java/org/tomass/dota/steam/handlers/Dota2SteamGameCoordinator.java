package org.tomass.dota.steam.handlers;

import java.util.HashMap;
import java.util.Map;

import org.tomass.dota.gc.clients.Dota2Client;
import org.tomass.dota.gc.handlers.ClientGCMsgHandler;
import org.tomass.dota.gc.handlers.Dota2ClientGCMsgHandler;
import org.tomass.dota.gc.handlers.callbacks.ClientRichPresenceInfoCallback;
import org.tomass.dota.gc.logger.SteamClientLogger;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IClientGCMsg;
import in.dragonbra.javasteam.base.IPacketGCMsg;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientP2PConnectionInfo;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientRichPresenceInfo;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientRichPresenceRequest;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgGCClient;
import in.dragonbra.javasteam.steam.handlers.steamgamecoordinator.SteamGameCoordinator;
import in.dragonbra.javasteam.steam.handlers.steamgamecoordinator.callback.MessageCallback;
import in.dragonbra.javasteam.util.compat.Consumer;

public class Dota2SteamGameCoordinator extends SteamGameCoordinator {

    private final Integer appId = Dota2Client.APP_ID;

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    private Map<Class<? extends ClientGCMsgHandler>, ClientGCMsgHandler> handlers = new HashMap<>();

    public Dota2SteamGameCoordinator() {
        dispatchMap = new HashMap<>();
        dispatchMap.put(EMsg.ClientFromGC, packetMsg -> handleFromGC(packetMsg));
        dispatchMap.put(EMsg.ClientRichPresenceInfo, packetMsg -> handleRichPresenceInfo(packetMsg));
        dispatchMap.put(EMsg.ClientP2PConnectionInfo, packetMsg -> handleConnectionInfo(packetMsg));
    }

    private void handleConnectionInfo(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientP2PConnectionInfo.Builder> protobuf = new ClientMsgProtobuf<>(
                CMsgClientP2PConnectionInfo.class, packetMsg);
        getLogger().trace(">>handleConnectionInfo: " + protobuf.getBody());
    }

    private void handleRichPresenceInfo(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientRichPresenceInfo.Builder> protobuf = new ClientMsgProtobuf<>(
                CMsgClientRichPresenceInfo.class, packetMsg);
        ClientRichPresenceInfoCallback callback = new ClientRichPresenceInfoCallback(protobuf.getTargetJobID(),
                protobuf.getBody());
        client.postCallback(callback);
    }

    // request
    public ClientRichPresenceInfoCallback requestClientRichPresence(Long steamId) {
        ClientMsgProtobuf<CMsgClientRichPresenceRequest.Builder> protobuf = new ClientMsgProtobuf<>(
                CMsgClientRichPresenceRequest.class, EMsg.ClientRichPresenceRequest);
        protobuf.getBody().addSteamidRequest(steamId);
        protobuf.getProtoHeader().setRoutingAppid(appId);
        return getClient().sendJobAndWait(protobuf, 10);
    }

    @Override
    public Dota2Client getClient() {
        return (Dota2Client) super.getClient();
    }

    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            throw new IllegalArgumentException("packetMsg is null");
        }

        try {
            getLogger().trace(">>handleMsg msg: " + packetMsg.getMsgType());
            Consumer<IPacketMsg> dispatcher = dispatchMap.get(packetMsg.getMsgType());
            if (dispatcher != null) {
                dispatcher.accept(packetMsg);
            }
        } catch (Exception e) {
            getLogger().error("!!handleMsg: ", e);
        }
    }

    public void handleFromGC(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgGCClient.Builder> msg = new ClientMsgProtobuf<>(CMsgGCClient.class, packetMsg);
        MessageCallback callback = new MessageCallback(msg.getSourceJobID(), msg.getBody());
        getLogger().trace(">>handleGCMsg GC msg: " + callback.geteMsg());
        if (callback.getAppID() != this.appId) {
            return;
        }
        handleFromGC(callback.getMessage());
    }

    public void handleFromGC(IPacketGCMsg packetMsg) {
        for (Map.Entry<Class<? extends ClientGCMsgHandler>, ClientGCMsgHandler> entry : handlers.entrySet()) {
            try {
                entry.getValue().handleGCMsg(packetMsg);
            } catch (Exception e) {
                getLogger().warn("Unhandled exception from " + entry.getKey().getName() + " handlers", e);
            }
        }
    }

    /**
     * Adds a new handler to the internal list of message handlers.
     *
     * @param handler
     *            The handler to add.
     */
    public void addDota2Handler(Dota2ClientGCMsgHandler handler) {
        handler.setup(this);
        addHandler(handler);
    }

    public void addHandler(ClientGCMsgHandler handler) {
        if (handlers.containsKey(handler.getClass())) {
            throw new IllegalArgumentException("A handler of type " + handler.getClass() + " is already registered.");
        }

        handlers.put(handler.getClass(), handler);
    }

    /**
     * Removes a registered handler by name.
     *
     * @param handler
     *            The handler name to remove.
     */
    public void removeHandler(Class<? extends ClientGCMsgHandler> handler) {
        handlers.remove(handler);
    }

    /**
     * Removes a registered handler.
     *
     * @param handler
     *            The handler name to remove.
     */
    public void removeHandler(ClientGCMsgHandler handler) {
        removeHandler(handler.getClass());
    }

    public Map<EMsg, Consumer<IPacketMsg>> getDispatchMap() {
        return dispatchMap;
    }

    public void send(IClientGCMsg msg) {
        send(msg, appId);
    }

    public SteamClientLogger getLogger() {
        return getClient().getLogger();
    }

}
