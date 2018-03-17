package StandardSim;

import ConcurrentUtil.Tuple;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Trader {

    private ConcurrentHashMap<String, Float> stocks;
    private long profit = 0;

    /**
     * Create a new Trader object.
     */
    public Trader() {
        stocks = new ConcurrentHashMap<>();
    }

    /**
     * Get the current profit of this trader.
     * @return long profit of this trader
     */
    public long getProfit() {
        return profit;
    }

    private Tuple<String, Float> getRandom() throws InterruptedException {
        Object[] keys = stocks.keySet().toArray();
        int index = ThreadLocalRandom.current().nextInt(stocks.size());
        String key = (String) keys[index];
        return new Tuple<>(key, stocks.get(key));
    }

    /**
     * Purchase a random stock.
     * @throws InterruptedException Thread is interrupted
     */
    public void purchaseStock() throws InterruptedException {
        Market market = Market.getInstance();
        Tuple<String, Float> stockInfo = market.getRandom();
        stocks.put(stockInfo.key, stockInfo.value);
        profit -= stockInfo.value;
    }

    /**
     * Update the values of owned stocks from the market prices.
     */
    public void updateValues() {
        Market market = Market.getInstance();
        stocks.forEach((k, v) -> {
            try {
                stocks.put(k, market.getStock(k));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Sell a stock, removing it from this Trader's owned stocks.
     * @throws InterruptedException Thread is interrupted
     */
    public void sellStock() throws InterruptedException {
        if (stocks.size() > 0) {
            Tuple<String, Float> stock = getRandom();
            stocks.remove(stock.key);
            profit += stock.value;
        }
    }

}
