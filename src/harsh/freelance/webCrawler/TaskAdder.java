package harsh.freelance.webCrawler;


import static harsh.freelance.webCrawler.Crawler.executor;
import static harsh.freelance.webCrawler.Crawler.linksAdded;
import static harsh.freelance.webCrawler.Crawler.links;
import static harsh.freelance.webCrawler.Crawler.queueEmpty;

public class TaskAdder implements Runnable{
    public Boolean fun(){
        // linksadded -> task is complete and links are to be filled
        // queueEmpty -> queue is empty

        // wait for queue to notify empty
        synchronized (queueEmpty) {
            try {
                queueEmpty.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // if all tasks are complete and no new links are in array then finish loop
        if(executor.getActiveCount() == 0 && links.isEmpty()){
            return false;
        }

        // if task is running wait for it to publish new links
        synchronized (linksAdded) {
            try {
                if(executor.getActiveCount() != 0){
                    linksAdded.wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // add unvisited links to queue
        links.forEach((x) -> executor.submit(new Task(x.getKey(), x.getValue())));
        links.clear();

        return true;
    }

    @Override
    public void run(){
        // loop until tasks ar found or waiting in queue
        while(fun());

        // when all tasks are exhausted call shutdown
        executor.shutdown();
    }
}
