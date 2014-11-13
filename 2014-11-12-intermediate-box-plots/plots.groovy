/**
 * Reddit daily programmer #188
 * http://www.reddit.com/r/dailyprogrammer/comments/2m48nn/20141112_challenge_188_intermediate_box_plot/
 *
 * A box plot is a convenient way of representing a set of univariate
 * (one-variable) numerical data, while showing some useful statistical
 * info about it at the same time. To understand what a box plot represents 
 * you need to learn about quartiles.
 *
 * Quartiles
 * -----------
 * Quartiles show us some info on the distribution of data in a data set.
 * For example, here's a made-up data set representing the number of lines
 * of code in 30 files of a software project, arranged into order.
 *
 * 7 12 21 28 28 29 30 32 34 35 35 36 38 39 40 40 42 44 45 46 47 49 50 53 55
 * 56 59 63 77 191
 *
 * The three quartiles can be found at the quarter intervals of a data set.
 * For this example, the number of data items is 30, so the lower quartile
 * (Q1) is item number (30/4=8 - round up) which the value is 32. The median
 * quartile (Q2) is item number (2*30/4=15) which the value is 40. The upper
 * quartile (Q3) is item number (3*30/4=23 - round up) which the value is 50.
 * The bit between Q1 and Q3 is called the inter quartile range or IQR. To 
 * demonstrate the fact that this splits the data set into 'quarters' the 
 * quartiles here are displayed.
 *
 * 7 12 21 28 28 29 30 32 34 35 35 36 38 39 40 40 42 44 45 46 47 49 50 53 55 56 59 63 80 191
 *                     ||                   ||                      ||
 * --- 1st quarter ----Q1--- 2nd quarter ---Q2---- 3rd quarter -----Q3--- 4th quarter -----
 *                      \           inter quartile range            /
 *
 * The value of the IQR here is 50-32=18 (ie. Q3-Q1.) This forms the 'box'
 * part of the box plot, with the line in the moddle of it representing the
 * median Q2 point. The 'whiskers' of the box plot are also fairly easy to 
 * work out. They represent the rest of the data set that isn't an outlier 
 * (anomalous). For example, here the 191-line-long file is an anomaly among 
 * the rest, and the 7-ling-long file might be too. How do we say for sure 
 * what is an anomaly and what isn't? If the data point is at the lower end
 * of the data set, you work out if the value is less than 1.5 times the 
 * inter-quartile range from Q1 - ie. if x < Q1 - 1.5 * IQR. If the data
 * point is at the higher end of the data set, you work out of the value is 
 * more than 1.5 times the inter-quartile range from Q3 - ie. 
 * if x > Q3 + 1.5 * IQR. Here, for 7, Q1 - 1.5 * IQR is 32 - 27 = 5, 
 * and 7 > 5, so 7 is not an outlier. But for 191, Q3 + 1.5 * IQR 
 * is 50 + 27 = 77, and both 80 and 191 are greater than 77, so they 
 * are outliers. The end of the 'whiskers' on the box plot (the endmost bits)
 * are the first and last values that aren't outliers - any outlying points
 * are represented as crosses x outside of the plot.
 *
 * Note: in reality, a better method than rounding up the quartile indices
 *       is usually used.
 *
 * Formal Inputs and Outputs
 * -----------------------------
 * Input Description
 * The program is to accept any number of numerical values, separated by
 * whitespace.
 *
 * Output Description
 * You are to output the box plot for the input data set. You have some
 * freedom as to how you draw the box plot - you could dynamically generate
 * an image, for example, or draw it ASCII style.
 */

import java.awt.*
import java.awt.image.*
import javax.imageio.ImageIO


new Plots().exec([
    1:"""
    7 12 21 28 28 29 30 32 34 35 35 36 38 39 40 40 42 44
    45 46 47 49 50 53 55 56 59 63 80 191
    """,
    2:"""
    2095 2180 1049 1224 1350 1567 1477 1598 1462  972 1198 1847
    2318 1460 1847 1600  932 1021 1441 1533 1344 1943 1617  978
    1251 1157 1454 1446 2182 1707 1105 1129 1222 1869 1430 1529
    1497 1041 1118 1340 1448 1300 1483 1488 1177 1262 1404 1514
    1495 2121 1619 1081  962 2319 1891 1169
    """])

public class Plots {

    def plotSize = [w:800, h:100, margin:100, boxHeight:50]

    def exec(dataSets) {
        dataSets.each { label, dataSet ->
            execOne(label, dataSet)
        }
    }

    def execOne(label, dataSetStr) {
        def dataSet = parseDataSetStr(dataSetStr)
        //println "dataSet.size=${dataSet.size()}"
        def quartileIndexes = findQartileIndexes(dataSet)
        //println "quartileIndexes=${quartileIndexes}"
        def q1 = dataSet[quartileIndexes.q1]
        def q2 = dataSet[quartileIndexes.q2]
        def q3 = dataSet[quartileIndexes.q3]
        def iqr = q3 - q1
        def median = q2

        def minInclusion =  q1 - 1.5 * iqr
        def maxInclusion =  q3 + 1.5 * iqr
        def plot = [:]
        plot.min = dataSet[0]
        plot.minOutliers = []
        plot.whiskersLeft = null
        plot.q1 = q1
        plot.median = median
        plot.q3 = q3
        plot.whiskersRight = null
        plot.maxOutliers = []
        plot.max = dataSet[-1]
        for (value in dataSet) {
            if (value < minInclusion) {
                plot.minOutliers << value
            } else if (plot.whiskersLeft == null) {
                plot.whiskersLeft = value
            }
            if (value > maxInclusion) {
                plot.maxOutliers << value
            } else {
                plot.whiskersRight = value
            }
        }
        createPlot(label, plot)
    }

