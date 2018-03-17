package StandardSim;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StdSim {

    private int numberOfTraders = 16;
    private ExecutorService traderPool = Executors.newFixedThreadPool(numberOfTraders);
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private ThreadLocal<Trader> trader = ThreadLocal.withInitial(Trader::new);
    private ThreadLocal<Boolean> update = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private Future<Long>[] profits = new Future[numberOfTraders];
    private boolean shouldRun = true;

    public int exec(String csvUrl) {
        AtomicInteger numOfLoops = new AtomicInteger(0);
        Market.getInstance().init(csvUrl);

        // execute market
        scheduler.scheduleAtFixedRate(() -> {
            if (!shouldRun) {
                scheduler.shutdown();
            }
            try {
                Market.getInstance().modifyAll();
                update = ThreadLocal.withInitial(() -> Boolean.TRUE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 500, 500, TimeUnit.MILLISECONDS);

        Callable<Long> task = () -> {
            while (shouldRun) {
                switch(ThreadLocalRandom.current().nextInt(2)) {
                    case 0:
                        try {
                            trader.get().purchaseStock();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        try {
                            trader.get().sellStock();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                }
                if (update.get()) {
                    trader.get().updateValues();
                    update.set(Boolean.FALSE);
                }
                numOfLoops.incrementAndGet();
            }
            return trader.get().getProfit();
        };

        for (int i = 0; i < numberOfTraders; i++) {
            profits[i] = traderPool.submit(task);
        }

        scheduler.schedule(() -> shouldRun = false, 5, TimeUnit.SECONDS);

        for (int i = 0; i < numberOfTraders; i++) {
            try {
                System.out.println("Trader " + i + " Profit: " + profits[i].get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        traderPool.shutdown();
        return numOfLoops.get();
    }

    public static void main(String[] args) {
        StdSim ss = new StdSim();
        int numOfLoops = ss.exec(
                "http://www.nasdaq.com/screening/companies-by-industry.aspx?exchange=NASDAQ&render=download");
        System.out.println("Number of Loops: " + numOfLoops);
    }

}
