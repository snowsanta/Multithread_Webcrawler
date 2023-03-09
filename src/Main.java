import harsh.freelance.webCrawler.Crawler;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        String URL;
        int DEPTH;

        // Parse arguments
        if(args.length < 2){
            URL = "https://en.wikipedia.org/wiki/India";
            DEPTH = 0;
        } else {
            try{
                URL = args[0];
                DEPTH = new Integer(args[1]);
            } catch(Exception e) {
                System.out.println("Please input correct args\n" + e.getMessage());
                return;
            }
        }

        // Initiate crawler
        Crawler webCrawler = new Crawler(URL, DEPTH);
        webCrawler.excecuteCrawler();
    }
}