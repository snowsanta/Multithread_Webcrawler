package harsh.freelance.webCrawler;

import javafx.util.Pair;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.*;

public class Crawler{
    public static final Object queueEmpty = new Object();
    public static final Object linksAdded = new Object();
    public static ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
    public static Set<String> visited = map.keySet(1);
    public static LinkedBlockingQueue<Runnable> processQueue = new LinkedBlockingQueue<Runnable>(){
        // notify when process queue is empty to load the next batch of task

        @Override
        public Runnable take(){
            final Runnable result = super.poll();
            synchronized (queueEmpty) {
                if (this.size() == 0) {
                    queueEmpty.notifyAll();
                }
            }
            return result;
        }
    };
    public static ConcurrentHashMap<String, Pair<Integer, Double>> scores = new ConcurrentHashMap<>();
    public static Vector<Pair<String, Integer>> links = new Vector<>();
    public static ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 100, 1, TimeUnit.SECONDS, processQueue);
    public static Integer maxDepth;

    private final Thread thread;

    public Crawler(String URL, int maxDepth) {
        // set fields
        Crawler.maxDepth = maxDepth;
        processQueue.add(new Task(URL, 0));
        thread = new Thread(new TaskAdder());
    }

    public void excecuteCrawler() throws InterruptedException {
        // start executor service and parallel task adder thread
        executor.prestartCoreThread();
        thread.start();

        // wait for executor to finish
        executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        thread.join();

        System.out.println("DONE, Publishing results");

        // export results to TSV
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get("data.tsv")))) {
            writer.printf("URL\tDepth\tScore");
            writer.println();
            scores.forEach((k, v) -> {
                System.out.println(k + " " + v);
                writer.printf("%1$20s\t%2$3s\t%3$s", k, v.getKey(), v.getValue());
                writer.println();
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
