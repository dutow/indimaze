package hu.cancellar.indimaze.server;


import hu.cancellar.indimaze.map.Game;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

  ClientRegistry clients;

  Server() throws IOException, InterruptedException {

    this.clients = new ClientRegistry();

    final Game game = new Game("maps");

    final Thread updateThread = new Thread(new GameUpdater(game, this.clients));
    updateThread.start();

    final ServerSocket welcomeSocket = new ServerSocket(1337);

    while (true) {
      final Socket connectionSocket = welcomeSocket.accept();
      this.clients.startClient(new ClientConnection(connectionSocket, game, this.clients.closeCallback()));
    }
  }

  public static void main(final String[] argv) throws Exception {
    new Server();
  }
}
