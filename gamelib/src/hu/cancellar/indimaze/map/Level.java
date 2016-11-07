package hu.cancellar.indimaze.map;

import com.google.common.collect.*;

import java.util.Random;

public class Level {

  private static Random rand = new Random();
  private Table<Integer, Integer, MapObject> map;

  public Level(final String serialized) {
    final String[] lines = serialized.split("\r\n|\r|\n");
    final ContiguousSet<Integer> rows = ContiguousSet.create(Range.closed(0, lines[0].length()), DiscreteDomain.integers());
    final ContiguousSet<Integer> cols = ContiguousSet.create(Range.closed(0, lines.length), DiscreteDomain.integers());
    this.map = ArrayTable.create(rows, cols);

    final ObjectCoder coder = new ObjectCoder();

    for (int iy = 0; iy < lines.length; ++iy) {
      final String line = lines[iy];
      for (int ix = 0; ix < line.length(); ++ix) {
        this.map.put(ix, iy, coder.fromCharacter(line.charAt(ix)));
      }
    }
  }

  public hu.cancellar.indimaze.map.MapObject at(final hu.cancellar.indimaze.map.Coordinate c) {
    return this.map.get(c.x, c.y);
  }

  public Player hasClient(final String clientId) {
    for (final MapObject mo : this.map.values()) {
      if (mo instanceof Player && ((Player) mo).getId().equals(clientId)) {
        return (Player) mo;
      }
    }
    return null;
  }

  public boolean add(final Coordinate c, final MapObject mo) {
    this.map.put(c.x, c.y, mo);
    return true;
  }

  public String dumpAround(final Coordinate coord) {

    final ObjectCoder coder = new ObjectCoder();
    final int radius = 3;
    final int diameter = radius * 2 + 1;

    final char[] data = new char[(diameter + 1) * diameter];

    for (int iy = coord.y - radius; iy <= coord.y + radius; ++iy) {
      for (int ix = coord.x - radius; ix <= coord.x + radius; ++ix) {
        data[(iy + radius - coord.y) * (diameter + 1) + ix + radius - coord.x] = coder.toCharacter(this.map.get(ix, iy));
      }
      data[(iy + radius - coord.y) * (diameter + 1) + diameter] = '\n';
    }

    return new String(data);
  }

  public Coordinate clientCoord(final String clientId) {
    for (final int iy : this.map.columnKeySet()) {
      for (final int ix : this.map.rowKeySet()) {
        final MapObject mo = this.map.get(ix, iy);
        if (mo instanceof Player && ((Player) mo).getId().equals(clientId)) {
          return new Coordinate(ix, iy);
        }
      }
    }
    return null;
  }

  public int width() {
    return this.map.rowKeySet().size();
  }

  public int height() {
    return this.map.columnKeySet().size();
  }

  public Coordinate randomWithin() {
    return new Coordinate(rand.nextInt(width() - 1), rand.nextInt(height() - 1));
  }

  public Coordinate findFreeSpace() {

    int tries = 1000;

    while (tries > 0) {
      final Coordinate c = randomWithin();
      if (this.map.get(c.x, c.y) == null) return c;
      tries--;
    }

    return null;
  }

  public boolean valid(final Coordinate c) {
    return c.x >= 0 && c.y >= 0 && c.x < width() && c.y < height();
  }

  public boolean placePlayer(final Player pp, final Coordinate c) {
    if (at(c) == null) {
      final Coordinate prevC = clientCoord(pp.getId());
      if (prevC != null) {
        this.map.put(prevC.x, prevC.y, null);
      }
      this.map.put(c.x, c.y, pp);
      return true;
    }
    return false;
  }

  public void respawn(final Player p) {
    System.out.println("respawning " + p.getId());
    final int tries = 1000;

    while (tries > 0) {
      final Coordinate c = randomWithin();
      if (this.map.get(c.x, c.y) == null) {
        placePlayer(p, c);
        return;
      }
    }
  }
}
