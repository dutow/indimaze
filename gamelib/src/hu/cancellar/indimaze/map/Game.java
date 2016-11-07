package hu.cancellar.indimaze.map;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Game {

  private ArrayList<Level> levels = new ArrayList<>();
  private ArrayList<Player> realPlayers = new ArrayList<>();

  public Game(final String directory) throws IOException {
    loadLevel(directory, 1);
    //loadLevel(directory, 2);
  }

  private void loadLevel(final String directory, final int id) throws IOException {
    this.levels.add(new Level(Files.toString(new File(directory + "/" + id + ".txt"), Charsets.UTF_8)));
    for (int i = 0; i < 25; ++i) {
      createNewPlayer(null);
    }
  }

  public Player findPlayer(final String clientId) {
    for (final Level l : this.levels) {
      final Player c = l.hasClient(clientId);
      if (c != null) return c;
    }
    createNewPlayer(clientId);
    return findPlayer(clientId);
  }

  public String aroundPlayer(final String clientId) {
    for (final Level l : this.levels) {
      final Coordinate c = l.clientCoord(clientId);
      if (c != null) return l.dumpAround(c);
    }
    createNewPlayer(clientId);
    return aroundPlayer(clientId);
  }

  private <T> T randomValue(final T[] values) {
    return values[new Random().nextInt(values.length)];
  }

  private void createNewPlayer(final String clientId) {
    final Player ch = new Player(randomValue(Player.Type.values()));
    if (clientId != null) {
      ch.setId(clientId);
      realPlayers.add(ch);
    }
    final Coordinate free = this.levels.get(0).findFreeSpace();
    this.levels.get(0).add(free, ch);
  }

  public ArrayList<Player> getRealPlayers() {
    return realPlayers;
  }

  public ArrayList<Level> getLevels() {
    return this.levels;
  }

  public void forAllTiles(final TileFunctor tf) {
    for (int il = 0; il < this.levels.size(); ++il) {
      final Level l = this.levels.get(il);
      for (int iy = 0; iy < l.height(); ++iy) {
        for (int ix = 0; ix < l.width(); ++ix) {
          final Coordinate c = new Coordinate(ix, iy);
          tf.run(il, c, l, l.at(c));
        }
      }
    }
  }

  public interface TileFunctor {
    void run(int levelId, Coordinate c, Level l, MapObject mo);
  }
}
