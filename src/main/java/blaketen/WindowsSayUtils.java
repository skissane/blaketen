package blaketen;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.COMLateBindingObject;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

public class WindowsSayUtils {
    private static SpVoice spVoice;

    @SuppressWarnings("UnusedReturnValue")
    public interface WinMM extends StdCallLibrary {
        WinMM INSTANCE = Native.load("winmm", WinMM.class);

        int waveOutSetVolume(Pointer hwo, WinDef.DWORD dwVolume);
    }

    public static class SpVoice extends COMLateBindingObject {
        public SpVoice() throws COMException {
            super("sapi.spvoice", true);
            WinMM.INSTANCE.waveOutSetVolume(Pointer.NULL, new WinDef.DWORD(0xFFFF));
            this.setProperty("Volume", 100);
            Runtime.getRuntime().addShutdownHook(new Thread(this::release));
        }

        public void say(String s) {
            if (s == null || s.trim().isEmpty()) return;
            this.setProperty("Rate", Character.isLetter(s.charAt(0)) ? -5 : 0);
            this.invokeNoReply(
                    "Speak", new Variant.VARIANT(s.toUpperCase()), new Variant.VARIANT(1));
            this.invokeNoReply("WaitUntilDone", new Variant.VARIANT(-1));
        }
    }

    public static void say(String s) {
        if (spVoice == null) {
            Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_MULTITHREADED);
            spVoice = new SpVoice();
        }
        spVoice.say(s);
    }
}
