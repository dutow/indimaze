package hu.cancellar.indimaze.map;

public class ObjectCoder {

  public MapObject fromCharacter(final char c) {
    switch (c) {
      case '#': {
        return new Wall();
      }
      case 'f': {
        return new Player(Player.Type.FAIRY);
      }
      case 'd': {
        return new Player(Player.Type.FARMER);
      }
      case 'c': {
        return new Player(Player.Type.COW);
      }
    }

    if (c >= '1' && c <= '9') {
      // TODO: gate
    }

    return null;
  }

  public char toCharacter(final MapObject mo) {
    if (mo instanceof Wall) {
      return '#';
    }
    if (mo instanceof Player) {
      final Player c = (Player) mo;
      switch (c.characterType) {
        case COW:
          return 'c';
        case FAIRY:
          return 'f';
        case FARMER:
          return 'd';
      }
    }
    return ' ';
  }

}
