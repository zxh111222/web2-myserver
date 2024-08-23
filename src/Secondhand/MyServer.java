package Secondhand;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.*;

import static Secondhand.MyDateConversionUtil.formatDate;

public class MyServer {

    public static void main(String[] args) throws IOException {
        try (ServerSocket ss = new ServerSocket(8080)) {
            System.out.println("服务器启动...");
            while (true) {
                Socket sock = ss.accept();
                System.out.println("接收到客户端链接：" + sock.getRemoteSocketAddress());
                Thread myThread = new MyHandler(sock);
                myThread.start();
            }
        }
    }
}

class MyHandler extends Thread {
    private final Socket sock;

    public MyHandler(Socket sock) {
        this.sock = sock;
    }

    public void run() {
        try (InputStream input = this.sock.getInputStream()) {
            try (OutputStream output = this.sock.getOutputStream()) {
                handle(input, output);
            }
        } catch (Exception e) {
            try {
                this.sock.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            System.out.println("client disconnected.");
        }
    }

    private void handle(InputStream input, OutputStream output) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        boolean requestOk = false;
        String first = reader.readLine();
        if (first.startsWith("GET / HTTP/1.")) {
            requestOk = true;
        }
        for (; ; ) {
            String header = reader.readLine();
            if (header.isEmpty()) {
                break;
            }
            System.out.println(header);
        }
        System.out.println(requestOk ? "Response OK" : "Response Error");
        if (!requestOk) {
            writer.write("HTTP/1.0 404 Not Found\r\n");
            writer.write("Content-Length: 0\r\n");
            writer.write("\r\n");
            writer.flush();
        } else {
            // 要返回的 HTML
            String body = getSecondhandGoods();
            // 发送成功响应:
            writer.write("HTTP/1.1 200 OK\r\n");
            writer.write("Connection: keep-alive\r\n");
            writer.write("Content-Type: text/html\r\n");
            writer.write("Content-Length: " + body.length() + "\r\n");
            writer.write("\r\n"); // 空行标识Header和Body的分隔
            writer.write(body);
            writer.flush();
        }

        System.out.println("=== === ===");
    }

    private String getSecondhandGoods() {
        StringBuilder htmlInfo = new StringBuilder();

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/app", "root", "123456");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT title, url, created_time, updated_time FROM information");

            while (rs.next()) {
                String title = rs.getString("title");
                String url = rs.getString("url");
                String created_time = formatDate(rs.getTimestamp("created_time"));
                String updated_time = formatDate(rs.getTimestamp("updated_time"));

                CustomResult article = new CustomResult(title, url, created_time, updated_time);

                htmlInfo.append("""
                <tr>
                    <th><a href="%s">%s</a></th>
                    <th>%s</th>
                    <th>%s</th>
                </tr>
                """.formatted(article.getUrl(), article.getTitle(), article.getCreatedAt(), article.getUpdatedAt()));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database error: " + e.getMessage());
        }

        String body = """
                    <!DOCTYPE html>
                    <html lang="zh">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>二手商品列表</title>
                        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
                    </head>
                    <body>
                        <div class="container my-5">
                        <h2 class="mb-4">物尽其用</h2>
                        <table class="table table-bordered border-secondary-subtle table-striped table-hover">
                            <caption>二手商品买卖信息表</caption>
                            <thead class="table-secondary">
                                <tr>
                                    <td>标题</td>
                                    <td>创建时间</td>
                                    <td>更新时间</td>
                                </tr>
                            </thead>
                            <tbody class="table-group-divider">
                            %s
                            </tbody>
                        </table>
                    </body>
                    </html>
                    """.formatted(htmlInfo.toString());


        return body;
    }
}