package nl.saxion.itech.server.util;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;

/**
 * A logger class that performs all the logging for the server.
 * It is created as a singleton to be accessible throughout every
 * class of the server without specifically being injected.
 */
public class Logger {
    private static volatile Logger instance;

    private PrintWriter out; // Where we send our logging output to

    private Logger() {
    }

    /**
     * When this method is called for the first time, the init()
     * method must be used to instantiate the output stream of the
     * logger.
     * @return the Logger instance
     */
    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger .class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    /**
     * Instantiates the output stream of the logger.
     * @param out the output stream to direct the logs to.
     */
    public synchronized void init(OutputStream out) {
        this.out = new PrintWriter(out);
    }

    public void logMessage(String message) {
        assert out != null : "Logger has not been initiated";

        this.out.printf("[%s] %s\n", new Date(), message);
        this.out.flush();
    }

    public void logError(String error) {
        assert out != null : "Logger has not been initiated";

        this.out.printf("[Error] %s\n", error);
        this.out.flush();
    }
}
