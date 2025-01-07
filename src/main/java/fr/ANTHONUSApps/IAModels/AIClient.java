package fr.ANTHONUSApps.IAModels;

import okhttp3.OkHttpClient;

import java.io.IOException;

public abstract class AIClient {
    protected String API_URL;
    protected String API_KEY;

    protected OkHttpClient client = new OkHttpClient();

    public abstract String getResponse(String query) throws IOException;
}
