package harsh.freelance.webCrawler;

import javafx.util.Pair;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static harsh.freelance.webCrawler.Crawler.*;


public class Task implements Runnable{
    private final Integer depth;
    private final String URL;
    public ArrayList<Pair<String, Integer>> links = new ArrayList<>();
    public Task(String URL, int depth){
        this.depth = depth;
        this.URL = URL;
    }

    // establishes connection and handles connection errors
    public Document requestConnection(String URL, Integer depth) throws IOException {
        /* jsoup connect
        if 200: return doc
        if 404: log
        if 429: processQueue.add(Pair(URL, depth);
         */

        Connection con = Jsoup.connect(URL).ignoreContentType(true);

        try {
            Document doc = con.get();

            if(con.response().statusCode() == 200){
                System.out.println("Visited Url: " + URL);
                visited.add(URL);
                return doc;
            }

        } catch (HttpStatusException e) {
            if(e.getStatusCode() == 404) {
                // ignore on 404 error
                System.out.println("404 error encountered on " + URL);
            }
            else if(e.getStatusCode() == 429) {
                // retry on 429 error
                System.out.println("Retrying 429 error got on " + URL);
                links.add(new Pair<>(URL, depth + 1));
            }
            return null;
        } catch (IOException e) {
            throw new IOException(e);
        }

        return null;
    }

    // takes task from processQueue and executes it
    @Override
    public void run() {
        if(depth >= maxDepth){ return; }
        if(visited.contains(URL)){ return; }

        URI uriRoot = null;
        Document doc = null;
        try {
            uriRoot = new URI(URL);
            doc = requestConnection(URL, depth);
        } catch (IOException e) {
            System.out.println("Error in connection request: " + e.getMessage());
        } catch (URISyntaxException e) {
            System.out.println("Error in URISyntax: " + e.getMessage());
        }

        if(doc == null){ return; }
        String hostRoot = uriRoot.getHost();

        double sameBaseUrl = 0.0;
        double diffBaseUrl = 0.0;
        for(Element link: doc.select("a[href]")){
            try {
                String newLink = link.absUrl("href");

                // excluding common ignore patterns
                if(newLink.contains("javascript") || newLink.contains("mailto:")){
                    continue;
                }

                // filter and compare anchor links
                newLink = (newLink.contains("#")) ? newLink.substring(0, newLink.indexOf("#")) : newLink;

                if (!visited.contains(newLink) && depth + 1 <= maxDepth) {
                    links.add(new Pair<>(newLink, depth + 1));
                }

                // score the urls
                URI uriLink = new URI(newLink);
                String hostLink = uriLink.getHost();
                if(hostLink.equals(hostRoot)){
                    sameBaseUrl += 1;
                } else {
                    diffBaseUrl += 1;
                }
            } catch (Exception e) {
                System.out.println("Error in processing link: " + link + " " + e.getMessage());
            }
        }

        // publish new links to array and notify waiting threads
        Crawler.links.addAll(links);
        synchronized (linksAdded){
            linksAdded.notifyAll();
        }

        // put scores in hashmap
        scores.put(URL, new Pair<>(depth, sameBaseUrl/(sameBaseUrl + diffBaseUrl)));
    }
}
