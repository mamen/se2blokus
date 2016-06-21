package at.aau.se2.test;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;

/**
 * Connection Class with all the necessary fields for maintaining a connection.
 */
public class Connection {

    private static Connection instance;

    private static GoogleApiClient apiClient;
    private static List<String> remotePeerEndpoints;
    private static ColorScreen colorScreen;
    private static FullscreenActivity fullscreenActivity;

    private Connection(){}

    public static Connection getInstance() {
        if (Connection.instance == null) {
            Connection.instance = new Connection();
        }
        return Connection.instance;
    }


    public static ColorScreen getColorScreen() {
        return getInstance().colorScreen;
    }

    public static void setColorScreen(ColorScreen colorScreen) {
        getInstance().colorScreen = colorScreen;
    }

    public static FullscreenActivity getFullscreenActivity() {
        return getInstance().fullscreenActivity;
    }

    public static void setFullscreenActivity(FullscreenActivity fullscreenActivity) {
        getInstance().fullscreenActivity = fullscreenActivity;
    }

    public static List<String> getRemotePeerEndpoints() {
        return getInstance().remotePeerEndpoints;
    }

    public static void setRemotePeerEndpoints(List<String> remotePeerEndpoints) {
        getInstance().remotePeerEndpoints = remotePeerEndpoints;
    }

    public static void setApiClient(GoogleApiClient apiClient) {
        getInstance().apiClient = apiClient;
    }

    public static GoogleApiClient getApiClient() {
        return getInstance().apiClient;
    }
}
