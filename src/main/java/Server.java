import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    private ServerSocket serverSocket;
    private final int port = 9999;
    private List<String> validParths = List.of("/index.html", "/spring.svg", "/spring.png");
    private ExecutorService executorService = Executors.newFixedThreadPool(64);

    public Server() throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public void startServer() {
        for (int i = 0; i < 5; i++) {
            executorService.submit(new Thread(() -> {
                try (Socket socket = serverSocket.accept();) {
                    readReaquest(socket);
                    Thread.sleep(10000);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));
        }
    }


    private void readReaquest(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            String reaquestLine = in.readLine();
            String[] parts = reaquestLine.split(" ");
            if (parts.length == 3) {
                String path = parts[1];

                if (!validParths.contains(path)) {
                    out.write((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Lenght: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                } else {
                    Path filePath = Path.of(".", "public", path);
                    String mimeType = Files.probeContentType(filePath);
                    Long length = Files.size(filePath);
                    out.write((
                            "HTTP/1.1 200 Ok\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Lenght: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    Files.copy(filePath, out);
                    out.flush();
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

