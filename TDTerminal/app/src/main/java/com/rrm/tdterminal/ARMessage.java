package com.rrm.tdterminal;

import com.google.gson.Gson;

/**
 * Created by Ryan on 11/24/2016.
 */

public class ARMessage
{
    private static Gson gson = new Gson();

    public String Type;
    public String Data;

    public ARMessage(String type, Object message_object)
    {
        Type = type;
        Data = gson.toJson(message_object);
    }
}


