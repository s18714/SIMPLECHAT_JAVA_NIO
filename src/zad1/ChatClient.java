/**
 * @author Kryzhanivskyi Denys S18714
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatClient {
    private final InetSocketAddress inetSocketAddress;
    private final StringBuilder clientView;
    private final String clientId;
    private SocketChannel channel;
    private final Thread receivingThread = new Thread(this::run);

    private final Lock lock = new ReentrantLock();

    public ChatClient(String host, int port, String id) {
        this.inetSocketAddress = new InetSocketAddress(host, port);
        this.clientId = id;
        clientView = new StringBuilder("=== " + clientId + " chat view\n");
    }

    public void login() {
        try {
            channel = SocketChannel.open(inetSocketAddress);
            channel.configureBlocking(false);
            while (!channel.finishConnect()) {

            }
            send("log in " + clientId);
            receivingThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        send("log out" + "#");
        try {
            lock.lock();
            receivingThread.interrupt();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void send(String req) {
        try {
            Thread.sleep(30);
            channel.write(StandardCharsets.UTF_8.encode(req + "#"));
            Thread.sleep(30);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getChatView() {
        return clientView.toString();
    }

    private void run() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        int bytesRead = 0;

        while (!receivingThread.isInterrupted()) {
            do {
                try {
                    lock.lock();
                    bytesRead = channel.read(buffer);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    lock.unlock();
                }
            } while (bytesRead == 0 && !receivingThread.isInterrupted());

            buffer.flip();
            String response = StandardCharsets.UTF_8.decode(buffer).toString();
            clientView.append(response);
            buffer.clear();
        }
    }
}