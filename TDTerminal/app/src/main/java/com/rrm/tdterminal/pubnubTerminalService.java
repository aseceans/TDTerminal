package com.rrm.tdterminal;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import com.google.gson.Gson;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

/**
 * Created by Ryan on 11/24/2016.
 */

public class pubnubTerminalService {
    public static Gson gson = new Gson();
    public static final String ROOM_NAME = "arDemo";
    public static Context context; // don't touch this
    public static Context currentActivity;
    public static boolean terminal = true;
    public static final String RESPONSE = "RESPONSE";
    public static final String REQUEST = "REQUEST";

    // Pubnub Keys
    private static String publishKey = "pub-c-3fde3c33-2104-4a30-be52-710969bfb4b3";
    private static String subscribeKey = "sub-c-45d16f72-b136-11e6-a7bb-0619f8945a4f";

    private static Pubnub pubnub = new Pubnub(publishKey, subscribeKey, true);

    public static Pubnub getPubnub()
    {
        return pubnub;
    }

    public static void SendMessage(String message_type, Object message_data)
    {
        //create the message object
        ARMessage message = new ARMessage(message_type, message_data);

        // serialize the message object
        String messageJson = gson.toJson(message);

        // send the message
        getPubnub().publish(ROOM_NAME, messageJson, new Callback() { });
    }

    public static void ReceivedMessage(Object message)
    {
        ARMessageResponse response = new ARMessageResponse();
        ARMessage arMessage = gson.fromJson(message.toString(), ARMessage.class);

        switch(arMessage.Type)
        {
            case(RESPONSE): {
                response = gson.fromJson(arMessage.Data, ARMessageResponse.class);
                Message m = ((TerminalActivity) currentActivity).getMessageHandle().obtainMessage();
                Bundle data = new Bundle();
                data.putString(RESPONSE, response.Message);
                m.setData(data);
                m.sendToTarget();
                break;
            }
            case(REQUEST):
                break;
        }
    }

    public static void PubnubConnect()
    {
        try
        {
            pubnub.subscribe(ROOM_NAME, new Callback()
            {
                @Override
                public void connectCallback(String channel_name, Object message)
                {
                    {
                        // Notify the presenter that the room was created
                        Message m = ((TerminalActivity)currentActivity).getRoomCreatedHandle().obtainMessage();
                        m.sendToTarget();
                    }
                }

                @Override
                public void successCallback(String channel_name, Object message)
                {
                    ReceivedMessage(message);
                }

                @Override
                public void errorCallback(String channel_name, PubnubError error)
                {

                }
            });
        }
        catch (PubnubException e)
        {
            System.out.println(e.toString());
        }
    }
}