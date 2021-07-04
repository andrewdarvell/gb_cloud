package ru.darvell.gb.cloud.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.darvell.gb.cloud.server.exceptions.CloudServerException;
import ru.darvell.gb.cloud.server.exceptions.HandlerException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ServerHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(ServerHandler.class);

    private final Socket socket;
    private final InputStream inputStream;
    private final String storagePath;

    public ServerHandler(Socket socket, String storagePath) throws CloudServerException {
        this.socket = socket;
        this.storagePath = storagePath;
        try {
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            throw new CloudServerException("Server cannot process incoming connection", e);
        }
    }

    @Override
    public void run() {
        try {
            int nameLength = readFileNameLength();
            String fileName = readFileName(nameLength);
            receiveAndStoreFile(storagePath + "\\" + fileName);
            finalizeConnection();
            logger.info("file received");
        } catch (IOException | HandlerException e) {
            logger.error(e.getMessage());
        }
    }

    private int readFileNameLength() throws IOException, HandlerException {
        byte[] buffer = new byte[4];
        if (inputStream.read(buffer) != 4) {
            throw new HandlerException("Cannot read file name length");
        }
        return byteArrayToInt(buffer);
    }

    private String readFileName(int byteLength) throws IOException {
        byte[] buffer = new byte[byteLength];
        if (inputStream.read(buffer) != byteLength) {
            throw new HandlerException("Cannot read file name");
        }
        return new String(buffer);
    }

    private void receiveAndStoreFile(String storeFilePath) throws IOException {
        byte[] buffer = new byte[1024 * 8];
        int readBytes;

        try (FileOutputStream fos = new FileOutputStream(storeFilePath)) {
            while ((readBytes = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, readBytes);
            }
            fos.flush();
        }
    }

    private void finalizeConnection() throws IOException{
        inputStream.close();
        socket.close();
    }

    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
}