    def createPlot(label, plot) {
        println plot
        BufferedImage bi = new BufferedImage(plotSize.w, plotSize.h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D img = bi.createGraphics();

        def font = new Font("TimesRoman", Font.BOLD, 10);
        img.setFont(font)
        def fontMetrics = img.getFontMetrics()
        img.setPaint(Color.black)
        /*
        String message = "www.java2s.com!";
        int stringWidth = fontMetrics.stringWidth(message);
        int stringHeight = fontMetrics.getAscent();
        img.drawString(message, (plotSize.w - stringWidth) / 2, plotSize.h / 2 + stringHeight / 4);
        */
        // Range of actual values
        def rangeSize = plot.max - plot.min
        // Where to start drawing
        def leftStart = plotSize.margin
        def topStart = plotSize.margin
        // Inside margins width
        def innerWidth = plotSize.w - (plotSize.margin * 2)
        def innerHeight = plotSize.h - (plotSize.margin * 2)
        // Number of pixels per data unit
        def pixPerUnit = innerWidth / rangeSize
        def textHeight = fontMetrics.getAscent()
        def centerTextTop = plotSize.h / 2 + textHeight / 4

        // Left outliers
        plot.minOutliers.each { value ->
            drawText(img, Color.red,
                "x ${value}", 
                leftStart + ((value - plot.min) * pixPerUnit), 
                centerTextTop)
        }
        // Left whiskers
        drawLine(img, Color.black,
            leftStart + ((plot.whiskersLeft - plot.min) * pixPerUnit), plotSize.margin,
            leftStart + ((plot.whiskersLeft - plot.min) * pixPerUnit), plotSize.h - plotSize.margin)
        drawLine(img, Color.black,
            leftStart + ((plot.whiskersLeft - plot.min) * pixPerUnit), plotSize.h / 2,
            leftStart + ((plot.q1 - plot.min) * pixPerUnit), plotSize.h / 2)
        drawText(img, Color.blue,
            "${plot.whiskersLeft}",
            leftStart + ((plot.whiskersLeft - plot.min) * pixPerUnit) + 5, centerTextTop - 5)

        // Primary box
        drawRect(img, Color.black,
            leftStart + ((plot.q1 - plot.min) * pixPerUnit), plotSize.h / 4,
            ((plot.q3 - plot.q1) * pixPerUnit), plotSize.h / 2);
        drawText(img, Color.blue,
            "${plot.q1}",
            leftStart + ((plot.q1 - plot.min) * pixPerUnit) + 5, centerTextTop - 5)
        drawText(img, Color.blue,
            "${plot.q3}",
            leftStart + ((plot.q3 - plot.min) * pixPerUnit) + 5, centerTextTop - 5)

        // Median
        drawLine(img, Color.black,
            leftStart + ((plot.median - plot.min) * pixPerUnit), plotSize.h / 4,
            leftStart + ((plot.median - plot.min) * pixPerUnit), (plotSize.h / 4) + (plotSize.h / 2))
        drawText(img, Color.blue,
            "${plot.median}",
            leftStart + ((plot.median - plot.min) * pixPerUnit) + 5, centerTextTop - 5)

        // Right whiskers
        drawLine(img, Color.black,
            leftStart + ((plot.whiskersRight - plot.min) * pixPerUnit), plotSize.margin,
            leftStart + ((plot.whiskersRight - plot.min) * pixPerUnit), plotSize.h - plotSize.margin)
        drawLine(img, Color.black,
            leftStart + ((plot.q3 - plot.min) * pixPerUnit), plotSize.h / 2,
            leftStart + ((plot.whiskersRight - plot.min) * pixPerUnit), plotSize.h / 2)
        drawText(img, Color.blue,
            "${plot.whiskersRight}",
            leftStart + ((plot.whiskersRight - plot.min) * pixPerUnit) + 5, centerTextTop - 5)
            
        // Right outliers
        plot.maxOutliers.each { value ->
            drawText(img, Color.red,
                "x ${value}", 
                leftStart + ((value - plot.min) * pixPerUnit), 
                centerTextTop)
        }


        ImageIO.write(bi, "PNG", new File("plot-${label}.png"));
    }

    def drawText(img, color, text, x, y) {
        println "Drawing ${text} at ${x}, ${y}"
        img.setPaint(color)
        img.drawString(text, x as Integer, y as Integer)
    }

    def drawLine(img, color, x1, y1, x2, y2) {
        println "Drawing ${x1}, ${y1} -> ${x2}, ${y2}"
        img.setPaint(color)
        img.drawLine(x1 as Integer, y1 as Integer, x2 as Integer, y2 as Integer)
    }

    def drawRect(img, color, x, y, w, h) {
        println "Drawing ${x}, ${y} w= ${x} h=${h}"
        img.setPaint(color)
        img.drawRect(x as Integer, y as Integer, w as Integer, h as Integer)
    }

    def parseDataSetStr(dataSetStr) {
        dataSetStr.trim().split("[ ]+").collect { it as Integer }.sort()
    }

    def findQartileIndexes(dataSet) {
        def indexes = [:]
        int size = dataSet.size()
        // -1 because indexes are 0 based
        indexes.q1 = (Math.ceil(size/4) as Integer) - 1
        indexes.q2 = (Math.ceil(size*2/4) as Integer) - 1
        indexes.q3 = (Math.ceil(size*3/4) as Integer) - 1
        indexes
    }
}
