package hu.cancellar.indimaze.server;

import java.util.LinkedList;

public class ClientRegistry {
  private LinkedList<ClientConnection> activeClients;

  ClientRegistry() {
    this.activeClients = new LinkedList<>();
  }

  public synchronized LinkedList<ClientConnection> getActiveClients() {
    return new LinkedList<>(this.activeClients);
  }

  public synchronized void startClient(final ClientConnection c) {
    this.activeClients.add(c);
    new Thread(c).start();
  }

  public synchronized ClientConnection.CloseCallback closeCallback() {
    return new ClientConnection.CloseCallback() {
      @Override
      public void close(final ClientConnection c) {
        ClientRegistry.this.activeClients.remove(c);
      }
    };
  }
}
