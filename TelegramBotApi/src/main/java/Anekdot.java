import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Scanner;

public class Anekdot {
    public static String getAnekdot() throws IOException {
        URL url = new URL("https://icanhazdadjoke.com/slack");
        Scanner in = new Scanner((InputStream) url.getContent());
        String result = "";
        while (in.hasNext()) {
            result += in.nextLine();
        }
        JSONObject jsonObject = new JSONObject(result);
        JSONArray jsonArray = jsonObject.getJSONArray("attachments");
        Iterator phonesItr = jsonArray.iterator();
        JSONObject test = (JSONObject) phonesItr.next();
        return (String)test.get("text");
    }
}
