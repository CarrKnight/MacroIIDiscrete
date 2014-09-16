package model.utilities.stats.regression;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * csv used to test whether regressions work or not
 */
public class RegressionTestData {
    double[] x;

    public double[] getX() {
        return x;
    }

    double[] y;

    public double[] getY() {
        return y;
    }

    double[] weights;

    public double[] getWeights() {
        return weights;
    }

    double[] x1;

    public double[] getX1() {
        return x1;
    }

    double[] y1;

    public double[] getY1() {
        return y1;
    }

    double[] weights1;

    public double[] getWeights1() {
        return weights1;
    }

    public RegressionTestData() {
    }

    void initializeData() throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(getClass().getResource("/recursive.csv").getFile()))) {

            List<String[]> lines = reader.readAll();
            x = new double[lines.size() - 1];
            y = new double[lines.size() - 1];
            weights = new double[lines.size() - 1];
            for (int i = 1; i < lines.size(); i++) {
                String[] line = lines.get(i);

                x[i - 1] = Double.parseDouble(line[1]);
                y[i - 1] = Double.parseDouble(line[2]);
                weights[i - 1] = Double.parseDouble(line[3]);

            }


        }

        try (CSVReader reader = new CSVReader(new FileReader(getClass().getResource("/tolearn.csv").getFile()))) {

            List<String[]> lines = reader.readAll();
            x1 = new double[lines.size() - 1];
            y1 = new double[lines.size() - 1];
            weights1 = new double[lines.size() - 1];
            for (int i = 1; i < lines.size(); i++)   //skip header
            {
                String[] line = lines.get(i);

                x1[i - 1] = Double.parseDouble(line[1]);
                y1[i - 1] = Double.parseDouble(line[2]);
                weights1[i - 1] = Double.parseDouble(line[3]);

            }


        }
    }
}