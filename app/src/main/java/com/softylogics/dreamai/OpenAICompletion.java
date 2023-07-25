package com.softylogics.dreamai;

import static com.softylogics.dreamai.Constants.API_ENDPOINT;
import static com.softylogics.dreamai.Constants.API_KEY;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OpenAICompletion {

    private static final String TAG = OpenAICompletion.class.getSimpleName();

    private Context context;
    private RequestQueue requestQueue;

    public OpenAICompletion(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    public void completeText(JSONObject requestBody, Response.Listener<String> listener, Response.ErrorListener errorListener) {



            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, API_ENDPOINT, requestBody,
                    response -> {
                        try {
                            String completedText = response.getJSONArray("choices").getJSONObject(0).getString("text");
                            listener.onResponse(completedText);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            errorListener.onErrorResponse(new VolleyError("JSON parsing error"));
                        }
                    },
                    error -> {
                        Log.e(TAG, "API request error: " + error.getMessage());
                        errorListener.onErrorResponse(error);
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + API_KEY);
                    return headers;
                }
            };

            requestQueue.add(request);


    }
}