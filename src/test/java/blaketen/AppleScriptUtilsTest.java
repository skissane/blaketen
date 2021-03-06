package blaketen;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.jna.Memory;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AppleScriptUtilsTest {

    @BeforeAll
    public static void beforeClass() {
        Assumptions.assumeTrue(Platform.isMac());
    }

    @Test
    public void AECreateDesc() {
        Memory aeDesc = AppleScriptUtils.AECreateDesc("null", Pointer.NULL, 0);
        assertNotNull(aeDesc);
        AppleScriptUtils.AEDisposeDesc(aeDesc);
    }

    @Test
    public void eval() {
        assertEquals("42", AppleScriptUtils.eval("return 42"));
        assertEquals("\"Hello\"", AppleScriptUtils.eval("return \"Hello\""));
    }
}
