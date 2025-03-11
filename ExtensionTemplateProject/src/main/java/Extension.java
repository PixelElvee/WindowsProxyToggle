
import java.util.prefs.Preferences;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.menu.BasicMenuItem;
import burp.api.montoya.ui.menu.Menu;
import burp.api.montoya.ui.menu.MenuItem;

public class Extension implements BurpExtension {

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("WindowsProxyToggle");
        api.logging().logToOutput("Extension has been loaded.");

        BasicMenuItem basicMenuItem = MenuItem.basicMenuItem("on/off");
        MenuItem unloadExtensionItem = basicMenuItem.withAction(() -> WindowsProxyToggle.run(api));

        Menu menu = Menu.menu("WindowsProxyToggle").withMenuItems(unloadExtensionItem);

        api.userInterface().menuBar().registerMenu(menu);
    }

    public class WindowsProxyToggle {

        private static final String REGISTRY_PATH = "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";
        private static final String PROXY_ENABLE_KEY = "ProxyEnable";

        public static void run(MontoyaApi api) {
            try {
                // Open the registry key
                Preferences userPrefs = Preferences.userRoot().node(REGISTRY_PATH);

                // Check if the proxy is enabled
                int proxyEnable = userPrefs.getInt(PROXY_ENABLE_KEY, 0);
                if (proxyEnable == 1) {
                    // Proxy is enabled, so disable it
                    userPrefs.putInt(PROXY_ENABLE_KEY, 0);
                    api.logging().logToOutput("Proxy is disabled");
                } else {
                    // Proxy is disabled, so enable it
                    userPrefs.putInt(PROXY_ENABLE_KEY, 1);
                    api.logging().logToOutput("Proxy is enabled");
                }
            } catch (Exception e) {
                api.logging().logToError(e);
            }
        }
    }
}
