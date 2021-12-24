package cpen221.mp3.FSFTBufferTests;

import cpen221.mp3.exceptions.InvalidIDException;
import cpen221.mp3.exceptions.NotFoundException;
import cpen221.mp3.FSFTBufferTests.Tee.Tee;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ThreadSafeTests {

    private static FSFTBuffer<Tee> fourSecSizeFour;
    private static Tee t0, t1, t2, t3, t4, t5, t6;

    @Before
    public void instantiate() throws InvalidIDException {

        t0 = new Tee("man");
        t1 = new Tee("woman");
        t2 = new Tee("dog");
        t3 = new Tee("cat");
        t4 = new Tee("rat");
        t5 = new Tee("mommy");
        t6 = new Tee("daddy");

        fourSecSizeFour = new FSFTBuffer<>(4, 4);
    }

    @Test
    public void sequential() throws InterruptedException {

        //filling the buffer object
        Assert.assertTrue(fourSecSizeFour.put(t0));
        Assert.assertTrue(fourSecSizeFour.put(t1));
        Assert.assertTrue(fourSecSizeFour.put(t2));

        Thread.sleep(2000);

        // "cat" added 2 seconds later, "cat" should be at 0 seconds
        Assert.assertTrue(fourSecSizeFour.put(t3));

        // "man" and "woman" reset to 0 seconds, "dog" should be at 2 seconds
        Assert.assertTrue(fourSecSizeFour.touch("man"));
        Assert.assertTrue(fourSecSizeFour.touch("woman"));

        // buffer contains t0, t1, t2, t3
        Thread.sleep(2500);

        // t2 should stale
        Assert.assertTrue(fourSecSizeFour.put(t4));

        // t2 should be gone
        Assert.assertFalse(fourSecSizeFour.update(t2));

        // t0, t1, t3, t4, want t0 and t1 to be stale with same duration
        Assert.assertTrue(fourSecSizeFour.update(t3));
        Assert.assertTrue(fourSecSizeFour.update(t4));
        Thread.sleep(3500);
    }

    @Test
    public void concurrentPutPut() throws InterruptedException {

        // multithreaded put
        new Thread(() -> {

            try {

                fourSecSizeFour.put(t0); // put man at time t = 0s
                Thread.sleep(2000);
                fourSecSizeFour.put(t3); // put cat at time t = 2s
                Thread.sleep(1100);
                // t = 3s put rat and mommy and daddy
                fourSecSizeFour.put(t5); // put mommy
                fourSecSizeFour.put(t6); // put daddy
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {

            try {

                Thread.sleep(900);// tentatively necessary
                fourSecSizeFour.put(t1); // put woman at time t = 1s
                fourSecSizeFour.put(t2); // put dog at time t = 1s
                Thread.sleep(2000);
                // t = 3s put rat and mommy and daddy
                fourSecSizeFour.put(t4); // put rat
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(2500); // wait 2.5 seconds, t0 = 3s, t1 = 2s, t2 = 2s, t3 = 1s hopefully
        System.out.printf("man: %-15d\n" +
                        "woman: %-15d\n" +
                        "dog: %-15d\n" +
                        "cat: %-15d\n", fourSecSizeFour.getBufferMap().get(t0).getSeconds(),
                fourSecSizeFour.getBufferMap().get(t1).getSeconds(),
                fourSecSizeFour.getBufferMap().get(t2).getSeconds(),
                fourSecSizeFour.getBufferMap().get(t3).getSeconds());
        Assert.assertEquals(2, fourSecSizeFour.getBufferMap().get(t0).getSeconds()); // man = 2s
        Assert.assertEquals(1, fourSecSizeFour.getBufferMap().get(t1).getSeconds()); // woman = 1s
        Assert.assertEquals(1, fourSecSizeFour.getBufferMap().get(t2).getSeconds()); // dog = 1s
        Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t3).getSeconds()); // cat = 0s

        Thread.sleep(1000); // t = 3.5s, buffer should not have t0 and should contain rat = mommy = daddy = 0s
        Assert.assertEquals(1, fourSecSizeFour.getBufferMap().get(t3).getSeconds()); // cat = 2s
        Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t4).getSeconds()); // rat = 0s
        Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t5).getSeconds()); // mommy = 0s
        Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t6).getSeconds()); // daddy = 0s
    }

    @Test
    public void sequentialPutPut() throws InterruptedException {

        // multithreaded put
        try {

            fourSecSizeFour.put(t0); // put man at time t = 0s
            Thread.sleep(1000);
            fourSecSizeFour.put(t1); // put woman at time t = 1s
            fourSecSizeFour.put(t2); // put dog at time t = 1s
            Thread.sleep(1000);
            fourSecSizeFour.put(t3); // put cat at time t = 2s
            Thread.sleep(500);
            Assert.assertEquals(2, fourSecSizeFour.getBufferMap().get(t0).getSeconds()); // man = 2s
            Assert
                    .assertEquals(1, fourSecSizeFour.getBufferMap().get(t1).getSeconds()); // woman = 1s
            Assert.assertEquals(1, fourSecSizeFour.getBufferMap().get(t2).getSeconds()); // dog = 1s
            Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t3).getSeconds()); // cat = 0s
            Thread.sleep(500);
            // t = 3s put rat and mommy and daddy
            fourSecSizeFour.put(t4); // put rat
            fourSecSizeFour.put(t5); // put mommy
            fourSecSizeFour.put(t6); // put daddy
            Thread.sleep(500);
            Assert.assertEquals(1, fourSecSizeFour.getBufferMap().get(t3).getSeconds()); // cat = 2s
            Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t4).getSeconds()); // rat = 0s
            Assert
                    .assertEquals(0, fourSecSizeFour.getBufferMap().get(t5).getSeconds()); // mommy = 0s
            Assert
                    .assertEquals(0, fourSecSizeFour.getBufferMap().get(t6).getSeconds()); // daddy = 0s
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sequentialGetGet() throws InterruptedException, NotFoundException {

        fourSecSizeFour.put(t0);
        Thread.sleep(1000);
        fourSecSizeFour.put(t1);
        fourSecSizeFour.put(t2);
        Thread.sleep(1000);
        fourSecSizeFour.put(t3);
        Thread.sleep(500);
        Assert.assertEquals(t0, fourSecSizeFour.get("man"));
        Assert.assertEquals(t1, fourSecSizeFour.get("woman"));
    }

    @Test
    public void concurrentGetGet() throws InterruptedException, NotFoundException {

        fourSecSizeFour.put(t0);
        Thread.sleep(1000);
        fourSecSizeFour.put(t1);
        fourSecSizeFour.put(t2);
        Thread.sleep(1000);
        fourSecSizeFour.put(t3);
        Thread.sleep(500);

        new Thread(() -> {

            try {

                Thread.sleep(2500);// tentatively necessary
                Assert.assertEquals(t0, fourSecSizeFour.get("man"));
                Assert.assertEquals(t1, fourSecSizeFour.get("woman"));
            } catch (InterruptedException | NotFoundException e) {

                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {

            try {

                Thread.sleep(2500);// tentatively necessary
                Assert.assertEquals(t0, fourSecSizeFour.get("man"));
                Assert.assertEquals(t1, fourSecSizeFour.get("woman"));
            } catch (InterruptedException | NotFoundException e) {

                e.printStackTrace();
            }
        }).start();
    }

    @Test
    public void concurrentPutGet() throws InterruptedException, NotFoundException {

        // multithreaded put
        new Thread(() -> {

            try {

                fourSecSizeFour.put(t0); // put man at time t = 0s
                Thread.sleep(2000);
                fourSecSizeFour.put(t3); // put cat at time t = 2s
                Assert.assertEquals(t1, fourSecSizeFour.get("woman"));
                Assert.assertEquals(t2, fourSecSizeFour.get("dog"));
                Thread.sleep(1100);
                // t = 3s put rat and mommy and daddy
                fourSecSizeFour.put(t5); // put mommy
                fourSecSizeFour.put(t6); // put daddy
            } catch (InterruptedException | NotFoundException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {

            try {

                Thread.sleep(900);// tentatively necessary
                fourSecSizeFour.put(t1); // put woman at time t = 1s
                fourSecSizeFour.put(t2); // put dog at time t = 1s

                Thread.sleep(1000);
                Assert.assertEquals(t0, fourSecSizeFour.get("man"));
                Thread.sleep(1100);
                // t = 3s put rat and mommy and daddy
                Assert.assertEquals(t3, fourSecSizeFour.get("cat"));
                fourSecSizeFour.put(t4); // put rat
            } catch (InterruptedException | NotFoundException e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(2500); // wait 2.5 seconds, t0 = 3s, t1 = 2s, t2 = 2s, t3 = 1s hopefully
        System.out.printf("man: %-15d\n" +
                        "woman: %-15d\n" +
                        "dog: %-15d\n" +
                        "cat: %-15d\n", fourSecSizeFour.getBufferMap().get(t0).getSeconds(),
                fourSecSizeFour.getBufferMap().get(t1).getSeconds(),
                fourSecSizeFour.getBufferMap().get(t2).getSeconds(),
                fourSecSizeFour.getBufferMap().get(t3).getSeconds());
        Assert.assertEquals(2, fourSecSizeFour.getBufferMap().get(t0).getSeconds()); // man = 2s
        Assert.assertEquals(1, fourSecSizeFour.getBufferMap().get(t1).getSeconds()); // woman = 1s
        Assert.assertEquals(1, fourSecSizeFour.getBufferMap().get(t2).getSeconds()); // dog = 1s
        Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t3).getSeconds()); // cat = 0s

        Thread.sleep(
                1000); // t = 3.5s, buffer should not have t0 and should contain rat = mommy = daddy = 0s
        Assert.assertEquals(1, fourSecSizeFour.getBufferMap().get(t3).getSeconds()); // cat = 2s
        Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t4).getSeconds()); // rat = 0s
        Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t5).getSeconds()); // mommy = 0s
        Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t6).getSeconds()); // daddy = 0s
    }

    @Test
    public void concurrentUpdateTouchPut() throws InterruptedException, NotFoundException {

        // multithreaded put
        new Thread(() -> {

            try {
                fourSecSizeFour.put(t0); // put man at time t = 0s
                Thread.sleep(1800);
                fourSecSizeFour.put(t3); // put cat at time t = 2s
                Thread.sleep(200);
                Assert.assertEquals(t1, fourSecSizeFour.get("woman"));
                Assert.assertEquals(t2, fourSecSizeFour.get("dog"));
                Thread.sleep(1300);
                // t = 3s put rat and mommy and daddy
                fourSecSizeFour.put(t5); // put mommy
                fourSecSizeFour.put(t6); // put daddy
            } catch (InterruptedException | NotFoundException e) {

                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {

            try {

                Thread.sleep(900);// tentatively necessary
                fourSecSizeFour.put(t1); // put woman at time t = 1s
                fourSecSizeFour.put(t2); // put dog at time t = 1s
                Assert.assertTrue(fourSecSizeFour.touch("man"));
                Assert.assertTrue(fourSecSizeFour.update(t0)); // man to 0s
                Thread.sleep(1000);
                Assert.assertEquals(t0, fourSecSizeFour.get("man"));
                Thread.sleep(100);
                Assert.assertTrue(fourSecSizeFour.touch("woman"));
                Assert.assertTrue(fourSecSizeFour.touch("dog"));
                Thread.sleep(1100);
                // t = 3s put rat and mommy and daddy
                Assert.assertEquals(t3, fourSecSizeFour.get("cat"));
                fourSecSizeFour.put(t4); // put rat
            } catch (InterruptedException | NotFoundException e) {

                e.printStackTrace();
            }
        }).start();

        Thread.sleep(2500); // wait 2.5 seconds, t0 = 3s, t1 = 2s, t2 = 2s, t3 = 1s hopefully
        System.out.printf("man: %-15d\n" +
                        "woman: %-15d\n" +
                        "dog: %-15d\n" +
                        "cat: %-15d\n", fourSecSizeFour.getBufferMap().get(t0).getSeconds(),
                fourSecSizeFour.getBufferMap().get(t1).getSeconds(),
                fourSecSizeFour.getBufferMap().get(t2).getSeconds(),
                fourSecSizeFour.getBufferMap().get(t3).getSeconds());
        Assert.assertEquals(1, fourSecSizeFour.getBufferMap().get(t0).getSeconds()); // man = 2s
        Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t1).getSeconds()); // woman = 1s
        Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t2).getSeconds()); // dog = 1s
        Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t3).getSeconds()); // cat = 0s

        Thread.sleep(
            1400); // t = 3.5s, buffer should not have t0 and should contain rat = mommy = daddy = 0s
        Assert.assertEquals(1, fourSecSizeFour.getBufferMap().get(t1).getSeconds()); // cat = 2s
        Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t4).getSeconds()); // rat = 0s
        Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t5).getSeconds()); // mommy = 0s
        Assert.assertEquals(0, fourSecSizeFour.getBufferMap().get(t6).getSeconds()); // daddy = 0s
    }

}
