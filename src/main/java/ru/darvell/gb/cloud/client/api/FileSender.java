package ru.darvell.gb.cloud.client.api;

import ru.darvell.gb.cloud.client.ProgressBarUpdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class FileSender implements Runnable {

    private final ProgressBarUpdater progressBarUpdater;
    private final SendFinisher sendFinisher;
    private final File selectedFile;
    private final long fileLength;

    public FileSender(ProgressBarUpdater progressBarUpdater, SendFinisher sendFinisher, File selectedFile) {
        this.progressBarUpdater = progressBarUpdater;
        this.sendFinisher = sendFinisher;
        this.selectedFile = selectedFile;
        this.fileLength = selectedFile.length();
    }

    @Override
    public void run() {
        try (Socket socket = new Socket("localhost", 8081)) {
            try (OutputStream outputStream = socket.getOutputStream()) {
                writeFileNameDataTo(outputStream);
                sendFile(outputStream);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        sendFinisher.finish();
    }

    private void writeFileNameDataTo(OutputStream outputStream) throws IOException {
        String fileName = selectedFile.getName();
        outputStream.write(intToByteArray(fileName.getBytes().length));
        outputStream.write(fileName.getBytes());
    }

    private void sendFile(OutputStream outputStream) throws IOException {
        long bytesSend = 0;
        byte[] buffer = new byte[1024 * 8];
        try (FileInputStream fis = new FileInputStream(selectedFile)) {
            int readBytes;
            while ((readBytes = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readBytes);
                bytesSend += readBytes;
                updateProgress(bytesSend);
            }
            outputStream.flush();
        }
    }

    private void updateProgress(long bytesSend) {
        progressBarUpdater.setProgress((double)bytesSend / fileLength);
    }

    public static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }
}
