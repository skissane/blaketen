package blaketen;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javax.sound.sampled.*;

public class RecordedSpeech {
    private static class ClipHolder {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();
        private final Clip clip;

        public ClipHolder(Clip clip) {
            this.clip = Objects.requireNonNull(clip);
        }
    }

    private static final ConcurrentMap<String, ClipHolder> CLIPS = new ConcurrentHashMap<>();
    private static final String RECORDED_SPEECH_DISABLED = "RECORDED_SPEECH_DISABLED";

    private static String getKey(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        if (isDisabled()) return null;
        if (s.length() == 1) {
            s = s.toUpperCase();
            if (s.charAt(0) >= 'A' && s.charAt(0) <= 'Z') {
                return ("letter_" + s);
            }
        }
        if (isValidInt(s)) {
            return ("number_" + s);
        }
        return null;
    }

    public static boolean canSay(String s) {
        String key = getKey(s);
        return key != null && hasClip(key);
    }

    private static boolean hasClip(String key) {
        try (InputStream is = getAudioResource(key)) {
            return is != null;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static boolean isValidInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static boolean isDisabled() {
        return "1".equals(System.getenv(RECORDED_SPEECH_DISABLED));
    }

    public static void say(String s) {
        if (!canSay(s))
            throw new IllegalArgumentException(String.format("Don't know how to say '%s'", s));
        String key = getKey(s);
        ClipHolder clip = getClip(key);
        clip.lock.lock();
        try {
            clip.clip.stop();
            clip.clip.setMicrosecondPosition(0);
            clip.clip.start();
            clip.condition.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            clip.lock.unlock();
        }
    }

    private static ClipHolder getClip(String key) {
        ClipHolder existing = CLIPS.get(key);
        if (existing != null) return existing;
        try (InputStream is = getAudioResource(key);
                AudioInputStream ais = AudioSystem.getAudioInputStream(is)) {
            Clip clip = AudioSystem.getClip();
            ClipHolder holder = new ClipHolder(clip);
            clip.open(ais);
            clip.addLineListener(
                    event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            holder.lock.lock();
                            try {
                                holder.condition.signal();
                            } finally {
                                holder.lock.unlock();
                            }
                        }
                    });
            CLIPS.put(key, holder);
            return holder;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (LineUnavailableException | UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        }
    }

    private static InputStream getAudioResource(String key) {
        try (InputStream inp =
                RecordedSpeech.class
                        .getClassLoader()
                        .getResourceAsStream("blaketen/sounds/" + key + ".wav")) {
            if (inp == null) return null;
            byte[] buf = new byte[4096];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (true) {
                int r = inp.read(buf);
                if (r < 0) break;
                baos.write(buf, 0, r);
            }
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
