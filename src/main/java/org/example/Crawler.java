package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

public class Crawler {
    private HashSet<String> urlLink;

    // create object for connection
    public Connection connection;

    // this method acts like crawler and indexer
    public Crawler(){

        connection = DatabaseConnection.getConnection();
        urlLink = new HashSet<String>();
    }
    /*  recursive crawler function
    this method gets text of that webpage, and it recursively adds all the link to the DB. */
    public void getPageTextAndLinks(String URL, int depth){
        if(!urlLink.contains(URL)){
            try{
                if(urlLink.add(URL)){
                    System.out.println(URL);
                }

                //connection provides a convenient interface to fetch content from the web, and parse them into Documents.
                //We connect to the url, set a 5 s time out, and send a GET request.
                //and A HTML document is returned.

                Document document = Jsoup.connect(URL).userAgent("Chrome").timeout(5000).get();

                String text =  document.text().length()<501? document.text():document.text().substring(0, 500);

               PreparedStatement preparedStatement=connection.prepareStatement("Insert into pages values (?,?,?)");
                preparedStatement.setString(1,document.title());
                preparedStatement.setString(2,URL);
                preparedStatement.setString(3,text);
                preparedStatement.executeUpdate();
                System.out.println(connection);

                depth++;
                // base method
                if(depth==2){
                    return;
                }
                //  From the document, we select the links.
                Elements availableLinksOnPage = document.select("a[href]");
                // traverse the Doc link recursively
                for(Element element:availableLinksOnPage){
                    //recursive call
                    getPageTextAndLinks(element.attr("abs:href"), depth);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }
    }
    public static void main(String[] args){
        Crawler crawler = new Crawler();
        crawler.getPageTextAndLinks("https://www.javatpoint.com", 0);
    }

}