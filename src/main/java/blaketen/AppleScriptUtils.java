package blaketen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/** Interfaces with AppleScript. */
public class AppleScriptUtils {
    private static final ScriptEngine ase = getAppleScriptEngine();

    public static ScriptEngine getAppleScriptEngine() {
        try {
            Class<?> c = Class.forName("apple.applescript.AppleScriptEngineFactory");
            Object f = c.newInstance();
            Method m = c.getMethod("getScriptEngine", new Class[0]);
            return (ScriptEngine) m.invoke(f, new Object[0]);
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException
                | NoSuchMethodException
                | SecurityException
                | IllegalArgumentException
                | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Object eval(String script) {
        try {
            return ase.eval(script);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
}
