import java.util.logging.Logger;

public class TimingUtils {

    public static void measureExecutionTime(Runnable function, long maxDurationMs, String label, Logger logger) {
        long startTime = System.currentTimeMillis();
        try {
            function.run();
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            if (duration > maxDurationMs) {
                logger.info(String.format("Long %s call: %.3f seconds.", label, duration / 1000f));
            }
        }
    }
}