package model.utilities.stats.regression;

import au.com.bytecode.opencsv.CSVReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.nio.file.Paths;
import java.util.List;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-11-10
 * @see
 */
public class RecursiveLinearRegressionTest {

    private double[] x;

    private double[] y;

    private double[] weights;




    @Before
    public void setUp() throws Exception
    {

        try (CSVReader reader = new CSVReader(new FileReader(Paths.get("runs", "recursive.csv").toFile())))
        {

            List<String[]> lines = reader.readAll();
            x = new double[lines.size()];
            y = new double[lines.size()];
            weights = new double[lines.size()];
            for(int i=1; i< lines.size(); i++)
            {   String[] line = lines.get(i);

                x[i] = Double.parseDouble(line[1]);
                y[i] = Double.parseDouble(line[2]);
                weights[i] = Double.parseDouble(line[3]);

            }


        }



    }

    @Test
    public void testUnweightedRegression() throws Exception {
        //2 dimensions, x + intercept
        RecursiveLinearRegression regression = new RecursiveLinearRegression(2);

        for(int i=0; i < x.length; i++)
        {
            regression.addObservation(1,y[i],1,x[i]);
        }
        Assert.assertEquals(2,regression.getBeta()[1],.1d);
        Assert.assertEquals(4.992,regression.getBeta()[0],.1d);


    }

    @Test
    public void testWeightedRegression() throws Exception {
        //2 dimensions, x + intercept
        RecursiveLinearRegression regression = new RecursiveLinearRegression(2);

        for(int i=0; i < x.length; i++)
        {
            regression.addObservation(weights[i],y[i],1,x[i]);
        }
        Assert.assertEquals(1.997189,regression.getBeta()[1],.1d);
        Assert.assertEquals(4.994421,regression.getBeta()[0],.1d);


    }
}
