package cop5618;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.junit.BeforeClass;
import sun.awt.image.ByteBandedRaster;

public class FJBufferedImage extends BufferedImage {

    
    private ColorModel colorModel;
    private final WritableRaster raster=null;
    
    
    
    
   /**Constructors*/
	
	public FJBufferedImage(int width, int height, int imageType) {
		super(width, height, imageType);
	}

	public FJBufferedImage(int width, int height, int imageType, IndexColorModel cm) {
		super(width, height, imageType, cm);
	}

	public FJBufferedImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied,
			Hashtable<?, ?> properties) {
		super(cm, raster, isRasterPremultiplied, properties);
	}
	

	/**
	 * Creates a new FJBufferedImage with the same fields as source.
	 * @param source
	 * @return
	 */
	public static FJBufferedImage BufferedImageToFJBufferedImage(BufferedImage source){
	       Hashtable<String,Object> properties=null; 
	       String[] propertyNames = source.getPropertyNames();
	       if (propertyNames != null) {
	    	   properties = new Hashtable<String,Object>();
	    	   for (String name: propertyNames){properties.put(name, source.getProperty(name));}
	    	   }
	 	   return new FJBufferedImage(source.getColorModel(), source.getRaster(), source.isAlphaPremultiplied(), properties);		
	}
	
	@Override
        @SuppressWarnings("empty-statement")
	public void setRGB(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize) throws NullPointerException{
        /****IMPLEMENT THIS METHOD USING PARALLEL DIVIDE AND CONQUER*****/
            new RecurTask(xStart, yStart, w, h, rgbArray, offset, scansize).compute();
            
	}
	

	@Override
	public int[] getRGB(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize){
	       /****IMPLEMENT THIS METHOD USING PARALLEL DIVIDE AND CONQUER*****/
       new RecurAction(xStart, yStart, w, h, rgbArray, offset, scansize).compute();
       return null;
	}




private class RecurTask extends RecursiveAction {

    
    int xStart;
    int yStart;
    int width;
    int height;
    int[] inputArray;
    int offset;
    int scansize;
    

    public RecurTask(int p1,int p2,int p3,int p4,int[] p5,int p6,int p7) {
        
        
        this.xStart=p1;
        this.yStart=p2;
        this.width=p3;
        this.height=p4;
        this.inputArray=p5;
        this.offset=p6;
        this.scansize=p7;
    }

    protected void compute() {
        
         int dcIndex= 6;
int hbar= height/dcIndex;    
        if( height <= 6 )
                FJBufferedImage.super.setRGB(xStart, yStart, width, height,inputArray, offset, scansize);
            else{
            
          ForkJoinPool.commonPool().invoke(new RecurTask(xStart, yStart, width, hbar, inputArray, offset, scansize));
            ForkJoinPool.commonPool().invoke(new RecurTask(xStart, yStart+hbar, width, hbar, inputArray, (hbar*scansize)+offset, scansize));
            ForkJoinPool.commonPool().invoke(new RecurTask(xStart, yStart+(2*hbar), width, hbar, inputArray, (2*hbar*scansize)+offset, scansize));
            ForkJoinPool.commonPool().invoke(new RecurTask(xStart, yStart+(3*hbar), width, hbar, inputArray, (3*hbar*scansize)+offset, scansize));
            ForkJoinPool.commonPool().invoke(new RecurTask(xStart, yStart+(4*hbar), width, hbar, inputArray, (4*hbar*scansize)+offset, scansize));
            ForkJoinPool.commonPool().invoke(new RecurTask(xStart, yStart+(5*hbar), width, height-(5*hbar), inputArray, (5*hbar*scansize)+offset, scansize));
            
            
        } 
    }
}



private class RecurAction extends RecursiveAction{

    
    int result[];
    
    int xStart;
    int yStart;
    int width;
    int height;
    int[] inputArray;
    int offset;
    int scansize;
    
    public RecurAction(int p1,int p2,int p3,int p4,int[] p5,int p6,int p7) {
         

      this.xStart=p1;
        this.yStart=p2;
        this.width=p3;
        this.height=p4;
        this.inputArray=p5;
        this.offset=p6;
        this.scansize=p7;
    }

    @Override
    protected void compute() {
       int dcIndex= 6;
int hbar= height/dcIndex;    
        if( height <= 6 )
                FJBufferedImage.super.getRGB(xStart, yStart, width, height,inputArray, offset, scansize);
            else{
                    
            ForkJoinPool.commonPool().invoke(new RecurAction(xStart, yStart, width, hbar, inputArray, offset, scansize));
            ForkJoinPool.commonPool().invoke(new RecurAction(xStart, yStart+hbar, width, hbar, inputArray, (hbar*scansize)+offset, scansize));
            ForkJoinPool.commonPool().invoke(new RecurAction(xStart, yStart+(2*hbar), width, hbar, inputArray, (2*hbar*scansize)+offset, scansize));
            ForkJoinPool.commonPool().invoke(new RecurAction(xStart, yStart+(3*hbar), width, hbar, inputArray, (3*hbar*scansize)+offset, scansize));
            ForkJoinPool.commonPool().invoke(new RecurAction(xStart, yStart+(4*hbar), width, hbar, inputArray, (4*hbar*scansize)+offset, scansize));
            ForkJoinPool.commonPool().invoke(new RecurAction(xStart, yStart+(5*hbar), width, height-(5*hbar), inputArray, (5*hbar*scansize)+offset, scansize));
            
        
            
        } 
        
    }
}
}