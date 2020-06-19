import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Laborator2 {
    private String websiteFolder;
    private String baseUri;
    private boolean HTMLFilesParsed;

    Laborator2(String websiteFolder, String baseUri) throws IOException
    {
        this.websiteFolder = websiteFolder;
        this.baseUri = baseUri;
        HTMLFilesParsed = false;
    }

    private String getTitle(Document doc)
    {
        String title = doc.title();
        return title;
    }

    private String getKeywords(Document doc)
    {
        Element keywords = doc.selectFirst("meta[name=keywords]");
        String keywordsString = "";
        if (keywords == null) {
        } else {
            keywordsString = keywords.attr("content");
        }
        return keywordsString;
    }

    private String getDescription(Document doc)
    {
        Element description = doc.selectFirst("meta[name=description]");
        String descriptionString = "";
        if (description == null) {
        } else {
            descriptionString = description.attr("content");
        }
        return descriptionString;
    }

    private String getRobots(Document doc)
    {
        Element robots = doc.selectFirst("meta[name=robots]");
        String robotsString = "";
        if (robots == null) {
            System.out.println("Nu exista tag-ul <meta name=\"robots\">!");
        } else {
            robotsString = robots.attr("content");
            // System.out.println("Lista de robots a site-ului a fost preluata!");
        }
        return robotsString;
    }

    private Set<String> getLinks(Document doc) throws IOException
    {
        Elements links = doc.select("a[href]");
        Set<String> URLs = new HashSet<String>();
        for (Element link : links) {
            String absoluteLink = link.attr("abs:href");
            if (absoluteLink.contains(baseUri))
            {
                continue;
            }

            int anchorPosition = absoluteLink.indexOf('#');
            if (anchorPosition != -1)
            {
                StringBuilder tempLink = new StringBuilder(absoluteLink);
                tempLink.replace(anchorPosition, tempLink.length(), "");
                absoluteLink = tempLink.toString();
            }
            URLs.add(absoluteLink);
        }
        return URLs;
    }

    private String getTextFromHTML(Document doc, File html) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getTitle(doc));
        sb.append(System.lineSeparator());
        sb.append(getKeywords(doc));
        sb.append(System.lineSeparator());
        sb.append(getDescription(doc));
        sb.append(System.lineSeparator());
        sb.append(doc.body().text());
        String text = sb.toString();

        StringBuilder textFileNameBuilder = new StringBuilder(html.getAbsolutePath());

        if (textFileNameBuilder.indexOf("?") != -1)
        {
            textFileNameBuilder.append(".txt");
        }
        else
        {
            textFileNameBuilder.replace(textFileNameBuilder.lastIndexOf(".") + 1, textFileNameBuilder.length(), "txt");
        }
        String textFileName = textFileNameBuilder.toString();

        FileWriter fw = new FileWriter(new File(textFileName), false);
        fw.write(text);
        fw.close();

        return textFileName;
    }

    private List<String> processText(String fileName) throws IOException
    {
        List<String> wordList = new LinkedList<>();

        FileReader inputStream = null;
        inputStream = new FileReader(fileName);

        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = inputStream.read()) != -1)
        {
            if (!Character.isLetterOrDigit((char)c))
            {
                String newWord = sb.toString();

                if (ExceptionList.exceptions.contains(newWord))
                {
                    if (!wordList.contains(newWord))
                    {
                        wordList.add(newWord);
                    }
                }
                else if (StopWordList.stopwords.contains(newWord))
                {
                    sb.setLength(0);
                    continue;
                }
                else
                {

                    if (!wordList.contains(newWord))
                    {
                        wordList.add(newWord);
                    }
                }

                sb.setLength(0);
            }
            else
            {
                sb.append((char)c);
            }
        }

        wordList.remove("");

        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileName + ".words"), "utf-8"));
        for (String word : wordList)
        {
            writer.write(word + "\n");
        }
        writer.close();

        inputStream.close();

        return wordList;
    }

    public void getTextFromHTMLFiles() throws IOException
    {
        LinkedList<String> folderQueue = new LinkedList<>();

        folderQueue.add(websiteFolder);

        while (!folderQueue.isEmpty())
        {
            String currentFolder = folderQueue.pop();
            File folder = new File(currentFolder);
            File[] listOfFiles = folder.listFiles();

            try {
                for (int i = 0; i < listOfFiles.length; i++)
                {
                    File file = listOfFiles[i];

                    if (file.isFile() && Files.probeContentType(file.toPath()).equals("text/html"))
                    {
                        Document doc = Jsoup.parse(file, null, baseUri);

                        getTextFromHTML(doc, file);

                        System.out.println("Am procesat fisierul HTML \"" + file.getAbsolutePath() + "\".");
                    }
                    else if (file.isDirectory())
                    {
                        folderQueue.add(file.getAbsolutePath());
                    }
                }
            } catch (NullPointerException e) {
                System.out.println("Nu exista fisiere in folderul \"" + currentFolder + "\"!");
            }
        }

        HTMLFilesParsed = true;
    }

    public void getWordsFromTextFiles() throws IOException
    {
        if (!HTMLFilesParsed)
        {
            getTextFromHTMLFiles();
        }

        LinkedList<String> folderQueue = new LinkedList<>();

        folderQueue.add(websiteFolder);

        while (!folderQueue.isEmpty())
        {
            String currentFolder = folderQueue.pop();
            File folder = new File(currentFolder);
            File[] listOfFiles = folder.listFiles();

            try {
                for (int i = 0; i < listOfFiles.length; i++)
                {
                    File file = listOfFiles[i];

                    if (file.isFile() && file.getAbsolutePath().endsWith(".txt"))
                    {
                        processText(file.getAbsolutePath());
                        System.out.println("Am procesat fisierul TEXT \"" + file.getAbsolutePath() + "\".");
                    }
                    else if (file.isDirectory())
                    {
                        folderQueue.add(file.getAbsolutePath());
                    }
                }
            } catch (NullPointerException e) {
                System.out.println("Nu exista fisiere in folderul \"" + currentFolder + "\"!");
            }
        }
    }
}
