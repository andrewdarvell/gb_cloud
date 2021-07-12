package ru.darvell.gb.cloud.netty;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

// cd *
// ls
// cat fileName
// mkdir name
// touch name
public class NioServer {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buf;
    private Path root = Paths.get("storage").toAbsolutePath();


    public NioServer() throws IOException {

        buf = ByteBuffer.allocate(256);
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8189));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (serverSocketChannel.isOpen()) {
            selector.select();
            Set<SelectionKey> keySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key);
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        buf.clear();
        StringBuilder s = new StringBuilder();
        int read;
        while (true) {
            read = channel.read(buf);
            if (read == -1) {
                channel.close();
                break;
            }
            if (read == 0) {
                break;
            }
            buf.flip();
            while (buf.hasRemaining()) {
                s.append((char) buf.get());
            }
            buf.clear();
        }
        String command = s.toString().trim();
        if ("ls".equals(command)) {
            lsCommand(channel, command);
        } else if (command.startsWith("cd")) {
            cdCommand(channel, command);
        } else if (command.startsWith("mkdir")) {
            mkdirCommand(channel, command);
        } else if (command.startsWith("touch")) {
            touchCommand(channel, command);
        } else if (command.startsWith("cat")) {
            catCommand(channel, command);
        } else {
            writeToChannel(channel, "Command not found");
        }
        printGreeting(channel);
    }

    private void catCommand(SocketChannel channel, String command) throws IOException {
        command = command.replaceAll("cat\\s+", "").trim();
        Path catPath = root.resolve(command);
        if (Files.exists(catPath)) {
            writeToChannel(channel,
                    Files.readAllLines(catPath)
                            .stream()
                            .collect(Collectors.joining("\n\r"))
            );
        } else {
            writeToChannel(channel, "File not exist");
        }
    }

    private void lsCommand(SocketChannel channel, String command) throws IOException {
        String childs = Files.list(root)
                .map(ch -> ch.getFileName().toString())
                .collect(Collectors.joining("\n\r")) + "\n\r";
        writeToChannel(channel, childs);
    }

    private void cdCommand(SocketChannel channel, String command) throws IOException {
        command = command.replaceAll("cd\\s+", "").trim();

        if ("..".equals(command)) {
            root = root.getParent();
            System.out.println(root);
        } else if (Files.isDirectory(root.resolve(command))) {
            root = root.resolve(command).toAbsolutePath();
            System.out.println(root);
        } else {
            writeToChannel(channel, command + " is not directory");
        }
    }

    private void mkdirCommand(SocketChannel channel, String command) throws IOException {
        command = command.replaceAll("mkdir\\s+", "").trim();
        Path mkdirPath = root.resolve(command);
        if (!Files.exists(mkdirPath)) {
            Files.createDirectory(mkdirPath);
            writeToChannel(channel, "Directory created: " + mkdirPath.toString());
        } else {
            writeToChannel(channel, "Directory exist");
        }
    }

    private void touchCommand(SocketChannel channel, String command) throws IOException {
        command = command.replaceAll("touch\\s+", "").trim();
        Path touchPath = root.resolve(command);
        if (!Files.exists(touchPath)) {
            Files.createFile(touchPath);
            writeToChannel(channel, "File created: " + touchPath.toString());
        } else {
            writeToChannel(channel, "File exist");
        }
    }


    private void printGreeting(SocketChannel channel) {
        writeToChannel(channel, root.toString() + "> ");
    }

    private void writeToChannel(SocketChannel channel, String s) {
        try {
            ByteBuffer response = ByteBuffer.wrap((s + "\n\r").getBytes(StandardCharsets.UTF_8));
            channel.write(response);
        } catch (IOException e) {
            System.out.println("Channel closed");
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = serverSocketChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);

    }

    public static void main(String[] args) throws IOException {
        new NioServer();
    }
}
