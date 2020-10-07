package com.consultantapp.utils;

import android.os.Handler;
import android.os.Looper;

import com.consultantapp.data.models.responses.chat.ChatMessage;
import com.consultantapp.data.models.responses.chat.MessageSend;
import com.consultantapp.data.repos.UserRepository;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import timber.log.Timber;

import static com.consultantapp.utils.AppConstantKt.SOCKET_URL;

/**
 * Created by Rishi Sharma on 9/8/17.
 */

@Singleton
public class AppSocket {

    private UserRepository userRepository;

    private Socket mSocket;

    private Timer manualReconnectTimer = new Timer();
    private List<OnMessageReceiver> onMessageReceiverList = new ArrayList<>();

    private List<ConnectionListener> onConnectionListeners = new ArrayList<>();
    private Emitter.Listener onConnect = args -> {
        manualReconnectTimer.cancel();
        Timber.e("AppSocket - onConnect called");
        notifyConnectionListeners(Socket.EVENT_CONNECT);
    };

    private Emitter.Listener onDisconnect = args -> {
        Timber.e("AppSocket - onDisconnect called");
        restartManualReconnection();
        notifyConnectionListeners(Socket.EVENT_DISCONNECT);
    };

    private Emitter.Listener onError = args -> {
        Timber.e("AppSocket -onError called");
        restartManualReconnection();
        notifyConnectionListeners(Socket.EVENT_ERROR);
    };

    private Emitter.Listener onTimeOut = args -> {
        Timber.e("AppSocket -onTimeOut called");
        restartManualReconnection();
        notifyConnectionListeners(Socket.EVENT_CONNECT_TIMEOUT);
    };

    private Emitter.Listener onReconnecting = args -> {
        Timber.e("AppSocket -onReconnecting called");
        restartManualReconnection();
        notifyConnectionListeners(Socket.EVENT_RECONNECTING);
    };

    private Emitter.Listener onReconnectError = args -> {
        Timber.e("AppSocket -onReconnectError called");
        restartManualReconnection();
        notifyConnectionListeners(Socket.EVENT_RECONNECT_ERROR);
    };

    @Inject
    AppSocket(UserRepository userRepository, Gson gson) {
        this.userRepository = userRepository;
        init();
    }

    public boolean init() {
        onMessageReceiverList.clear();
        onConnectionListeners.clear();
        try {
            if (mSocket != null) {
                mSocket.off();
                mSocket.close();
            }


            if (userRepository.isUserLoggedIn()) {
                Timber.e("Socket" + SOCKET_URL + "\n" + userRepository.getUser().getId());

                IO.Options options = new IO.Options();
                options.forceNew = false;
                options.reconnection = true;

                options.query = "user_id=" + userRepository.getUser().getId() + "&domain=" + userRepository.getAppSetting().getDomain();

                mSocket = IO.socket(SOCKET_URL, options);
                connect();
                mSocket.on(Socket.EVENT_CONNECT, onConnect);
                mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
                mSocket.on(Socket.EVENT_CONNECT_ERROR, onError);
                mSocket.on(Socket.EVENT_ERROR, onError);
                mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onTimeOut);
                mSocket.on(Socket.EVENT_RECONNECTING, onReconnecting);
                mSocket.on(Socket.EVENT_RECONNECT_ERROR, onReconnectError);
                mSocket.on(Socket.EVENT_RECONNECT_FAILED, onReconnectError);
                return true;
            } else {
                return false;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean isConnected() {
        return mSocket.connected();
    }

    private void restartManualReconnection() {
        manualReconnectTimer.cancel();
        manualReconnectTimer = new Timer();
        int MANUAL_RECONNECT_INTERVAL = 10000;
        manualReconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mSocket.io().reconnection(true);
                connect();
                Timber.e("AppSocket --> ManualReconnection Timer Task Called");
            }
        }, MANUAL_RECONNECT_INTERVAL);
    }

    public Socket getSocket() {
        if (!mSocket.connected())
            connect();
        return mSocket;
    }

    public void connect() {
        if (!mSocket.connected())
            mSocket.connect();
    }

    public void disconnect() {
        mSocket.disconnect();
    }

    public void emit(final String event, final Object... args) {
        mSocket.emit(event, args);
    }

    public void on(String event, Emitter.Listener fn) {
        mSocket.on(event, fn);
    }

    public void off() {
        mSocket.off();
    }

    public void off(String event) {
        mSocket.off(event);
    }

    public void off(String event, Emitter.Listener fn) {
        mSocket.off(event, fn);
    }

    public void sendMessage(MessageSend message, final OnMessageReceiver msgAck) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(new Gson().toJson(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit(Events.SEND_MESSAGE, jsonObject, (Ack) args ->
                new Handler(Looper.getMainLooper()).post(() ->
                        msgAck.onMessageReceive(new Gson().fromJson(args[0].toString(), ChatMessage.class))));
    }

    public void sendMessageDelivery(String id,String receiverID) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("messageId", id);
            jsonObject.put("receiverId", receiverID);
            mSocket.emit(Events.DELIVERED_MESSAGE, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addConnectionListener(ConnectionListener listener) {
        onConnectionListeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        onConnectionListeners.remove(listener);
    }

    public void removeAllConnectionListeners() {
        onConnectionListeners.clear();
    }

    private void notifyConnectionListeners(final String status) {
        for (final ConnectionListener listener : onConnectionListeners) {
            new Handler(Looper.getMainLooper()).post(() -> listener.onConnectionStatusChanged(status));
        }
    }

    public void addOnMessageReceiver(OnMessageReceiver receiver) {
        if (onMessageReceiverList.isEmpty()) {
            onReceiveMessageEvent();
        }
        onMessageReceiverList.add(receiver);
    }

    public void removeOnMessageReceiver(OnMessageReceiver receiver) {
        onMessageReceiverList.remove(receiver);
        if (onMessageReceiverList.isEmpty()) {
            mSocket.off(Events.RECEIVE_MESSAGE);
        }
    }

    public void removeAllMessageReceivers() {
        onMessageReceiverList.clear();
        mSocket.off(Events.RECEIVE_MESSAGE);
    }

    private void onReceiveMessageEvent() {
        mSocket.on(Events.RECEIVE_MESSAGE, args -> {
            ChatMessage chat;
            chat = new Gson().fromJson(args[0].toString(), ChatMessage.class);
            notifyMessageReceivers(chat);
        });
    }

    private void notifyMessageReceivers(final ChatMessage message) {
        for (final OnMessageReceiver receiver : onMessageReceiverList) {
            new Handler(Looper.getMainLooper()).post(() -> receiver.onMessageReceive(message));
        }
    }

    public interface Events {
        String SEND_MESSAGE = "sendMessage";
        String RECEIVE_MESSAGE = "messageFromServer";
        String TYPING = "typing";
        String BROADCAST = "broadcast";
        String ACKNOWLEDGE_MESSAGE = "acknowledgeMessage";
        String READ_MESSAGE = "readMessage";
        String DELIVERED_MESSAGE = "deliveredMessage";
        String SEND_LIVE_LOCATION = "sendlivelocation";
    }

    public interface MessageStatus {
        String NOT_SENT = "NOT_SENT";
        String SENDING = "SENDING";
        String SENT = "SENT";
        String DELIVERED = "DELIVERED";
        String SEEN = "SEEN";
    }

    public interface OnMessageReceiver {
        void onMessageReceive(ChatMessage message);
    }

    public interface ConnectionListener {
        void onConnectionStatusChanged(String status);
    }
}

