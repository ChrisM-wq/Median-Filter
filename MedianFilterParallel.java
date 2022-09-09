import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.*;

public class MedianFilterParallel extends RecursiveAction  {

	private static final double CUTOFF = 600;

	int[][] pixels;
	BufferedImage img;
	int filter;
	int xLow;
	int xHigh;
	int yLow;
	int yHigh;
	double xScale;
	double yScale;

	MedianFilterParallel(int[][] pixelsInput, BufferedImage imgInput, int filterInput, int xLowInput, int xHighInput, int yLowInput, int yHighInput, double xSc, double ySc) { 
		pixels = pixelsInput;
		img = imgInput;
		filter = filterInput;
		xLow = xLowInput;
		xHigh = xHighInput;
		yLow = yLowInput;
		yHigh = yHighInput;

		xScale = xSc;
		yScale = ySc;
	}

	protected void compute(){

		int xdynamicScale = (int)(CUTOFF*xScale);
		int ydynamicScale = (int)(CUTOFF*yScale);

		if((xHigh - xLow) <= ydynamicScale && (yHigh - yLow) <= xdynamicScale) { 

			int Start = (filter-1)/2;
			
			int[] red = new int[filter*filter];
            int[] green = new int[filter*filter];
            int[] blue = new int[filter*filter]; 

            //go through each pixel needing smoothing
            for (int i = xLow; i < xHigh; i++) {					
                for( int j = yLow; j < yHigh; j++ ) {
					
                    //calc for smoothing at a point
                    int index = 0;
					//extract RGB components here
                    for (int k = i - Start; k <= i + Start; k++) {
                        for (int l = j - Start; l <= j + Start; l++) {
                            Color tempColor = new Color(pixels[k][l]);
							red[index] = tempColor.getRed();
                        	green[index] = tempColor.getGreen();
                        	blue[index] = tempColor.getBlue();
                            index++;
                        }
                    }
                    
                    //sorting RGB components and finding median values
                    Arrays.sort(red);
                    Arrays.sort(green);
                    Arrays.sort(blue);
                    //int alpha = ((img.getRGB(i, j))>>24) & 0xff;
                    int redMedian = red[(red.length-1)/filter]; 
                    int greenMedian = green[(green.length-1)/filter]; 
                    int blueMedian = blue[(blue.length-1)/filter]; 
                    int medianPixel = new Color(redMedian, greenMedian, blueMedian).getRGB();
                    img.setRGB(j, i, medianPixel); 
                }
            }

		}
		else {

			if ((xHigh - xLow) <= ydynamicScale ) {
				MedianFilterParallel topRight = new MedianFilterParallel(pixels, img, filter, xLow, xHigh, yLow, (yHigh+yLow)/2, xScale,  yScale);//first half
				MedianFilterParallel bottomRight = new MedianFilterParallel(pixels, img, filter, xLow, xHigh, (yHigh+yLow)/2, yHigh, xScale,  yScale);//first half
				topRight.fork();
				bottomRight.fork();
				topRight.join();
				bottomRight.join();

			} else if ((yHigh - yLow) <= xdynamicScale){
				MedianFilterParallel topLeft = new MedianFilterParallel(pixels, img, filter, xLow, (xLow+xHigh)/2, yLow, yHigh, xScale,  yScale);//first half
				MedianFilterParallel bottomLeft = new MedianFilterParallel(pixels, img, filter, (xLow+xHigh)/2, xHigh, yLow, yHigh, xScale,  yScale);//second half
				topLeft.fork();
				bottomLeft.fork();
				topLeft.join();
				bottomLeft.join();
			}
			else {
				MedianFilterParallel topRight = new MedianFilterParallel(pixels, img, filter, xLow, xHigh, yLow, (yHigh+yLow)/2, xScale,  yScale);//first half
				MedianFilterParallel bottomRight = new MedianFilterParallel(pixels, img, filter, xLow, xHigh, (yHigh+yLow)/2, yHigh, xScale,  yScale);//first half
				MedianFilterParallel topLeft = new MedianFilterParallel(pixels, img, filter, xLow, (xLow+xHigh)/2, yLow, yHigh, xScale,  yScale);//first half
				MedianFilterParallel bottomLeft = new MedianFilterParallel(pixels, img, filter, (xLow+xHigh)/2, xHigh, yLow, yHigh, xScale,  yScale);//second half
				topLeft.fork();
				topRight.fork();
				bottomRight.fork();
				bottomLeft.fork();

				topLeft.join();
				topRight.join();
				bottomRight.join();
				bottomLeft.join();

			}
		}
	}

	public static void main(String[] args) {
	
		/* Input needed through arguments
		 * 1. File input name
		 * 2. File output name
		 * 3. Filter size
		 */
		String fileInputName = null;
		String fileOutputName = null;
		int filter = 0;

		/* Validating user input */
		try {
			fileInputName = args[0];
			fileOutputName = args[1];
			filter = Integer.parseInt(args[2]);
		} catch (Exception e) {
			System.out.println("\nInvalid arguments/input!");
			System.out.println("1. File input name, 2. File output name, 3. Filter size.");
			System.out.println("Example : 'input.jpg output.jpg 3'\n");
			System.exit(0);
		}

		/* Validating file input */
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(fileInputName));
		} catch (Exception e) {
			System.out.println("\nCan't read input file!");
			System.out.println("Please check for file spelling and/or file extention type.\n");
			System.exit(0);
		}

		/* Create and populate array of all pixels in the input image */
		int pictureFile[][] = new int [img.getHeight()][img.getWidth()];
        for( int i = 0; i < img.getHeight(); i++ ){
            for( int j = 0; j < img.getWidth(); j++ ){
                pictureFile[i][j] = img.getRGB( j, i );
            }
        }		

		int borderBuffer = (filter-1)/2;
		int xHigh = img.getHeight() - borderBuffer;
		int xLow = borderBuffer;
		int yHigh = img.getWidth() - borderBuffer;
		int yLow = borderBuffer;

		double xScaleVal = ((double)(yHigh))/xHigh;
		double yScaleVal = ((double)(xHigh))/yHigh;

		long startTime = System.currentTimeMillis(); // Begin timing parallel seection

		/* Framework for parallel structure */
		MedianFilterParallel sayhello = new MedianFilterParallel(pictureFile, img, filter, xLow, xHigh, yLow, yHigh, xScaleVal, yScaleVal); //the task to be done, divide and conquer
		ForkJoinPool pool = new ForkJoinPool(); //the pool of worker threads
		pool.invoke(sayhello); //start everything running - give the task to the pool

		long endTime = System.currentTimeMillis(); // End timing parallel seection
		System.out.println("That took " + (endTime - startTime) + " milliseconds");


		/* Validating output file
		 * Please note to change file output type to match file input type!
		 */
		try {
			File outputfile = new File(fileOutputName);
            ImageIO.write(img, "jpg", outputfile);
		} catch (Exception e) {
			System.out.println("\nUnable to output file!");
			System.out.println("Please check file extention type.\n");
			System.exit(0);
		}

		}
	}


