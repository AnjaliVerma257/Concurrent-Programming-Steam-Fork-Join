package cop5618;

import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


public class ColorHistEq {

   static String[] labels = { "getRGB", "convert to HSB and create brightness map", "parallel prefix",
         "probability array", "equalize pixels", "setRGB" };

   static ColorHistEq histEqInstance = new ColorHistEq();
    static final int NUMBINS = 250;
   class HSB_Pixel {
      float hue;
      float saturation;
      float brightness;

      public HSB_Pixel(float[] hsbArr) {
         hue = hsbArr[0];
         saturation = hsbArr[1];
         brightness = hsbArr[2];
      }

      @Override
      public String toString() {
         return String.valueOf(brightness);
      }
   };

   static Timer colorHistEq_serial(BufferedImage image, BufferedImage newImage) {
      Timer time = new Timer(labels);
      /**
       * IMPLEMENT SERIAL METHOD
       */
      ColorModel colorModel = ColorModel.getRGBdefault();
      int w = image.getWidth();
      int h = image.getHeight();
      time.now();
      int[] sourceRGBPixelArray = image.getRGB(0, 0, w, h, new int[w * h], 0, w);
      time.now(); // getRGB


      List<HSB_Pixel> listHSB= Arrays.stream(sourceRGBPixelArray)
                                  .mapToObj(pixel -> {
                                     float[] hsbFloatArrayofPixel =
                                              java.awt.Color.RGBtoHSB(colorModel.getRed(pixel),
                                                              colorModel.getGreen(pixel),
                                                                    colorModel.getBlue(pixel), null);
                                           return histEqInstance.new HSB_Pixel(hsbFloatArrayofPixel);
                                                 }).collect(Collectors.toList());



       Map <Object , List<HSB_Pixel>> binsMap = listHSB.stream()
                                 .collect(Collectors.groupingBy(hsb -> {
                              return (int) Math.floor(NUMBINS * hsb.brightness);
                           }));


      time.now(); // convert to HSB and create brightness map

      // count of pixels in each histogram bin
      double[] binsArray = binsMap.entrySet().stream()
            .map(Map.Entry::getValue)
            .mapToDouble(hsbList -> hsbList.size())
            .toArray();

      // calculate prefix sum
      Arrays.parallelPrefix(binsArray, (x, y) -> x + y);

      time.now(); // parallel prefix

      double noOfPixels = w * h;

      // get the cumulative probability array (divide prefix counts by size of
      // pixels)

      final double[] binsCPArray = Arrays.stream(binsArray).map(x -> (x / noOfPixels)).toArray();
      time.now(); // cumulative probability array

       ///////////////////////////////
      int[] outputRGBPixelArray = listHSB.stream()
            .map(hsb -> {
               int ind = (int) Math.floor(NUMBINS*hsb.brightness);
               hsb.brightness = (float)binsCPArray[ind];
               return hsb;
            }).mapToInt(hsb -> java.awt.Color.HSBtoRGB(hsb.hue, hsb.saturation, hsb.brightness)).toArray();

      time.now(); // equalize pixel



      newImage.setRGB(0, 0, w, h, outputRGBPixelArray, 0, w);
      time.now(); // setRGB

      return time;
   }

   static Timer colorHistEq_parallel(FJBufferedImage image, FJBufferedImage newImage) {
      Timer time = new Timer(labels);
      /**
       * IMPLEMENT SERIAL METHOD
       */
      ColorModel colorModel = ColorModel.getRGBdefault();
      int w = image.getWidth();
      int h = image.getHeight();
      time.now();
      int[] sourceRGBPixelArray = image.getRGB(0, 0, w, h, new int[w * h], 0, w);
      time.now(); // getRGB


      List<HSB_Pixel> listHSB= Arrays.stream(sourceRGBPixelArray)
            .parallel()
            .mapToObj(pixel -> {
               float[] hsbFloatArrayofPixel =
                     java.awt.Color.RGBtoHSB(colorModel.getRed(pixel),
                           colorModel.getGreen(pixel),
                           colorModel.getBlue(pixel), null);
               return histEqInstance.new HSB_Pixel(hsbFloatArrayofPixel);
            }).collect(Collectors.toList());



      Map <Object,List<HSB_Pixel>> binsMap = listHSB.stream()
            .parallel()
            .collect(Collectors.groupingBy(hsb -> {
               return (int) Math.floor(NUMBINS * hsb.brightness);
            }));


      time.now(); // convert to HSB and create brightness map

      // count of pixels in each histogram bin
      double[] binsArray = binsMap.entrySet().stream()
            .map(Map.Entry::getValue)
            .mapToDouble(hsbList -> hsbList.size())
            .toArray();

      // calculate prefix sum
      Arrays.parallelPrefix(binsArray, (a, b) -> a + b);

      time.now(); // parallel prefix

      double noOfPixels = w * h;

      // get the cumulative probability array (divide prefix counts by size of
      // pixels)

      final double[] binsCPArray = Arrays.stream(binsArray).map(n -> (n / noOfPixels)).toArray();
      time.now(); // cumulative probability array

      ///////////////////////////////
      int[] outputRGBPixelArray = listHSB.stream()
            .parallel()
            .map(hsb -> {
               int ind = (int) Math.floor(NUMBINS * hsb.brightness);
               hsb.brightness = (float) binsCPArray[ind];
               return hsb;
            }).mapToInt(hsb -> java.awt.Color.HSBtoRGB(hsb.hue, hsb.saturation, hsb.brightness)).toArray();

      time.now(); // equalize pixel



      newImage.setRGB(0, 0, w, h, outputRGBPixelArray, 0, w);
      time.now(); // setRGB

      return time;
   }

}