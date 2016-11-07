package hu.cancellar.indimaze.server;

import hu.cancellar.indimaze.map.Game;
import hu.cancellar.indimaze.map.ICommander;
import hu.cancellar.indimaze.map.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientConnection implements Runnable, ICommander {

  private final Socket connectionSocket;
  private final BufferedReader inFromClient;
  private final OutputStreamWriter outToClient;
  private final String clientId;
  private final Game game;
  private final Player player;
  private final CloseCallback callback;

  public ClientConnection(final Socket connectionSocket, final Game game, final CloseCallback c) throws IOException {
    this.connectionSocket = connectionSocket;
    this.inFromClient =
        new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
    this.outToClient = new OutputStreamWriter(connectionSocket.getOutputStream(), "UTF-8");
    this.clientId = this.inFromClient.readLine();
    this.game = game;
    this.player = this.game.findPlayer(this.clientId);
    this.callback = c;
    System.out.println("Client connected:" + clientId);
    sendMap();
  }

  @Override
  public void run() {
    int errors = 0;
    while (errors < 5 && this.connectionSocket.isConnected()) {
      try {
        final String clientSentence = this.inFromClient.readLine();
        System.out.println("(" + clientId + ") Received: " + clientSentence);
        if(clientSentence != null) {
          errors = 0;
          switch (clientSentence) {
            case "up":
              up();
              break;
            case "down":
              down();
              break;
            case "left":
              left();
              break;
            case "right":
              right();
              break;
          }
        } else {
          errors++;
        }
      } catch (final IOException e) {
        errors++;
      }
    }
    player.setCurrentAction(Player.ActionType.NPC);
    this.callback.close(this);
  }

  public void sendMap() {
    try {
      Player p = this.game.findPlayer(this.clientId);
      this.outToClient.write("" + 7 + "\n");
      this.outToClient.write(this.game.aroundPlayer(this.clientId));
      this.outToClient.write(p.getMaxhealth() + "\n");
      this.outToClient.write(p.getHealth()+"\n");
      this.outToClient.write(p.getRanking()+"\n");
      this.outToClient.flush();
    } catch (final IOException e) {
      this.callback.close(this);
    }
  }

  @Override
  public void up() {
    this.player.setCurrentAction(Player.ActionType.UP);
  }

  @Override
  public void down() {
    this.player.setCurrentAction(Player.ActionType.DOWN);
  }

  @Override
  public void left() {
    this.player.setCurrentAction(Player.ActionType.LEFT);
  }

  @Override
  public void right() {
    this.player.setCurrentAction(Player.ActionType.RIGHT);
  }

  interface CloseCallback {
    void close(ClientConnection c);
  }
}
