package ru.darvell.gb.cloud;

import ru.darvell.gb.cloud.server.CloudServer;

public class ServerLauncher {

    public static void main(String[] args) {
        new CloudServer().launch();
    }
}
