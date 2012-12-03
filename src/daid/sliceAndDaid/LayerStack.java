package daid.sliceAndDaid;

import java.util.Vector;

import daid.sliceAndDaid.config.CraftConfig;
import daid.sliceAndDaid.util.Logger;

public class LayerStack
{
    public final static double BORDER_MM = 5;
    private double maxX = Double.MIN_VALUE;
    private double maxY = Double.MIN_VALUE;
    private double minX = Double.MAX_VALUE;
    private double minY = Double.MAX_VALUE;
    private int Xoffset = 0;
    private int Yoffset = 0;
    private int width;
    private int height;
    private double pixelPerMm = 100;
    private final Vector<Layer> layers = new Vector<Layer>();

    public LayerStack()
    {
        pixelPerMm = (1/CraftConfig.perimeterWidth);
    }

    public void add(final Layer layer)
    {
        layers.add(layer);
    }

    public Layer get(final int i)
    {
        return layers.get(i);
    }

    public int size()
    {
        return layers.size();
    }

    public void dumpStackToText()
    {
        if(Logger.LOG_LEVEL_DEBUG <= Logger.getLevel())
        {
            Logger.debug("Textual Description of all Layers:");
            for (int i = 0; i < size(); i++)
            {
                final Layer l = get(i);
                Logger.debug("Description of Layer {} :", i);
                Logger.debug("Xmax = {}", l.getMaxX());
                Logger.debug("Xmin = {}", l.getMinX());
                Logger.debug("Ymax = {}", l.getMaxY());
                Logger.debug("Ymin = {}", l.getMinY());
                Logger.debug("Z = {}", l.getZ());
                l.logState();

            }
            Logger.debug("End of Textual Description of all Layers.");
        }
        // else we don't log this
    }

    private void detectStackVolume(final double extraBorderMm)
    {
        maxX = Double.MIN_VALUE;
        maxY = Double.MIN_VALUE;
        minX = Double.MAX_VALUE;
        minY = Double.MAX_VALUE;

        for (int i = 0; i < size(); i++)
        {
            final Layer l = get(i);
            double help = l.getMaxX();
            if (help > maxX)
            {
                maxX = help;
            }
            help = l.getMaxY();
            if (help > maxY)
            {
                maxY = help;
            }
            help = l.getMinX();
            if (help < minX)
            {
                minX = help;
            }
            help = l.getMinY();
            if (help < minY)
            {
                minY = help;
            }
        }
        Logger.message("Layer Stack has this Volume:");
        Logger.message("Min X  : {}", minX);
        Logger.message("Min Y  : {}", minY);
        Logger.message("Max X  : {}", maxX);
        Logger.message("Max Y  : {}", maxY);
        final long lXoffset = Math.round(pixelPerMm * (Math.abs(minX) + BORDER_MM + extraBorderMm));
        final long lYoffset = Math.round(pixelPerMm * (Math.abs(minY) + BORDER_MM + extraBorderMm));
        final long lwidth = Math.round(pixelPerMm * ((Math.abs(maxX - minX) + (2 * (BORDER_MM + extraBorderMm)))));
        final long lheight = Math.round(pixelPerMm * ((Math.abs(maxY - minY) + (2 * (BORDER_MM + extraBorderMm)))));
        if(   (lXoffset > Integer.MAX_VALUE) || (lXoffset < Integer.MIN_VALUE)
           || (lYoffset > Integer.MAX_VALUE) || (lYoffset < Integer.MIN_VALUE)
           || (lwidth > Integer.MAX_VALUE) || (lwidth < Integer.MIN_VALUE)
           || (lheight > Integer.MAX_VALUE) || (lheight < Integer.MIN_VALUE) )
        {
            throw new IllegalArgumentException("Volume of LayerStack is too big !");
        }
        else
        {
            Xoffset = (int)lXoffset;
            Yoffset = (int)lYoffset;
            width = (int)lwidth;
            height = (int)lheight;
        }
        Logger.message("Border for Optimizers/mm : {}", extraBorderMm);
        Logger.message("Pixel/mm : {}", pixelPerMm);
        Logger.message("X-Offset : {}", Xoffset);
        Logger.message("Y-Offset : {}", Yoffset);
        Logger.message("width    : {}", width);
        Logger.message("height   : {}", height);

        Logger.message("complete Stack has {} Pixels.", width * height * layers.size());
    }

    public void dumpStackToLayerFiles(final String filePrefix)
    {
        detectStackVolume(0);

        for (int i = 0; i < size(); i++)
        {
            // With all the Layers...
            final Layer l = get(i);
            l.saveToPng(filePrefix + "_Layer_" + i + ".png");
            Logger.debug("Layer: {}", i);
        }
    }

    public int getPixelWidth()
    {
        return width;
    }

    public int getPixelHeight()
    {
        return height;
    }

    public double getPixelPerMm()
    {
        return pixelPerMm;
    }

    public int getPixelXoffset()
    {
        return Xoffset;
    }

    public int getPixelYoffset()
    {
        return Yoffset;
    }

    public void createLayerBitmaps(final double extraBorderMm)
    {
        detectStackVolume(extraBorderMm);
        for (int i = 0; i < size(); i++)
        {
            // With all the Layers...
            final Layer l = get(i);
            l.createBitmap(width, height, Xoffset, Yoffset);
        }
    }

    public void projectVectorsToBitmap()
    {
        Logger.debug("Projekting Layers:");
        int size = size();
        for (int i = 0; i < size; i++)
        {
            // With all the Layers...
            final Layer l = get(i);
            Logger.debug("Layer {} :", i);
            if(false == l.projectVectorsToBitmap())
            {
                Logger.debug("Removing Layer {}", i);
                layers.remove(i);
                i = i -1;
                size = size -1;
            }
        }
    }

    public void dumpBitMapsToFiles(final String filePrefix)
    {
        for (int i = 0; i < size(); i++)
        {
            // With all the Layers...
            final Layer l = get(i);
            l.saveBitmapToTxt(filePrefix + "_Layer_" + i + ".txt");
            // TODO l.saveBitmapToPng(filePrefix + "_Layer_" + i + ".png");
            Logger.debug("Layer: {}", i);
        }
    }
}