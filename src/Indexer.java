import java.io.IOException;

public class Indexer {
    public static void main(String[] args) throws IOException {
        Laborator2 l2 = new Laborator2("./ietf.org/", "http://ietf.org/");
        l2.getWordsFromTextFiles();
    }
}
