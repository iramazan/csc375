package CustomSim;

import ConcurrentUtil.HashTable;
import ConcurrentUtil.Tuple;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;

public class Market {

    private HashTable stocks;

    public Market() {
        stocks = new HashTable();
    }

    // Instance is initialized
    private static class InstanceHolder {
        static final Market INSTANCE = new Market();
    }

    public void load(String csvUrl) {
        // get stock data
        Iterable<CSVRecord> record = null;
        BufferedReader input = null;
        try {
            URL dataURL =new URL(csvUrl);
            input = new BufferedReader(new InputStreamReader(dataURL.openStream()));
            input.readLine(); // read the header line to skip it
            record = CSVFormat.DEFAULT.withIgnoreHeaderCase().parse(input);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Reading stock data failed");
            System.exit(1);
        }
        // put stock data into table
        for (CSVRecord r : record) {
            try {
                stocks.add(r.get(0), Float.parseFloat(r.get(2)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NumberFormatException en) {
                // do nothing
            }
        }
        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to get singleton instance object.
     * @return Singleton object of this class
     */
    public static Market getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Get the number of stocks in this market.
     * @return The number of stocks in this market
     */
    public int getSize() {
        return stocks.getSize();
    }

    /**
     * Get the value of a stock.
     * @param ticker String ticker symbol of a stock
     * @return Price of a stock. Zero if stock is not in the Table
     * @throws InterruptedException
     */
    public float getStock(String ticker) throws InterruptedException {
        return stocks.getValue(ticker);
    }


    /**
     * Get a random stock and it's value.
     * @return A tuple containing a stock and it's value
     * @throws InterruptedException Thread is interrupted
     */
    public Tuple<String, Float> getRandom() throws InterruptedException {
        Object[] keys = stocks.getKeys().toArray();
        int index = ThreadLocalRandom.current().nextInt(stocks.getSize());
        String key = (String) keys[index];
        return new Tuple<>(key, stocks.getValue(key));
    }

    /**
     * Randomly modify all stock prices with gaussian distributed values.
     * @throws InterruptedException
     */
    public void modifyAll() throws InterruptedException {
        stocks.modifyValues();
    }

}
