package blaketen;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.nio.charset.StandardCharsets;

/** Interfaces with AppleScript. */
public class AppleScriptUtils {
    public static String eval(String script) {
        Memory resultDesc = AECreateDesc("null", Pointer.NULL, 0);
        try {
            byte[] utf8script = script.getBytes(StandardCharsets.UTF_8);
            Memory mScript = new Memory(utf8script.length + 1);
            mScript.clear();
            mScript.write(0, utf8script, 0, utf8script.length);
            Memory scriptTextDesc = AECreateDesc("TEXT", mScript, utf8script.length);
            try {
                Pointer component = openDefaultComponent("osa ", "ascr");
                try {
                    int scriptId = OSACompile(scriptTextDesc, component);
                    try {
                        int resultId = OSAExecute(component, scriptId);
                        try {
                            Pointer text = OSADisplay(component, resultId, "TEXT");
                            try {
                                long size = CoreServices.INSTANCE.AEGetDescDataSize(text);
                                Memory buf = new Memory(size + 1);
                                buf.clear();
                                int rc = CoreServices.INSTANCE.AEGetDescData(text, buf, size);
                                if (rc != 0)
                                    throw new GeneralException("AEGetDescData failed, rc=%d", rc);
                                return buf.getString(0, StandardCharsets.UTF_8.name());
                            } finally {
                                AEDisposeDesc(text);
                            }
                        } finally {
                            OSADispose(component, resultId);
                        }
                    } finally {
                        OSADispose(component, scriptId);
                    }
                } finally {
                    closeComponent(component);
                }
            } finally {
                AEDisposeDesc(scriptTextDesc);
            }
        } finally {
            AEDisposeDesc(resultDesc);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static Pointer OSADisplay(Pointer component, int resultId, String type) {
        Pointer aeDesc = new Memory(12);
        int rc = Carbon.INSTANCE.OSADisplay(component, resultId, make4CC(type), 0, aeDesc);
        if (rc != 0) throw new GeneralException("OSADisplay failed with rc=%d", rc);
        return aeDesc;
    }

    private static void OSADispose(Pointer component, int scriptId) {
        int rc = Carbon.INSTANCE.OSADispose(component, scriptId);
        if (rc != 0) throw new GeneralException("OSADispose failed with rc=%d", rc);
    }

    private static int OSACompile(Memory scriptTextDesc, Pointer component) {
        IntByReference scriptId = new IntByReference(0);
        int rc = Carbon.INSTANCE.OSACompile(component, scriptTextDesc, 0, scriptId);
        if (rc != 0) throw new GeneralException("OSACompile failed with rc=%d", rc);
        return scriptId.getValue();
    }

    public static int OSAExecute(Pointer scriptingComponent, int compiledScriptID) {
        IntByReference resultingScriptValueID = new IntByReference(0);
        int rc =
                Carbon.INSTANCE.OSAExecute(
                        scriptingComponent, compiledScriptID, 0, 0, resultingScriptValueID);
        if (rc != 0) throw new GeneralException("OSAExecute failed with rc=%d", rc);
        return resultingScriptValueID.getValue();
    }

    private static void closeComponent(Pointer component) {
        int rc = CoreServices.INSTANCE.CloseComponent(component);
        if (rc != 0) throw new GeneralException("CloseComponent failed: rc=%d", rc);
    }

    @SuppressWarnings("SameParameterValue")
    private static Pointer openDefaultComponent(String type, String subtype) {
        Pointer ci = new Memory(Native.POINTER_SIZE);
        int rc = CoreServices.INSTANCE.OpenADefaultComponent(make4CC(type), make4CC(subtype), ci);
        if (rc != 0)
            throw new GeneralException(
                    "OpenADefaultComponent(%s,%s) failed rc=%d", type, subtype, rc);
        return ci.getPointer(0);
    }

    public static boolean isValid4CC(String code) {
        if (code.length() != 4) return false;
        for (int i = 0; i < code.length(); i++) if (code.charAt(i) >= 256) return false;
        return true;
    }

    public static int make4CC(String code) {
        if (!isValid4CC(code)) throw new GeneralException("Not a valid FourCC: '%s'", code);
        return ((code.charAt(0)) << 24)
                | ((code.charAt(1)) << 16)
                | ((code.charAt(2)) << 8)
                | ((code.charAt(3)));
    }

    public static Memory AECreateDesc(String descType, Pointer data, long dataSize) {
        Memory resultData = new Memory(12);
        int rc = CoreServices.INSTANCE.AECreateDesc(make4CC(descType), data, dataSize, resultData);
        if (rc != 0) throw new GeneralException("AECreateDesc returned OS error #%d", rc);
        return resultData;
    }

    public static void AEDisposeDesc(Pointer aeDesc) {
        int rc = CoreServices.INSTANCE.AEDisposeDesc(aeDesc);
        if (rc != 0) throw new GeneralException("AEDisposeDesc returned OS error #%d", rc);
    }

    public interface Carbon extends Library {
        Carbon INSTANCE = Native.load("Carbon", Carbon.class);

        int OSACompile(
                Pointer componentInstance,
                Pointer scriptTextDesc,
                int mode,
                IntByReference scriptId);

        int OSAExecute(
                Pointer scriptingComponent,
                int compiledScriptID,
                int contextID,
                int modeFlags,
                IntByReference resultingScriptValueID);

        int OSADisplay(
                Pointer scriptingComponent,
                int scriptValueID,
                int desiredType,
                int modeFlags,
                Pointer resultingText);

        int OSADispose(Pointer component, int scriptId);
    }

    public interface CoreServices extends Library {
        CoreServices INSTANCE = Native.load("CoreServices", CoreServices.class);

        int AECreateDesc(int descType, Pointer data, long dataSize, Pointer resultData);

        int AEDisposeDesc(Pointer desc);

        long AEGetDescDataSize(Pointer desc);

        int AEGetDescData(Pointer desc, Pointer dataPtr, long maxSize);

        int OpenADefaultComponent(int componentType, int componentSubType, Pointer ci);

        int CloseComponent(Pointer ci);
    }
}
