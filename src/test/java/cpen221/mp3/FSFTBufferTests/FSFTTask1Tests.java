package cpen221.mp3.FSFTBufferTests;

import cpen221.mp3.exceptions.InvalidIDException;
import cpen221.mp3.FSFTBufferTests.Tee.Tee;
import cpen221.mp3.fsftbuffer.Bufferable;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import cpen221.mp3.exceptions.NotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;

public class FSFTTask1Tests {

    public static FSFTBuffer<Bufferable> sizeFiveDurTen;
    public static FSFTBuffer<Bufferable> sizeOneDurOne;
    public static FSFTBuffer<Bufferable> defuaultBuffer;

    @Before
    public void makeBuffer() {
        sizeFiveDurTen = new FSFTBuffer<>(5, 10);
        sizeOneDurOne  = new FSFTBuffer<>(1,1);
        defuaultBuffer = new FSFTBuffer<>();
    }

    @Test
    public void basicPutTest() throws InvalidIDException {
        Tee t0 = new Tee("peepoo");
        Assert.assertTrue(sizeFiveDurTen.put(t0));
        Assert.assertTrue(sizeOneDurOne.put(t0));
    }

    @Test
    public void putTooMuch() throws InvalidIDException {
        Tee t0 = new Tee("peepoo");
        Assert.assertTrue(sizeOneDurOne.put(t0));
        Assert.assertTrue(sizeOneDurOne.put(t0));
    }

    @Test
    public void basicGetTest() throws InvalidIDException {
        Tee t0 = new Tee("joe biden");
        sizeFiveDurTen.put(t0);
        try {
            Assert.assertEquals(t0, sizeFiveDurTen.get("joe biden"));
        } catch (NotFoundException e) {
            System.out.println("no valid object");
        }
    }
    @Test
    public void getJohnCena() throws InvalidIDException {
        Tee t0 = new Tee("joe biden");
        sizeFiveDurTen.put(t0);
        try{
            Assert.assertEquals(t0, sizeFiveDurTen.get("john cena"));
        }catch(NotFoundException e){
            System.out.println("no valid object");
        }
    }

    @Test
    public void basicTouchTest() throws InvalidIDException {
        Tee t0 = new Tee("sathish");
        sizeFiveDurTen.put(t0);
        try {
            Thread.sleep(3500);
            Assert.assertTrue(sizeFiveDurTen.touch("sathish"));
            Thread.sleep(3500);
            try {
                Assert.assertEquals(t0, sizeFiveDurTen.get("sathish"));
            } catch (NotFoundException e) {
                System.out.println("no valid object");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void basicUpdateTest() throws InvalidIDException {
        Tee t0 = new Tee("amongus");
        sizeFiveDurTen.put(t0);
        try {
            Thread.sleep(3500);
            Assert.assertTrue(sizeFiveDurTen.update(t0));
            Thread.sleep(3500);
            try {
                Assert.assertEquals(t0, sizeFiveDurTen.get("amongus"));
            } catch (NotFoundException e) {
                System.out.println("no valid object");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updateFailTest() throws InvalidIDException {
        Tee t0 = new Tee("kanye");
        Tee t1 = new Tee("jay z");
        sizeOneDurOne.put(t0);
        sizeFiveDurTen.put(t1);
        try {
            Thread.sleep(1500);
            Assert.assertFalse(sizeOneDurOne.update(t0));
            Assert.assertFalse(sizeFiveDurTen.update(t0));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void touchFailTest() throws InvalidIDException {
        Tee t0 = new Tee("the rock");
        Tee t1 = new Tee("kevin hart");
        sizeOneDurOne.put(t0);
        sizeFiveDurTen.put(t1);
        try {
            Thread.sleep(1500);
            Assert.assertFalse(sizeOneDurOne.touch("the rock"));
            Assert.assertFalse(sizeOneDurOne.touch("kevin hart"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void basicReplaceTest() throws InvalidIDException {
        Tee t0 = new Tee("the rock");
        Tee t1 = new Tee("kevin hart");

        Assert.assertTrue(sizeFiveDurTen.put(t0));
        Assert.assertTrue(sizeFiveDurTen.replace("the rock", t1));

        Assert.assertFalse(sizeFiveDurTen.replace("the rock", t1)); // shouldnt work\
        int maxSize = sizeFiveDurTen.getMaxSize();
        Duration maxTime = sizeFiveDurTen.getMaxTime();
    }
}
