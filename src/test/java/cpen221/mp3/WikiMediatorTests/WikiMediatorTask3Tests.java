package cpen221.mp3.WikiMediatorTests;

import cpen221.mp3.wikimediator.WikiMediator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class WikiMediatorTask3Tests {

    private static WikiMediator wm2_4;
    private static WikiMediator wm1_1;

    @Before
    public void initMediators() {

        WikiMediator testDefault = new WikiMediator();
        wm2_4 = new WikiMediator(2, 4);
        wm1_1 = new WikiMediator(1, 1);
    }

    @Test
    public void basicSearch() {

        List<String> expected1 = new ArrayList<>();
        List<String> expected2 = new ArrayList<>();
        expected1.add("Barack Obama");
        expected2.add("Arsenal");
        expected2.add("Talk:Arsenal");
        Assert.assertEquals(expected1, wm1_1.search("Barack Obama", 1));
        Assert.assertEquals(expected2, wm2_4.search("Arsenal", 2));
    }

    @Test
    public void basicGetPage() {

        System.out.println(wm1_1.getPage("New York State Route 373"));
    }
    @Test
    public void pageDNE(){
        System.out.println(wm1_1.getPage("The blank page"));
        System.out.println(wm1_1.getPage("fesakjlfjsadlfdskaf;l"));
        System.out.println(wm1_1.windowedPeakLoad());
    }

    @Test
    public void basicZeitgeist() {

        WikiMediator zgTest = new WikiMediator(2, 4);
        for (int i = 0; i < 3; i++) {

            zgTest.search("three", 3);
        }
        zgTest.search("two", 2);
        zgTest.search("two", 2);
        zgTest.search("one", 1);

        List<String> expected = new ArrayList<>();
        expected.add("three");
        expected.add("five");
        expected.add("two");

        Assert.assertEquals(expected, zgTest.zeitgeist(3));
    }

    @Test
    public void basicTrending() throws InterruptedException {

        WikiMediator trendTest = new WikiMediator(1, 1);
        for (int i = 0; i < 5; i++) {

            trendTest.search("five", 5);
        }
        Thread.sleep(3000);
        trendTest.search("two", 2);
        trendTest.search("two", 2);

        List<String> expected = new ArrayList<>();
        expected.add("two");

        Assert.assertEquals(expected, trendTest.trending(2, 10));
    }

    @Test
    public void basicPeakLoad() throws InterruptedException {

        WikiMediator peakLoad = new WikiMediator();

        peakLoad.search("three", 3);
        peakLoad.search("three", 3);
        peakLoad.search("three", 3);

        Thread.sleep(3000);

        peakLoad.getPage("three");
        peakLoad.getPage("three");
        peakLoad.search("three", 1);
        peakLoad.trending(2, 1);

        Thread.sleep(3000);

        peakLoad.zeitgeist(4);
        int out = peakLoad.windowedPeakLoad(1);
        Assert.assertEquals(4, out);
    }

}
