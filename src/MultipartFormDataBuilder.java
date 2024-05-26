import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MultipartFormDataBuilder {
    private final String boundary;
    private final ByteArrayOutputStream outputStream;

    public MultipartFormDataBuilder(String boundary) {
        this.boundary = boundary;
        this.outputStream = new ByteArrayOutputStream();
    }

    public MultipartFormDataBuilder addFormField(String name, String value) throws IOException {
        outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Type: text/plain; charset=UTF-8\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write((value + "\r\n").getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public MultipartFormDataBuilder addFilePart(String fieldName, String fileName, byte[] fileContent, String mimeType) throws IOException {
        outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(fileContent);
        outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public ByteBuffer build() throws IOException {
        outputStream.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return ByteBuffer.wrap(outputStream.toByteArray());
    }

    public static void main(String[] args) throws IOException {
        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        MultipartFormDataBuilder builder = new MultipartFormDataBuilder(boundary);

        // Add a form field
        builder.addFormField("username", "test_user");

        // Add a file part
        String fileName = "test.txt";
        byte[] fileContent = "This is a test file.".getBytes(StandardCharsets.UTF_8);
        builder.addFilePart("file", fileName, fileContent, "text/plain");

        // Build the request body
        ByteBuffer requestBody = builder.build();

        // Print the result (for demonstration purposes)
        System.out.println(StandardCharsets.UTF_8.decode(requestBody).toString());
    }
}
