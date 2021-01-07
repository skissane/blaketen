package blaketen;

import com.sun.jna.Platform;

public class SayUtils {
    public static void doSay(String s) {
        if (RecordedSpeech.canSay(s)) {
            RecordedSpeech.say(s);
            return;
        }
        if (Platform.isMac()) doSayMacos(s);
        else if (Platform.isWindows()) doSayWindows(s);
        else throw new IllegalStateException("Unsupported platform");
    }

    private static void doSayWindows(String s) {
        WindowsSayUtils.say(s);
    }

    private static void doSayMacos(String s) {
        AppleScriptUtils.eval("say \"" + s + "\"");
    }
}
