package com.babamodz.darktricks;

public class ModuleStatus {
    /**
     * This method returns false by default.
     * When the Xposed/LSPosed module is properly loaded and activated,
     * it is hooked in Main.java to return true.
     */
    public static boolean isModuleActive() {
        return false;
    }
}
