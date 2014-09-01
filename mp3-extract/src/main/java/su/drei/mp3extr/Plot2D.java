package su.drei.mp3extr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.SamplingXYLineRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

public class Plot2D extends ApplicationFrame {

	public static void main(String[] s){
		byte b1=-5;
		byte b2=-10;
		ByteBuffer bb = ByteBuffer.wrap(new byte[]{b1,b2});

	    bb.order(ByteOrder.BIG_ENDIAN);

    	System.out.println(bb.getShort());
    	int i1=b1;
    	int i2=b2;
    	
    	System.out.println((i1<<8)|(i2&0xff));
	}
	
	public Plot2D(final String title, List<? extends Number> bytes) {

        super(title);

        final XYDataset dataset = createDataset(bytes);
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);

    }
	
	public Plot2D(final String title, double[] bytes, double xResize) {

        super(title);

        final XYDataset dataset = createDataset(bytes, xResize);
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);

    }
	
	 private JFreeChart createChart(final XYDataset dataset) {
	        
	        // create the chart...
	        final JFreeChart chart = ChartFactory.createXYLineChart(
	            "Line Chart Demo 6",      // chart title
	            "X",                      // x axis label
	            "Y",                      // y axis label
	            dataset,                  // data
	            PlotOrientation.VERTICAL,
	            true,                     // include legend
	            true,                     // tooltips
	            false                     // urls
	        );

	        chart.setBackgroundPaint(Color.white);

	        // get a reference to the plot for further customisation...
	        final XYPlot plot = chart.getXYPlot();
	        plot.setBackgroundPaint(Color.lightGray);
	        plot.setDomainGridlinePaint(Color.white);
	        plot.setRangeGridlinePaint(Color.white);

	        final SamplingXYLineRenderer renderer = new SamplingXYLineRenderer();
	        renderer.setSeriesStroke(0, new BasicStroke(0.5f), true);
	        plot.setRenderer(renderer);

	        
	        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
	        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	                
	        return chart;
	        
	    }
	 
	   
	   
	   private XYDataset createDataset(List<? extends Number> bytes) {
	        System.out.println("Preparing dataset");
	        final XYSeries series1 = new XYSeries("First");
	        int pos = 0;
	        for(Number b: bytes){
	        	series1.add(++pos, b);
	        }

	        final XYSeriesCollection dataset = new XYSeriesCollection();
	        dataset.addSeries(series1);

	        System.out.println("Preparing dataset. Done.");
	                
	        return dataset;
	        
	    }
	   
	   private XYDataset createDataset(double[] bytes, double xResize) {
	        System.out.println("Preparing dataset");
	        final XYSeries series1 = new XYSeries("First");
	        int pos = 0;
	        for(int i=0;i<bytes.length/2;i++){
	        	series1.add(++pos*xResize, bytes[i]);
	        }


	        final XYSeriesCollection dataset = new XYSeriesCollection();
	        dataset.addSeries(series1);

	        System.out.println("Preparing dataset. Done.");
	                
	        return dataset;
	        
	    }
	   
}
