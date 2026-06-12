import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {
    public static void main(String[] args) throws Exception {

        // Reusable client for sending HTTP requests
        HttpClient client = HttpClient.newHttpClient();

        // Build a GET request to fetch the list of users from the JSONPlaceholder API
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://jsonplaceholder.typicode.com/users"))
                .GET()
                .build();

        // Send the request and read the response body as a plain string
        HttpResponse<String> response =
                client.send(request,
                        HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
    }
}