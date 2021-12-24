package cpen221.mp3.ShortestPathTests;


import cpen221.mp3.wikimediator.WikiMediator;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class ShortestPathTask5Tests {

    private static final WikiMediator sp = new WikiMediator(1, 1);

    @Test
    public void timeOutTest() throws TimeoutException {

        boolean thrown = false;
        try {

            sp.shortestPath("rock", "poop", 10);
        } catch (TimeoutException e) {

            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    @Test
    public void directPath() throws TimeoutException {

        List<String> expected = new ArrayList<>();
        expected.add("Philosophy");
        expected.add("Academic bias");
        Assert.assertEquals(expected,
                sp.shortestPath("Philosophy", "Academic bias", 300));
    }

    @Test
    public void philToObama() {
        boolean thrown = false;
        List<String> expected = List.of("Philosophy", "Academic bias", "Barack Obama");
        try {
            List<String> path = sp.shortestPath(
                "Philosophy",
                "Barack Obama",
                120);
            Assert.assertEquals(expected, path);
            System.out.println(path);
        }catch (TimeoutException e){
            thrown = true;
        }
    }

    @Test
    public void kingOfScotland() {
        boolean thrown = false;
        List<String> expected =
            List.of("Forties pipeline system", "Cruden Bay", "Malcolm II of Scotland");
        try{
            List<String> path = sp.shortestPath(
                "Forties pipeline system",
                "Malcolm II of Scotland",
                120);
            Assert.assertEquals(expected, path);
            System.out.println(path);
        } catch (TimeoutException e) {
            thrown = true;
        }
        System.out.println(thrown);
    }

    @Test
    public void funnyHowThatWorks() {
        boolean thrown = false;
        List<String> expected = List.of("Cock and ball torture", "Masturbation", "Bill Clinton");
        try{
            List<String> path = sp.shortestPath(
                "Cock and ball torture",
                "Bill Clinton",
                360);
            Assert.assertEquals(expected, path);
            System.out.println(path);
        } catch (TimeoutException e) {
            thrown = true;
        }
        System.out.println(thrown);
    }

    @Test
    public void empty() throws TimeoutException {
        Assert.assertEquals(new ArrayList<>(),
            sp.shortestPath(
                "Wikipedia:The blank page",
                "Feces",
                300));
    }

    @Test
    public void highDegreeEasyMode(){
        boolean thrown = false;
        List<String> expected = List.of(
            "Straight (magazine)",
            "ISBN (identifier)",
            "International Standard Book Number",
            "Barcode",
            "University of Cambridge");
        try{
            List<String> path = sp.shortestPath(
                "Straight (magazine)",
                "University of Cambridge",
                3000);
            Assert.assertEquals(expected, path);
            System.out.println(path);
        } catch (TimeoutException e) {
            thrown = true;
        }
        System.out.println(thrown);
    }
}
