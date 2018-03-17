package CustomSim;

import ConcurrentUtil.HashTable;
import ConcurrentUtil.Tuple;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Trader {

    private HashTable stocks;
    private long profit = 0;

    /**
     * Create a new Trader object.
     */
    public Trader() {
        stocks = new HashTable();
    }

    /**
     * Get the current profit of this trader.
     * @return long profit of this trader
     */
    public long getProfit() {
        return profit;
    }

    private Tuple<String, Float> getRandom() throws InterruptedException {
        Object[] keys = stocks.getKeys().toArray();
        int index = ThreadLocalRandom.current().nextInt(stocks.getSize());
        String key = (String) keys[index];
        return new Tuple<>(key, stocks.getValue(key));
    }

    /**
     * Purchase a random stock.
     * @throws InterruptedException Thread is interrupted
     */
    public void purchaseStock() throws InterruptedException {
        Market market = Market.getInstance();
        Tuple<String, Float> stockInfo = market.getRandom();
        stocks.add(stockInfo.key, stockInfo.value);
        profit -= stockInfo.value;
    }

    /**
     * Update the values of owned stocks from the market prices.
     */
    public void updateValues() throws InterruptedException {
        Market market = Market.getInstance();
        Set<String> keys = stocks.getKeys();
        keys.forEach(key -> {
            try {
                stocks.updatePair(key, market.getStock(key));
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
        if (stocks.getSize() > 0) {
            Tuple<String, Float> stock = getRandom();
            stocks.remove(stock.key);
            profit += stock.value;
        }
    }

}
