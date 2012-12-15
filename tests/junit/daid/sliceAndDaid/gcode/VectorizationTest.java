/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses/>
 *
 */
package daid.sliceAndDaid.gcode;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import daid.sliceAndDaid.Layer;
import daid.sliceAndDaid.LayerBitmap;
import daid.sliceAndDaid.LayerDirection;
import daid.sliceAndDaid.LayerStack;
import daid.sliceAndDaid.bitmap.Pixel;
import daid.sliceAndDaid.bitmap.PixelCode;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class VectorizationTest
{
    @Test
    public void testNull()
    {
        final Vectorization v = new Vectorization(null, null);
        try
        {
            v.generatePathsFor(null, null, null, null, null);
            fail("Should have Failed, but succeded");
        }
        catch (final IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("unexpected Exception");
        }
        catch (final NullPointerException e)
        {
            // this shall happen
        }
    }

    @Test
    public void testEmpty()
    {
        final LayerStack stack = new LayerStack();
        final Layer l = new Layer(75, 0, 0);
        l.createBitmap(50, 50, 25, 25);
        stack.add(l);
        final GCodeOptimizerStub gCodeStub = new GCodeOptimizerStub();
        final Vectorization v = new Vectorization(gCodeStub, stack);
        try
        {
            final Pixel start = new Pixel(0,0);
            Pixel res = v.generatePathsFor(l.getBitmap(),
                                           PixelCode.OUTLINE_CODE,
                                           start,
                                           LayerDirection.X_THEN_Y,
                                           RoutingAlgorithm.OUTLINE);
            if(false == start.equals(res))
            {
                fail("Moved without reason !");
            }
            if(0 != gCodeStub.getNumberReceivedGCodes())
            {
                fail("Generated G-Codes without reason !");
            }
            res = v.generatePathsFor(l.getBitmap(),
                                     PixelCode.FILLIN_CODE,
                                     start,
                                     LayerDirection.X_THEN_Y,
                                     RoutingAlgorithm.AREA);
            if(false == start.equals(res))
            {
                fail("Moved without reason !");
            }
            if(0 != gCodeStub.getNumberReceivedGCodes())
            {
                fail("Generated G-Codes without reason !");
            }
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            fail("unexpected Exception");
        }
    }

    @Test
    public void testSingleLineOutLine()
    {
        final LayerStack stack = new LayerStack();
        final Layer l = new Layer(stack.getPixelPerMm(), 0, 0);
        l.createBitmap(50, 50, 25, 25);
        stack.add(l);
        final LayerBitmap bitmap = l.getBitmap();
        bitmap.drawLine(0, 0, 20, 20, PixelCode.OUTLINE_CODE, PixelCode.EMPTY_CODE);

        final GCodeOptimizerStub gCodeStub = new GCodeOptimizerStub();
        final Vectorization v = new Vectorization(gCodeStub, stack);
        try
        {
            final Pixel start = new Pixel(0,0);
            // Outline Routing
            final Pixel res = v.generatePathsFor(bitmap,
                                           PixelCode.OUTLINE_CODE,
                                           start,
                                           LayerDirection.X_THEN_Y,
                                           RoutingAlgorithm.OUTLINE);
            final Pixel end = new Pixel(20,20);
            if(false == end.equals(res))
            {
                fail("Moved without reason ! expected: " + end + " received: " + res);
            }
            if(1 != gCodeStub.getNumberReceivedGCodes())
            {
                fail("Generated G-Codes without reason !");
            }
            if(false == gCodeStub.codeIsAExtrude(0))
            {
                fail("Generated wrong G-Code!");
            }
            final double expectedX = 20 / stack.getPixelPerMm();
            final LineOfGCode line = gCodeStub.getCodeLine(0);
            if(Math.abs(line.getX() - expectedX) > 0.00001)
            {
                fail("Generated wrong X Coordinate G-Code! expected : " + expectedX + " received: " + line.getX());
            }
            final double expectedY = 20 / stack.getPixelPerMm();
            if(Math.abs(line.getY() - expectedY) > 0.00001)
            {
                fail("Generated wrong Y Coordinate G-Code! expected : " + expectedX + " received: " + line.getX());
            }
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            fail("unexpected Exception");
        }
    }

    @Test
    public void testSingleLineArea()
    {
        final LayerStack stack = new LayerStack();
        final Layer l = new Layer(stack.getPixelPerMm(), 0, 0);
        l.createBitmap(50, 50, 25, 25);
        stack.add(l);
        final LayerBitmap bitmap = l.getBitmap();
        bitmap.drawLine(0, 0, 20, 20, PixelCode.FILLIN_CODE, PixelCode.EMPTY_CODE);

        final GCodeOptimizerStub gCodeStub = new GCodeOptimizerStub();
        final Vectorization v = new Vectorization(gCodeStub, stack);
        try
        {
            final Pixel start = new Pixel(0,0);
            // Area Routing
            final Pixel res = v.generatePathsFor(l.getBitmap(),
                                           PixelCode.FILLIN_CODE,
                                           start,
                                           LayerDirection.X_THEN_Y,
                                           RoutingAlgorithm.AREA);
            final Pixel end = new Pixel(20,20);
            if(false == end.equals(res))
            {
                fail("Moved without reason ! expected: " + end + " received: " + res);
            }
            if(1 != gCodeStub.getNumberReceivedGCodes())
            {
                fail("Generated G-Codes without reason !");
            }
            if(false == gCodeStub.codeIsAExtrude(0))
            {
                fail("Generated wrong G-Code!");
            }
            final double expectedX = 20 / stack.getPixelPerMm();
            final LineOfGCode line = gCodeStub.getCodeLine(0);
            if(Math.abs(line.getX() - expectedX) > 0.00001)
            {
                fail("Generated wrong X Coordinate G-Code! expected : " + expectedX + " received: " + line.getX());
            }
            final double expectedY = 20 / stack.getPixelPerMm();
            if(Math.abs(line.getY() - expectedY) > 0.00001)
            {
                fail("Generated wrong Y Coordinate G-Code! expected : " + expectedY + " received: " + line.getY());
            }
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            fail("unexpected Exception");
        }
    }
}