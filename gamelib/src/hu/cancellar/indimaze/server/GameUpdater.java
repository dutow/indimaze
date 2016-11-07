package hu.cancellar.indimaze.server;

import com.google.common.collect.HashMultimap;
import hu.cancellar.indimaze.map.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

public class GameUpdater implements Runnable {

  private Game game;
  private ClientRegistry clients;

  public GameUpdater(final Game game, final ClientRegistry clients) {
    this.game = game;
    this.clients = clients;
  }

  @Override
  public void run() {
    try {

      runUpdateLoop();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }

  }

  private void runUpdateLoop() throws InterruptedException {
    while (true) {

      updateGame();

      for (final ClientConnection cc : this.clients.getActiveClients()) {
        if(cc!=null) cc.sendMap();
      }

      Thread.sleep(1000);
    }
  }

  private synchronized void updateGame() {
    System.out.println("Tick ...");
    final TurnContext tc = new TurnContext();
    wamtsToMoveTo(tc);
    doActions(tc);
    doSteps(tc);
    updateRanks();
  }

  private void updateRanks() {
    ArrayList<Player> players = game.getRealPlayers();
    Collections.sort(players, new Comparator<Player>() {
      @Override
      public int compare(Player p1, Player p2) {
        return p2.getWeightedCount() - p1.getWeightedCount();
      }
    });
    int size = players.size();
    for(int i =0; i < size;++i) {
      players.get(i).setRanking((int) Math.round(Math.floor((size-i) * 64.f  / size)));
    }
  }

  private void wamtsToMoveTo(final TurnContext tc) {
    this.game.forAllTiles(new Game.TileFunctor() {
      @Override
      public void run(final int levelId, final Coordinate c, final Level l, final MapObject mo) {
        if (mo instanceof Player) {
          final Player p = (Player) mo;
          p.UpdateRealAction();

          final Coordinate next = new Coordinate(c.x, c.y);

          switch (p.getRealAction()) {
            case UP:
              next.y++;
              break;
            case DOWN:
              next.y--;
              break;
            case LEFT:
              next.x--;
              break;
            case RIGHT:
              next.x++;
              break;
          }
          if (!next.equals(c)) {
            tc.addAction(p, new LevelAndCoord(levelId, next));
          }
          p.ClearAction();
        }
      }
    });
  }

  private void doActions(final TurnContext tc) {
    for (final LevelAndCoord c : tc.actions.keySet()) {
      final Level l = this.game.getLevels().get(c.level);
      if (l.valid(c.c) && l.at(c.c) instanceof Player) {
        final Player p = (Player) l.at(c.c);
        boolean died = false;
        int playerCount = tc.actions.get(c).size();
        for (final Player pp : tc.actions.get(c)) {
          died = p.hitBy(pp, l, c.c);
        }
        if (died) {
          for (final Player pp : tc.actions.get(c)) {
            pp.incKillCount();
            pp.incLife(Math.round(p.getMaxhealth() / (playerCount + 3.0f)));
          }
        }
      }
    }
  }

  private void doSteps(final TurnContext tc) {
    for (final LevelAndCoord c : tc.actions.keySet()) {
      final Set<Player> targetingThis = tc.actions.get(c);
      if (targetingThis.size() == 1) { // otherwise: ignore
        final Level l = this.game.getLevels().get(c.level);
        for (final Player pp : targetingThis) {
          l.placePlayer(pp, c.c);
        }
      }
    }
  }

  private class LevelAndCoord {
    int level;
    Coordinate c;

    public LevelAndCoord(final int level, final Coordinate c) {
      this.level = level;
      this.c = c;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final LevelAndCoord that = (LevelAndCoord) o;

      if (this.level != that.level) return false;
      return this.c != null ? this.c.equals(that.c) : that.c == null;

    }

    @Override
    public int hashCode() {
      int result = this.level;
      result = 31 * result + (this.c != null ? this.c.hashCode() : 0);
      return result;
    }
  }

  private class TurnContext {

    HashMultimap<LevelAndCoord, Player> actions = HashMultimap.create();

    void addAction(final Player p, final LevelAndCoord next) {
      this.actions.put(next, p);
    }
  }
}
