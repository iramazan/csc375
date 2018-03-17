package Benchmark;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Benchmark {

    private static final String NASDAQUrl =
            "http://www.nasdaq.com/screening/companies-by-industry.aspx?exchange=NASDAQ&render=download";
    private static final String NYSEUrl =
            "http://www.nasdaq.com/screening/companies-by-industry.aspx?exchange=NYSE&render=download";
    private static final int samples = 200;

    public static int testCustomSimNASDAQ() {
        int[] dataSet = new int[samples];
        for (int i = 0; i < samples; i++) {
            System.out.println("\nTEST CUSTOM SIM NASDAQ -- LOOP " + Integer.toString(i+1));
            CustomSim.CustomSim cs = new CustomSim.CustomSim();
            dataSet[i] = cs.exec(NASDAQUrl);
        }
        int average = Arrays.stream(dataSet).reduce((x,y) -> x + y).getAsInt();
        return average / samples;
    }

    public static int testCustomSimNYSE() {
        int[] dataSet = new int[samples];
        for (int i = 0; i < samples; i++) {
            System.out.println("\nTEST CUSTOM SIM NYSE -- LOOP " + Integer.toString(i+1));
            CustomSim.CustomSim cs = new CustomSim.CustomSim();
            dataSet[i] = cs.exec(NYSEUrl);
        }
        int average = Arrays.stream(dataSet).reduce((x,y) -> x + y).getAsInt();
        return average / samples;
    }

    public static int testStdSimNASDAQ() {
        int[] dataSet = new int[samples];
        for (int i = 0; i < samples; i++) {
            System.out.println("\nTEST STANDARD SIM NASDAQ -- LOOP " + Integer.toString(i+1));
            StandardSim.StdSim ss = new StandardSim.StdSim();
            dataSet[i] = ss.exec(NASDAQUrl);
        }
        int average = Arrays.stream(dataSet).reduce((x,y) -> x + y).getAsInt();
        return average / samples;
    }

    public static int testStdSimNYSE() {
        int[] dataSet = new int[samples];
        for (int i = 0; i < samples; i++) {
            System.out.println("\nTEST STANDARD SIM NYSE -- LOOP " + Integer.toString(i+1));
            StandardSim.StdSim ss = new StandardSim.StdSim();
            dataSet[i] = ss.exec(NYSEUrl);
        }
        int average = Arrays.stream(dataSet).reduce((x,y) -> x + y).getAsInt();
        return average / samples;
    }

    public static void main(String[] args) {
        FileWriter fw = null;
        CSVPrinter cp = null;
        try {
            fw = new FileWriter("benchmark-data.csv");
            cp = new CSVPrinter(fw, CSVFormat.DEFAULT.withRecordSeparator("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Benchmark StdSimNASDAQ
        int stdSimNasdaqResult = testStdSimNASDAQ();
        List<String> record1 = new ArrayList<>();
        record1.add("StdSimNASDAQResult");
        record1.add(Integer.toString(stdSimNasdaqResult));
        try {
            cp.printRecord(record1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Benchmark StdSimNYSE
        int stdSimNyseResult = testStdSimNYSE();
        List<String> record2 = new ArrayList<>();
        record2.add("StdSimNYSEResult");
        record2.add(Integer.toString(stdSimNyseResult));
        try {
            cp.printRecord(record2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Benchmark CustomSimNASDAQ
        int customSimNasdaqResult = testCustomSimNASDAQ();
        List<String> record3 = new ArrayList<>();
        record3.add("CustomSimNASDAQResult");
        record3.add(Integer.toString(customSimNasdaqResult));
        try {
            cp.printRecord(record3);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Benchmark CustomSimNYSE
        int customSimNyseResult = testCustomSimNYSE();
        List<String> record4 = new ArrayList<>();
        record4.add("CustomSimNYSEResult");
        record4.add(Integer.toString(customSimNyseResult));
        try {
            cp.printRecord(record4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // close IO Stuff
        try {
            cp.flush();
            cp.close();
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
