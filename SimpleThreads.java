public class SimpleThreads {

    static void threadMessage(String message) {
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    }

    private static class MessageLoop
        implements Runnable {
        public void run() {
            String importantInfo[] = {
                "Mares eat oats",
                "Does eat oats",
                "Little lambs eat ivy",
                "A kid will eat ivy too"
            };
            try {
                for (int i = 0; i < importantInfo.length; i++) {
                    Thread.sleep(4000);
                    threadMessage(importantInfo[i]);
                }
            } catch (InterruptedException e) {
                threadMessage("I wasn't done!");
            }
        }
    }

    private static class CpuIntensiveTask implements Runnable {
        private final int limit;

        CpuIntensiveTask(int limit) {
            this.limit = limit;
        }

        public void run() {
            threadMessage("Starting CPU-intensive prime computation up to " + limit);
            long sum = 0;
            int count = 0;

            for (int n = 2; n <= limit; n++) {
                if (Thread.interrupted()) {
                    threadMessage("CPU task interrupted! Computed " + count
                            + " primes so far (partial sum = " + sum + ").");
                    return;
                }

                if (isPrime(n)) {
                    sum += n;
                    count++;
                }
            }

            threadMessage("CPU task done! Found " + count
                    + " primes up to " + limit + " (sum = " + sum + ").");
        }

        private boolean isPrime(int n) {
            if (n < 2) return false;
            if (n == 2) return true;
            if (n % 2 == 0) return false;
            for (int i = 3; (long) i * i <= n; i += 2) {
                if (n % i == 0) return false;
            }
            return true;
        }
    }

    public static void main(String args[])
        throws InterruptedException {

        long patience = 1000 * 60 * 60;

        if (args.length > 0) {
            try {
                patience = Long.parseLong(args[0]) * 1000;
            } catch (NumberFormatException e) {
                System.err.println("Argument must be an integer.");
                System.exit(1);
            }
        }

        threadMessage("Starting MessageLoop thread");
        long startTime = System.currentTimeMillis();
        Thread t = new Thread(new MessageLoop());

        t.start();

        threadMessage("Waiting for MessageLoop thread to finish");

        while (t.isAlive()) {
            threadMessage("Still waiting...");
            t.join(1000);
            if (((System.currentTimeMillis() - startTime) > patience) && t.isAlive()) {
                threadMessage("Tired of waiting!");
                t.interrupt();
                t.join();
            }
        }
        threadMessage("Finally!");

        long cpuPatience = patience;
        threadMessage("Starting CPU-intensive thread (limit = 5000000, patience = "
                + cpuPatience + " ms)");
        long cpuStart = System.currentTimeMillis();
        Thread cpuThread = new Thread(new CpuIntensiveTask(5_000_000), "CpuTask");
        cpuThread.start();

        while (cpuThread.isAlive()) {
            threadMessage("CPU thread still running...");
            cpuThread.join(1000);
            if (((System.currentTimeMillis() - cpuStart) > cpuPatience) && cpuThread.isAlive()) {
                threadMessage("CPU task is taking too long — interrupting!");
                cpuThread.interrupt();
                cpuThread.join();
            }
        }
        threadMessage("CPU thread finished.");
    }
}
