import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.*;

public class MedianFilterSerial {
    
    public static void main(String[] args) throws IOException {

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



            //get dimensions
            int maxHeight = img.getHeight();
            int maxWidth = img.getWidth();

            //create 2D Array for new picture
            int pictureFile[][] = new int [maxHeight][maxWidth];
            for( int i = 0; i < maxHeight; i++ ){
                for( int j = 0; j < maxWidth; j++ ){
                    pictureFile[i][j] = img.getRGB( j, i );
                }
            }

            int Start = (filter-1)/2;

            //go through each pixel needing smoothing

            int[] red = new int[filter*filter];
            int[] green = new int[filter*filter];
            int[] blue = new int[filter*filter];

            long startTime = System.currentTimeMillis();

            for (int i = Start; i < maxHeight - Start; i++) {
                for( int j = Start; j < maxWidth - Start; j++ ) {
                    
                    //extract RGB components here
                    Color[] myColors = new Color[filter*filter];
                    int index = 0;
                    for (int k = i - Start; k <= i + Start; k++) {
                        for (int l = j - Start; l <= j + Start; l++) {
                            Color color = new Color(pictureFile[k][l]);
                            red[index] = color.getRed();
                            green[index] = color.getGreen();
                            blue[index] = color.getBlue();
                            index++;
                        }
                    }

                    //sorting RGB components and finding median values
                    Arrays.sort(red);
                    Arrays.sort(green);
                    Arrays.sort(blue);
                
                    int redMedian = red[(red.length-1)/filter]; 
                    int greenMedian = green[(green.length-1)/filter]; 
                    int blueMedian = blue[(blue.length-1)/filter]; 

                    int medianPixel = new Color(redMedian, greenMedian, blueMedian).getRGB();
                    img.setRGB(j, i, medianPixel); 
    
                    // end of array
                }
            }

            long endTime = System.currentTimeMillis();

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