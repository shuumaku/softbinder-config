package dev.softbinder.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/** Runs one command through su. Blocking: call off the main thread. */
final class Root {
    private Root() {
    }

    /** Runs one command through su and returns its raw stdout (for binary output such as a PNG). */
    static byte[] bytes(String cmd) throws IOException {
        Process p = new ProcessBuilder("su", "-c", cmd).redirectErrorStream(false).start();
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        try {
            java.io.InputStream in = p.getInputStream();
            byte[] buf = new byte[65536];
            for (int n; (n = in.read(buf)) > 0;)
                out.write(buf, 0, n);
            if (!p.waitFor(10, TimeUnit.SECONDS)) {
                p.destroy();
                throw new IOException("su timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted");
        }
        if (p.exitValue() != 0)
            throw new IOException("su failed (" + p.exitValue() + ")");
        return out.toByteArray();
    }

    static String run(String cmd) throws IOException {
        Process p = new ProcessBuilder("su", "-c", cmd).redirectErrorStream(true).start();
        StringBuilder out = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        try {
            for (String line; (line = r.readLine()) != null;)
                out.append(line).append('\n');
        } finally {
            r.close();
        }
        try {
            if (!p.waitFor(10, TimeUnit.SECONDS)) {
                p.destroy();
                throw new IOException("su timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted");
        }
        if (p.exitValue() != 0)
            throw new IOException("su failed (" + p.exitValue() + "): " + out.toString().trim());
        return out.toString();
    }
}
