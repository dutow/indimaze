package com.mygdx.indimaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import hu.cancellar.indimaze.map.ICommander;
import hu.cancellar.indimaze.map.Level;
import hu.cancellar.indimaze.map.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NetworkCommander implements ICommander {


  static private final String serverUrl = "178.62.136.176";
  final Lock lock = new ReentrantLock();
  final Condition cdSend = this.lock.newCondition();
  private final String clientId;
  private Socket socket;
  private OutputStreamWriter wrt;
  private BufferedReader inStream;
  private GameState gs;

  private Thread outputThread;
  private Thread incomingListener;
  private volatile boolean running;

  String readline() throws IOException {
    String str = NetworkCommander.this.inStream.readLine();
    //System.out.println(str);
    return str;
  }

  NetworkCommander(final String clientId) {
    this.gs = new GameState();
    this.running = true;

    this.clientId = clientId;

    try {
      connect();

      this.incomingListener = new Thread(new Runnable() {
        @Override
        public void run() {
          while (NetworkCommander.this.running) {
            try {
              final String fromServer;
              fromServer = readline();
              final int lines = Integer.parseInt(fromServer);
              final StringBuilder map = new StringBuilder();
              for (int i = 0; i < lines; ++i) {
                map.append(readline());
                map.append('\n');
              }
              NetworkCommander.this.gs.currentLevelAround = new Level(map.toString());
              NetworkCommander.this.gs.nextAction = Player.ActionType.NOPE;
              gs.maxhp = Integer.parseInt(readline());
              gs.hp  = Integer.parseInt(readline());
              gs.onepercent = Integer.parseInt(readline());
              if (gs.onepercent > 64) gs.onepercent = 64;
                if (gs.onepercent < 0) gs.onepercent = 0;
            } catch (final IOException e) {
              e.printStackTrace();
              ensureConnection();
            } catch (final NumberFormatException e) {
              System.out.println("NPE");
            e.printStackTrace();
            ensureConnection();
          }
          }
        }
      });
      this.incomingListener.start();

      this.outputThread = new Thread(new Runnable() {
        @Override
        public void run() {
          while (NetworkCommander.this.running) {
            NetworkCommander.this.lock.lock();
            try {
              NetworkCommander.this.cdSend.await();
            } catch (final InterruptedException e) {
              e.printStackTrace();
            }
            if (NetworkCommander.this.gs.nextAction != Player.ActionType.NOPE) {
              try {
                NetworkCommander.this.wrt.write(serialzieAction(NetworkCommander.this.gs.nextAction));
                NetworkCommander.this.wrt.flush();
              } catch (final IOException e) {
                ensureConnection();
                e.printStackTrace();
              }
            }
            NetworkCommander.this.lock.unlock();
          }
        }
      });
      this.outputThread.start();

    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private String serialzieAction(final Player.ActionType nextAction) {
    switch (nextAction) {
      case UP:
        return "up\n";
      case DOWN:
        return "down\n";
      case LEFT:
        return "left\n";
      case RIGHT:
        return "right\n";
    }
    return "wtf\n";
  }

  private void connect() throws IOException {
    this.socket = Gdx.net.newClientSocket(Net.Protocol.TCP, serverUrl, 1337, new SocketHints());
    this.wrt = new OutputStreamWriter(this.socket.getOutputStream(), "UTF-8");
    this.wrt.write(this.clientId + '\n');
    this.wrt.flush();
    this.inStream = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
  }

  private void ensureConnection() {
      try {
        System.out.println(".");
        Thread.sleep(2000);
        connect();
        System.out.println("Reconnected");
      } catch (final IOException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
  }

  @Override
  public synchronized void up() {
    changeActionTo(Player.ActionType.UP);
  }

  @Override
  public synchronized void down() {
    changeActionTo(Player.ActionType.DOWN);
  }

  @Override
  public synchronized void left() {
    changeActionTo(Player.ActionType.LEFT);
  }

  @Override
  public synchronized void right() {
    changeActionTo(Player.ActionType.RIGHT);
  }

  private void changeActionTo(final Player.ActionType act) {
    this.gs.nextAction = act;
    this.lock.lock();
    this.cdSend.signal();
    this.lock.unlock();
  }

  public GameState getGameState() {
    return this.gs;
  }

  public synchronized void dispose() {
    this.running = false;
    this.gs.nextAction = Player.ActionType.NOPE;
    this.lock.lock();
    this.cdSend.signal();
    this.lock.unlock();
    try {
      this.outputThread.join();
      this.incomingListener.join();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }
}
