package webscraper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Translator {

    // Add your API credentials here
    private static final String API_KEY = "aa2fee34ecmsh811794f29f92faep19c484jsn9aa33c07c42f";
    private static final String API_HOST = "google-translate113.p.rapidapi.com";
    private static final String ENDPOINT = "https://" + API_HOST + "/api/v1/translator/json";

    public static String translateToEnglish(String spanishText) {
        try {
            String jsonBody = """
                    {
                        "from": "auto",
                        "to": "en",
                        "json": {
                            "text": "%s"
                        }
                    }
                    """.formatted(spanishText.replace("\"", "\\\""));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("x-rapidapi-key", API_KEY)
                    .header("x-rapidapi-host", API_HOST)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String body = response.body();
           // System.out.println("🔍 API Response:\n" + body);  // DEBUG print


            int index = body.indexOf("\"text\":\"");
            if (index != -1) {
                String partial = body.substring(index + 8);
                return partial.substring(0, partial.indexOf("\""));
            }

            return "Translation not found";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error translating text";
        }
    }
}
