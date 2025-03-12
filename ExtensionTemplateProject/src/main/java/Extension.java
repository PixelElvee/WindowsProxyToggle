
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

        private static final String REG_QUERY_COMMAND = "reg query \"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable";
        private static final String REG_ADD_COMMAND_TEMPLATE = "reg add \"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable /t REG_DWORD /d %s /f";

        public static void run(MontoyaApi api) {
            try {
                // Read the current ProxyEnable value
                String currentProxyEnable = readProxyEnable();

                // Toggle the ProxyEnable value
                String newProxyEnable = currentProxyEnable.equals("0x0") ? "1" : "0";
                setProxyEnable(newProxyEnable, api);
                api.logging().raiseInfoEvent("ProxyEnable value set to: " + newProxyEnable);
            } catch (IOException e) {
                api.logging().raiseErrorEvent(e.toString());
            }
        }

        private static String readProxyEnable() throws IOException {
            Process process = Runtime.getRuntime().exec(REG_QUERY_COMMAND);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("ProxyEnable")) {
                        // Extract the value from the line
                        String[] tokens = line.trim().split("\\s+");
                        return tokens[tokens.length - 1];
                    }
                }
            }
            return null;
        }

        private static void setProxyEnable(String value, MontoyaApi api) throws IOException {
            String command = String.format(REG_ADD_COMMAND_TEMPLATE, value);
            Process process = Runtime.getRuntime().exec(command);
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                api.logging().raiseErrorEvent(e.toString());
            }
        }
    }
}
