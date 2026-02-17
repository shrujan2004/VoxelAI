import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

public class AIClient {

    private static final String AI_URL = "http://127.0.0.1:8000/ask";
    private final HttpClient client = HttpClient.newHttpClient();

    public String askAI(String userMessage) {

        try {
            String safeMessage = userMessage
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"");

            String jsonBody = "{ \"message\": \"" + safeMessage + "\" }";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AI_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("⚠️ AI unavailable (status " + response.statusCode() + ")");
                System.out.println("⚠️ Reason: " + response.body());
                return null; // IMPORTANT: do NOT crash
            }

            return response.body();

        } catch (Exception e) {
            System.out.println("⚠️ AI connection failed: " + e.getMessage());
            return null;
        }
    }
}
