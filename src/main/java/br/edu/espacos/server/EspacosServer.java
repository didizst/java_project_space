package br.edu.espacos.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EspacosServer {
    private static final int PORT = 12346;

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor Iniciado");
            while (true) {
                Socket clientSocket = serverSocket.accept();

                new Thread(new ClientHandlerEspacos(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());

        }
    }
}


