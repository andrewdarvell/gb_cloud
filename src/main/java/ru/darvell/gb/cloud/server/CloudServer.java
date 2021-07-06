package ru.darvell.gb.cloud.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.darvell.gb.cloud.server.exceptions.CloudServerException;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CloudServer {

    private static final Logger logger = LogManager.getLogger(CloudServer.class);
    private static final String STORE_FOLDER_NAME = "\\storage";

    private final String rootStorePath;

    public CloudServer() {
        rootStorePath = prepareStorageRootPath();
    }

    private String prepareStorageRootPath() {
        File rootPath = new File(System.getProperty("user.dir") + STORE_FOLDER_NAME);
        if (!rootPath.exists()) {
            rootPath.mkdir();
        }
        return rootPath.getAbsolutePath();
    }

    public void launch() {
        try (ServerSocket serverSocket = new ServerSocket(8081)) {
            logger.info("Server start and wait connection ");
            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("Connection recieved");
                processIncomingConnection(socket);
            }
        } catch (IOException e) {
            logger.info("Server don't start");
        }
    }

    private void processIncomingConnection(Socket socket) {
        try {
            new Thread(new ServerHandler(socket, rootStorePath)).start();
        } catch (CloudServerException e) {
            e.printStackTrace();
        }
    }


}
